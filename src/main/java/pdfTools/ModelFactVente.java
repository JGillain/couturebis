package pdfTools;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import entities.Facture;
import entities.FactureDetail;
import entities.Magasin;
import entities.Utilisateur;
import entities.UtilisateurAdresse;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ModelFactVente implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ModelFactVente.class);

    // Fixed paths as requested
    private static final String BASE_DIR = "C:\\REVECouture\\";
    private static final String OUT_DIR  = BASE_DIR + "facture\\";
    private static final String RES_DIR  = BASE_DIR + "resources\\";
    private static final String HEADER_IMAGE = RES_DIR + "imgCouture.png";
    private static final String BARCODE_FONT = RES_DIR + "LibreBarcodeEAN13Text-Regular.ttf";

    /**
     * Crée le PDF de facture de VENTE.
     * @return chemin complet du PDF généré, ou "Erreur" en cas de souci.
     */
    public String creation(Facture fact, Magasin magasin) {
        // Préconditions minimales
        if (fact == null || magasin == null) {
            log.warn("ModelFactVente.creation: facture ou magasin null");
            return "Erreur";
        }

        // Dossier de sortie
        File out = new File(OUT_DIR);
        if (!out.exists() && !out.mkdirs()) {
            log.warn("Impossible de créer le répertoire: " + OUT_DIR);
            return "Erreur";
        }

        String numfacture = safe(fact.getNumeroFacture());
        String outputPdf  = OUT_DIR + numfacture + ".pdf";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String today = sdf.format(new Date());

        // Infos client (si présentes)
        Utilisateur cli = fact.getUtilisateurIdUtilisateur();
        String nomPrenom = (cli == null) ? "" : (safe(cli.getNom()) + " " + safe(cli.getPrenom())).trim();
        String adr1 = "";
        String adr2 = "";
        if (cli != null && cli.getUtilisateurAdresse() != null) {
            for (UtilisateurAdresse ua : cli.getUtilisateurAdresse()) {
                if (ua.getActif() != null && ua.getActif()) {
                    String boite = (ua.getAdresseIdAdresse().getBoite() == null) ? "" : (" " + ua.getAdresseIdAdresse().getBoite());
                    adr1 = safe(ua.getAdresseIdAdresse().getRue()) + " " + safe(ua.getAdresseIdAdresse().getNumero()) + boite;
                    adr2 = safe(String.valueOf(ua.getAdresseIdAdresse().getLocaliteIdLocalite().getCp())) + " "
                            + safe(ua.getAdresseIdAdresse().getLocaliteIdLocalite().getVille());
                    break;
                }
            }
        }

        // Totaux
        double total = (fact.getPrixTVAC() == null) ? 0d : fact.getPrixTVAC();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            // Meta
            PDDocumentInformation info = doc.getDocumentInformation();
            info.setAuthor("REVECouture");
            info.setTitle("Facture " + numfacture);
            info.setSubject("Facturation vente");
            info.setCreationDate(Calendar.getInstance());

            // Fonts
            PDFont fontTitle   = PDType1Font.HELVETICA_BOLD;
            PDFont fontBold    = PDType1Font.TIMES_BOLD;
            PDFont fontText    = PDType1Font.TIMES_ROMAN;
            PDFont fontMono    = PDType1Font.COURIER_BOLD_OBLIQUE;

            // Barcode font (full EAN-13 w/ digits)
            PDFont fontBarcode;
            try (FileInputStream fis = new FileInputStream(BARCODE_FONT)) {
                fontBarcode = PDType0Font.load(doc, fis, true);
            }

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Header image
                try {
                    org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject logo =
                            org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromFile(HEADER_IMAGE, doc);
                    cs.drawImage(logo, 35, 650, 120, 60);
                } catch (IOException e) {
                    log.warn("Image d’en-tête introuvable: " + HEADER_IMAGE);
                }

                // En-tête magasin
                cs.beginText();
                cs.setFont(fontTitle, 24f);
                cs.setNonStrokingColor(Color.BLACK);
                cs.setLeading(24.5f);
                cs.newLineAtOffset(198, 725);
                cs.showText(safe(magasin.getNom()));
                cs.newLine();
                cs.setFont(fontTitle, 10f);
                cs.setLeading(14.5f);
                String l1 = safe(magasin.getAdresseIdAdresse().getRue()) + " "
                        + safe(magasin.getAdresseIdAdresse().getNumero()) + " "
                        + safe(String.valueOf(magasin.getAdresseIdAdresse().getLocaliteIdLocalite().getCp())) + " "
                        + safe(magasin.getAdresseIdAdresse().getLocaliteIdLocalite().getVille());
                cs.showText(l1);
                cs.newLine();
                cs.showText("TVA: BE0448.150.750 - Tel: 071 35 44 71");
                cs.endText();

                // --- Cadre client ---
                Encadrement.creation(cs, 350, 615, 200, 80);

                // --- Bloc client avec retour à la ligne auto ---
                final float boxX = 350f, boxY = 615f, boxW = 200f, boxH = 80f;
                // point de départ du texte (petite marge intérieure)
                final float textX = 360f;
                final float textY = 600f;
                final float labelFontSize   = 12f;
                final float contentFontSize = 10f;
                final float leading         = 12.5f;
                // largeur max utilisable à l’intérieur du cadre
                final float maxWidth = boxW - 16f; // ~8 pt de marge de chaque côté

                // lignes sources
                String label = "Client :";

                cs.beginText();
                cs.setNonStrokingColor(Color.BLACK);
                cs.setLeading(leading);

                // positionner le curseur
                cs.newLineAtOffset(textX, textY);

                // 1) libellé
                cs.setFont(fontText, labelFontSize);
                cs.showText(label);
                cs.newLine();

                // 2) contenu (nom + adresses) avec wrap dans la largeur disponible
                cs.setFont(fontText, contentFontSize);

                // on limite à 3 lignes de contenu pour rester dans 80 pt de hauteur
                int linesLeft = 3;
                for (String src : new String[]{nomPrenom, adr1, adr2}) {
                    if (linesLeft <= 0 || src == null || src.isEmpty()) continue;

                    // wrap naïf par mots selon largeur max
                    String[] words = src.split("\\s+");
                    StringBuilder line = new StringBuilder();
                    for (String w : words) {
                        String trial = line.length() == 0 ? w : line + " " + w;
                        float trialW = 0f;
                        trialW = fontText.getStringWidth(trial) / 1000f * contentFontSize;

                        if (trialW <= maxWidth) {
                            line.setLength(0);
                            line.append(trial);
                        } else {
                            // écrire la ligne pleine
                            cs.showText(line.toString());
                            cs.newLine();
                            if (--linesLeft == 0) break;
                            // démarrer une nouvelle ligne avec le mot courant
                            line.setLength(0);
                            line.append(w);
                        }
                    }
                    if (linesLeft > 0 && line.length() > 0) {
                        cs.showText(line.toString());
                        cs.newLine();
                        linesLeft--;
                    }
                }
                cs.endText();

                // Entête facture
                cs.beginText();
                cs.setFont(fontBold, 12f);
                cs.setLeading(14.5f);
                cs.newLineAtOffset(80, 600);
                cs.showText("Facture de vente n° : " + numfacture);
                cs.newLine();
                cs.showText("créée le " + today);
                cs.endText();

                // Colonne de séparation
                cs.setLineWidth(1);
                cs.moveTo(450, 475);
                cs.lineTo(450, 175);
                cs.closeAndStroke();

                // Corps: lignes d’articles
                float startX = 80f;
                float y = 455f;
                float lineGap = 16f;

                cs.beginText();
                cs.setFont(fontBold, 12f);
                cs.newLineAtOffset(startX, y);
                cs.showText("Articles vendus :");
                cs.endText();
                y -= (lineGap * 2);

                List<String> rightCol = new ArrayList<>();
                if (fact.getFactureDetails() != null) {
                    for (FactureDetail fd : fact.getFactureDetails()) {
                        String nom = safe(fd.getExemplaireArticleIdEA().getArticleIdArticle().getNom());
                        String cb  = safe(fd.getExemplaireArticleIdEA().getArticleIdArticle().getCodeBarreIdCB().getCodeBarre());
                        String qty = "Qté: 1";
                        String prix = String.format("%.2f", fd.getPrix());

                        cs.beginText();
                        cs.setFont(fontText, 11f);
                        cs.newLineAtOffset(startX, y);
                        cs.showText(nom);
                        cs.endText(); y -= lineGap;

                        // --- BARCODE (ZXing -> image) ---
                        try {
                            int bw =  Math.round(95 * 0.90f), bh = 80; // pixels
                            Map<EncodeHintType,Object> hints = new EnumMap<>(EncodeHintType.class);
                            hints.put(EncodeHintType.MARGIN, 0);

                            BitMatrix m = new MultiFormatWriter()
                                    .encode(cb, BarcodeFormat.EAN_13, bw, bh, hints);

                            BufferedImage img =
                                    MatrixToImageWriter.toBufferedImage(m);

                            PDImageXObject ximg =
                                    LosslessFactory.createFromImage(doc, img);

                            cs.drawImage(ximg, startX, y - bh + 8, bw, bh);

                            // human-readable digits below the bars
                            cs.beginText();
                            cs.setFont(fontText, 9f);
                            cs.newLineAtOffset(startX + 8, y - bh - 6);
                            cs.showText(cb);
                            cs.endText();

                            y -= (bh + 24); // advance after barcode
                        } catch (WriterException we) {
                            log.warn("EAN-13 generation failed for CB=" + cb, we);
                            // Fallback: at least print the digits
                            cs.beginText();
                            cs.setFont(fontText, 9f);
                            cs.newLineAtOffset(startX, y);
                            cs.showText(cb);
                            cs.endText();
                            y -= lineGap;
                        }

                        cs.beginText();
                        cs.setFont(fontMono, 9f);
                        cs.newLineAtOffset(startX, y);
                        cs.showText(qty);
                        cs.endText(); y -= (lineGap + 2);

                        rightCol.add(prix);

                        // safety break per page (simple)
                        if (y < 200) break;
                    }
                }

                // Colonne des prix alignée à droite
                float priceColX = 475f;
                float yp = 455f - (lineGap * 2); // aligne sur 1ère ligne d’article
                cs.beginText();
                cs.setFont(fontBold, 12f);
                cs.newLineAtOffset(priceColX, 455f);
                cs.showText("Prix");
                cs.endText();

                for (String p : rightCol) {
                    cs.beginText();
                    cs.setFont(fontText, 11f);
                    cs.newLineAtOffset(priceColX, yp);
                    cs.showText(p);
                    cs.endText();
                    yp -= (lineGap * 3 + 8);
                    if (yp < 200) break;
                }

                // Trait horizontal
                cs.setNonStrokingColor(Color.BLACK);
                cs.addRect(57, 260, 500, 2);
                cs.fill();

                // Total
                cs.beginText();
                cs.setFont(fontText, 12f);
                cs.setLeading(17.5f);
                cs.newLineAtOffset(363, 235);
                cs.showText("Total à payer");
                cs.endText();

                cs.beginText();
                cs.setFont(fontText, 12f);
                cs.setLeading(17.5f);
                cs.newLineAtOffset(475, 235);
                cs.showText(String.format("%.2f Euros", total));
                cs.endText();

                // Pied de page
                cs.setNonStrokingColor(Color.RED);
                cs.addRect(57, 100, 500, 2);
                cs.fill();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.setLeading(7.25f);
                cs.newLineAtOffset(57, 90);
                cs.showText("Conditions générales");
                cs.newLine();
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 7f);
                cs.showText("Toutes nos factures doivent être payées au moment de la création de la facture.");
                cs.newLine();
                cs.showText("Les réclamations doivent être introduites par lettre recommandée dans les 8 jours de la réception.");
                cs.newLine();
                cs.showText("A défaut, nos factures sont réputées conformes.");
                cs.endText();
            }

            doc.save(outputPdf);
            return outputPdf;
        } catch (IOException ioe) {
            log.error("Erreur génération facture vente " + numfacture, ioe);
            return "Erreur";
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}
