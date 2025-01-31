package fertilizer;

import static fertilizer.Content.convertDouble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class MatrixBuilder {


    private double[][] analysisMatrix;

    private LinkedHashMap<String,Double> ingredientMap, nutrientMap;

    private HashMap<String, Integer> analysisIngredientMap,analysisNutrientMap;
    private String[] nutrientNames, ingredientNames;

  
    public MatrixBuilder(ArrayList<ArrayList<String>> priceRows, ArrayList<ArrayList<String>> requirementRows, ArrayList<ArrayList<String>> ingredientRows) {
        ingredientMap = priceRows.stream()
                .skip(1) // skip headers
                .collect(Collectors.toMap(row -> row.get(0), row -> convertDouble(row.get(1)), (x, y) -> y, LinkedHashMap::new));

        nutrientMap = requirementRows.stream().skip(1) // skip headers
                .collect(Collectors.toMap(row -> row.get(0), row -> convertDouble(row.get(1)), (x, y) -> y, LinkedHashMap::new));        
        
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
 
    public LinkedHashMap<String, Double> getIngredientMap() {
        return ingredientMap;
    }

    public LinkedHashMap<String, Double> getNutrientMap() {
        return nutrientMap;
    }

    public ArrayList<ArrayList<Double>> getAnalysisMatrixs() {
       ArrayList<ArrayList<Double>> matrixList = new ArrayList<>();
       for (int i = 0; i < analysisMatrix.length; i++) {
           ArrayList<Double> rowList= new ArrayList<>();
           for (int j = 0; j <analysisMatrix[i].length; j++) {
               rowList.add(analysisMatrix[i][j]);
           } 
           matrixList.add(rowList);       
       }
       return matrixList;
    }

}
