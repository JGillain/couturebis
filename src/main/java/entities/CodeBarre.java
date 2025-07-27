package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "code_barre")
@NamedQueries({
        @NamedQuery(name = "CodeBarre.findAll", query = "SELECT cb FROM CodeBarre cb"),
        @NamedQuery(name = "CodeBarre.findOne", query = "SELECT cb FROM CodeBarre cb WHERE cb.codeBarre=:barcode"),
        @NamedQuery(name = "CodeBarre.findAllInRange", query = "SELECT cb FROM CodeBarre cb WHERE cb.codeBarre >= :start AND cb.codeBarre <= :end ORDER BY cb.codeBarre DESC"),
})
public class CodeBarre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCodeBarre", nullable = false)
    private Integer id;

    @Size(max = 13)
    @NotNull
    @Column(name = "codeBarre", nullable = false, length = 13)
    private String codeBarre;

    @OneToOne(mappedBy = "codeBarreIdCB", fetch = FetchType.LAZY)
    private Article articles;

    @OneToOne(mappedBy = "codeBarreIdCB", fetch = FetchType.LAZY)
    private ExemplaireArticle exemplaireArticles;

    @OneToOne(mappedBy = "codeBarreIdCB", fetch = FetchType.LAZY)
    private Utilisateur utilisateurs;

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

    public Utilisateur getUtilisateurs() {
        return utilisateurs;
    }

    public void setUtilisateurs(Utilisateur utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    public ExemplaireArticle getExemplaireArticles() {
        return exemplaireArticles;
    }

    public void setExemplaireArticles(ExemplaireArticle exemplaireArticles) {
        this.exemplaireArticles = exemplaireArticles;
    }

    public Article getArticles() {
        return articles;
    }

    public void setArticles(Article articles) {
        this.articles = articles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeBarre codeBarre1 = (CodeBarre) o;
        return Objects.equals(id, codeBarre1.id) && Objects.equals(codeBarre, codeBarre1.codeBarre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codeBarre);
    }
}