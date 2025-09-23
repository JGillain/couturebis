package services;


import entities.Article;
import entities.ExemplaireArticle;
import entities.Magasin;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcExemplaireArticle extends Service<ExemplaireArticle> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcExemplaireArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcExemplaireArticle() {
        super();
        log.info("SvcExemplaireArticle called");
    }

    // Méthode qui permet de sauver un exemplaire d'article et de le mettre en DB
    @Override
    public ExemplaireArticle save(ExemplaireArticle EA) {
        if (EA.getId() == null) {
            em.persist(EA);
        } else if (EA.getId() == 0) {
            em.persist(EA);
        } else {
            EA = em.merge(EA);
        }

        return EA;
    }
    public void loueExemplaire(ExemplaireArticle exemplaireArticle){
        log.debug("Je loue l'exemplaire article :" + exemplaireArticle.getLoue());
        exemplaireArticle.setLoue(true);
    }

    public List<ExemplaireArticle> findAllExArticles() {
        return finder.findByNamedQuery("ExArticle.findAllTri", null);
    }

    public List<ExemplaireArticle> findAllActive() {
        return finder.findByNamedQuery("ExArticle.findActive", null);
    }

    public List<ExemplaireArticle> findAllInactive() {
        return finder.findByNamedQuery("ExArticle.findInactive", null);
    }

    public int countAvailableExArticlesRent(Article article) {
        Map<String, Object> param = new HashMap<>();
        param.put("article", article);
        return finder.findByNamedQuery("ExArticle.AvailableExArticlesRent", param).size();
    }

    public int countAvailableExArticlesRentNotReserved(Article article, Magasin  magasin) {
        Map<String, Object> param = new HashMap<>();
        param.put("article", article);
        return finder.findByNamedQuery("ExArticle.AvailableExArticlesRentNotReserved", param).size();
    }
    public int countAvailableExArticlesRentReserved(Article article) {
        Map<String, Object> param = new HashMap<>();
        param.put("article", article);
        return finder.findByNamedQuery("ExArticle.AvailableExArticlesRentReserved", param).size();
    }

    public int countAvailableExArticlesSales(Article article) {
        Map<String, Object> param = new HashMap<>();
        param.put("article", article);
        return finder.findByNamedQuery("ExArticle.AvailableExArticlesSales", param).size();
    }
    public List<ExemplaireArticle> findAvailableExArticlesSales(Article article) {
        Map<String, Object> param = new HashMap<>();
        param.put("article", article);
        return finder.findByNamedQuery("ExArticle.AvailableExArticlesSales", param);
    }

    public List<ExemplaireArticle> findByArticle(Article article) {
        Map<String, Object> param = new HashMap<>();
        param.put("article", article);
        return finder.findByNamedQuery("ExArticle.findByArticle", param);
    }

    public List<ExemplaireArticle> findOneByCodeBarre(String codeBare) {
        Map<String, Object> param = new HashMap<>();
        param.put("CB", codeBare);
        return finder.findByNamedQuery("ExArticle.findOneByCodeBarre", param);
    }

    public List<ExemplaireArticle> search (String nom) {
        Map<String, Object> param = new HashMap<>();
        param.put("nom", nom);
        return finder.findByNamedQuery("ExArticle.findByArticle", param);
    }

}
