package services;

import entities.Article;
import entities.Penalite;
import entities.Tarif;
import entities.TarifPenalite;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcTarifPenalite extends Service<TarifPenalite> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcTarifPenalite.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcTarifPenalite() {
        super();
        log.info("SvcTarifPenalite called");
    }

    // Méthode qui permet de sauver un tarifPenalite et de le mettre en DB
    @Override
    public TarifPenalite save(TarifPenalite tarifPenalite) {
        if (tarifPenalite.getId() == null) {
            em.persist(tarifPenalite);
        }else if (tarifPenalite.getId() == 0) {
            em.persist(tarifPenalite);
        } else {
            tarifPenalite = em.merge(tarifPenalite);
        }

        return tarifPenalite;
    }

    public List<TarifPenalite> findAllTarifsPenalites() {
        return finder.findByNamedQuery("TarifPenalite.findAll", null);
    }
    public List<TarifPenalite> findByPena(Tarif t, Penalite p, Date d) {
        Map<String, Object> param = new HashMap<>();
        param.put("dateDebut", d);
        param.put("penalite", p);
        param.put("tarif", t);
        param.put("dateFin", d);
        return finder.findByNamedQuery("TarifPenalite.findByPenalite", param);
    }



    public TarifPenalite createTarifPenalite(Tarif t, Penalite pe, Double pr, Date db, Date df, Article a)
    {
        TarifPenalite tp = new TarifPenalite();
        tp.setPenaliteIdPenalite(pe);
        tp.setTarifIdTarif(t);
        tp.setPrix(pr);
        tp.setDateDebut(db);
        tp.setDateFin(df);
        tp.setArticleIdArticle(a);
        return tp;
    }
}