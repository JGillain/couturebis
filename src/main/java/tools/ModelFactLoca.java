package tools;

import entities.*;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ModelFactLoca implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ModelFactLoca.class);
    private static final String RES_BASE     = "C:\\REVECouture\\resources\\";
    private static final String IMG_LOGO     = RES_BASE + "imgCouture.png";
    private static final String OUT_BASE     = "C:\\REVECouture\\facture\\";

    /*Creation de la facture en PDF*/
    public String creation(Facture fact, Magasin magasin) {
        // --- Préconditions ---
        if (fact == null || magasin == null) {
            log.warn("ModelFactLoca.creation: facture ou magasin null");
            return "Erreur";
        }

        // --- Dossier de sortie ---
        File outDir = new File(OUT_BASE);
        if (!outDir.exists() && !outDir.mkdirs()) {
            log.warn("Impossible de créer le répertoire: " + OUT_BASE);
            return "Erreur";
        }

        final String numfacture = PdfUtils.safe(fact.getNumeroFacture());
        final String outputPdf  = OUT_BASE + numfacture + ".pdf";
        final String today      = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        // --- Infos client (compact, 3 lignes max) ---
        Utilisateur cli = fact.getUtilisateurIdUtilisateur();
        String nomPrenom = (cli == null) ? "" : (PdfUtils.safe(cli.getNom()) + " " + PdfUtils.safe(cli.getPrenom())).trim();
        String adr1 = "", adr2 = "";
        if (cli != null && cli.getUtilisateurAdresse() != null) {
            for (UtilisateurAdresse ua : cli.getUtilisateurAdresse()) {
                if (ua.getActif() != null && ua.getActif()) {
                    String boite = (ua.getAdresseIdAdresse().getBoite() == null) ? "" : (" " + ua.getAdresseIdAdresse().getBoite());
                    adr1 = PdfUtils.safe(ua.getAdresseIdAdresse().getRue()) + " " +
                            PdfUtils.safe(ua.getAdresseIdAdresse().getNumero()) + boite;
                    adr2 = PdfUtils.safe(String.valueOf(ua.getAdresseIdAdresse().getLocaliteIdLocalite().getCp())) + " " +
                            PdfUtils.safe(ua.getAdresseIdAdresse().getLocaliteIdLocalite().getVille());
                    break;
                }
            }
        }

        double total = (fact.getPrixTVAC() == null) ? 0d : fact.getPrixTVAC();

        // --- Constantes de mise en page (alignées sur la vente) ---
        final float START_X          = 80f;
        final float PRICE_COL_X      = 475f; // position du texte "Prix" et des montants
        final float COL_LINE_X       = 450f; // position de la règle verticale
        final float HEADER_Y_FIRST   = 600f; // en-tête (page 1)
        final float HEADER_Y_CONT    = 750f; // en-tête (pages suivantes)
        final float Y_START_FIRST    = 455f; // "Articles loués :" (page 1)
        final float Y_START_CONT     = 680f; // "Articles loués :" (pages suivantes)
        final float COL_LINE_TOP_OFF = 12f;  // démarre la règle 12pt sous le libellé
        final float LINE_GAP         = 16f;
        final float GUARD_INTER      = 60f;  // garde basse pages intermédiaires
        final float FOOTER_TOP_Y     = 260f; // Y séparateur pied de page
        final float FOOTER_RESERVE   = 24f;  // marge de confort au-dessus du pied
        final float ITEM_EXTRA_GAP   = 8f;

        try (PDDocument doc = new PDDocument()) {
            // --- Polices & méta ---
            PDFont fontTitle = PDType1Font.HELVETICA_BOLD;
            PDFont fontBold  = PDType1Font.TIMES_BOLD;
            PDFont fontText  = PDType1Font.TIMES_ROMAN;

            PDDocumentInformation info = doc.getDocumentInformation();
            info.setAuthor("REVECouture");
            info.setTitle("Facture " + numfacture);
            info.setSubject("Facturation location");
            info.setCreationDate(Calendar.getInstance());

            // --- Première page ---
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            // Logo + coordonnées magasin
            try {
                PDImageXObject logo = PDImageXObject.createFromFile(IMG_LOGO, doc);
                cs.drawImage(logo, 35, 650, 120, 60);
            } catch (IOException e) {
                log.warn("Image d’en-tête introuvable: " + IMG_LOGO);
            }

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
                    PdfUtils.safe(String.valueOf(magasin.getAdresseIdAdresse().getLocaliteIdLocalite().getCp())) + " " +
                    PdfUtils.safe(magasin.getAdresseIdAdresse().getLocaliteIdLocalite().getVille());
            cs.showText(l1);
            cs.newLine();
            cs.showText("TVA: BE0448.150.750 - Tel: 071 35 44 71");
            cs.endText();

            // Cadre client + contenu (wrap simple)
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
            cs.showText("Facture de location n° : " + numfacture);
            cs.newLine();
            cs.showText("créée le " + today);
            cs.endText();

            // Libellés colonnes (page 1)
            cs.beginText();
            cs.setFont(fontBold, 12f);
            cs.newLineAtOffset(START_X, Y_START_FIRST);
            cs.showText("Articles loués :");
            cs.endText();

            cs.beginText();
            cs.setFont(fontBold, 12f);
            cs.newLineAtOffset(PRICE_COL_X, Y_START_FIRST);
            cs.showText("Prix (€)");
            cs.endText();

            float currentYStart = Y_START_FIRST;
            float colLineTop    = currentYStart - COL_LINE_TOP_OFF;

            float y         = currentYStart - (LINE_GAP * 2);
            float leftWidth = PRICE_COL_X - START_X - 12f;

            // Itérateur des détails
            Iterator<FactureDetail> it =
                    (fact.getFactureDetails() == null)
                            ? java.util.Collections.<FactureDetail>emptyList().iterator()
                            : fact.getFactureDetails().iterator();

            // --- Lignes avec pagination ---
            while (it.hasNext()) {
                FactureDetail fd = it.next();
                boolean lastItem = !it.hasNext();

                // --- Descriptif: "Nom — Modèle — Fabricant — CB: 13chiffres (+ durée)" ---
                // 1) description de base
                Article a = fd.getExemplaireArticleIdEA().getArticleIdArticle();
                String nom   = PdfUtils.safe(a.getNom());
                String model = PdfUtils.safe(a.getNumSerie());
                String fab   = (a.getFabricantIdFabricant() != null) ? PdfUtils.safe(a.getFabricantIdFabricant().getNom()) : "";
                String cb    = (fd.getExemplaireArticleIdEA().getCodeBarreIdCB() != null)
                        ? PdfUtils.safe(fd.getExemplaireArticleIdEA().getCodeBarreIdCB().getCodeBarre())
                        : "";
                String baseDesc = PdfUtils.joinWithDash(nom, model, fab, cb.isEmpty() ? null : ("CB: " + cb));

                // 2) durée en jours (arrondi simple entre DateDébut de la facture et DateFin de la ligne)
                LocalDateTime dStart = fact.getDateDebut().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime dEnd   = fd.getDateFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                long days = Math.max(ChronoUnit.DAYS.between(dStart, dEnd), 0L);
                String dureePart = "durée: " + days + " j";

                // 3) construit les lignes finales en essayant de mettre la durée sur la même ligne si ça tient
                java.util.List<String> itemLines = new java.util.ArrayList<>();

                String combo = baseDesc + " — " + dureePart;
                float comboW = fontText.getStringWidth(combo) / 1000f * 11f;

                if (comboW <= leftWidth) {
                    // tout tient sur une seule ligne → wrap standard
                    itemLines.addAll(PdfUtils.wrap(combo, fontText, 11f, leftWidth));
                } else {
                    // on garde la description, puis on met la durée sur une ligne séparée
                    itemLines.addAll(PdfUtils.wrap(baseDesc, fontText, 11f, leftWidth));
                    itemLines.add(dureePart);
                }

                if (itemLines.isEmpty()) itemLines = java.util.Collections.singletonList("");

                // 4) calcul hauteur nécessaire
                float needed = itemLines.size() * LINE_GAP + ITEM_EXTRA_GAP;
                // garde basse selon dernière ligne globale ou pas
                float guard  = lastItem ? (FOOTER_TOP_Y + FOOTER_RESERVE) : GUARD_INTER;

                // 5) pagination si nécessaire
                if (y - needed < guard) {
                    // règle verticale courant → garde intermédiaire
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
                    cs.showText("Facture de location n° : " + numfacture + " (suite)");
                    cs.newLine();
                    cs.showText("créée le " + today);
                    cs.endText();

                    // libellés colonnes
                    cs.beginText();
                    cs.setFont(fontBold, 12f);
                    cs.newLineAtOffset(START_X, Y_START_CONT);
                    cs.showText("Articles loués :");
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

                // 6) affichage lignes + prix (prix uniquement sur la 1ère ligne du bloc)
                for (int i = 0; i < itemLines.size(); i++) {
                    cs.beginText();
                    cs.setFont(fontText, 11f);
                    cs.newLineAtOffset(START_X, y);
                    cs.showText(itemLines.get(i));
                    cs.endText();

                    if (i == 0) {
                        cs.beginText();
                        cs.setFont(fontText, 11f);
                        cs.newLineAtOffset(PRICE_COL_X, y);
                        cs.showText(String.format(java.util.Locale.US, "%.2f", (fd.getPrix() == null ? 0d : fd.getPrix())));
                        cs.endText();
                    }
                    y -= LINE_GAP;
                }
                y -= ITEM_EXTRA_GAP;

            }

            // --- Dernière page : règle + pied + totaux ---
            cs.setLineWidth(1);
            cs.moveTo(COL_LINE_X, colLineTop);
            cs.lineTo(COL_LINE_X, FOOTER_TOP_Y);
            cs.closeAndStroke();

            cs.setNonStrokingColor(Color.BLACK);
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
            cs.showText("À défaut, nos factures sont réputées conformes.");
            cs.endText();

            cs.close();
            doc.save(outputPdf);
            return outputPdf;
        } catch (IOException ioe) {
            log.error("Erreur génération facture location " + numfacture, ioe);
            return "Erreur";
        }
    }



                    //-------------------------------------------------------------------------------------------------------

	public static Logger getLog() {
		return log;
	}

}