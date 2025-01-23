package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "article")
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdArticle", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "Nom", nullable = false)
    private String nom;

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif = false;

    @NotNull
    @Column(name = "Prix", nullable = false)
    private Double prix;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "FabricantIdFabricant", nullable = false)
    private Fabricant fabricantIdFabricant;

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

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Fabricant getFabricantIdFabricant() {
        return fabricantIdFabricant;
    }

    public void setFabricantIdFabricant(Fabricant fabricantIdFabricant) {
        this.fabricantIdFabricant = fabricantIdFabricant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(id, article.id) && Objects.equals(nom, article.nom) && Objects.equals(actif, article.actif) && Objects.equals(prix, article.prix) && Objects.equals(fabricantIdFabricant, article.fabricantIdFabricant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, actif, prix, fabricantIdFabricant);
    }
}