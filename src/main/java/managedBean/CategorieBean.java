package managedBean;

import entities.Categorie;
import org.apache.log4j.Logger;
import services.SvcCategorie;

import javax.annotation.PostConstruct;
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
public class CategorieBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private Categorie categorie;
    private static final Logger log = Logger.getLogger(CategorieBean.class);

    @PostConstruct
    public void init()
    {
        log.debug("CategorieBean init");
        categorie = new Categorie();
    }

    // Méthode qui permet l'appel de save() qui créée une nouvelle categorie et envoi un message si jamais
    // la categorie se trouve déjà en base de donnée et nous renvoi sur la table des categories
    public String newCategorie()
    {
        log.debug("test 1 ");
        log.debug(categorie.getId());
        log.debug(categorie.getNom());
        if(verifCategorieExist(categorie))
        {
            save();
        }
        else{
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"La donnée est déjà existante en DB",null));
            init();
        }
        return "/tableCategorie.xhtml?faces-redirect=true";

    }

    // Méthode qui permet la sauvegarde d'une categorie en base de donnée
    public void save()
    {
        SvcCategorie service = new SvcCategorie();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            log.debug("test 2");
            log.debug(categorie.getId());
            log.debug(categorie.getNom());
            service.save(categorie);
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

    // Méthode qui vérifie qu'une categorie déjà ou pas dans la base de donnée
    public boolean verifCategorieExist(Categorie Ca)
    {
        SvcCategorie serviceC = new SvcCategorie();
        if(serviceC.findOneCategorie(Ca).size() > 0)
        {
            log.debug('1');
            serviceC.close();
            return false;
        }
        else {
            log.debug('2');
            serviceC.close();
            return true;
        }

    }
    /*
     * Méthode qui permet de vider les variables et de revenir sur la table des categorie.
     * */
    public String flushCat()
    {
        init();
        return "/tableCategorie.xhtml?faces-redirect=true";
    }


    /*
     * Méthode qui permet via le service de retourner
     * la liste des categorie
     */
    public List<Categorie> getReadAll()
    {
        SvcCategorie service = new SvcCategorie();
        List<Categorie> listCa = new ArrayList<Categorie>();
        listCa = service.findAllCategorie();

        service.close();
        return listCa;
    }



//-------------------------------Getter & Setter--------------------------------------------

    public Categorie getCategorie() {
        log.debug("getFabricant call");
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        log.debug("setFabricant call");
        this.categorie = categorie;
    }

}
