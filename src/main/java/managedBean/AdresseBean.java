package managedBean;

import entities.Adresse;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import services.SvcAdresse;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class AdresseBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private Adresse adresse;
    private static final Logger log = Logger.getLogger(AdresseBean.class);

    @PostConstruct
    public void init()
    {
        log.info("AdresseBean init");
        adresse = new Adresse();
    }

    // Méthode qui permet l'appel de save() qui créée une nouvelle adresse et envoi un message si jamais
    // l'adresse se trouve déjà en base de donnée et nous renvoi sur la table des adresses
    public String newAdress()
    {
        log.debug("test 1 ");
        log.debug(adresse.getId());
        log.debug(adresse.getRue());
        log.debug(adresse.getNumero());
        log.debug(adresse.getLocaliteIdLocalite().getCp());
        if(verifAdresseExist(adresse))
        {
            save();
        }
        else{
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"La donnée est déjà existante en DB",null));
            init();
        }
        return "/tableAdresses.xhtml?faces-redirect=true";

    }

    // Méthode qui permet la sauvegarde d'une adresse en base de donnée
    public void save()
    {
        SvcAdresse service = new SvcAdresse();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            log.debug("test 2");
            log.debug(adresse.getId());
            log.debug(adresse.getRue());
            log.debug(adresse.getNumero());
            log.debug(adresse.getLocaliteIdLocalite().getCp());
            service.save(adresse);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation a reussie",null));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"l'operation n'a pas reussie",null));
            }
            else
            {
                init();
            }
            service.close();
        }
    }

    // Méthode qui vérifie qu'une adresse déjà ou pas dans la base de donnée
    public boolean verifAdresseExist(Adresse ad)
    {
        SvcAdresse serviceA = new SvcAdresse();
        if(serviceA.findOneAdresse(ad).size() > 0)
        {
            log.debug('1');
            serviceA.close();
            return false;
        }
        else {
            log.debug('2');
            serviceA.close();
            return true;
        }

    }
    /*
     * Méthode qui permet de vider les variables et de revenir sur la table des Adresses .
     * */
    public String flushAdd()
    {
        init();
        return "/tableAdresses?faces-redirect=true";
    }


    /*
     * Méthode qui permet via le service de retourner
     * la liste des adresses
     */
    public List<Adresse> getReadAll()
    {
        SvcAdresse service = new SvcAdresse();
        List<Adresse> listAd = new ArrayList<Adresse>();
        listAd= service.findAllAdresse();

        service.close();
        return listAd;
    }


//-------------------------------Getter & Setter--------------------------------------------

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

}
