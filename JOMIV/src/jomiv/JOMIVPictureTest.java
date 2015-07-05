package jomiv;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;

import javax.swing.JOptionPane;


public class JOMIVPictureTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void generalPictureConstructorTest() {
		LinkedList<String> tags = new LinkedList<String>();
		tags.add("2013");
		tags.add("Summer Vacation");
		tags.add("The Lucky Shamrock Pool");
		tags.add("#TLSP2K13LOL");
		
		try {
			JOMIVPicture p = new JOMIVPicture(tags,
			"With my friend Roy at the pool",
			"Pool Picture #25",
			"C:\\Users\\arizona16\\Downloads\\balloon_kitty.jpg",
			"Columbia, SC, USA", new Date(), new Date(), new Date());
			p.toString(); //Just to get rid of the compile warning that
			//this class is never used.
		} catch (Exception e) {
			e.printStackTrace();
			fail("This exception happened: " + e.getMessage());
		}
	}
	
	@Test
	public void bareBonesPictureConstructorTest() {
		try {
			JOMIVPicture p = new JOMIVPicture("C:\\Not\\A\\Real\\Folder\\image.jpg");
			fail("We created a picture with an invalid file name!");
			p.toString();
		} catch (RuntimeException e) {
			//This exception is OK. We hope that this exception gets thrown.
		} catch (Exception e) {
			e.printStackTrace();
			fail("We threw an unwanted exception");
		}
	}

	@Test
	public void otherBareBonesPictureConstructorTest() {
		try {
			JOMIVPicture p = new JOMIVPicture(
					"C:\\Users\\arizona16\\Downloads\\tyvm.doc");
			fail("We created a picture from a .doc file!");
			p.toString();
		} catch (RuntimeException e) {
			//This exception is OK. In fact, we hope
			//fervently that this exception gets thrown.
		} catch (Exception e) {
			e.printStackTrace();
			fail("We threw an unwanted exception");
		}
	}

	@Test
	public void comparisonTest() {
		try {
			JOMIVPicture p = new JOMIVPicture(
			"C:\\Users\\arizona16\\Downloads\\balloon_kitty.jpg");
			
			assertTrue(p.equals(new JOMIVPicture(
			"C:\\Users\\arizona16\\Downloads\\balloon_kitty.jpg")));
			
			assertTrue(p.compareTo(new JOMIVPicture(
			"C:\\Users\\arizona16\\Downloads\\balloon_kitty.jpg")) == 0);

			assertTrue(p.compareTo(new JOMIVPicture(
			"My Title", "My Caption",
			"C:\\Users\\arizona16\\Downloads\\balloon_kitty.jpg")) == 0);
			
			assertTrue(p.equals(new JOMIVPicture(
			"My Title", "My Caption",
			"C:\\Users\\arizona16\\Downloads\\balloon_kitty.jpg")));
		} catch (Exception e) {
			e.printStackTrace();
			fail("We threw an unwanted exception");
		}
	}
	
	@Test
	public void imageIconTest() {
		JOMIVPicture p = new JOMIVPicture(
		"C:\\Users\\arizona16\\Downloads\\balloon_kitty.jpg");
		JOptionPane.showMessageDialog(null, "Hee hee (^_^)", "Lol kitty", JOptionPane.INFORMATION_MESSAGE, p.getImageIcon());
	}
}
