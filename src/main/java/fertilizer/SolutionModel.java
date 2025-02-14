package fertilizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
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

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

public class SolutionModel {

    private static final double objectiveConstant = 0.0;

    // Inputs as collections for easier insertion and deletion
    private LinkedHashMap<String, Double> ingredientMap;
    private LinkedHashMap<String, Double> nutrientMap;
    private ArrayList<ArrayList<Double>> coefficients;
    private ArrayList<Boolean> enableList;
    private LinkedHashMap<String, Relationship> constraintMap;

    // Output as arrays for simplicity and easier interface with LP optimization
    // library
    private double[] solutionIngredientAmounts;
    private String solutionPrice;
    private double[] solutionNutrientAmounts;
    private String solutionTotal;

    private volatile boolean infeasible = false;

    private List<String> rowHeaders;
    private int numberOfNutrientContraints;

    final int nameColumn;
    final int enableColumn;
    final int startAnalysisColumn;
    final int priceColumn;
    final int solutionColumn;

    private List<List<Content>> cachedItems;

    private ArrayList<String> columnHeaders;

    private List<Relationship> constraintRelationsShips;

    public SolutionModel(MatrixBuilder matrix) {
        this.ingredientMap = matrix.getIngredientMap();
        this.nutrientMap = matrix.getNutrientMap();
        this.coefficients = matrix.getAnalysisMatrixs();
        this.constraintMap = matrix.getConstraintMap();
        this.enableList = matrix.getEnableMap().values().stream().collect(Collectors.toCollection(ArrayList<Boolean>::new));

        nameColumn = 0;
        enableColumn = nameColumn + 1;
        startAnalysisColumn = enableColumn + 1;
        priceColumn = nutrientMap.size() + startAnalysisColumn;
        solutionColumn = priceColumn + 1;

        this.rowHeaders = new ArrayList<String>();
        rowHeaders.addAll(ingredientMap.keySet());
        rowHeaders.add("Relationship");
        rowHeaders.add("Constraint lbs");
        rowHeaders.add("Actual lbs");

        columnHeaders = new ArrayList<>();
        columnHeaders.add("Ingredients");
        columnHeaders.add("Enable");
        columnHeaders.addAll(nutrientMap.keySet());
        columnHeaders.add("$/Ton");
        columnHeaders.add("Amount lbs");

        cachedItems = new ArrayList<>();
        for (int i = 0; i < rowHeaders.size(); i++) {
            ArrayList<Content> itemsRow = new ArrayList<>();
            for (int j = 0; j < columnHeaders.size(); j++) {
                itemsRow.add(new Content());
            }
            cachedItems.add(itemsRow);
            itemsRow.set(0, new Content(rowHeaders.get(i), Celltype.name));
        }
        for (int i = 0; i < enableList.size(); i++) {
            boolean enable = enableList.get(i);
            List<Content> itemsRows = cachedItems.get(i);
            itemsRows.set(enableColumn, new Content(enable));
        }

        int rColumn = rowHeaders.indexOf("Relationship");
        List<Content> relationShipRow = cachedItems.get(rColumn);
        constraintRelationsShips = constraintMap.values().stream().collect(Collectors.toList());
        for (int j = 0; j < constraintRelationsShips.size(); j++) {
            Relationship realtionShip = constraintRelationsShips.get(j);
            relationShipRow.set(j + startAnalysisColumn, new Content(realtionShip));
        }

        for (int j = 0; j < coefficients.size(); j++) {
            for (int i = 0; i < coefficients.get(j).size(); i++) {
                double coef = coefficients.get(j).get(i);
                cachedItems.get(i).set(j + startAnalysisColumn, new Content(coef, Celltype.analysis));
            }
        }

        List<Double> priceColList = ingredientMap.values().stream().collect(Collectors.toList());
        for (int i = 0; i < priceColList.size(); i++) {
            Double price = priceColList.get(i);
            cachedItems.get(i).set(priceColumn, new Content(price, Celltype.price));
        }

        List<Double> constraintRowList = nutrientMap.values().stream().collect(Collectors.toList());
        for (int i = 0; i < constraintRowList.size(); i++) {
            Double requirementOrLimit = constraintRowList.get(i);
            cachedItems.get(ingredientMap.size() + 1).set(i + startAnalysisColumn, new Content(requirementOrLimit, Celltype.constraintAmount));
        }

        /*
        this.enableList = new ArrayList<Boolean>(matrix.getEnableMap().values());
        solutionIngredientAmounts = new double[ingredientMap.size()];
        solutionPrice="";
        solutionNutrientAmounts = new double[ingredientMap.size()+2];       
        
        numberOfIngredients = ingredientMap.size();
        solveAmountRow = numberOfIngredients+2;
        relationshipRow = numberOfIngredients;
        constraintRow = numberOfIngredients+1;
        numberOfNutrientContraints = nutrientMap.size();
        priceColumn = numberOfNutrientContraints + 2;
        amountColumn = numberOfNutrientContraints + 3;  
        */     
    }

