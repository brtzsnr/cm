import java.util.Random;

/* Defines the Samaritan */
public class Samaritan implements Runnable {
  private static Player[] players;
  private static Resource[] resources;
  private static Random random = new Random();

  public synchronized void run() {
    while (true) {
      // Picks two resources and makes them available
      Resource r1 = resources[random.nextInt(resources.length)];
      Resource r2 = resources[random.nextInt(resources.length)];

      if (r1 != r2) {
        r1.setAvailable(true);
        r2.setAvailable(true);

        // Wakes hungry Players
        notifyAll();
        try {
          // Waits for players to finish building
          wait();
        } catch (InterruptedException e) {
          /* Ignored */
        }
      }
    }
  }

  public static void main(String[] args) {
    Samaritan samaritan = new Samaritan();

    resources = new Resource[3];
    resources[0] = new Resource();
    resources[1] = new Resource();
    resources[2] = new Resource();

    players = new Player[3];
    players[0] = new Player("Alice", samaritan, resources[2], resources[1]);
    players[1] = new Player("Bob", samaritan, resources[0], resources[2]);
    players[2] = new Player("Trudy", samaritan, resources[1], resources[0]);

    (new Thread(players[0])).start();
    (new Thread(players[1])).start();
    (new Thread(players[2])).start();
    (new Thread(samaritan)).start();
  }
}

/* Defines an unique Resource */
class Resource {
  private boolean available = false;

  public synchronized boolean isAvailable() {
    return available;
  }

  public synchronized void setAvailable(boolean available) {
    this.available = available;
  }
}

/* Defines the Player */
class Player implements Runnable {
  String name;        // Players name;
  Object samaritan;   // Samaritan
  Resource r1, r2;    // Resources

  public Player(String name, Object samaritan, Resource r1, Resource r2) {
    this.name = name;
    this.samaritan = samaritan;
    this.r1 = r1;
    this.r2 = r2;
  }

  public void run() {
    synchronized (samaritan) {
      while (true) {
        System.out.println(name + " is sleeping");

        try {
          samaritan.wait();
        } catch (InterruptedException e) {
          /* Ignored */
        }

        System.out.println(name + " is alive");

        if (r1.isAvailable() && r2.isAvailable()) {
          // Uses resources
          r1.setAvailable(false);
          r2.setAvailable(false);

          // Does some work
          System.out.println(name + " is working");
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            /* Ignored */
          }

          // Wakes samaritan
          samaritan.notifyAll();
        }
      }
    }
  }
}
