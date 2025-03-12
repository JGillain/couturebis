package services;

import entities.Fabricant;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SvcFabricant extends Service<Fabricant> implements Serializable {

    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcFabricant.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcFabricant() {
        super();
    }

    public List<Fabricant> findAllFabricants() {
        return finder.findByNamedQuery("Fabricant.findAll",null);
    }
    public List<Fabricant> findAllActiveFabricants() {
        return finder.findByNamedQuery("Fabricant.",null);
    }

    public List<Fabricant> findOneFabricant(Fabricant f) {
        Map<String, Object> param = new HashMap<>();
        param.put("nom", f.getNom());
        return finder.findByNamedQuery("Fabricant.findOne",param);
    }

    // Méthode qui permet de sauver un fabricant et de le mettre en DB
    @Override
    public Fabricant save(Fabricant fabricant) {
        log.debug("save Fabricant");
        log.debug(fabricant.getId());
        log.debug(fabricant.getNom());
        if (fabricant.getId() == null) {
            em.persist(fabricant);}
        else if (fabricant.getId() == 0) {
            em.persist(fabricant);
        } else {
            fabricant = em.merge(fabricant);
        }

        return fabricant;
    }
}