package jomiv;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class JOMIVViewer {

	public JOMIVViewer() {

	}

	public void viewRootDirs() {
		Iterable<Path> roots =
				FileSystems.getDefault().getRootDirectories();

		for (Path p : roots) {
			System.out.println(p);
		}
	}

	public int getAllImageFiles() {
		Iterable<Path> roots =
				FileSystems.getDefault().getRootDirectories();
		Iterator<Path> rootDirs = roots.iterator();

		String rootDirectoryValue = null;
		
		LinkedList<String> fileNames = new LinkedList<String>();

		//Create JFrame and put some little "Loading, please wait."
		//message on it. This message will be replaced with
		//important stuff later on.
		JFrame imageViewer = new JFrame();
		JPanel jp = new JPanel();
		jp.add(new JLabel("Finding your pictures. This may take a"
				+ " few minutes. Please be patient."));
		imageViewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		imageViewer.setTitle("Just One More Image Viewer (JOMIV)");
		imageViewer.getContentPane().add(jp);
		imageViewer.setVisible(true);
		imageViewer.pack();

		//Determine underlying OS type.
		//This part assumes that Windows, Mac, and
		//various Linux distros are the only OSes that are used in the world.
		String osName = System.getProperty("os.name").toLowerCase();
		
		boolean isWindows = osName.startsWith("windows");
		boolean isMac = osName.startsWith("apple");
		boolean isLinux = osName.startsWith("linux") ||
				(isWindows == false && isMac == false);
		
		while (rootDirs.hasNext()) {
			rootDirectoryValue = rootDirs.next().toString();
			
			if (isWindows) {
				rootDirectoryValue += "Users";
			}
			else if (isLinux) {
				rootDirectoryValue += "home";
			}
			else if (isMac) {
				System.err.println("Case of Mac not implemented!!!");
				System.exit(-1);
			}
			else {}
			
			getAllImageFiles(new File(rootDirectoryValue), fileNames);
		}

		imageViewer.getContentPane().remove(jp);
		
		jp = new JPanel();
		
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

		for (String fileName : fileNames) {
			jp.add(new JOMIVPictureListItem(fileName));
		}

		JScrollPane jsp = new JScrollPane(jp);

		imageViewer.getContentPane().add(jsp);

		imageViewer.pack();
		
		return fileNames.size();
	}

	public void getAllImageFiles(File currentFile,
	LinkedList<String> fileNames) {
	
		if (currentFile == null || currentFile.exists() == false) {
			return;
		}
		else if (fileNames == null) {
			return;
		}
		else if (currentFile.canRead()) {
			if (currentFile.isDirectory() &&
			currentFile.isHidden() == false && 
			currentFile.getAbsolutePath().contains(".") == false) {

				File filesInDirectory[] = currentFile.listFiles();

				//Can still be null even if currentFile.isDirectory()
				//returns true. Example: see recycle bin on Windows 8.1
				if (filesInDirectory != null) {
					for (int i = 0; i < filesInDirectory.length; i += 1) {
						getAllImageFiles(filesInDirectory[i], fileNames);
					}
				}
			}
			else if (currentFile.isFile() &&
					JOMIVPicture.hasImageFileExtension(
							currentFile.getAbsolutePath())) {
				fileNames.add(currentFile.getAbsolutePath());
			}
			else {
				//We encountered a non-image file or a file that we
				//cannot process. Do nothing.
			}
		}
		else {
			//We could not read this file. Do nothing.
		}
	}

	public static void main(String args[]) {
		JOMIVViewer jomivv = new JOMIVViewer();
		System.out.println("Processed " + jomivv.getAllImageFiles());
	}
}
