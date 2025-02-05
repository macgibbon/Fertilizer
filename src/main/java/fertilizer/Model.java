package fertilizer;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.commons.math4.legacy.optim.linear.LinearConstraint;
import org.apache.commons.math4.legacy.optim.linear.LinearConstraintSet;
import org.apache.commons.math4.legacy.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math4.legacy.optim.linear.NonNegativeConstraint;
import org.apache.commons.math4.legacy.optim.linear.Relationship;
import org.apache.commons.math4.legacy.optim.linear.SimplexSolver;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

public class Model { 
    
    private static final double objectiveConstant= 0.0;
    
    // Inputs as collections for easier insertion and deletion
    private LinkedHashMap<String, Double> ingredientMap;
    private LinkedHashMap<String, Double> nutrientMap;
    private ArrayList<ArrayList<Double>> coefficients;

    // Output as arrays for simplicity and easier interface with LP optimization library
    private double[] solutionIngredientAmounts;
    private String solutionPrice;
    private double[] solutionNutrientAmounts;
    private String solutionTotal;
    private ArrayList<String> solutionHeaders;
  
    public Model( LinkedHashMap<String,Double> nutrientMap, LinkedHashMap<String,Double> ingredientMap, ArrayList<ArrayList<Double>> coefficients) {
        this.ingredientMap = ingredientMap;
        this.nutrientMap = nutrientMap;
        this.coefficients = coefficients;
        this.solutionHeaders = new ArrayList<String>();
        solutionIngredientAmounts = new double[ingredientMap.size()];
        solutionPrice="";
        solutionNutrientAmounts = new double[ingredientMap.size()+2];
        solutionHeaders.addAll(ingredientMap.keySet());
        solutionHeaders.add("Constraint lbs");
        solutionHeaders.add("Actual lbs");
    }

    public PointValuePair calculateSolution() {   
        int numberOfNutrientContraints = nutrientMap.size();
        Relationship[] constraintRelationsShips = new Relationship[numberOfNutrientContraints];
        Arrays.fill(constraintRelationsShips,Relationship.GEQ);
        double[] nutrientAmounts = nutrientMap.values().stream().mapToDouble( D -> D.doubleValue()).toArray();

        Collection<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < numberOfNutrientContraints; i++) {
            double[] constraintCoefficients = coefficients.get(i).stream().mapToDouble(D -> D.doubleValue()).toArray();
            double constraint = nutrientAmounts[i];
            Relationship r = constraintRelationsShips[i];
            constraints.add(new LinearConstraint(constraintCoefficients, r, constraint));
        }
        
