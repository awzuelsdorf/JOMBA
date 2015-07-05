package com.threadTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArrayThreadManager {
	public static final int NUM_NUMBERS = 10;
	public static final int NUM_THREADS = 2;
	
	public static void main(String args[]) {
		ArrayList<Integer> array = new ArrayList<Integer>();
		int i;
		
		for (i = 0; i < NUM_NUMBERS; i += 1) {
			array.add((int)Math.ceil(NUM_NUMBERS * Math.random()));
		}
		
		Collections.shuffle(array);
		
		//Sorting commences
		ExecutorService executor = Executors.newFixedThreadPool(5);
		for (i = 0; i <= NUM_NUMBERS - NUM_NUMBERS / NUM_THREADS; i += NUM_NUMBERS / NUM_THREADS) {
			Runnable worker = new ArrayThread<Integer>(array, i, i + NUM_NUMBERS / NUM_THREADS, false);
			executor.execute(worker);
		}
		executor.shutdown();
		while (executor.isTerminated() == false);
		System.out.println("Finished with phase 1!");
		//ArrayThread.printArray(array);
		
		//Merging commences
		executor = Executors.newFixedThreadPool(5);
		for (i = 0; i < NUM_NUMBERS - NUM_NUMBERS / NUM_THREADS; i += NUM_NUMBERS / NUM_THREADS) {
			Runnable worker = new ArrayThread<Integer>(array, i, i + NUM_NUMBERS / NUM_THREADS, true);
			executor.execute(worker);
			//System.out.println("Lower Limit: " + i);
			//System.out.println("Upper Limit: " + (i + NUM_NUMBERS / NUM_THREADS));
		}
		executor.shutdown();
		while (executor.isTerminated() == false);
		System.out.println("Finished with phase 2!");
		//ArrayThread.printArray(array);
		ArrayThread.assert2(ArrayThread.isSorted(array));
	}
}
