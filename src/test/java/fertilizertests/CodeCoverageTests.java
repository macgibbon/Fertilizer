package fertilizertests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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
        File currentDefaults = new File(model.appDir, "currentDefaults");
        FileOutputStream fos = new FileOutputStream(new File(currentDefaults,"spacesOnly.csv"));
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
        try {
            var version = model.loadProperty("notThere.properties", "something");
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
    void testCreateUserDir() {
        String realUserHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", "C:/Users/.test");   
            File testFolder = new File(System.getProperty("user.home"));
            if (testFolder.exists()) {
                Util.delDirTree(testFolder);
            }
            Model.reset();
            Model model = Model.getInstance();
          
        } catch (Throwable t) {           
        } finally {
            System.setProperty("user.home", realUserHome);
        }
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
    
    
    @Test
    void testCopy() {
        Throwable expected = null;
        try {
            Model.copy(Path.of("somewhere"), Path.of("somewhereelse"), Path.of("aFile"));
        } catch (Throwable t) {
            expected = t;
        }
        assertTrue(expected != null);
        
        Throwable expected2 = null;
        try {
            Model.deepCopy(Path.of("somewhere"), Path.of("somewhereelse"));
        } catch (Throwable t) {
            expected2 = t;
        }
        assertTrue(expected2 != null);
    }
    
}
