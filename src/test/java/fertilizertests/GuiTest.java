package fertilizertests;

import static fertilizertests.Util.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math4.legacy.optim.linear.Relationship;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;

import fertilizer.Celltype;
import fertilizer.Content;
import fertilizer.ContentTableCell;
import fertilizer.MainApp;
import fertilizer.MainController;
import fertilizer.MatrixBuilder;
import fertilizer.Model;
import fertilizer.SolutionModel;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Pair;

@ExtendWith(ApplicationExtension.class)
public class GuiTest extends MainApp {

    private File testFolder;

    @Start
    public void onStart(Stage primaryStage) throws Exception {
        testFolder = new File(System.getProperty("user.home"), ".fertilizer");
  
        delDirTree(testFolder);
        String key = MainController.LAST_USED_FOLDER;
        File registryFolder =  new File(Preferences.userNodeForPackage(Model.class).get(key, testFolder.toString()));
        delDirTree(registryFolder);
         
        super.start(primaryStage);
        // assert folder doesn't exist
    }

    @Stop
    void onStop() throws Exception {
        delay(4);
        super.stop();
    }

    @AfterAll
    public static void cleanup() {
        // Platform.exit();
    }

    @Test
    void testSaveAndLoad(FxRobot robot) throws Exception {
        delay(2);
        robot.clickOn("File");
        robot.clickOn("Save Formulation");
        delay(2);
        robot.push(KeyCode.F);
        robot.push(KeyCode.E);
        robot.push(KeyCode.R);
        robot.push(KeyCode.T);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.J);
        robot.push(KeyCode.S);
        robot.push(KeyCode.O);
        robot.push(KeyCode.N);
        robot.push(KeyCode.ENTER);
        // check that file exists and solution price matches

