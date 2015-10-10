import static org.junit.Assert.*;
import java.util.LinkedList;
import jomba.ZipFileCreator;
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

	public static boolean printAllFiles(LinkedList<String> files) {
		if (files == null) return true;
		
		System.out.println("Failed files:");
		
		for (final String s: files) {
			System.out.println(s);
		}
		
		return false;
	}
	
	@Test
	public void zipFileCreatorTest() {
		LinkedList<String> filePathsList = new LinkedList<String>();

		filePathsList.add("C:\\Users\\arizona16\\Downloads\\225practiceexam1solutions.pdf");

		String zipFilePath = "C:\\Users\\arizona16\\Downloads\\test_zipfile_4.zip";
		
		ZipFileCreator zfc = new ZipFileCreator(filePathsList, zipFilePath);
	
		LinkedList<String> returnValue = zfc.writeAllFilesIntoZipOutputStream();
		
		assertFalse(returnValue == null);
		assertTrue(returnValue.isEmpty() || printAllFiles(returnValue));
		
		System.out.println();

	}

}
