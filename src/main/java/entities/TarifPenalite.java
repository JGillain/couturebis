package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "tarif_penalite")
@NamedQueries
        ({
                @NamedQuery(name = "TarifPenalite.findAll", query = "SELECT tp FROM TarifPenalite tp"),
                @NamedQuery(name = "TarifPenalite.FindTarifPenaByTarifByArticle", query = "SELECT tp FROM TarifPenalite tp WHERE tp.dateDebut<=:date AND tp.dateFin>=:date AND tp.tarifIdTarif=:tarif AND tp.articleIdArticle=:article ORDER BY tp.dateDebut DESC"),
                @NamedQuery(name = "TarifPenalite.findByPenalitesByArticle", query = "SELECT tp FROM TarifPenalite tp WHERE tp.dateDebut<=:dateDebut AND tp.dateFin>=:dateFin AND tp.penaliteIdPenalite=:penalite AND tp.tarifIdTarif=:tarif AND tp.articleIdArticle=:article ORDER BY tp.dateDebut DESC"),
        })
public class TarifPenalite implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdTarifPenalite", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "TarifIdTarif", nullable = false)
    private Tarif tarifIdTarif;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "PenaliteIdPenalite", nullable = false)
    private Penalite penaliteIdPenalite;

    @NotNull
    @Column(name = "Prix", nullable = false)
    private Double prix;

    @NotNull
    @Column(name = "DateDebut", nullable = false)
    private Date dateDebut;

    @NotNull
    @Column(name = "DateFin", nullable = false)
    private Date dateFin;

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

    public Tarif getTarifIdTarif() {
        return tarifIdTarif;
    }

    public void setTarifIdTarif(Tarif tarifIdTarif) {
        this.tarifIdTarif = tarifIdTarif;
    }

    public Penalite getPenaliteIdPenalite() {
        return penaliteIdPenalite;
    }

    public void setPenaliteIdPenalite(Penalite penaliteIdPenalite) {
        this.penaliteIdPenalite = penaliteIdPenalite;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
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
        TarifPenalite that = (TarifPenalite) o;
        return Objects.equals(id, that.id) && Objects.equals(tarifIdTarif, that.tarifIdTarif) && Objects.equals(penaliteIdPenalite, that.penaliteIdPenalite) && Objects.equals(prix, that.prix) && Objects.equals(dateDebut, that.dateDebut) && Objects.equals(dateFin, that.dateFin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tarifIdTarif, penaliteIdPenalite, prix, dateDebut, dateFin);
    }
}