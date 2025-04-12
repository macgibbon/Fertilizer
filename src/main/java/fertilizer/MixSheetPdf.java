package fertilizer;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        for (String line : reportHeaders) {
            if (line.contains("{Date}"))
                line = line.replace("{Date}", LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
            if (line.contains("{NPK}"))
                line = line.replace("{NPK}", solution.getStandardDescription());
            if (line.contains("{Contact}"))
                line = line.replace("{Contact}", model.contact.get());
            if (line.length() == 0)
                line = " ";
            Paragraph paragraph = new Paragraph(line);
            paragraph.setFont(FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL));
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(paragraph);
        }

       
        int cols = Batch.tableHeaders.length;
        float[] widths = new float[cols];
        widths[0] = 4.0f;
        for (int i = 1; i < widths.length; i++) {
            widths[i] = 1.0f;
        }
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100.0f);
        // table.setBorderWidth(1);
        // table.setBorderColor(new Color(0, 0, 255));
      
        for (int i = 0; i < cols; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(Batch.tableHeaders[i], FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.BOLD)));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
            if (i == 0)
                cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            else
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            table.addCell(cell);
        }
        
        List<Batch> batches = Model.getInstance().batchTableList;
        int rows = batches.size();
        for (int i = 0; i < rows; i++) {
            Batch batchrow = batches.get(i);
            PdfPCell tableCell = new PdfPCell(new Phrase(batchrow.name(), FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.BOLD)));
            tableCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            table.addCell(tableCell);

            double percent = batchrow.percent();
            String percentasString = String.format("%.2f", percent);
            PdfPCell tableCell3 = new PdfPCell(new Phrase(percentasString, FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.NORMAL)));
            tableCell3.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            table.addCell(tableCell3);

            double batchAmount = batchrow.amount();
            String amountAsString = String.format("%.2f", batchAmount);
            PdfPCell tableCell2 = new PdfPCell(new Phrase(amountAsString, FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.NORMAL)));
            tableCell2.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            table.addCell(tableCell2);

            double rowScale = batchrow.scale();
            String totalAsString = String.format("%.0f", rowScale);
            PdfPCell tableCell4 = new PdfPCell(new Phrase(totalAsString, FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.BOLD)));
            tableCell4.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            table.addCell(tableCell4);

            String actualAsString = batchrow.actual();
            PdfPCell tableCell5 = new PdfPCell(new Phrase(actualAsString, FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.NORMAL)));
            tableCell5.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            table.addCell(tableCell5);

        }
        // step 3: we open the document
        document.add(table);

        document.add(new Paragraph(" "));
        Paragraph paragraph = new Paragraph(model.notes.get());
        paragraph.setFont(FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL));
        paragraph.setAlignment(Paragraph.ALIGN_LEFT);
        document.add(paragraph);
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
