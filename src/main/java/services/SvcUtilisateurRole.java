package services;

import entities.*;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcUtilisateurRole extends Service<UtilisateurRole> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcUtilisateurRole() {
        super();
    }

    // Méthode qui permet de sauver un utilisateurRole et de le mettre en DB
    @Override
    public UtilisateurRole save(UtilisateurRole utilisateurRole) {
        if (utilisateurRole.getId() == 0) {
            em.persist(utilisateurRole);
        } else {
            utilisateurRole = em.merge(utilisateurRole);
        }

        return utilisateurRole;
    }

    public List<UtilisateurRole> findByUser(Utilisateur utilisateur) {
        Map<String, Integer> param = new HashMap<>();
        param.put("utilisateur", utilisateur.getId());

        return finder.findByNamedQuery("UtilisateurRole.findUserRoleByUser", param);
    }

    public UtilisateurRole createUtilisateurRole(Utilisateur u, Role r)
    {
        UtilisateurRole ur = new UtilisateurRole();
        ur.setRoleIdRole(r);
        ur.setUtilisateurIdUtilisateur(u);

        return ur;
    }
}