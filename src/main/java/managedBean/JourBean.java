package managedBean;

import entities.Jour;
import org.apache.log4j.Logger;
import services.SvcJours;

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
public class JourBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private Jour jour;
    private static final Logger log = Logger.getLogger(JourBean.class);

    @PostConstruct
    public void  init()
    {
        jour=new Jour();
    }

    // Méthode qui permet la sauvegarde du jour dans la base de donnée.
    public void save()
    {
        SvcJours service = new SvcJours();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            service.save(jour);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"l'operation a reussie",null));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Erreur fatale",null));
            }

            init();
            service.close();
        }

    }
    /*
     * Méthode qui permet via le service de retourner la liste de tous les jours
     */
    public List<Jour> getReadAll()
    {
        SvcJours service = new SvcJours();
        List<Jour> listJours = new ArrayList<Jour>();
        listJours= service.findAllJours();

        service.close();
        return listJours;
    }


    //-------------------------------Getter & Setter--------------------------------------------

    public Jour getJour() {
        return jour;
    }

    public void setJour(Jour jour) {
        this.jour = jour;
    }


}