        int numberOfIngredients = ingredientMap.size();
        double[] ingredientPrices = ingredientMap.values().stream().mapToDouble(D -> D.doubleValue()/2000.0).toArray();
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(ingredientPrices,
                objectiveConstant);

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(objectiveFunction, new LinearConstraintSet(constraints),
                GoalType.MINIMIZE, new NonNegativeConstraint(true));
        double[] points = solution.getPoint();
        solutionIngredientAmounts = new double[numberOfIngredients];
        solutionPrice = String.format("$%.2f", solution.getValue().doubleValue());
        solutionNutrientAmounts = new double[numberOfNutrientContraints];
        double total = 0.0 ;
        for (int i = 0; i < numberOfIngredients; i++) {
            double amount = points[i];
            solutionIngredientAmounts[i] = amount;
            total+=amount;
            for (int j = 0; j < numberOfNutrientContraints; j++) {
                double analysis = coefficients.get(j).get(i);
                solutionNutrientAmounts[j] += amount * analysis;
            }
        }
        solutionTotal = String.format("%.0f lbs", total);
        return solution;
    }

    public List<List<Content>> getItems() {
         final int numberOfIngredients = ingredientMap.size();
         final int numberOfNutrientContraints = nutrientMap.size();  
         final int priceColumn = numberOfNutrientContraints + 1;
         final int amountColumn = numberOfNutrientContraints + 2;
         var list = new AbstractList<List<Content>>() {

            @Override
            public int size() {
                return numberOfIngredients + 2;
            }

            @Override
            public List<Content> get(int row) {
                return new AbstractList<Content>() {
                     @Override
                    public int size() {
                        return numberOfNutrientContraints + 3;
                    }

                    @Override
                    public Content get(int column) {
                        if ((column == priceColumn) && (row == numberOfIngredients+1))
                            return new Content(solutionPrice); 
                        if ((column == amountColumn) && (row == numberOfIngredients+1))
                            return new Content(solutionTotal);     
                        if (column == 0)
                            return new Content(solutionHeaders.get(row));
                        if (row == numberOfIngredients) {
                            String nutrient = (String) nutrientMap.keySet().toArray()[column-1];
                            return  new Content(nutrientMap.get(nutrient));                           
                        }
                         if (row == numberOfIngredients+1)
                            return new Content(solutionNutrientAmounts[column-1]); 
                        if (column == amountColumn)
                            return new Content(solutionIngredientAmounts[row]);
                        if (column == priceColumn) {
                            String ingredient = (String) ingredientMap.keySet().toArray()[row]; 
                            return new Content(ingredientMap.get(ingredient)); 
                        }
                         return new Content(coefficients.get(column-1).get(row));
                    }

                    @Override
                    public Content set(int column, Content cell) {
                        if ((column == priceColumn) && (row == numberOfIngredients+1))
                           throw new RuntimeException("Cell can't be set!");
                        else if (column == 0)
                            throw new RuntimeException("Cell can't be set!");
                        else if (row == numberOfIngredients) {
                            String nutrient = (String) nutrientMap.keySet().toArray()[column-1];
                            nutrientMap.put(nutrient,cell.value);
                        }
                        else if (row == numberOfIngredients+1)
                            throw new RuntimeException("Cell can't be set!");
                        else if (column == amountColumn)
                            throw new RuntimeException("Cell can't be set!");
                        else if (column == priceColumn) {
                            String ingredient = (String) ingredientMap.keySet().toArray()[row]; 
                            ingredientMap.put(ingredient, cell.value);
                        }
                        else 
                            coefficients.get(column-1).set(row, cell.value); 
                        return cell;
                    }
                };
            }
        };
        return list;
    }

    private TableColumn<List<Content>, String> createStringColumn(ArrayList<String> displayHeaders, int column) {
        final int col = column;
        final int numberOfIngredients = ingredientMap.size();
        final int numberOfNutrientContraints = nutrientMap.size();
        TableColumn<List<Content>, String> aTableColumn = new TableColumn<>(displayHeaders.get(column));
        aTableColumn.setCellFactory(list -> {
            TextFieldTableCell<List<Content>,String>  cell= new TextFieldTableCell<List<Content>,String>(new DefaultStringConverter()) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    try {
                    int row = getTableRow().getIndex();
                    if (row == numberOfIngredients+1) {
                        this.getStyleClass().add("pricecolumn");
                        setEditable(false);
                    }
                    } catch (Throwable t) {
                    }
                }                
            };
            return cell;
        });
        aTableColumn.setCellValueFactory(cellData -> {
            String cellValue = "";
            try {
                cellValue = cellData.getValue().get(col).toString();
            } catch (IndexOutOfBoundsException e) {
            }
            return new ReadOnlyStringWrapper(cellValue);
        });
        if ((col==0) || (col > numberOfNutrientContraints+1))
            aTableColumn.setEditable(false);
        else
            aTableColumn.setEditable(true); 
        aTableColumn.setOnEditCommit(event -> {
            final String value = event.getNewValue();
            int row = event.getTablePosition().getRow();
            try {
                double d = Double.valueOf(value.trim());  
                event.getTableView().getItems().get(row).set(col, new Content(d));
            } catch (NumberFormatException nfe) {
                event.getTableView().getItems().get(row).set(col, new Content(value));
            }
         });     
        return aTableColumn;
    }

    public List<TableColumn<List<Content>, String>> getTableColumns() {
        ArrayList<String> columnHeaders = new ArrayList<>();
        columnHeaders.add("Ingredients");
        columnHeaders.addAll(nutrientMap.keySet());
        columnHeaders.add("$/Ton");
        columnHeaders.add("Amount lbs");

        List<TableColumn<List<Content>, String>> columns = new ArrayList<TableColumn<List<Content>, String>>();
        for (int i = 0; i < columnHeaders.size(); i++) {
            TableColumn<List<Content>, String> stringColumn = createStringColumn(columnHeaders, i);
            columns.add(stringColumn);
        }
        final int priceColumn = nutrientMap.size() + 1;
        columns.get(priceColumn).getStyleClass().add("pricecolumn");
        columns.get(priceColumn+1).getStyleClass().add("pricecolumn");
        return columns;
    }
}
