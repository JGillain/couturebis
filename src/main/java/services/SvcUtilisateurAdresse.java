package services;

import entities.UtilisateurAdresse;
import entities.Adresse;
import entities.Utilisateur;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcUtilisateurAdresse extends Service<UtilisateurAdresse> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcUtilisateurAdresse() {
        super();
    }

    // Méthode qui permet de sauver un utilisateurAdresse et de le mettre en DB
    @Override
    public UtilisateurAdresse save(UtilisateurAdresse utilisateurAdresse) {
        if (utilisateurAdresse.getId() == 0) {
            em.persist(utilisateurAdresse);
        } else {
            utilisateurAdresse = em.merge(utilisateurAdresse);
        }
        return utilisateurAdresse;
    }

    public UtilisateurAdresse createUtilisateurAdresse(Utilisateur u, Adresse a)
        {
            UtilisateurAdresse ua = new UtilisateurAdresse();
            ua.setAdresseIdAdresse(a);
            ua.setUtilisateurIdUtilisateur(u);

            return ua;
        }
}