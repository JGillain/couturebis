package managedBean;

import entities.*;

import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import security.SecurityManager;
import services.SvcRole;
import services.SvcUtilisateur;
import services.SvcUtilisateurAdresse;
import services.SvcUtilisateurRole;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class UtilisateurBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(UtilisateurBean.class);

    private Utilisateur utilisateur;
    private List<Utilisateur> listUtil = new ArrayList<>();
    private List<Utilisateur> listCli = new ArrayList<>();
    private List<Utilisateur> searchResults;
    private String numMembre;
    private Adresse adresses;
    private Role role;
    private UtilisateurAdresse UA;
    private UtilisateurRole UR;
    private String mdpNouveau;
    private String mdpNouveau2;

    public UtilisateurBean() {
        super();
    }

    @PostConstruct
    public void init() {

        listUtil = getReadAllUtil();
        listCli = getReadAllCli();
        utilisateur = new Utilisateur();
        UA = new UtilisateurAdresse();
        UR = new UtilisateurRole();
        adresses = new Adresse();
        role = new Role();
        SvcUtilisateur service = new SvcUtilisateur();
        if (service.findlastMembre().size()==0){
            numMembre = "0";
        }
        else {
            numMembre="0";
        }
        service.close();


    }

    public String redirectModifUtil(){
        for (UtilisateurAdresse ua: utilisateur.getUtilisateurAdresse()) {
            if(ua.getActif()){
                adresses=ua.getAdresseIdAdresse();
            }
        }
        return "/formEditUtilisateur.xhtml?faces-redirect=true";
    }

    public void saveActif() {
        SvcUtilisateur service = new SvcUtilisateur();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            service.save(utilisateur);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("messageGenre", new FacesMessage("Modification réussie"));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("messageGenre", new FacesMessage("le rollback a pris le relais"));
            }

            service.close();
        }

    }

    public void saveUtilisateur() {
        SvcUtilisateur service = new SvcUtilisateur();
        SvcUtilisateurAdresse serviceUA = new SvcUtilisateurAdresse();
        SvcUtilisateurRole serviceUR = new SvcUtilisateurRole();
        serviceUA.setEm(service.getEm());
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            service.save(utilisateur);

            if(utilisateur.getId()!=0) {
                for (UtilisateurAdresse utiladress : utilisateur.getUtilisateurAdresse()) {
                    if (!utiladress.equals(UA) && utiladress.getActif()) {
                        utiladress.setActif(false);
                        serviceUA.save(utiladress);
                    }
                }
                for (UtilisateurRole utilrole : utilisateur.getUtilisateurRole()) {
                    if (!utilrole.equals(UR) && utilrole.getActif()) {
                        utilrole.setActif(false);
                        serviceUR.save(utilrole);
                    }
                }
            }
            serviceUR.save(UR);
            serviceUA.save(UA);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation a reussie",null));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation n'a pas reussie",null));
            }

            service.close();
        }

    }
    public String modifMdp()
    {
        SvcUtilisateur serviceU = new SvcUtilisateur();
        EntityTransaction transaction = serviceU.getTransaction();
        log.debug("utilisateur objet : " + utilisateur);
        log.debug("utilisateur findbyLogin : " + serviceU.findByLogin(utilisateur.getLogin()).get(0).getMdp());
        log.debug("utilisateur getmdp : " + utilisateur.getMdp());

        try
        {
            if(!mdpNouveau.equals(mdpNouveau2))
            {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Le nouveau mot de passe et la confirmation ne correspondent pas", null));
            }
            else
            {
                if (utilisateur.getMdp().equals(serviceU.findByLogin(utilisateur.getLogin()).get(0).getMdp())) {
                    if (SecurityManager.PasswordMatch(mdpNouveau, utilisateur.getMdp())) {
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.getExternalContext().getFlash().setKeepMessages(true);
                        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "le mot de passe est le même que précédemment", null));
                    } else {

                        transaction.begin();
                        utilisateur.setMdp(mdpNouveau);
                        serviceU.save(utilisateur);
                        transaction.commit();
                        FacesContext fc = FacesContext.getCurrentInstance();
                        fc.getExternalContext().getFlash().setKeepMessages(true);
                        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "L'operation a reussie", null));

                    }

                }
            }
        }
        catch (NullPointerException npe)
        {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Une erreur est survenue(erreur 101), veuillez contacter le support technique", null));
        }
        finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "L'operation n'a pas reussie", null));
            }
            serviceU.close();
        }

        init();
        return "/tableUtilisateurs.xhtml?faces-redirect=true";
    }

    public String newUtil() {
        boolean flag = false;
        SvcUtilisateurAdresse serviceUA = new SvcUtilisateurAdresse();
        SvcUtilisateurRole serviceUR = new SvcUtilisateurRole();
        utilisateur.setNom(utilisateur.getNom().substring(0,1).toUpperCase() + utilisateur.getNom().substring(1));
        utilisateur.setPrenom(utilisateur.getPrenom().substring(0,1).toUpperCase() + utilisateur.getPrenom().substring(1));


        //log.debug((SecurityManager.encryptPassword(utilisateur.getMdp())));

        //PasswordMatcher matcher = new PasswordMatcher();
        //log.debug(matcher.getPasswordService().passwordsMatch(utilisateur.getMdp(),SecurityManager.encryptPassword(utilisateur.getMdp())));

        log.debug("utilisateur objet : " + utilisateur);
        if (utilisateur.getId()!=0) {
            for (UtilisateurAdresse ua : utilisateur.getUtilisateurAdresse()) {
                if (ua.getAdresseIdAdresse().equals(adresses)) {
                    flag = true;
                    UA = ua;
                    break;
                }
            }
        }
        if (!flag){
            UA = serviceUA.createUtilisateurAdresse(utilisateur, adresses);
            UR = serviceUR.createUtilisateurRole(utilisateur, role);
        }
        if(verifUtilExist(utilisateur)) {
            UA.setActif(true);
            saveUtilisateur();
        }else {

            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'utilisateur existe déjà tel quel en DB; opération échouée",null));
        }

            init();
            return "/tableUtilisateurs.xhtml?faces-redirect=true";

    }


    //todo : test the function to check if user exist in the case of changed roles

    public boolean verifUtilExist(Utilisateur util)
    {
        SvcUtilisateur serviceU = new SvcUtilisateur();
        boolean flag= false;
        if (util.getId()!=0) {
            for (UtilisateurAdresse ua : util.getUtilisateurAdresse()) {
                if (ua.getAdresseIdAdresse().equals(adresses) && ua.getActif()) {
                    flag = true;
                    break;
                }
            }
            if (serviceU.findOneUtilisateur(util).size() > 0 && flag) {
                serviceU.close();
                return false;
            } else {
                serviceU.close();
                return true;
            }
        }
        else {
            if (serviceU.findOneUtilisateur(util).size() > 0) {
                serviceU.close();
                return false;
            } else {
                serviceU.close();
                return true;
            }
        }
    }

    //todo : correct function for creating new client
