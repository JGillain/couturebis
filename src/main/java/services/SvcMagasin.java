package services;

import entities.Localite;
import entities.Magasin;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcMagasin extends Service<Magasin> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcMagasin.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcMagasin() {
        super();
        log.info("SvcMagasin called");
    }

    // Méthode qui permet de sauver un magasin et de le mettre en DB
    @Override
    public Magasin save(Magasin magasin) {
        if (magasin.getId() == null) {
            em.persist(magasin);
        } else if (magasin.getId() == 0) {
            em.persist(magasin);
        } else {
            magasin = em.merge(magasin);
        }

        return magasin;
    }

    //Méthode qui permet via une requete de retourner la liste entière des magasins
    public List<Magasin> findAllMagasin() {
        return finder.findByNamedQuery("Magasin.findAll", null);
    }
}