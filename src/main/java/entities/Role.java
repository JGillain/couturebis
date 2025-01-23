package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "role")
@NamedQuery(name = "Roles.findRoleById", query="SELECT r FROM Role r WHERE r.id=:id")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdRole", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 150)
    @NotNull
    @Column(name = "Denomination", nullable = false, length = 150)
    private String denomination;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && Objects.equals(denomination, role.denomination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, denomination);
    }
}