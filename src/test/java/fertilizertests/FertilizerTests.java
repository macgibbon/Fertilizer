package fertilizertests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import fertilizer.MainApp;

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
    void testMaxOutErrorCount() {
    }
    
    @Test
    void testErrorDialogException() {
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
