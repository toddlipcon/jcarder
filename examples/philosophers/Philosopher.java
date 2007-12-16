package philosophers;

public class Philosopher extends Thread {
    private Chopstick mLeftChopstick;
    private Chopstick mRightChopstick;

    public Philosopher(String name,
                       Chopstick leftChopstick,
                       Chopstick rightChopstick) {
        super(name);
        mLeftChopstick = leftChopstick;
        mRightChopstick = rightChopstick;
    }

    public void run() {
        long endTime = System.currentTimeMillis() + 2000;
        while (endTime > System.currentTimeMillis()) {
            pickUpSticksAndEat();
        }
    }

    private void pickUpSticksAndEat() {
        synchronized (mLeftChopstick) {
            synchronized (mRightChopstick) {
                System.out.println(getName() + " is eating.");
            }
        }
    }
}
