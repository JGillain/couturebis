package managedBean;

import entities.*;
import enumeration.ExemplaireArticleEtatEnum;
import enumeration.FactureEtatEnum;
import enumeration.FactureTypeEnum;
import objectCustom.locationCustom;
import org.apache.log4j.Logger;
import pdfTools.ModelFactLoca;
import pdfTools.ModelFactLocaPena;
import services.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.*;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
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
    private List<TarifPenalite> tarifsPenalites;
    private String numMembre;
    private String CB;
    private boolean choixetat;
    @Inject
    private MagasinBean magasinBean;
    private Magasin magasin;
    private ExemplaireArticle exemplaireArticle;

    @PostConstruct
    public void init(){
        listLC = new ArrayList<>();
        addNewListRow();
        facture = new Facture();
        numMembre= "";
        CB= "";
        magasin = magasinBean.getMagasin();
    }
    public void addNewListRow() {
        listLC.add(new locationCustom());
    }

    public void delListRow() {
        if (listLC.size() >1)
        {
            listLC.remove(listLC.size()-1);
        }
    }
    /*TODO : Penser a mettre les fonctions d'envoi de mail dans une même classe*/
    // Méthode qui permet l'envoi d'un mail via le mail de la bibliotheque avec la facture
    public static void sendMessage( String filename, String mailDest, String Texte, String Titre)  {
        //Création de la session
        final String host = "sandbox.smtp.mailtrap.io";
        final int port = 587;
        final String user = "api"; // or hardcode while testing
        final String pass = "850fd96712000a9cc38e9f2bf1162e65";

        Path attachment = Paths.get(System.getProperty("user.dir"),
                        "src","main","webapp","Factures", filename)
                .normalize();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@yourapp.local")); // any sender for Mailtrap
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailDest, false));
            message.setSubject(Titre, "UTF-8");

            // multipart: text + optional attachment
            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(Texte, "UTF-8");
            multipart.addBodyPart(textPart);

            if (Files.exists(attachment)) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(attachment.toFile());
                attachPart.setFileName(filename);
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);

            // Use STARTTLS SMTP (not "smtps")
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace(); // or log.error("mail failed", e);
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
        ModelFactLoca MFB =new ModelFactLoca();
        Tarif T= new Tarif();
        Utilisateur u = serviceU.getByNumMembre(numMembre).get(0);
        boolean flag = false;
        if(magasin ==null){
            flag=true;
        }
        else if (serviceT.getTarifByMagasin(date, magasin.getNom()).size()==0){
            flag=true;
        }
        else {
            T = serviceT.getTarifByMagasin(date, magasin.getNom()).get(0);
        }
        //vÃ©rif si livre non louÃ©
        for (locationCustom lc: listLC) {
            if (serviceEA.findOneByCodeBare(lc.getCB()).get(0).getLoue()){
                flag=true;
                break;
            }
        }

        if (!flag) {
            //initialisation de la transaction
            EntityTransaction transaction = service.getTransaction();
            transaction.begin();
            try {
                //crÃ©ation de la facture
                fact.setDateDebut(timestampdebut);
                fact.setNumeroFacture(createNumFact());
                String path = "Factures\\" + fact.getNumeroFacture() + ".pdf";
                fact.setLienPdf(path);
                fact.setEtat(FactureEtatEnum.en_cours);
                fact.setType(FactureTypeEnum.Location);
                fact.setMagasinIdMagasin(magasin);
                fact.setUtilisateurIdUtilisateur(u);
                // parcour de la liste des location a inscrire dans la facture
                for (locationCustom lc : listLC) {
                    //crÃ©ation des dÃ©tails de la facture
                    ExemplaireArticle ea = serviceEA.findOneByCodeBare(lc.getCB()).get(0);
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
                sendMessage(fact.getNumeroFacture()+".pdf",fact.getUtilisateurIdUtilisateur().getCourriel(),"vous trouverez la facture concernant votre location en piece jointe","Facture de location");
                return "/tableFactureLoca.xhtml?faces-redirect=true";
            } finally {
                //bloc pour gÃ©rer les erreurs lors de la transactions
                if (transaction.isActive()) {
                    transaction.rollback();
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"l'operation n'a pas reussie",null));
                    return "";
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
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"une erreur est survenue, soit Le livre est déjà loué ou une information est manquante (tarif, biblotheque)",null));
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
        Tarif T = serviceT.getTarifByMagasin(Date.from(facturesDetail.getFactureIdFacture().getDateDebut().toInstant()), magasin.getNom()).get(0);
        Utilisateur u = serviceU.getByNumMembre(numMembre).get(0);



        //initialisation de la transaction
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            //création de la facture
            fact.setDateDebut(timestampfacture);
            fact.setNumeroFacture(createNumFact());
            String path = "Factures\\" + fact.getNumeroFacture() + ".pdf";
            fact.setLienPdf(path);
            fact.setEtat(FactureEtatEnum.terminer);
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

                if (serviceTP.findByPena(T,serviceP.findByName("Retard").get(0),Date.from(facturesDetail.getFactureIdFacture().getDateDebut().toInstant()),facturesDetail.getExemplaireArticleIdEA().getArticleIdArticle()).size() >= 1){
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
            sendMessage(fact.getNumeroFacture()+".pdf",fact.getUtilisateurIdUtilisateur().getCourriel(),"vous trouverez la facture concernant les pénalités suite a votre location en piece jointe","Facture de pénalité");
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

    public String redirectChoix(){
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        exemplaireArticle = serviceEA.findOneByCodeBare(CB).get(0);
        tarifsPenalites= new ArrayList<>();
        if (choixetat){
            Date date = new Date();
            SvcTarif serviceT = new SvcTarif();
            tarifsPenalites= (List<TarifPenalite>) serviceT.getTarifByMagasin(date, magasin.getNom()).get(0).getTarifPenalite();
            return "/formEtatArticle.xhtml?faces-redirect=true";
        }
        else {
            return retourArticleLoca();
        }
    }

    // Méthode qui permet
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
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"retour ok",null));
                } finally {
                    if (transaction.isActive()) {
                        transaction.rollback();
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.addMessage("Erreur", new FacesMessage("l'operation n'est pas reussie"));
                    }
                    service.close();
                }
            }
        }
        else
        {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Le livre n'est pas loue",null));
        }
        return "/tableFactureLoca.xhtml?faces-redirect=true";
    }

    /*Méthode permettant de créer un numéro de facture avec FB(FactureBiblio) suivi de l'année, le mois et un nombre a 4 chiffres*/
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
            if (fact.size() != 0){
                String text = fact.get(0).getNumeroFacture();
                int anneelastFact = Integer.parseInt(text.substring(2, 6));

                if(annee == anneelastFact)
                {
                    int nb = Integer.parseInt(text.substring(text.length() - 5, text.length()));
                    numFact = "FB" + annee + String.format("%02d", mois) + String.format("%05d",nb+1);
                }
                else
                {
                    numFact = "FB" + annee + String.format("%02d", mois) + "00001";
                }
            }
            else{
                numFact = "FB" + annee + String.format("%02d", mois) + "00001";
            }
        }
        catch(NullPointerException npe) {
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
     * Méthode qui permet de vider les variables et retourne sur la table des factures
     * */
    public String flushFact()
    {
        init();
        return "/tableFactureLoca.xhtml?faces-redirect=true";
    }

    //-------------------------------Getter & Setter--------------------------------------------

    public Facture getFacture() {
        return facture;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
    }

    public List<locationCustom> getListLC() {
        return listLC;
    }

    public void setListLC(List<locationCustom> listLC) {
        this.listLC = listLC;
    }

    public Magasin getMagasin() {
        return magasin;
    }

    public void setMagasin(Magasin magasin) {
        this.magasin = magasin;
    }

    public String getNumMembre() {
        return numMembre;
    }

    public void setNumMembre(String numMembre) {
        this.numMembre = numMembre;
    }

    public String getCB() {
        return CB;
    }

    public void setCB(String CB) {
        this.CB = CB;
    }

    public List<TarifPenalite> getTarifsPenalites() {
        return tarifsPenalites;
    }

    public void setTarifsPenalites(List<TarifPenalite> tarifsPenalites) {
        this.tarifsPenalites = tarifsPenalites;
    }

    public boolean isChoixetat() {
        return choixetat;
    }

    public void setChoixetat(boolean choixetat) {
        this.choixetat = choixetat;
    }

    public ExemplaireArticle getExemplaireArticle() {
        return exemplaireArticle;
    }

    public void setExemplaireArticle(ExemplaireArticle exemplaireArticle) {
        this.exemplaireArticle = exemplaireArticle;
    }
}
