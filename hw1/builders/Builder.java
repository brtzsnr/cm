import java.util.Random;

public class Builder implements Runnable {
  private static final int S_PAUSE = 0;
  private static final int S_WAIT = 1;
  private static final int S_WORK = 2;
  private static final int S_NUMSTATES = 3;

  private static Random random = new Random();
  private static long startTime = System.currentTimeMillis();
  private static long[] times = new long[S_NUMSTATES];

  private static int K = 20;            // number of builders
  private static Builder[] builders;    // builders

  private int id;                       // id, 0 ... K-1
  private Object first, second;         // resources
  private int state = S_PAUSE;          // states
  private int numBuildings;             // buildings build
  private long timer;

  public Builder(int id, Object first, Object second) {
    this.id = id;

    // Breaks the graph cycle
    // if (id % 2 == 0) {
    // if (id != 0) {
    if (random.nextBoolean()) {
      System.err.println(id + " did not reversed resources");
      this.first = first;
      this.second = second;
    } else {
      System.err.println(id + " reversed resources");
      this.first = second;
      this.second = first;
    }
  }

  public synchronized void run() {
    timer = System.currentTimeMillis();

    while (true) {
      try {
        build();
        updateStats();

        // Give waiting builders a chance to acquire
        // resources.
        // Thread.yield();
      } catch (InterruptedException e) {
        /* Ignored */
      }
    }
  }

  private void build() throws InterruptedException {
    boolean flag;

    switchState(S_WAIT);

    synchronized (first) {
      synchronized (second) {
        switchState(S_WORK);

        // Pretend to do some work.
        // The amount of work is random chosen between 5 and 15.
        // With a fixed amount of work and Thread.sleep(0, 1)
        // between jobs the threads start to alternate giving
        // an almost perfect scenario.
        Thread.sleep(5 + random.nextInt(10));
      }
    }

    switchState(S_PAUSE);
  }

  // Moves to a new state.
  private void switchState(int newState) {
    updateTime();
    state = newState;
  }

  // Updates time delta in array for current builder
  private void updateTime() {
    long curr = System.currentTimeMillis();

    synchronized (Builders.class) {
      times[state] += curr - timer;
      timer = curr;
    }
  }

  // Prints some nice stats from time to time.
  private void updateStats() {
    synchronized (Builders.class) {
      numBuildings++;

      if (numBuildings % (10 * K) == 0) {
        for (int i = 0; i < K; i++) {
          builders[i].updateTime();
        }

        double average = 0.;
        for (int i = 0; i < K; i++) {
          average += builders[i].numBuildings;
        }
        average /= K;

        double variance = 0.;
        for (int i = 0; i < K; i++) {
          double tmp = builders[i].numBuildings - average;
          variance += tmp * tmp;
        }
        variance /= K;

        long total = times[S_PAUSE] + times[S_WAIT] + times[S_WORK];

        // Prints total time across all threads.
        System.err.print(total / 1000.);
        // Percentage of pausing, waiting and working
        System.err.print(" " + (100. * times[S_PAUSE] / total));
        System.err.print(" " + (100. * times[S_WAIT] / total));
        System.err.print(" " + (100. * times[S_WORK] / total));
        // Index of dispersion
        // http://en.wikipedia.org/wiki/Variance-to-mean_ratio
        System.err.println(" " + (variance / average));

        // Number of buildings.
        for (int i = 0; i < K; i++) {
          System.err.print(" " + builders[i].numBuildings);
        }
        System.err.println();
        System.err.println();

        if (System.currentTimeMillis() - startTime > 100000) {
          System.err.println("Finished");
          System.exit(0);
        }
      }
    }
  }



  public static void main(String[] args)
  {
    if (args.length >= 2) {
      System.err.println("Usage: java Builder [K]");
      System.exit(1);
    }
    if (args.length >= 1) {
      K = Integer.parseInt(args[0]);
    }

    // Creates resources
    Object[] resources = new Object[K];
    for (int i = 0; i < K; i++) {
      resources[i] = new Object();
    }

    // Creates builders
    builders = new Builder[K];
    for (int i = 0; i < K; i++) {
      builders[i] = new Builder(i, resources[i], resources[(i + 1) % K]);
    }

    Thread[] threads = new Thread[K];
    for (int i = 0; i < K; i++) {
      threads[i] = new Thread(builders[i]);
      threads[i].start();
    }

    for (int i = 0; i < K; i++) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        // Ignored
      }
    }
  }
}

