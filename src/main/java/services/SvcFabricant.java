package services;


import entities.Fabricant;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcFabricant extends Service<Fabricant> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcFabricant() {
        super();
    }

    // Méthode qui permet de sauver un fabricant et de le mettre en DB
    @Override
    public Fabricant save(Fabricant fabricant) {
        if (fabricant.getId() == 0) {
            em.persist(fabricant);
        } else {
            fabricant = em.merge(fabricant);
        }

        return fabricant;
    }
}