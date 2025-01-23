package services;

import entities.Penalite;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcPenalite extends Service<Penalite> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcPenalite() {
        super();
    }

    // Méthode qui permet de sauver une penalite et de la mettre en DB
    @Override
    public Penalite save(Penalite penalite) {
        if (penalite.getId() == 0) {
            em.persist(penalite);
        } else {
            penalite = em.merge(penalite);
        }

        return penalite;
    }
}