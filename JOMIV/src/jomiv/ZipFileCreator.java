package jomiv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ZipFileCreator {
	private static final int END_OF_FILE = -1;
	
	/**
	 * Caller provides a buffer and a FileInputStream object.
	 * This function reads up to the next buffer.length
	 * bytes of the file into the buffer, starting with the byte at offset
	 * "offset". This method returns the number of bytes read if there is more of the file
	 * to read. END_OF_FILE otherwise. NOTE: It is the caller's responsibility
	 * to close fis, not the callee.
	 */
	public static int readFileIntoBuffer(byte buffer[], int offset,
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
			e.printStackTrace();
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
	public static boolean writeFileIntoZipOutputStream(String filePath, ZipOutputStream zos, int bufferLength) {
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
			e.printStackTrace();
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
				e3.printStackTrace();
			}
			
			e2.printStackTrace();
			return true;
		} catch (IOException e1) {
			//Failed to put new entry into zipfile (NOT because of a
			//duplicate entry).
			//Return false after
			//attempting to close open resources.
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			e1.printStackTrace();
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
						e1.printStackTrace();
					}
					
					e.printStackTrace(); //Failed to write. Cannot write file.
					//Return false.
					return false;
				}
			}
		} while (bytesRead != END_OF_FILE);
	
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Takes in a list of file paths, the path to an output zipfile,
	 * and writes the files to the zipfile.
	 * @param fileNamesList
	 * @return LinkedList of file names of any files that could not be
	 * written to zipfile.
	 */
	public static LinkedList<String> writeAllFilesIntoZipOutputStream(LinkedList<String> filePathsList, String zipFilePath) {
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
			
			e.printStackTrace();
			
			return failedFilesList;
		}
		
		int bufferLength = 1024; //1kb buffer used to transfer data.
		
		ListIterator<String> fileIter = filePathsList.listIterator();
	
		while (fileIter.hasNext()) {
			if (!writeFileIntoZipOutputStream(fileIter.next(), zos, bufferLength)) {
				failedFilesList.add(fileIter.previous());
				System.out.printf("Failed on %s!\n", fileIter.next());
			}
			else {
				System.out.printf("Succeeded on %s!\n", fileIter.previous());
				fileIter.next(); //Essentially undo the previous() operation.
			}
		}
		
		try {
			zos.close();
		} catch (IOException e) {
			//Could not close zipfile...but succeeded in copying all
			//files. So just keep going. In a few lines, we will
			//return empty failedFilesList.
			e.printStackTrace();
		}
		
		return failedFilesList;
	}
	
	/*public static void main(String args[]) throws IOException {
		String filePath = "C:\\Users\\arizona16\\Downloads\\stinkbug.png";
		String filePath2 = "C:\\Users\\arizona16\\Downloads\\superdevil.jpg";
		String filePath3 = "C:\\Users\\arizona16\\Downloads\\ubuntu-15.04-desktop-amd64.iso";
	
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File("C:\\Users\\arizona16\\Downloads\\test_zipfile_2.zip")));
		
		int bufferLength = 1024; //Completely arbitrary number of bytes.
		
		if (writeFileIntoZipOutputStream(filePath, zos, bufferLength)) {
			System.out.printf("Success on %s!\n", filePath);
		}
		else {
			System.out.printf("Failure on %s!\n", filePath);
		}
	
		if (writeFileIntoZipOutputStream(filePath2, zos, bufferLength)) {
			System.out.printf("Success on %s!\n", filePath2);
		}
		else {
			System.out.printf("Failure on %s!\n", filePath2);
		}
	
		if (writeFileIntoZipOutputStream(filePath3, zos, bufferLength)) {
			System.out.printf("Success on %s!\n", filePath3);
		}
		else {
			System.out.printf("Failure on %s!\n", filePath3);
		}
		
		zos.close();
	}*/
	
	public static void main(String args[]) {
		LinkedList<String> filePathsList = new LinkedList<String>();
		
		filePathsList.add("C:\\Users\\arizona16\\Downloads\\ubuntu-15.04-desktop-amd64.iso");
		filePathsList.add("C:\\Users\\arizona16\\Downloads\\stinkbug.png");
		filePathsList.add("C:\\Users\\arizona16\\Music\\ubuntu-15.04-desktop-amd64.iso");
		filePathsList.add("C:\\Users\\arizona16\\Documents\\superdevil.jpg");
	
		String zipFilePath = "C:\\Users\\arizona16\\Downloads\\test_zipfile_3.zip";
		
		System.out.println(writeAllFilesIntoZipOutputStream(filePathsList, zipFilePath));
	}
}
