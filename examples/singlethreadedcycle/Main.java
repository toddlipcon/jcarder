package singlethreadedcycle;
public class Main {
    public static void main(String[] args) {
        Object lock1 = new Object();
        Object lock2 = new Object();
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
