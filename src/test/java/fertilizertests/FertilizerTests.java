package fertilizertests;

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
