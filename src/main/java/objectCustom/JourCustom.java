package objectCustom;

import entities.Article;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JourCustom
{

    private int nbrJours;
    private Double prix;
    private Date dateDebut, dateFin;
    private Article article;

    public JourCustom(int nbrJours, Double prix, Date dateDebut, Date dateFin, Article article) {
        this.nbrJours = nbrJours;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.article = article;
    }

    public JourCustom() {
        this.nbrJours = 0;
        this.prix = 0.0;
        this.dateDebut = new Date();
        this.dateFin = new Date();
        this.article = new Article();
    }
    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return "JourCustom{" +
                "nbrJours=" + nbrJours +
                ", prix=" + prix +
                ", dateDebut=" + (dateDebut != null ? df.format(dateDebut) : "null") +
                ", dateFin=" + (dateFin != null ? df.format(dateFin) : "null") +
                ", article=" + (article != null ? article.getNom() : "null") +
                '}';
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

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
