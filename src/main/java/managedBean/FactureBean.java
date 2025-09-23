package managedBean;

import entities.*;
import enumeration.ExemplaireArticleEtatEnum;
import enumeration.ExemplaireArticleStatutEnum;
import enumeration.FactureEtatEnum;
import enumeration.FactureTypeEnum;
import objectCustom.locationCustom;
import objectCustom.venteCustom;
import org.apache.log4j.Logger;
import org.primefaces.event.RowEditEvent;
import tools.MailUtils;
import tools.ModelFactLoca;
import tools.ModelFactLocaPena;
import tools.ModelFactVente;
import services.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

@Named
@SessionScoped
public class FactureBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;

    private Facture facture;
    private static final Logger log = Logger.getLogger(FactureBean.class);
    private List<locationCustom> listLC = new ArrayList<>();
    private List<venteCustom> listVC = new ArrayList<>();
    private List<TarifPenalite> tarifsPenalites;
    private String numMembre;
    private String CB;
    private boolean choixetat;
    private double montant;
    @Inject
    private MagasinBean magasinBean;
    @Inject
    private ReservationBean reservationBean;
    private Magasin magasin;
    private ExemplaireArticle exemplaireArticle;

    @PostConstruct
    public void init(){
        listLC = new ArrayList<>();
        listVC = new ArrayList<>();
        addNewListRowLoca();
        addNewListRowVente();
        facture = new Facture();
        numMembre= "";
        CB= "";
        magasin = magasinBean.getMagasin();
    }

    //methodes de gestions des tableaux newFactLoca et newFactVente

    public void addNewListRowLoca() {
        listLC.add(new locationCustom());
    }

    public void delListRowLoca() {
        if (listLC.size() >1)
        {
            listLC.remove(listLC.size()-1);
        }
    }
    public void addNewListRowVente() {
        listVC.add(new venteCustom());
    }

    public void delListRowVente() {
        if (listVC.size() >1)
        {
            listVC.remove(listVC.size()-1);
        }
    }

    public void onRowEdit(RowEditEvent event) {
        locationCustom row = (locationCustom) event.getObject();
        log.info("Row saved: " + row);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Ligne mise à jour", null));
    }

    public void onRowEditCancel(RowEditEvent event) {
        locationCustom row = (locationCustom) event.getObject();
        log.info("Edit canceled: " + row);
    }
    public void onRowEditVente(RowEditEvent event) {
        venteCustom row = (venteCustom) event.getObject();
        String cb = row.getCB();
        if (cb == null || cb.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Code barre manquant", null));
            return;
        }

        SvcArticle serviceA = new SvcArticle();
        try {
            List<Article> arts = serviceA.findOneByCodeBarre(cb);
            if (arts == null || arts.isEmpty()) {
                row.setArticleNom("");
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Article introuvable", "CB: " + cb));
                return;
            }

            Article a = arts.get(0);
            row.setArticleNom(a.getNom());

            if (row.getNbrArticles() < 1) {
                row.setNbrArticles(1);
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Ligne mise à jour", a.getNom()));
            log.info("Row saved: " + row);
        } catch (Exception e) {
            log.error("onRowEditVente error for CB=" + cb, e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur lors de la mise à jour de la ligne", null));
        } finally {
            serviceA.close();
        }
    }

    public void onRowEditCancelVente(RowEditEvent event) {
        venteCustom row = (venteCustom) event.getObject();
        log.info("Edit canceled: " + row);
    }

    //methode qui permet le téléchargement sur les machines client des factures
    public void downloadPdfFacture() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        String numFacture = ec.getRequestParameterMap().get("numFacture");
        if (numFacture == null || numFacture.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Téléchargement", "Numéro de facture manquant."));
            return;
        }

        Path file = Paths.get("C:"+ File.separator +"REVECouture"+File.separator +"facture", numFacture + ".pdf");

        try {
            if (!Files.exists(file)) {
                HttpServletResponse resp = (HttpServletResponse) ec.getResponse();
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF introuvable");
                fc.responseComplete();
                return;
            }

            HttpServletResponse resp = (HttpServletResponse) ec.getResponse();
            resp.reset();
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"" + file.getFileName().toString() + "\"");
            resp.setHeader("Content-Length", String.valueOf(Files.size(file)));

            try (OutputStream out = resp.getOutputStream()) {
                Files.copy(file, out);
                out.flush();
            }

            fc.responseComplete();
        } catch (IOException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Téléchargement", "Erreur lors de l’envoi du PDF."));
        }
    }

    //début du flux de la facture de location; avant payement.
    public String preparerLocation() {
        // --- doublons CB ---
        List<String> dupCbs = new ArrayList<>();
        if (listLC == null || listLC.isEmpty()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Aucun article", "Ajoutez au moins une ligne."));
            return "";
        }
        for (int i = 0; i < listLC.size(); i++) {
            String cbi = listLC.get(i).getCB();
            if (cbi == null) continue;
            for (int j = i + 1; j < listLC.size(); j++) {
                String cbj = listLC.get(j).getCB();
                if (cbi.equals(cbj) && !dupCbs.contains(cbi)) {
                    dupCbs.add(cbi);
                }
            }
        }
        if (!dupCbs.isEmpty()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Doublon",
                    "Les codes-barres suivants sont saisis plusieurs fois : " + String.join(", ", dupCbs)
            ));
            return "";
        }

        // --- services ---
        SvcFacture           service   = new SvcFacture();
        SvcFactureDetail     serviceFD = new SvcFactureDetail();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        SvcTarif             serviceT  = new SvcTarif();
        SvcUtilisateur       serviceU  = new SvcUtilisateur();

        serviceFD.setEm(service.getEm());
        serviceEA.setEm(service.getEm());
        serviceT .setEm(service.getEm());
        serviceU .setEm(service.getEm());

        try {
            // magasin déjà injecté dans @PostConstruct : this.magasin = magasinBean.getMagasin();
            if (magasin == null) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Magasin manquant", null));
                return "";
            }

            // client via numMembre
            List<Utilisateur> lu = serviceU.getByNumMembre(numMembre);
            if (lu == null || lu.isEmpty()) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Client introuvable", "Vérifiez le code membre."));
                return "";
            }
            Utilisateur u = lu.get(0);

            // tarif actif aujourd’hui
            Date today = new Date(System.currentTimeMillis());
            List<Tarif> tarifs = serviceT.findTarifByMagasin(today, magasin);
            if (tarifs == null || tarifs.isEmpty()) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Aucun tarif actif pour ce magasin aujourd’hui.", null));
                return "";
            }
            Tarif T = tarifs.get(0);

            // horodatage arrondi à la minute
            long now = System.currentTimeMillis();
            long rounded = now - (now % 60000);
            Timestamp tsDebut = new Timestamp(rounded);

            facture.setDateDebut(tsDebut);
            facture.setEtat(enumeration.FactureEtatEnum.en_cours);
            facture.setType(enumeration.FactureTypeEnum.Location);
            facture.setMagasinIdMagasin(magasin);
            facture.setUtilisateurIdUtilisateur(u);

            // estimation du total (sans persist) en réutilisant newRent(...)
            double total = 0d;
            for (locationCustom lc : listLC) {
                String cb = (lc.getCB() == null) ? "" : lc.getCB().trim();
                if (cb.isEmpty()) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Code-barres manquant", "Veuillez compléter la ligne."));
                    return "";
                }

                List<ExemplaireArticle> found = serviceEA.findOneByCodeBarre(cb);
                if (found == null || found.isEmpty()) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Exemplaire introuvable", "CB: " + cb));
                    return "";
                }
                ExemplaireArticle ea = found.get(0);

                if (!ea.getStatut().equals(ExemplaireArticleStatutEnum.Location)) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Statut invalide", "L’exemplaire n’est pas en Location (CB: " + cb + ")"));
                    return "";
                }
                if (!Boolean.TRUE.equals(ea.getActif()) || Boolean.TRUE.equals(ea.getLoue())) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Indisponible", "Exemplaire inactif ou déjà loué (CB: " + cb + ")"));
                    return "";
                }

                int nbJours = Math.max(1, lc.getNbrJours());
                Timestamp tsFin = new Timestamp(tsDebut.getTime() + (long) nbJours * 24L * 3600L * 1000L);

                //
                FactureDetail fdPreview = serviceFD.newRent(ea, facture, T, nbJours, tsFin);
                total += (fdPreview != null ? fdPreview.getPrix() : 0d);

                if (ea.getArticleIdArticle() != null) {
                    lc.setArticleNom(ea.getArticleIdArticle().getNom());
                }
            }

            // total affiché sur confirmLoca.xhtml
            montant = total;

            // go confirmation
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            return "/confirmLoca.xhtml?faces-redirect=true";
        } finally {
            service.close();
        }
    }
    // continuation du flux apres confirmation du payement.
    public String finaliserLocation() {
        // services
        SvcFacture           service   = new SvcFacture();
        SvcFactureDetail     serviceFD = new SvcFactureDetail();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        SvcTarif             serviceT  = new SvcTarif();

        serviceFD.setEm(service.getEm());
        serviceEA.setEm(service.getEm());
        serviceT .setEm(service.getEm());

        try {
            // sécurités minimales
            if (facture == null || facture.getUtilisateurIdUtilisateur() == null || magasin == null) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Contexte incomplet", "Relancez la préparation."));
                return "";
            }

            // numéro de facture au dernier moment
            facture.setNumeroFacture(createNumFact());
            facture.setType(enumeration.FactureTypeEnum.Location);
            facture.setEtat(enumeration.FactureEtatEnum.en_cours);
            facture.setMagasinIdMagasin(magasin);

            // tarif actif
            Date today = new Date(System.currentTimeMillis());
            List<Tarif> tarifs = serviceT.findTarifByMagasin(today, magasin);
            if (tarifs == null || tarifs.isEmpty()) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Aucun tarif actif pour ce magasin aujourd’hui.", null));
                return "";
            }
            Tarif T = tarifs.get(0);

            // date début : on garde celle fixée en préparation (si null, on recalcule)
            if (facture.getDateDebut() == null) {
                long now = System.currentTimeMillis();
                long rounded = now - (now % 60000);
                facture.setDateDebut(new Timestamp(rounded));
            }
            Timestamp tsDebut = facture.getDateDebut();

            double total = 0d;

            EntityTransaction tx = service.getTransaction();
            tx.begin();
            try {
                for (locationCustom lc : listLC) {
                    String cb = lc.getCB();
                    int nbJours = Math.max(1, lc.getNbrJours());

                    List<ExemplaireArticle> found = serviceEA.findOneByCodeBarre(cb);
                    if (found == null || found.isEmpty()) {
                        tx.rollback();
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.getExternalContext().getFlash().setKeepMessages(true);
                        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Exemplaire introuvable au moment de la validation", "CB: " + cb));
                        return "";
                    }
                    ExemplaireArticle ea = found.get(0);

                    if (!ExemplaireArticleStatutEnum.Location.equals(ea.getStatut()) ||
                            !Boolean.TRUE.equals(ea.getActif()) ||
                            Boolean.TRUE.equals(ea.getLoue())) {
                        tx.rollback();
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.getExternalContext().getFlash().setKeepMessages(true);
                        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Indisponible", "Exemplaire non louable/actif/déjà loué (CB: " + cb + ")"));
                        return "";
                    }

                    // marquer loué
                    serviceEA.loueExemplaire(ea);

                    Timestamp tsFin = new Timestamp(tsDebut.getTime() + (long) nbJours * 24L * 3600L * 1000L);
                    FactureDetail fd = serviceFD.newRent(ea, facture, T, nbJours, tsFin);
                    serviceFD.save(fd);

                    ea.setReserve(false);
                    ea.setLoue(true);
                    serviceEA.save(ea);

                    total += fd.getPrix();
                }

                facture.setPrixTVAC(total);
                service.save(facture);
                tx.commit();

                // post-commit : PDF + mail
                service.refreshEntity(facture);
                ModelFactLoca mfb = new ModelFactLoca();
                String path = mfb.creation(facture, magasin);
                try {
                    MailUtils.sendWithAttachment(
                            facture.getUtilisateurIdUtilisateur().getCourriel(),
                            "Facture de location",
                            "Vous trouverez la facture concernant votre location en pièce jointe.",
                            path
                    );
                } catch (Exception mailEx) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Facture créée, mais l’envoi d’e-mail a échoué.", null));
                }

                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Location finalisée",
                        "Facture " + facture.getNumeroFacture() + " créée."));

                // nettoyage et retour
                init();
                return "/tableFactureLoca.xhtml?faces-redirect=true";

            } catch (Exception e) {
                FacesContext fc = FacesContext.getCurrentInstance();
                log.error("Erreur finaliserLocation()", e);
                if (tx.isActive()) {
                    tx.rollback();
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erreur fatale",
                            "La location n'a pas pu être enregistrée."));
                } else {
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erreur",
                            "La génération du pdf ou de l'email n'a pas réussie."));
                }
                return "";
            }
        } finally {
            service.close();
        }
    }

    // Méthode qui permet de créer une facture pénalité
    public void newFactPena(FactureDetail facturesDetail) {

        // --- Services ---
        SvcFacture           service   = new SvcFacture();
        SvcFactureDetail     serviceFD = new SvcFactureDetail();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        SvcUtilisateur       serviceU  = new SvcUtilisateur();
        SvcTarif             serviceT  = new SvcTarif();
        SvcPenalite          serviceP  = new SvcPenalite();
        SvcTarifPenalite     serviceTP = new SvcTarifPenalite();

        // Partage EM
        serviceFD.setEm(service.getEm());
        serviceEA.setEm(service.getEm());

        // --- Temps ---
        long now     = System.currentTimeMillis();
        long rounded = now - now % 60000;
        Timestamp tsFacture = new Timestamp(rounded);

        // --- Contexte magasin / utilisateur / tarif ---
        Magasin magasinUse = (magasin != null)
                ? magasin
                : (facturesDetail != null && facturesDetail.getFactureIdFacture() != null
                ? facturesDetail.getFactureIdFacture().getMagasinIdMagasin()
                : null);


        Utilisateur u = facturesDetail.getFactureIdFacture().getUtilisateurIdUtilisateur();

        Date dateDebutOrigin = Date.from(facturesDetail.getFactureIdFacture().getDateDebut().toInstant());

        Tarif T = serviceT.findTarifByMagasin(dateDebutOrigin, magasinUse).get(0);

        // Lignes à imprimer dans le PDF (TP sélectionnées + Retard si appliqué)
        List<TarifPenalite> lignesPDF = new ArrayList<>();
        if (tarifsPenalites != null && !tarifsPenalites.isEmpty()) {
            lignesPDF.addAll(tarifsPenalites);
        }

        double total = 0d;
        Facture fact = new Facture();
        FactureDetail fdRetard = null;

        EntityTransaction tx = service.getTransaction();
        tx.begin();
        try {
            // 1) Créer la facture Pénalité
            fact.setDateDebut(tsFacture);
            fact.setNumeroFacture(createNumFact());
            fact.setEtat(FactureEtatEnum.terminer);
            fact.setType(FactureTypeEnum.Penalite);
            fact.setMagasinIdMagasin(magasinUse);
            fact.setUtilisateurIdUtilisateur(u);

            // 2) Pénalités choisies (SAUF "Retard" qui est calculé à part)
            if (!tarifsPenalites.isEmpty()) {
                for (TarifPenalite tp : tarifsPenalites) {
                    String lib = (tp.getPenaliteIdPenalite() != null)
                            ? tp.getPenaliteIdPenalite().getDenomination() : null;
                    if (lib != null && lib.equalsIgnoreCase("Retard")) {
                        continue; // géré au point 3
                    }
                    FactureDetail fd = serviceFD.newPena(
                            facturesDetail.getExemplaireArticleIdEA(),
                            fact, T,
                            tp.getPenaliteIdPenalite(),
                            dateDebutOrigin,
                            tsFacture
                    );
                    total += (fd.getPrix() == null ? 0d : fd.getPrix());
                    serviceFD.save(fd);
                }
            }

            // 3) Ajouter "Retard" si applicable (jours entamés → 1 jour min)
            if (facturesDetail.getDateRetour().after(facturesDetail.getDateFin())) {

                long MILLIS_PAR_JOUR = 24L * 3600L * 1000L;
                long diff = facturesDetail.getDateRetour().getTime() - facturesDetail.getDateFin().getTime();
                int nbjours = (int) Math.max(1L, (diff + (MILLIS_PAR_JOUR - 1)) / MILLIS_PAR_JOUR);

                List<Penalite> retardRef = serviceP.findByName("Retard");
                if (!retardRef.isEmpty()) {
                    List<TarifPenalite> tpsRetard = serviceTP.findByPenalitesByArticle(
                            T, retardRef.get(0), dateDebutOrigin,
                            facturesDetail.getExemplaireArticleIdEA().getArticleIdArticle());

                    if (tpsRetard != null && !tpsRetard.isEmpty()) {
                        fdRetard = serviceFD.newPenaretard(
                                facturesDetail.getExemplaireArticleIdEA(),
                                fact,
                                T,
                                retardRef.get(0),
                                nbjours,
                                dateDebutOrigin,
                                tsFacture
                        );
                        total += (fdRetard.getPrix() == null ? 0d : fdRetard.getPrix());
                        serviceFD.save(fdRetard);

                        // S’assurer que "Retard" figure dans les lignes PDF (sans doublon)
                        boolean present = false;
                        for (TarifPenalite t : lignesPDF) {
                            if (t.getPenaliteIdPenalite().getDenomination().equalsIgnoreCase("Retard")) {
                                present = true;
                                break;
                            }
                        }
                        if (!present) {
                            lignesPDF.add(tpsRetard.get(0));
                        }
                    }
                }
            }

            // 4) Total facture + save
            fact.setPrixTVAC(total);
            service.save(fact);
            tx.commit();

            // 5) PDF + mail
            service.refreshEntity(fact);
            ModelFactLocaPena builder = new ModelFactLocaPena();
            String pdfPath = builder.creation(fact, lignesPDF, fdRetard, magasinUse);

            try {
                MailUtils.sendWithAttachment(
                        fact.getUtilisateurIdUtilisateur().getCourriel(),
                        "Facture de pénalité",
                        "Vous trouverez la facture concernant les pénalités suite à votre location en pièce jointe.",
                        pdfPath
                );
            } catch (Exception mailEx) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Facture créée, mais l’e-mail n’a pas pu être envoyé.", null));
            }

            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Facture de pénalité créée",
                    "Total à payer : " + String.format(Locale.FRANCE, "%.2f €", fact.getPrixTVAC())
            ));

        } catch (Exception e) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (tx.isActive()) {
                tx.rollback();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Erreur fatale", "La facture de pénalité n’a pas pu être enregistrée."
                ));
            }
            else  {
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Erreur fatale", "La facture de pénalité a été enregistrée mais le pdf n'a pa pu être créé. Total à payer : " + String.format(Locale.FRANCE, "%.2f €", fact.getPrixTVAC())
                ));
            }
        } finally {
            // FD/EA partagent l’EM de 'service' → ne ferme que 'service'
            service.close();
            // Ces services ont leur propre EM
            serviceU.close();
            serviceT.close();
            serviceP.close();
            serviceTP.close();
        }
    }


    public String preparerVente() {
        SvcArticle serviceA = new SvcArticle();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        double total = 0d;

        try {
            if (listVC == null || listVC.isEmpty()) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Aucun article", "Ajoutez au moins une ligne."));
                return "";
            }

            for (venteCustom v : listVC) {
                String cb = (v.getCB() == null) ? "" : v.getCB().trim();

                if (cb.isEmpty()) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Code barre manquant", "Veuillez compléter la ligne."));
                    return "";
                }

                int qte = Math.max(v.getNbrArticles(), 1);

                // Récupère l'article via le CB (re-vérifié par sécurité)
                List<Article> arts = serviceA.findOneByCodeBarre(cb);
                if (arts == null || arts.isEmpty()) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Article introuvable", "CB: " + cb));
                    return "";
                }
                Article a = arts.get(0);
                // ajoute le nom de l'article pour affichage
                v.setArticleNom(a.getNom());
                // Vérifie le stock disponible d'exemplaires vendables (statut=Vente, actif=true, loue=false, reserve=false)
                int dispo = serviceEA.countAvailableExArticlesSales(a);

                if (dispo < qte) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Stock insuffisant",
                            a.getNom() + " — demandé: " + qte + ", disponible: " + dispo));
                    return "";
                }

                total += a.getPrix() * qte;
            }

            montant = total;

            // On passe à la page de confirmation de paiement
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            return "/confirmVente.xhtml?faces-redirect=true";

        } finally {
            serviceEA.close();
            serviceA.close();
        }
    }
    public String finaliserVente() {
        SvcFacture service = new SvcFacture();
        SvcFactureDetail serviceFD = new SvcFactureDetail();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        SvcArticle serviceA = new SvcArticle();
        SvcUtilisateur serviceU = new SvcUtilisateur();

        // Partage de l’EM/transaction
        serviceFD.setEm(service.getEm());
        serviceEA.setEm(service.getEm());

        double total = 0d;

        long now = System.currentTimeMillis();
        long rounded = now - (now % 60000);
        Timestamp ts = new Timestamp(rounded);

        try {
            // Client (via numéro membre)
            Utilisateur u = serviceU.getByNumMembre(numMembre).get(0);

            // Facture à persister
            Facture fact = new Facture();
            fact.setDateDebut(ts);
            fact.setNumeroFacture(createNumFact());
            fact.setEtat(FactureEtatEnum.terminer);     // vente réglée
            fact.setType(FactureTypeEnum.Vente);
            fact.setMagasinIdMagasin(magasin);
            fact.setUtilisateurIdUtilisateur(u);

            EntityTransaction tx = service.getTransaction();
            tx.begin();
            try {
                // Pour chaque ligne saisie
                for (venteCustom v : listVC) {
                    String cb = v.getCB();
                    int qte = v.getNbrArticles();

                    // Article par code-barres (déjà validé en amont)
                    Article a = serviceA.findOneByCodeBarre(cb).get(0);

                    // Récupérer qte exemplaires vendables (actif=true, loue=false, reserve=false, statut=Vente)
                    List<ExemplaireArticle> dispo = serviceEA.findAvailableExArticlesSales(a);

                    // vérification minimale : si stock changé entre-temps (ne devrait pas poser probleme ici
                    if (dispo.size() < qte) {
                        tx.rollback();
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.getExternalContext().getFlash().setKeepMessages(true);
                        fc.addMessage(null, new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "Stock insuffisant",
                                a.getNom() + " — demandé: " + qte + ", disponible: " + dispo.size()));
                        return "";
                    }

                    for (int i = 0; i < qte; i++) {
                        ExemplaireArticle ea = dispo.get(i);

                        // Marquer vendu : inactif = vendu ou HS
                        ea.setActif(false);
                        serviceEA.save(ea);

                        // Détail de facture
                        FactureDetail fd = new FactureDetail();
                        fd.setFactureIdFacture(fact);
                        fd.setExemplaireArticleIdEA(ea);
                        fd.setDateFin(ts);
                        fd.setDateRetour(ts);
                        fd.setEtatRendu(null);
                        fd.setPrix(a.getPrix());
                        serviceFD.save(fd);

                        total += a.getPrix();
                    }
                }

                fact.setPrixTVAC(total);
                service.save(fact);
                tx.commit();

                // Post-commit : PDF + mail
                service.refreshEntity(fact);

                ModelFactVente mfv = new ModelFactVente();
                String path = mfv.creation(fact, magasin); // retourne "C:\\REVECouture\\facture\\FRC....pdf"
                MailUtils.sendWithAttachment(fact.getUtilisateurIdUtilisateur().getCourriel(),"Facture de vente","Vous trouverez la facture concernant votre achat en pièce jointe.",path);

                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Vente finalisée",
                        "Facture " + fact.getNumeroFacture() + " créée."));

                // Reset page
                init();
                return "/tableFactureVente.xhtml?faces-redirect=true";

            } catch (Exception e) {
                FacesContext fc = FacesContext.getCurrentInstance();
                log.error("Erreur finaliserVente()", e);
                if (tx.isActive()) {
                    tx.rollback();
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erreur fatale",
                            "La vente n'a pas pu être enregistrée."));
                }
                else {
                    fc.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erreur fatale",
                            "La génération du pdf ou de l'email n'a pas réussie."));
                }
                return "";
            }

        } finally {
            serviceA.close();
            serviceU.close();
            service.close();
        }
    }

    public String redirectChoix() {
        // Service pour récupérer l'exemplaire via le code-barres
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        try {
            List<ExemplaireArticle> found = serviceEA.findOneByCodeBarre(CB);
            if (found == null || found.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Erreur", "Exemplaire introuvable"));
                return "";
            }
            exemplaireArticle = found.get(0);
        } finally {
            serviceEA.close();
        }

        tarifsPenalites = new ArrayList<>();

        if (choixetat) {
            // Chemin "constater l'état" : on va chercher la facture initiale + les pénalités applicables
            SvcFacture serviceF = new SvcFacture();
            SvcTarif serviceT = new SvcTarif();
            SvcTarifPenalite serviceTP = new SvcTarifPenalite();
            try {
                List<Facture> facture1 = serviceF.findActiveByExemplaireArticle(exemplaireArticle);
                if (facture1 == null || facture1.isEmpty()) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "erreur", "facture initiale non trouvée"));
                    return "";
                } else {
                    Date dateDebutFacture = facture1.get(0).getDateDebut();
                    List<Tarif> tarifs = serviceT.findTarifByMagasin(dateDebutFacture, magasin);
                    if (tarifs != null && !tarifs.isEmpty()) {
                        List<TarifPenalite> tmp = serviceTP.FindTarifPenaByTarifByArticle(
                                dateDebutFacture, tarifs.get(0), exemplaireArticle.getArticleIdArticle());
                        for  (TarifPenalite penalite : tmp) {
                            if (!penalite.getPenaliteIdPenalite().getDenomination().equalsIgnoreCase("Retard")){
                                tarifsPenalites.add(penalite);
                            }
                        }
                    }
                    else{
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "erreur", "grille tarifaire liée à la facture non trouvée"));
                        return "";
                    }
                    return "/formEtatArticle.xhtml?faces-redirect=true";
                }
            } finally {
                serviceTP.close();
                serviceT.close();
                serviceF.close();
            }
        } else {
            // Chemin "retour simple"
            return retourArticleLoca();
        }
    }


    // Méthode qui gere le retour des articles en location
    public String retourArticleLoca(){
        FactureDetail facturesDetail = new FactureDetail();
        SvcExemplaireArticle serviceEL = new SvcExemplaireArticle();
        Facture fact = new Facture();
        long now =  System.currentTimeMillis();
        long rounded = now - now % 60000;
        Timestamp timestampretour = new Timestamp(rounded);
        boolean flag = false;

        // exemplaireArticle est alimenté par redirectChoix()

        if (exemplaireArticle.getLoue()) {
            // prendre le FD ouvert pour cet exemplaire
            for (FactureDetail fd : exemplaireArticle.getFactureDetails()){
                if (fd.getDateRetour() == null) {
                    facturesDetail = fd;
                    numMembre = fd.getFactureIdFacture()
                            .getUtilisateurIdUtilisateur()
                            .getCodeBarreIdCB()
                            .getCodeBarre();
                    flag = true;
                    break;
                }
            }

            if (flag) {
                flag = false;
                facturesDetail.setDateRetour(timestampretour);

                // pénalité si en retard OU si une pénalité a été choisie (tarifsPenalites)
                if (facturesDetail.getDateRetour().after(facturesDetail.getDateFin()) || !tarifsPenalites.isEmpty()) {
                    newFactPena(facturesDetail);
                }

                // si tous les détails sont rendus → facture terminée
                for (FactureDetail fd: facturesDetail.getFactureIdFacture().getFactureDetails()){
                    if (fd.getDateRetour() == null) {
                        flag = true; // il en reste au moins un en cours
                        break;
                    }
                }
                if (!flag) {
                    fact = facturesDetail.getFactureIdFacture();
                    fact.setEtat(FactureEtatEnum.terminer);
                }

                // === Persistance
                SvcFacture service = new SvcFacture();
                SvcFactureDetail serviceFD = new SvcFactureDetail();
                serviceEL.setEm(service.getEm());
                serviceFD.setEm(service.getEm());

                EntityTransaction transaction = service.getTransaction();
                transaction.begin();
                try {
                    exemplaireArticle.setLoue(false);
                    if (exemplaireArticle.getEtat() == ExemplaireArticleEtatEnum.Mauvais) {
                        exemplaireArticle.setActif(false);
                    }
                    serviceEL.save(exemplaireArticle);
                    serviceFD.save(facturesDetail);
                    if (fact.getEtat() == FactureEtatEnum.terminer) {
                        service.save(fact);
                    }
                    transaction.commit();

                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"retour accepté",null));

                    // === verif si reservation applicable
                    reservationBean.promouvoirSiPossible(exemplaireArticle);

                } finally {
                    if (transaction.isActive()) {
                        transaction.rollback();
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.addMessage("Erreur", new FacesMessage("l'operation n'est pas reussie"));
                    }

                    service.close();
                    init();
                }
            }
        } else {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"l'article n'est pas loue",null));
            init();
        }
        return "/tableFactureLoca.xhtml?faces-redirect=true";
    }

    public String createFromReservation(Reservation r, Date dateFin) {
        log.debug(r.toString());
        log.debug(dateFin.toString());
        log.debug(r.getId());
        log.debug(r.getUtilisateurIdUtilisateur());
        log.debug(r.getStatut());
        log.debug(r.getHoldUntil());
        final long MILLIS_PAR_JOUR = 24L * 3600L * 1000L;

        long now = 0L;
        long rounded = 0L;
        int nbJours = 1;
        Date today; // sera éventuellement recalé
        String pdfPath;
        String retour;

        Timestamp tsDebut;
        Timestamp tsRetour;
        Tarif T;
        Facture fact;
        FactureDetail fd;
        ModelFactLoca mfb;
        ExemplaireArticle ea;
        List<Tarif> tarifs;

        // ------------------------------------------------------------
        // Services  + partage du même EM
        // ------------------------------------------------------------

        SvcFacture           service   = new SvcFacture();
        SvcFactureDetail     serviceFD = new SvcFactureDetail();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        SvcReservation       serviceR  = new SvcReservation();
        SvcTarif             serviceT  = new SvcTarif();

        serviceFD.setEm(service.getEm());
        serviceEA.setEm(service.getEm());
        serviceR .setEm(service.getEm());
        serviceT .setEm(service.getEm());

        // ------------------------------------------------------------
        // Garde-fous basiques
        // ------------------------------------------------------------

        if (r == null || r.getUtilisateurIdUtilisateur() == null
                || r.getMagasinIdMagasin() == null || r.getExemplaire() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Réservation incomplète (utilisateur, magasin ou exemplaire manquant).", null));
            service.close();
            return null;
        }
        ea = r.getExemplaire();
        if (Boolean.TRUE.equals(ea.getLoue())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "L’exemplaire est déjà loué.", null));
            service.close();
            return null;
        }

        // ------------------------------------------------------------
        // Préparation temps & nb jours
        // ------------------------------------------------------------
        now     = System.currentTimeMillis();
        rounded = now - (now % 60000);                 // arrondi à la minute
        tsDebut = new Timestamp(rounded);

        if (dateFin == null || dateFin.getTime() <= tsDebut.getTime()) {
            // défaut : 1 jour si non fourni ou incohérent
            dateFin = new Date(tsDebut.getTime() + MILLIS_PAR_JOUR);
        }
        dateFin = mergeChosenDateWithNow(dateFin);
        long diffMillis = dateFin.getTime() - tsDebut.getTime();
        nbJours = (int) Math.max(1L, (diffMillis + MILLIS_PAR_JOUR - 1) / MILLIS_PAR_JOUR);
        today   = new Date(System.currentTimeMillis());

        // ------------------------------------------------------------
        // Tarif actif
        // ------------------------------------------------------------
        tarifs = serviceT.findTarifByMagasin(today, r.getMagasinIdMagasin());
        if (tarifs == null || tarifs.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Aucun tarif actif pour ce magasin aujourd’hui.", null));
            serviceT.close(); serviceR.close(); serviceEA.close(); serviceFD.close(); service.close();
            return null;
        }
        T = tarifs.get(0);

        // ------------------------------------------------------------
        // Transaction
        // ------------------------------------------------------------
        EntityTransaction tx = service.getTransaction();
        tx.begin();
        try {
            // creation de la facture
            fact = new Facture();
            fact.setDateDebut(tsDebut);
            fact.setNumeroFacture(createNumFact());
            fact.setEtat(enumeration.FactureEtatEnum.en_cours);
            fact.setType(enumeration.FactureTypeEnum.Location);
            fact.setMagasinIdMagasin(r.getMagasinIdMagasin());
            fact.setUtilisateurIdUtilisateur(r.getUtilisateurIdUtilisateur());

            // Détail avec tarification (newRent calcule le prix)
            tsRetour = new Timestamp(tsDebut.getTime() + (nbJours * MILLIS_PAR_JOUR));
            serviceEA.loueExemplaire(ea);

            fd = serviceFD.newRent(ea, fact, T, nbJours, tsRetour);
            serviceFD.save(fd);

            // MAJ exemplaire
            ea.setReserve(false);
            serviceEA.save(ea);

            // Total TVAC (un seul détail ici)
            fact.setPrixTVAC(fd.getPrix());

            // Sauvegarde & clôture réservation
            service.save(fact);
            serviceR.markValide(r);

            tx.commit();

            // PDF + mail (post-commit)
            service.refreshEntity(fact);
            mfb     = new ModelFactLoca();
            pdfPath = mfb.creation(fact, fact.getMagasinIdMagasin());

            try {
                String dest = fact.getUtilisateurIdUtilisateur().getCourriel();
                MailUtils.sendWithAttachment(dest,
                        "Facture de location",
                        "Vous trouverez en pièce jointe la facture concernant votre location.",
                        pdfPath);
            } catch (Exception mailEx) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Facture créée, mais l’envoi d’e-mail a échoué.", null));
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Réservation convertie en facture de location.", null));
            retour = "/tableFactureLoca.xhtml?faces-redirect=true";

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur lors de la validation de la réservation.", null));
            retour = null;
        } finally {
            service.close();
        }
        return retour;
    }


    /*Méthode permettant de créer un numéro de facture avec FRC (facture REVEcouture) suivi de l'année, le mois et un nombre a 4 chiffres*/
    public String createNumFact()
    {

        String numFact="";
        Calendar cal = Calendar.getInstance();
        int annee = cal.get(Calendar.YEAR);
        int mois = cal.get(Calendar.MONTH) +1;
        SvcFacture serviceF = new SvcFacture();
        List<Facture> fact;

        //tester si l'année en cours = année de la derniÃ¨re facture
        try
        {
            fact = serviceF.findAllFactureDesc();
            if (!fact.isEmpty()){
                String text = fact.get(0).getNumeroFacture();
                int anneelastFact = Integer.parseInt(text.substring(3, 7));

                if(annee == anneelastFact)
                {
                    int nb = Integer.parseInt(text.substring(text.length() - 5, text.length()));
                    numFact = "FRC" + annee + String.format("%02d", mois) + String.format("%05d",nb+1);
                }
                else
                {
                    numFact = "FRC" + annee + String.format("%02d", mois) + "00001";
                }
            }
            else{
                numFact = "FRC" + annee + String.format("%02d", mois) + "00001";
            }
        }
        catch(NullPointerException npe) {
            npe.printStackTrace();
        }

        return numFact;
    }
    /*
     * Méthode qui permet via le service de retourner la liste de toutes les factures de location
     */
    public List<Facture> getReadAllLocation()
    {
        SvcFacture service = new SvcFacture();
        List<Facture> listFact = new ArrayList<Facture>();
        listFact= service.findAllFactureLocation();

        service.close();
        return listFact;
    }
    /*
     * Méthode qui permet via le service de retourner la liste de toutes les factures de vente
     */
    public List<Facture> getReadAllVente()
    {
        SvcFacture service = new SvcFacture();
        List<Facture> listFact = new ArrayList<Facture>();
        listFact= service.findAllFactureVente();

        service.close();
        return listFact;
    }
    /*
     * Méthode qui permet via le service de retourner la liste de toutes les factures de pénalité
     */
    public List<Facture> getReadAllPenalite()
    {
        SvcFacture service = new SvcFacture();
        List<Facture> listFact = new ArrayList<Facture>();
        listFact= service.findAllFacturePenalite();

        service.close();
        return listFact;
    }
    /*
     * Méthode qui permet le formatage des dates pour le filtre dans l'UI
     */
    public String fmtDate(Date d) {
        if (d == null) return "";
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        f.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Brussels"));
        return f.format(d);
    }
    //méthode qui colle l'heure actuelle a la date fournie.
    public static Date mergeChosenDateWithNow(Date chosenDate) {
        if (chosenDate == null) return null;
        TimeZone tz = TimeZone.getTimeZone("Europe/Brussels");

        Calendar now = Calendar.getInstance(tz);      // heure/minute/… actuelles
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(chosenDate);                      // 00:00 au jour choisi

        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      now.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      now.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND));
        return cal.getTime();
    }

    /*
     * Méthode qui permet de vider les variables et retourne sur la table des factures associée
     * */
    public String flushFactLoca()
    {
        init();
        return "/tableFactureLoca.xhtml?faces-redirect=true";
    }
    public String flushFactVente()
    {
        init();
        return "/tableFactureVente.xhtml?faces-redirect=true";
    }

    //-------------------------------Getter & Setter--------------------------------------------

    public Facture getFacture() {
        log.debug("getFacture");
        return facture;
    }

    public void setFacture(Facture facture) {
        log.debug("setFacture");
        this.facture = facture;
    }

    public List<locationCustom> getListLC() {
        log.debug("getListLC");
        return listLC;
    }

    public void setListLC(List<locationCustom> listLC) {
        log.debug("setListLC");
        log.debug("values : "+listLC );
        this.listLC = listLC;
    }

    public List<venteCustom> getListVC() {
        log.debug("getListVC");
        return listVC;
    }

    public void setListVC(List<venteCustom> listVC) {
        log.debug("setListVC");
        log.debug("values : "+listVC );
        this.listVC = listVC;
    }
    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Magasin getMagasin() {
        log.debug("getMagasin");
        return magasin;
    }

    public void setMagasin(Magasin magasin) {
        log.debug("setMagasin");
        this.magasin = magasin;
    }

    public String getNumMembre() {
        log.debug("getNumMembre");
        return numMembre;
    }

    public void setNumMembre(String numMembre) {
        log.debug("setNumMembre");
        this.numMembre = numMembre;
    }

    public String getCB() {
        log.debug("getCB");
        return CB;
    }

    public void setCB(String CB) {
        log.debug("setCB");
        this.CB = CB;
    }

    public List<TarifPenalite> getTarifsPenalites() {
        log.debug("getTarifsPenalites");
        return tarifsPenalites;
    }

    public void setTarifsPenalites(List<TarifPenalite> tarifsPenalites) {
        log.debug("setTarifsPenalites");
        this.tarifsPenalites = tarifsPenalites;
    }

    public boolean isChoixetat() {
        log.debug("isChoixetat");
        return choixetat;
    }

    public void setChoixetat(boolean choixetat) {
        log.debug("setChoixetat");
        this.choixetat = choixetat;
    }

    public ExemplaireArticle getExemplaireArticle() {
        log.debug("getExemplaireArticle");
        return exemplaireArticle;
    }

    public void setExemplaireArticle(ExemplaireArticle exemplaireArticle) {
        log.debug("setExemplaireArticle");
        this.exemplaireArticle = exemplaireArticle;
    }
}
