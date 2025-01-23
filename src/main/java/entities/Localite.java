package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "localite")
@NamedQueries
        ({
                @NamedQuery(name= "Localite.findAll", query="SELECT l FROM Localite l"),
                @NamedQuery(name= "Localite.findAllTry", query="SELECT l FROM Localite l ORDER BY l.ville ASC"),
        })

public class Localite implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdLocalite", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "CP", nullable = false)
    private Integer cp;

    @Size(max = 255)
    @NotNull
    @Column(name = "Ville", nullable = false)
    private String ville;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "PaysIdPays", nullable = false)
    private Pays paysIdPays;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCp() {
        return cp;
    }

    public void setCp(Integer cp) {
        this.cp = cp;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Pays getPaysIdPays() {
        return paysIdPays;
    }

    public void setPaysIdPays(Pays paysIdPays) {
        this.paysIdPays = paysIdPays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Localite localite = (Localite) o;
        return Objects.equals(id, localite.id) && Objects.equals(cp, localite.cp) && Objects.equals(ville, localite.ville) && Objects.equals(paysIdPays, localite.paysIdPays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cp, ville, paysIdPays);
    }
}