package fertilizer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

    static Stage currentStage;
    protected static MainController controller;

    @Override
	public void start(Stage primaryStage) throws Exception {
        currentStage = primaryStage;
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> showError(t,e)); 
        URL resource = MainApp.class.getResource("/fertilizer/MainView.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Pane  root = (Pane) loader.load();
        controller = loader.getController();
		primaryStage.setTitle("Fertilizer formulator");
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		double width = primScreenBounds.getWidth();
		double height = primScreenBounds.getHeight();
		primaryStage.setX(width / 8.0);
		primaryStage.setY(height / 8.0);		
		Scene scene = new Scene(root, width * 0.75, height * 0.75);
		scene.getStylesheets().add(getClass().getResource("/fertilizer/styles.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
    
	public static void main(String[] args) {
		launch(args);
	}

	public static void showError(Thread t, Throwable e) {
		if ((e instanceof NoClassDefFoundError))
			return;
		if (e instanceof NoSuchMethodError)
			return;		
		e.printStackTrace();
	    Platform.runLater(() -> {
	        showErrorDialog(t, e);
	    });
	}

	private volatile static int errorCount;
	
    private static void showErrorDialog(Thread t, Throwable e) {
        try {
        	errorCount++;
        	if (errorCount > 10)
        		return;
            // Create a new dialog
            // Create a dialog to display the exception
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Unhandled Exception");
            alert.setHeaderText("An unhandled exception occurred");

            // Create a TextArea to display the stack trace
            TextArea textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setWrapText(true);

            // Get the stack trace as a string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            // Set the stack trace in the TextArea
            textArea.setText(stackTrace);

            BorderPane pane = new BorderPane();
            pane.setCenter(textArea);
            
            final Stage stage = new Stage();

            double x = currentStage.getX()+75.0;
            double y = currentStage.getY()+75.0;
            stage.setX(x);
            stage.setY(y);
            stage.initModality(Modality.NONE);
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            double width = primScreenBounds.getWidth();
            double height = primScreenBounds.getHeight();
      
            Scene scene = new Scene(pane, width * 0.65, height * 0.65);
            stage.setScene(scene);
            stage.show();
        } catch (Throwable x) {           
            x.printStackTrace();
        }
    }

    public static void close() {
        Platform.runLater(() -> currentStage.close()); 
    }
}
