package main;
/*********************************************************************
 *
 *      Copyright (C) 2015 Andrew Zuelsdorf
   

 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 ***************************************************************************/

import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

public class Combine {
	protected String primaryKeyName; //See the tutorials on the MWM wiki
	//for information on what the significance of a "primary key" is.
	protected LinkedList<String> files, shCols, unShCols;

	public Combine() {
		files = new LinkedList<String>(); //list of .xls files' locations.
		shCols = new LinkedList<String>(); //list of columns that at least
		//two sheets share
		unShCols = new LinkedList<String>(); //list of columns that are
		//unique to a given sheet.
	}

	/**
	 * Fill shCols with the names
	 * of shared columns in our given spreadsheets.
	 * @author Andrew W. Zuelsdorf
	 * @throws IOException 
	 * @throws BiffException 
	 */
	protected void identifySharedColumns() throws IOException, BiffException {
		Iterator<String> spreadsheetIterator = files.iterator();
		Workbook currentWorkbook;

		int indexInShCols, indexInUnShCols, col;
		String columnName;

		int currentIndex = 0;

		while (++currentIndex < files.size()) {
			currentWorkbook = Workbook.getWorkbook(
					new File(spreadsheetIterator.next()));

			for (Sheet currentSheet : currentWorkbook.getSheets()) {
				for (col = 0; col < currentSheet.getColumns(); col += 1) {
					//If a column name is in the shCols LinkedList but
					//not the unShCols LinkedList, then do nothing.
					//If a column name is in the unShCols LinkedList but
					//not the shCols LinkedList, then remove the column
					//name from the unShCols LinkedList and put it into
					//the shCols LinkedList. If a column name is in neither
					//list, then insert it into the unShCols LinkedList.
					columnName = currentSheet.getCell(col, 0).getContents();

					indexInShCols = shCols.indexOf(columnName);

					if (indexInShCols == -1) {
						indexInUnShCols = unShCols.indexOf(columnName);
						if (indexInUnShCols == -1) {
							unShCols.addLast(columnName);
						}
						else {
							shCols.addLast(unShCols.remove(indexInUnShCols));
						}
					}
				}
			}

			currentWorkbook.close();
		}
	}

