package com.threadTest;

import java.io.IOException;

public class WorkerThread implements Runnable {
	protected String cmd;
	private static int CURRENT_ID = 1;
	protected int id;
	
	public WorkerThread(String command) {
		cmd = command;
		id = CURRENT_ID++;
	}
	
	public void run() {
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finish();
	}
	
	public void finish() {
		System.out.printf("Thread with id %d finished\n", id);
	}
}
