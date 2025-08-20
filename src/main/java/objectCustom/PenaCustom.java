package objectCustom;

import entities.Article;

import java.util.Date;

public class PenaCustom {

    private String name;
    private Double prix;
    private Date dateDebut, dateFin;
    private Article article;


    public PenaCustom(String name, Double prix, Date dateDebut, Date dateFin, Article article) {
        this.name = name;
        this.prix = prix;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.article = article;
    }

    public PenaCustom() {
        this.name = "";
        this.prix = 0.0;
        this.dateDebut = new Date();
        this.dateFin = new Date();
        this.article = new  Article();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
