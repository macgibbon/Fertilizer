package fertilizer;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class TablePdf {
    List<List<Content>> tableItems;
    String[] tableHeaders;

    public void write(File outFile) throws Exception, FileNotFoundException {
        // step 1: creation of a document-object
        Document document = new Document(PageSize.LETTER, 18, 18, 18, 18);

        int rows = tableItems.size();
        int cols = tableItems.get(0).size();
        // step 2:
        // we create a writer that listens to the document
        // and directs a PDF-stream to a file
        PdfWriter.getInstance(document, new FileOutputStream(outFile));
        HeaderFooter footer = new HeaderFooter(new Phrase("Fertilizer"), true);
        footer.setBorder(Rectangle.NO_BORDER);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.setFooter(footer);
        // step 3: we open the document
        document.open();
        // step 4: we create a table and add it to the document

      
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
        for (int i = 0; i < cols; i++) {
            cell = new PdfPCell(
                    new Phrase(tableHeaders[i], FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL)));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
            table.addCell(cell);
        }
        for (int i = 0; i < rows; i++) {
            List<Content> row = tableItems.get(i);
            for (int j = 0; j < cols; j++) {
               Content cellItem = row.get(j);
                PdfPCell tableCell = new PdfPCell(
                        new Phrase(cellItem.toString(), FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL)));
                if (cellItem.name != null)
                    tableCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                else
                    tableCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                table.addCell(tableCell);
            }
        }
        document.add(table);

        // step 5: we close the document
        document.close();
    }

    public TablePdf(List<List<Content>> tableItems, String[] headers) {
        super();
        this.tableItems = tableItems;
        this.tableHeaders = headers;
    }

}
