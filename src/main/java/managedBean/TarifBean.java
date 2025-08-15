package managedBean;


import entities.*;
import objectCustom.JourCustom;
import objectCustom.PenaCustom;
import org.apache.log4j.Logger;
import services.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Named
@SessionScoped
public class TarifBean implements Serializable {
    // Déclaration des variables globales
    private static final Logger log = Logger.getLogger(TarifBean.class);
    private static final long serialVersionUID = 1L;
    private Tarif tarif;

    private List<PenaCustom> grillePena = new ArrayList<>();
    private List<JourCustom> grilleJour = new ArrayList<>();

    @PostConstruct
    public void init()
    {
        tarif=new Tarif();
        grilleJour.clear();
        grillePena.clear();
        grillePena.add(new PenaCustom());
        grilleJour.add(new JourCustom());
    }

    public void save()
    {
        SvcTarif service = new SvcTarif();
        EntityTransaction transaction = service.getTransaction();
        transaction.begin();
        try {
            service.save(tarif);
            transaction.commit();
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation a reussie",null));
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"L'operation n'a pas reussie",null));
            }
            service.close();
        }
    }
    public String redirectEdit(){
        long dn = new Date().getTime();
        if(tarif.getDateDebut().getTime() > dn){
            grillePena.clear();
            grilleJour.clear();
            if (tarif.getTarifPenalite().size()!=0){
                for (TarifPenalite TP: tarif.getTarifPenalite()) {
                    grillePena.add(new PenaCustom(TP.getPenaliteIdPenalite().getDenomination(), TP.getPrix(), TP.getDateDebut(),TP.getDateFin()));
                }
            }
            for (TarifJour TJ:tarif.getTarifJour()){
                grilleJour.add(new JourCustom(TJ.getJourIdJour().getNbrJour(), TJ.getPrix(),TJ.getDateDebut(),TJ.getDateFin()));
            }
            return "/formEditTarif.xhtml?faces-redirect=true";
        }
        else {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().getFlash().setKeepMessages(true);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"ce tarif ne peut être modifié",null));
            init();
            return "/tableTarifs.xhtml?faces-redirect=true";
        }

    }

    public String newTarif()
    {

        //si tarif demom exist ou que dans jourcustom il ny as pas le 1 jour => erreur;
        boolean flagJ=false;
        boolean flagD1=false;
        boolean flagD2=false;
        boolean flagD3;
        boolean flagV1;
        SvcTarif service = new SvcTarif();

        if(tarif.getId()!=0){
            flagV1= (service.getById(tarif.getId()).getDenomination().equals(tarif.getDenomination())|| service.findOneTarifByDenom(tarif).size()==0);}
        else {
            flagV1=service.findOneTarifByDenom(tarif).size()==0;
        }

        for (JourCustom j: grilleJour){
            if (j.getNbrJours() == 1) {
                flagJ = true;
            }
            if (j.getDateDebut().getTime()<tarif.getDateDebut().getTime()){
                flagD1 = true;
            }
            if (j.getDateFin().getTime()<tarif.getDateDebut().getTime()){
                flagD2 = true;
            }
        }
        for (PenaCustom p:grillePena) {
            if (p.getDateDebut().getTime()<tarif.getDateDebut().getTime()){
                flagD1 = true;
            }
            if (p.getDateFin().getTime()<p.getDateDebut().getTime()){
                flagD2 = true;
            }
        }
        if(tarif.getId()!=0){
            flagD3=false;
        }
        else {flagD3=service.findOneTarifByDateDebut(tarif).size()!=0;}


        if (flagJ && !flagD1 && !flagD2 && flagV1 && !flagD3) {

            SvcTarifJour serviceTJ = new SvcTarifJour();
            SvcTarifPenalite serviceTP = new SvcTarifPenalite();
            SvcJours serviceJ = new SvcJours();
            SvcPenalite serviceP = new SvcPenalite();
            EntityTransaction transaction = service.getTransaction();
            serviceTJ.setEm(service.getEm());
            serviceTP.setEm(service.getEm());
            serviceJ.setEm(service.getEm());
            serviceP.setEm(service.getEm());

            Penalite penalites;
            Jour jours;

            transaction.begin();
            try {

                tarif = service.save(tarif);
                if(tarif.getId()!=0){
                    for (TarifJour tarifsJours:tarif.getTarifJour())
                    {
                        serviceTJ.delete(tarifsJours.getId());
                    }
                    for (TarifPenalite tp:tarif.getTarifPenalite())
                    {
                        serviceTP.delete(tp.getId());
                    }
                }
                for (PenaCustom p : grillePena) {
                    penalites = serviceP.addPena(p.getName());
                    serviceTP.save(serviceTP.createTarifPenalite(tarif, penalites, ((int)((p.getPrix()*100)+0.5)/100.0), p.getDateDebut(), p.getDateFin()));
                }
                for (JourCustom j : grilleJour) {


                    jours = serviceJ.addJours(j.getNbrJours());
                    log.debug("test1 "+j.getNbrJours());
                    log.debug("test2 "+jours.getNbrJour());
                    serviceTJ.save(serviceTJ.createTarifJour(tarif, jours, ((int)((j.getPrix()*100)+0.5)/100.0), j.getDateDebut(), j.getDateFin()));
                }
                transaction.commit();
                return "/tableTarifs.xhtml?faces-redirect=true";
            } finally {
                if (transaction.isActive()) {
                    transaction.rollback();
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.getExternalContext().getFlash().setKeepMessages(true);
                    fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "L'operation n'a pas reussie", null));
                }

                init();
                service.close();
            }
        }
        else {

            service.close();
            if(!flagJ){
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "la valeur tarifaire pour 1 jours est requise, veuillez l'ajouter", null));
                return "";
            }
            else if (flagD1){
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "les dates encodées dans les tableaux ne peuvent être antérieure à la date de début du tarif, veuillez corriger", null));
                return "";
            }
            else if (flagD2){
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "les dates de fin encodées dans les tableaux ne peuvent être antérieure à leur date de début correspondante, veuillez corriger", null));
                return "";
            }
            else if (flagD3){
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "la dates de debut du tarif doit être unique, veuillez corriger", null));
                return "";
            }
            else {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getExternalContext().getFlash().setKeepMessages(true);
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "ce nom de tarif existe déjà; veuillez en choisir un autre", null));
                return "";
            }

        }
    }

    public void addNewPenaRow() {
        grillePena.add(new PenaCustom());
    }

    public void addNewJourRow() {
        grilleJour.add(new JourCustom());
    }

    public void delPenaRow() {
        if (grillePena.size() >1)
        {
            grillePena.remove(grillePena.size()-1);
        }

    }

    public void delJourRow() {
        if (grilleJour.size() >1)
        {
            grilleJour.remove(grilleJour.size()-1);
        }
    }

    /*
     * Méthode qui permet via le service de retourner la liste de toutes les grilles tarifaires
     */

    public List<Tarif> getReadAll()
    {
        SvcTarif service = new SvcTarif();
        List<Tarif> listTarifs;
        listTarifs = service.findAllTarifs();

        service.close();
        return listTarifs;
    }

    public String flushTarifs() {
        init();
        return "/tableTarifs?faces-redirect=true";
    }

    //-------------------------------Getter & Setter--------------------------------------------

    public Tarif getTarif() {
        return tarif;
    }

    public void setTarif(Tarif tarif) {
        this.tarif = tarif;
    }

    public List<PenaCustom> getGrillePena() {
        return grillePena;
    }

    public void setGrillePena(List<PenaCustom> grillePena) {
        this.grillePena = grillePena;
    }

    public List<JourCustom> getGrilleJour() {
        return grilleJour;
    }

    public void setGrilleJour(List<JourCustom> grilleJour) {
        this.grilleJour = grilleJour;
    }
}