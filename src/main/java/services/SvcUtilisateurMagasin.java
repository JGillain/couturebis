package services;

import entities.UtilisateurMagasin;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcUtilisateurMagasin extends Service<UtilisateurMagasin> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcUtilisateurMagasin() {
        super();
    }

    // Méthode qui permet de sauver un utilisateurMagasin et de le mettre en DB
    @Override
    public UtilisateurMagasin save(UtilisateurMagasin utilisateurMagasin) {
        if (utilisateurMagasin.getId() == 0) {
            em.persist(utilisateurMagasin);
        } else {
            utilisateurMagasin = em.merge(utilisateurMagasin);
        }

        return utilisateurMagasin;
    }
}