	/**
	 * Tests whether item is contained in array based on the contents
	 * of item without case sensitivity
	 * @return true if array contains item, false otherwise.
	 */
	protected boolean contains(Cell array[], Cell item) {
		for (Cell otherItem : array) {
			if (item.getContents().equals(otherItem.getContents())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to find a column that appears in every spreadsheet and
	 * whose values are not duplicated within an arbitrary spreadsheet.
	 * @author Andrew Zuelsdorf
	 * @return A String object representing the name of the column
	 * whose values we will use as primary keys. Returns null if
	 * no such column exists.
	 * @throws IOException
	 * @throws BiffException
	 * @throws WriteException
	 */
	protected String getPrimaryKeyName() throws IOException, BiffException, WriteException {
		Iterator<String> spreadsheetIterator;
		Workbook currentWorkbook;
		String primaryKey = null;
		int currentIndex, col;

		for (String shColName : shCols) {
			currentIndex = 0;
			primaryKey = shColName;
			spreadsheetIterator = files.iterator();
			while (primaryKey != null && ++currentIndex < files.size()) {
				currentWorkbook = Workbook.getWorkbook(
						new File(spreadsheetIterator.next()));

				for (Sheet currentSheet : currentWorkbook.getSheets()) {
					if (primaryKey == null) {
						break;
					}

					//If this key is not found in even one sheet, then it
					//cannot be a primary key.
					try {
						col = currentSheet.findCell(primaryKey).getColumn();

						//If this column of "primary keys" contains
						//duplicates, then it cannot be a primary key.
						if (hasDuplicates(currentSheet.getColumn(col))) {
							primaryKey = null;
						}
					}
					catch (NullPointerException ex) {
						primaryKey = null;
					}
				}
				currentWorkbook.close();
			}

			if (primaryKey != null) {
				return primaryKey;
			}
		}

		return primaryKey;
	}

	/**
	 * Determines whether 
	 * @author Andrew Zuelsdorf
	 * @param column
	 * @return true if column contains a cell with the
	 * same content element twice. Returns false if column
	 * is null or does not contain duplicates. This method
	 * is case sensitive with respect to the contents of
	 * each cell.
	 */
	public boolean hasDuplicates(Cell column[]) {
		if (column == null) return false;

		for (int i = 1; i < column.length; i += 1) {
			for (int j = 1; j < column.length; j += 1) {
				if (column[i].getContents().equals(
						column[j].getContents()) && i != j) {

					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method allows one to put all duplicated and non-duplicated
	 * spreadsheet cells in the primary keys' shared column into a corresponding
	 * column on the output spreadsheet. Duplicates are not duplicated.
	 * IMPORTANT: Unlike with the zeroth shared column, order of values matters here.
	 * @throws IOException 
	 * @throws BiffException
	 * @author Andrew Zuelsdorf
	 */
	protected Vector<String> consolidatePrimaryKeyColumn() throws BiffException, WriteException, IOException {
		Iterator<String> spreadsheetIterator = files.iterator();
		Workbook currentWorkbook;
		Vector<String> primaryKeys = new Vector<String>();

		int inputColumn, currentIndex;

		//The index of the primary keys' column in our shared columns linked list.
		primaryKeyName = getPrimaryKeyName();

		//check that the user did not give us a bogus primary key column
		if (primaryKeyName == null) {
			//The user gave us the name of a nonexistent column
			throw new RuntimeException("Error: did not find a column of"
					+ " unique values.");
		}

		currentIndex = 0;

		//Find and consolidate all primary keys in all the sheets of
		//all the files.
		while (++currentIndex < files.size()) {
			currentWorkbook = Workbook.getWorkbook(
					new File(spreadsheetIterator.next()));

			for (Sheet currentSheet : currentWorkbook.getSheets()) {

				//Get the column in the current input sheet that our first
				//shared column occurs in.
				//Note: we need the try-catch if findCell() returns null
				//(because shCols.getFirst() may not be in this spreadsheet).
				try {
					inputColumn = currentSheet.findCell(
							primaryKeyName).getColumn();
				} catch (NullPointerException ex) {
					inputColumn = currentSheet.getColumns();
					//Shared column does not occur in this sheet. Do not enter
					//the conditional below.
				}

				//Put values from this column into our new sheet's column
				//iff those values are not already in our new sheet's column.
				//We must put the values in the same order as the values
				//in the first column.
				if (inputColumn != currentSheet.getColumns()) {
					for (Cell currentCell : 
						currentSheet.getColumn(inputColumn)) {
						//This value is not already in our vector
						//of primary keys.
						if (!(primaryKeys.contains(currentCell.getContents()))) {
							primaryKeys.add(currentCell.getContents());
						}
					}
				}
			}
			currentWorkbook.close();
		}

		return primaryKeys;
	}

	/**
	 * Returns the index of the Cell object in
	 * targetColumn whose contents matched the
	 * text of targetContents.
	 * @author Andrew Zuelsdorf
	 * @param targetColumn - an array of Cell objects.
	 * @param targetContents - the contents of the cell that
	 * we are searching for.
	 * @return -1 if cell not found. A number between 0 and
	 * targetColumn.length - 1, inclusive, otherwise.
	 */
	protected int getIndexOfTargetKey(Cell targetColumn[], String targetContents) {
		int index;

		for (index = 0; index < targetColumn.length; index += 1) {
			if (targetColumn[index].getContents().equals(targetContents)) {
				return index;
			}
		}

		//We did not find the target. Return -1 to indicate that the column does not exist.
		return -1;
	}

	/**
	 * Identifies duplicate information and consolidates info from all spreadsheets
	 * before writing to output file.
	 * @author Andrew Zuelsdorf
	 * @param pkColumn - a Vector object of String objects that represent our
	 * primary keys.
	 * @throws IOException
	 * @throws BiffException
	 * @throws RowsExceededException
	 * @throws WriteException
	 * @throws IndexOutOfBoundsException
	 */
	protected void consolidateOtherRows(Vector<String> pkColumn) 
			throws IOException, BiffException, RowsExceededException, WriteException, IndexOutOfBoundsException {

		//TreeMap used because the keys of the elements of the vector
		//must be in the same order. 
		Vector<TreeMap<String, String>> outputRows = new Vector<>();

		//No need to check for boundary overrun because output
		//workbook should have one sheet and one column by now
		String columnValue, rowValue;
		int index0, currentIndex, col, row, index2, index1;

		Iterator<String> spreadsheetIterator;
		Workbook currentWorkbook;

		//Create vector of hashmaps.
		//Hashmap key represents column value.
		//Hashmap value represents row value.
		for (index0 = 1; index0 < pkColumn.size(); index0 += 1) {
			outputRows.add(new TreeMap<String, String>());
			outputRows.get(index0 - 1).put(primaryKeyName,
					pkColumn.get(index0));
		}

		//Go through each sheet of each spreadsheet. Find the row
		//that contains the current key, then insert into the
		//current key's map any values in that row for which a
		//key does not exist in the current element.
		currentIndex = 0;
		spreadsheetIterator = files.iterator();
		while (++currentIndex < files.size()) {
			String fileName = spreadsheetIterator.next();
			currentWorkbook = Workbook.getWorkbook(
					new File(fileName));

			for (Sheet currentSheet : currentWorkbook.getSheets()) {
				//index1 is the current primary key's index
				col = currentSheet.findCell(primaryKeyName).getColumn();
				
				for (index1 = 0; index1 < outputRows.size(); index1 += 1) {
					try {
						row = getIndexOfTargetKey(currentSheet.getColumn(col), outputRows.get(index1).get(primaryKeyName));

						if (row != -1) {
							//index0 is the current column in this row that we are processing.
							for (index0 = 0; index0 < currentSheet.getColumns(); index0 += 1) {
								//The name of the index0th column.
								columnValue = currentSheet.getCell(index0, 0).getContents();

								//We do not have a value for this column in this row.
								if (!(outputRows.get(index1).containsKey(columnValue))) {
									rowValue = currentSheet.getCell(index0, row).getContents();
									outputRows.get(index1).put(columnValue, rowValue);
								}
								//We *do* have a value for this column in this row...
								else {
									//...and the information is contradictory.
									if (!(currentSheet.getCell(index0,
											row).getContents().equals(outputRows.get(index1).
													get(columnValue)))) {
										String errorString = "MWM has encountered a discrepancy in your data.\n";
										errorString += String.format("The \"%s\" value \"%s\"\nhas a(n) \"%s\" value of \"%s\"\n in one sheet and a(n) \"%s\" value of \"%s\"\nin another. Please correct this and try merging again.\n",
										primaryKeyName, outputRows.get(index1).get(primaryKeyName),
										columnValue, currentSheet.getCell(index0, row).getContents(),
										columnValue,
										outputRows.get(index1).get(columnValue));
									
										currentWorkbook.close();
										throw new DiscrepancyException(errorString);
									}
								}
							}
						}
					} catch (NullPointerException e) {
						//The index1th primary key is not in this spreadsheet.
						//Go on to the next primary key.
					}
				}
			}
			
			currentWorkbook.close();
		}

		//Now that we have all the rows for our spreadsheet, we must
		//ensure that all our elements have the same keys.
		//If one element does not have at least one of the keys that
		//the others have, then insert that key and a value of "" into
		//the deficient element.

		TreeSet<String> superSet = new TreeSet<String>();
		//Will hold all the keys that a given row.
		//In the output spreadsheet must hold.

		//Determine the set of columns that our rows must have values for.
		for (index0 = 0; index0 < outputRows.size(); index0 += 1) {
			for (String key : outputRows.get(index0).keySet()) {
				superSet.add(key); //The set will handle duplicate
				//addition.
			}
		}

		//If a row does not contain a given key, then put in that key and
		//a blank value for that key.
		for (index0 = 0; index0 < outputRows.size(); index0 += 1) {
			for (String key : superSet) {
				if (!(outputRows.get(index0).containsKey(key))) {
					outputRows.get(index0).put(key, "");
				}
			}
		}

		//Now, write the information out to the spreadsheet.
		//This will overwrite the column of primary keys that
		//we already put in.

		//Start by writing the names of the columns on the top row.
		WritableWorkbook outputWorkbook = 
				Workbook.createWorkbook(new File(this.files.getLast()));

		//There is no sheet already in this spreadsheet.
		outputWorkbook.createSheet("Sheet1", 0);

		index2 = 0;
		for (String nameOfColumn : superSet) {
			outputWorkbook.getSheet(0).addCell(new Label(index2++,
					0, nameOfColumn));
		}

		//Now write the other rows' values.
		row = 1;
		for (TreeMap<String, String> mp : outputRows) {
			col = 0;
			for (String key : mp.keySet()) {
				outputWorkbook.getSheet(0).addCell(new Label(col,
						row, mp.get(key)));
				col += 1;
			}
			row += 1;
		}

		//Now swap the 0th column with the column of
		//primary keys if the 0th column is not the
		//column of primary keys.
		if (!(outputWorkbook.getSheet(0).getColumn(0)[0].
				getContents().equals(primaryKeyName))) {

			swapColumns(0,
					getIndexOfPrimaryKeyColumn(
							outputWorkbook.getSheet(0)), outputWorkbook);
		}

		outputWorkbook.write();
		outputWorkbook.close();
	}

	public int getIndexOfPrimaryKeyColumn(Sheet wb) {
		if (wb.getRows() == 0) return -1;
		return this.getIndexOfTargetKey(wb.getRow(0), primaryKeyName);
	}

	public void swapColumns(int i, int j, WritableWorkbook wwb) 
			throws RowsExceededException, WriteException {

		Cell temp;

		int row;
		int numberOfRows = wwb.getSheet(0).getRows();

		for (row = 0; row < numberOfRows; row += 1) {
			temp = wwb.getSheet(0).getCell(i, row);
			wwb.getSheet(0).addCell(new Label(i, row,
					wwb.getSheet(0).getCell(j, row).getContents()));
			wwb.getSheet(0).addCell(new Label(j, row,
					temp.getContents()));
		}
	}

	/**
	 * Where we join the spreadsheets that the user gave us.
	 * @author Andrew Zuelsdorf
	 */
	public boolean joinSpreadsheets(List<String> files) {
		if (!insertSpreadsheets(files)) {
			return false;
		}
		try {
			identifySharedColumns();
		} catch (BiffException | IndexOutOfBoundsException
				| IOException e) {
			return false;
		}

		try {
			consolidateOtherRows(consolidatePrimaryKeyColumn());
		} catch (BiffException | WriteException
				| IndexOutOfBoundsException | IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * In lieu of a GUI, the user can insert files to be joined
	 * using this method.
	 * @author Andrew Zuelsdorf
	 * @param files - a list of files ordered so that the last
	 * is the file name of the joined spreadsheets and all the
	 * other strings are the names of the spreadsheets that need
	 * to be joined.
	 * @return true if insertion succeeded, false otherwise.
	 */
	public boolean insertSpreadsheets(List<String> files) {
		//Stave off NullPointerExceptions.
		if (files == null || files.isEmpty()) return false;

		Iterator<String> it = files.iterator();

		while (it.hasNext()) {
			this.files.addLast(it.next());
		}
		return true;
	}
}
