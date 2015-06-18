package com.threadTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArrayThreadManager {
	public static final int N = 6;
	
	public static void main(String args[]) {
		ArrayList<Double> array = new ArrayList<Double>();
		
		for (int i = 0; i < N; i += 1) {
			array.add(Math.ceil(i * Math.random()));
		}
		
		Collections.shuffle(array);
		
		ExecutorService executor = Executors.newFixedThreadPool(5);
		//for (int i = 0; i < 10; i += 1) {
			Runnable worker = new ArrayThread<Double>(array);
			executor.execute(worker);
		//}
		executor.shutdown();
		while (executor.isTerminated() == false);
		System.out.println("Finished!");
	}
}
