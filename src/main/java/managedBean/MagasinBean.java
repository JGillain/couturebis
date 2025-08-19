package managedBean;

import entities.Magasin;
import services.SvcMagasin;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class MagasinBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Integer ID_MAGASIN = 1;
    private Magasin magasin;

    @PostConstruct
    public void init() {
        SvcMagasin service = new SvcMagasin();
        try {
            magasin = service.getById(ID_MAGASIN);

            if (magasin == null) {
                throw new IllegalStateException("le magasin n'a pas été trouvé");
            }
        }finally {
            service.close();
        }
    }

    public Magasin getMagasin() {
        return magasin;
    }

    public void setMagasin(Magasin magasin) {
        this.magasin = magasin;
    }

}
