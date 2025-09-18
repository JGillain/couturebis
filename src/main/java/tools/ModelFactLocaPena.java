package tools;

import entities.Article;
import entities.ExemplaireArticle;
import entities.Facture;
import entities.FactureDetail;
import entities.Magasin;
import entities.TarifPenalite;
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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class ModelFactLocaPena implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ModelFactLocaPena.class);

    // Chemins fixes (cohérents avec vos autres modèles)
    private static final String BASE_DIR     = "C:\\REVECouture\\";
    private static final String OUT_DIR      = BASE_DIR + "facture\\";
    private static final String RES_DIR      = BASE_DIR + "resources\\";
    private static final String HEADER_IMAGE = RES_DIR + "imgCouture.png";

    /**
     * Génère le PDF de FACTURE DE PÉNALITÉ (location).
     * – description de l’exemplaire (Article — Modèle — Fabricant — CB: 13chiffres)
     * – liste des pénalités à gauche, prix à droite
     * – code-barres uniquement en chiffres (pas de rendu sur le code barres)
     */
    public String creation(Facture fact, List<TarifPenalite> tp, FactureDetail retard, Magasin magasin) {
        if (fact == null || magasin == null) {
            log.warn("ModelFactLocaPena.creation: facture ou magasin null");
            return "erreur";
        }

        // Dossier de sortie
        File out = new File(OUT_DIR);
        if (!out.exists() && !out.mkdirs()) {
            log.warn("Impossible de créer le répertoire: " + OUT_DIR);
            return "erreur";
        }

        final String numfacture = PdfUtils.safe(fact.getNumeroFacture());
        final String outputPdf  = OUT_DIR + numfacture + ".pdf";
        final String today      = new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());

        // --- Infos client (compact, 3 lignes max comme les autres) ---
        Utilisateur cli = fact.getUtilisateurIdUtilisateur();
        String nomPrenom = (cli == null) ? "" : (PdfUtils.safe(cli.getNom()) + " " + PdfUtils.safe(cli.getPrenom())).trim();
        String adr1 = "", adr2 = "";
        if (cli != null && cli.getUtilisateurAdresse() != null) {
            for (UtilisateurAdresse ua : cli.getUtilisateurAdresse()) {
                if (ua.getActif() != null && ua.getActif()) {
                    String boite = (ua.getAdresseIdAdresse().getBoite() == null) ? "" : (" " + ua.getAdresseIdAdresse().getBoite());
                    adr1 = PdfUtils.safe(ua.getAdresseIdAdresse().getRue()) + " " + PdfUtils.safe(ua.getAdresseIdAdresse().getNumero()) + boite;
                    adr2 = String.valueOf(ua.getAdresseIdAdresse().getLocaliteIdLocalite().getCp()) + " " +
                            PdfUtils.safe(ua.getAdresseIdAdresse().getLocaliteIdLocalite().getVille());
                    break;
                }
            }
        }

        // Total (déjà calculé côté métier)
        double total = (fact.getPrixTVAC() == null) ? 0d : fact.getPrixTVAC();

        // --- Constantes de mise en page (identiques à ModelfactVente où pertinent) ---
        final float START_X         = 80f;
        final float PRICE_COL_X     = 475f; // position des montants et du libellé “Prix”
        final float COL_LINE_X      = 450f; // règle verticale
        final float HEADER_Y_FIRST  = 600f; // tête page 1
        final float HEADER_Y_CONT   = 750f; // tête pages suivantes
        final float Y_START_FIRST   = 455f; // y “bloc liste” page 1
        final float Y_START_CONT    = 680f; // y “bloc liste” pages suivantes
        final float COL_LINE_TOP_OFF= 12f;  // départ de la règle sous le libellé
        final float LINE_GAP        = 16f;
        final float GUARD_INTER     = 60f;  // garde basse pages intermédiaires
        final float FOOTER_TOP_Y    = 260f; // y du séparateur pied
        final float FOOTER_RESERVE  = 24f;  // marge au-dessus du pied
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
            info.setSubject("Facturation pénalités");
            info.setCreationDate(Calendar.getInstance());

            // --- Première page ---
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

            // En-tête magasin
            cs.beginText();
            cs.setFont(fontTitle, 24f);
            cs.setNonStrokingColor(Color.BLACK);
            cs.setLeading(24.5f);
            cs.newLineAtOffset(198, 725);
            cs.showText(PdfUtils.safe(magasin.getNom()));
            cs.newLine();
            cs.setFont(fontTitle, 10f);
            cs.setLeading(14.5f);
            String l1 = PdfUtils.safe(magasin.getAdresseIdAdresse().getRue()) + " " +
                    PdfUtils.safe(magasin.getAdresseIdAdresse().getNumero()) + " " +
                    magasin.getAdresseIdAdresse().getLocaliteIdLocalite().getCp() + " " +
                    PdfUtils.safe(magasin.getAdresseIdAdresse().getLocaliteIdLocalite().getVille());
            cs.showText(l1);
            cs.newLine();
            cs.showText("TVA: BE0448.150.750 - Tel: 071 35 44 71");
            cs.endText();

            // Cadre client
            PdfUtils.cadreCreation(cs, 350, 615, 200, 80);

            // Bloc client (wrap simple)
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
            cs.showText("Facture de pénalité n° : " + numfacture);
            cs.newLine();
            cs.showText("créée le " + today);
            cs.endText();

            // Libellés colonnes (page 1)
            cs.beginText();
            cs.setFont(fontBold, 12f);
            cs.newLineAtOffset(START_X, Y_START_FIRST);
            cs.showText("Pénalités :");
            cs.endText();

            cs.beginText();
            cs.setFont(fontBold, 12f);
            cs.newLineAtOffset(PRICE_COL_X, Y_START_FIRST);
            cs.showText("Prix (€)");
            cs.endText();

            float currentYStart = Y_START_FIRST;
            float colLineTop    = currentYStart - COL_LINE_TOP_OFF;

            // Position d’écriture de la liste
            float y = currentYStart - (LINE_GAP * 2);
            float leftWidth = PRICE_COL_X - START_X - 12f;

            // --- Contexte: exemplaire concerné (Article — Modèle — Fabricant — CB: ...) ---
            ExemplaireArticle ex = null;
            if (retard != null && retard.getExemplaireArticleIdEA() != null) {
                ex = retard.getExemplaireArticleIdEA();
            } else if (fact.getFactureDetails() != null && !fact.getFactureDetails().isEmpty()) {
                ex = fact.getFactureDetails().iterator().next().getExemplaireArticleIdEA();
            }

            if (ex != null) {
                Article a = ex.getArticleIdArticle();
                String nom   = (a != null) ? PdfUtils.safe(a.getNom()) : "";
                String model = (a != null) ? PdfUtils.safe(a.getNumSerie()) : "";
                String fab   = (a != null && a.getFabricantIdFabricant() != null) ? PdfUtils.safe(a.getFabricantIdFabricant().getNom()) : "";
                String cb    = (ex.getCodeBarreIdCB() != null) ? PdfUtils.safe(ex.getCodeBarreIdCB().getCodeBarre()) : "";

                // “Objet : …” sur une ou plusieurs lignes
                String objet = "Objet : " + PdfUtils.joinWithDash(nom, model, fab, cb.isEmpty() ? null : ("CB: " + cb));
                for (String line : PdfUtils.wrap(objet, fontText, 11f, leftWidth)) {
                    cs.beginText();
                    cs.setFont(fontText, 11f);
                    cs.newLineAtOffset(START_X, y);
                    cs.showText(line);
                    cs.endText();
                    y -= LINE_GAP;
                }
                y -= ITEM_EXTRA_GAP; // petit espace avant la liste de pénalités
            }

            // --- Impression des pénalités avec pagination ---
            Iterator<TarifPenalite> it = (tp == null) ? java.util.Collections.<TarifPenalite>emptyList().iterator() : tp.iterator();

            while (it.hasNext()) {
                TarifPenalite t = it.next();
                boolean lastItem = !it.hasNext(); // dernière ligne globale ?

                String lib = (t.getPenaliteIdPenalite() != null)
                        ? PdfUtils.safe(t.getPenaliteIdPenalite().getDenomination())
                        : "Pénalité";

                // prix: “Retard” → utiliser FactureDetail retard si fourni, sinon prix du TP
                double prix = 0d;
                if ("Retard".equalsIgnoreCase(lib)) {
                    if (retard != null && retard.getPrix() != null) {
                        prix = retard.getPrix();
                    } else if (t.getPrix() != null) {
                        prix = t.getPrix();
                    }
                } else {
                    prix = (t.getPrix() == null) ? 0d : t.getPrix();
                }

                // wrap du libellé
                List<String> lines = PdfUtils.wrap(lib, fontText, 11f, leftWidth);
                if (lines.isEmpty()) lines = java.util.Collections.singletonList("");

                float needed = lines.size() * LINE_GAP + ITEM_EXTRA_GAP;
                float guard  = lastItem ? (FOOTER_TOP_Y + FOOTER_RESERVE) : GUARD_INTER;

                // pas assez de place → nouvelle page
                if (y - needed < guard) {
                    // règle verticale jusqu’à la garde intermédiaire
                    cs.setLineWidth(1);
                    cs.moveTo(COL_LINE_X, colLineTop);
                    cs.lineTo(COL_LINE_X, GUARD_INTER);
                    cs.closeAndStroke();

                    cs.close();
                    page = new PDPage();
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);

                    // entête minimal (pages suivantes)
                    cs.beginText();
                    cs.setFont(fontBold, 12f);
                    cs.setLeading(14.5f);
                    cs.newLineAtOffset(START_X, HEADER_Y_CONT);
                    cs.showText("Facture de pénalité n° : " + numfacture + " (suite)");
                    cs.newLine();
                    cs.showText("créée le " + today);
                    cs.endText();

                    // libellés colonnes
                    cs.beginText();
                    cs.setFont(fontBold, 12f);
                    cs.newLineAtOffset(START_X, Y_START_CONT);
                    cs.showText("Pénalités :");
                    cs.endText();

                    cs.beginText();
                    cs.setFont(fontBold, 12f);
                    cs.newLineAtOffset(PRICE_COL_X, Y_START_CONT);
                    cs.showText("Prix (€)");
                    cs.endText();

                    currentYStart = Y_START_CONT;
                    colLineTop    = currentYStart - COL_LINE_TOP_OFF;
                    y             = currentYStart - (LINE_GAP * 2);
                }

                // affichage des lignes de la pénalité + prix sur la 1re ligne
                for (int iLine = 0; iLine < lines.size(); iLine++) {
                    cs.beginText();
                    cs.setFont(fontText, 11f);
                    cs.newLineAtOffset(START_X, y);
                    cs.showText(lines.get(iLine));
                    cs.endText();

                    if (iLine == 0) {
                        cs.beginText();
                        cs.setFont(fontText, 11f);
                        cs.newLineAtOffset(PRICE_COL_X, y);
                        cs.showText(String.format(java.util.Locale.US, "%.2f", prix));
                        cs.endText();
                    }
                    y -= LINE_GAP;
                }
                y -= ITEM_EXTRA_GAP;
            }

            // --- Dernière page : règle verticale jusqu’au pied + pied & totaux ---
            cs.setLineWidth(1);
            cs.moveTo(COL_LINE_X, colLineTop);
            cs.lineTo(COL_LINE_X, FOOTER_TOP_Y);
            cs.closeAndStroke();

            // séparateur horizontal pied
            cs.setNonStrokingColor(Color.BLACK);
            cs.addRect(57, FOOTER_TOP_Y, 500, 2);
            cs.fill();

            // total
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

            // conditions
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

            cs.close();
            doc.save(outputPdf);
            return outputPdf;
        } catch (IOException ioe) {
            log.error("Erreur génération facture pénalité " + numfacture, ioe);
            return "erreur";
        }
    }
    //-------------------------------------------------------------------------------------------------------

    public static Logger getLog() {
        return log;
    }
}


