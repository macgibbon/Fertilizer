package fertilizertests;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import fertilizer.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TestGui{

   

    @Start
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/fertilizer/MainView.fxml"));
        primaryStage.setTitle("Fertilizer formulator");
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        double width = primScreenBounds.getWidth();
        double height = primScreenBounds.getHeight();
        primaryStage.setX(width / 8.0);
        primaryStage.setY(height / 8.0);
        Scene scene = new Scene(root, width * 0.75, height * 0.75);
        scene.getStylesheets().add(MainApp.class.getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    @Test
    void solve(FxRobot robot) {
    robot.clickOn("Action");
    robot.clickOn("Calculate least cost solution");
    
    }

}