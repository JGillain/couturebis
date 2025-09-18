package tools;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Génère des planches de codes-barres EAN-13 (A4, paginées).
 * Sortie: C:\REVECouture\barcodes\*.pdf
 */
public class ModelCodeBarre implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ModelCodeBarre.class);

    // --- Dossiers fixes (comme vos autres modèles) ---
    private static final String BASE_DIR = "C:\\REVECouture\\";
    private static final String OUT_DIR  = BASE_DIR + "barcodes\\";

    // --- Mise en page (A4 en points: 595 x 842) ---
    // Tout est en millimètres ici, puis converti en points.
    private static final float PAGE_MARGIN_MM = 10f;
    private static final float LABEL_W_MM     = 50f;   // largeur d’une étiquette
    private static final float LABEL_H_MM     = 30f;   // hauteur d’une étiquette
    private static final float COL_GAP_MM     = 4f;    // espace horizontal entre étiquettes
    private static final float ROW_GAP_MM     = 6f;    // espace vertical entre étiquettes

    // --- Taille du code-barres dans l’étiquette ---
    private static final float BAR_W_MM       = 38f;   // largeur visuelle des barres
    private static final float BAR_H_MM       = 18f;   // hauteur (1.8 cm par défaut)
    private static final float DIGITS_GAP_MM  = 2.5f;  // espace barres → chiffres

    // Génération ZXing en pixels (on génère en haute résolution, puis on met à l’échelle)
    private static final int   ZXING_DPI      = 300;

    // --- Police pour les chiffres imprimés sous le code ---
    private static final PDFont FONT_DIGITS   = PDType1Font.HELVETICA;
    private static final float  FONT_SIZE_PT  = 8.5f;

    /**
     * Génère une planche à partir d’une liste de codes EAN-13 (chaque string = 13 chiffres).
     * @return chemin complet du PDF, ou "Erreur"
     */
    public String createSheet(List<String> codes, String filenameBase) {
        if (codes == null || codes.isEmpty()) {
            log.warn("ModelCodeBarre.createSheet: liste vide");
            return "Erreur";
        }
        File out = new File(OUT_DIR);
        if (!out.exists() && !out.mkdirs()) {
            log.warn("Impossible de créer " + OUT_DIR);
            return "Erreur";
        }
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name  = (filenameBase == null || filenameBase.trim().isEmpty())
                ? ("barcodes_" + stamp + ".pdf")
                : (filenameBase + "_" + stamp + ".pdf");
        String outputPdf = OUT_DIR + name;

        // ---- layout (mm -> pt) ----
        final float margin     = mmToPt(PAGE_MARGIN_MM);
        final float labelW     = mmToPt(LABEL_W_MM);
        final float labelH     = mmToPt(LABEL_H_MM);
        final float colGap     = mmToPt(COL_GAP_MM);
        final float rowGap     = mmToPt(ROW_GAP_MM);
        final float barWpt     = mmToPt(BAR_W_MM);
        final float barHpt     = mmToPt(BAR_H_MM);
        final float digitsGap  = mmToPt(DIGITS_GAP_MM);

        final int   barWpx     = mmToPx(BAR_W_MM, ZXING_DPI);
        final int   barHpx     = mmToPx(BAR_H_MM, ZXING_DPI);

        final Map<EncodeHintType,Object> zxingHints = new EnumMap<>(EncodeHintType.class);
        zxingHints.put(EncodeHintType.MARGIN, 0);

        try (PDDocument doc = new PDDocument()) {
            PDDocumentInformation info = doc.getDocumentInformation();
            info.setAuthor("REVECouture");
            info.setTitle("Planche codes-barres");
            info.setSubject("Étiquettes EAN-13");
            info.setCreationDate(Calendar.getInstance());

            // A4 explicit
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageW = page.getMediaBox().getWidth();
            float pageH = page.getMediaBox().getHeight();

            // how many columns/rows fit between margins
            int cols = Math.max(1, (int) Math.floor((pageW - 2*margin + colGap) / (labelW + colGap)));
            int rows = Math.max(1, (int) Math.floor((pageH - 2*margin + rowGap) / (labelH + rowGap)));

            // horizontal centering; top anchored under the top margin
            float gridW = cols * labelW + (cols - 1) * colGap;
            float startX = (pageW - gridW) / 2f;
            float startYTop = pageH - margin;               // top edge of first row

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            int i = 0;
            for (String code : codes) {
                if (i > 0 && i % (cols * rows) == 0) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                }

                int idx = i % (cols * rows);
                int r = idx / cols;      // row index
                int c = idx % cols;      // column index

                float x = startX + c * (labelW + colGap);
                float yTop = startYTop - r * (labelH + rowGap);  // top-left Y of this label

                // (optional) light border around each label
                cs.setStrokingColor(Color.LIGHT_GRAY);
                cs.addRect(x, yTop - labelH, labelW, labelH);
                cs.stroke();
                cs.setStrokingColor(Color.BLACK);

                try {
                    BitMatrix m = new MultiFormatWriter()
                            .encode(code, BarcodeFormat.EAN_13, barWpx, barHpx, zxingHints);
                    BufferedImage img = MatrixToImageWriter.toBufferedImage(m);
                    PDImageXObject ximg = LosslessFactory.createFromImage(doc, img);

                    // center barcode inside label
                    float imgX = x + (labelW - barWpt) / 2f;
                    float imgY = (yTop - labelH) + (labelH - (barHpt + digitsGap + FONT_SIZE_PT + 2f)) / 2f;

                    cs.drawImage(ximg, imgX, imgY, barWpt, barHpt);

                    // human-readable digits centered under bars
                    cs.beginText();
                    cs.setFont(FONT_DIGITS, FONT_SIZE_PT);
                    float textWidth = stringWidth(FONT_DIGITS, FONT_SIZE_PT, code);
                    cs.newLineAtOffset(x + (labelW - textWidth) / 2f, imgY - digitsGap);
                    cs.showText(code);
                    cs.endText();
                } catch (WriterException we) {
                    log.warn("ZXing WriterException pour code=" + code, we);
                    // fallback: just the digits near the bottom
                    cs.beginText();
                    cs.setFont(FONT_DIGITS, FONT_SIZE_PT);
                    cs.newLineAtOffset(x + 6f, yTop - labelH + 6f);
                    cs.showText(code);
                    cs.endText();
                }

                i++;
            }

            cs.close();
            doc.save(outputPdf);
            return outputPdf;
        } catch (IOException ioe) {
            log.error("Erreur génération planche codes-barres", ioe);
            return "Erreur";
        }
    }

    /**
     *  methode pour répéter un même code N fois.
     */
    public String createSheet(String code, int quantity, String filenameBase) {
        if (quantity < 1) quantity = 1;
        List<String> list = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) list.add(code);
        return createSheet(list, filenameBase);
    }

    // --- utilitaires locaux ---
    private static float mmToPt(float mm) {
        return mm * 72f / 25.4f;
    }
    private static int mmToPx(float mm, int dpi) {
        return Math.max(1, Math.round(mm * dpi / 25.4f));
    }
    private static float stringWidth(PDFont font, float size, String s) throws IOException {
        if (s == null) return 0f;
        return font.getStringWidth(s) / 1000f * size;
    }
}