    public void updateSolutionItems() {
        for (int i = 0; i < solutionIngredientAmounts.length; i++) {
            double amt = solutionIngredientAmounts[i];
            cachedItems.get(i).set(solutionColumn, new Content(amt, Celltype.ingredientAmount));
        }

        int actualRow = rowHeaders.indexOf("Actual lbs");
        for (int j = 0; j < solutionNutrientAmounts.length; j++) {
            double amt = solutionNutrientAmounts[j];
            cachedItems.get(actualRow).set(j + startAnalysisColumn, new Content(amt, Celltype.actualAmount));
        }
        cachedItems.get(actualRow).set(priceColumn, new Content(solutionPrice, Celltype.solutionPrice));
        cachedItems.get(actualRow).set(solutionColumn, new Content(solutionTotal, Celltype.totalAmount));

    }

    public List<List<Content>> getItems() {
        return cachedItems;
    }

    public void calculateSolution() {
        infeasible = false;

        double[] nutrientAmounts = nutrientMap.values().stream().mapToDouble(D -> D.doubleValue()).toArray();
        Collection<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < nutrientAmounts.length; i++) {
            double[] constraintCoefficients = coefficients.get(i).stream().mapToDouble(D -> D.doubleValue()).toArray();
            double constraint = nutrientAmounts[i];
            Relationship r = constraintRelationsShips.get(i);
            constraints.add(new LinearConstraint(constraintCoefficients, r, constraint));
        }
        int numberOfIngredients = ingredientMap.size();
        double[] ingredientPrices = ingredientMap.values().stream().mapToDouble(D -> D.doubleValue() / 2000.0).toArray();
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(ingredientPrices, objectiveConstant);

