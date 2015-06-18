package com.threadTest;

import java.util.ArrayList;

public class ArrayThread<T extends Comparable<T>> implements Runnable {
	protected ArrayList<T> array; //Pointer to an array that we want to
	//sort
	protected int upperIndex; //One plus the highest index that we can
	//consider for sorting in this array
	protected int lowerIndex; //The index of the first element that we can
	//consider for sorting in this array.
	protected int n;
	
	public ArrayThread(ArrayList<T> array) {
		this(array, 0, array.size());
	}
	
	public ArrayThread(ArrayList<T> array, int lowerIndex, int upperIndex) {
		//Error checking
		if (array == null) {
			throw new IllegalArgumentException("Array(List) must not be null");
		}
		if (lowerIndex < 0) {
			throw new IllegalArgumentException("Lower index must not be less than zero");
		}
		if (upperIndex < lowerIndex) {
			throw new IllegalArgumentException("Upper index must not be less than lower index");
		}
		
		this.lowerIndex = lowerIndex;
		this.upperIndex = upperIndex;
		n = upperIndex - lowerIndex;
		this.array = array;
	}
	
	public void run() {
		printArray();
		insertionSort();
		printArray();
		assert2(isSorted());
	}
	
	public boolean isSorted() {
		for (int i = lowerIndex; i < upperIndex - 1; i += 1) {
			if (array.get(i).compareTo(array.get(i + 1)) > 0) {
				return false;
			}
		}
		return true;
	}
	
	public void printArray() {
		System.out.println("Starting to print");
		for (int i = lowerIndex; i < upperIndex; i += 1) {
			System.out.print(array.get(i));
			System.out.print(i != upperIndex - 1 ? ", " : "\n");
		}
		System.out.println("Finished printing");
	}
	
	public static final void assert2(boolean condition) {
		if (condition == false) {
			throw new AssertionError("Condition not met");
		}
		System.out.println("Blah");
	}
	
	public void insertionSort() {
		System.out.println("Starting insertion sort");
		T key;
		int j;
		
		for (int i = 1; i < n; i += 1) {
			key = array.get(lowerIndex + i);
			
			j = i - 1;
			
			while (j >= 0 && array.get(lowerIndex + j).compareTo(key) > 0) {
				array.set(lowerIndex + j + 1, array.get(lowerIndex + j));
				j -= 1;
			}
			
			array.set(lowerIndex + j + 1, key);
		}
		System.out.println("Finished insertion sort");
	}
}
