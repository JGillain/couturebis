package services;

import entities.TarifJour;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcTarifJour extends Service<TarifJour> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcTarifJour() {
        super();
    }

    // Méthode qui permet de sauver un tarifJour et de le mettre en DB
    @Override
    public TarifJour save(TarifJour tarifJour) {
        if (tarifJour.getId() == 0) {
            em.persist(tarifJour);
        } else {
            tarifJour = em.merge(tarifJour);
        }

        return tarifJour;
    }
}