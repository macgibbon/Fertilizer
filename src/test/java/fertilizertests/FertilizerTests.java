package fertilizertests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    
    
   
}
