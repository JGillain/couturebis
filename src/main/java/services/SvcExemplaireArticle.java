package services;


import entities.Article;
import entities.ExemplaireArticle;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcExemplaireArticle extends Service<ExemplaireArticle> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcExemplaireArticle() {
        super();
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
    public int countAvailableExArticlesSales(Article article) {
        Map<String, Object> param = new HashMap<>();
        param.put("article", article);
        return finder.findByNamedQuery("ExArticle.AvailableExArticlesSales", param).size();
    }
}
