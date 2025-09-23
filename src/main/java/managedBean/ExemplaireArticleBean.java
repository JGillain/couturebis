package managedBean;

import entities.*;
import enumeration.ExemplaireArticleStatutEnum;
import org.apache.log4j.Logger;
import services.SvcReservation;
import tools.MailUtils;
import tools.ModelCodeBarre;
import services.SvcCodeBarre;
import services.SvcExemplaireArticle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.File;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Named
@SessionScoped
public class ExemplaireArticleBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private ExemplaireArticle EA;
    private Article article;
    private Magasin magasin;
    private int nombreExemplaire;
    private boolean flagVente;
    private List<ExemplaireArticle> listexart = new ArrayList<ExemplaireArticle>();
    private List<ExemplaireArticle> searchResults = new ArrayList<ExemplaireArticle>();
    @Inject
    private MagasinBean magasinBean;
    @Inject
    private CodeBarreBean codeBarreBean;
    @Inject
    private ReservationBean reservationBean;
    private static final Logger log = Logger.getLogger(ExemplaireArticleBean.class);

    @PostConstruct
    public void init() {
        log.debug("ExemplaireArticleBean init");
        EA = new ExemplaireArticle();
        listexart = getReadAll();
        magasin = magasinBean.getMagasin();
    }

    public void save() {
        log.debug("ExemplaireArticleBean save");

        /* ---- validation ---- */
        if (article == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Article manquant", null));
            return;
        }
        if (magasin == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magasin manquant", null));
            return;
        }

        // Services
        SvcExemplaireArticle service = new SvcExemplaireArticle();
        SvcCodeBarre svcCB = new SvcCodeBarre();

        // Partager l'EM
        svcCB.setEm(service.getEm());

        // === Variables pour la promo batch (post-commit) ===
        SvcReservation svcR = null;                    // init plus bas seulement si besoin
        List<Reservation> reservationsPromues = new ArrayList<>();
        Date now = new Date();
        Date dateLimite = addDays(now, 3);            // fenêtre de 3 jours

        EntityTransaction tx = service.getTransaction();
        tx.begin();
        try {
            if (EA != null && EA.getId() != null) {
                // === CAS MODIFICATION ===
                EA.setArticleIdArticle(article);
                EA.setMagasinIdMagasin(magasin);
                EA.setStatut(flagVente ? ExemplaireArticleStatutEnum.Vente
                        : ExemplaireArticleStatutEnum.Location);
                EA.setCommentaireEtat(EA.getCommentaireEtat()); // au cas où modifié
                service.save(EA);

                tx.commit();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Exemplaire modifié avec succès", null));
            } else {
                // === CAS CREATION (batch) ===
                if (nombreExemplaire < 1) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "La quantité doit être un entier positif (≥ 1)", null));
                    tx.rollback();
                    return;   // abort
                }

                // Créer codes-barres à l’avance pour la LOCATION
                List<String> codes = Collections.emptyList();
                if (!flagVente) {
                    codes = codeBarreBean.createCB(false, nombreExemplaire);
                }

                // SvcReservation (uniquement si LOCATION)
                if (!flagVente) {
                    svcR = new SvcReservation();
                    svcR.setEm(service.getEm()); // EM partagé pour la promo in-TX
                }

                for (int i = 0; i < nombreExemplaire; i++) {
                    ExemplaireArticle ex = new ExemplaireArticle();
                    ex.setArticleIdArticle(article);
                    ex.setMagasinIdMagasin(magasin);
                    ex.setStatut(flagVente ? ExemplaireArticleStatutEnum.Vente
                            : ExemplaireArticleStatutEnum.Location);

                    if (!flagVente) {
                        // l’exemplaire de LOCATION a son propre code-barres
                        CodeBarre cb = new CodeBarre();
                        cb.setCodeBarre(codes.get(i));
                        svcCB.save(cb);          // persist codebarre
                        ex.setCodeBarreIdCB(cb); // link
                    }
                    service.save(ex);              // persist exemplaire

                    // === PROMOTION FIFO DANS LA MEME TX (LOCATION uniquement) ===
                    if (!flagVente) {
                        reservationBean.promouvoirInTx(
                                ex,
                                svcR,
                                service,
                                now,
                                dateLimite,
                                reservationsPromues
                        );
                    }
                }

                tx.commit();

                // === POST-COMMIT ===
                // 1) Génération PDF et messages (logique existante conservée)
                String pdf = "erreur";
                if (flagVente) {
                    // pour la vente : étiquettes basées sur le CB de l’article
                    try {
                        pdf = new ModelCodeBarre().createSheet(
                                article.getCodeBarreIdCB().getCodeBarre(),
                                nombreExemplaire,
                                "CB_vente_" + article.getId());
                    } catch (Exception e) {
                        log.error("Erreur génération PDF (vente)", e);
                    }
                } else {
                    try {
                        ModelCodeBarre mcb = new ModelCodeBarre();
                        // NOTE: on réutilise la liste 'codes' de création
                        pdf = mcb.createSheet(codes, "CB_loc_" + article.getId());
                    } catch (Exception e) {
                        log.error("Erreur génération PDF (location)", e);
                    }
                }

                // 2) Envoi des mails clients pour TOUTES les réservations promues (si besoin)
                if (!reservationsPromues.isEmpty()) {
                    for (Reservation rsv : reservationsPromues) {
                        try {
                            String dest = (rsv.getUtilisateurIdUtilisateur() != null)
                                    ? rsv.getUtilisateurIdUtilisateur().getCourriel() : null;
                            if (dest == null) continue;

                            String articleNom = (rsv.getArticleIdArticle() != null)
                                    ? String.valueOf(rsv.getArticleIdArticle().getNom()) : "?";
                            String limiteTxt = (rsv.getHoldUntil() != null)
                                    ? new SimpleDateFormat("dd/MM/yyyy").format(rsv.getHoldUntil()) : "?";

                            String corps = "Bonjour,\n\nVotre article réservé est disponible au magasin pendant 3 jours.\n"
                                    + "Article : " + articleNom + "\n"
                                    + "Date limite : " + limiteTxt + "\n\n"
                                    + "Merci de votre confiance.";

                            MailUtils.sendText(dest, "Votre réservation est prête", corps);
                        } catch (Exception mailEx) {
                            // On n'interrompt pas : simple avertissement
                            FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                                            "Réservation promue, mais l’envoi d’un mail a échoué.", null));
                        }
                    }

                    // 3) Poser mailEnvoye=true pour toutes (mini-TX avec le même EM)
                    EntityTransaction tx2 = service.getTransaction(); // on peut réutiliser l’EM de 'service'
                    tx2.begin();
                    try {
                        for (Reservation rsv : reservationsPromues) {
                            rsv.setMailEnvoye(true);
                            if (svcR == null) {
                                // par sécurité, si non init (cas vente => liste vide, donc jamais ici)
                                svcR = new SvcReservation();
                                svcR.setEm(service.getEm());
                            }
                            svcR.save(rsv);
                        }
                        tx2.commit();
                    } catch (Exception e) {
                        if (tx2.isActive()) tx2.rollback();
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_WARN,
                                        "Mails envoyés mais l’indicateur n’a pas pu être enregistré pour certaines réservations.", null));
                    }

                    // 4) Un SEUL growl récapitulatif pour le staff (batch)
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    String titre  = reservationsPromues.size() + " réservation(s) prête(s)";
                    String detail = "Plusieurs réservations ont été promues. "
                            + "Ouvrez la page Réservations et filtrez sur « Prêt » pour mettre les articles de côté.";
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, titre, detail));
                }

                // 5) Message et redirection
                if (!Objects.equals(pdf, "erreur")) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    ExternalContext ec = fc.getExternalContext();

                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                            nombreExemplaire + " exemplaire(s) enregistré(s)", null));
                    ec.getFlash().setKeepMessages(true);

                    try {
                        String url = ec.getRequestContextPath()
                                + "/formNewExArticle.xhtml?faces-redirect=true"
                                + "&barcodeFile=" + URLEncoder.encode(new File(pdf).getName(), "UTF-8");
                        ec.redirect(url);
                        fc.responseComplete();
                    } catch (Exception e) {
                        log.error("Erreur redirection après création", e);
                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    nombreExemplaire + " exemplaire(s) enregistré(s)", null));
                }
            }
        }
        catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
                log.error("Erreur lors du save", e);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Échec de l'enregistrement", null));
            } else  {
                log.error("Erreur lors du save (post-commit)", e);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Échec de la génération du pdf", null));
            }
        }
        finally {
            // Fermeture du service principal
            service.close();
        }
    }

    private Date addDays(Date base, int jours) {
        Calendar c = Calendar.getInstance();
        c.setTime(base != null ? base : new Date());
        c.add(Calendar.DAY_OF_MONTH, jours);
        return c.getTime();
    }

    public String redirectModif(){
        article = EA.getArticleIdArticle();
        magasin = EA.getMagasinIdMagasin();
        return "/formEditExArticle.xhtml?faces-redirect=true";
    }
    public String activdesactivExArt() {

        SvcExemplaireArticle service = new SvcExemplaireArticle();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            if (EA.getActif()) {
                EA.setActif(false);
            } else {
                EA.setActif(true);
            }
            service.save(EA);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "L'operation a reussie", null));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "l'operation a échoué", null));
            }
            init();
            service.close();
        }
        return ("/tableExArticle.xhtml?faces-redirect=true");
    }

    public String getExArticleByArticle() {
        SvcExemplaireArticle service = new SvcExemplaireArticle();
        listexart = service.findByArticle(article);
        service.close();
        return "/tableExArticle.xhtml?faces-redirect=true";
    }

    public String flushBienv() {
        init();
        if (searchResults != null) {
            searchResults.clear();
        }
        return "/bienvenue.xhtml?faces-redirect=true";
    }

    public String flushExArt() {
        init();
        if (searchResults != null) {
            searchResults.clear();
        }
        return "/tableExArticle.xhtml?faces-redirect=true";
    }

    public List<ExemplaireArticle> getReadAll() {
        SvcExemplaireArticle service = new SvcExemplaireArticle();
        listexart = service.findAllExArticles();
        service.close();
        return listexart;
    }

    public List<ExemplaireArticle> getReadExArticle() {
        listexart.clear();
        listexart.add(EA);
        return listexart;
    }

    //-------------------------------Getter & Setter--------------------------------------------


    public int getNombreExemplaire() {
        return nombreExemplaire;
    }

    public void setNombreExemplaire(int nombreExemplaire) {
        this.nombreExemplaire = nombreExemplaire;
    }

    public boolean isFlagVente() {
        return flagVente;
    }

    public void setFlagVente(boolean flagVente) {
        this.flagVente = flagVente;
    }

    public List<ExemplaireArticle> getListexart() {
        return listexart;
    }

    public void setListexart(List<ExemplaireArticle> listexart) {
        this.listexart = listexart;
    }

    public List<ExemplaireArticle> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<ExemplaireArticle> searchResults) {
        this.searchResults = searchResults;
    }

    public ExemplaireArticle getEA() {
        return EA;
    }

    public void setEA(ExemplaireArticle EA) {
        this.EA = EA;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void setMagasin(Magasin magasin) {
        this.magasin = magasin;
    }
}