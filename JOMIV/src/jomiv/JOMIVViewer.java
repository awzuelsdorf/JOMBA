package jomiv;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JOMIVViewer {

	private LinkedList<String> fileNames;
	private JPanel pictureListItemPanel;
	private String saveDirectory, saveZipFileName;

	public JOMIVViewer() {
		fileNames = null;
		saveDirectory = null;
		saveZipFileName = null;
		pictureListItemPanel = null;
	}

	public int createAndShowGUI() {
		Iterable<Path> roots =
				FileSystems.getDefault().getRootDirectories();
		Iterator<Path> rootDirs = roots.iterator();

		String rootDirectoryValue = null;

		this.fileNames = new LinkedList<String>();

		//Create JFrame and put some. 
		JFrame imageViewer = new JFrame();

		//Create a file menu and put it at the top of the JFrame.
		JMenuBar jmb = new JMenuBar();
		JMenu jmFileMenu = new JMenu("File");

		//The obligatory exit button that almost no one has used since 1997
		JMenuItem jmiExitOption = new JMenuItem("Exit");
		jmiExitOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean confirmed = JOptionPane.YES_OPTION ==
						JOptionPane.showConfirmDialog(null,
								"Are you sure you want to quit?");
				if (confirmed) {
					imageViewer.dispose();
				}
			}
		});
		jmFileMenu.add(jmiExitOption);

		//Option to zip up files.
		JMenuItem jmiTarUp = new JMenuItem("Back up photos");

		//Will allow user to set save directory and zip file
		//name, then zip up files.
		jmiTarUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Get location for zip file. Return if none provided.
				if (!JOMIVViewer.this.setSaveDirectoryAndZipFileName()) {
					return;
				}

				//Then put zip file in that location.
				LinkedList<String> rejected = JOMIVViewer.this.zipUpFiles();

				if (rejected != null && rejected.isEmpty()) {
					JOptionPane.showMessageDialog(null, String.format(
							"All done backing up your photos! You can find them at %s. Thanks!",
							saveDirectory + File.separatorChar + saveZipFileName));
				}
				else {
					System.out.println(rejected);
				}
			}
		});

		jmFileMenu.add(jmiTarUp);

		jmb.add(jmFileMenu);
		imageViewer.setJMenuBar(jmb);

		//Put some little "Loading, please wait."
		//message in JFrame. This message will be replaced with
		//important stuff later on.
		pictureListItemPanel = new JPanel();
		pictureListItemPanel.add(new JLabel("Finding your pictures. This may take a"
				+ " few minutes. Please be patient."));
		imageViewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		imageViewer.setTitle("Just One More Image Viewer (JOMIV)");
		imageViewer.getContentPane().add(pictureListItemPanel);
		imageViewer.pack();
		imageViewer.setVisible(true);

		//Determine underlying OS type.
		//This part assumes that Windows, Mac, and
		//various Linux distros are the only OSes that are used in the world.
		String osName = System.getProperty("os.name").toLowerCase();

		boolean isWindows = osName.toLowerCase().startsWith("windows");
		boolean isLinux = osName.toLowerCase().startsWith("linux");
		boolean isMac = osName.toLowerCase().startsWith("mac") ||
				(isWindows == false && isLinux == false);

		while (rootDirs.hasNext()) {
			rootDirectoryValue = rootDirs.next().toString();

			if (isWindows) {
				rootDirectoryValue += "Users";
			}
			else if (isLinux) {
				rootDirectoryValue += "home";
			}
			else if (isMac) {
				rootDirectoryValue += "Users";
			}
			else {}

			getAllImageFiles(new File(rootDirectoryValue), fileNames);
		}

		//Get rid of "Please be patient" message.
		imageViewer.getContentPane().remove(pictureListItemPanel);
		pictureListItemPanel = new JPanel();

		//Add Picture List
		pictureListItemPanel.setLayout(new BoxLayout(pictureListItemPanel, BoxLayout.Y_AXIS));
		
		for (String fileName : fileNames) {
			pictureListItemPanel.add(new JOMIVPictureListItem(fileName));
		}

		JScrollPane jsp = new JScrollPane(pictureListItemPanel);
		imageViewer.getContentPane().add(jsp);

		GraphicsDevice gd = 
				GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();

		int width = gd.getDisplayMode().getWidth() / 2;
		int height = gd.getDisplayMode().getHeight() / 2;
		imageViewer.setSize(width, height);

		//Return number of images this computer had in
		//this user's directory.
		return fileNames.size();
	}

	//Because the canWrite() method for the
	//Java File class often returns false positives
	//for directories under Windows's file systems.
	public static boolean canWriteToDirectory(File f) {
		if (f == null) {
			throw new IllegalArgumentException("f must not be null!");
		}
		else if (f.isDirectory() == false) {
			throw new IllegalArgumentException(
					String.format("%s must be a directory, was a file.",
							f.getAbsolutePath()));
		}
		else {
			//Create a unique file in the directory.
			String fDir = f.getAbsolutePath() + File.separatorChar;
			long number = 0;

			while (new File(fDir + number + ".txt").exists()) {
				number += 1;
			}

			//And then immediately delete it. If we can do this,
			//then we can write to the directory. If not, then
			//we cannot write to the directory.
			try {
				new File(fDir + number + ".txt").createNewFile();
				new File(fDir + number + ".txt").delete();
				return true;
			} catch (Exception ex) {
				return false;
			}
		}
	}

	//Ask user to find directory to save zip
	//file and choose name for zip file.
	//Returns false if user did not choose file or directory.
	//Returns true otherwise.
	public boolean setSaveDirectoryAndZipFileName() {
		boolean valid = false;

		int retVal;

		JFileChooser jfc = new JFileChooser();

		jfc.setFileFilter(new FileNameExtensionFilter("zip files", "zip"));

		JOptionPane.showMessageDialog(null, "Hello! To export your photos"
				+ " to a zip file, "
				+ "just choose a folder, choose a name for your zip"
				+ " file, and press OK.");

		while (!valid) {

			retVal = jfc.showSaveDialog(null);

			switch (retVal) {
			case JFileChooser.CANCEL_OPTION:
				return false;
			case JFileChooser.APPROVE_OPTION:
				try {
					saveDirectory = jfc.getSelectedFile().getParent();
					saveZipFileName = jfc.getSelectedFile().getName();

					if (!saveZipFileName.endsWith(".zip")) {
						saveZipFileName += ".zip";
					}

					if (!JOMIVViewer.canWriteToDirectory(jfc.getSelectedFile().getParentFile())) {
						JOptionPane.showMessageDialog(null, "Sorry! JOMIV"
								+ " cannot create a zip archive there. Please"
								+ " choose another place.", "Sorry!",
								JOptionPane.INFORMATION_MESSAGE);
					}
					else {
						valid = true;
					}
				}
				catch (NullPointerException ex) {
					JOptionPane.showMessageDialog(null, "Sorry! JOMIV cannot create"
							+ " a zip archive there. Please choose another place.",
							"Sorry!", JOptionPane.INFORMATION_MESSAGE);
				}
				break;
			default:
				JOptionPane.showMessageDialog(null, "Sorry! JOMIV had an "
						+ "unexpected problem. Please try again.",
						"Sorry!", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
		}

		return true;
	}
	
	protected LinkedList<String> getSelectedPhotos() {
		LinkedList<String> selectedPhotos = new LinkedList<String>();
	
		for (Component c : pictureListItemPanel.getComponents()) {
			if (c instanceof JOMIVPictureListItem) {
				if (((JOMIVPictureListItem)c).isSelected()) {
					selectedPhotos.add(((JOMIVPictureListItem)c).getFilePath());
				}
			}
		}
		
		return selectedPhotos;
	}
	
	//Now that we have dragged a file path out of the user, zip up the files.
	//Return any files that could not be written to the zip file.
	public LinkedList<String> zipUpFiles() {
		//Determine which photos are still selected in the viewer.
		//These are the photos we will zip up.
		LinkedList<String> selectedPhotos = getSelectedPhotos();
		
		//File names actually file paths. 
		ZipFileCreator zfc = new ZipFileCreator(selectedPhotos,
				saveDirectory + File.separatorChar + saveZipFileName);
		
		return zfc.writeAllFilesIntoZipOutputStream();
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
		jomivv.createAndShowGUI();
	}
}
