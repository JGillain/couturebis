package services;


import entities.Article;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcArticle extends Service<Article> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcArticle() {
        super();
    }

    // Méthode qui permet de sauver un article et de le mettre en DB
    @Override
    public Article save(Article article) {
        if (article.getId() == 0) {
            em.persist(article);
        } else {
            article = em.merge(article);
        }

        return article;
    }
}