package managedBean;

import entities.Article;
import entities.ExemplaireArticle;
import entities.Fabricant;
import org.apache.log4j.Logger;
import services.SvcArticle;
import services.SvcExemplaireArticle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class ArticleBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private Article article;
    private Fabricant fabricant;
    private List<Article> listart = new ArrayList<Article>();
    private List<Article> searchResults = new ArrayList<Article>();
    private static final Logger log = Logger.getLogger(ArticleBean.class);

    @PostConstruct
    public void init() {
        log.debug("ArticleBean init");
        article = new Article();
        fabricant = new Fabricant();
        listart = getReadAll();

    }
    public String readByFabricants(Fabricant fabricant) {
        return("/tableFabricant.xhtml?faces-redirect=true");
    }
    public String activdesactivArt() {
        SvcArticle service = new SvcArticle();
        SvcExemplaireArticle serviceEA = new SvcExemplaireArticle();
        serviceEA.setEm(service.getEm());
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            if(article.getActif())
            {
                article.setActif(false);
                for (ExemplaireArticle EA: article.getExemplaireArticles()){
                    EA.setActif(false);
                    serviceEA.save(EA);
                }
            }
            else {
                article.setActif(true);
            }
            service.save(article);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation a reussie",null));
        }finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"l'operation a échoué",null));
            }
            init();
            service.close();
        }
        return("/tableArticle.xhtml?faces-redirect=true");
    }
    public String searchArticle()
    {

        SvcArticle service = new SvcArticle();

        if(service.getbyName(article.getNom()).isEmpty())
        {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("artRech", new FacesMessage("aucun article correspondant n'a été trouvé"));
            return null;
        }
        else
        {
            searchResults = service.getbyName(article.getNom());
        }
        return "/formSearchArticle.xhtml?faces-redirect=true";
    }

    /*
     * Méthode qui permet via le service de retourner la liste de tous les articles actifs
     */
    public void getReadActiv()
    {
        SvcArticle service = new SvcArticle();
        listart = service.findAllActive();

        service.close();

    }
    /*
     * Méthode qui permet via le service de retourner la liste de tous les articles inactifs
     */
    public void getReadInactiv()
    {
        SvcArticle service = new SvcArticle();
        listart = service.findAllInactive();

        service.close();
    }
    public String flushBienv(){
        init();
        if(searchResults!= null)
        {
            searchResults.clear();
        }
        return "/bienvenue.xhtml?faces-redirect=true";
    }
    public String flushArt(){
        init();
        if(searchResults!= null)
        {
            searchResults.clear();
        }
        return "/tableArticle.xhtml?faces-redirect=true";
    }

    public List<Article> getReadAll() {
        SvcArticle service = new SvcArticle();
        listart=service.findAllArticles();
        service.close();
        return listart;
    }

    public List<Article> getReadArticle()
    {
        listart.clear();
        listart.add(article);
        return listart;
    }

    //-------------------------------Getter & Setter--------------------------------------------
    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public Fabricant getFabricant() {
        return fabricant;
    }

    public void setFabricant(Fabricant fabricant) {
        this.fabricant = fabricant;
    }

    public List<Article> getListart() {
        return listart;
    }

    public void setListart(List<Article> listart) {
        this.listart = listart;
    }

    public List<Article> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Article> searchResults) {
        this.searchResults = searchResults;
    }
}
