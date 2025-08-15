package managedBean;

import entities.TarifJour;
import org.apache.log4j.Logger;
import services.SvcTarifJour;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.List;


@Named
@SessionScoped
public class TarifJourBean implements Serializable {

    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private TarifJour tarifJour;


    private static final Logger log = Logger.getLogger(TarifJourBean.class);

    public void save()
    {
        SvcTarifJour service = new SvcTarifJour();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            service.save(tarifJour);
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

    /*
     * Méthode qui permet via le service de retourner la liste de toutes les tarifs journalier
     */

    public List<TarifJour> getReadAll()
    {
        SvcTarifJour service = new SvcTarifJour();
        List<TarifJour> listTarifsJours = service.findAllTarifJour();

        service.close();
        return listTarifsJours;
    }


    //-------------------------------Getter & Setter--------------------------------------------


    public TarifJour getTarifJour() {
        return tarifJour;
    }

    public void setTarifJour(TarifJour tarifJour) {
        this.tarifJour = tarifJour;
    }
}
