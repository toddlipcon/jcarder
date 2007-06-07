public class DeadLock {
    public static void main(String[] args) throws Exception {
        DataModel dataModel = new DataModel();
        @SuppressWarnings("unused")
        View view1 = new View(dataModel);
        View view2 = new View(dataModel);
        dataModel.doSomeAsynchronousModelUpdate();
        Thread.sleep(Integer.parseInt(args[0]));
        view2.freeze();
        view2.thaw();
        System.out.println("Main thread finished");
    }
}
