import java.util.Random;

public class Search implements Runnable {
  private static int visited = 0;
  private static boolean found = false;
  // private static volatile boolean found = false;

  private int[] table;         // Table of values
  private int start, end;      // start and end search indexes
  private int value;           // value to search

  public Search(int[] table, int start, int end, int value) {
    this.table = table;
    this.start = start;
    this.end = end;
    this.value = value;
  }

  public void run() {
    int i;

    for (i = start; i < end && !found; i++) {
      if (table[i] == value) {
        found = true;
        i++;  // element accessed
        break;
      }
    }

    synchronized (Search.class) {
      visited += i - start;
    }
  }

  public static void main(String[] args) {
    int N = 2;
    int K = 100000000;

    // Parses arguments
    if (args.length >= 3) {
      System.err.println("Usage: java Search [N [K]]");
      System.exit(1);
    }
    if (args.length >= 2) {
      K = Integer.parseInt(args[1]);
    }
    if (args.length >= 1) {
      N = Integer.parseInt(args[0]);
    }

    // Fills the table
    Random random = new Random(1);
    int[] table = new int[K];
    for (int i = 0; i < K; i++) {
      table[i] = random.nextInt();
    }

    int value = table[K / N / 2];
    run(N, table, value);
  }

  // Spawns N threads to search the table
  private static void run(int N, int[] table, int value)
  {
    Thread[] threads = new Thread[N];

    int start = 0;
    for (int i = 0; i < N; i++) {
      int length = table.length / N + (i < table.length % N ? 1 : 0);
      int end = start + length;
      threads[i] = new Thread(new Search(table, start, end, value));
      threads[i].start();
      start = end;
    }

    for (int i = 0; i < N; i++) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        // Ignored
      }
    }

    System.out.println("visited " + visited + " elements");
  }
}

