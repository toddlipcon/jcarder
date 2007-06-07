import java.util.ArrayList;
import java.util.List;

class DataModel {
    private final List<View> mListeners = new ArrayList<View>();

    synchronized void registerListener(View view) {
        mListeners.add(view);
    }

    synchronized void unregisterListener(View view) {
        mListeners.remove(view);
    }

    synchronized void notifyListeners() {
        System.out.println("Notifying listeners");
        for (View view: mListeners) {
            view.modelUpdated();
        }
    }

    void doSomeAsynchronousModelUpdate() {
        new Thread("asynchronousThread") {
           public void run() {
               System.out.println("Performing some update");
               // ...
               notifyListeners();
           };
        }.start();
    }
}
