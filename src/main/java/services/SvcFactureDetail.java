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

    public FactureDetail newRent(ExemplaireArticle ea, Facture fa, Tarif t, int J, Timestamp d)
    {
        double prix=0.0;
        int nbrjours=0;
        int index=0;
        SvcTarifJour serviceTJ = new SvcTarifJour();
        SvcJours serviceJ = new SvcJours();
        List<Jour> jours = serviceJ.findByNbrJ(J);
        FactureDetail facturesDetail = new FactureDetail();
        facturesDetail.setExemplaireArticleIdEA(ea);
        facturesDetail.setFactureIdFacture(fa);
        List<TarifJour> tj = new ArrayList<>();
        for(Jour jours1 : jours)
        {
            tj.add(serviceTJ.findByJourByArticle(t,jours1,ea).get(0));
        }
        boolean flag=false;
        //try catch nécéssaire
        try {
            while (!flag) {
                prix = prix + tj.get(index).getPrix();
                nbrjours = nbrjours + tj.get(index).getJourIdJour().getNbrJour();
                if (nbrjours > J) {
                    prix = prix - tj.get(index).getPrix();
                    nbrjours = nbrjours - tj.get(index).getJourIdJour().getNbrJour();
                    index++;
                } else if (nbrjours == J) {
                    flag = true;
                }
            }
        }
        catch (IndexOutOfBoundsException ignored){}
        facturesDetail.setPrix(prix);
        facturesDetail.setDateFin(d);
        serviceTJ.close();
        serviceJ.close();
        return facturesDetail;
    }
    public FactureDetail newPena(ExemplaireArticle ea, Facture fa, Tarif t, Penalite p, Date dp, Timestamp df)
    {
        SvcTarifPenalite serviceTP = new SvcTarifPenalite();
        FactureDetail facturesDetail = new FactureDetail();
        facturesDetail.setExemplaireArticleIdEA(ea);
        facturesDetail.setFactureIdFacture(fa);
        facturesDetail.setPrix(serviceTP.findByPena(t,p,dp,ea.getArticleIdArticle()).get(0).getPrix());
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
        facturesDetail.setPrix(serviceTP.findByPena(t,p,dp, ea.getArticleIdArticle()).get(0).getPrix()*nbjour);
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