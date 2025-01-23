package services;

import entities.Magasin;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcMagasin extends Service<Magasin> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcMagasin() {
        super();
    }

    // Méthode qui permet de sauver un magasin et de le mettre en DB
    @Override
    public Magasin save(Magasin magasin) {
        if (magasin.getId() == 0) {
            em.persist(magasin);
        } else {
            magasin = em.merge(magasin);
        }

        return magasin;
    }
}