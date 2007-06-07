import java.util.ArrayList;
import java.util.List;

public class DataModel {
    private List<View> mListeners = new ArrayList<View>();

    synchronized public void registerListener(View view) {
        mListeners.add(view);
    }

    synchronized public void unregisterListener(View view) {
        mListeners.remove(view);
    }

    synchronized public void notifyListeners() {
        System.out.println("Notifying listeners");
        for (View view : mListeners) {
            view.modelUpdated();
        }
    }

    public void doSomeAsynchronousModelUpdate() {
        Thread updateThread = new Thread("modelUpdateThread") {
           public void run() {
               System.out.println("Performing some update");
               // ...
               notifyListeners();
           };
        };
        updateThread.start();
    }
}
