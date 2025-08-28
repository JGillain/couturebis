package managedBean;

import entities.CodeBarre;
import org.apache.log4j.Logger;
import services.SvcCodeBarre;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class CodeBarreBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private CodeBarre CB;
    private static final Logger log = Logger.getLogger(CodeBarreBean.class);

    @PostConstruct
    public void init()
    {
        log.info("CodeBarreBean init");
        CB = new CodeBarre();
    }

    public List<String> createCB(boolean client, int count)
    {
        log.info("CodeBarreBean createCB");

        long start = client ? 900_000_000_000L : 901_000_000_000L;
        long end   = client ? 900_999_999_999L : 999_999_999_999L;
        List<String> list = new ArrayList<String>();
        SvcCodeBarre svc = new SvcCodeBarre();
        List<CodeBarre> results = svc.findBarcodeInRange(start+"0", end+"9");

        Long currentMax = null;
        if (!results.isEmpty()) {
            CodeBarre barcode = results.get(0);
            String codeBarreFull = barcode.getCodeBarre();
            if (codeBarreFull.length() != 13) {
                throw new IllegalStateException("longueur cb invalide : " + codeBarreFull);
            }
            try {
                String codeWithoutCheckDigit = codeBarreFull.substring(0, 12);
                currentMax = Long.parseLong(codeWithoutCheckDigit);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("format cb invalide : " + barcode.getCodeBarre(), e);
            }
        }


        long next = (currentMax == null) ? start : currentMax + 1;

        for(int i=0;i<count;i=i+1) {

            if (next > end) {
                throw new IllegalStateException("limite de code barre atteinte");
            }

            String base = String.format("%012d", next);
            int checkDigit = calculateCheckDigit(base);
            list.add(base+checkDigit);
            next = next+1L;
        }
        init();
        return list;
    }
    public int calculateCheckDigit(String data) {
        int sum = 0;
        for (int i = 0; i < data.length(); i++) {
            int digit = Character.getNumericValue(data.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int mod = sum % 10;
        return (mod == 0) ? 0 : 10 - mod;
    }
}
