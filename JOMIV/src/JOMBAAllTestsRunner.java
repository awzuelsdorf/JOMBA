

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JOMBAAllTestsRunner {
	public static void main(String args[]) {
		Result r = JUnitCore.runClasses(JOMBAViewerTest.class);

		System.err.println(
				String.format(
						"Number of failures in JOMIVViewerTest: %d",
						r.getFailureCount()));

		for (Failure f : r.getFailures()) {
			System.err.println(f.toString());
		}
		
		r = JUnitCore.runClasses(ZipFileCreatorTest.class);
		
		System.err.println(String.format(
		"Number of failures in ZipFileCreatorTest: %d",
		r.getFailureCount()));
		
		for (Failure f : r.getFailures()) {
			System.err.println(f.toString());
		}
	}
}
