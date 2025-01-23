package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "facture_detail")
public class FactureDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdFactureDetail", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "FactureIdFacture", nullable = false)
    private Facture factureIdFacture;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "ExemplaireArticleIdEA", nullable = false)
    private ExemplaireArticle exemplaireArticleIdEA;

    @NotNull
    @Column(name = "DateFin", nullable = false)
    private Date dateFin;

    @Column(name = "DateRetour")
    private Date dateRetour;

    @Size(max = 500)
    @Column(name = "EtatRendu", length = 500)
    private String etatRendu;

    @NotNull
    @Column(name = "Prix", nullable = false)
    private Double prix;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Facture getFactureIdFacture() {
        return factureIdFacture;
    }

    public void setFactureIdFacture(Facture factureIdFacture) {
        this.factureIdFacture = factureIdFacture;
    }

    public ExemplaireArticle getExemplaireArticleIdEA() {
        return exemplaireArticleIdEA;
    }

    public void setExemplaireArticleIdEA(ExemplaireArticle exemplaireArticleIdEA) {
        this.exemplaireArticleIdEA = exemplaireArticleIdEA;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public Date getDateRetour() {
        return dateRetour;
    }

    public void setDateRetour(Date dateRetour) {
        this.dateRetour = dateRetour;
    }

    public String getEtatRendu() {
        return etatRendu;
    }

    public void setEtatRendu(String etatRendu) {
        this.etatRendu = etatRendu;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactureDetail that = (FactureDetail) o;
        return Objects.equals(id, that.id) && Objects.equals(factureIdFacture, that.factureIdFacture) && Objects.equals(exemplaireArticleIdEA, that.exemplaireArticleIdEA) && Objects.equals(dateFin, that.dateFin) && Objects.equals(dateRetour, that.dateRetour) && Objects.equals(etatRendu, that.etatRendu) && Objects.equals(prix, that.prix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, factureIdFacture, exemplaireArticleIdEA, dateFin, dateRetour, etatRendu, prix);
    }
}