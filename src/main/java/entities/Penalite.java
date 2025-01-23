package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "penalite")
public class Penalite implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdPenalites", nullable = false)
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
        Penalite penalite = (Penalite) o;
        return Objects.equals(id, penalite.id) && Objects.equals(denomination, penalite.denomination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, denomination);
    }
}