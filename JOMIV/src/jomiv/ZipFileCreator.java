package jomiv;

/*
Copyright 2015 Andrew Zuelsdorf.
Licensed under GNU GPL version 3.0.

This file is part of JOMIV

JOMIV is free software:
you can redistribute it and/or modify it under the terms of the
GNU General Public License as published by the Free Software 
Foundation, either version 3 of the License, or (at your option)
any later version. This program is distributed in the hope that
it will be useful, but WITHOUT ANY WARRANTY; without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ZipFileCreator {
	private static final int END_OF_FILE = -1;
	private static final int BYTE_SIZE = 8;
	private static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 1024;
	
	private LinkedList<String> filePathsList;
	private HashSet<String> entriesSoFar;
	private String zipFilePath;
	private int bufferSizeInBytes;

	public ZipFileCreator(List<String> filePaths, String zipFilePath, int bufferSizeInBytes) {
		if (filePaths == null) {
			throw new IllegalArgumentException("fileNames cannot be null!");
		}
		if (zipFilePath == null) {
			throw new IllegalArgumentException("zip file name cannot be null!");
		}
	
		entriesSoFar = new HashSet<String>();
		this.zipFilePath = zipFilePath;
		filePathsList = new LinkedList<String>();
		filePathsList.addAll(filePaths);
		setBufferSizeInBytes(bufferSizeInBytes);
	}
	
	public ZipFileCreator(List<String> filePaths, String zipFilePath) {
		this(filePaths, zipFilePath, DEFAULT_BUFFER_SIZE_IN_BYTES);
	}
	
	public String getZipFilePath() {
		return String.format("%s", zipFilePath);
	}

	public void setZipFilePath(String filePath) {
		if (filePath == null) {
			throw new IllegalArgumentException("null file path not allowed");
		}
		else if (!filePath.endsWith(".zip")) {
			throw new IllegalArgumentException("file path must be for a zip file");
		}
		else {
			this.zipFilePath = String.format("%s", filePath);
		}
		
	}
	
	/**
	 * Caller provides a buffer and a FileInputStream object.
	 * This function reads up to the next buffer.length
	 * bytes of the file into the buffer, starting with the byte at offset
	 * "offset". This method returns the number of bytes read if there is more of the file
	 * to read. END_OF_FILE otherwise. NOTE: It is the caller's responsibility
	 * to close fis, not the callee.
	 */
	protected int readFileIntoBuffer(byte buffer[], int offset,
			FileInputStream fis) {
		if (fis == null || buffer == null) {
			return END_OF_FILE;
		}
		
		int retVal = END_OF_FILE;
		
		try {
			retVal = fis.read(buffer, 0, buffer.length);
		} catch (IOException e) {
			retVal = END_OF_FILE; //Stop reading this file before we
			//generate more IOExceptions.
		}
		
		return retVal;
	}

	/**
	 * 
	 * @param filePath: an absolute path to a file.
	 * @param zos: a ZipOutputStream (basically, an object that lets us put
	 * stuff into a zipfile). We do this by repeatedly reading bufferLength
	 * bytes of the file at filePath into zos.
	 * @return boolean indicating whether the file at filePath is now an
	 * entry in our zipfile.
	 */
	protected boolean writeFileIntoZipOutputStream(String filePath,
			ZipOutputStream zos, int bufferLength) {
		
		boolean succeeded = false; //Right now, we have not succeeded in
		//putting our entry into our zipfile.
		
		//We cannot put an unknown file into a zipfile,
		//nor can we put a file into a null zipfile, nor does it make sense
		//for us to read in zero or fewer bytes at a time.
		if (zos == null || filePath == null || bufferLength <= 0) {
			return succeeded;
		}
		
		byte buffer[] = new byte[bufferLength];
		
		FileInputStream fis;
		
		//We could not find the file, so we cannot put it in the zipfile.
		//Return false.
		try {
			fis = new FileInputStream(new File(filePath));
		} catch (FileNotFoundException e) {
			return false;
		}
		
		String fname = new File(filePath).getName(); //The name of our new
		//entry to this file.
		
		//Put new entry (the file to be written) into zipfile.
		try {
			zos.putNextEntry(new ZipEntry(fname));
			entriesSoFar.add(fname);
		} catch (ZipException e2) {
			//Failed to put new entry into zipfile (Probably because of a
			//duplicate entry). Create a unique entry name and add that to
			//the zip file.
			
			//Get location of last period in file name.
			int periodIndex = fname.length(); //Index of last period in file name
			
			while (periodIndex > 0 && fname.charAt(--periodIndex) != '.');
			
			if (periodIndex == 0) {
				//The last period in this file is the first character of its name.
				//Therefore, the file has no extension. Therefore, we can just add
				//an integer to the end of the file name such that there is no
				//entry in this zip file that has such a name.
				long unique = 1;
				
				while (entriesSoFar.contains(fname + "(" + unique + ")")) unique += 1;
				
				fname = fname + "(" + unique + ")";
			}
			else if (periodIndex == fname.length())
			{
				//The last period in this file is the last character of its name.
				//Therefore, we can just add an integer to the end of the file
				//name such that there is no entry in this zip file that has
				//such a name.
				long unique = 1;
				
				while (entriesSoFar.contains(fname + "(" + unique + ")")) unique += 1;
				
				fname = fname + "(" + unique + ")";
			}
			else {
				//The last period in this file is not the first character of its name.
				//Also, the last period in this file is not the last character of its name.
				//Therefore, we can split the string fname into
				//fname.substring(0, periodIndex) + "(" + <unique_integer> + ")" +
				//fname.substring(periodIndex + 1, fname.length())
				long unique = 1;
				
				while (entriesSoFar.contains(fname.substring(0, periodIndex) +
				"(" + unique + ")" + fname.substring(periodIndex, fname.length()))) {
					unique += 1;
				}
				
				fname = fname.substring(0, periodIndex) +
				"(" + unique + ")" + fname.substring(periodIndex, fname.length());
			}
			
			//We have our unique entry. Put it in the zip file and our entries set.
			try {
				zos.putNextEntry(new ZipEntry(fname));
				entriesSoFar.add(fname);
			} catch (IOException e) {
				//Failed to put new entry into zipfile (NOT because of a
				//duplicate entry).
				//Return false after
				//attempting to close open resources.
				try {
					fis.close();
				} catch (IOException e1) {}
				return false;
			}
		} catch (IOException e1) {
			//Failed to put new entry into zipfile (NOT because of a
			//duplicate entry).
			//Return false after
			//attempting to close open resources.
			try {
				fis.close();
			} catch (IOException e) {}
			
			return false;
		}
		
		int offset = 0;
		
		int bytesRead = 0;
		
		do {
			bytesRead = readFileIntoBuffer(buffer, offset, fis);

			//We got some data.
			if (bytesRead != END_OF_FILE) {
				try {
					//Write from 0th byte, 1st byte, ...,
					//(bufferLength - 1)th byte to zipfile.
					zos.write(buffer, 0, buffer.length);
					offset += bytesRead;
				} catch (IOException e) {
					
					//We must exit since we failed to write. Attempt to close open resources.
					try {
						fis.close();
					} catch (IOException e1) {}
					
					//Failed to write. Cannot write file.
					//Return false.
					return false;
				}
			}
		} while (bytesRead != END_OF_FILE);
	
		try {
			fis.close();
		} catch (IOException e) {}
		
		return true;
	}
	
	public int getBufferSizeInBytes() {
		return this.bufferSizeInBytes;
	}
	
	public void setBufferSizeInBytes(int bufferSizeInBytes) {
		if (bufferSizeInBytes <= 0) {
			throw new IllegalArgumentException(String.format(
			"Buffer size in bytes must be at least one, was %d\n",
			bufferSizeInBytes));
		}
		
		this.bufferSizeInBytes = bufferSizeInBytes;
	}
	
	public int getBufferSizeInBits() {
		return BYTE_SIZE * this.bufferSizeInBytes;
	}
	
	/**
	 * Takes in a list of file paths, the path to an output zipfile,
	 * and writes the files to the zipfile.
	 * @param fileNamesList
	 * @return LinkedList of file names of any files that could not be
	 * written to zipfile.
	 */
	public LinkedList<String> writeAllFilesIntoZipOutputStream() {
		LinkedList<String> failedFilesList = new LinkedList<String>();
	
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(new File(zipFilePath)));
		} catch (FileNotFoundException e) {
			//Could not open zipfile. Therefore, we failed to copy
			//all of the files that we were supposed to copy.
			//Therefore, return the entire list of files that we
			//were supposed to copy.
			failedFilesList.addAll(filePathsList);
			
			return failedFilesList;
		}
		
		ListIterator<String> fileIter = filePathsList.listIterator();
	
		String fileName = null;
		
		while (fileIter.hasNext()) {
			fileName = fileIter.next();
			
			if (!writeFileIntoZipOutputStream(fileName, zos, bufferSizeInBytes)) {
				failedFilesList.add(fileName);
			}
		}
		
		try {
			zos.close();
		} catch (IOException e) {
			//Could not close zipfile...but succeeded in copying all
			//files. So just keep going. In a few lines, we will
			//return empty failedFilesList.
		}
		
		return failedFilesList;
	}
}
