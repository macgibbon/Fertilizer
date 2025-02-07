package fertilizertests;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import fertilizer.MainController;

public class Util {   
    
    
    public static void reflectiveSet(Object badModel, MainController controller, String name) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = controller.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, badModel);        
    }
    
    public static Object reflectiveGet( MainController controller, String name) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = controller.getClass().getDeclaredField(name);
        field.setAccessible(true);
       return  field.get(controller);        
    }

    public static void delay(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
        }
    }

    public static  void pressGlobalExitKey() throws AWTException {
        java.awt.Robot awtRobot = new java.awt.Robot();
        awtRobot.keyPress(KeyEvent.VK_ESCAPE);
        awtRobot.keyRelease(KeyEvent.VK_ESCAPE);
    }
    
    public static void delDirTree(String testFolder) throws IOException {
        Files.walk(Path.of(testFolder))
            .filter( path -> path.toFile().isFile())
            .map(path -> path.toFile())
            .forEach(file -> file.delete());
        Files.walk(Path.of(testFolder))
            .map(path -> path.toFile())
            .forEach(file -> file.delete());
    }
    
    
}
