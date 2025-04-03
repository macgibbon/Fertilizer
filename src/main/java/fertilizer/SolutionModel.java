package fertilizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
import javafx.event.Event;
import javafx.scene.control.TableColumn;

public class SolutionModel {
    
    private static final double objectiveConstant = 0.0;

    // Inputs as collections for easier insertion and deletion
    private LinkedHashMap<String, Double> ingredientMap;
 
    private LinkedHashMap<String, Double> nutrientMap;
    private ArrayList<ArrayList<Double>> coefficients;
    private LinkedHashMap<String,Boolean> enableMap;
    private LinkedHashMap<String, Relationship> constraintMap;
    // Output as arrays for simplicity and easier interface with LP optimization
    // library
    private double[] solutionIngredientAmounts;
    private String solutionPrice;


    private double[] solutionNutrientAmounts;
    private String solutionTotal;
    private AtomicBoolean infeasible = new AtomicBoolean(false);
    // members for displaying in tableView
    private List<String> rowHeaders;
    int nameColumn;

    int enableColumn;
    int startAnalysisColumn;
    int priceColumn;
    int solutionColumn;
    private List<List<Content>> cachedItems;
    private ArrayList<String> columnHeaders;
    private List<Relationship> constraintRelationsShips;
    private double totalAmount;
    private String standardDescription;

	public SolutionModel(MatrixBuilder matrix) {
        this.ingredientMap = matrix.getIngredientMap();
        this.nutrientMap = matrix.getNutrientMap();
        this.coefficients = matrix.getAnalysisMatrixs();
        this.constraintMap = matrix.getConstraintMap();
        this.enableMap = matrix.getEnableMap();
        initMembersForTableView();
    }

    public SolutionModel(PersistanceModel pm) {
        super();
        this.ingredientMap = pm.ingredientMap;
        this.nutrientMap = pm.nutrientMap;
        this.coefficients = pm.coefficients;
        this.enableMap = pm.enableMap;
        this.constraintMap = pm.constraintMap;
        initMembersForTableView();
    }

    public void calculateSolution() {
        infeasible.set(false);
        double[] nutrientAmounts = nutrientMap.values().stream().mapToDouble(D -> D.doubleValue()).toArray();
        Collection<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < nutrientAmounts.length; i++) {
            double[] constraintCoefficients = coefficients.get(i).stream().mapToDouble(D -> D.doubleValue()).toArray();
            double constraint = nutrientAmounts[i];
            Relationship r = constraintRelationsShips.get(i);
            constraints.add(new LinearConstraint(constraintCoefficients, r, constraint));
        }
        
        List<Boolean> enableList = enableMap.values().stream().collect(Collectors.toCollection(ArrayList<Boolean>::new));
        for (int i = 0; i < enableList.size(); i++) {
            boolean addConstraint = !enableList.get(i);
            if (addConstraint) {
                double[] newConstraints = new double[enableList.size()];
                newConstraints[i] = 1.00;
                constraints.add(new LinearConstraint(newConstraints, Relationship.EQ, 0.0));
            }
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
            //System.out.println(solutionPrice);
            solutionNutrientAmounts = new double[nutrientMap.size()];
            totalAmount = 0.0;
            for (int i = 0; i < solutionIngredientAmounts.length; i++) {
                double amount = points[i];
                solutionIngredientAmounts[i] = amount;
                totalAmount += amount;
                for (int j = 0; j < nutrientMap.size(); j++) {
                    double analysis = coefficients.get(j).get(i);
                    solutionNutrientAmounts[j] += amount * analysis;
                }
            }
            solutionTotal = String.format("%.0f lbs", totalAmount);
        } catch (NoFeasibleSolutionException n) {
            infeasible.set(true);
            solutionPrice = "!$!!!!!";
            solutionTotal = "!!!!! lbs";
            for (int i = 0; i < numberOfIngredients; i++) {
                solutionIngredientAmounts[i] = 0;
            }
            for (int j = 0; j < solutionNutrientAmounts.length; j++) {
                solutionNutrientAmounts[j] = 0;
            }
        }
        updateSolutionItems();
    }

    public PersistanceModel getAsSolutionModel() {
        return new PersistanceModel(ingredientMap, nutrientMap, coefficients, enableMap, constraintMap);    
    }

    public LinkedHashMap<String, Double> getIngredientMap() {
        return ingredientMap;
    }

    public List<List<Content>> getItems() {
        return cachedItems;
    }

    public double[] getSolutionIngredientAmounts() {
        return solutionIngredientAmounts;
    }

    public String getStandardDescription() {
        return standardDescription;
    }

    public TableColumn<List<Content>, Content> getTableColumn(int column) {
        var aTableColumn = new TableColumn<List<Content>, Content>(columnHeaders.get(column));
        aTableColumn.setCellFactory(list -> new ContentTableCell(infeasible));
        aTableColumn.setCellValueFactory(cellData -> {
            Content content = cellData.getValue().get(column);
            return new ReadOnlyObjectWrapper<Content>(content);
        });
        aTableColumn.setEditable(true);
        aTableColumn.setOnEditCommit(event -> {
            final Content value = event.getNewValue();
            // System.out.println("Table edit commit " + event.getNewValue());
            int row = event.getTablePosition().getRow();
            // double d = value.value;
            event.getTableView().getItems().get(row).set(column, value);
            writeThroughCache(row, column, value);
            Event.fireEvent(event.getTableView(), new SolveItEvent());
        });
        return aTableColumn;
    }
            
    public double getTotalAmount() {
		return totalAmount;
	}

	private void initMembersForTableView() {
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
        List<Boolean> enableList = enableMap.values().stream().collect(Collectors.toCollection(ArrayList<Boolean>::new));

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

        
        String[] npkheader = {"N","P","K"};
        List<String> nutrientNames = nutrientMap.keySet().stream().toList();
        String[] reportHeader = Stream.of(npkheader)
              .map(n ->  nutrientNames.indexOf(n) )
              .mapToDouble(index -> solutionNutrientAmounts[index])
              .mapToObj(d-> String.format("%.0f", d))
              .toArray(String[]::new);
        StringBuilder sb = new StringBuilder(reportHeader[0]);
        for (int i = 1; i < reportHeader.length; i++) {
            sb.append("-");
            sb.append(reportHeader[i]);
        }
        standardDescription = sb.toString();   

    }
    
    private void writeThroughCache(int row, int column, Content content) {
        Celltype celltype = content.celltype;
        switch (celltype) {
        case analysis:
        //     Double a = coefficients.get(column - 2).get(row);
            coefficients.get(column - startAnalysisColumn).set(row, content.value);
            break;
        case constraintAmount:
            String nutrient = nutrientMap.keySet().toArray(new String[0])[column - startAnalysisColumn];
        //    Double c = nutrientMap.get(nutrient);
            nutrientMap.put(nutrient, content.value);
            break;
        case price:
            String ingredient = ingredientMap.keySet().toArray(new String[0])[row];
        //    Double cp = ingredientMap.get(ingredient);
            ingredientMap.put(ingredient, content.value);
            break;
        case enable:           
        //    Boolean b = enableList.get(row);
            String ingredient2 = ingredientMap.keySet().toArray(new String[0])[row];
            Boolean newB = content.enabled;
            enableMap.put(ingredient2, newB);
            break;
           
        case relationship:
        //    Relationship r = constraintRelationsShips.get(column - 2);
            Relationship r2 = Relationship.valueOf(content.toString());
            constraintRelationsShips.set(column - startAnalysisColumn, r2);
            break;
        default:
            throw new IllegalArgumentException("Unexpected value: " + celltype);
        }
    }
 
}
