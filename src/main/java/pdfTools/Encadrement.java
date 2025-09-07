package pdfTools;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Encadrement 
{
	
	public static void creation(PDPageContentStream cs, int x, int y, int lg, int ht) throws IOException
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
	
	
}
