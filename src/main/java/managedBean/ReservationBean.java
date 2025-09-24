package managedBean;

import entities.Article;
import entities.ExemplaireArticle;
import entities.Magasin;
import entities.Reservation;
import entities.Utilisateur;
import enumeration.ReservationStatutEnum;
import org.apache.log4j.Logger;
import services.SvcExemplaireArticle;
import services.SvcReservation;
import services.SvcUtilisateur;
import tools.MailUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Bean UI pour la gestion des réservations.
 * - Création (mise en file)
 * - Annulation / Expiration (staff)
 * - Promotion (mise de côté + notification)
 */
@Named
@SessionScoped
public class ReservationBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ReservationBean.class);

    @Inject
    private MagasinBean magasinSession;

    // Table
    private List<Reservation> listeReservation;

    // Formulaire
    private String  numMembre;     // code-barres client
    private Article article;       // article visé
    private Reservation selected;  // sélection de table
    private Date    dateFin;       // utilisée lors de la validation → facture


    @PostConstruct
    public void init() {
        refresh();
        numMembre = "";
        article   = new Article();       // objet toujours présent (id null => non choisi)
        selected  = new Reservation();
        dateFin   = null;
    }

    /** Recharge la table. */
    public void refresh() {
        SvcReservation svc = new SvcReservation();
        try { listeReservation = svc.findAll(); }
        finally { svc.close(); }
    }

    public Date getTomorrow() {
        // Demain à 00:00 dans la TZ Europe/Brussels
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Europe/Brussels"));
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public String flushReservation() {
        init();
        return "tableReservation.xhtml?faces-redirect=true";
    }

    public String flushBienvenue(){
        init();
        return "bienvenue.xhtml?faces-redirect=true";
    }

    /** Crée une réservation en file (client via code-barres + magasin de session). */
    public String newReservation() {
        SvcReservation service  = new SvcReservation();
        SvcUtilisateur serviceU = new SvcUtilisateur();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();


        try {
            serviceU.setEm(service.getEm()); // partager EM
            serviceEA.setEm(service.getEm());


            if (article == null || article.getId() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Article manquant", null));
                return null;
            }
            Magasin magasin = (magasinSession != null) ? magasinSession.getMagasin() : null;
            if (magasin == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magasin manquant", null));
                return null;
            }
            if (serviceEA.countAvailableExArticlesRentNotReserved(article, magasin)>=1) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Article disponible immédiatement en magasin",
                                "Passez plutôt par la création d’une facture de location (réservation inutile)."));
                return null;
            }
            EntityTransaction tx = service.getTransaction();
            tx.begin();
            try {
                List<Utilisateur> found = serviceU.getByNumMembre(numMembre);
                if (found == null || found.isEmpty()) {
                    tx.rollback();
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Aucun client pour ce code-barres", null));
                    return null;
                }
                Utilisateur user = found.get(0);

                service.create(user, magasin, article);
                tx.commit();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Réservation créée (file)", null));
                return "tableReservation.xhtml?faces-redirect=true";
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur création réservation", null));
                return null;
            }
        } finally {
            service.close();
            refresh();
        }
    }

    /** Annule (staff) : libère l’exemplaire réservé, puis statut=annule. Tente une promotion après commit. */
    public void cancelReservation(Reservation r) {
        if (r == null || r.getId() == null) return;

        SvcReservation       service  = new SvcReservation();
        SvcExemplaireArticle svcEA = new SvcExemplaireArticle();
        svcEA.setEm(service.getEm()); // même EM

        ExemplaireArticle freed = null;

        EntityTransaction tx = service.getTransaction();
        tx.begin();
        try {
            ExemplaireArticle ea = r.getExemplaire();
            if (ea != null) {
                ea.setReserve(false);
                svcEA.save(ea);
                freed = ea;
            }
            service.markAnnule(r);
            tx.commit();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Réservation annulée", null));
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur à l'annulation", null));
        } finally {
            service.close();
            refresh();
        }

        if (freed != null) {
            promouvoirSiPossible(freed); // message détaillé (cas unitaire)
        }
    }

    /** Expire (staff) : libère l’exemplaire réservé, puis statut=expire. Tente une promotion après commit. */
    public void expireReservation(Reservation r) {
        if (r == null || r.getId() == null) return;
        if (r.getStatut() != ReservationStatutEnum.pret) return;

        SvcReservation       service  = new SvcReservation();
        SvcExemplaireArticle svcEA = new SvcExemplaireArticle();
        svcEA.setEm(service.getEm());

        ExemplaireArticle freed = null;

        EntityTransaction tx = service.getTransaction();
        tx.begin();
        try {
            ExemplaireArticle ea = r.getExemplaire();
            if (ea != null) {
                ea.setReserve(false);
                svcEA.save(ea);
                freed = ea;
            }
            service.markExpire(r);
            tx.commit();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Réservation expirée", null));
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur à l'expiration", null));
        } finally {
            service.close();
            refresh();
        }

        if (freed != null) {
            promouvoirSiPossible(freed); // message détaillé (cas unitaire)
        }
    }

    /* =========================================================
     *  Helpers de PROMOTION — centralisés ici
     * ========================================================= */

    /**
     * Promotion autonome (cas unitaire, post-commit ailleurs).
     * - transaction pour pose des drapeaux (reserve=true, statut=pret, dates)
     * - Envoie le mail après commit
     * - Pose mailEnvoye=true dans une mini transaction si mail envoyé
     * - Ajoute un growl détaillé pour le staff (CB, article, client, limite)
     */
    public void promouvoirSiPossible(ExemplaireArticle ea) {
        if (ea == null) return;

        // seulement pour unités de location disponibles
        String statutStr = String.valueOf(ea.getStatut());
        if (!"Location".equals(statutStr) || Boolean.TRUE.equals(ea.getLoue()) || Boolean.TRUE.equals(ea.getReserve())) {
            return;
        }

        SvcReservation       service  = new SvcReservation();
        SvcExemplaireArticle svcEA = new SvcExemplaireArticle();
        svcEA.setEm(service.getEm()); // même EM

        Date now = new Date();
        Date dateLimite = calcDateLimite(now, 3);

        Reservation promue = null;
        String dest = null;

        EntityTransaction tx = service.getTransaction();
        tx.begin();
        try {
            Reservation next = service.findNextByArticleMagasin(
                    ea.getArticleIdArticle(),
                    ea.getMagasinIdMagasin());

            if (next != null && !Boolean.TRUE.equals(ea.getReserve())) {
                // mettre de côté
                ea.setReserve(true);
                svcEA.save(ea);

                // marquer prête
                next.setStatut(ReservationStatutEnum.pret);
                next.setExemplaire(ea);
                next.setDateReady(now);
                next.setHoldUntil(dateLimite);
                next.setMailEnvoye(false);
                service.save(next);

                promue = next;
                if (next.getUtilisateurIdUtilisateur() != null) {
                    dest = next.getUtilisateurIdUtilisateur().getCourriel();
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur promotion réservation", null));
            svcEA.close(); service.close();
            return;
        }

        // Post-commit : mail + mailEnvoye + growl détaillé
        if (promue != null && dest != null) {
            try {
                String articleNom = (promue.getArticleIdArticle() != null) ? String.valueOf(promue.getArticleIdArticle().getNom()) : "?";
                String limiteTxt  = (promue.getHoldUntil() != null) ? new SimpleDateFormat("dd/MM/yyyy").format(promue.getHoldUntil()) : "?";
                String corps = "Bonjour,\n\nVotre article réservé est disponible au magasin pendant 3 jours.\n"
                        + "Article : " + articleNom + "\n"
                        + "Date limite : " + limiteTxt + "\n\n"
                        + "Merci de votre confiance.";
                MailUtils.sendText(dest, "Votre réservation est prête", corps);
            } catch (Exception mailEx) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Réservation promue, mais l’envoi du mail a échoué.", null));
            }

            EntityTransaction tx2 = service.getTransaction();
            tx2.begin();
            try {
                promue.setMailEnvoye(true);
                service.save(promue);
                tx2.commit();
            } catch (Exception e2) {
                if (tx2.isActive()) tx2.rollback();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Mail envoyé mais l’indicateur n’a pas pu être enregistré.", null));
            }

            // growl détaillé (unitaire)
            String articleNom = (promue.getArticleIdArticle() != null) ? String.valueOf(promue.getArticleIdArticle().getNom()) : "?";
            String prenom = (promue.getUtilisateurIdUtilisateur() != null && promue.getUtilisateurIdUtilisateur().getPrenom() != null)
                    ? promue.getUtilisateurIdUtilisateur().getPrenom() : "";
            String nom    = (promue.getUtilisateurIdUtilisateur() != null && promue.getUtilisateurIdUtilisateur().getNom() != null)
                    ? promue.getUtilisateurIdUtilisateur().getNom() : "";
            String client = (prenom + " " + nom).trim();
            if (client.isEmpty()) client = "?";
            String limiteTxt = (promue.getHoldUntil() != null) ? new SimpleDateFormat("dd/MM/yyyy").format(promue.getHoldUntil()) : "?";
            String cb = "-";
            if (promue.getExemplaire() != null && promue.getExemplaire().getCodeBarreIdCB() != null
                    && promue.getExemplaire().getCodeBarreIdCB().getCodeBarre() != null) {
                cb = promue.getExemplaire().getCodeBarreIdCB().getCodeBarre();
            }

            String detail = "Mettre de côté → Article: " + articleNom
                    + " | Client: " + client
                    + " | À retirer avant: " + limiteTxt
                    + " | CB: " + cb;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Réservation prête", detail));

            refresh();
        }

        service.close();
    }

    /**
     * Promotion à utiliser DANS une transaction existante (batch création, EM partagé).
     * Pas de mail ni growl ici : le caller traite post-commit.
     */
    public void promouvoirInTx(ExemplaireArticle ea,
                               SvcReservation svcR,
                               SvcExemplaireArticle svcEA,
                               Date now,
                               Date dateLimite,
                               List<Reservation> outPromues) {
        if (ea == null || svcR == null || svcEA == null) return;

        String statutStr = String.valueOf(ea.getStatut());
        if (!"Location".equals(statutStr) || Boolean.TRUE.equals(ea.getLoue()) || Boolean.TRUE.equals(ea.getReserve())) {
            return;
        }

        try {
            Reservation next = svcR.findNextByArticleMagasin(
                    ea.getArticleIdArticle(),
                    ea.getMagasinIdMagasin());

            if (next != null && !Boolean.TRUE.equals(ea.getReserve())) {
                ea.setReserve(true);
                svcEA.save(ea);

                next.setStatut(ReservationStatutEnum.pret);
                next.setExemplaire(ea);
                next.setDateReady(now);
                next.setHoldUntil(dateLimite);
                next.setMailEnvoye(false);
                svcR.save(next);

                if (outPromues != null) outPromues.add(next);
            }
        } catch (Exception ignore) {
            // on ne casse pas la création si la promo échoue
        }
    }

    /* ======================
     *  Helpers locaux simples
     * ====================== */

    private Date calcDateLimite(Date base, int jours) {
        Calendar c = Calendar.getInstance();
        c.setTime(base != null ? base : new Date());
        c.add(Calendar.DAY_OF_MONTH, jours);
        return c.getTime();
    }

    /* ======================
     *  Getters / Setters
     * ====================== */

    public List<Reservation> getListeReservation() { return listeReservation; }
    public void setListeReservation(List<Reservation> listeReservation) { this.listeReservation = listeReservation; }

    public String getNumMembre() { return numMembre; }
    public void setNumMembre(String numMembre) { this.numMembre = numMembre; }

    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

    public Reservation getSelected() { return selected; }
    public void setSelected(Reservation selected) { this.selected = selected; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }
}

