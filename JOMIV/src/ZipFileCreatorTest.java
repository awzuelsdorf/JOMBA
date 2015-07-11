

import static org.junit.Assert.*;

import java.util.LinkedList;

import jomiv.ZipFileCreator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipFileCreatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void zipFileCreatorTest() {
		LinkedList<String> filePathsList = new LinkedList<String>();

		filePathsList.add("C:\\Users\\arizona16\\Downloads\\ubuntu-15.04-desktop-amd64.iso");
		filePathsList.add("C:\\Users\\arizona16\\Downloads\\stinkbug.png");
		filePathsList.add("C:\\Users\\arizona16\\Music\\az-ubuntu-15.04-desktop-amd64.iso");
		filePathsList.add("C:\\Users\\arizona16\\Documents\\superdevil.jpg");

		String zipFilePath = "C:\\Users\\arizona16\\Downloads\\test_zipfile_4.zip";
		
		ZipFileCreator zfc = new ZipFileCreator(filePathsList, zipFilePath);
	
		LinkedList<String> returnValue = zfc.writeAllFilesIntoZipOutputStream();
		
		assertFalse(returnValue == null);
		assertTrue(returnValue.isEmpty());
		
		System.out.println();

	}

}
