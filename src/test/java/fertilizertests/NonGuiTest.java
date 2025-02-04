package fertilizertests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import fertilizer.MainApp;

class NonGuiTest {

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
    void testErrorDialogException() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method showErrorDialogMethod = Stream.of(MainApp.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("showErrorDialog"))
                .findFirst().get();
        showErrorDialogMethod.setAccessible(true);
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
