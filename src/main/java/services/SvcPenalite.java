package services;

import entities.Penalite;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcPenalite extends Service<Penalite> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcPenalite.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcPenalite() {
        super();
        log.info("SvcPenalite called");
    }

    // Méthode qui permet de sauver une penalite et de la mettre en DB
    @Override
    public Penalite save(Penalite penalite) {
        if (penalite.getId() == null) {
            em.persist(penalite);
        } else if (penalite.getId() == 0) {
            em.persist(penalite);
        } else {
            penalite = em.merge(penalite);
        }

        return penalite;
    }

    public Penalite addPena(String d)
    {

        if(findByName(d).size() == 1)
        {
            return findByName(d).get(0);
        }
        else
        {
            Penalite penalite = new Penalite();
            penalite.setDenomination(d);
            save(penalite);
            return penalite;
        }
    }

    //Méthode qui permet via une requete de retourner la liste entière des pénalités
    public List<Penalite> findAllPenalite() {
        return finder.findByNamedQuery("Penalite.findAll", null);
    }

    public List<Penalite> findByName(String denom) {
        Map<String, String> param = new HashMap<>();
        param.put("denomination", denom);

        return finder.findByNamedQuery("Penalite.findByName", param);
    }
}