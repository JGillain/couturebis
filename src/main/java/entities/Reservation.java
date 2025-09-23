package entities;

import enumeration.ReservationStatutEnum;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "reservation")
@NamedQueries({
        @NamedQuery(name = "Reservation.findAll", query = "SELECT r FROM Reservation r ORDER BY r.statut, r.dateDemande, r.id"),
        @NamedQuery(name = "Reservation.findNextByArticleMagasin",query = "SELECT r FROM Reservation r WHERE r.articleIdArticle = :article AND r.magasinIdMagasin = :magasin AND r.actif = TRUE AND r.statut = enumeration.ReservationStatutEnum.file ORDER BY r.dateDemande ASC, r.id ASC")
})
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdReservations", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif = true;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="Statut", length=20, nullable=false)
    private ReservationStatutEnum statut = ReservationStatutEnum.file;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="DateDemande", nullable=false)
    private Date dateDemande = new Date();

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ExemplaireArticleIdEA")
    private ExemplaireArticle exemplaire;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="DateReady")
    private Date dateReady;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="HoldUntil")
    private Date holdUntil;


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

    public ReservationStatutEnum getStatut() {
        return statut;
    }

    public void setStatut(ReservationStatutEnum statut) {
        this.statut = statut;
    }

    public Date getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Date dateDemande) {
        this.dateDemande = dateDemande;
    }

    public ExemplaireArticle getExemplaire() {
        return exemplaire;
    }

    public void setExemplaire(ExemplaireArticle exemplaire) {
        this.exemplaire = exemplaire;
    }

    public Date getDateReady() {
        return dateReady;
    }

    public void setDateReady(Date dateReady) {
        this.dateReady = dateReady;
    }

    public Date getHoldUntil() {
        return holdUntil;
    }

    public void setHoldUntil(Date holdUntil) {
        this.holdUntil = holdUntil;
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