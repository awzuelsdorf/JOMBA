package com.threadTest;

import java.util.ArrayList;

public class ArrayThread<T extends Comparable<T>> implements Runnable {
	protected ArrayList<T> array; //Pointer to an array that we want to
	//sort
	protected int upperIndex; //One plus the highest index that we can
	//consider for sorting in this array
	protected int lowerIndex; //The index of the first element that we can
	//consider for sorting in this array.
	protected int n; //The length of this array. Equal to upperIndex - lowerIndex
	//after initialization.
	protected boolean doMerge; //Whether this thread is in charge of merging or sorting.

	public ArrayThread(ArrayList<T> array) {
		this(array, 0, array != null ? array.size() : 0, false);
	}

	public ArrayThread(ArrayList<T> array, int lowerIndex, int upperIndex, boolean doMerge) {
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
		this.doMerge = doMerge;
	}

	public void run() {
		if (doMerge) {
			merge();
		}
		else {
			//printArray();
			insertionSort();
			//printArray();
			assert2(isSorted());
		}
	}

	public boolean isSorted() {
		T minimum = array.get(lowerIndex); //The value that each element
		//after this one must equal or exceed.
		for (int i = lowerIndex; i < upperIndex; i += 1) {
			if (minimum.compareTo(array.get(i)) > 0) {
				return false;
			}
			minimum = array.get(i);
		}
		return true;
	}

	//After this method finishes, all elements with indices in the range
	//[lowerIndex, upperIndex + n] will be sorted.
	public void merge() {
		ArrayList<T> buffer = new ArrayList<T>(upperIndex + n - lowerIndex);
		while (buffer.size() < upperIndex + n) buffer.add(array.get(0));


		int i = lowerIndex; //Our starting point on the sorted lower half of our array.
		int j = upperIndex; //Our starting point on the sorted upper half of our array.
		int k = 0; //Our starting point for insertion into our
		//soon-to-be-sorted array.
		
		while (i < upperIndex && j < upperIndex + n) {
			if (array.get(i).compareTo(array.get(j)) <= 0) {
				buffer.get(k);
				array.get(i);
				buffer.set(k, array.get(i));
				i += 1;
			}
			else {
				buffer.get(k);
				array.get(j);
				buffer.set(k, array.get(j));
				j += 1;
			}
			k += 1;
			
		}
		
		boolean executedFirst = false;
		boolean executedSecond = false;
		
		while (i < upperIndex) {
			executedFirst = true;
			buffer.set(k++, array.get(i++));
		}
		
		while (j < upperIndex + n) {
			executedSecond = true;
			buffer.set(k++, array.get(j++));
		}
		
		printArray(buffer);
		
		//Copy elements from buffer back into array.
		k = 0;
		i = lowerIndex;
		
		assert2(array.size() >= upperIndex + n);
		
		while (i < upperIndex + n) {
			array.set(k++, buffer.get(i++));
		}
		
		assert2((executedFirst == false && executedSecond == false) || (executedFirst == true && executedSecond == false) || (executedFirst == false && executedSecond == true));
	}

	public static boolean isSorted(ArrayList<Integer> array) {
		boolean isSorted = true;

		for (int i = 0; i < array.size() - 1; i += 1) {
			if (array.get(i).compareTo(array.get(i + 1)) > 0) {
				System.out.println(array.get(i) + " and " + array.get(i + 1) + " are out of order");
				isSorted = false;
			}
		}
		return isSorted;
	}

	public void printArray() {
		for (int i = lowerIndex; i < upperIndex; i += 1) {
			System.out.print(array.get(i));
			System.out.print(i != upperIndex - 1 ? ", " : "\n");
		}
	}

	public void printArray(ArrayList<T> array) {
		for (int i = 0; i < array.size(); i += 1) {
			System.out.print(array.get(i));
			System.out.print(i != array.size() - 1 ? ", " : "\n");
		}
	}

	public static final void assert2(boolean condition) {
		if (condition == false) {
			throw new AssertionError("Condition not met");
		}
	}

	public void insertionSort() {
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
	}
}
