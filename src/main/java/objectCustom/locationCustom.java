package objectCustom;

public class locationCustom {
    private int nbrJours;
    private String CB;
    public locationCustom(){
        nbrJours=0;
        CB="";
    }
    public locationCustom(int nbrJours, String CB) {
        this.nbrJours = nbrJours;
        this.CB = CB;
    }
    public int getNbrJours() {
        return nbrJours;
    }

    public void setNbrJours(int nbrJours) {
        this.nbrJours = nbrJours;
    }

    public String getCB() {
        return CB;
    }

    public void setCB(String CB) {
        this.CB = CB;
    }


}
