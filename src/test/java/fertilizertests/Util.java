package fertilizertests;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
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
    
    public static Object reflectiveGet( Object controller, String name) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
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
    
    public static  void pressGlobalEnterKey() throws AWTException {
        java.awt.Robot awtRobot = new java.awt.Robot();
        awtRobot.keyPress(KeyEvent.VK_ENTER);
        awtRobot.keyRelease(KeyEvent.VK_ENTER);
    }
    
    public static void delDirTree(String testFolder) throws IOException {
        Path testPath =Path.of(testFolder);
        if (!(testPath.toFile().exists()))
            testPath.toFile().mkdirs();
        Files.walk(testPath)
            .filter( path -> path.toFile().isFile())
            .map(path -> path.toFile())
            .forEach(file -> deleteFile(file));
        Files.walk(Path.of(testFolder))
            .map(path -> path.toFile())
            .forEach(file -> deleteFile(file));
     }

    private static void deleteFile(File file) {
        System.out.println(file.getAbsolutePath());
        file.delete();
    }
    
}
