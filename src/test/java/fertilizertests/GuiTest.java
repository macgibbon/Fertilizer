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
import org.testfx.framework.junit5.Stop;

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
public class GuiTest extends MainApp {

    
    
    @Start
    void onStart(Stage primaryStage) throws Exception {
        super.start(primaryStage);
    }
    
    @Stop
    void onStop() throws Exception {
        super.stop();
    }

    @AfterAll
    public static void cleanup() {
        // Platform.exit();
    }

    @Test
    void testSave(FxRobot robot) throws Exception {
        robot.clickOn("File");
        robot.clickOn("Save Formulation");
    }

    @Test
    void testLoad(FxRobot robot) throws Exception {
        robot.clickOn("File");
        robot.clickOn("Load Formulation");
    }

    @Test
    void testSolve(FxRobot robot) throws Exception {
        robot.clickOn("Action");
        robot.clickOn("Calculate least cost solution");
        robot.clickOn("Solution");

    }

    @Test
    void testEdits(FxRobot robot) {
        try {
            robot.clickOn("Edit");
            robot.clickOn("Edit Default Prices");
            delay(2);
            pressGlobalExitKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            robot.clickOn("Edit");
            robot.clickOn("Edit Default Requirements");
            delay(2);
            pressGlobalExitKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            robot.clickOn("Edit");
            robot.clickOn("Edit Default Ingredient Analysis");
            delay(2);
            pressGlobalExitKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testAnalysisEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("0.46");
        robot.push(KeyCode.PERIOD);
        robot.push(KeyCode.DIGIT4);
        robot.push(KeyCode.DIGIT5);
        robot.push(KeyCode.ENTER);
    }

    @Test
    void testPriceEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("700.00");
        robot.push(KeyCode.DIGIT7);
        robot.push(KeyCode.DIGIT0);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.ENTER);
    }

    @Test
    void testRequirementEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("360.00");
        robot.push(KeyCode.DIGIT3);
        robot.push(KeyCode.DIGIT6);
        robot.push(KeyCode.DIGIT1);
        robot.push(KeyCode.ENTER);
    }

    @Test
    void testBadEntry(FxRobot robot) {
        robot.clickOn("Solution");
        robot.doubleClickOn("0.24");
        robot.push(KeyCode.Z);
        robot.push(KeyCode.Z);
        robot.push(KeyCode.ENTER);
    }

    @Test
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
                    throw new RuntimeException("Deliberate exception to test exception handling");
                }

            };
            reflectiveSet(model, controller, "model");
            robot.clickOn("Action");
            robot.clickOn("Calculate least cost solution");
            delay(2);
            // PointValuePair solution = model.calculateSolution();
            // System.out.println(solution.getValue());
        } catch (Throwable t) {
            error = t;
            t.printStackTrace();
        }
        assertTrue(error == null);
    }

    @Test
    void testRedundantRows()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String prices = """
                Urea,640
                Ammonium Nitrate,700
                Ammonium Nitrate,790
                Diammonium Phosphate (DAP),710
                """;

        ArrayList<ArrayList<String>> rows = Stream.of(prices.split("\n")).filter(line -> line != null)
                .map(line -> line.split(",")).map(array -> new ArrayList<String>(Arrays.asList(array)))
                .collect(Collectors.toCollection((ArrayList::new)));
        MatrixBuilder matrix = new MatrixBuilder(rows, rows, rows);

        TableView<Content> solutionTable = (TableView<Content>) reflectiveGet(controller, "solutiontable");
        TableColumn tc = solutionTable.getColumns().get(0);
        solutionTable.getSelectionModel().select(1000, tc);
    }

    @Test
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

    @SuppressWarnings("unchecked")
    @Test
    void testgetItemsEditingCornerCases() throws IOException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Model model = (Model) reflectiveGet(controller, "model");
        List<List<Content>> items = model.getItems();
        int rows = items.size();
        int columns = items.get(0).size();
        Content testContent = new Content(1000.0);
        int solutionAmountColumn = columns-1;
        int priceColumn = columns-2;
        int solutionAmountRow = rows -1;
        int requirementRow = rows-2;
        int nameColumn = 0;

        var pairs = List.of(
                new Pair(0,nameColumn),                      // test name column 
                new Pair(0, solutionAmountColumn),  // test solution nutrient amount column              
                new Pair(solutionAmountRow, 1),      // test solution ingredient amount column 
                new Pair(requirementRow,priceColumn )                    // test price in requirement row 
              //  new Pair(0, columns - 2),        
              //  new Pair(rows - 2, 3), 
             //   new Pair(3, columns - 1), 
            //    new Pair(11, 7)
        );
        for (Pair pair : pairs) {
            Integer x = (Integer) pair.getKey();
            Integer y = (Integer) pair.getValue();
            try {
                items.get(x).set(y, testContent);
            } catch (RuntimeException e) {
            }
        }
        delay(2);
    }

    @Test
    void testEmptySelection() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
       TableView<List<Content>> solutionTable = (TableView<List<Content>>) reflectiveGet(controller, "solutiontable");
       TableColumn<List<Content>, ?> aTableColumn = solutionTable.getColumns().get(1);
       solutionTable.getSelectionModel().select(2, aTableColumn);
       solutionTable.getSelectionModel().clearSelection();
        
        
    }
    
}