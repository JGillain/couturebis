package objectCustom;

import org.apache.log4j.Logger;

public class venteCustom {
    private static final Logger log = Logger.getLogger(venteCustom.class);
    private int nbrArticles;
    private String CB;
    private String articleNom;
    public venteCustom(){
        nbrArticles=0;
        CB="";
        articleNom="";
    }
    public venteCustom(int nbrJours, String CB, String articleNom) {
        this.nbrArticles = nbrJours;
        this.CB = CB;
        this.articleNom=articleNom;
    }

    @Override
    public String toString() {
        return "venteCustom{" +
                "CB='" + ((CB.isEmpty()) ? "empty" : CB) + '\'' +
                ", nbrArticles=" + nbrArticles +
                ", articleNom=" + ((articleNom.isEmpty() ) ? "empty" : articleNom) +
                '}';
    }

    public int getNbrArticles() {
        return nbrArticles;
    }

    public void setNbrArticles(int nbrArticles) {
        this.nbrArticles = nbrArticles;
    }

    public String getCB() {
        log.debug("getCB");
        return CB;
    }

    public void setCB(String CB) {
        log.debug("setCB : " + CB);
        this.CB = CB;
    }

    public String getArticleNom() {
        return articleNom;
    }

    public void setArticleNom(String articleNom) {
        this.articleNom = articleNom;
    }
}
