package services;

import entities.Pays;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcPays extends Service<Pays> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcPays() {
        super();
    }

    // Méthode qui permet de sauver un Pays et de le mettre en DB
    @Override
    public Pays save(Pays pays) {
        if (pays.getId() == 0) {
            em.persist(pays);
        } else {
            pays = em.merge(pays);
        }

        return pays;
    }
}