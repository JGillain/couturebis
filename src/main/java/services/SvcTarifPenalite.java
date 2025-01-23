package services;

import entities.TarifPenalite;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcTarifPenalite extends Service<TarifPenalite> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcTarifPenalite() {
        super();
    }

    // Méthode qui permet de sauver un tarifPenalite et de le mettre en DB
    @Override
    public TarifPenalite save(TarifPenalite tarifPenalite) {
        if (tarifPenalite.getId() == 0) {
            em.persist(tarifPenalite);
        } else {
            tarifPenalite = em.merge(tarifPenalite);
        }

        return tarifPenalite;
    }
}