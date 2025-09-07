package entities;

import enumeration.ExemplaireArticleStatutEnum;
import enumeration.FactureEtatEnum;
import enumeration.FactureTypeEnum;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;


@Entity
@Table(name = "facture")
@NamedQueries
        ({
                @NamedQuery(name = "Facture.findAll", query = "SELECT f FROM Facture f"),
                @NamedQuery(name = "Facture.findByType", query = "SELECT f FROM Facture f WHERE f.type=:type"),
                @NamedQuery(name = "Facture.findActiveByExemplaireArticle", query = "SELECT f FROM Facture f,FactureDetail fa WHERE f.etat=:etat AND fa.factureIdFacture=f AND fa.exemplaireArticleIdEA=:exArticle "),
                @NamedQuery(name = "Facture.findLastId", query="SELECT f FROM Facture f ORDER BY f.id DESC")
        })
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
    @Enumerated(EnumType.STRING)
    @Column(name = "Etat", nullable = false)
    private FactureEtatEnum etat;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false)
    private FactureTypeEnum type;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "UtilisateurIdUtilisateur", nullable = false)
    private Utilisateur utilisateurIdUtilisateur;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "MagasinIdMagasin", nullable = false)
    private Magasin magasinIdMagasin;

    @OneToMany(mappedBy = "factureIdFacture", fetch = FetchType.LAZY)
    private Collection<FactureDetail> factureDetails;

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

    public FactureTypeEnum getType() {
        return type;
    }

    public void setType(FactureTypeEnum type) {
        this.type = type;
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

    public Collection<FactureDetail> getFactureDetails() {
        return factureDetails;
    }

    public void setFactureDetails(Collection<FactureDetail> factureDetails) {
        this.factureDetails = factureDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Facture facture = (Facture) o;
        return Objects.equals(id, facture.id) && Objects.equals(dateDebut, facture.dateDebut) && Objects.equals(prixTVAC, facture.prixTVAC) && Objects.equals(numeroFacture, facture.numeroFacture) && etat == facture.etat && type == facture.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateDebut, prixTVAC, numeroFacture, etat, type);
    }
}