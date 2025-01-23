package managedBean;

import entities.Utilisateur;
import entities.UtilisateurRole;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import security.SecurityManager;
import services.SvcRole;
import services.SvcUtilisateur;
import services.SvcUtilisateurRole;

import javax.enterprise.context.SessionScoped;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.PersistenceUnit;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;


@Named
@SessionScoped

public class LoginBean implements Serializable {
    /*Déclaration des variables*/
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(LoginBean.class);
    @PersistenceUnit  (unitName = "couture")
    private String login;
    private String mdp;
    Utilisateur utilisateurAuth = new Utilisateur();

    //---------------------------------------------------------
    /*
     * Méthode qui permet l'authentification de l'utilisateur,
     * on vérifie que l'utilisateur existe dans la base de données, mais également qu'il a la permission de se connecter
     *
     *
     * */
    public void auth()
    {

        log.debug("---------------------------------debut--------------------------");
        FacesMessage m = new FacesMessage("Login ou/et mot de passe incorrect");
        SvcUtilisateur serviceU= new SvcUtilisateur();
        SvcUtilisateurRole serviceURole= new SvcUtilisateurRole();
        SvcRole serviceRole= new SvcRole();
        RoleBean RB = new RoleBean();

        try {
            log.debug(login + " + " + mdp);
            log.debug("1");
            List<Utilisateur> results = serviceU.findByLogin(login);
            log.debug("2");
            if (SecurityManager.processToLogin(login, mdp, false)){
                log.debug("OKAY");
                utilisateurAuth = results.get(0);
                List<UtilisateurRole> role = serviceURole.findByUser(utilisateurAuth);
                if (!role.isEmpty()){
                    boolean flagAdmin = false;
                    boolean flagManager = false;
                    boolean flagUtilisateur = false;
                    boolean flagUtilisateurRole = false;
                    for (UtilisateurRole ur : role){
                        if (ur.getRoleIdRole().getId()==1 && ur.getActif()){
                            flagAdmin = true;
                            log.debug("role admim");
                        }
                        else if (ur.getRoleIdRole().getId()==2 && ur.getActif()){
                            flagManager = true;
                            log.debug("role manager");
                        }
                        else if (ur.getRoleIdRole().getId()==3 && ur.getActif()){
                            flagUtilisateur = true;
                            log.debug("role user");
                        }
                    }
                    if (flagAdmin){
                        SecurityUtils.getSubject().getSession().setAttribute("role", serviceRole.findById(1).get(0).getId());
                        flagUtilisateurRole = true;
                        log.debug("role admim valide");
                    }
                    else if (flagManager){
                        SecurityUtils.getSubject().getSession().setAttribute("role", serviceRole.findById(2).get(0).getId());
                        flagUtilisateurRole = true;
                        log.debug("role manager valide");
                    }
                    else if (flagUtilisateur){
                        SecurityUtils.getSubject().getSession().setAttribute("role", serviceRole.findById(3).get(0).getId());
                        flagUtilisateurRole = true;
                        log.debug("role user valide");
                    }

                    if (flagUtilisateurRole) {
                        log.debug(utilisateurAuth.getId());
                        SecurityUtils.getSubject().getSession().setAttribute("user", utilisateurAuth);
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userAuth", utilisateurAuth);
                        FacesContext.getCurrentInstance().getExternalContext().redirect("bienvenue.xhtml");
                    }
                    else{
                        throw new FacesException("Une erreur est survenue");
                    }
                }
                else{
                    throw new FacesException("Une erreur est survenue");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }

    /*Cette méthode permet la deconnexion de l'utilisateur*/
    public String deconnexion() throws IOException {
        log.debug("test deco " + utilisateurAuth.getNom());
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().clear();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
        utilisateurAuth = new Utilisateur();
        log.debug("test deco2 " + utilisateurAuth.getNom());
        return "login";
    }

    //-------------------------Getter & Setter--------------------------------------------------------------------------------

    public Utilisateur getUtilisateurAuth() {
        return utilisateurAuth;
    }
    public void setUtilisateurAuth(Utilisateur utilisateurAuth) {
        this.utilisateurAuth = utilisateurAuth;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }
}
