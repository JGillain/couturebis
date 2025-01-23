package services;


import entities.FactureDetail;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcFactureDetail extends Service<FactureDetail> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcFactureDetail() {
        super();
    }

    // Méthode qui permet de sauver un detail de facture et de le mettre en DB
    @Override
    public FactureDetail save(FactureDetail factureDetail) {
        if (factureDetail.getId() == 0) {
            em.persist(factureDetail);
        } else {
            factureDetail = em.merge(factureDetail);
        }

        return factureDetail;
    }
}