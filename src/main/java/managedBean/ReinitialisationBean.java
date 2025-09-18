package managedBean;

import entities.Utilisateur;
import org.apache.log4j.Logger;
import security.SecurityManager;
import services.SvcUtilisateur;
import tools.MailUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

@Named
@SessionScoped

public class ReinitialisationBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ReinitialisationBean.class);
    private String login;
    private String courriel;

    public String reinitialisation()
    {
        SvcUtilisateur serviceU = new SvcUtilisateur();
        List<Utilisateur> results = serviceU.reinitialisation(login,courriel);
        EntityTransaction transaction = serviceU.getTransaction();

        try
        {
            if (results.isEmpty())
            {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "L'operation a reussie", null));
                return "/envoiInfo?faces-redirect=true";
            }
            else
            {
                Utilisateur utilisateur = results.get(0);
                String mdp = randomMdp();
                transaction.begin();
                utilisateur.setMdp(SecurityManager.encryptPassword(mdp));
                serviceU.save(utilisateur);
                transaction.commit();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                MailUtils.sendText(utilisateur.getCourriel(),"Réinitialisation du mot de passe","Vous avez demandé une réinitialisation du mot de passe, désormais votre nouveau mot de passe sera : " + mdp);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "L'operation a reussie", null));
            }
        }
        finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "L'operation n'a pas réussie", null));
            }
            serviceU.close();
        }

        return "/envoiInfo?faces-redirect=true";
    }

    /**
     *
     * candidateChars
     *            the candidate chars
     * length
     *            the number of random chars to be generated
     *
     *
     */
    public String randomMdp()
    {
        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        int length = 10;
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars
                    .length())));
        }

        return sb.toString();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getCourriel() {
        return courriel;
    }

    public void setCourriel(String courriel) {
        this.courriel = courriel;
    }

}

