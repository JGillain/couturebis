package entities;

import enumeration.ExemplaireArticleEtatEnum;
import enumeration.ExemplaireArticleStatutEnum;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "exemplaire_article")
public class ExemplaireArticle implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdExemplaireArticle", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "CodeBarre", nullable = false, length = 45)
    private String codeBarre;

    @NotNull
    @Lob
    @Column(name = "Etat", nullable = false)
    private ExemplaireArticleEtatEnum etat;

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif = false;

    @Size(max = 500)
    @NotNull
    @Column(name = "CommentaireEtat", nullable = false, length = 500)
    private String commentaireEtat;

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
    @Lob
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExemplaireArticle that = (ExemplaireArticle) o;
        return Objects.equals(id, that.id) && Objects.equals(codeBarre, that.codeBarre) && etat == that.etat && Objects.equals(actif, that.actif) && Objects.equals(commentaireEtat, that.commentaireEtat) && Objects.equals(loue, that.loue) && Objects.equals(reserve, that.reserve) && Objects.equals(transfert, that.transfert) && statut == that.statut && Objects.equals(articleIdArticle, that.articleIdArticle) && Objects.equals(magasinIdMagasin, that.magasinIdMagasin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codeBarre, etat, actif, commentaireEtat, loue, reserve, transfert, statut, articleIdArticle, magasinIdMagasin);
    }
}