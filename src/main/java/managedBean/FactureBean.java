package managedBean;

import entities.*;
import enumeration.ExemplaireArticleEtatEnum;
import enumeration.FactureEtatEnum;
import enumeration.FactureTypeEnum;
import enumeration.ReservationStatutEnum;
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

    // Méthode qui permet de créer une facture de location
    public String newFactLoca()
    {
        //verification si doublons presents
        List<String> dupCbs = new ArrayList<>();
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
            return "/formNewFactLoca.xhtml?faces-redirect=true";
        }
        //initialisation des services requis
        SvcFacture service =new SvcFacture();
        SvcFactureDetail serviceFD = new SvcFactureDetail();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        SvcUtilisateur serviceU = new SvcUtilisateur();
        SvcTarif serviceT = new SvcTarif();
        SvcJours serviceJ = new SvcJours();

        //rassemblement des entity managers pour la transaction
        serviceFD.setEm(service.getEm());
        serviceEA.setEm(service.getEm());

        //initialisation des object et variables
        double prixTVAC = 0;
        long now =  System.currentTimeMillis();
        long rounded = now - now % 60000;
        Timestamp timestampdebut = new Timestamp(rounded);
        Date date = new Date();
        Facture fact = new Facture();
        Tarif T = new Tarif();
        ModelFactLoca MFB =new ModelFactLoca();
        Utilisateur u = serviceU.getByNumMembre(numMembre).get(0);
        boolean noTarif = false;
        boolean missingMagasin = (magasin == null);
        boolean alreadyLoue = false;
        boolean missingExemplaire = false;

        Date today = new Date(System.currentTimeMillis());
        List<Tarif> tarifs = Collections.emptyList();

        if (!missingMagasin) {
            tarifs = serviceT.findTarifByMagasin(today, magasin);
            noTarif = (tarifs == null || tarifs.isEmpty());
        }

        for (locationCustom lc : listLC) {
            List<ExemplaireArticle> found = serviceEA.findOneByCodeBarre(lc.getCB());
            if (found == null || found.isEmpty()) {
                missingExemplaire = true;
                break;
            }
            if (Boolean.TRUE.equals(found.get(0).getLoue())) {
                alreadyLoue = true;
                break;
            }
        }

        boolean flag = missingMagasin || noTarif || missingExemplaire || alreadyLoue;

        if (!flag) {
            T=tarifs.get(0);
            //initialisation de la transaction
            EntityTransaction transaction = service.getTransaction();
            transaction.begin();
            try {
                //crÃ©ation de la facture
                fact.setDateDebut(timestampdebut);
                fact.setNumeroFacture(createNumFact());
                fact.setEtat(FactureEtatEnum.en_cours);
                fact.setType(FactureTypeEnum.Location);
                fact.setMagasinIdMagasin(magasin);
                fact.setUtilisateurIdUtilisateur(u);
                // parcour de la liste des location a inscrire dans la facture
                for (locationCustom lc : listLC) {
                    //crÃ©ation des dÃ©tails de la facture
                    ExemplaireArticle ea = serviceEA.findOneByCodeBarre(lc.getCB()).get(0);
                    serviceEA.loueExemplaire(ea);
                    Timestamp timestampretour = new Timestamp(rounded + (lc.getNbrJours() * 24 * 3600 * 1000));
                    FactureDetail Factdet = serviceFD.newRent(ea, fact, T, lc.getNbrJours(), timestampretour);
                    serviceFD.save(Factdet);
                    serviceEA.save(ea);
                    prixTVAC = prixTVAC + Factdet.getPrix();
                }

                fact.setPrixTVAC(prixTVAC);

                // sauvegarde de la facture et commit de transaction
                service.save(fact);
                transaction.commit();
                service.refreshEntity(fact);
                String path=MFB.creation(fact, magasin);
                MailUtils.sendWithAttachment(fact.getUtilisateurIdUtilisateur().getCourriel(),"Facture de location","vous trouverez la facture concernant votre location en piece jointe",path);
                return "/tableFactureLoca.xhtml?faces-redirect=true";
            }catch (Exception e){
                log.error("Erreur pendant la creation de la facture ", e);
                return "";
            }
            finally {
                //bloc pour gérer les erreurs lors de la transactions
                if (transaction.isActive()) {

                    transaction.rollback();
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"l'operation n'a pas reussie",null));
                }
                //fermeture des service
                service.close();
                serviceJ.close();
                serviceU.close();
                serviceT.close();
            }
        }
        else {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"une erreur est survenue, soit l'article est déjà loué ou une information est manquante (tarif, magasin)",null));
            return "/formNewFactLoca.xhtml?faces-redirect=true";
        }
    }

    // Méthode qui permet de créer une facture pénalité
    public void newFactPena(FactureDetail facturesDetail)
    {

        //initialisation des services requis

        SvcFacture service =new SvcFacture();
        SvcFactureDetail serviceFD = new SvcFactureDetail();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        SvcUtilisateur serviceU = new SvcUtilisateur();
        SvcTarif serviceT = new SvcTarif();
        SvcPenalite serviceP = new SvcPenalite();
        SvcTarifPenalite serviceTP = new SvcTarifPenalite();

        //rassemblement des entity managers pour la transaction

        serviceFD.setEm(service.getEm());
        serviceEA.setEm(service.getEm());

        //initialisation des object et variables
        double prixTVAC = 0;
        long now =  System.currentTimeMillis();
        long rounded = now - now % 60000;
        Timestamp timestampfacture = new Timestamp(rounded);

        Date date = new Date();

        Facture fact = new Facture();
        FactureDetail factdet= new FactureDetail();
        FactureDetail factdetretard= null;
        ModelFactLocaPena MFB =new ModelFactLocaPena();
        Tarif T = serviceT.findTarifByMagasin(Date.from(facturesDetail.getFactureIdFacture().getDateDebut().toInstant()), magasin).get(0);
        Utilisateur u = serviceU.getByNumMembre(numMembre).get(0);



        //initialisation de la transaction
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            //création de la facture
            fact.setDateDebut(timestampfacture);
            fact.setNumeroFacture(createNumFact());
            fact.setEtat(FactureEtatEnum.terminer);
            fact.setType(FactureTypeEnum.Penalite);
            fact.setMagasinIdMagasin(magasin);
            fact.setUtilisateurIdUtilisateur(u);

            //création des facture dÃ©tails
            if (tarifsPenalites.size() >= 1){
                for (TarifPenalite tp: tarifsPenalites)
                {
                    factdet=serviceFD.newPena(facturesDetail.getExemplaireArticleIdEA(),fact,T, tp.getPenaliteIdPenalite(), Date.from(facturesDetail.getFactureIdFacture().getDateDebut().toInstant()),timestampfacture);
                    prixTVAC=prixTVAC+factdet.getPrix();
                    serviceFD.save(factdet);
                }
            }

            if (facturesDetail.getDateRetour().after(facturesDetail.getDateFin())){
                int nbjour = (int)((facturesDetail.getDateRetour().getTime() - facturesDetail.getDateFin().getTime())/(1000*60*60*24));

                if (serviceTP.findByPenalitesByArticle(T,serviceP.findByName("Retard").get(0),Date.from(facturesDetail.getFactureIdFacture().getDateDebut().toInstant()),facturesDetail.getExemplaireArticleIdEA().getArticleIdArticle()).size() >= 1){
                    factdetretard= serviceFD.newPenaretard(facturesDetail.getExemplaireArticleIdEA() , fact , T , serviceP.findByName("Retard").get(0) , nbjour , Date.from(facturesDetail.getFactureIdFacture().getDateDebut().toInstant()),timestampfacture);
                    prixTVAC=prixTVAC+factdetretard.getPrix();
                    serviceFD.save(factdetretard);
                }

            }

            fact.setPrixTVAC(prixTVAC);
            // sauvegarde de la facture et commit de transaction
            service.save(fact);
            transaction.commit();
            //refresh pour récupérer les collections associÃ©es
            service.refreshEntity(fact);
            String path = MFB.creation(fact,tarifsPenalites,factdetretard, magasin);
            MailUtils.sendWithAttachment(fact.getUtilisateurIdUtilisateur().getCourriel(),"Facture de pénalité","vous trouverez la facture concernant les pénalités suite a votre location en piece jointe",path);
        }
        finally {
            //bloc pour gérer les erreurs lors de la transactions
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Erreur fatale",null));
            }
            //fermeture des service
            service.close();
            serviceP.close();
            serviceU.close();
            serviceT.close();
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
            return "/venteConfirm.xhtml?faces-redirect=true";

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
                        tarifsPenalites = serviceTP.FindTarifPenaByTarifByArticle(
                                dateDebutFacture, tarifs.get(0), exemplaireArticle.getArticleIdArticle());
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
    public String retourArticleLoca() {
        // Variables locales
        FactureDetail        facturesDetail = null;
        SvcExemplaireArticle serviceEL      = new SvcExemplaireArticle();
        SvcReservation       serviceR       = new SvcReservation();     // service pour les réservations (FIFO)
        Facture              fact           = new Facture();
        long now = System.currentTimeMillis();
        long rounded = now - now % 60000;
        Timestamp timestampretour = new Timestamp(rounded);
        boolean flag = false;

        // Réservation promue (pour envoi mail après commit)
        Reservation resPromo = null;
        String mailDest = null;

        // Vérifications de base
        if (Boolean.TRUE.equals(exemplaireArticle.getLoue())) {
            // Trouver le détail "ouvert" (sans DateRetour)
            for (FactureDetail fd : exemplaireArticle.getFactureDetails()) {
                if (fd.getDateRetour() == null) {
                    facturesDetail = fd;
                    // Récup du membre pour d’autres flux (inchangé)
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

                // Marquer le retour sur le détail
                facturesDetail.setDateRetour(timestampretour);

                // Pénalité si retard OU si une pénalité a déjà été identifiée côté "état"
                if (facturesDetail.getDateRetour().after(facturesDetail.getDateFin()) || (tarifsPenalites != null && !tarifsPenalites.isEmpty())) {
                    newFactPena(facturesDetail);
                }

                // Si TOUS les détails ont une DateRetour => facture terminée
                for (FactureDetail fd : facturesDetail.getFactureIdFacture().getFactureDetails()) {
                    if (fd.getDateRetour() == null) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    fact = facturesDetail.getFactureIdFacture();
                    fact.setEtat(FactureEtatEnum.terminer);
                }

                // Transaction pour sauver le retour + (éventuelle) promo de réservation
                SvcFacture service   = new SvcFacture();
                SvcFactureDetail serviceFD = new SvcFactureDetail();
                // Partager le même EM
                serviceEL.setEm(service.getEm());
                serviceFD.setEm(service.getEm());
                serviceR .setEm(service.getEm());

                EntityTransaction transaction = service.getTransaction();
                transaction.begin();
                try {
                    // Libérer l'exemplaire (retour)
                    exemplaireArticle.setLoue(false);
                    if (exemplaireArticle.getEtat() == ExemplaireArticleEtatEnum.Mauvais) {
                        exemplaireArticle.setActif(false);
                    }
                    serviceEL.save(exemplaireArticle);

                    // Sauver le détail mis à jour
                    serviceFD.save(facturesDetail);

                    // Sauver la facture si terminée
                    if (fact.getEtat() == FactureEtatEnum.terminer) {
                        service.save(fact);
                    }

                    // ----------------------------------------------------
                    // Promotion d'une réservation FIFO (si présente)
                    // ----------------------------------------------------
                    try {
                        Reservation next = serviceR.findNextByArticleMagasin(
                                exemplaireArticle.getArticleIdArticle(),
                                exemplaireArticle.getMagasinIdMagasin()
                        );
                        if (next != null) {
                            // Mettre de côté l'exemplaire
                            exemplaireArticle.setReserve(true);
                            serviceEL.save(exemplaireArticle);

                            // Passer la réservation en "prêt"
                            Date nowDate = new Date();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(nowDate);
                            cal.add(Calendar.DAY_OF_MONTH, 3);

                            next.setStatut(ReservationStatutEnum.pret);
                            next.setExemplaire(exemplaireArticle);
                            next.setDateReady(nowDate);
                            next.setHoldUntil(cal.getTime());
                            next.setMailEnvoye(false);

                            serviceR.save(next);

                            // Pour l'envoi post-commit
                            resPromo = next;
                            mailDest = next.getUtilisateurIdUtilisateur().getCourriel();
                        }
                    } catch (Exception ignore) {
                        // En cas d'erreur de promotion, ne pas bloquer le retour
                    }

                    // Commit global
                    transaction.commit();

                    // --------------------------------------------
                    // Après commit : envoyer l'email de retrait
                    // --------------------------------------------
                    if (resPromo != null && mailDest != null) {
                        try {
                            String sujet = "Votre réservation est prête";
                            String corps = "Bonjour,\n\nVotre article réservé est disponible au magasin " + magasin.getNom() + " pendant 3 jours.\n"
                                    + "Article : " + resPromo.getArticleIdArticle().getNom() + "\n"
                                    + "Date limite : " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(resPromo.getHoldUntil()) + "\n\n"
                                    + "Merci de votre confiance.";
                            MailUtils.sendText(mailDest, sujet, corps);

                            // Marquer 'mailEnvoye = true' dans une petite transaction séparée
                            EntityTransaction tx2 = service.getTransaction();
                            tx2.begin();
                            try {
                                resPromo.setMailEnvoye(true);
                                serviceR.save(resPromo);
                                tx2.commit();
                            } catch (Exception e2) {
                                if (tx2.isActive()) tx2.rollback();
                                FacesContext.getCurrentInstance().addMessage(null,
                                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                                "Mail envoyé mais l’indicateur 'mail envoyé' n’a pas pu être enregistré.", null));
                            }
                        } catch (Exception mailEx) {
                            FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                                            "Réservation mise de côté, mais l’envoi du mail a échoué.", null));
                        }
                    }

                    // Message succès
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "retour accepté", null));

                } finally {
                    // Gestion rollback si nécessaire
                    if (transaction.isActive()) {
                        transaction.rollback();
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.addMessage("Erreur", new FacesMessage("l'opération n'a pas réussi"));
                    }
                    // Fermeture des services + flush donees
                    serviceR.close();
                    serviceFD.close();
                    service.close();
                    init();
                }

            }
        } else {
            // Cas : pas loué
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "l'article n'est pas loué", null));
            init();
        }

        return "/tableFactureLoca.xhtml?faces-redirect=true";
    }


    public String createFromReservation(Reservation r, Date dateFin) {

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
            serviceT.close(); serviceR.close(); serviceEA.close(); serviceFD.close(); service.close();
            return null;
        }
        ea = r.getExemplaire();
        if (Boolean.TRUE.equals(ea.getLoue())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "L’exemplaire est déjà loué.", null));
            serviceT.close(); serviceR.close(); serviceEA.close(); serviceFD.close(); service.close();
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
            ea.setLoue(true);
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
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur lors de la validation de la réservation.", null));
            retour = null;
        } finally {
            serviceT.close(); serviceR.close(); serviceEA.close(); serviceFD.close(); service.close();
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
