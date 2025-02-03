package fertilizertests;


import static fertilizertests.Util.delay;
import static fertilizertests.Util.pressGlobalExitKey;
import static fertilizertests.Util.reflectiveGet;
import static fertilizertests.Util.reflectiveSet;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import fertilizer.Content;
import fertilizer.MainApp;
import fertilizer.MatrixBuilder;
import fertilizer.Model;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Pair;

@ExtendWith(ApplicationExtension.class)
public class TestGui extends MainApp {    
  
    @Start
    void onStart(Stage primaryStage) throws Exception {
        super.start(primaryStage);
    }

    @AfterAll
    public static void cleanup() {
        // Platform.exit();
    }
  
    @Test
    @Order(94)
    void solve(FxRobot robot) throws Exception {
        delay(5);
        robot.clickOn("Action");
        robot.clickOn("Calculate least cost solution");
        delay(2);
        robot.clickOn("Solution");
        robot.doubleClickOn("0.46");
        delay(2);
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.DIGIT4);
        robot.push(KeyCode.DIGIT5);
        robot.push(KeyCode.ENTER);
        
        robot.doubleClickOn("700.00");
        delay(2);
        robot.push(KeyCode.Z);
        robot.push(KeyCode.Z);
        robot.push(KeyCode.Z);
        robot.push(KeyCode.ENTER);
        delay(10);
    }

    @Test
    @Order(99)
    void doEdits(FxRobot robot) {
        try {
            robot.clickOn("Edit");
            robot.clickOn("Edit Default Prices");
            delay(5);
            pressGlobalExitKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            robot.clickOn("Edit");
            robot.clickOn("Edit Default Requirements");
            delay(5);
            pressGlobalExitKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            robot.clickOn("Edit");
            robot.clickOn("Edit Default Ingredient Analysis");
            delay(5);
            pressGlobalExitKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        robot.closeCurrentWindow();
    }
    
    @Test
    @Order(98)
    void testUncaughtExceptionHandler(FxRobot robot) {
        Throwable error = null;
        try {
            
            ArrayList<ArrayList<String>> priceRows = controller.readCsvfile(Path.of("defaultPrices.csv"));
            ArrayList<ArrayList<String>> ingredientRows = controller.readCsvfile(Path.of("defaultIngredients.csv"));
            ArrayList<ArrayList<String>> requirementRows = controller.readCsvfile(Path.of("defaultRequirements.csv"));
            MatrixBuilder matrix = new MatrixBuilder(priceRows, requirementRows, ingredientRows);
            Model model = new Model(matrix.getNutrientMap(), matrix.getIngredientMap(), matrix.getAnalysisMatrixs()) {

                @Override
                public PointValuePair calculateSolution() {
                    throw new RuntimeException("Deliberate exception to test exception handling");                }
                
            };
            reflectiveSet(model, controller, "model");
            robot.clickOn("Action");
            robot.clickOn("Calculate least cost solution");
            delay(5);
       //     PointValuePair solution =  model.calculateSolution();
       //     System.out.println(solution.getValue());
        } catch (Throwable t) {
            error = t;
            t.printStackTrace();
        }
        assertTrue(error == null); 
    }  
    
    
    
    @Test
    @Order(97)
    void testRedundantRows() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String prices =   """
                Urea,640
                Ammonium Nitrate,700
                Ammonium Nitrate,790
                Diammonium Phosphate (DAP),710
                """;

        ArrayList<ArrayList<String>> rows = Stream.of(prices.split("\n"))
                .filter(line -> line != null)
                .map(line -> line.split(","))
                .map(array -> new ArrayList<String>(Arrays.asList(array)))
                .collect(Collectors.toCollection((ArrayList::new)));
        MatrixBuilder matrix = new MatrixBuilder(rows, rows, rows);     
        
        TableView<Content> solutionTable = (TableView<Content>) reflectiveGet(controller, "solutiontable");
        TableColumn tc = solutionTable.getColumns().get(0);
        solutionTable.getSelectionModel().select(1000,tc);
    }  
    
    @Test
    @Order(96)
    void testBadDefaultFile() throws IOException {
        Throwable expected = null;
        try {
            ArrayList<ArrayList<String>> priceRows = controller.readCsvfile(Path.of("notDefaultPrice.csv"));
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
        try {
            ArrayList<ArrayList<String>> errorRows = controller.readCsvfile(Path.of("spacesOnly.csv"));
        } catch (Throwable t) {
            expected = t;
        }
    }
    
    @Test
    @Order(95)
    void testgetItemsCornerCases() throws IOException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Model model = (Model) reflectiveGet(controller, "model");
        List<List<Content>> items = model.getItems();
        int rows = items.size();
        Content testContent = new Content(1000.0);
        int columns = model.getTableColumns().size();
        int itemColumns = items.get(0).size();
        var pairs = List.of(new Pair(0, 0), new Pair(rows - 1, columns - 1), new Pair(0, columns - 2),
                new Pair(rows - 2, 3), new Pair(3, columns-1),new Pair(11,7));
        for (Pair pair : pairs) {
            Integer x = (Integer) pair.getKey();
            Integer y = (Integer) pair.getValue();
            try {
                items.get(x).set(y, testContent);
            } catch (RuntimeException e) {
            }
        }
      
    }
    

}