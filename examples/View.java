class View {
    private final DataModel mDataModel;
    private boolean mFreezed = false;
     
    View(DataModel dataModel) {
       mDataModel = dataModel;
       System.out.println("Registering view in data model");
       dataModel.registerListener(this);
    }

    synchronized void freeze() {
        if (!mFreezed) {
           System.out.println("Freezing view");
           mDataModel.unregisterListener(this);
           mFreezed = true;
       }
    }

    synchronized void thaw() {
       if (mFreezed) {
           System.out.println("Thawing view");
           mDataModel.registerListener(this);
           mFreezed = false;
       }
    }

     synchronized void modelUpdated() {
         // ...
     }
}
