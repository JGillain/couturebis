package services;


import entities.Facture;
import enumeration.FactureTypeEnum;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcFacture extends Service<Facture> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcFacture.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcFacture() {
        super();
        log.info("SvcFacture called");
    }

    // Méthode qui permet de sauver une facture et de la mettre en DB
    @Override
    public Facture save(Facture facture) {
        if (facture.getId() == null) {
            em.persist(facture);
        } else if (facture.getId() == 0) {
            em.persist(facture);
        } else {
            facture = em.merge(facture);
        }
        return facture;
    }

    public List<Facture> findAllFacture() {
        return finder.findByNamedQuery("Facture.findAll", null);
    }
    public List<Facture> findAllFactureLocation() {
        Map<String, Object> param = new HashMap<>();
        param.put("type", FactureTypeEnum.Location);
        return finder.findByNamedQuery("Facture.findByType", param);
    }
    public List<Facture> findAllFactureVente() {
        Map<String, Object> param = new HashMap<>();
        param.put("type", FactureTypeEnum.Vente);
        return finder.findByNamedQuery("Facture.findByType", param);
    }
    public List<Facture> findAllFactureDesc() {
        return finder.findByNamedQuery("Facture.findLastId", null);
    }

}