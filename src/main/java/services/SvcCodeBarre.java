package services;


import entities.CodeBarre;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvcCodeBarre extends Service<CodeBarre> implements Serializable {
    //Déclaration des variables
    private static final Logger log = Logger.getLogger(SvcArticle.class);
    private static final long serialVersionUID = 1L;
    Map<String, Object> params = new HashMap<String, Object>();

    public SvcCodeBarre() {
        super();
    }

    // Méthode qui permet de sauver un code barre et de la mettre en DB
    @Override
    public CodeBarre save(CodeBarre cb) {
        if (cb.getId() == null) {
            em.persist(cb);
        } else if (cb.getId() == 0) {
            em.persist(cb);
        } else {
            cb = em.merge(cb);
        }

        return cb;
    }

    public List<CodeBarre> findBarcodeInRange(String start, String end) {
            Map<String, Object> param = new HashMap<>();
            param.put("start", start);
            param.put("end", end);
            return finder.findByNamedQuery("CodeBarre.findAllInRange", param);
    }

}