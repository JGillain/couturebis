package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "utilisateur_magasin")
public class UtilisateurMagasin implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdUtilisateurMagasin", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
        UtilisateurMagasin that = (UtilisateurMagasin) o;
        return Objects.equals(id, that.id) && Objects.equals(utilisateurIdUtilisateur, that.utilisateurIdUtilisateur) && Objects.equals(magasinIdMagasin, that.magasinIdMagasin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, utilisateurIdUtilisateur, magasinIdMagasin);
    }
}