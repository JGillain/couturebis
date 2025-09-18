package managedBean;

import entities.*;
import enumeration.ReservationStatutEnum;
import services.SvcExemplaireArticle;
import services.SvcReservation;
import services.SvcUtilisateur;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Named
@SessionScoped
public class ReservationBean implements Serializable {
    @Inject
    private MagasinBean magasinSession;
    private List<Reservation> rows;

    private String numMembre;
    private Article article;
    private Reservation selected;
    private Date dateFin;

    @PostConstruct
    public void init() {
        refresh();
        numMembre = "";
        article = new  Article();
        selected = new   Reservation();
    }

    public void refresh() {
        SvcReservation svc = new SvcReservation();
        try { rows = svc.findAllActif(); }
        finally { svc.close(); }
    }

    /** Create a new reservation (file) using barcode -> Utilisateur + Magasin from session. */
    public String newReservation() {
        SvcReservation  service = new SvcReservation();
        SvcUtilisateur serviceU = new SvcUtilisateur();
        try {
            serviceU.setEm(service.getEm());

            EntityTransaction tx = service.getTransaction();
            tx.begin();
            try {
                List<Utilisateur> found = serviceU.getByNumMembre(numMembre);
                if (found == null || found.isEmpty()) {
                    tx.rollback();
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Aucun client pour ce code-barres", null));
                    return null;
                }
                Utilisateur user = found.get(0);
                Magasin store = magasinSession.getMagasin();

                service.create(user, store, article);
                tx.commit();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Réservation créée (file)", null));
                return "tableReservations.xhtml?faces-redirect=true";
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur création réservation", null));
                return null;
            }
        } finally {
            serviceU.close();
            service.close();
            refresh();
        }
    }

    public void cancelReservation(Reservation r) {
        SvcReservation       svcR  = new SvcReservation();
        SvcExemplaireArticle svcEA = new SvcExemplaireArticle();
        svcEA.setEm(svcR.getEm());

        EntityTransaction tx = svcR.getTransaction();
        tx.begin();
        try {
            // Libérer l’exemplaire mis de côté (si présent)
            ExemplaireArticle ea = r.getExemplaire();
            if (ea != null) {
                ea.setReserve(false);
                svcEA.save(ea);
            }

            // Marquer la réservation ANNULE et inactive
            svcR.markAnnule(r); // save/merge côté service
            tx.commit();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Réservation annulée", null));
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur à l'annulation", null));
        } finally {
            svcEA.close(); svcR.close();
            refresh();
        }
    }


    /** Expire (staff). Same as cancel, different terminal status. */
    public void expireReservation(Reservation r) {
        if (r.getStatut() != ReservationStatutEnum.pret) return;

        SvcReservation       svcR  = new SvcReservation();
        SvcExemplaireArticle svcEA = new SvcExemplaireArticle();
        svcEA.setEm(svcR.getEm()); // même EM

        EntityTransaction tx = svcR.getTransaction();
        tx.begin();
        try {
            // Libérer l’exemplaire mis de côté (si présent)
            ExemplaireArticle ea = r.getExemplaire();
            if (ea != null) {
                ea.setReserve(false);
                svcEA.save(ea);
            }

            // Marquer la réservation EXPIRE et inactive
            svcR.markExpire(r);
            tx.commit();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Réservation expirée", null));
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur à l'expiration", null));
        } finally {
            svcEA.close(); svcR.close();
            refresh();
        }
    }

    public List<Reservation> getRows() {
        return rows;
    }

    public void setRows(List<Reservation> rows) {
        this.rows = rows;
    }

    public String getNumMembre() {
        return numMembre;
    }

    public void setNumMembre(String numMembre) {
        this.numMembre = numMembre;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public Reservation getSelected() {
        return selected;
    }

    public void setSelected(Reservation reservation) {
        this.selected = reservation;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }
}
