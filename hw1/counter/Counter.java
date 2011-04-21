public class Counter implements Runnable {
  private CounterInterface counter;
  private long K;

  public Counter(CounterInterface counter, long K) {
    this.counter = counter;
    this.K = K;
  }

  public void run() {
    while (K > 0) {
      counter.inc();
      K--;
    }
  }

  public static void main(String[] args) {
    int N = 2;
    long K = 2000000000;

    if (args.length >= 1) {
      N = Integer.parseInt(args[0]);
    } else if (args.length >= 2) {
      K = Long.parseLong(args[1]);
    } else if (args.length >= 3) {
      System.err.println("Usage: java Counter [N K]");
      System.exit(1);
    }

    run(N, K, new CounterSimple());
    run(N, K, new CounterVolatile());
    run(N, K, new CounterSynchronized());
  }

  private static void run(int N, long K, CounterInterface counter)
  {
    Thread[] threads = new Thread[N];

    for (int i = 0; i < N; i++) {
      threads[i] = new Thread(new Counter(counter, K / N));
      threads[i].start();
    }

    for (int i = 0; i < N; i++) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        // Ignored
      }
    }

    System.out.println(counter.getClass().getName() + ": " + counter.get());
  }
}

interface CounterInterface {
  public void inc();
  public long get();
}

/* At first, do not use any synchronization constructs. */
class CounterSimple implements CounterInterface {
  long value = 0;

  public void inc() {
    value++;
  }

  public long get() {
    return value;
  }
}

/* Then, make the value inside the Counter class volatile. */
class CounterVolatile implements CounterInterface {
  volatile long value = 0;

  public void inc() {
    value++;
  }

  public long get() {
    return value;
  }
}

/* Finally, make the inc() method synchronized. */
class CounterSynchronized implements CounterInterface {
  long value = 0;

  public synchronized void inc() {
    value++;
  }

  public synchronized long get() {
    return value;
  }
}