/*
    public String newUtilCli() {
        boolean flag = false;
        SvcUtilisateurAdresse serviceUA = new SvcUtilisateurAdresse();
        SvcRole serviceR = new SvcRole();
        utilisateur.setNom(utilisateur.getNom().substring(0,1).toUpperCase() + utilisateur.getNom().substring(1));
        utilisateur.setPrenom(utilisateur.getPrenom().substring(0,1).toUpperCase() + utilisateur.getPrenom().substring(1));
        utilisateur.setRoles(serviceR.findRole("Client").get(0));
        utilisateur.setNumMembre(createNumMembre());
        if (utilisateur.getIdUtilisateurs()!=0) {
            for (UtilisateursAdresses ua : utilisateur.getUtilisateursAdresses()) {
                if (ua.getAdresse().equals(adresses)) {
                    flag = true;
                    UA = ua;
                    break;
                }
            }
        }
        if (!flag){
            UA = serviceUA.createUtilisateursAdresses(utilisateur, adresses);
        }
        if(!verifUtilExist(utilisateur)) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Le client existe déjà tel quel en DB; opération échouée",null));
        }
        else if(utilisateur.getNumMembre().equals("999999999")){
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Le nombre de client maximal a été atteint; opération échouée",null));
        }
        else {
            UA.setActif(true);
            saveUtilisateur();
        }
        init();
        return "/tableUtilisateursCli.xhtml?faces-redirect=true";
    }
  */
    //Méthode qui va créer un nouveau membre en commencant par le nombre 400000000 (déprécié)
    public String createNumMembre()
    {
        if (numMembre.equals("0")){
            numMembre="400000000";
            return numMembre;
        }
        else{
            numMembre=String.valueOf(Integer.parseInt(numMembre)+1);
            if (!numMembre.equals("500000000")){
                return numMembre;
            }
            else {
                return "999999999";
            }
        }
    }


    /*Méthode qui permet de désactiver un utilisateur et de le réactiver en verifiant si son rôle est actif ou pas.
    * Si on desactiver/active un client il nous renverra sur la table des clients sinon il nous renverra sur la table des utilisateurs*/
    // todo : correct the function that activate or deactivate user

    public String activdesactivUtil() {
        if (utilisateur.getActif()) {
            utilisateur.setActif(false);
            saveActif();
        }
        else {
            utilisateur.setActif(true);
            saveActif();
        }

            init();
            return "/tableUtilisateurs.xhtml?faces-redirect=true";

    }


    // Méthode qui permet en fonction de la donnée de l'utilisateur de rechercher un nom parmi les utilisateurs(Client) et nous renvoi sur le formulaire de recherche des utilisateurs(Client)
    //todo : correct the function that search client

    public String searchUtilisateur() {

        SvcUtilisateur service = new SvcUtilisateur();

        if (service.getByName(utilisateur.getNom()).isEmpty()) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("utilRech", new FacesMessage("l'utilisateur n'a pas été trouvé"));
            return null;
        } else {
            searchResults = service.getByName(utilisateur.getNom());
        }

        return "/formSearchUtilisateur?faces-redirect=true";
    }


    //Méthode qui permet de vider les variables et de revenir sur le table des utilisateurs
    public String flushUtil() {
        init();
        if (searchResults != null) {
            searchResults.clear();
        }
        return "/tableUtilisateurs?faces-redirect=true";
    }
    //Méthode qui permet de vider les variables et de revenir sur le table des utilisateurs(Client)
    public String flushUtilCli() {
        init();
        if (searchResults != null) {
            searchResults.clear();
        }
        return "/tableUtilisateursCli?faces-redirect=true";
    }
    //Méthode qui permet de vider les variables et de revenir sur la page de bienvenue
    public String flushBienv()
    {
        init();
        return "/bienvenue?faces-redirect=true";
    }
    /*
     * Méthode qui permet via le service de retourner la liste de tous les utilisateurs actifs
     */
    //todo : correct the function that give the list of active users

    public List<Utilisateur> getReadUtilActiv()
    {
        SvcUtilisateur service = new SvcUtilisateur();
        listUtil = service.findAllUtilisateursActiv();

        service.close();
        return listUtil;
    }


    /*
     * Méthode qui permet via le service de retourner la liste de tous les utilisateurs inactifs
     */
    //todo : correct the function that gives the list of inactive users

    public List<Utilisateur> getReadUtilInactiv()
    {
        SvcUtilisateur service = new SvcUtilisateur();
        listUtil = service.findAllUtilisateursInactiv();

        service.close();
        return listUtil;
    }

    /*
     * Méthode qui permet via le service de retourner la liste de tous les utilisateurs(Client) inactifs
     */
    // todo : correct the function that gives the list of unactive client

    public List<Utilisateur> getReadCliInactiv()
    {
        SvcUtilisateur service = new SvcUtilisateur();
        listCli = service.findAllUtilisateursCliInactiv();

        service.close();
        return listCli;
    }



    /*
     * Méthode qui permet via le service de retourner la liste de tous les utilisateurs(Client) actifs
     */
    //todo : correct the function that give the list of active client

    public List<Utilisateur> getReadCliActiv()
    {
        SvcUtilisateur service = new SvcUtilisateur();
        listCli = service.findAllUtilisateursCliActiv();

        service.close();
        return listCli;
    }



    /*
     * Méthode qui permet via le service de retourner la liste de tous les utilisateurs
     */
    // todo : correct the funtion that give the list of all the users

    public List<Utilisateur> getReadAllUtil()
    {
        SvcUtilisateur service = new SvcUtilisateur();
        listUtil = service.findAllUtilisateursUtil();

        service.close();
        return listUtil;
    }



    /*
     * Méthode qui permet via le service de retourner la liste de tous les utilisateurs(Client)
     */
    //todo : correct the function that give the list of all the clients

    public List<Utilisateur> getReadAllCli()
    {
        SvcUtilisateur service = new SvcUtilisateur();
        listCli = service.findAllUtilisateursCli();

        service.close();
        return listCli;
    }


