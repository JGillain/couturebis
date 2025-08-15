package objectCustom;

import java.util.Date;

public class JourCustom
{

    private int nbrJours;
    private Double prix;
    private Date dateDebut, dateFin;

    public JourCustom(int nbrJours, Double prix, Date dateDebut, Date dateFin) {
        this.nbrJours = nbrJours;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public JourCustom() {
        this.nbrJours = 0;
        this.prix = 0.0;
        this.dateDebut = new Date();
        this.dateFin = new Date();
    }

    public int getNbrJours() {
        return nbrJours;
    }

    public void setNbrJours(int nbrJours) {
        this.nbrJours = nbrJours;
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
}
