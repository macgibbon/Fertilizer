package fertilizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

public class MixSheetPdf {
    private Model model;
    private SolutionModel solution;

    public void write(File outFile) throws Exception, FileNotFoundException {
        // step 1: creation of a document-object
        Document document = new Document(PageSize.LETTER, 18, 18, 18, 18);
        List<String> ingredientNames = solution.getIngredientMap().keySet()
                .stream()
                .filter(ing -> solution.getEnableMap().get(ing))
                .toList();
/*        List<Double> ingredientAmounts = solution.getIngredientMap().keySet()
                .stream()
                .filter(ing -> solution.getEnableMap().get(ing))
                .map(ing -> solution.get)
                .toList(); */
        PdfWriter.getInstance(document, new FileOutputStream(outFile));
        HeaderFooter footer = new HeaderFooter(new Phrase("Mix Sheet"), true);
        footer.setBorder(Rectangle.NO_BORDER);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.setFooter(footer);
        // step 3: we open the document
        document.open();
        // step 4: we create a table and add it to the document
        document.add(new Phrase("Mix Sheet"));
        document.add(new Phrase(LocalDate.now().toString()));
        document.add(new Phrase());

        // step 5: we close the document
        document.close();
    }

    public MixSheetPdf(Model model, SolutionModel solution) {
        super();
        this.model = model;
        this.solution = solution;
    }

}
