package pdfTools;

import entities.*;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.faces.context.FacesContext;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

public class ModelFactLoca implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ModelFactLoca.class);
    private static final String RES_BASE     = "C:\\REVECouture\\resources\\";
    private static final String IMG_LOGO     = RES_BASE + "images\\imgCouture.png";
    private static final String FONT_BARCODE = RES_BASE + "fonts\\LibreBarcodeEAN13Text-Regular.ttf";
    private static final String OUT_BASE     = "C:\\REVECouture\\facture\\";

    private PDFont loadBarcodeFontFromFile(PDDocument doc) {
        try {
            File f = new File(FONT_BARCODE);
            if (!f.exists()) {
                log.warn("Barcode font not found: " + f.getAbsolutePath());
                return null;
            }
            return PDType0Font.load(doc, f);
        } catch (IOException e) {
            log.warn("Failed to load barcode font: " + e.getMessage());
            return null;
        }
    }

    private String requireEan13(String raw) {
        if (raw == null) return null;
        String s = raw.replaceAll("\\D", "");
        if (s.length() != 13) {
            log.warn("EAN-13 expected 13 digits, got: " + raw);
            return null;
        }
        return s;
    }

    /*Creation de la facture en PDF*/
    public String creation(Facture fact, Magasin mag) {
        SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
        List<String> price = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        String numfacture = fact.getNumeroFacture();
        String utilisateur = fact.getUtilisateurIdUtilisateur().getCodeBarreIdCB().getCodeBarre();
        String nompreClient = fact.getUtilisateurIdUtilisateur().getNom() + " " +
                fact.getUtilisateurIdUtilisateur().getPrenom();
        String adresse = "";
        String adresse2 = "";
        for (UtilisateurAdresse ua : fact.getUtilisateurIdUtilisateur().getUtilisateurAdresse()) {
            if (ua.getActif()) {
                adresse = ua.getAdresseIdAdresse().getRue() + " " + ua.getAdresseIdAdresse().getNumero() + " ";
                if (ua.getAdresseIdAdresse().getBoite() != null) {
                    adresse = adresse + ua.getAdresseIdAdresse().getBoite() + " ";
                }
                adresse2 = ua.getAdresseIdAdresse().getLocaliteIdLocalite().getCp() + " " +
                        ua.getAdresseIdAdresse().getLocaliteIdLocalite().getVille();
                break;
            }
        }
        String laDateDuJour = sf.format(new java.util.Date());
        Double PTVAC = fact.getPrixTVAC();

        File outDir = new File(OUT_BASE);
        if (!outDir.exists() && !outDir.mkdirs()) {
            log.warn("Unable to create output folder: " + OUT_BASE);
        }

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            PDDocumentInformation pdd = doc.getDocumentInformation();
            pdd.setAuthor("REVECouture");
            pdd.setTitle("Facture " + numfacture);
            pdd.setSubject("Facturation du client: " + utilisateur);
            pdd.setCreationDate(cal);

            PDImageXObject pdImage = null;
            try {
                File logo = new File(IMG_LOGO);
                if (logo.exists()) {
                    pdImage = PDImageXObject.createFromFile(logo.getAbsolutePath(), doc);
                } else {
                    log.warn("Logo not found: " + logo.getAbsolutePath());
                }
            } catch (IOException e) {
                log.warn("Logo load failed: " + e.getMessage());
            }

            PDFont barcodeFont = loadBarcodeFontFromFile(doc);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                if (pdImage != null) {
                    cs.drawImage(pdImage, 35, 650);
                }

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 24);
                cs.setNonStrokingColor(Color.BLACK);
                cs.setLeading(24.5f);
                cs.newLineAtOffset(198, 725);
                String entete1 = mag.getNom();
                String entete2 = mag.getAdresseIdAdresse().getRue() + " " + mag.getAdresseIdAdresse().getNumero() + " " +
                        mag.getAdresseIdAdresse().getLocaliteIdLocalite().getCp() + " " +
                        mag.getAdresseIdAdresse().getLocaliteIdLocalite().getVille();
                String entete3 = "TVA: BE0448.150.750 - Tel: 071 35 44 71";
                cs.showText(entete1);
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                cs.setLeading(14.5f);
                cs.showText(entete2);
                cs.newLine();
                cs.showText(entete3);
                cs.endText();

                Encadrement.creation(cs, 350, 615, 200, 80);

                cs.beginText();
                cs.setFont(PDType1Font.TIMES_ROMAN, 14);
                cs.setNonStrokingColor(Color.BLACK);
                cs.setLeading(14.5f);
                cs.newLineAtOffset(360, 600);
                cs.showText("Client :");
                cs.newLine();
                cs.showText(nompreClient);
                cs.newLine();
                cs.showText(adresse);
                cs.newLine();
                cs.showText(adresse2);
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.TIMES_BOLD, 12);
                cs.setLeading(14.5f);
                cs.newLineAtOffset(80, 600);
                cs.showText("Facture n\u00B0 : " + numfacture + " cr\u00E9\u00E9e le " + laDateDuJour);
                cs.endText();

                cs.setLineWidth(1);
                cs.moveTo(450, 475);
                cs.lineTo(450, 175);
                cs.closeAndStroke();

                cs.beginText();
                cs.newLineAtOffset(80, 455);
                cs.showText("Exemplaire lou\u00E9 :");
                cs.newLine();
                cs.newLine();

                for (FactureDetail fd : fact.getFactureDetails()) {
                    java.time.LocalDateTime dStart = fact.getDateDebut().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    java.time.LocalDateTime dEnd   = fd.getDateFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    long days = ChronoUnit.DAYS.between(dStart, dEnd);

                    String label = fd.getExemplaireArticleIdEA().getArticleIdArticle().getNom()
                            + " pour une dur\u00E9e de " + days + " jour" + (days > 1 ? "s" : "");
                    cs.showText(label);
                    cs.newLine();

                    price.add(String.valueOf(fd.getPrix()));
                    cs.newLine();
                }
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(475, 455);
                cs.showText("Prix");
                cs.newLine();
                cs.newLine();
                for (String s : price) {
                    cs.showText(s);
                    cs.newLine();
                    cs.newLine();
                }
                cs.endText();

                if (barcodeFont != null) {
                    float y = 420f;
                    for (FactureDetail fd : fact.getFactureDetails()) {
                        String cb = fd.getExemplaireArticleIdEA().getCodeBarreIdCB().getCodeBarre();
                        String ean = requireEan13(cb);
                        if (ean != null) {
                            cs.beginText();
                            cs.setFont(barcodeFont, 48);
                            cs.newLineAtOffset(80, y);
                            cs.showText(ean);
                            cs.endText();
                            y -= 60f; // next barcode lower
                            if (y < 180f) break; // avoid footer overlap
                        }
                    }
                }

                // separator
                cs.setNonStrokingColor(Color.BLACK);
                cs.addRect(57, 260, 500, 2);
                cs.fill();

                // totals
                cs.beginText();
                cs.setLeading(17.5f);
                cs.newLineAtOffset(363, 235);
                cs.showText("Total \u00E0 payer");
                cs.endText();

                cs.beginText();
                cs.setLeading(17.5f);
                cs.newLineAtOffset(475, 235);
                cs.showText(String.format("%5.02f Euros", PTVAC));
                cs.endText();

                // footer
                cs.setNonStrokingColor(Color.RED);
                cs.addRect(57, 100, 500, 2);
                cs.fill();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                cs.setLeading(7.25f);
                cs.newLineAtOffset(57, 90);
                cs.showText("Conditions g\u00E9n\u00E9rales");
                cs.setFont(PDType1Font.HELVETICA, 7);
                cs.newLine();
                cs.newLine();
                cs.showText("Toutes nos factures doivent \u00EAtre pay\u00E9es au moment de la cr\u00E9ation de la facture.");
                cs.newLine();
                cs.showText("Les r\u00E9clamations doivent \u00EAtre introduites par lettre recommand\u00E9e, sous peine de d\u00E9ch\u00E9ance,");
                cs.newLine();
                cs.showText("dans les 8 jours de la r\u00E9ception de la facture. \u00C0 d\u00E9faut, nos factures sont r\u00E9put\u00E9es conformes.");
                cs.endText();
            }

            String outPath = OUT_BASE + numfacture + ".pdf";
            doc.save(outPath);
            return outPath;

        } catch (IOException e) {
            log.debug(e.getMessage());
            return "Erreur";
        }
    }

	//-------------------------------------------------------------------------------------------------------

	public static Logger getLog() {
		return log;
	}

}