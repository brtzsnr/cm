package data_structures;

import java.util.Random;

import data_structures.implementation.CoarseGrainedList;
import data_structures.implementation.CoarseGrainedTree;
import data_structures.implementation.FineGrainedList;
import data_structures.implementation.FineGrainedTree;
import data_structures.implementation.LockFreeList;
import data_structures.implementation.LockFreeTree;

public class Main {
	private static final int NR_OPERATIONS = 1000000;
	private static final boolean ALLOW_DOUBLE_ELEMENTS = false;

	private static final String CL = "cl";
	private static final String CT = "ct";
	private static final String FL = "fl";
	private static final String FT = "ft";
	private static final String LFL = "lfl";
	private static final String LFT = "lft";

	private static void permute(int[] array) {
		Random random = new Random();

		for (int i = 0; i < array.length; i++) {
			int r = random.nextInt(array.length);
			int swapped = array[i];
			array[i] = array[r];
			array[r] = swapped;
		}
	}

	private static void createWorkDataWithoutDoubles(int[] itemsToAdd,
			int[] itemsToRemove) {
		for (int i = 0; i < NR_OPERATIONS; i++) {
			itemsToAdd[i] = i;
			itemsToRemove[i] = i;
		}

		permute(itemsToAdd);
		permute(itemsToRemove);
	}

	private static void createWorkDataWithDoubles(int[] itemsToAdd,
			int[] itemsToRemove) {
		Random random = new Random();

		for (int i = 0; i < NR_OPERATIONS; i++) {
			int nextRandom = random.nextInt(NR_OPERATIONS);
			itemsToAdd[i] = nextRandom;
			itemsToRemove[i] = nextRandom;
		}

		permute(itemsToAdd);
		permute(itemsToRemove);
	}

	private static void createWorkData(int[] itemsToAdd, int[] itemsToRemove) {
		if (ALLOW_DOUBLE_ELEMENTS) {
			createWorkDataWithDoubles(itemsToAdd, itemsToRemove);
		} else {
			createWorkDataWithoutDoubles(itemsToAdd, itemsToRemove);
		}
	
	}

	private static void startThreads(
      boolean op, Sorted<Integer> sorted, int nrThreads)
			throws InterruptedException {
		int[] itemsToAdd = new int[NR_OPERATIONS];
		int[] itemsToRemove = new int[NR_OPERATIONS];
		createWorkData(itemsToAdd, itemsToRemove);

		WorkerThread[] workerThreads = new WorkerThread[nrThreads];

		for (int i = 0; i < nrThreads; i++) {
			workerThreads[i] = new WorkerThread(
          i, op, sorted, NR_OPERATIONS / nrThreads, itemsToAdd, itemsToRemove);
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < nrThreads; i++) {
			workerThreads[i].start();
		}
		for (int i = 0; i < nrThreads; i++) {
			workerThreads[i].join();
		}
		long end = System.currentTimeMillis();

    System.out.printf("time: %d ms\n\n", end - start);
	}

	private static void performWork(String dataStructure, int nrThreads)
			throws InterruptedException {
		Sorted<Integer> sorted = null;
		if (dataStructure.equals(CL)) {
			sorted = new CoarseGrainedList<Integer>();
		} else if (dataStructure.equals(CT)) {
			sorted = new CoarseGrainedTree<Integer>();
		} else if (dataStructure.equals(FL)) {
			sorted = new FineGrainedList<Integer>();
		} else if (dataStructure.equals(FT)) {
			sorted = new FineGrainedTree<Integer>();
		} else if (dataStructure.equals(LFL)) {
			sorted = new LockFreeList<Integer>();
		} else if (dataStructure.equals(LFT)) {
			sorted = new LockFreeTree<Integer>();
		} else {
			exitWithError();
		}

    System.out.println(">>> ADD");
		startThreads(true, sorted, nrThreads);
    System.out.println(">>> REMOVE");
		startThreads(false, sorted, nrThreads);
    System.out.println(sorted);
	}

	private static void exitWithError() {
		System.out
				.println("java data_structures.Main <data_structure> <nrThreads>");
		System.out.println("  where:");
		System.out.printf("    <data_structure> in {%s, %s, %s, %s, %s, %s}\n",
				CL, CT, FL, FT, LFL, LFT);
		System.out.println("    <nrThreads> is a number > 0");
		System.exit(1);
	}

	public static void main(String[] args) throws InterruptedException {
		if (args.length != 2) {
			exitWithError();
		}

		String dataStructure = args[0];
		int nrThreads = Integer.parseInt(args[1]);
		if (nrThreads < 1) {
			exitWithError();
		}

		performWork(dataStructure, nrThreads);
	}
}
