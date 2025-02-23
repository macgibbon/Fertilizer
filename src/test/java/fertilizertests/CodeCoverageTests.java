package fertilizertests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import fertilizer.Celltype;
import fertilizer.Content;
import fertilizer.MainApp;
import fertilizer.Model;

class CodeCoverageTests {

 
    @Test
    void testBadDefaultFile() throws IOException {
        Throwable expected = null;
        Model model = Model.getInstance();
        try {           
            ArrayList<ArrayList<String>> priceRows = model.readCsvfile("notDefaultPrice.csv");
        } catch (Throwable t) {
            expected = t;
        }
        assertTrue(expected != null);
        FileOutputStream fos = new FileOutputStream("spacesOnly.csv");
        try {
            fos.write("    \n\n".getBytes());
        } finally {
            fos.close();
        }
        expected = null;
        try {
            ArrayList<ArrayList<String>> errorRows = model.readCsvfile("spacesOnly.csv");
        } catch (Throwable t) {
            expected = t;
        }
        assertTrue(expected == null);
        expected = null;
        try {
        var lines = model.readTextFile("notDefaultPrice.txt");
        } catch (Throwable t) {
            expected = t;
        }
        assertTrue(expected != null);
    }

    @Test
    void testGetCause() {
        Throwable t = new RuntimeException(new NullPointerException());
        Throwable nestedException = MainApp.getCause(t);
        assertTrue(nestedException instanceof NullPointerException);
    }
    
    @Test
    void testContentUpdates() {
        Content nameContent = new Content("Urea", Celltype.name);
        Content.update(nameContent, "Urea mix");

        Content enableContent = new Content(Boolean.FALSE);
        Content.update(enableContent, "Urea mix");

        Content emptyContent = new Content();
        try {
            Content.update(emptyContent, "Urea mix");
        } catch (RuntimeException re) {
            assertTrue(re.getMessage().equals("Unexpected Entry"));
        }
    }
    
}
