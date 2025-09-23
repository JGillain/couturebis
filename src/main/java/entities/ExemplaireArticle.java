package entities;

import enumeration.ExemplaireArticleEtatEnum;
import enumeration.ExemplaireArticleStatutEnum;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "exemplaire_article")
@NamedQueries
        ({
                @NamedQuery(name = "ExArticle.findAllTri", query="SELECT EA FROM ExemplaireArticle EA ORDER BY EA.articleIdArticle.nom ASC"),
                @NamedQuery(name = "ExArticle.findActive", query = "SELECT EA FROM ExemplaireArticle EA WHERE EA.actif=TRUE"),
                @NamedQuery(name = "ExArticle.findInactive", query = "SELECT EA FROM ExemplaireArticle EA WHERE EA.actif=FALSE"),
                @NamedQuery(name = "ExArticle.searchTri", query="SELECT EA FROM ExemplaireArticle EA WHERE EA.articleIdArticle.nom=:nom ORDER BY EA.articleIdArticle.nom ASC"),
                @NamedQuery(name = "ExArticle.search", query="SELECT EA FROM ExemplaireArticle EA WHERE EA.articleIdArticle.nom=:nom"),
                @NamedQuery(name = "ExArticle.findByArticle", query="SELECT EA FROM ExemplaireArticle EA WHERE EA.articleIdArticle=:article"),
                @NamedQuery(name = "ExArticle.findOneByCodeBarre", query="SELECT EA FROM ExemplaireArticle EA WHERE EA.codeBarreIdCB.codeBarre=:CB"),
                @NamedQuery(name = "ExArticle.AvailableExArticlesRent", query="SELECT EA FROM ExemplaireArticle EA WHERE EA.articleIdArticle =:article AND EA.statut = enumeration.ExemplaireArticleStatutEnum.Location and EA.actif=true"),
                @NamedQuery(name = "ExArticle.AvailableExArticlesRentNotReserved", query="SELECT EA FROM ExemplaireArticle EA WHERE EA.articleIdArticle =:article AND EA.statut = enumeration.ExemplaireArticleStatutEnum.Location and EA.actif=true and EA.reserve=false"),
                @NamedQuery(name = "ExArticle.AvailableExArticlesRentReserved", query="SELECT EA FROM ExemplaireArticle EA WHERE EA.articleIdArticle =:article AND EA.statut = enumeration.ExemplaireArticleStatutEnum.Location and EA.actif=true and EA.reserve=true"),
                @NamedQuery(name = "ExArticle.AvailableExArticlesSales", query="SELECT EA FROM ExemplaireArticle EA WHERE EA.articleIdArticle =:article AND EA.statut = enumeration.ExemplaireArticleStatutEnum.Vente and EA.actif=true and EA.loue=false and EA.reserve=false"),
        })
public class ExemplaireArticle implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdExemplaireArticle", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "Etat", nullable = false)
    private ExemplaireArticleEtatEnum etat = ExemplaireArticleEtatEnum.Bon;

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif = true;

    @Size(max = 500)
    @NotNull
    @Column(name = "CommentaireEtat", nullable = false, length = 500)
    private String commentaireEtat="neuf";

    @NotNull
    @Column(name = "Loue", nullable = false)
    private Boolean loue = false;

    @NotNull
    @Column(name = "Reserve", nullable = false)
    private Boolean reserve = false;

    @NotNull
    @Column(name = "Transfert", nullable = false)
    private Boolean transfert = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private ExemplaireArticleStatutEnum statut;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "ArticleIdArticle", nullable = false)
    private Article articleIdArticle;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "MagasinIdMagasin", nullable = false)
    private Magasin magasinIdMagasin;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codeBarreIdCB", nullable = true)
    private CodeBarre codeBarreIdCB;

    @OneToMany(mappedBy = "exemplaireArticleIdEA" ,fetch = FetchType.LAZY)
    private Collection<FactureDetail> factureDetails;

    public CodeBarre getCodeBarreIdCB() {
        return codeBarreIdCB;
    }

    public void setCodeBarreIdCB(CodeBarre codeBarreIdCB) {
        this.codeBarreIdCB = codeBarreIdCB;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ExemplaireArticleEtatEnum getEtat() {
        return etat;
    }

    public void setEtat(ExemplaireArticleEtatEnum etat) {
        this.etat = etat;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public String getCommentaireEtat() {
        return commentaireEtat;
    }

    public void setCommentaireEtat(String commentaireEtat) {
        this.commentaireEtat = commentaireEtat;
    }

    public Boolean getLoue() {
        return loue;
    }

    public void setLoue(Boolean loue) {
        this.loue = loue;
    }

    public Boolean getReserve() {
        return reserve;
    }

    public void setReserve(Boolean reserve) {
        this.reserve = reserve;
    }

    public Boolean getTransfert() {
        return transfert;
    }

    public void setTransfert(Boolean transfert) {
        this.transfert = transfert;
    }

    public ExemplaireArticleStatutEnum getStatut() {
        return statut;
    }

    public void setStatut(ExemplaireArticleStatutEnum statut) {
        this.statut = statut;
    }

    public Article getArticleIdArticle() {
        return articleIdArticle;
    }

    public void setArticleIdArticle(Article articleIdArticle) {
        this.articleIdArticle = articleIdArticle;
    }

    public Magasin getMagasinIdMagasin() {
        return magasinIdMagasin;
    }

    public void setMagasinIdMagasin(Magasin magasinIdMagasin) {
        this.magasinIdMagasin = magasinIdMagasin;
    }

    public Collection<FactureDetail> getFactureDetails() {
        return factureDetails;
    }

    public void setFactureDetails(Collection<FactureDetail> factureDetails) {
        this.factureDetails = factureDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExemplaireArticle that = (ExemplaireArticle) o;
        return Objects.equals(id, that.id) && etat == that.etat && Objects.equals(actif, that.actif) && Objects.equals(commentaireEtat, that.commentaireEtat) && Objects.equals(loue, that.loue) && Objects.equals(reserve, that.reserve) && Objects.equals(transfert, that.transfert) && statut == that.statut && Objects.equals(articleIdArticle, that.articleIdArticle) && Objects.equals(magasinIdMagasin, that.magasinIdMagasin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, etat, actif, commentaireEtat, loue, reserve, transfert, statut, articleIdArticle, magasinIdMagasin);
    }

}