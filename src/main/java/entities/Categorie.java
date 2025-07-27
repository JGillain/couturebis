package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "categorie")
@NamedQueries
        ({
                @NamedQuery(name = "Categorie.findAll", query = "SELECT C FROM Categorie C"),
                @NamedQuery(name = "Categorie.findOne", query = "SELECT C FROM Categorie C WHERE C.nom=:nom"),
        })
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCategorie", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "Nom", nullable = false)
    private String nom;

    @OneToMany(mappedBy = "categorieIdCategorie")
    private Set<Article> articles = new LinkedHashSet<>();

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

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categorie categorie = (Categorie) o;
        return Objects.equals(id, categorie.id) && Objects.equals(nom, categorie.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }
}