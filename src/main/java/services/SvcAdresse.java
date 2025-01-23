package services;

import entities.Adresse;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SvcAdresse extends Service<Adresse> implements Serializable {

    // Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcAdresse.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcAdresse()
    {
        super();
    }

    //Méthode qui permet via une requete de retourner la liste entière des adresses
    public List<Adresse> findAllAdresse()
    {
        return finder.findByNamedQuery("Adresses.findAll",null);
    }

    //Méthode qui permet via une requete de retourner une liste avec une adresse
    public List<Adresse> findOneAdresse(Adresse ad)
    {
        Map<String, Object> param = new HashMap<>();
        param.put("boite", ad.getBoite());
        param.put("localite", ad.getLocaliteIdLocalite());
        param.put("numero", ad.getNumero());
        param.put("rue", ad.getRue());
        return finder.findByNamedQuery("Adresses.findOne",param);
    }

    // Méthode qui permet de sauver une adresse et de la mettre en DB
    @Override
    public Adresse save(Adresse adresse) {
        if (adresse.getId() == null) {
            em.persist(adresse);
        } else if (adresse.getId()==0) {
            em.persist(adresse);
        } else {
            adresse = em.merge(adresse);
        }

        return adresse;
    }


}