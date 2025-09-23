package objectCustom;

import managedBean.UtilisateurBean;
import org.apache.log4j.Logger;

public class locationCustom {
    private static final Logger log = Logger.getLogger(locationCustom.class);
    private int nbrJours;
    private String CB;
    private String articleNom;
    public locationCustom(){
        nbrJours=0;
        CB="";
        articleNom="";
    }
    public locationCustom(int nbrJours, String CB, String articleNom) {
        this.nbrJours = nbrJours;
        this.CB = CB;
        this.articleNom = articleNom;
    }

    @Override
    public String toString() {
        return "locationCustom{" +
                "CB='" + CB + '\'' +
                ", nbrJours=" + nbrJours +'\'' +
                ", articleNom=" + articleNom +
                '}';
    }

    public int getNbrJours() {
        log.debug("getNbrJours");
        return nbrJours;
    }

    public void setNbrJours(int nbrJours) {
        log.debug("setNbrJours : " + nbrJours);
        this.nbrJours = nbrJours;
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
