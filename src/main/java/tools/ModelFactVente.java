package tools;

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
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;

public class ModelFactVente implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ModelFactVente.class);

    // Fixed paths as requested
    private static final String BASE_DIR = "C:\\REVECouture\\";
    private static final String OUT_DIR = BASE_DIR + "facture\\";
    private static final String RES_DIR = BASE_DIR + "resources\\";
    private static final String HEADER_IMAGE = RES_DIR + "imgCouture.png";

    /**
     * Crée le PDF de facture de VENTE.
     *
     * @return chemin complet du PDF généré, ou "Erreur" en cas de souci.
     */
    public String creation(Facture fact, Magasin magasin) {
        if (fact == null || magasin == null) {
            log.warn("ModelFactVente.creation: facture ou magasin null");
            return "Erreur";
        }

        File out = new File(OUT_DIR);
        if (!out.exists() && !out.mkdirs()) {
            log.warn("Impossible de créer le répertoire: " + OUT_DIR);
            return "Erreur";
        }

        String numfacture = PdfUtils.safe(fact.getNumeroFacture());
        String outputPdf  = OUT_DIR + numfacture + ".pdf";
        String today = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());

        // --- Infos client (compact, 3 lignes max) ---
        Utilisateur cli = fact.getUtilisateurIdUtilisateur();
        String nomPrenom = (cli == null) ? "" : (PdfUtils.safe(cli.getNom()) + " " + PdfUtils.safe(cli.getPrenom())).trim();
        String adr1 = "", adr2 = "";
        if (cli != null && cli.getUtilisateurAdresse() != null) {
            for (UtilisateurAdresse ua : cli.getUtilisateurAdresse()) {
                if (ua.getActif() != null && ua.getActif()) {
                    String boite = (ua.getAdresseIdAdresse().getBoite() == null) ? "" : (" " + ua.getAdresseIdAdresse().getBoite());
                    adr1 = PdfUtils.safe(ua.getAdresseIdAdresse().getRue()) + " " + PdfUtils.safe(ua.getAdresseIdAdresse().getNumero()) + boite;
                    adr2 = PdfUtils.safe(String.valueOf(ua.getAdresseIdAdresse().getLocaliteIdLocalite().getCp())) + " "
                            + PdfUtils.safe(ua.getAdresseIdAdresse().getLocaliteIdLocalite().getVille());
                    break;
                }
            }
        }

        double total = (fact.getPrixTVAC() == null) ? 0d : fact.getPrixTVAC();

        // --- Constantes de mise en page ---
        final float START_X         = 80f;
        final float PRICE_COL_X     = 475f; // colonne des montants
        final float COL_LINE_X      = 450f; // règle verticale
        final float HEADER_Y_FIRST  = 600f; // en-tête (page 1)
        final float HEADER_Y_CONT   = 750f; // en-tête (pages suivantes)
        final float Y_START_FIRST   = 455f; // "Articles vendus :" (page 1)
        final float Y_START_CONT    = 680f; // "Articles vendus :" (pages suivantes)
        final float COL_LINE_TOP_OFF= 12f;  // règle démarre 12pt sous le libellé
        final float LINE_GAP        = 16f;
        final float GUARD_INTER     = 60f;  // garde basse pour pages intermédiaires
        final float FOOTER_TOP_Y    = 260f; // Y du séparateur pied de page
        final float FOOTER_RESERVE  = 24f;  // marge de confort au-dessus du pied
        final float ITEM_EXTRA_GAP  = 8f;

        try (PDDocument doc = new PDDocument()) {
            // Polices
            PDFont fontTitle = PDType1Font.HELVETICA_BOLD;
            PDFont fontBold  = PDType1Font.TIMES_BOLD;
            PDFont fontText  = PDType1Font.TIMES_ROMAN;

            // Métadonnées
            PDDocumentInformation info = doc.getDocumentInformation();
            info.setAuthor("REVECouture");
            info.setTitle("Facture " + numfacture);
            info.setSubject("Facturation vente");
            info.setCreationDate(java.util.Calendar.getInstance());

            // --- Prépare la première page ---
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            // Logo + coordonnées magasin
            try {
                PDImageXObject logo = PDImageXObject.createFromFile(HEADER_IMAGE, doc);
                cs.drawImage(logo, 35, 650, 120, 60);
            } catch (IOException e) {
                log.warn("Image d’en-tête introuvable: " + HEADER_IMAGE);
            }

            cs.beginText();
            cs.setFont(fontTitle, 24f);
            cs.setNonStrokingColor(java.awt.Color.BLACK);
            cs.setLeading(24.5f);
            cs.newLineAtOffset(198, 725);
            cs.showText(PdfUtils.safe(magasin.getNom()));
            cs.newLine();
            cs.setFont(fontTitle, 10f);
            cs.setLeading(14.5f);
            String l1 = PdfUtils.safe(magasin.getAdresseIdAdresse().getRue()) + " "
                    + PdfUtils.safe(magasin.getAdresseIdAdresse().getNumero()) + " "
                    + PdfUtils.safe(String.valueOf(magasin.getAdresseIdAdresse().getLocaliteIdLocalite().getCp())) + " "
                    + PdfUtils.safe(magasin.getAdresseIdAdresse().getLocaliteIdLocalite().getVille());
            cs.showText(l1);
            cs.newLine();
            cs.showText("TVA: BE0448.150.750 - Tel: 071 35 44 71");
            cs.endText();

            // Cadre client + libellés (wrap simple)
            PdfUtils.cadreCreation(cs, 350, 615, 200, 80);
            final float boxW = 200f, maxWidth = boxW - 16f;
            cs.beginText();
            cs.setLeading(12.5f);
            cs.setFont(fontText, 12f);
            cs.newLineAtOffset(360f, 600f);
            cs.showText("Client :");
            cs.newLine();
            cs.setFont(fontText, 10f);
            for (String src : new String[]{nomPrenom, adr1, adr2}) {
                if (src == null || src.isEmpty()) continue;
                for (String line : PdfUtils.wrap(src, fontText, 10f, maxWidth)) {
                    cs.showText(line);
                    cs.newLine();
                }
            }
            cs.endText();

            // En-tête facture (page 1)
            cs.beginText();
            cs.setFont(fontBold, 12f);
            cs.setLeading(14.5f);
            cs.newLineAtOffset(START_X, HEADER_Y_FIRST);
            cs.showText("Facture de vente n° : " + numfacture);
            cs.newLine();
            cs.showText("créée le " + today);
            cs.endText();

            // Libellés colonnes (page 1)
            cs.beginText();
            cs.setFont(fontBold, 12f);
            cs.newLineAtOffset(START_X, Y_START_FIRST);
            cs.showText("Articles vendus :");
            cs.endText();

            cs.beginText();
            cs.setFont(fontBold, 12f);
            cs.newLineAtOffset(PRICE_COL_X, Y_START_FIRST);
            cs.showText("Prix (€)");
            cs.endText();

            float currentYStart = Y_START_FIRST;
            float colLineTop = currentYStart - COL_LINE_TOP_OFF;

            float y = currentYStart - (LINE_GAP * 2);
            float leftWidth = PRICE_COL_X - START_X - 12f;

            // ============================================================
            // REGROUPEMENT : clé = libellé "nom — modèle — fabricant"
            // ============================================================
            java.util.LinkedHashMap<String, int[]> qtyByKey   = new java.util.LinkedHashMap<>();
            java.util.LinkedHashMap<String, Double> sumByKey  = new java.util.LinkedHashMap<>();
            java.util.LinkedHashMap<String, String> descByKey = new java.util.LinkedHashMap<>();
            java.util.List<FactureDetail> details = (fact.getFactureDetails() == null)
                    ? java.util.Collections.<FactureDetail>emptyList()
                    : new java.util.ArrayList<>(fact.getFactureDetails());

            for (FactureDetail fd : details) {
                entities.Article a = fd.getExemplaireArticleIdEA().getArticleIdArticle();
                String nom   = PdfUtils.safe(a.getNom());
                String model = PdfUtils.safe(a.getNumSerie());
                String fab   = (a.getFabricantIdFabricant() != null) ? PdfUtils.safe(a.getFabricantIdFabricant().getNom()) : "";
                String key   = PdfUtils.joinWithDash(nom, model, fab);

                double unit = (fd.getPrix() == null)
                        ? ((a.getPrix() == null) ? 0d : a.getPrix())
                        : fd.getPrix();

                // quantité
                int[] box = qtyByKey.get(key);
                if (box == null) {
                    qtyByKey.put(key, new int[]{1});
                    sumByKey.put(key, unit);
                    descByKey.put(key, key);
                } else {
                    box[0] += 1;
                    sumByKey.put(key, sumByKey.get(key) + unit);
                }
            }

            // Liste ordonnée des clés (dans l’ordre d’insertion)
            java.util.List<String> keys = new java.util.ArrayList<>(qtyByKey.keySet());

            // ============================================================
            // IMPRESSION des groupes (2 lignes par groupe + wraps)
            // ============================================================
            for (int idx = 0; idx < keys.size(); idx++) {
                String key = keys.get(idx);
                boolean lastItem = (idx == keys.size() - 1);

                int qty = qtyByKey.get(key)[0];
                double sum = sumByKey.get(key);
                double unit = (qty > 0) ? (sum / qty) : 0d;

                // Lignes de description (ligne 2, potentiellement multi-lignes)
                java.util.List<String> descLines = PdfUtils.wrap(descByKey.get(key), fontText, 11f, leftWidth);
                if (descLines.isEmpty()) descLines = java.util.Collections.singletonList("");

                // Hauteur nécessaire : 1 ligne (Quantité) + nbLignes(desc) + marge
                float needed = (1 + descLines.size()) * LINE_GAP + ITEM_EXTRA_GAP;

                float guard = lastItem ? (FOOTER_TOP_Y + FOOTER_RESERVE) : GUARD_INTER;
                if (y - needed < guard) {
                    // Clore la règle verticale de cette page
                    cs.setLineWidth(1);
                    cs.moveTo(COL_LINE_X, colLineTop);
                    cs.lineTo(COL_LINE_X, GUARD_INTER);
                    cs.closeAndStroke();

                    // Nouvelle page
                    cs.close();
                    page = new PDPage();
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);

                    // Petit en-tête
                    cs.beginText();
                    cs.setFont(fontBold, 12f);
                    cs.setLeading(14.5f);
                    cs.newLineAtOffset(START_X, HEADER_Y_CONT);
                    cs.showText("Facture de vente n° : " + numfacture + " (suite)");
                    cs.newLine();
                    cs.showText("créée le " + today);
                    cs.endText();

                    // Libellés colonnes
                    cs.beginText();
                    cs.setFont(fontBold, 12f);
                    cs.newLineAtOffset(START_X, Y_START_CONT);
                    cs.showText("Articles vendus ");
                    cs.endText();

                    cs.beginText();
                    cs.setFont(fontBold, 12f);
                    cs.newLineAtOffset(PRICE_COL_X, Y_START_CONT);
                    cs.showText("Prix (€)");
                    cs.endText();

                    currentYStart = Y_START_CONT;
                    colLineTop = currentYStart - COL_LINE_TOP_OFF;
                    y = currentYStart - (LINE_GAP * 2);
                }

                // --- Ligne 1 : "Quantité : N"  |  "N × prix_unitaire"
                cs.beginText();
                cs.setFont(fontText, 11f);
                cs.newLineAtOffset(START_X, y);
                cs.showText("Quantité : " + qty);
                cs.endText();

                String right1 = qty + " \u00D7 " + String.format(java.util.Locale.US, "%.2f", unit);
                cs.beginText();
                cs.setFont(fontText, 11f);
                cs.newLineAtOffset(PRICE_COL_X, y);
                cs.showText(right1);
                cs.endText();
                y -= LINE_GAP;

                // --- Ligne 2..n : description (wrap)  |  ligne 2 -> "= total"
                for (int i = 0; i < descLines.size(); i++) {
                    cs.beginText();
                    cs.setFont(fontText, 11f);
                    cs.newLineAtOffset(START_X, y);
                    cs.showText(descLines.get(i));
                    cs.endText();

                    if (i == 0) {
                        String right2 = "= " + String.format(java.util.Locale.US, "%.2f", sum);
                        cs.beginText();
                        cs.setFont(fontText, 11f);
                        cs.newLineAtOffset(PRICE_COL_X, y);
                        cs.showText(right2);
                        cs.endText();
                    }
                    y -= LINE_GAP;
                }

                y -= ITEM_EXTRA_GAP; // espace entre groupes
            }

            // --- Dernière page : règle + pied + totaux ---
            cs.setLineWidth(1);
            cs.moveTo(COL_LINE_X, colLineTop);
            cs.lineTo(COL_LINE_X, FOOTER_TOP_Y);
            cs.closeAndStroke();

            cs.setNonStrokingColor(java.awt.Color.BLACK);
            cs.addRect(57, FOOTER_TOP_Y, 500, 2);
            cs.fill();

            cs.beginText();
            cs.setFont(fontText, 12f);
            cs.setLeading(17.5f);
            cs.newLineAtOffset(363, 235);
            cs.showText("Total à payer");
            cs.endText();

            cs.beginText();
            cs.setFont(fontText, 12f);
            cs.setLeading(17.5f);
            cs.newLineAtOffset(PRICE_COL_X, 235);
            cs.showText(String.format(java.util.Locale.US, "%.2f €", total));
            cs.endText();

            cs.setNonStrokingColor(java.awt.Color.RED);
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

            cs.close();
            doc.save(outputPdf);
            return outputPdf;
        } catch (IOException ioe) {
            log.error("Erreur génération facture vente " + numfacture, ioe);
            return "Erreur";
        }
    }


    /* ---- local helpers (kept inside the class) ---- */

}
