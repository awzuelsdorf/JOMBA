package jomiv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipTest {
	public static void main(String args[]) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File("C:\\Users\\arizona16\\Downloads\\test_zipfile.zip")));
		
		zos.putNextEntry(new ZipEntry("stinkbug.png"));
		Path path = new File("C:\\Users\\arizona16\\Downloads\\stinkbug.png").toPath();
		
		byte array[] = Files.readAllBytes(path);
	
		if (array == null) {
			zos.close();
			throw new NullPointerException("NULL!");
		}
		
		zos.write(array, 0, array.length);
		
		zos.finish();
		zos.close();
		
		System.out.println("I'm yelling at you!");
	}
}
