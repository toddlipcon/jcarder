package philosophers;
public class Main {
    public static void main(String[] args) throws Exception {
        Chopstick stick1 = new Chopstick();
        Chopstick stick2 = new Chopstick();
        Chopstick stick3 = new Chopstick();
        Chopstick stick4 = new Chopstick();
        Chopstick stick5 = new Chopstick();

        Chopstick stick6 = new Chopstick();
        Chopstick stick7 = new Chopstick();

        Philosopher phil1 = new Philosopher("Philosopher1", stick1, stick2);
        Philosopher phil2 = new Philosopher("Philosopher2", stick2, stick3);
        Philosopher phil3 = new Philosopher("Philosopher3", stick3, stick4);
        Philosopher phil4 = new Philosopher("Philosopher4", stick4, stick5);
        Philosopher phil5 = new Philosopher("Philosopher5", stick5, stick1);

        Philosopher phil6 = new Philosopher("PhilosopherWithOwnSticks",
                                            stick6,
                                            stick7);

        phil1.start();
        phil1.join();
        phil2.start();
        phil2.join();
        phil3.start();
        phil3.join();
        phil4.start();
        phil4.join();
        phil5.start();

        phil6.start();
    }
}
