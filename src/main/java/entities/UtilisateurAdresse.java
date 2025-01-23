package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "utilisateur_adresse")
public class UtilisateurAdresse implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdUtilisateurAdresse", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "UtilisateurIdUtilisateur", nullable = false)
    private Utilisateur utilisateurIdUtilisateur;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "AdresseIdAdresse", nullable = false)
    private Adresse adresseIdAdresse;

    @NotNull
    @Column(name = "Actif", nullable = false)
    private Boolean actif = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Utilisateur getUtilisateurIdUtilisateur() {
        return utilisateurIdUtilisateur;
    }

    public void setUtilisateurIdUtilisateur(Utilisateur utilisateurIdUtilisateur) {
        this.utilisateurIdUtilisateur = utilisateurIdUtilisateur;
    }

    public Adresse getAdresseIdAdresse() {
        return adresseIdAdresse;
    }

    public void setAdresseIdAdresse(Adresse adresseIdAdresse) {
        this.adresseIdAdresse = adresseIdAdresse;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtilisateurAdresse that = (UtilisateurAdresse) o;
        return Objects.equals(id, that.id) && Objects.equals(utilisateurIdUtilisateur, that.utilisateurIdUtilisateur) && Objects.equals(adresseIdAdresse, that.adresseIdAdresse) && Objects.equals(actif, that.actif);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, utilisateurIdUtilisateur, adresseIdAdresse, actif);
    }
}