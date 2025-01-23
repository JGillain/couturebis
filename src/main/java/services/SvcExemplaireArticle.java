package services;


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
        if (EA.getId() == 0) {
            em.persist(EA);
        } else {
            EA = em.merge(EA);
        }

        return EA;
    }
}
