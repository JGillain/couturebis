package managedBean;

import entities.*;
import enumeration.ExemplaireArticleEtatEnum;
import enumeration.FactureEtatEnum;
import enumeration.FactureTypeEnum;
import objectCustom.locationCustom;
import objectCustom.venteCustom;
import org.apache.log4j.Logger;
import org.primefaces.event.RowEditEvent;
import pdfTools.ModelFactLoca;
import pdfTools.ModelFactLocaPena;
import pdfTools.ModelFactVente;
import services.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.*;

import javax.mail.internet.InternetAddress;
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
        listLC.add(new locationCustom());
    }

    public void delListRowVente() {
        if (listLC.size() >1)
        {
            listLC.remove(listLC.size()-1);
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

    public void onCbChangeVente(int index) {
        if (listVC == null || index < 0 || index >= listVC.size()) return;

        venteCustom v = listVC.get(index);
        String cb = v.getCB();
        if (cb == null || cb.length() != 13) {
            v.setArticleNom("");
            return;
        }

        SvcArticle serviceA = new SvcArticle();
        try {
            java.util.List<Article> arts = serviceA.findOneByCodeBarre(cb);
            v.setArticleNom((arts == null || arts.isEmpty()) ? "" : arts.get(0).getNom());
        } finally {
            serviceA.close();
        }
    }

    //methode qui permet le téléchargement sur les machines client des factures
    public void downloadPdf() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        String numFacture = ec.getRequestParameterMap().get("numFacture");
        if (numFacture == null || numFacture.isEmpty()) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Téléchargement", "Numéro de facture manquant."));
            return;
        }

        Path file = Paths.get("C:"+ File.separator +"Facture", numFacture + ".pdf");

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

    // Méthode qui permet l'envoi d'un mail via le mail du magasin avec la facture
    public static void sendMessage(String absolutePdfPath,
                                   String destEmail,
                                   String bodyText,
                                   String subject) {
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "revecouture1990@gmail.com";
        String password = "qcukddisvzhbkdfb";




        try {

            java.nio.file.Path p = java.nio.file.Paths.get(absolutePdfPath);
            if (!java.nio.file.Files.isReadable(p)) {
                throw new java.io.FileNotFoundException("PDF introuvable: " + absolutePdfPath);
            }
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        @Override protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                            return new javax.mail.PasswordAuthentication(username, password);
                        }
                    });

            javax.mail.internet.MimeMessage msg = new javax.mail.internet.MimeMessage(session);
            msg.setFrom(new InternetAddress(username));
            msg.setRecipients(javax.mail.Message.RecipientType.TO,
                    javax.mail.internet.InternetAddress.parse(destEmail, false));
            msg.setSubject(subject, "UTF-8");

            javax.mail.internet.MimeBodyPart text = new javax.mail.internet.MimeBodyPart();
            text.setText(bodyText, "UTF-8");

            javax.mail.internet.MimeBodyPart attach = new javax.mail.internet.MimeBodyPart();
            javax.activation.DataSource src = new javax.activation.FileDataSource(absolutePdfPath);
            attach.setDataHandler(new javax.activation.DataHandler(src));
            attach.setFileName(new java.io.File(absolutePdfPath).getName());

            javax.mail.Multipart mp = new javax.mail.internet.MimeMultipart();
            mp.addBodyPart(text);
            mp.addBodyPart(attach);
            msg.setContent(mp);

            javax.mail.Transport.send(msg);
            log.info("Mail sent to " + destEmail + " with " + absolutePdfPath);
        } catch (Exception e) {
            log.error("Email send failed to " + destEmail + " (file: " + absolutePdfPath + ")", e);
            javax.faces.context.FacesContext fc = javax.faces.context.FacesContext.getCurrentInstance();
            if (fc != null) {
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new javax.faces.application.FacesMessage(
                        javax.faces.application.FacesMessage.SEVERITY_WARN,
                        "Facture créée mais l’envoi du mail a échoué.",
                        "Vous pouvez télécharger la facture depuis l’application." ));
            }
        }
    }

    // Méthode qui permet de créer une facture de location
    public String newFactLoca()
    {
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

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        List<Tarif> tarifs = java.util.Collections.emptyList();

        if (!missingMagasin) {
            tarifs = serviceT.findTarifByMagasin(today, magasin);
            noTarif = (tarifs == null || tarifs.isEmpty());
        }

        for (locationCustom lc : listLC) {
            List<ExemplaireArticle> found = serviceEA.findOneByCodeBarre(lc.getCB()); // NOTE: method name spelled “Barre”
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
                String path = "c:\\REVEcouture\\Facture\\"+fact.getNumeroFacture() + ".pdf";
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
                MFB.creation(fact, magasin);

                sendMessage(path,fact.getUtilisateurIdUtilisateur().getCourriel(),"Facture de location","vous trouverez la facture concernant votre location en piece jointe");
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
            String path = "c:\\Facture\\"+fact.getNumeroFacture() + ".pdf";
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
            MFB.creation(fact,tarifsPenalites,factdetretard, magasin);
            sendMessage(path,fact.getUtilisateurIdUtilisateur().getCourriel(),"Facture de pénalité","vous trouverez la facture concernant les pénalités suite a votre location en piece jointe");
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
                String pdfPath = mfv.creation(fact, magasin); // retourne "C:\\REVECouture\\facture\\FRC....pdf"

                sendMessage(
                        pdfPath,
                        fact.getUtilisateurIdUtilisateur().getCourriel(),
                        "Facture de vente",
                        "Vous trouverez la facture concernant votre achat en pièce jointe."
                );

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
                if (tx.isActive()) {
                    tx.rollback();
                }
                log.error("Erreur finaliserVente()", e);
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Erreur fatale",
                        "La vente n'a pas pu être enregistrée."));
                return "";
            }

        } finally {
            serviceFD.close();
            serviceEA.close();
            serviceA.close();
            serviceU.close();
            service.close();
        }
    }

    public String redirectChoix(){
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        exemplaireArticle = serviceEA.findOneByCodeBarre(CB).get(0);
        tarifsPenalites= new ArrayList<>();
        if (choixetat){

            SvcFacture serviceF = new SvcFacture();
            SvcTarif serviceT = new SvcTarif();
            SvcTarifPenalite serviceTP = new SvcTarifPenalite();
            List<Facture> facture1 = serviceF.findActiveByExemplaireArticle(exemplaireArticle);
            if(facture1.isEmpty())
            {
                serviceT.close();
                serviceTP.close();
                serviceF.close();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"erreur","facture initialle non trouvee"));
                return "";
            }
            else{
                Date date = facture1.get(0).getDateDebut();
                tarifsPenalites= serviceTP.FindTarifPenaByTarifByArticle(date,serviceT.findTarifByMagasin(date, magasin).get(0), exemplaireArticle.getArticleIdArticle());
                serviceT.close();
                serviceTP.close();
                serviceF.close();
                return "/formEtatArticle.xhtml?faces-redirect=true";
            }
        }
        else {
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
        boolean flag =false;
        if (exemplaireArticle.getLoue())
        {
            for (FactureDetail fd : exemplaireArticle.getFactureDetails()){
                if (fd.getDateRetour() == null) {
                    facturesDetail = fd;
                    numMembre=fd.getFactureIdFacture().getUtilisateurIdUtilisateur().getCodeBarreIdCB().getCodeBarre();
                    flag=true;
                }
            }
            if (flag)
            {
                flag=false;
                facturesDetail.setDateRetour(timestampretour);
                if (facturesDetail.getDateRetour().after(facturesDetail.getDateFin()) || tarifsPenalites.size()>=1)
                {

                    newFactPena(facturesDetail);
                }
                for (FactureDetail fd: facturesDetail.getFactureIdFacture().getFactureDetails())
                {
                    if (fd.getDateRetour() == null) {
                        flag = true;
                        break;
                    }
                }
                if (!flag){
                    fact = facturesDetail.getFactureIdFacture();
                    fact.setEtat(FactureEtatEnum.terminer);
                }
                SvcFacture service = new SvcFacture();
                SvcFactureDetail serviceFD = new SvcFactureDetail();
                serviceEL.setEm(service.getEm());
                serviceFD.setEm(service.getEm());
                EntityTransaction transaction = service.getTransaction();
                transaction.begin();
                try {
                    exemplaireArticle.setLoue(false);
                    if (exemplaireArticle.getEtat()== ExemplaireArticleEtatEnum.Mauvais)
                    {
                        exemplaireArticle.setActif(false);
                    }
                    serviceEL.save(exemplaireArticle);
                    serviceFD.save(facturesDetail);
                    if (fact.getEtat()==FactureEtatEnum.terminer){
                        service.save(fact);
                    }
                    transaction.commit();
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"retour accepté",null));
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
        }
        else
        {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"l'article n'est pas loue",null));
            init();
        }
        return "/tableFactureLoca.xhtml?faces-redirect=true";
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
        log.debug("getListLC");
        return listVC;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public void setListVC(List<venteCustom> listVC) {
        log.debug("setListLC");
        log.debug("values : "+listVC );
        this.listVC = listVC;
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
