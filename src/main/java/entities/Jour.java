package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "jour")
@NamedQueries
        ({
                @NamedQuery(name = "Jours.findAll", query = "SELECT j FROM Jour j"),
                @NamedQuery(name = "Jours.findByNbrJ", query = "SELECT j FROM Jour j WHERE j.nbrJour<=:nbrJour ORDER BY j.nbrJour DESC"),
                @NamedQuery(name = "Jours.findByNbrJExact", query = "SELECT j FROM Jour j WHERE j.nbrJour=:nbrJour"),
        })
public class Jour implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdJour", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "NbrJour", nullable = false)
    private Integer nbrJour;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNbrJour() {
        return nbrJour;
    }

    public void setNbrJour(Integer nbrJour) {
        this.nbrJour = nbrJour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Jour jour = (Jour) o;
        return Objects.equals(id, jour.id) && Objects.equals(nbrJour, jour.nbrJour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nbrJour);
    }
}