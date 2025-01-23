package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pays")
public class Pays implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdPays", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 150)
    @NotNull
    @Column(name = "Nom", nullable = false, length = 150)
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
        Pays pays = (Pays) o;
        return Objects.equals(id, pays.id) && Objects.equals(nom, pays.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }
}