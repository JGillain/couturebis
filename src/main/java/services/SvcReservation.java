package services;

import entities.*;
import enumeration.ReservationStatutEnum;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcReservation extends Service<Reservation> implements Serializable {
    private static final Logger log = Logger.getLogger(SvcReservation.class);
    private static final long serialVersionUID = 1L;

    // --- Persist (same pattern you use everywhere)
    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            em.persist(reservation);
        } else if (reservation.getId() == 0) {
            em.persist(reservation);
        } else {
            reservation = em.merge(reservation);
        }
        return reservation;
    }

    // --- Finders
    public List<Reservation> findAll() {
        return finder.findByNamedQuery("Reservation.findAll", null);
    }

    public Reservation findNextByArticleMagasin(Article article, Magasin magasin) {
        Map<String,Object> p = new HashMap<>();
        p.put("article", article);
        p.put("magasin", magasin);
        List<Reservation> list = finder.findByNamedQuery("Reservation.findNextByArticleMagasin", p);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    // --- Mutations limited to Reservation

    /** Create -> file. */
    public Reservation create(Utilisateur user, Magasin store, Article sku) {
        Reservation r = new Reservation();
        r.setUtilisateurIdUtilisateur(user);
        r.setMagasinIdMagasin(store);
        r.setArticleIdArticle(sku);
        r.setStatut(ReservationStatutEnum.file);
        return save(r);
    }

    public Reservation markReady(Reservation r, ExemplaireArticle unit, Date dateReady, Date holdUntil, boolean mailDone) {
        r.setExemplaire(unit);
        r.setStatut(ReservationStatutEnum.pret);
        r.setDateReady(dateReady);
        r.setHoldUntil(holdUntil);
        r.setMailEnvoye(mailDone);
        return save(r);
    }

    public Reservation markValide(Reservation r) {
        r.setStatut(ReservationStatutEnum.valide);
        r.setActif(false);
        return save(r);
    }

    public Reservation markAnnule(Reservation r) {
        r.setStatut(ReservationStatutEnum.annule);
        r.setActif(false);
        return save(r);
    }

    public Reservation markExpire(Reservation r) {
        r.setStatut(ReservationStatutEnum.expire);
        r.setActif(false);
        return save(r);
    }
}

