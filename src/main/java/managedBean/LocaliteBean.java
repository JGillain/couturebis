package managedBean;

import entities.Localite;
import org.apache.log4j.Logger;
import services.SvcLocalite;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class LocaliteBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private Localite localite;
    private static final Logger log = Logger.getLogger(LocaliteBean.class);

    /*
     * Méthode qui permet via le service de retourner la liste de toutes les localités
     */
    public List<Localite> getReadAll()
    {
        SvcLocalite service = new SvcLocalite();
        List<Localite> listLoca = new ArrayList<Localite>();
        listLoca = service.findAllLocalites();


        return listLoca;
    }


    //-------------------------------Getter & Setter--------------------------------------------

    public Localite getLocalite() {
        return localite;
    }

    public void setLocalite(Localite localite) {
        this.localite = localite;
    }

}