        // change cell 0 0 
        delay(3);
        robot.clickOn("File");
        robot.clickOn("Load Formulation");
        delay(2);
        robot.push(KeyCode.F);
        robot.push(KeyCode.E);
        robot.push(KeyCode.R);
        robot.push(KeyCode.T);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.J);
        robot.push(KeyCode.S);
        robot.push(KeyCode.O);
        robot.push(KeyCode.N);
        robot.push(KeyCode.ENTER);
        // assert cell 0 0 different

        delay(3);
        robot.clickOn("File");
        robot.clickOn("Save Formulation");
        robot.push(KeyCode.ESCAPE);

        delay(3);
        robot.clickOn("File");
        robot.clickOn("Load Formulation");
        robot.push(KeyCode.ESCAPE);
        
        delay(3);
        robot.clickOn("Action");
        robot.clickOn("Browse Data Files");
        delay(3);
        Util.pressGlobalExitKey();
        
        delay(3);
        robot.clickOn("Action");
        robot.clickOn("Browse Program Files");
        delay(3);
        Util.pressGlobalExitKey();
        
    }
    
    
    // testing print requires using Windows print to PDF virtual driver
    @Test
    void testPrint(FxRobot robot) throws Exception {
    	   File testPrintFile = new File(testFolder, "Test1.pdf");
           if (testPrintFile.exists())
               testPrintFile.delete();
           robot.clickOn("Action");
           robot.clickOn("Print feed mix");
           delay(2);
           java.awt.Robot awtRobot = new java.awt.Robot();
           awtRobot.keyPress(KeyEvent.VK_T);
           awtRobot.keyRelease(KeyEvent.VK_T);
           awtRobot.keyPress(KeyEvent.VK_E);
           awtRobot.keyRelease(KeyEvent.VK_E);
           awtRobot.keyPress(KeyEvent.VK_S);
           awtRobot.keyRelease(KeyEvent.VK_S);
           awtRobot.keyPress(KeyEvent.VK_T);
           awtRobot.keyRelease(KeyEvent.VK_T);
           awtRobot.keyPress(KeyEvent.VK_1);
           awtRobot.keyRelease(KeyEvent.VK_2);
           awtRobot.keyPress(KeyEvent.VK_ENTER);
           awtRobot.keyRelease(KeyEvent.VK_ENTER);
    	
    	
        File testPrintFile2 = new File(testFolder, "Test2.pdf");
        if (testPrintFile2.exists())
            testPrintFile2.delete();
        robot.clickOn("Action");
        robot.clickOn("Print least cost table");
        delay(2);       
        awtRobot.keyPress(KeyEvent.VK_T);
        awtRobot.keyRelease(KeyEvent.VK_T);
        awtRobot.keyPress(KeyEvent.VK_E);
        awtRobot.keyRelease(KeyEvent.VK_E); 
        awtRobot.keyPress(KeyEvent.VK_S);
        awtRobot.keyRelease(KeyEvent.VK_S);
        awtRobot.keyPress(KeyEvent.VK_T);
        awtRobot.keyRelease(KeyEvent.VK_T);
        awtRobot.keyPress(KeyEvent.VK_2);
        awtRobot.keyRelease(KeyEvent.VK_2);
        awtRobot.keyPress(KeyEvent.VK_ENTER);
        awtRobot.keyRelease(KeyEvent.VK_ENTER);
        
        
        
        
        SolutionModel solutionModel = (SolutionModel) reflectiveGetField(controller, "solution");
        String price = (String) reflectiveGetField(solutionModel, "solutionPrice");
        assert(price.equals("$32.41"));
        delay(3);
    }

    @Test
    void testAnalysisEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("0.46");
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.DIGIT4);
        robot.push(KeyCode.DIGIT5);
        robot.push(KeyCode.ENTER);
        // assert cell changed
    }
    
    @Test
    void testAnalysisEscape(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("18.00");
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.DIGIT4);
        robot.push(KeyCode.DIGIT5);
        robot.press(KeyCode.ESCAPE);
        robot.release(KeyCode.ESCAPE);
        // assert cell changed
    }

    @Test
    void testPriceEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("700.00");
        robot.push(KeyCode.DIGIT7);
        robot.push(KeyCode.DIGIT0);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.ENTER);
        delay(5);
        // solve assert solution changed
    }

    @Test
    void testRequirementEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("18.00");
        robot.push(KeyCode.DIGIT3);
        robot.push(KeyCode.DIGIT6);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.ENTER);
     // solve assert solution changed
    }
    
    @Test
    void testRelationshipEntry(FxRobot robot) {
        robot.clickOn("Solution");
        delay(1);
        robot.doubleClickOn("EQ");
        robot.push(KeyCode.SPACE);
        robot.push(KeyCode.DOWN);
        robot.push(KeyCode.ENTER);
 
        delay(1);
        robot.clickOn("GEQ");     // solve assert solution changed
    }

    
    @Test
    void testInfeasibleEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("18.00");
        robot.push(KeyCode.DIGIT9);
        robot.push(KeyCode.DIGIT6);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.ENTER);
        delay(1);
     // solve assert solution changed
    }
    
    // tests for code coverage
    @Test
    void testBadEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("0.24");
        robot.push(KeyCode.Z);
        robot.push(KeyCode.Z);
        robot.push(KeyCode.ENTER);
    }

 // tests for code coverage
    @Test
    void testEnables(FxRobot robot) {
        robot.doubleClickOn("true");
        robot.push(KeyCode.SPACE);
    }
 
    @Test
    void testsForGuiDependentCodeCoverage() throws Exception {
        testUncaughtExceptionHandler();
        testRedundantRows();
        testgetItemsEditingCornerCases();
        testEmptySelection();
        testRelationShipConvert();
        testContentTableCell();
    }

    
    void testUncaughtExceptionHandler() {
        Throwable error = null;
        try {
            Platform.runLater(() -> {
                ArrayList<ArrayList<String>> priceRows = model.readCsvfile("defaultPrices.csv");
                ArrayList<ArrayList<String>> ingredientRows = model.readCsvfile("defaultIngredients.csv");
                ArrayList<ArrayList<String>> requirementRows = model.readCsvfile("defaultRequirements.csv");
                priceRows.remove(0);
                requirementRows.remove(0);
                ingredientRows.remove(0);
                MatrixBuilder matrix = new MatrixBuilder(priceRows, requirementRows, ingredientRows);              
                SolutionModel model = new SolutionModel(matrix) {
                    @Override
                    public void calculateSolution() {
                        throw new RuntimeException("Deliberate exception to test exception handling");
                    }

                };
                model.calculateSolution();
            });
           
       } catch (Throwable t) {
            error = t;
            t.printStackTrace();
        }
        assertTrue(error == null);
    }
    

    void testRedundantRows()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String prices = """
                Urea,>=,640
                Ammonium Nitrate,>=,700
                Ammonium Nitrate,>=,790
                Diammonium Phosphate (DAP),>=,710
                """;

        ArrayList<ArrayList<String>> rows = Stream.of(prices.split("\n"))
                .filter(line -> line != null)
                .map(line -> line.split(","))
                .map(array -> new ArrayList<String>(Arrays.asList(array)))
                .collect(Collectors.toCollection((ArrayList::new)));
        MatrixBuilder matrix = new MatrixBuilder(rows, rows, rows);

        TableView<Content> solutionTable = (TableView<Content>) reflectiveGetField(controller, "solutiontable");
        TableColumn tc = solutionTable.getColumns().get(0);
        SolutionModel solutionModel = (SolutionModel) reflectiveGetField(controller, "solution");
        Method wtcMethod = reflectiveGetMethod(solutionModel, "writeThroughCache");
        Object[] args = new Object[] { 0,0,new Content() };
        InvocationTargetException expected = null;
        try { 
        wtcMethod.invoke(solutionModel, args);
        } catch (InvocationTargetException e) {
            expected = e;
        }
        assertTrue(expected.getTargetException().getMessage().equals("Unexpected value: whitespace"));
        
        solutionTable.getSelectionModel().select(1000, tc);
        // 
    }
    
    void testgetItemsEditingCornerCases() throws IOException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        SolutionModel model = (SolutionModel) reflectiveGetField(controller, "solution");
        List<List<Content>> items = model.getItems();
        int rows = items.size();
        int columns = items.get(0).size();
        Content testContent = new Content(1000.0, Celltype.ingredientAmount);
        int solutionAmountColumn = columns - 1;
        int priceColumn = columns - 2;
        int solutionAmountRow = rows - 1;
        int requirementRow = rows - 2;
        int relationshipRow = rows -3;
        int nameColumn = 0;

        var pairs = List.of(new Pair(0, nameColumn), // test name column
                new Pair(0, solutionAmountColumn), // test solution nutrient amount column
                new Pair(solutionAmountRow, 1), // test solution ingredient amount column
                new Pair(12, 10), 
                new Pair(11, 10), 
                new Pair(9, 13), 
                new Pair(9, 12), 
                new Pair(9, 11), 
                new Pair(10, 10),
      
                new Pair(4, 5), 
                new Pair(8, 12), 
                new Pair(12, 2), 
                new Pair(1, 13), 
                new Pair(0, 15), 
                new Pair(requirementRow, priceColumn)); // test price in requirement row
                
        // new Pair(0, columns - 2),
        // new Pair(rows - 2, 3),
        // new Pair(3, columns - 1),
        // new Pair(11, 7)

        for (Pair pair : pairs) {
            Integer x = (Integer) pair.getKey();
            Integer y = (Integer) pair.getValue();
            try {
                items.get(x).set(y, testContent);
            } catch (RuntimeException e) {
            }
        }
        
        Integer x = relationshipRow;
        Integer y =3;
        try {
            items.get(x).set(y, new Content(Relationship.GEQ));
        } catch (RuntimeException e) {
        }
        delay(4);
    }

    void testEmptySelection() {
        final TableView<List<Content>> solutionTable = (TableView<List<Content>>) reflectiveGetField(controller,
                "solutiontable");
        final TableColumn<List<Content>, ?> aTableColumn = solutionTable.getColumns().get(1);
        Platform.runLater(() -> {
            solutionTable.getSelectionModel().select(2, aTableColumn);
            solutionTable.getSelectionModel().clearSelection();
        });
    }
    
    void testRelationShipConvert() {
        var rc = ContentTableCell.relationshipConverter;
        Relationship[] values = Relationship.values();
        for (int i = 0; i < values.length; i++) {
            Relationship r1 = values[i];
            String s = rc.toString(r1);
            Relationship r2 = rc.fromString(s);
            assertTrue(r1.equals(r2));
        }        
    }
    
    void testContentTableCell() {
        ContentTableCell cell = new ContentTableCell(new AtomicBoolean());
         cell.updateSelected(false);
        cell.updateItem(new Content(), true);    
        cell.updateItem(new Content(), false);  
        cell.updateItem(null, true);    
        cell.updateItem(null, false);  
        assertFalse(cell.isSelected());
    }
   
}