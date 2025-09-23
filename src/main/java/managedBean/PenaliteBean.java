package managedBean;

import entities.Penalite;
import org.apache.log4j.Logger;
import services.SvcPenalite;

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
public class PenaliteBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private Penalite penalite;
    private static final Logger log = Logger.getLogger(PenaliteBean.class);

    @PostConstruct
    public void init() {
        penalite = new Penalite();
    }

    /*
     * Méthode qui permet de sauvegarder une entité pénalité
     */
    public void save()
    {
        SvcPenalite service = new SvcPenalite();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            service.save(penalite);
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

            init();
            service.close();
        }
    }
    /*
     * Méthode qui permet via le service de retourner la liste de toutes les pénalités
     */
    public List<Penalite> getReadAll()
    {
        SvcPenalite service = new SvcPenalite();
        List<Penalite> listPenalites = new ArrayList<Penalite>();
        listPenalites = service.findAllPenalite();


        return listPenalites;
    }

    //-------------------------------Getter & Setter--------------------------------------------

    public Penalite getPenalite() {
        return penalite;
    }

    public void setPenalite(Penalite penalite) {
        this.penalite = penalite;
    }
}