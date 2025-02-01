package fertilizertests;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import fertilizer.Content;
import fertilizer.MainApp;
import fertilizer.MainController;
import fertilizer.MatrixBuilder;
import fertilizer.Model;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

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
    @Order(100)
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
        robot.push(KeyCode.DIGIT6);
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
    void testReadMatrix() {
        Throwable error = null;
        try {
            MainController controller = new MainController();
            ArrayList<ArrayList<String>> priceRows = controller.readCsvfile(Path.of("defaultPrices.csv"));
            ArrayList<ArrayList<String>> ingredientRows = controller.readCsvfile(Path.of("defaultIngredients.csv"));
            ArrayList<ArrayList<String>> requirementRows = controller.readCsvfile(Path.of("defaultRequirements.csv"));
            MatrixBuilder matrix = new MatrixBuilder(priceRows, requirementRows, ingredientRows);
       ////     Model model = new Model(matrix.getIngredientNames(), matrix.getNutrientNames(), matrix.getIngredientPrices(), matrix.getNutrientRequirements(), matrix.getAnalysisMatrixs());
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
    void testRedundantRows() {
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
    }  
    
    @Test
    @Order(96)
    void testUncaughtExceptionHandler(FxRobot robot) throws Throwable, SecurityException {
    
    }
    
   
    private void delay(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
        }
    }

    private void pressGlobalExitKey() throws AWTException {
        java.awt.Robot awtRobot = new java.awt.Robot();
        awtRobot.keyPress(KeyEvent.VK_ESCAPE);
        awtRobot.keyRelease(KeyEvent.VK_ESCAPE);
    }
}