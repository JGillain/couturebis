package managedBean;

import entities.TarifPenalite;
import org.apache.log4j.Logger;
import services.SvcTarifPenalite;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class TarifPenaliteBean implements Serializable {

    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private TarifPenalite tarifsPenalite;
    private static final Logger log = Logger.getLogger(TarifPenaliteBean.class);

    public void save()
    {
        SvcTarifPenalite service = new SvcTarifPenalite();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            service.save(tarifsPenalite);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation a reussie",null));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("Erreur", new FacesMessage("le rollback a pris le relais"));
            }
            service.close();
        }
    }

    /*
     * Méthode qui permet via le service de retourner la liste de toutes les pénalités
     */
    public List<TarifPenalite> getReadAll()
    {
        SvcTarifPenalite service = new SvcTarifPenalite();
        List<TarifPenalite> listTarifsPenalites = service.findAllTarifsPenalites();
        service.close();

        return listTarifsPenalites;
    }


    //-------------------------------Getter & Setter--------------------------------------------

    public TarifPenalite getTarifsPenalite() {
        return tarifsPenalite;
    }

    public void setTarifsPenalite(TarifPenalite tarifsPenalite) {
        this.tarifsPenalite = tarifsPenalite;
    }
}
