package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "article")
@NamedQueries
        ({
                @NamedQuery(name = "Article.findAll", query = "SELECT A FROM Article A"),
                @NamedQuery(name = "Article.findAllTri", query="SELECT A FROM Article A ORDER BY A.nom ASC"),
                @NamedQuery(name = "Article.findActive", query = "SELECT A FROM Article A WHERE A.actif=TRUE"),
                @NamedQuery(name = "Article.findInactive", query = "SELECT A FROM Article A WHERE A.actif=FALSE"),
                @NamedQuery(name = "Article.searchTri", query="SELECT A FROM Article A WHERE A.nom=:nom ORDER BY A.nom ASC"),//A verifier
                @NamedQuery(name = "Article.findByFabricant", query = "SELECT A FROM Article A WHERE A.fabricantIdFabricant=:fab"),
                @NamedQuery(name = "Article.findByNumserie", query = "SELECT A FROM Article A WHERE A.numSerie=:numserie"),
                @NamedQuery(name = "Article.search", query="SELECT A FROM Article A WHERE A.nom=:nom"),
        })
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
    @Column(name = "NumSerie", nullable = false)
    private String numSerie;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "FabricantIdFabricant", nullable = false)
    private Fabricant fabricantIdFabricant;

    @OneToMany(mappedBy = "articleIdArticle")
    private Collection<ExemplaireArticle> exemplaireArticles;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategorieIdCategorie", nullable = false)
    private Categorie categorieIdCategorie;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CodeBarreIdCB", nullable = false)
    private CodeBarre codeBarreIdCB;

    public CodeBarre getCodeBarreIdCB() {
        return codeBarreIdCB;
    }

    public void setCodeBarreIdCB(CodeBarre codeBarreIdCB) {
        this.codeBarreIdCB = codeBarreIdCB;
    }

    public Categorie getCategorieIdCategorie() {
        return categorieIdCategorie;
    }

    public void setCategorieIdCategorie(Categorie categorieIdCategorie) {
        this.categorieIdCategorie = categorieIdCategorie;
    }

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

    public String getNumSerie() {
        return numSerie;
    }

    public void setNumSerie(String numSerie) {
        this.numSerie = numSerie;
    }

    public Collection<ExemplaireArticle> getExemplaireArticles() {
        return exemplaireArticles;
    }

    public void setExemplaireArticles(Collection<ExemplaireArticle> exemplaireArticles) {
        this.exemplaireArticles = exemplaireArticles;
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
        return Objects.equals(id, article.id) && Objects.equals(nom, article.nom) && Objects.equals(actif, article.actif) && Objects.equals(prix, article.prix) && Objects.equals(numSerie, article.numSerie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, actif, prix, numSerie);
    }
}