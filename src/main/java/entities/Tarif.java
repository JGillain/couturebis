package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "tarif")
public class Tarif implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "IdTarif", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "Denomination", nullable = false)
    private String denomination;

    @NotNull
    @Column(name = "DateDebut", nullable = false)
    private Date dateDebut;

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

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
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
        Tarif tarif = (Tarif) o;
        return Objects.equals(id, tarif.id) && Objects.equals(denomination, tarif.denomination) && Objects.equals(dateDebut, tarif.dateDebut) && Objects.equals(magasinIdMagasin, tarif.magasinIdMagasin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, denomination, dateDebut, magasinIdMagasin);
    }
}