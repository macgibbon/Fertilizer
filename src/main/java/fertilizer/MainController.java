package fertilizer;


import java.awt.Desktop;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

public class MainController implements Initializable {
    
    public static final int SOLUTIONTAB = 0;

    public static final String LAST_USED_FOLDER = "lastUsedFolder";

	@FXML
	TableView<List<Content>> solutiontable;

	@FXML
	TableView<List<String>> requirementstable;

	@FXML
	TableView<List<String>> ingredientstable;

	@FXML
	TableView<List<String>> pricestable;
	
	@FXML
	TableView<List<String>> densitiestable;	

    @FXML
    TableView<Batch> batchtable;

	@FXML
	TabPane tabpane;

	@FXML
	TextArea notes;
	
	@FXML
	Menu menuFile;
	
	@FXML
	TextField textFieldWeight;
	
	@FXML
	TextField textFieldContact;

	@FXML
	MenuItem menuItemVersion;
	
	@FXML
	Label meandensitylabel;
	
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
        Bindings.bindBidirectional(textFieldWeight.textProperty(), model.batchWt, new DecimalFormat("#.0"));        
        textFieldWeight.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                textFieldWeight.fireEvent(new ActionEvent());
        });
        
        Bindings.bindBidirectional(textFieldContact.textProperty(), model.contact);
        textFieldContact.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                textFieldContact.fireEvent(new ActionEvent());
        });
        
        Bindings.bindBidirectional(notes.textProperty(), model.notes);
        menuItemVersion.textProperty().bind(Bindings.concat("Version ", model.version));
        loadDefaultData();
        solutiontable.setEditable(true);
        solutiontable.getSelectionModel().setCellSelectionEnabled(true);
        solutiontable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Json Files", "*.json"));
        textFieldWeight.setOnAction(e -> solve());
        meandensitylabel.textProperty().bind(Bindings.format("%.2f",model.meanDensity));
        solve();
    }

	@SuppressWarnings("unchecked")
    private void loadDefaultData() {
	    var prices = model.priceRows;
		var priceHeaders = new ArrayList<String>(prices.remove(0));
		pricestable.setItems(FXCollections.observableArrayList(prices));
		pricestable.getSelectionModel().setCellSelectionEnabled(true);
		pricestable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		createColumns(priceHeaders, pricestable,1);
		
		var densities = model.densityRows;
        var densityHeaders = new ArrayList<String>(densities.remove(0));
        densitiestable.setItems(FXCollections.observableArrayList(densities));
        densitiestable.getSelectionModel().setCellSelectionEnabled(true);
        densitiestable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        createColumns(densityHeaders, densitiestable,1);

		var ingredients = model.ingredientRows;
		var ingredientHeaders = new ArrayList<String>(ingredients.remove(0));
		ingredientstable.setItems(FXCollections.observableArrayList(ingredients));
		ingredientstable.getSelectionModel().setCellSelectionEnabled(true);
		ingredientstable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		createColumns(ingredientHeaders, ingredientstable,1);
		
		

		var requirements = model.requirementRows;
		var requirementHeaders = new ArrayList<String>(requirements.remove(0));
		requirementstable.setItems(FXCollections.observableArrayList(requirements));
		requirementstable.getSelectionModel().setCellSelectionEnabled(true);
		requirementstable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		createColumns(requirementHeaders, requirementstable,2);
		
	    MatrixBuilder matrix = new MatrixBuilder(prices, requirements, ingredients);
	    solution = new SolutionModel(matrix);
	    solutiontable.setItems(model.soulutionTableList);
	    
	    batchtable.setItems(model.batchTableList);
	    TableColumn<Batch, String> colummn1 = new TableColumn<>(Batch.tableHeaders[0]);
	    colummn1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));

	    TableColumn<Batch, String> colummn2 = new TableColumn<>(Batch.tableHeaders[1]);
        colummn2.setCellValueFactory(data -> Bindings.format("%.2f",data.getValue().density()));  
	    
	    TableColumn<Batch, String> colummn3 = new TableColumn<>(Batch.tableHeaders[2]);
	    colummn3.setCellValueFactory(data -> Bindings.format("%.2f",data.getValue().percent()));  
       
        TableColumn<Batch, String> colummn4 = new TableColumn<>((Batch.tableHeaders[3]));
        colummn4.setCellValueFactory(data ->  Bindings.format("%.2f",data.getValue().amount()));  
      
        TableColumn<Batch, String> colummn5 = new TableColumn<>((Batch.tableHeaders[4]));
        colummn5.setCellValueFactory(data ->  Bindings.format("%.0f",data.getValue().scale()));  
        batchtable.getColumns().addAll(colummn1,colummn2,colummn3,colummn4,colummn5);
	}

    private void loadSolutionsFromModel(SolutionModel model) {
        solutiontable.getColumns().clear();  
        solutiontable.getItems().clear();;
 		solutiontable.getItems().addAll(solution.getItems());
 		int columns = model.getItems().get(0).size();
 		for (int i = 0; i < columns; i++) {
 		   solutiontable.getColumns().add(model.getTableColumn(i));
        }       
    }	

    private void createColumns(ArrayList<String> displayHeaders, TableView<List<String>> tableView, int strcolcount) {
        tableView.getColumns().clear();
        int nCols = displayHeaders.size();
        for (int i = 0; i < strcolcount; i++) {
            TableColumn<List<String>, String> sTableColumn = createStringColumn(displayHeaders, i);
            tableView.getColumns().add(sTableColumn);
        }
        for (int i = strcolcount; i < nCols; i++) {
            TableColumn<List<String>, Double> dTableColumn = createDoubleColumn(displayHeaders, i);
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

	public void solve()  {
	    solutiontable.getColumns().clear();  
		solution.calculateSolution();
		solution.calculateBatch();
		
		solutiontable.getItems().clear();
		solutiontable.getItems().addAll(solution.getItems());
        int columns = solution.getItems().get(0).size();
        for (int i = 0; i < columns; i++) {
           solutiontable.getColumns().add(solution.getTableColumn(i));
        }        
	}
	
    public void save() throws JsonIOException, IOException {
        // Load the last used directory
        File lastUsedDirectory = new File(model.preferences.get(LAST_USED_FOLDER, model.appDir.getAbsolutePath()));
        lastUsedDirectory.mkdirs();
        fileChooser.setInitialDirectory(lastUsedDirectory); 

        // Show the save file dialog
        File file = fileChooser.showSaveDialog((Stage) solutiontable.getScene().getWindow()); 
        if (file != null) {
            // Save the directory of the chosen file
            model.preferences.put(LAST_USED_FOLDER, file.getParent());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(file);
            try {
                gson.toJson(solution.getAsSolutionModel(), writer);
            } finally {
                writer.close();
            }
        }      
    }
	
    public void load() throws JsonSyntaxException, JsonIOException, IOException {
        // Load the last used directory
        File lastUsedDirectory = new File(model.preferences.get(LAST_USED_FOLDER, model.appDir.getAbsolutePath()));
        lastUsedDirectory.mkdirs();
        fileChooser.setInitialDirectory(lastUsedDirectory); 

        // Show the save file dialog
        File file = fileChooser.showOpenDialog((Stage) solutiontable.getScene().getWindow());
        if (file != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileReader jsonReader = new FileReader(file);
            try {
                PersistanceModel pmodel = gson.fromJson(jsonReader, PersistanceModel.class);
                solution = new SolutionModel(pmodel);
            } finally {
                jsonReader.close();
            }
          
            loadSolutionsFromModel(solution);
          
        }
        tabpane.getSelectionModel().select(SOLUTIONTAB);
        solve();
    }
    
	public void printTable() throws Exception {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String filename = String.format("Fertilizer%s.pdf", formatter.format(LocalDateTime.now()));
		File outFile = new File(model.appDir, filename);
		var items = solutiontable.getItems();
		String[] headers = solutiontable.getColumns().stream().map(column -> column.getText()).toArray(String[]::new);
		TablePdf pdf = new TablePdf(items, headers);
		pdf.write(outFile);

		PDDocument document = Loader.loadPDF(outFile);
		try {
			// Create a PrinterJob
			PrinterJob job = PrinterJob.getPrinterJob();
			// Set the PDF document as the printable object
			job.setPageable(new PDFPageable(document));
			job.print();
			// Close the document
        } catch (PrinterAbortException e) {
            // User cancelled the print job
        }  finally {
			document.close();
		}	
	}
    
	public void printMixsheet() throws Exception {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String filename = String.format("MixSheet%s.pdf", formatter.format(LocalDateTime.now()));
		File outFile = new File(model.appDir, filename);
		MixSheetPdf pdf = new MixSheetPdf(model, solution);
		pdf.write(outFile);

		PDDocument document = Loader.loadPDF(outFile);
		try {
			// Create a PrinterJob
			PrinterJob job = PrinterJob.getPrinterJob();
			// Set the PDF document as the printable object
			job.setPageable(new PDFPageable(document));
			job.print();
			// Close the document
        } catch (PrinterAbortException e) {
            // User cancelled the print job
		} finally {
			document.close();
		}
	}
    
	
    public void browseProgram() throws IOException {
        Desktop.getDesktop().open(new File(System.getProperty("user.dir")));
    }
    
    public void browseData() throws IOException {
        Desktop.getDesktop().open(model.appDir);
    }


}