        SimplexSolver solver = new SimplexSolver();
        try {
            PointValuePair solution = solver.optimize(objectiveFunction, new LinearConstraintSet(constraints), GoalType.MINIMIZE,
                    new NonNegativeConstraint(true));
            double[] points = solution.getPoint();
            solutionIngredientAmounts = new double[numberOfIngredients];
            solutionPrice = String.format("$%.2f", solution.getValue().doubleValue());
            System.out.println(solutionPrice);
            solutionNutrientAmounts = new double[nutrientMap.size()];
            double total = 0.0;
            for (int i = 0; i < solutionIngredientAmounts.length; i++) {
                double amount = points[i];
                solutionIngredientAmounts[i] = amount;
                total += amount;
                for (int j = 0; j < nutrientMap.size(); j++) {
                    double analysis = coefficients.get(j).get(i);
                    solutionNutrientAmounts[j] += amount * analysis;
                }
            }
            solutionTotal = String.format("%.0f lbs", total);
        } catch (NoFeasibleSolutionException n) {
            infeasible = true;
            solutionPrice = "!$!!!!!";
            solutionTotal = "!!!!! lbs";
            for (int i = 0; i < numberOfIngredients; i++) {
                solutionIngredientAmounts[i] = 0;
            }
            for (int j = 0; j < numberOfNutrientContraints; j++) {
                solutionNutrientAmounts[j] = 0;
            }
        }
        updateSolutionItems();
    }
            
    public TableColumn<List<Content>, Content> getTableColumn(int column) {
        var aTableColumn = new TableColumn<List<Content>, Content>(columnHeaders.get(column));
        aTableColumn.setCellFactory(list -> new ContentTableCell());

        aTableColumn.setCellValueFactory(cellData -> {
            Content content = cellData.getValue().get(column);
            return new ReadOnlyObjectWrapper<Content>(content);
        });

        aTableColumn.setEditable(true);
        aTableColumn.setOnEditCommit(event -> {
            final Content value = event.getNewValue();
            System.out.println("Table edit commit " + event.getNewValue());
            int row = event.getTablePosition().getRow();
            try {
                // double d = value.value;
                event.getTableView().getItems().get(row).set(column, value);
                writeThroughCache(row, column, value);
                Event.fireEvent(event.getTableView(), new SolveItEvent());
            } catch (NumberFormatException nfe) {
                event.getTableView().getItems().get(row).set(column, value);
            }
        });

        return aTableColumn;
    }

    private void writeThroughCache(int row, int column, Content content) {
        Celltype celltype = content.celltype;
        switch (celltype) {
        case analysis:
            Double a = coefficients.get(column - 2).get(row);
            coefficients.get(column - 2).set(row, content.value);
            break;
        case constraintAmount:
            String nutrient = nutrientMap.keySet().toArray(new String[0])[column - 2];
            Double c = nutrientMap.get(nutrient);
            nutrientMap.put(nutrient, content.value);
            break;
        case price:
            String ingredient = ingredientMap.keySet().toArray(new String[0])[row];
            Double cp = ingredientMap.get(ingredient);
            ingredientMap.put(ingredient, content.value);
            break;
        case enable:
            break;
        case relationship:
            Relationship r = constraintRelationsShips.get(column - 2);
            Relationship r2 = Relationship.valueOf(content.toString());
            constraintRelationsShips.set(column - 2, r2);
            break;
        default:
            throw new IllegalArgumentException("Unexpected value: " + celltype);
        }

    }

 /*
       
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
      */

    /*
    private TableColumn<List<Content>, Content> createStringColumn(ArrayList<String> displayHeaders, int col) {

        TableColumn<List<Content>, Content> aTableColumn = new TableColumn<>(displayHeaders.get(col));
        aTableColumn.setCellFactory(list -> {
            return customCellFactory(col);
        });
        aTableColumn.setCellValueFactory(cellData -> {
            Content cellValue = cellData.getValue().get(col);
            return new ReadOnlyObjectWrapper<Content>(cellValue);
        });
        if ((col==0) || (col > numberOfNutrientContraints+2))
            aTableColumn.setEditable(false);
        else
            aTableColumn.setEditable(true); 
        aTableColumn.setOnEditCommit(event -> {
            final Content value = event.getNewValue();
            int row = event.getTablePosition().getRow();
            try {
                double d = value.value;
                event.getTableView().getItems().get(row).set(col, new Content(d));
                Event.fireEvent(event.getTableView(), new SolveItEvent());
            } catch (NumberFormatException nfe) {
                event.getTableView().getItems().get(row).set(col, value);
            }
        });    
        return aTableColumn;
    }

    private TableCell<List<Content>, Content> customCellFactory(int col) {
        if (col == 1) {
            CheckBoxTableCell tcell = new CheckBoxTableCell<List<Content>, Content>() {
                @Override
                public void updateItem(Content item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        CheckBox cb = (CheckBox) getGraphic();
                        if (item.enabled != null)
                            cb.setSelected(item.enabled);
                    }
                }
            };
            return tcell;
        } else {
            TextFieldTableCell<List<Content>, Content> cell = new TextFieldTableCell<List<Content>, Content>() {
                @Override
                public void updateItem(Content item, boolean empty) {
                    super.updateItem(item, empty);
                    var tableRow = getTableRow();
                    if (tableRow != null) {
                        int row = getTableRow().getIndex();
                        // bottom row
                        if ((col == 0) || (row == solveAmountRow) ||
                        // right bottom color
                                ((row >= relationshipRow) && (col <= 1)) ||
                        // rightmost column
                                (col == numberOfNutrientContraints + 3) ||
                        // left bottom corner
                                ((row >= relationshipRow) && (col == priceColumn))) {
                            setEditable(false);
                            this.getStyleClass().add("readonly");
                            if (infeasible) {
                                this.getStyleClass().add("infeasible");
                            } else {
                                this.getStyleClass().remove("infeasible");
                            }
                        }
                    }
                }
            };
            return cell;
        }
    }

    public List<TableColumn<List<Content>, Content>> getTableColumns() {
       
        List<TableColumn<List<Content>, Content>> columns = new ArrayList<TableColumn<List<Content>, Content>>();
        for (int i = 0; i < columnHeaders.size(); i++) {
            TableColumn<List<Content>, Content> stringColumn = createStringColumn(columnHeaders, i);
            columns.add(stringColumn);
        }
       // final int priceColumn = nutrientMap.size() + 1;
      //  columns.get(priceColumn).getStyleClass().add("readonly");
       // columns.get(priceColumn+1).getStyleClass().add("readonly");
        return columns;
    }
    */
}
