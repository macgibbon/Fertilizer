package fertilizer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.math3.optim.PointValuePair;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

public class MainController implements Initializable {

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

	ArrayList<ArrayList<String>> ingredientRows, priceRows, requirementRows, mergedMatrix;

	Path pricesPath, ingredientsPath, requirementsPath;

	private Model model;

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
    }

	private void loadDefaultData() {
		pricesPath = Path.of("defaultPrices.csv");
		priceRows = readCsvfile(pricesPath);
		ingredientsPath = Path.of("defaultIngredients.csv");
		ingredientRows = readCsvfile(ingredientsPath);
		requirementsPath = Path.of("defaultRequirements.csv");
		requirementRows = readCsvfile(requirementsPath);
	    MatrixBuilder matrix = new MatrixBuilder(priceRows, requirementRows, ingredientRows);
        model = new Model(matrix.getNutrientMap(), matrix.getIngredientMap(), matrix.getAnalysisMatrixs());
 
		var priceHeaders = new ArrayList<String>(priceRows.remove(0));
		pricestable.setItems(FXCollections.observableArrayList(priceRows));
		pricestable.getSelectionModel().setCellSelectionEnabled(true);
		pricestable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		createColumns(priceHeaders, pricestable);

		var ingredientHeaders = new ArrayList<String>(ingredientRows.remove(0));
		ingredientstable.setItems(FXCollections.observableArrayList(ingredientRows));
		ingredientstable.getSelectionModel().setCellSelectionEnabled(true);
		ingredientstable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		createColumns(ingredientHeaders, ingredientstable);

		var requirementHeaders = new ArrayList<String>(requirementRows.remove(0));
		requirementstable.setItems(FXCollections.observableArrayList(requirementRows));
		requirementstable.getSelectionModel().setCellSelectionEnabled(true);
		requirementstable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		createColumns(requirementHeaders, requirementstable);
		solutiontable.setItems(FXCollections.observableArrayList(model.getItems()));
        solutiontable.getColumns().clear();
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
		PointValuePair result =  model.calculateSolution();
		System.out.println(result.getValue());
	//	updateTable(result);
		tabpane.getSelectionModel().select(3);
		solutiontable.setItems(FXCollections.observableArrayList(model.getItems()));
        solutiontable.getColumns().clear();
        solutiontable.getColumns().addAll(model.getTableColumns());
	}

	public ArrayList<ArrayList<String>> readCsvfile(Path ingredientsPath) {
		ArrayList<ArrayList<String>> lines;
		try {
			lines = Files.lines(ingredientsPath)
			        .filter(line -> line.length() != 0)
			        .map(line -> line.split(","))
					.map(array -> new ArrayList<String>(Arrays.asList(array)))
					.collect(Collectors.toCollection((ArrayList::new)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return lines;
	}

	public void editRequirements() throws IOException {
		Desktop.getDesktop().open(requirementsPath.toFile());
	}

	public void editPrices() throws IOException {
		Desktop.getDesktop().open(pricesPath.toFile());
	}

	public void editAnalysis() throws IOException {
		Desktop.getDesktop().open(ingredientsPath.toFile());
	}

}
