package services;

import entities.Role;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcRole extends Service<Role> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcRole.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcRole() {
        super();
        log.info("SvcRole called");
    }

    // Méthode qui permet de sauver un role et de le mettre en DB
    @Override
    public Role save(Role role) {
        if (role.getId() == null) {
            em.persist(role);
        } else if (role.getId() == 0) {
            em.persist(role);
        } else {
            role = em.merge(role);
        }

        return role;
    }
    public List<Role> findById(int id) {
        Map<String, Integer> param = new HashMap<>();
        param.put("id", id);
        return finder.findByNamedQuery("Role.findRoleById", param);
    }
    public List<Role> findByNom(String nom) {
        Map<String, String> param = new HashMap<>();
        param.put("nom", nom);
        return finder.findByNamedQuery("Role.findRoleByNom", param);
    }

    public List<Role> findAllRoleUtil() {
        return finder.findByNamedQuery("Role.findAllUtil", null);
    }
}