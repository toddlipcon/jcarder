package modelview;
public class View implements DataModel.Listener {
    private DataModel mDataModel;
    private boolean mFrozen = false;

    public View(DataModel dataModel) {
       mDataModel = dataModel;
       System.out.println("Registering view in data model");
       dataModel.registerListener(this);
    }

    synchronized void freeze() {
        if (!mFrozen) {
           System.out.println("Freezing view");
           mDataModel.unregisterListener(this);
           mFrozen = true;
       }
    }

    synchronized void thaw() {
       if (mFrozen) {
           System.out.println("Thawing view");
           mDataModel.registerListener(this);
           mFrozen = false;
       }
    }

     synchronized public void modelUpdated() {
         // ...
     }
}
