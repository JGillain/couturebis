package tools;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfUtils
{
	
	public static void cadreCreation(PDPageContentStream cs, int x, int y, int lg, int ht) throws IOException
	{
		// Création d'un encadrement
		//ligne supérieure
		cs.setLineWidth(1);
		cs.moveTo(x, y);
		cs.lineTo(x+lg, y);
		cs.closeAndStroke();
		
		// ligne inférieure
		cs.setLineWidth(1);
		cs.moveTo(x, y-ht);
		cs.lineTo(x + lg, y-ht);
		cs.closeAndStroke();
		
		// ligne verticale gauche
		cs.setLineWidth(1);
		cs.moveTo(x, y);
		cs.lineTo(x, y-ht);
		cs.closeAndStroke();
		
		// ligne verticale droite
		cs.setLineWidth(1);
		cs.moveTo(x+lg, y);
		cs.lineTo(x+lg, y -ht);
		cs.closeAndStroke();
		
	}


    static String safe(String s) {
        return (s == null) ? "" : s;
    }

    static List<String> wrap(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> out = new ArrayList<>();
        if (text == null) return out;
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String trial = (line.length() == 0) ? w : line + " " + w;
            float wpt = font.getStringWidth(trial) / 1000f * fontSize;
            if (wpt <= maxWidth) {
                line.setLength(0);
                line.append(trial);
            } else {
                if (line.length() > 0) out.add(line.toString());
                line.setLength(0);
                line.append(w);
            }
        }
        if (line.length() > 0) out.add(line.toString());
        if (out.isEmpty()) out.add("");
        return out;
    }

    static String joinWithDash(String... parts) {
        List<String> list = new ArrayList<>();
        for (String p : parts) if (p != null && !p.trim().isEmpty()) list.add(p.trim());
        return String.join(" — ", list);
    }
}
