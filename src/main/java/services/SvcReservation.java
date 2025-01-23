package services;

import entities.Reservation;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcReservation extends Service<Reservation> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcReservation() {
        super();
    }

    // Méthode qui permet de sauver une reservation et de la mettre en DB
    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == 0) {
            em.persist(reservation);
        } else {
            reservation = em.merge(reservation);
        }

        return reservation;
    }
}