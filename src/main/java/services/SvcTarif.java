package services;

import entities.Role;
import entities.Tarif;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcTarif extends Service<Tarif> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcTarif() {
        super();
    }

    // Méthode qui permet de sauver un tarif et de le mettre en DB
    @Override
    public Tarif save(Tarif tarif) {
        if (tarif.getId() == 0) {
            em.persist(tarif);
        } else {
            tarif = em.merge(tarif);
        }

        return tarif;
    }
}