package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "tarif_jour")
@NamedQueries
        ({
                @NamedQuery(name = "TarifJour.findAll", query = "SELECT tj FROM TarifJour tj"),
                // Todo : a tester
                @NamedQuery(name = "TarifJour.findByJour", query = "SELECT tj FROM TarifJour tj WHERE tj.dateDebut<=:dateDebut AND tj.dateFin>=:dateFin AND tj.jourIdJour.nbrJour<=:jour AND tj.tarifIdTarif=:tarif ORDER BY tj.dateDebut DESC, tj.jourIdJour.nbrJour DESC "),
        })
public class TarifJour implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdTarifJour", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "TarifIdTarif", nullable = false)
    private Tarif tarifIdTarif;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "JourIdJour", nullable = false)
    private Jour jourIdJour;

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

    public Jour getJourIdJour() {
        return jourIdJour;
    }

    public void setJourIdJour(Jour jourIdJour) {
        this.jourIdJour = jourIdJour;
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
        TarifJour tarifJour = (TarifJour) o;
        return Objects.equals(id, tarifJour.id) && Objects.equals(tarifIdTarif, tarifJour.tarifIdTarif) && Objects.equals(jourIdJour, tarifJour.jourIdJour) && Objects.equals(prix, tarifJour.prix) && Objects.equals(dateDebut, tarifJour.dateDebut) && Objects.equals(dateFin, tarifJour.dateFin) && Objects.equals(articleIdArticle, tarifJour.articleIdArticle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tarifIdTarif, jourIdJour, prix, dateDebut, dateFin, articleIdArticle);
    }
}