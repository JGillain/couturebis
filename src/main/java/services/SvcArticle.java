package services;


import entities.Article;
import entities.Categorie;
import entities.ExemplaireArticle;
import entities.Fabricant;
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
        log.info("SvcArticle called");
    }

    // Méthode qui permet de sauver un article et de le mettre en DB
    @Override
    public Article save(Article article) {
        if (article.getId() == null) {
            em.persist(article);
        } else if (article.getId() == 0) {
            em.persist(article);
        } else {
            article = em.merge(article);
        }

        return article;
    }
    public List<Article> findOneByCodeBarre(String codeBare) {
        Map<String, Object> param = new HashMap<>();
        param.put("CB", codeBare);
        return finder.findByNamedQuery("Article.findOneByCodeBarre", param);
    }

    public List<Article> getbyName(String nom) {
        Map<String, String> param = new HashMap<>();
        param.put("nom", nom);

        return finder.findByNamedQuery("Article.search", param);
    }

    public List<Article> findAllArticles() {
        return finder.findByNamedQuery("Article.findAllTri", null);
    }

    public List<Article> findAllActive() {
        return finder.findByNamedQuery("Article.findActive", null);
    }

    public List<Article> findAllInactive() {
        return finder.findByNamedQuery("Article.findInactive", null);
    }

    public List<Article> findOneByDetails(Article article) {
        int id = (article.getId() != null) ? article.getId() : 0 ;
        Map<String, Object> param = new HashMap<>();
        param.put("nom", article.getNom());
        param.put("fabricant", article.getFabricantIdFabricant());
        param.put("numSerie", article.getNumSerie());
        param.put("id", id);
        return finder.findByNamedQuery("Article.findOneByDetails", param);

    }

    public List<Article> findByFabricant(Fabricant fabricant) {
        Map<String, Object> param = new HashMap<>();
        param.put("fab", fabricant);
        return finder.findByNamedQuery("Article.findByFabricant", param);
    }

    public List<Article> findByCategorie(Categorie categorie) {
        Map<String, Object> param = new HashMap<>();
        param.put("cat", categorie);
        return finder.findByNamedQuery("Article.findByCategorie", param);
    }
}