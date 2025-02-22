package fertilizer;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class MixSheetPdf {
    private Model model;
    private SolutionModel solution;

    public void write(File outFile) throws Exception, FileNotFoundException {
        // step 1: creation of a document-object
        Document document = new Document(PageSize.LETTER, 18, 18, 18, 18);
        PdfWriter.getInstance(document, new FileOutputStream(outFile));
        document.open();

        List<String> reportHeaders = model.getReportHeaders();
        for (String string : reportHeaders) {
        	Paragraph paragraph = new Paragraph(string);
        	paragraph.setFont(FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL));
        	paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        	document.add(paragraph);
		}     
        
        String[] ingredientNames = solution.getIngredientMap().keySet()
                .stream()
                .filter(ing -> solution.getEnableMap().get(ing))
                .toArray(String[]::new);

       	double solutionTotalAmount = solution.getTotalAmount();
        double[] ingredientAmounts = solution.getSolutionIngredientAmounts();
        ArrayList<MixRow> mixrows = new ArrayList<>();
        for (int i = 0; i < ingredientAmounts.length; i++) {
        	mixrows.add(new MixRow(ingredientNames[i], ingredientAmounts[i]));
		}
      
        String[] tableHeaders = {"Material Name", "Percent", "Units/Batch", "Scale", "Actual" };
        MixRow[] sortedMixrows = mixrows.stream()
        		.filter(mr -> mr.amount() > 0.0 )
        		.sorted()
        		.toArray(MixRow[]::new);
        int cols = tableHeaders.length;
        float[] widths = new float[cols];
        widths[0] =4.0f;
        for (int i = 1; i < widths.length; i++) {
            widths[i] =1.0f;
        }
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100.0f);
        // table.setBorderWidth(1);
        // table.setBorderColor(new Color(0, 0, 255));
        PdfPCell cell = null;
       
        int rows = sortedMixrows.length;
        for (int i = 0; i < cols; i++) {
            cell = new PdfPCell(
                    new Phrase(tableHeaders[i], FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL)));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
            if (i == 0)
            	cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            else 
            	cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            table.addCell(cell);
        }

        double totalScale = 0;
        for (int i = 0; i < rows; i++) {
        	MixRow mr = sortedMixrows[i];

                PdfPCell tableCell = new PdfPCell(new Phrase(mr.name(), FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL)));
                tableCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                table.addCell(tableCell);
                
              
                double percent = 100.0* (mr.amount()/solutionTotalAmount) ;
				String percentasString = String.format("%.2f",percent);
                PdfPCell tableCell3 = new PdfPCell(new Phrase(percentasString, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL)));  
                tableCell3.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                table.addCell(tableCell3);
                
                double batchAmount = mr.amount()*model.batchWt.get()/solutionTotalAmount;
                totalScale += batchAmount;
                
                String amountAsString = String.format("%.2f",batchAmount);
                PdfPCell tableCell2 = new PdfPCell(new Phrase(amountAsString, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL)));  
                tableCell2.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                table.addCell(tableCell2);
                
                
            	String totalAsString = String.format("%.0f",totalScale);
                PdfPCell tableCell4= new PdfPCell(new Phrase(totalAsString, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL)));  
                tableCell4.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                table.addCell(tableCell4);
                

                PdfPCell tableCell5= new PdfPCell(new Phrase("", FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL)));  
                tableCell5.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                table.addCell(tableCell5);
               

        }
     // step 3: we open the document
      
        document.add(table);
        document.close();

           
    }

    public MixSheetPdf(Model model, SolutionModel solution) {
        super();
        this.model = model;
        this.solution = solution;
    }

}

record MixRow(String name, double amount) implements Comparable<MixRow> {

	@Override
	public int compareTo(MixRow o) {
		return Double.compare(o.amount, this.amount);
	}

}
