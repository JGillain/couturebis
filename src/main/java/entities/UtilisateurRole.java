package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "utilisateur_role")
@NamedQueries({
        @NamedQuery(name = "UtilisateurRole.findUserRoleByUser", query = "SELECT ur FROM UtilisateurRole ur where ur.utilisateurIdUtilisateur.id=:utilisateur"),
})
public class UtilisateurRole implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdUtilisateurRole", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "UtilisateurIdUtilisateur", nullable = false)
    private Utilisateur utilisateurIdUtilisateur;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "RoleIdRole", nullable = false)
    private Role roleIdRole;

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Utilisateur getUtilisateurIdUtilisateur() {
        return utilisateurIdUtilisateur;
    }

    public void setUtilisateurIdUtilisateur(Utilisateur utilisateurIdUtilisateur) {
        this.utilisateurIdUtilisateur = utilisateurIdUtilisateur;
    }

    public Role getRoleIdRole() {
        return roleIdRole;
    }

    public void setRoleIdRole(Role roleIdRole) {
        this.roleIdRole = roleIdRole;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtilisateurRole that = (UtilisateurRole) o;
        return Objects.equals(id, that.id) && Objects.equals(utilisateurIdUtilisateur, that.utilisateurIdUtilisateur) && Objects.equals(roleIdRole, that.roleIdRole) && Objects.equals(actif, that.actif);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, utilisateurIdUtilisateur, roleIdRole, actif);
    }
}