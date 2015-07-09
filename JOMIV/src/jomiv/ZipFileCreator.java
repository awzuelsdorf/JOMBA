package jomiv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	private String zipFilePath;
	private int bufferSizeInBytes;

	public ZipFileCreator(List<String> filePaths, String zipFilePath, int bufferSizeInBytes) {
		if (filePaths == null) {
			throw new IllegalArgumentException("fileNames cannot be null!");
		}
		if (zipFilePath == null) {
			throw new IllegalArgumentException("zip file name cannot be null!");
		}
		
		this.zipFilePath = zipFilePath;
		this.filePathsList = new LinkedList<String>();
		this.filePathsList.addAll(filePaths);
		this.setBufferSizeInBytes(bufferSizeInBytes);
	}
	
	public ZipFileCreator(List<String> filePaths, String zipFilePath) {
		this(filePaths, zipFilePath, DEFAULT_BUFFER_SIZE_IN_BYTES);
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
		
		//Put new entry (the file to be written) into zipfile.
		try {
			zos.putNextEntry(new ZipEntry(new File(filePath).getName()));
		} catch (ZipException e2) {
			//Failed to put new entry into zipfile (Probably because of a
			//duplicate entry). Return true after
			//attempting to close open resources.
			try {
				fis.close();
			} catch (IOException e3) {
			}
			
			return true;
		} catch (IOException e1) {
			//Failed to put new entry into zipfile (NOT because of a
			//duplicate entry).
			//Return false after
			//attempting to close open resources.
			try {
				fis.close();
			} catch (IOException e) {
			}
			
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
					} catch (IOException e1) {
					}
					
					//Failed to write. Cannot write file.
					//Return false.
					return false;
				}
			}
		} while (bytesRead != END_OF_FILE);
	
		try {
			fis.close();
		} catch (IOException e) {
		}
		
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
			
			if (!writeFileIntoZipOutputStream(fileName, zos, this.bufferSizeInBytes)) {
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
