package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "fabricant")
public class Fabricant implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdFabricant", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "Nom", nullable = false)
    private String nom;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fabricant fabricant = (Fabricant) o;
        return Objects.equals(id, fabricant.id) && Objects.equals(nom, fabricant.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }
}