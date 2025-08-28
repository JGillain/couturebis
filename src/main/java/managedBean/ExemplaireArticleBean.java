package managedBean;

import entities.*;
import enumeration.ExemplaireArticleStatutEnum;
import org.apache.log4j.Logger;
import services.SvcArticle;
import services.SvcCodeBarre;
import services.SvcExemplaireArticle;
import services.SvcMagasin;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
@SessionScoped
public class ExemplaireArticleBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private ExemplaireArticle EA;
    private Article article;
    private Magasin magasin;
    private int nombreExemplaire;
    private boolean flagVente;
    private List<ExemplaireArticle> listexart = new ArrayList<ExemplaireArticle>();
    private List<ExemplaireArticle> searchResults = new ArrayList<ExemplaireArticle>();
    @Inject
    private MagasinBean magasinBean;
    @Inject
    private CodeBarreBean codeBarreBean;
    private static final Logger log = Logger.getLogger(ExemplaireArticleBean.class);

    @PostConstruct
    public void init() {
        log.debug("ExemplaireArticleBean init");
        EA = new ExemplaireArticle();
        listexart = getReadAll();
        magasin = magasinBean.getMagasin();
    }

    public void saveBatch() {
        log.debug("ExemplaireArticleBean saveBatch");

        /* ---- basic validation ---- */
        if ( nombreExemplaire < 1) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "La quantité doit être un entier positif (≥ 1)", null));
            return;   // abort
        }
        if (article == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Article manquant", null));
            return;
        }
        if (magasin == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Magasin manquant", null));
            return;
        }

        SvcExemplaireArticle service = new SvcExemplaireArticle();
        SvcCodeBarre svcCB = new SvcCodeBarre();
        svcCB.setEm(service.getEm());
        EntityTransaction tx = service.getTransaction();
        tx.begin();
        try {
            /* initialisation liste code barres, ne sera utilisé que si mise en location*/
            List<String> code = Collections.emptyList();
            if (!flagVente){
                if (nombreExemplaire < 1 ){
                    throw new IllegalStateException("nombreExemplaire < 1");
                }
                code = codeBarreBean.createCB(false,nombreExemplaire);

            }
            for (int i = 0; i < nombreExemplaire; i++) {
                ExemplaireArticle ex = new ExemplaireArticle();
                ex.setArticleIdArticle(article);
                ex.setMagasinIdMagasin(magasin);
                ex.setStatut(flagVente ? ExemplaireArticleStatutEnum.Vente
                        : ExemplaireArticleStatutEnum.Location);

                if (!flagVente) {
                    CodeBarre cb = new CodeBarre();
                    cb.setCodeBarre(code.get(i));
                    svcCB.save(cb);          // persist codebarre
                    ex.setCodeBarreIdCB(cb); // link
                }
                service.save(ex);              // persist exemplaire
            }
            tx.commit();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            nombreExemplaire + " exemplaire(s) enregistré(s)", null));
        }
        catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            log.error("Erreur lors du saveBatch", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Échec de l'enregistrement", null));
        }
        finally {
            service.close();
        }
    }

    public String activdesactivExArt() {

        SvcExemplaireArticle service = new SvcExemplaireArticle();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            if (EA.getActif()) {
                EA.setActif(false);
            } else {
                EA.setActif(true);
            }
            service.save(EA);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "L'operation a reussie", null));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "l'operation a échoué", null));
            }
            init();
            service.close();
        }
        return ("/tableExArticle.xhtml?faces-redirect=true");
    }

    public String getExArticleByArticle() {
        SvcExemplaireArticle service = new SvcExemplaireArticle();
        listexart = service.findByArticle(article);
        service.close();
        return "/tableExArticle.xhtml?faces-redirect=true";
    }

    public String flushBienv() {
        init();
        if (searchResults != null) {
            searchResults.clear();
        }
        return "/bienvenue.xhtml?faces-redirect=true";
    }

    public String flushExArt() {
        init();
        if (searchResults != null) {
            searchResults.clear();
        }
        return "/tableExArticle.xhtml?faces-redirect=true";
    }

    public List<ExemplaireArticle> getReadAll() {
        SvcExemplaireArticle service = new SvcExemplaireArticle();
        listexart = service.findAllExArticles();
        service.close();
        return listexart;
    }

    public List<ExemplaireArticle> getReadExArticle() {
        listexart.clear();
        listexart.add(EA);
        return listexart;
    }

    //-------------------------------Getter & Setter--------------------------------------------


    public int getNombreExemplaire() {
        return nombreExemplaire;
    }

    public void setNombreExemplaire(int nombreExemplaire) {
        this.nombreExemplaire = nombreExemplaire;
    }

    public boolean isFlagVente() {
        return flagVente;
    }

    public void setFlagVente(boolean flagVente) {
        this.flagVente = flagVente;
    }

    public List<ExemplaireArticle> getListexart() {
        return listexart;
    }

    public void setListexart(List<ExemplaireArticle> listexart) {
        this.listexart = listexart;
    }

    public List<ExemplaireArticle> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<ExemplaireArticle> searchResults) {
        this.searchResults = searchResults;
    }

    public ExemplaireArticle getEA() {
        return EA;
    }

    public void setEA(ExemplaireArticle EA) {
        this.EA = EA;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void setMagasin(Magasin magasin) {
        this.magasin = magasin;
    }
}