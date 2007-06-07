public class SingleThreadedCycle {

    public static void main(String[] args) {
        final Object lock1 = new Object();
        final Object lock2 = new Object();
        synchronized (lock1) {
            synchronized (lock2) {
            }
        }
        synchronized (lock2) {
            synchronized (lock1) {
            }
        }
    }
}
