package data_structures;

import java.lang.management.*;

public class WorkerThread extends Thread {

	private static final boolean DO_SLEEP = false;
	private static int SLEEP_TIME = 10; // ms

	private int id;
  private boolean op;
	private int nrIterations;
	private Sorted<Integer> sorted;
	private int[] itemsToAdd;
	private int[] itemsToRemove;
	
	WorkerThread(int id, boolean op,  Sorted<Integer> list,
               int nrIterations, int[] itemsToAdd, int[] itemsToRemove) {
		this.sorted = list;
		this.id = id;
    this.op = op;
		this.nrIterations = nrIterations;
		this.itemsToAdd = itemsToAdd;
		this.itemsToRemove = itemsToRemove;
	}

  private long getCPUTimeMillis() {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
    return bean.getCurrentThreadCpuTime() / 1000000;
  }

	public void run() {
		int startIndex = nrIterations * id;

    long start = getCPUTimeMillis();
    if (op) {
      add(sorted, startIndex, nrIterations, itemsToAdd);
    } else {
      remove(sorted, startIndex, nrIterations, itemsToRemove);
    }
    long end = getCPUTimeMillis();

    System.err.println("Worked " + id + " spent " + (end - start) + " ms");
	}

	private void remove(Sorted<Integer> sorted, int startIndex, int nrIterations, int[] itemsToRemove) {
		for (int i = startIndex; i < startIndex + nrIterations; i++) {
			sleep();
			sorted.remove(itemsToRemove[i]);
		}
	}

	private void add(Sorted<Integer> sorted, int startIndex, int nrIterations, int[] itemsToAdd) {
		for (int i = startIndex; i < startIndex + nrIterations; i++) {
			sleep();
			sorted.add(itemsToAdd[i]);
		}
	}

	private void sleep() {
		if (DO_SLEEP)
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
}
