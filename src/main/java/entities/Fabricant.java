package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "fabricant")
@NamedQueries
        ({
                @NamedQuery(name = "Fabricant.findAll", query = "SELECT F FROM Fabricant F"),
                @NamedQuery(name = "Fabricant.findAllActif", query = "SELECT F FROM Fabricant F where F.actif=true"),
                @NamedQuery(name = "Fabricant.findOne", query = "SELECT F FROM Fabricant F WHERE F.nom=:nom"),
        })
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

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif = true;

    @OneToMany(mappedBy = "fabricantIdFabricant")
    private Collection<Article> articles;

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

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Collection<Article> getArticles() {
        return articles;
    }

    public void setArticles(Collection<Article> articles) {
        this.articles = articles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fabricant fabricant = (Fabricant) o;
        return Objects.equals(id, fabricant.id) && Objects.equals(nom, fabricant.nom) && Objects.equals(actif, fabricant.actif);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, actif);
    }
}