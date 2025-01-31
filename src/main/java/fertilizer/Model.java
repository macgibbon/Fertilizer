package fertilizer;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

public class Model { 
    
    private static final double objectiveConstant= 0.0;

    private LinkedHashMap<String, Double> ingredientMap;
    private LinkedHashMap<String, Double> nutrientMap;
    private double[][] coefficients;
  
    public Model( LinkedHashMap<String,Double> nutrientMap, LinkedHashMap<String,Double> ingredientMap, double[][] coefficients) {
        this.ingredientMap = ingredientMap;
        this.nutrientMap = nutrientMap;
        this.coefficients = coefficients;    
    }

    public PointValuePair calculateSolution() {   
        int numberOfNutrientContraints = nutrientMap.size();
        Relationship[] constraintRelationsShips = new Relationship[numberOfNutrientContraints];
        double[] nutrientAmounts = nutrientMap.values().stream().mapToDouble( D -> D.doubleValue()).toArray();

        Collection<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < numberOfNutrientContraints; i++) {
            double[] constraintCoefficients = coefficients[i];
            double constraint = nutrientAmounts[i];
            Relationship r = constraintRelationsShips[i];
            constraints.add(new LinearConstraint(constraintCoefficients, r, constraint));
        }
        
        int numberOfIngredients = ingredientMap.size();
        double[] ingredientPrices = ingredientMap.values().stream().mapToDouble(D -> D.doubleValue()).toArray();
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(ingredientPrices,
                objectiveConstant);

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(objectiveFunction, new LinearConstraintSet(constraints),
                GoalType.MINIMIZE, new NonNegativeConstraint(true));
        double[] points = solution.getPoint();
        double[] solutionIngredientAmounts = new double[numberOfIngredients];
        String solutionPrice = String.format("$%.2f", solution.getValue().doubleValue());
        double[] solutionNutrientAmounts = new double[numberOfNutrientContraints];
        double total = 0.0 ;
        for (int i = 0; i < numberOfIngredients; i++) {
            double amount = points[i];
            solutionIngredientAmounts[i] = amount;
            total+=amount;
            for (int j = 0; j < numberOfNutrientContraints; j++) {
                double analysis = coefficients[j][i];
                solutionNutrientAmounts[j] += amount * analysis;
            }
        }
        String solutionTotal = String.format("%.0f lbs", total);
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
                        if (row == numberOfIngredients)
                            return new Content(nutrientAmounts[column-1]); 
                        if (row == numberOfIngredients+1)
                            return new Content(solutionNutrientAmounts[column-1]); 
                        if (column == amountColumn)
                            return new Content(solutionIngredientAmounts[row]);
                        if (column == priceColumn)
                            return new Content(ingredientPrices[row]*2000.0);
                         return new Content(analysisMatrix[column-1][row]);
                    }

                    @Override
                    public Content set(int column, Content cell) {
                        if ((column == priceColumn) && (row == numberOfIngredients+1))
                            solutionPrice = cell.name;  
                        else if (column == 0)
                            solutionHeaders.set(row,cell.name);
                        else if (row == numberOfIngredients)
                            nutrientAmounts[column-1] = cell.value;
                        else if (row == numberOfIngredients+1)
                           solutionNutrientAmounts[column-1]= cell.value; 
                        else if (column == amountColumn)
                            solutionIngredientAmounts[row]= cell.value; 
                        else if (column == priceColumn)
                            ingredientPrices[row]= cell.value/2000.0; 
                        else analysisMatrix[column-1][row]= cell.value; 
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
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            return new ReadOnlyStringWrapper(cellValue);
        });
        if ((col==0) || (col > numberOfNutrientContraints+1))
            aTableColumn.setEditable(false);
        else
            aTableColumn.setEditable(true);            
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

    /*
    public String[] getIngredientNames() {
        return ingredientNames;
    }

    public String[] getNutrientNames() {
        return nutrientNames;
    }
    */
    
}
