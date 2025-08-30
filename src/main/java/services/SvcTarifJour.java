package services;

import entities.*;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcTarifJour extends Service<TarifJour> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcTarifJour.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcTarifJour() {
        super();
        log.info("SvcTarifJour called");
    }

    // Méthode qui permet de sauver un tarifJour et de le mettre en DB
    @Override
    public TarifJour save(TarifJour tarifJour) {
        if (tarifJour.getId() == null) {
            em.persist(tarifJour);
        } else if (tarifJour.getId() == 0) {
            em.persist(tarifJour);
        } else {
            tarifJour = em.merge(tarifJour);
        }

        return tarifJour;
    }

    public List<TarifJour> findByJourByArticle(Tarif t, Jour j, ExemplaireArticle ea) {
        Map<String, Object> param = new HashMap<>();
        Date date = new Date();
        param.put("dateDebut", date);
        param.put("jour", j.getNbrJour());
        param.put("tarif", t);
        param.put("article", ea.getArticleIdArticle());
        param.put("dateFin", date);
        return finder.findByNamedQuery("TarifJour.findByJourByArticle", param);
    }

    public TarifJour createTarifJour(Tarif t, Jour j, Double p, Date db, Date df, Article a)
    {
        TarifJour tj = new TarifJour();
        tj.setJourIdJour(j);
        tj.setTarifIdTarif(t);
        tj.setPrix(p);
        tj.setDateDebut(db);
        tj.setDateFin(df);
        tj.setArticleIdArticle(a);
        return tj;
    }


    public List<TarifJour> findAllTarifJour() {
        return finder.findByNamedQuery("TarifJour.findAll", null);
    }

    public List<TarifJour> findAllForArticleOnDate(Tarif t, Article article,int maxJ, java.sql.Date today) {
        Map<String, Object> param = new HashMap<>();
        param.put("article", article);
        param.put("date", today);
        param.put("tarif", t);
        param.put("jour", maxJ);

        return finder.findByNamedQuery("TarifJour.findAllForArticleOnDate", param);
    }
}