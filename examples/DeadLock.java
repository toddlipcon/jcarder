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
        dataModel.setDataAsynchronously();
        Thread.sleep(timeToSleep);

        // For some (here unspecified) reason, we want to freeze view2
        // so that it doesn't receive any notifications about changed
        // data.
        view2.freeze();

        // Now we want notifications about updates again.
        view2.thaw();

        System.out.println("Main thread finished");
    }
}
