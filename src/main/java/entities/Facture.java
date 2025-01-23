package entities;

import enumeration.FactureEtatEnum;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;


@Entity
@Table(name = "facture")
public class Facture implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdFacture", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "DateDebut", nullable = false)
    private Timestamp dateDebut;

    @Column(name = "PrixTVAC")
    private Double prixTVAC;

    @Size(max = 45)
    @Column(name = "NumeroFacture", length = 45)
    private String numeroFacture;

    @NotNull
    @Lob
    @Column(name = "Etat", nullable = false)
    private FactureEtatEnum etat;

    @Size(max = 255)
    @NotNull
    @Column(name = "LienPdf", nullable = false)
    private String lienPdf;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "UtilisateurIdUtilisateur", nullable = false)
    private Utilisateur utilisateurIdUtilisateur;

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

    public Timestamp getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Timestamp dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Double getPrixTVAC() {
        return prixTVAC;
    }

    public void setPrixTVAC(Double prixTVAC) {
        this.prixTVAC = prixTVAC;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public FactureEtatEnum getEtat() {
        return etat;
    }

    public void setEtat(FactureEtatEnum etat) {
        this.etat = etat;
    }

    public String getLienPdf() {
        return lienPdf;
    }

    public void setLienPdf(String lienPdf) {
        this.lienPdf = lienPdf;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Facture facture = (Facture) o;
        return Objects.equals(id, facture.id) && Objects.equals(dateDebut, facture.dateDebut) && Objects.equals(prixTVAC, facture.prixTVAC) && Objects.equals(numeroFacture, facture.numeroFacture) && etat == facture.etat && Objects.equals(lienPdf, facture.lienPdf) && Objects.equals(utilisateurIdUtilisateur, facture.utilisateurIdUtilisateur) && Objects.equals(magasinIdMagasin, facture.magasinIdMagasin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateDebut, prixTVAC, numeroFacture, etat, lienPdf, utilisateurIdUtilisateur, magasinIdMagasin);
    }
}