package fertilizertests;


import java.awt.event.KeyEvent;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import fertilizer.MainApp;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TestGui extends MainApp {

    @Start
    void onStart(Stage primaryStage) throws Exception {
        super.start(primaryStage);
    }

    @AfterAll
    public static void cleanup() {
        // Platform.exit();
    }

    private void delay(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void solve(FxRobot robot) throws Exception {
        delay(5);
        robot.clickOn("Action");
        robot.clickOn("Calculate least cost solution");
        delay(5);
        editAnalysis();
        delay(15);
        robot.closeCurrentWindow();
    }

    void editAnalysis() {
        try {
            MainApp.controller.editAnalysis();
            delay(5);
            java.awt.Robot awtRobot = new java.awt.Robot();
            awtRobot.keyPress(KeyEvent.VK_ESCAPE);
            awtRobot.keyRelease(KeyEvent.VK_ESCAPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            MainApp.controller.editPrices();
            delay(5);
            java.awt.Robot awtRobot = new java.awt.Robot();
            awtRobot.keyPress(KeyEvent.VK_ESCAPE);
            awtRobot.keyRelease(KeyEvent.VK_ESCAPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            MainApp.controller.editRequirements();
            delay(5);
            java.awt.Robot awtRobot = new java.awt.Robot();
            awtRobot.keyPress(KeyEvent.VK_ESCAPE);
            awtRobot.keyRelease(KeyEvent.VK_ESCAPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}