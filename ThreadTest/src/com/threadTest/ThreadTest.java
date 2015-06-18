package com.threadTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {
	public static void main(String args[]) {
		ExecutorService executor = Executors.newFixedThreadPool(5);
		for (int i = 0; i < 10; i += 1) {
			Runnable worker = new WorkerThread("/bin/ls ~");
			executor.execute(worker);
		}
		executor.shutdown();
		while (executor.isTerminated() == false);
		System.out.println("Finished!");
	}
}
