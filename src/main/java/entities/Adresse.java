package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "adresse")
@NamedQueries
        ({
                @NamedQuery(name = "Adresses.findAll", query = "SELECT a FROM Adresse a"),
                @NamedQuery(name = "Adresses.findOne", query = "SELECT a FROM Adresse a WHERE a.boite=:boite AND a.numero=:numero AND a.localiteIdLocalite=:localite AND a.rue=:rue"),
                @NamedQuery(name = "Adresses.findAllNotAdMagasin", query = "SELECT a FROM Adresse a WHERE a.magasinIdMagasin IS NULL"),

        })
public class Adresse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdAdresse", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "Rue", nullable = false)
    private String rue;

    @Size(max = 4)
    @Column(name = "Boite", length = 4)
    private String boite;

    @Size(max = 6)
    @NotNull
    @Column(name = "Numero", nullable = false, length = 6)
    private String numero;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "LocaliteIdLocalite", nullable = false)
    private Localite localiteIdLocalite;

    @ManyToOne
    @JoinColumn(name = "MagasinIdMagasin")
    private Magasin magasinIdMagasin;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRue() {
        return rue;
    }

    public void setRue(String rue) {
        this.rue = rue;
    }

    public String getBoite() {
        return boite;
    }

    public void setBoite(String boite) {
        this.boite = boite;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Localite getLocaliteIdLocalite() {
        return localiteIdLocalite;
    }

    public void setLocaliteIdLocalite(Localite localiteIdLocalite) {
        this.localiteIdLocalite = localiteIdLocalite;
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
        Adresse adresse = (Adresse) o;
        return Objects.equals(id, adresse.id) && Objects.equals(rue, adresse.rue) && Objects.equals(boite, adresse.boite) && Objects.equals(numero, adresse.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rue, boite, numero);
    }
}