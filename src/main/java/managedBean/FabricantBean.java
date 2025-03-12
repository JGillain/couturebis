package managedBean;

import entities.Fabricant;
import org.apache.log4j.Logger;
import services.SvcFabricant;

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
public class FabricantBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private Fabricant fabricant;
    private static final Logger log = Logger.getLogger(FabricantBean.class);

    @PostConstruct
    public void init()
    {
        log.debug("FabricantBean init");
        fabricant = new Fabricant();
    }

    // Méthode qui permet l'appel de save() qui créée une nouvelle adresse et envoi un message si jamais
    // l'adresse se trouve déjà en base de donnée et nous renvoi sur la table des auteurs
    public String newFabricant()
    {
        log.debug("test 1 ");
        log.debug(fabricant.getId());
        log.debug(fabricant.getNom());
        if(verifFabricantExist(fabricant))
        {
            save();
        }
        else{
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"La donnée est déjà existante en DB",null));
            init();
        }
        return "/tableFabricant.xhtml?faces-redirect=true";

    }

    // Méthode qui permet la sauvegarde d'une adresse en base de donnée
    public void save()
    {
        SvcFabricant service = new SvcFabricant();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            log.debug("test 2");
            log.debug(fabricant.getId());
            log.debug(fabricant.getNom());
            service.save(fabricant);
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
    public boolean verifFabricantExist(Fabricant fa)
    {
        SvcFabricant serviceF = new SvcFabricant();
        if(serviceF.findOneFabricant(fa).size() > 0)
        {
            log.debug('1');
            serviceF.close();
            return false;
        }
        else {
            log.debug('2');
            serviceF.close();
            return true;
        }

    }
    /*
     * Méthode qui permet de vider les variables et de revenir sur le table des Adresses .
     * */
    public String flushFab()
    {
        init();
        return "/tableFabricant?faces-redirect=true";
    }


    /*
     * Méthode qui permet via le service de retourner
     * la liste des adresses
     */
    public List<Fabricant> getReadAll()
    {
        SvcFabricant service = new SvcFabricant();
        List<Fabricant> listFa = new ArrayList<Fabricant>();
        listFa= service.findAllFabricants();

        service.close();
        return listFa;
    }
    public List<Fabricant> getReadAllActif()
    {
        SvcFabricant service = new SvcFabricant();
        List<Fabricant> listFa = new ArrayList<Fabricant>();
        listFa= service.findAllFabricants();

        service.close();
        return listFa;
    }


//-------------------------------Getter & Setter--------------------------------------------

    public Fabricant getFabricant() {
        log.debug("getFabricant call");
        return fabricant;
    }

    public void setFabricant(Fabricant fabricant) {
        log.debug("setFabricant call");
        this.fabricant = fabricant;
    }

}
