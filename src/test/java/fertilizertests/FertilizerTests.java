package fertilizertests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.commons.math3.optim.PointValuePair;
import org.junit.jupiter.api.Test;

import fertilizer.MainController;
import fertilizer.MatrixBuilder;
import fertilizer.Model;

class FertilizerTests {

    @Test
    void testReadMatrix() {
        Throwable error = null;
        try {
            MainController controller = new MainController();
            ArrayList<ArrayList<String>> priceRows = controller.readCsvfile(Path.of("defaultPrices.csv"));
            ArrayList<ArrayList<String>> ingredientRows = controller.readCsvfile(Path.of("defaultIngredients.csv"));
            ArrayList<ArrayList<String>> requirementRows = controller.readCsvfile(Path.of("defaultRequirements.csv"));
            MatrixBuilder matrix = new MatrixBuilder(priceRows, requirementRows, ingredientRows);
            Model model = new Model(matrix.getIngredientNames(), matrix.getNutrientNames(), matrix.getIngredientPrices(), matrix.getNutrientRequirements(), matrix.getAnalysisMatrixs());
            PointValuePair solution =  model.calculateSolution();
            System.out.println(solution.getValue());
        } catch (Throwable t) {
            error = t;
            t.printStackTrace();
        }
        assertTrue(error == null);
    }


    
   
}
