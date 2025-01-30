package fertilizer;

import static fertilizer.Content.convertDouble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class MatrixBuilder {

    private double[] ingredientPrices, nutrientRequirements;
    private double[][] analysisMatrix;

    private LinkedHashMap<String,String> ingredientMap, nutrientMap;

    private HashMap<String, Integer> analysisIngredientMap,analysisNutrientMap;
    private String[] nutrientNames, ingredientNames;

  
    public MatrixBuilder(ArrayList<ArrayList<String>> priceRows, ArrayList<ArrayList<String>> requirementRows, ArrayList<ArrayList<String>> ingredientRows) {
        ingredientMap = priceRows.stream()
                .skip(1) // skip headers
                .collect(Collectors.toMap(row -> row.get(0), row -> row.get(1), (x, y) -> y, LinkedHashMap::new));
        
        ingredientPrices = ingredientMap.values().stream()
                .mapToDouble(val -> convertDouble(val)/2000.0).toArray();        

        nutrientMap = requirementRows.stream().skip(1) // skip headers
                .collect(Collectors.toMap(row -> row.get(0), row -> row.get(1), (x, y) -> y, LinkedHashMap::new));        

        nutrientRequirements = nutrientMap.values().stream()
                .mapToDouble(str -> convertDouble(str)).toArray();
        
        analysisIngredientMap = new HashMap<String, Integer>();
        for (int i = 1; i < ingredientRows.size(); i++) {
            analysisIngredientMap.put(ingredientRows.get(i).get(0), i);
        }

        analysisNutrientMap = new HashMap<String, Integer>();
        for (int i = 1; i < requirementRows.size(); i++) {
            analysisNutrientMap.put(requirementRows.get(i).get(0).replace('%', ' ').trim(), i);
        }
        int lprows = nutrientMap.size();
        int lpcols = ingredientMap.size();
        analysisMatrix = new double[lprows][lpcols];
        nutrientNames = nutrientMap.keySet().stream().toArray(String[]::new);
        ingredientNames = ingredientMap.keySet().stream().toArray(String[]::new);
        for (int i = 0; i < lprows; i++) {
            String nutrient = nutrientNames[i];
            for (int j = 0; j < lpcols; j++) {
                double val = 0.0;
                try {
                    String ingredientName = ingredientNames[j];
                    int rowPosition = analysisIngredientMap.get(ingredientName);
                    int colPosition = analysisNutrientMap.get(nutrient);
                    String valString = ingredientRows.get(rowPosition).get(colPosition);
                    val = convertDouble(valString);
                } catch (Throwable t) {
                }
                analysisMatrix[i][j] = val / 100.0;
            }
        }
    }

    public double[] getIngredientPrices() {
        return ingredientPrices;
    }

    public double[] getNutrientRequirements() {
        return nutrientRequirements;
    }

    public double[][] getAnalysisMatrixs() {
        return analysisMatrix;
    }
    
    public String[] getNutrientNames() {
        return nutrientNames;
    }

    public String[] getIngredientNames() {
        return ingredientNames;
    }

}
