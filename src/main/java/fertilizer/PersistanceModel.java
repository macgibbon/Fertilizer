package fertilizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.math4.legacy.optim.linear.Relationship;

public class PersistanceModel {

    LinkedHashMap<String, Double> ingredientMap;
    LinkedHashMap<String, Double> nutrientMap;
    ArrayList<ArrayList<Double>> coefficients;
    LinkedHashMap<String,Boolean> enableMap;
    LinkedHashMap<String, Relationship> constraintMap;
    
      public PersistanceModel(LinkedHashMap<String, Double> ingredientMap, LinkedHashMap<String, Double> nutrientMap,
            ArrayList<ArrayList<Double>> coefficients, LinkedHashMap<String, Boolean> enableMap, LinkedHashMap<String, Relationship> constraintMap) {
        super();
        this.ingredientMap = ingredientMap;
        this.nutrientMap = nutrientMap;
        this.coefficients = coefficients;
        this.enableMap = enableMap;
        this.constraintMap = constraintMap;
    }
    
    
}
