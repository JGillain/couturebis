package services;

import entities.Utilisateur;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcUtilisateur extends Service<Utilisateur> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcUtilisateur() {
        super();
    }

    // Méthode qui permet de sauver un utilisateur et de le mettre en DB
    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        if (utilisateur.getId() == 0) {
            em.persist(utilisateur);
        } else {
            utilisateur = em.merge(utilisateur);
        }

        return utilisateur;
    }
    //Méthode qui permet via une requete de retourner une liste avec un utilisateur
    public List<Utilisateur> findOneUtilisateur(Utilisateur util)
    {
        Map<String, Object> param = new HashMap<>();
        param.put("nom", util.getNom());
        param.put("prenom", util.getPrenom());
        param.put("sexe", util.getSexe());
        param.put("courriel", util.getCourriel());
        return finder.findByNamedQuery("Utilisateurs.findOne",param);
    }

    //Méthode qui permet via une requete de retourner la liste entière des utilisateurs
    public List<Utilisateur> findAllUtilisateursUtil() {
        return finder.findByNamedQuery("Utilisateur.findAllUtil", null);
    }
    //Méthode qui permet via une requete de retourner la liste entière des utilisateurs (client)
    public List<Utilisateur> findAllUtilisateursCli() {
        return finder.findByNamedQuery("Utilisateur.findAllCli", null);
    }

    //Méthode qui permet via une requete de retourner la liste entière des utiisateurs actifs
    public List<Utilisateur> findAllUtilisateursActiv() {
        return finder.findByNamedQuery("Utilisateur.findActiv", null);
    }
    //Méthode qui permet via une requete de retourner la liste entière des utilisateurs inactifs
    public List<Utilisateur> findAllUtilisateursInactiv() {
        return finder.findByNamedQuery("Utilisateur.findInactiv", null);
    }
    //Méthode qui permet via une requete de retourner la liste entière des utilisateurs (client) actifs
    public List<Utilisateur> findAllUtilisateursCliActiv() {
        return finder.findByNamedQuery("Utilisateur.findCliActiv", null);
    }
    //Méthode qui permet via une requete de retourner la liste entière des utilisateurs (client) inactif
    public List<Utilisateur> findAllUtilisateursCliInactiv() {
        return finder.findByNamedQuery("Utilisateur.findCliInactiv", null);
    }

    //Méthode qui permet via une requete de retourner une avec le dernier numero d'utilisateur (client)
    public List<Utilisateur> findlastMembre()
    {
        return finder.findByNamedQuery("Utilisateur.findLastMembre",null);
    }



    public List<Utilisateur> getByName(String nom) {
        Map<String, String> param = new HashMap<>();
        param.put("nom", nom);

        return finder.findByNamedQuery("Utilisateur.searchName", param);
    }

    public List<Utilisateur> getByNumMembre(String numMembre) {
        Map<String, String> param = new HashMap<>();
        param.put("numMembre", numMembre);

        return finder.findByNamedQuery("Utilisateur.searchMembre", param);
    }
    public List<Utilisateur> findByLogin(String login) {
        Map<String, String> param = new HashMap<>();
        param.put("login", login);

        return finder.findByNamedQuery("Utilisateur.findByLogin", param);
    }

    public List<Utilisateur> reinitialisation(String login, String courriel) {
        Map<String, String> param = new HashMap<>();
        param.put("login", login);
        param.put("courriel", courriel);

        return finder.findByNamedQuery("Utilisateur.findByLoginMail", param);
    }

}