//-------------------------------Getter & Setter--------------------------------------------
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public List<Utilisateur> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Utilisateur> searchResults) {
        this.searchResults = searchResults;
    }


    public List<Utilisateur> getListUtil() {
        return listUtil;
    }

    public void setListUtil(List<Utilisateur> listUtil) {
        this.listUtil = listUtil;
    }

    public String getNumMembre() {
        return numMembre;
    }

    public void setNumMembre(String numMembre) {
        this.numMembre = numMembre;
    }

    public Adresse getAdresses() {
        return adresses;
    }

    public void setAdresses(Adresse adresses) {
        this.adresses = adresses;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UtilisateurAdresse getUA() {
        return UA;
    }

    public void setUA(UtilisateurAdresse UA) {
        this.UA = UA;
    }

    public List<Utilisateur> getListCli() {
        return listCli;
    }

    public void setListCli(List<Utilisateur> listCli) {
        this.listCli = listCli;
    }

    public String getMdpNouveau() {
        return mdpNouveau;
    }

    public void setMdpNouveau(String mdpNouveau) {
        this.mdpNouveau = mdpNouveau;
    }

    public String getMdpNouveau2() {
        return mdpNouveau2;
    }

    public void setMdpNouveau2(String mdpNouveau2) {
        this.mdpNouveau2 = mdpNouveau2;
    }
}

