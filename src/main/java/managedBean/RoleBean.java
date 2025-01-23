package managedBean;

import entities.Role;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import java.io.Serializable;

@Named
@SessionScoped
public class RoleBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private Role role;
    private static final Logger log = Logger.getLogger(RoleBean.class);

    @PostConstruct
    public void init()
    {
        role = new Role();
    }




// todo : verifier les appels et les retirer, fonction depreciee

    public boolean checkPermission(String permission)
    {


        return true;

    }







    //-------------------------------Getter & Setter--------------------------------------------

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
