package jomiv;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
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

		//Determine underlying OS type.
		//This part assumes that only POSIX-type, Mac, and Windows
		//OSes are used in the world.
		String osName = System.getProperty("os.name").toLowerCase();
		
		boolean isWindows = osName.startsWith("windows");
		boolean isMac = osName.startsWith("apple");
		boolean isPOSIX = osName.startsWith("linux") ||
				(isWindows == false && isMac == false);
		
		while (rootDirs.hasNext()) {
			rootDirectoryValue = rootDirs.next().toString();
			
			if (isWindows) {
				rootDirectoryValue += "Users";
			}
			else if (isPOSIX) {
				rootDirectoryValue += "home";
			}
			else if (isMac) {
				throw new RuntimeException("Case of Mac not implemented!!!");
			}
			else {}
			
			//System.out.println("Root Directory value: " + rootDirectoryValue);
			getAllImageFiles(new File(rootDirectoryValue), fileNames);
		}

		JFrame imageViewer = new JFrame();

		JPanel jp = new JPanel();
		
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

		for (String fileName : fileNames) {
			jp.add(new JOMIVPictureListItem(fileName));
		}

		JScrollPane jsp = new JScrollPane(jp);

		imageViewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		imageViewer.setTitle("Just One More Image Viewer");

		imageViewer.getContentPane().add(jsp);

		imageViewer.setVisible(true);

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

				System.out.println(currentFile.getAbsolutePath());
			}
			else if (currentFile.isFile() == false) {
				System.err.println("Encountered entity that is neither "
						+ "directory nor file. Absolute pathname is " + 
						currentFile.getAbsolutePath());
			}
			else {
				//We encountered a non-image file. Do nothing.
			}
		}
		else {
			System.err.println("Could not read file \"" +
					currentFile.getAbsolutePath() + "\"");
		}
	}

	public static void main(String args[]) {
		JOMIVViewer jomivv = new JOMIVViewer();
		System.out.println("Processed " + jomivv.getAllImageFiles());
	}
}
