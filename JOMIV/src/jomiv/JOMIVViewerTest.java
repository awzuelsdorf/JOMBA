package jomiv;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JOMIVViewerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		JOMIVViewer viewer = new JOMIVViewer();
		
		viewer.getAllImageFiles();
	}
}
