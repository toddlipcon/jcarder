public class Philosopher extends Thread {
    private final Chopstick mLeftChopstick;
    private final Chopstick mRightChopstick;

    public Philosopher(String name,
                       Chopstick leftChopstick,
                       Chopstick rightChopstick) {
        super(name);
        mLeftChopstick = leftChopstick;
        mRightChopstick = rightChopstick;
    }

    public void run() {
        pickUpSticksAndEat();
    }

    private void pickUpSticksAndEat() {
        synchronized (mLeftChopstick) {
            synchronized (mRightChopstick) {
                System.err.println(getName() + " is eating.");

            }
        }
    }
}
