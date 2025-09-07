package services;

import entities.Article;
import entities.Magasin;
import entities.Tarif;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcTarif extends Service<Tarif> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcTarif.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcTarif() {
        super();
        log.info("SvcTarif called");
    }

    // Méthode qui permet de sauver un tarif et de le mettre en DB
    @Override
    public Tarif save(Tarif tarif) {
        if (tarif.getId() == null) {
            em.persist(tarif);
        } else if (tarif.getId() == 0) {
            em.persist(tarif);
        } else {
            tarif = em.merge(tarif);
        }

        return tarif;
    }

    public List<Tarif> findOneTarifByDenom(Tarif tar)
    {
        Map<String, Object> param = new HashMap<>();
        param.put("denomination", tar.getDenomination());
        return finder.findByNamedQuery("Tarif.findOneByDenom",param);
    }
    public List<Tarif> findOneTarifByDateDebut(Tarif tar)
    {
        Map<String, Object> param = new HashMap<>();
        param.put("date", tar.getDateDebut());
        return finder.findByNamedQuery("Tarif.findOneByDateDebut",param);
    }

    public List<Tarif> findAllTarifs() {
        return finder.findByNamedQuery("Tarif.findAll", null);
    }

    public List<Tarif> findTarifByMagasin(Date d, Magasin Magasin) {
        Map<String, Object> param = new HashMap<>();
        param.put("date", d);
        param.put("magasin", Magasin);
        return finder.findByNamedQuery("Tarif.findByMagasin", param);
    }
    public List<Tarif> FindTarifByMagasinByArticle(Date d, Magasin Magasin, Article article) {
        Map<String, Object> param = new HashMap<>();
        param.put("date", d);
        param.put("magasin", Magasin);
        return finder.findByNamedQuery("Tarif.findByMagasin", param);
    }

}