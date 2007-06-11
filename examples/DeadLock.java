public class DeadLock {
    public static void main(String[] args) throws Exception {
        int timeToSleep;
        if (args.length > 0) {
            timeToSleep = Integer.parseInt(args[0]);
        } else {
            timeToSleep = 0;
        }
        DataModel dataModel = new DataModel();
        @SuppressWarnings("unused")
        View view1 = new View(dataModel);
        View view2 = new View(dataModel);
        dataModel.doSomeAsynchronousModelUpdate();
        Thread.sleep(timeToSleep);
        view2.freeze();
        view2.thaw();
        System.out.println("Main thread finished");
    }
}
