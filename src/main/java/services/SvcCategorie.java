package services;

import entities.Categorie;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SvcCategorie extends Service<Categorie> implements Serializable {

    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcCategorie.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcCategorie() {
        super();
        log.info("SvcCategorie called");
    }

    // Méthode qui permet de sauver une categorie et de le mettre en DB
    @Override
    public Categorie save(Categorie categorie) {
        if (categorie.getId() == null) {
            em.persist(categorie);}
        else if (categorie.getId() == 0) {
            em.persist(categorie);
        } else {
            categorie = em.merge(categorie);
        }

        return categorie;
    }

    public List<Categorie> findAllCategorie() {
        return finder.findByNamedQuery("Categorie.findAll",null);
    }

    public List<Categorie> findOneCategorie(Categorie c) {
        Map<String, Object> param = new HashMap<>();
        param.put("nom", c.getNom());
        return finder.findByNamedQuery("Categorie.findOne",param);
    }

}