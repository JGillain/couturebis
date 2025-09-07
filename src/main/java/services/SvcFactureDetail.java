package services;


import entities.*;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

public class SvcFactureDetail extends Service<FactureDetail> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcFactureDetail.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcFactureDetail() {
        super();
        log.info("SvcFactureDetail called");
    }

    // Méthode qui permet de sauver un detail de facture et de le mettre en DB
    @Override
    public FactureDetail save(FactureDetail factureDetail) {
        if (factureDetail.getId() == null) {
            em.persist(factureDetail);
        } else if (factureDetail.getId() == 0) {
            em.persist(factureDetail);
        } else {
            factureDetail = em.merge(factureDetail);
        }

        return factureDetail;
    }

    public FactureDetail newRent(ExemplaireArticle ea, Facture fa, Tarif t, int J, Timestamp dateFin) {
        // --- initialisation variables ---
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());;
        SvcTarifJour serviceTJ = new SvcTarifJour();
        List<TarifJour> tiers;
        FactureDetail fd  = new FactureDetail();
        int remaining = J;
        double prix= 0.0;
        int chunk;

        try {
            tiers = serviceTJ.findAllForArticleOnDate(t, ea.getArticleIdArticle(), J, today);
            if (tiers == null || tiers.isEmpty()) {
                throw new IllegalStateException("Aucun tarif actif pour l’article "
                        + ea.getArticleIdArticle().getNom() + " le " + today);
            }

            for (TarifJour row : tiers) {
                chunk = row.getJourIdJour().getNbrJour();
                if (chunk <= 0) continue;
                while (remaining >= chunk) {
                    prix += row.getPrix();
                    remaining -= chunk;
                }
                if (remaining == 0) break;
            }

            if (remaining != 0) {
                boolean hasDay1 = tiers.stream().anyMatch(tj -> tj.getJourIdJour().getNbrJour() == 1);
                String hint = hasDay1 ? "" : " (ajoutez une ligne 1 jour au tarif)";
                throw new IllegalStateException("Impossible de couvrir exactement " + J
                        + " jours pour l’article \"" + ea.getArticleIdArticle().getNom()
                        + "\" le " + today + hint);
            }

            fd.setExemplaireArticleIdEA(ea);
            fd.setFactureIdFacture(fa);
            fd.setPrix(prix);
            fd.setDateFin(dateFin);
            return fd;
        } finally {
            serviceTJ.close();
        }
    }

    public FactureDetail newPena(ExemplaireArticle ea, Facture fa, Tarif t, Penalite p, Date dp, Timestamp df)
    {
        SvcTarifPenalite serviceTP = new SvcTarifPenalite();
        FactureDetail facturesDetail = new FactureDetail();
        facturesDetail.setExemplaireArticleIdEA(ea);
        facturesDetail.setFactureIdFacture(fa);
        facturesDetail.setPrix(serviceTP.findByPenalitesByArticle(t,p,dp,ea.getArticleIdArticle()).get(0).getPrix());
        facturesDetail.setDateFin(df);
        serviceTP.close();
        return facturesDetail;
    }
    public FactureDetail newPenaretard(ExemplaireArticle ea, Facture fa, Tarif t, Penalite p,double nbjour, Date dp, Timestamp df)
    {
        SvcTarifPenalite serviceTP = new SvcTarifPenalite();
        FactureDetail facturesDetail = new FactureDetail();
        facturesDetail.setExemplaireArticleIdEA(ea);
        facturesDetail.setFactureIdFacture(fa);
        facturesDetail.setPrix(serviceTP.findByPenalitesByArticle(t,p,dp, ea.getArticleIdArticle()).get(0).getPrix()*nbjour);
        facturesDetail.setDateFin(df);
        facturesDetail.setDateRetour(df);
        serviceTP.close();
        return facturesDetail;
    }

    public List<FactureDetail> findAllFactureDetail()
    {
        return finder.findByNamedQuery("FactureDetail.findAll",null);
    }

}