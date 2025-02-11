package fertilizer;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.commons.math4.legacy.optim.linear.LinearConstraint;
import org.apache.commons.math4.legacy.optim.linear.LinearConstraintSet;
import org.apache.commons.math4.legacy.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math4.legacy.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math4.legacy.optim.linear.NonNegativeConstraint;
import org.apache.commons.math4.legacy.optim.linear.Relationship;
import org.apache.commons.math4.legacy.optim.linear.SimplexSolver;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.Event;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

public class SolutionModel { 
    
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
    ArrayList<String> solutionHeaders;
    private Relationship[] constraintRelationsShips;
    private LinkedHashMap<String, Relationship> constraintMap;   
    private volatile boolean infeasible = false;

    private Boolean[] enableArray;

    private int col;

    private int numberOfIngredients;

    private int solveAmountRow;

    private int relationshipRow;

    private int constraintRow;

    private int numberOfNutrientContraints;
    

    final int nameColumn = 0;
    final int enableColumn =1;
    final int priceColumn ;
    final int amountColumn;   
  
  
    public SolutionModel(MatrixBuilder matrix) {
        this.ingredientMap = matrix.getIngredientMap();
        this.nutrientMap = matrix.getNutrientMap();
        this.coefficients = matrix.getAnalysisMatrixs();
        this.constraintMap = matrix.getConstraintMap();
        this.constraintRelationsShips = constraintMap.values().toArray(new Relationship[constraintMap.size()]);
        this.solutionHeaders = new ArrayList<String>();  
        this.enableArray = matrix.getEnableMap().values().stream().toArray(Boolean[]::new);
        solutionIngredientAmounts = new double[ingredientMap.size()];
        solutionPrice="";
        solutionNutrientAmounts = new double[ingredientMap.size()+2];
        solutionHeaders.addAll(ingredientMap.keySet());
        solutionHeaders.add("Relationship");
        solutionHeaders.add("Constraint lbs");
        solutionHeaders.add("Actual lbs");
        
        numberOfIngredients = ingredientMap.size();
        solveAmountRow = numberOfIngredients+2;
        relationshipRow = numberOfIngredients;
        constraintRow = numberOfIngredients+1;
        numberOfNutrientContraints = nutrientMap.size();
        priceColumn = numberOfNutrientContraints + 2;
        amountColumn = numberOfNutrientContraints + 3;
      
        
    }

    public void calculateSolution() {  
        infeasible = false;
        
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
        try {
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
        } catch (NoFeasibleSolutionException n) {
            infeasible =true;
            solutionPrice = "!$!!!!!";
            solutionTotal = "!!!!! lbs";
            for (int i = 0; i < numberOfIngredients; i++) {
                solutionIngredientAmounts[i] = 0;
            }
            for (int j = 0; j < numberOfNutrientContraints; j++) {
                solutionNutrientAmounts[j] =0;
            }
        }
    }

