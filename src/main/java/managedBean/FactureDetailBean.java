package managedBean;

import entities.FactureDetail;
import org.apache.log4j.Logger;
import services.SvcFactureDetail;

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
public class FactureDetailBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private FactureDetail factureDetail;

    private static final Logger log = Logger.getLogger(FactureDetailBean.class);


    // Méthode permettant de mettre en base de données toutes les informations concernant l'entité facture detail
    public void save()
    {
        SvcFactureDetail service = new SvcFactureDetail();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            service.save(factureDetail);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation a reussie",null));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation n'est pas reussie",null));
            }
            service.close();
        }

    }
    // Méthode qui retourne la liste de toutes les facturedetails.
    public List<FactureDetail> getReadAll()
    {
        SvcFactureDetail service = new SvcFactureDetail();
        List<FactureDetail> listFactD = new ArrayList<FactureDetail>();
        listFactD= service.findAllFactureDetail();

        service.close();
        return listFactD;
    }

    //-------------------------------Getter & Setter--------------------------------------------

    public FactureDetail getFactureDetail() {
        return factureDetail;
    }

    public void setFactureDetail(FactureDetail facturesDetail) {
        this.factureDetail = facturesDetail;
    }







}
