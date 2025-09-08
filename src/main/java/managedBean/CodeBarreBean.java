package managedBean;

import entities.CodeBarre;
import org.apache.log4j.Logger;
import services.SvcCodeBarre;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Named
@ApplicationScoped
public class CodeBarreBean implements Serializable {
    // Déclaration des variables globales
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(CodeBarreBean.class);

    @PostConstruct
    public void init()
    {
        log.info("CodeBarreBean init");
    }

    public void downloadBarcode() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        String fileName = ec.getRequestParameterMap().get("barcodeFile");
        if (fileName == null || fileName.isEmpty()) {
            bounceWithError(ec, fc, null);
            return;
        }

        Path file = Paths.get("C:\\REVECouture\\barcodes", fileName);

        try {
            if (!Files.exists(file)) {
                bounceWithError(ec, fc, fileName);
                return;
            }

            HttpServletResponse resp = (HttpServletResponse) ec.getResponse();
            resp.reset();
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName().toString() + "\"");
            resp.setHeader("Content-Length", String.valueOf(Files.size(file)));

            try (OutputStream out = resp.getOutputStream()) {
                Files.copy(file, out);
                out.flush();
            }

            fc.responseComplete();
        } catch (IOException e) {
            bounceWithError(ec, fc, fileName);
        }
    }

    private void bounceWithError(ExternalContext ec, FacesContext fc, String fileName) {
        try {
            String base = ec.getRequestContextPath() + "/formNewExArticle.xhtml";
            String qs = "?dlError=1";
            if (fileName != null) {
                qs += "&barcodeFile=" + URLEncoder.encode(fileName, "UTF-8");
            }
            ec.redirect(base + qs);
        } catch (IOException ignored) {
        } finally {
            fc.responseComplete();
        }
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