    public List<List<Content>> getItems() {        
       
         var list = new AbstractList<List<Content>>() {

            @Override
            public int size() {
                return numberOfIngredients + 3;
            }

            @Override
            public List<Content> get(int row) {
                
                return new AbstractList<Content>() {
                     @Override
                    public int size() {
                         // +2 constraint amount and ingredient amount delivered in solution
                        return numberOfNutrientContraints + 2;
                    }

                    @Override
                    public Content get(int column) {
                        try {
                        if ((column == priceColumn) && (row == solveAmountRow))
                            return new Content(solutionPrice); 
                        if ((column == amountColumn) && (row == solveAmountRow))
                            return new Content(solutionTotal);     
                        if (column == nameColumn)
                            return new Content(solutionHeaders.get(row));
                        if (column == enableColumn)
                            return new Content(enableArray[row]);
                        if (row == constraintRow) {
                            String nutrient = (String) nutrientMap.keySet().toArray()[column-2];
                            Double value = nutrientMap.get(nutrient);
                            System.out.println( row + " " + column + " " + value);
                            return  new Content(value);                           
                        }
                         if (row == solveAmountRow)
                            return new Content(solutionNutrientAmounts[column-2]); 
                        if (column == amountColumn)
                            return new Content(solutionIngredientAmounts[row]);
                        if (column == priceColumn) {
                            String ingredient = (String) ingredientMap.keySet().toArray()[row]; 
                            return new Content(ingredientMap.get(ingredient)); 
                        }
                        if (row == relationshipRow) {
                            String nutrient = (String) nutrientMap.keySet().toArray()[column-2];
                            return new Content(constraintMap.get(nutrient));   
                        }
                         return new Content(coefficients.get(column-2).get(row));
                        } catch (Throwable t) {
                            return new Content("");
                        }
                    }

                    @Override
                    public Content set(int column, Content cell) {                       
                        if ((column == amountColumn) && (row == solveAmountRow))
                            throw new RuntimeException("Cell can't be set!");
                        if (row == solveAmountRow)
                            throw new RuntimeException("Cell can't be set!");
                        else if (column == 0)
                            throw new RuntimeException("Cell can't be set!");
                        else if (row == constraintRow) {
                            String nutrient = (String) nutrientMap.keySet().toArray()[column-2];
                            nutrientMap.put(nutrient,cell.value);
                        }
                        else if (column == amountColumn)
                            throw new RuntimeException("Cell can't be set!");
                        else if (column == priceColumn) {
                            String ingredient = (String) ingredientMap.keySet().toArray()[row]; 
                            ingredientMap.put(ingredient, cell.value);
                        }  
                        else if (row == relationshipRow) {
                            String nutrient = (String) nutrientMap.keySet().toArray()[column-2];
                            Relationship rMatch = Stream.of(Relationship.values())
                                    .filter(r -> r.toString().equals(cell.name))
                                    .findFirst().get();
                            constraintMap.put(nutrient,rMatch);
                        }
                        else 
                            coefficients.get(column-2).set(row, cell.value); 
                        return cell;
                    }
                };
            }
        };
        return list;
    }

    private TableColumn<List<Content>, String> createStringColumn(ArrayList<String> displayHeaders, int col) {

        TableColumn<List<Content>, String> aTableColumn = new TableColumn<>(displayHeaders.get(col));
        aTableColumn.setCellFactory(list -> {
            return customCellFactory(col);
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
                Event.fireEvent(event.getTableView(), new SolveItEvent());
            } catch (NumberFormatException nfe) {
                event.getTableView().getItems().get(row).set(col, new Content(value));
            }
        });    
        return aTableColumn;
    }

    private TableCell<List<Content>, String> customCellFactory(int col) {        
      
        TextFieldTableCell<List<Content>,String>  cell= new TextFieldTableCell<List<Content>,String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                var tableRow = getTableRow();
                if (tableRow != null) {
                int row = getTableRow().getIndex();
                // bottom row
                if ((col == 0) ||
                   (row == solveAmountRow) || 
                   // right bottom color   
                   ((row >= relationshipRow) && (col <=1)) || 
                   // rightmost column
                   (col == numberOfNutrientContraints+3) ||
                   // left bottom corner
                   ((row >= relationshipRow) && (col == priceColumn ))) {                   
                     setEditable(false);
                     this.getStyleClass().add("readonly");
                     if (infeasible) {
                         this.getStyleClass().add("infeasible");
                     }
                     else {
                         this.getStyleClass().remove("infeasible");
                     }                         
                }
                }
            }
        };
        return cell;
    }

    public List<TableColumn<List<Content>, String>> getTableColumns() {
        ArrayList<String> columnHeaders = new ArrayList<>();
        columnHeaders.add("Ingredients");
        columnHeaders.add("Enable");
        columnHeaders.addAll(nutrientMap.keySet());
        columnHeaders.add("$/Ton");
        columnHeaders.add("Amount lbs");

        List<TableColumn<List<Content>, String>> columns = new ArrayList<TableColumn<List<Content>, String>>();
        for (int i = 0; i < columnHeaders.size(); i++) {
            TableColumn<List<Content>, String> stringColumn = createStringColumn(columnHeaders, i);
            columns.add(stringColumn);
        }
       // final int priceColumn = nutrientMap.size() + 1;
      //  columns.get(priceColumn).getStyleClass().add("readonly");
       // columns.get(priceColumn+1).getStyleClass().add("readonly");
        return columns;
    }
}
