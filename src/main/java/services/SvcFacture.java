package services;


import entities.Facture;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcFacture extends Service<Facture> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcFacture() {
        super();
    }

    // Méthode qui permet de sauver une facture et de la mettre en DB
    @Override
    public Facture save(Facture facture) {
        if (facture.getId() == 0) {
            em.persist(facture);
        } else {
            facture = em.merge(facture);
        }

        return facture;
    }
}