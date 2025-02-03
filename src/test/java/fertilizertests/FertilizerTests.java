package fertilizertests;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import fertilizer.Content;
import fertilizer.MainApp;
import fertilizer.MainController;
import fertilizer.Model;
import static fertilizertests.Util.*;

class FertilizerTests {

    @Test
    void testMainLauncher() {
        Throwable t = null;
        try {
            Thread launcherThread = new Thread(() ->  startup());
            launcherThread.start();
            MainApp.main(new String[0]);   
           } catch (Throwable t1) {
            t = t1;
        }
        assert (t == null);

    }

    @Test
    void testbadError() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
       Method showErrorMethod = Stream.of(MainApp.class.getDeclaredMethods())
               .filter(method -> method.getName().equals("showError"))
               .findFirst().get();
       showErrorMethod.setAccessible(true);
       NoClassDefFoundError ncdfe = new NoClassDefFoundError();
       NoSuchMethodError nsme = new NoSuchMethodError();
       Object[] args = new Object[] { new Thread(), ncdfe };
       showErrorMethod.invoke(null, args);
       Object[] args2 = new Object[] { new Thread(), nsme };
       showErrorMethod.invoke(null, args2);
       
       
      
       
       
    }
    
   
    @Test
    void testErrorDialogException() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method showErrorDialogMethod = Stream.of(MainApp.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("showErrorDialog"))
                .findFirst().get();
        showErrorDialogMethod.setAccessible(true);
        RuntimeException re = new RuntimeException("Exception for testing");
        Object[] args = new Object[] { new Thread(), re };
        for (int i = 0; i <11; i++) {           
            showErrorDialogMethod.invoke(null, args);
        }
        
        Object[] args2 = new Object[] { new Thread(), null };
            
            showErrorDialogMethod.invoke(null, args2);

    }
    
    
    

    private void delay()  {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
    }

    private void startup() {
        delay();
        MainApp.close();
    }
}
