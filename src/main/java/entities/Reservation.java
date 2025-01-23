package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "reservation")
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdReservations", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif;

    @NotNull
    @Column(name = "MailEnvoye", nullable = false)
    private Boolean mailEnvoye = false;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "UtilisateurIdUtilisateur", nullable = false)
    private Utilisateur utilisateurIdUtilisateur;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "MagasinIdMagasin", nullable = false)
    private Magasin magasinIdMagasin;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "ArticleIdArticle", nullable = false)
    private Article articleIdArticle;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Boolean getMailEnvoye() {
        return mailEnvoye;
    }

    public void setMailEnvoye(Boolean mailEnvoye) {
        this.mailEnvoye = mailEnvoye;
    }

    public Utilisateur getUtilisateurIdUtilisateur() {
        return utilisateurIdUtilisateur;
    }

    public void setUtilisateurIdUtilisateur(Utilisateur utilisateurIdUtilisateur) {
        this.utilisateurIdUtilisateur = utilisateurIdUtilisateur;
    }

    public Magasin getMagasinIdMagasin() {
        return magasinIdMagasin;
    }

    public void setMagasinIdMagasin(Magasin magasinIdMagasin) {
        this.magasinIdMagasin = magasinIdMagasin;
    }

    public Article getArticleIdArticle() {
        return articleIdArticle;
    }

    public void setArticleIdArticle(Article articleIdArticle) {
        this.articleIdArticle = articleIdArticle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(actif, that.actif) && Objects.equals(mailEnvoye, that.mailEnvoye) && Objects.equals(utilisateurIdUtilisateur, that.utilisateurIdUtilisateur) && Objects.equals(magasinIdMagasin, that.magasinIdMagasin) && Objects.equals(articleIdArticle, that.articleIdArticle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, actif, mailEnvoye, utilisateurIdUtilisateur, magasinIdMagasin, articleIdArticle);
    }
}