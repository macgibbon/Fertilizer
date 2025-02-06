package fertilizer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.math4.legacy.optim.PointValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

public class MainController implements Initializable {
    
    private static final String LAST_USED_FOLDER = "lastUsedFolder";

	@FXML
	TableView<List<Content>> solutiontable;

	@FXML
	TableView<List<String>> requirementstable;

	@FXML
	TableView<List<String>> ingredientstable;

	@FXML
	TableView<List<String>> pricestable;

	@FXML
	TabPane tabpane;

	@FXML
	TextArea textarea;
	
	@FXML
	Menu menuFile;
	
    FileChooser fileChooser;
    Model model;
    SolutionModel solution;
    
    public MainController(Model model) {
        super();
        this.model = model;
    }
   
	@SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDefaultData();
        solutiontable.setEditable(true);
        solutiontable.getSelectionModel().setCellSelectionEnabled(true);
        solutiontable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        solutiontable.getSelectionModel().selectedItemProperty().addListener((o, oldSelection, newSelection) -> {
            var selectedCells = solutiontable.getSelectionModel().getSelectedCells();
            if (selectedCells.size() > 0) {
                var selectedCell = selectedCells.get(0);
                solutiontable.edit(selectedCell.getRow(), selectedCell.getTableColumn());
            }
        });
       fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().add(new ExtensionFilter("Json Files", "*.json"));      
    }

	private void loadDefaultData() {
	    var prices = model.priceRows;
		var priceHeaders = new ArrayList<String>(prices.remove(0));
		pricestable.setItems(FXCollections.observableArrayList(prices));
		pricestable.getSelectionModel().setCellSelectionEnabled(true);
		pricestable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		createColumns(priceHeaders, pricestable);

		var ingredients = model.ingredientRows;
		var ingredientHeaders = new ArrayList<String>(ingredients.remove(0));
		ingredientstable.setItems(FXCollections.observableArrayList(ingredients));
		ingredientstable.getSelectionModel().setCellSelectionEnabled(true);
		ingredientstable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		createColumns(ingredientHeaders, ingredientstable);

		var requirements = model.requirementRows;
		var requirementHeaders = new ArrayList<String>(requirements.remove(0));
		requirementstable.setItems(FXCollections.observableArrayList(requirements));
		requirementstable.getSelectionModel().setCellSelectionEnabled(true);
		requirementstable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		createColumns(requirementHeaders, requirementstable);
		
	    MatrixBuilder matrix = new MatrixBuilder(prices, requirements, ingredients);
	    solution = new SolutionModel(matrix.getNutrientMap(), matrix.getIngredientMap(), matrix.getAnalysisMatrixs());
	    loadSolutionsFromModel(solution);
	}

    private void loadSolutionsFromModel(SolutionModel model) {
        solutiontable.getColumns().clear();  
        solutiontable.setItems(FXCollections.emptyObservableList());
 		solutiontable.setItems(FXCollections.observableArrayList(model.getItems()));
        solutiontable.getColumns().addAll(model.getTableColumns());
    }	

	private void createColumns(ArrayList<String> displayHeaders, TableView<List<String>> tableView) {
		tableView.getColumns().clear();
		int nCols = displayHeaders.size();
		int ingredientNameColumn = 0;
		TableColumn<List<String>, String> aTableColumn = createStringColumn(displayHeaders, ingredientNameColumn);
		tableView.getColumns().add(aTableColumn);
		for (int i = 1; i < nCols; i++) {
			TableColumn<List<String>, Double> dTableColumn = createDoubleColumn(displayHeaders, i);
			dTableColumn.setEditable(true);
			tableView.getColumns().add(dTableColumn);
		}
	}

    private TableColumn<List<String>, String> createStringColumn(ArrayList<String> displayHeaders, int column) {
        final int col = column;
        TableColumn<List<String>, String> aTableColumn = new TableColumn<>(displayHeaders.get(column));
        aTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        aTableColumn.setCellValueFactory(cellData -> {
            String cellValue = "";
            cellValue = cellData.getValue().get(col);
            return new ReadOnlyStringWrapper(cellValue);
        });
        return aTableColumn;
    }

	private TableColumn<List<String>, Double> createDoubleColumn(ArrayList<String> displayHeaders, int column) {
		final int col = column;
		TableColumn<List<String>, Double> aTableColumn = new TableColumn<>(displayHeaders.get(column));
		aTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		aTableColumn.setPrefWidth(100.0);
		aTableColumn.setCellValueFactory(cellData -> {
    			double cellValue = 0.0;
    			try {
    				cellValue = Double.parseDouble(cellData.getValue().get(col));
    			} catch (Throwable e) {
    			}
    			return new SimpleObjectProperty<Double>(cellValue);
    		}
		);
		return aTableColumn;
	}

	public void solve() throws IOException {
		PointValuePair result =  solution.calculateSolution();
		System.out.println(result.getValue());
	//	updateTable(result);
		tabpane.getSelectionModel().select(3);
		solutiontable.setItems(FXCollections.observableArrayList(solution.getItems()));
        solutiontable.getColumns().clear();
        solutiontable.getColumns().addAll(solution.getTableColumns());
	}

	

	
    public void save() throws JsonIOException, IOException {
        File userDir = new File(System.getProperty("user.home"));
        File appDir = new File(userDir, ".fertilizer");

        // Load the last used directory
        String lastUsedDirectory = model.preferences.get(LAST_USED_FOLDER, appDir.getAbsolutePath());
        fileChooser.setInitialDirectory(new File(lastUsedDirectory)); 

        // Show the save file dialog
        File file = fileChooser.showSaveDialog((Stage) solutiontable.getScene().getWindow()); 
        if (file != null) {
            // Save the directory of the chosen file
            model.preferences.put(LAST_USED_FOLDER, file.getParent());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(file);
            try {
                gson.toJson(solution, writer);
            } finally {
                writer.close();
            }
        }
    }
	
    public void load() throws JsonSyntaxException, JsonIOException, IOException {
        File userDir = new File(System.getProperty("user.home"));
        File appDir = new File(userDir, ".fertilizer");

        // Load the last used directory
        String lastUsedDirectory = model.preferences.get(LAST_USED_FOLDER, appDir.getAbsolutePath());
        fileChooser.setInitialDirectory(new File(lastUsedDirectory)); 

        // Show the save file dialog
        File file = fileChooser.showOpenDialog((Stage) solutiontable.getScene().getWindow());
        if (file != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileReader jsonReader = new FileReader(file);
            try {
                solution = gson.fromJson(jsonReader, SolutionModel.class);
            } finally {
                jsonReader.close();
            }
            solutiontable.setItems(FXCollections.emptyObservableList());
            solutiontable.getColumns().clear();
            loadSolutionsFromModel(solution);
            tabpane.getSelectionModel().select(3);
        }
    }
	

}
