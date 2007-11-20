import java.util.ArrayList;
import java.util.List;

/**
 * This class is an example of a data model that implements the
 * Observer pattern and also tries to be thread-safe. Updates to the
 * model are considered taking a long time and are therefore performed
 * in a background thread.
 */
public class DataModel {
    public interface Listener {
        /** Called when the model has been updated. */
        void modelUpdated();
    }

    /** A list of listeners. */
    private List<Listener> mListeners = new ArrayList<Listener>();

    // Some mutable data fields here...

    /**
     * Register a new listener to changes of data in the model.
     *
     * @param listener Listener to subscribe.
     */
    synchronized public void registerListener(Listener listener) {
        mListeners.add(listener);
    }

    /**
     * Unregister a listener.
     *
     * @param listener Listener to unsubscribe.
     */
    synchronized public void unregisterListener(Listener listener) {
        mListeners.remove(listener);
    }

    /**
     * Modify the model's data.
     *
     * Since the update can take a long time, it is performed in a
     * background thread. When the update has been completed, the
     * listeners are notified.
     */
    public void setDataAsynchronously() {
        Thread updateThread = new Thread("modelUpdateThread") {
            public void run() {
                System.out.println("Updating the model");
                // Set data fields here...
                notifyListeners();
            };
        };
        updateThread.start();
    }

    /**
     * Notify listeners that the data has been modified.
     */
    synchronized private void notifyListeners() {
        System.out.println("Notifying listeners");
        for (Listener listener : mListeners) {
            listener.modelUpdated();
        }
    }
}
