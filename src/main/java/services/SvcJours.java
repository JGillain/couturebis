package services;

import entities.Jour;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcJours extends Service<Jour> implements Serializable {
    private static final Logger log = Logger.getLogger(SvcJours.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcJours() {
        super();
    }

    // Méthode qui permet de sauver un jour et de le mettre en DB
    @Override
    public Jour save(Jour jours) {
        if (jours.getId() == 0) {
            em.persist(jours);
        } else {
            jours = em.merge(jours);
        }

        return jours;
    }

    public Jour addJours(int j) {
        if (findByNbrJExact(j).size() != 0) {
            return findByNbrJExact(j).get(0);
        } else {
            Jour jours = new Jour();
            jours.setNbrJour(j);
            save(jours);
            return jours;
        }
    }

    public List<Jour> findByNbrJExact(int nbrJ) {
        Map<String, Integer> param = new HashMap<>();
        param.put("nbrJour", nbrJ);
        return finder.findByNamedQuery("Jours.findByNbrJExact", param);
    }
    public List<Jour> findByNbrJ(int nbrJ) {
        Map<String, Integer> param = new HashMap<>();
        param.put("nbrJour", nbrJ);
        return finder.findByNamedQuery("Jours.findByNbrJ", param);
    }


    public List<Jour> findAllJours() {
        return finder.findByNamedQuery("Jours.findAll", null);
    }


}
