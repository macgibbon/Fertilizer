package fertilizer;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    private int numberOfIngredients;
    private int numberOfNutrientContraints;
    private String[] ingredientNames;
    private String[] nutrientNames;
    private double[] ingredientPrices;
    private double[] nutrientAmounts;
    private double[][] analysisMatrix;

    private Relationship[] constraintRelationsShips;
    private double objectiveConstant = 0.0;
    
    private List<String> solutionHeaders;
    private String solutionPrice;
    private String solutionTotal;
    private double[] solutionNutrientAmounts;
    private double[] solutionIngredientAmounts;    

    public Model(String[] ingredientNames, String[] nutrientNames, double[] ingredientPrices, double[] nutrientAmounts,  double[][] coefficients) {
        this.ingredientNames = ingredientNames;
        this.nutrientNames = nutrientNames;
        this.ingredientPrices = ingredientPrices;
        this.nutrientAmounts = nutrientAmounts;
        this.analysisMatrix = coefficients;
        numberOfIngredients = ingredientPrices.length;
        numberOfNutrientContraints = nutrientAmounts.length;
        constraintRelationsShips = new Relationship[numberOfNutrientContraints];
        Arrays.fill(constraintRelationsShips, Relationship.GEQ);
        this.solutionHeaders = new ArrayList<String>();
        solutionHeaders.addAll(Arrays.asList(ingredientNames));
        solutionHeaders.add("Constraint lbs");
        solutionHeaders.add("Actual lbs");
        solutionNutrientAmounts = new double[numberOfNutrientContraints];
        solutionIngredientAmounts = new double[numberOfIngredients];
    }

    public PointValuePair calculateSolution() {
        Collection<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < numberOfNutrientContraints; i++) {
            double[] constraintCoefficients = analysisMatrix[i];
            double constraint = nutrientAmounts[i];
            Relationship r = constraintRelationsShips[i];
            constraints.add(new LinearConstraint(constraintCoefficients, r, constraint));
        }
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(ingredientPrices,
                objectiveConstant);

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(objectiveFunction, new LinearConstraintSet(constraints),
                GoalType.MINIMIZE, new NonNegativeConstraint(true));
        double[] points = solution.getPoint();
        solutionIngredientAmounts = new double[numberOfIngredients];
        this.solutionPrice = String.format("$%.2f", solution.getValue().doubleValue());
        solutionNutrientAmounts = new double[numberOfNutrientContraints];
        double total = 0.0;
        for (int i = 0; i < numberOfIngredients; i++) {
            double amount = points[i];
            solutionIngredientAmounts[i] = amount;
            total+=amount;
            for (int j = 0; j < numberOfNutrientContraints; j++) {
                double analysis = analysisMatrix[j][i];
                solutionNutrientAmounts[j] += amount * analysis;
            }
        }
        this.solutionTotal = String.format("%.0f lbs", total);
        return solution;
    }

    public List<List<Content>> getItems() {
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
        columnHeaders.addAll(Arrays.asList(nutrientNames));
        columnHeaders.add("$/Ton");
        columnHeaders.add("Amount lbs");

        List<TableColumn<List<Content>, String>> columns = new ArrayList<TableColumn<List<Content>, String>>();
        for (int i = 0; i < columnHeaders.size(); i++) {
            TableColumn<List<Content>, String> stringColumn = createStringColumn(columnHeaders, i);
            columns.add(stringColumn);
        }
        final int priceColumn = numberOfNutrientContraints + 1;
        columns.get(priceColumn).getStyleClass().add("pricecolumn");
        columns.get(priceColumn+1).getStyleClass().add("pricecolumn");
        return columns;
    }

    public String[] getIngredientNames() {
        return ingredientNames;
    }

    public String[] getNutrientNames() {
        return nutrientNames;
    }
    
}
