package jomba;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
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

/*
Copyright 2015 Andrew Zuelsdorf.
Licensed under GNU GPL version 3.0.

This file is part of JOMBA

JOMBA is free software:
you can redistribute it and/or modify it under the terms of the
GNU General Public License as published by the Free Software 
Foundation, either version 3 of the License, or (at your option)
any later version. This program is distributed in the hope that
it will be useful, but WITHOUT ANY WARRANTY; without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class JOMBAViewer {

	private static final int SEPARATION_PIXELS = 10;
	private static final String imageFileExtensions[] = {".png", ".jpg", ".svg", ".gif"};
	private static final String JOMBA_URL = "https://www.sourceforge.net/p/jomba";
	private static final String JOMBA_ABOUT = "JOMBA Copyright 2015 Andrew Zuelsdorf.\n"
			+ " Licensed under GNU GPL version 3.0. This program is free software:\n"
			+ " you can redistribute it and/or modify it under the terms of the\n"
			+ " GNU General Public License as published by the Free Software\n"
			+ "Foundation, either version 3 of the License, or (at your option)\n"
			+ " any later version. This program is distributed in the hope that\n"
			+ " it will be useful, but WITHOUT ANY WARRANTY; without even the\n"
			+ " implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR\n"
			+ " PURPOSE.  See the GNU General Public License at\n"
			+ "http://www.gnu.org/licenses for more information.";

	private LinkedList<String> fileNames;
	private JPanel pictureListItemPanel;
	private String saveDirectory, saveZipFileName;

	public JOMBAViewer() {
		fileNames = null;
		saveDirectory = null;
		saveZipFileName = null;
		pictureListItemPanel = null;
	}

	public void getDuplicateFiles(LinkedList<String> fileNames, LinkedList<String> duplicateFileNames) throws IOException {
		ListIterator<String> iter1 = fileNames.listIterator();
		ListIterator<String> iter2 = null;
		ListIterator<byte[]> iter3 = null;
		ListIterator<byte[]> iter4 = null;
		byte ithFileContents[] = null;
		String ithFileName = null;
		String jthFileName = null;

		//Get list of byte arrays of file contents.
		LinkedList<byte[]> fileContents = new LinkedList<byte[]>();

		while (iter1.hasNext()) {
			fileContents.add(Files.readAllBytes(Paths.get(iter1.next())));
		}
		
		iter3 = fileContents.listIterator();
		iter1 = fileNames.listIterator();
		
		while (iter1.hasNext() && iter3.hasNext()) {
			ithFileContents = iter3.next();
			ithFileName = iter1.next();
			
			iter4 = fileContents.listIterator();
			iter2 = fileNames.listIterator();
			
			while (iter4.hasNext() && iter2.hasNext()) {
				jthFileName = iter2.next();
			
				//Note: need a way to preserve one of two or more duplicated files. 
				if (!jthFileName.equals(ithFileName) && equals(iter4.next(), ithFileContents)) {
					System.out.printf("%s is a duplicate of %s\n",
							jthFileName, ithFileName);
					duplicateFileNames.add(ithFileName);
				}
			}
		}
	}
	
	protected boolean equals(byte array1[], byte array2[]) {
		if (array1.length != array2.length) {
			return false;
		}
		else {
			for (int i = 0; i < array1.length; i += 1) {
				if (array1[i] != array2[i]) {
					return false;
				}
			}
			return true;
		}
	}

	public int createAndShowGUI() {
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

		//Option to zip up files.
		JMenuItem jmiTarUp = new JMenuItem("Back up photos");

		//Will allow user to set save directory and zip file
		//name, then zip up files.
		jmiTarUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Get location for zip file. Return if none provided.
				if (!JOMBAViewer.this.setSaveDirectoryAndZipFileName()) {
					return;
				}

				//Let user know her time commitment and offer her
				//the chance to quit before she backs up her photos.
				switch (JOptionPane.showConfirmDialog(null,
						"Backing up your photos may take several minutes.\n" +
								"to complete. Click OK to begin. Click cancel to\n" +
								"end backup.", "Please be patient",
								JOptionPane.OK_CANCEL_OPTION)) {

								case JOptionPane.OK_OPTION:
									break;
								case JOptionPane.CANCEL_OPTION:
								default:
									return;
				}

				//Then put zip file in that location.
				LinkedList<String> rejected = JOMBAViewer.this.zipUpFiles();

				if (rejected == null || rejected.isEmpty()) {
					JOptionPane.showMessageDialog(null, String.format(
							"All done backing up your photos! You can find them at %s",
							saveDirectory + File.separatorChar + saveZipFileName));
				}
				else if (rejected != null) {
					String rejectedFilePaths = "";

					for (String filePath : rejected) {
						rejectedFilePaths = rejectedFilePaths + "\n" + filePath;
					}

					JOptionPane.showMessageDialog(null, String.format(
							"Sorry! The following photos could not be backed up: %s\nThe rest of your photos are at %s",
							rejectedFilePaths, saveDirectory + File.separatorChar + saveZipFileName));
				}

				JOptionPane.showMessageDialog(null,
						"Thanks for using JOMBA! At this time, you may close this program.",
						"Thanks!", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		jmFileMenu.add(jmiTarUp);
		jmFileMenu.add(jmiExitOption);

		jmb.add(jmFileMenu);

		//About bar
		JMenu jmAboutMenu = new JMenu("Help"); 

		JMenuItem jmiOnlineHelp = new JMenuItem("Online help");

		jmiOnlineHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						String.format("For help with JOMBA, please visit our website at %s",
								JOMBA_URL), "JOMBA Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		JMenuItem jmiAbout = new JMenuItem("About");

		jmiAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						String.format("%s",
								JOMBA_ABOUT), "JOMBA", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		jmAboutMenu.add(jmiOnlineHelp);
		jmAboutMenu.add(jmiAbout);
		jmb.add(jmAboutMenu);

		imageViewer.setJMenuBar(jmb);

		//Terms of Use for user to agree to.
		int value = JOptionPane.showConfirmDialog(null,
				"JOMBA Copyright 2015 Andrew Zuelsdorf\n" +
						"This program comes with ABSOLUTELY NO WARRANTY.\n" +
						"Licensed under GNU General Public License v3.0\n" +
						"For more information, visit http://www.gnu.org/licenses.\n" +
						"By clicking \"Yes\", you agree to the terms in the GNU\n" +
						"General Public License. Click \"No\" to exit this program.",
						"Terms of Use", JOptionPane.YES_NO_OPTION);

		//Did user agree to Terms of Use?
		if (value != JOptionPane.YES_OPTION) {
			return 0; //The user did not agree
			//to the terms of use. End this program.
		}

		//Put some little "Loading, please wait."
		//message in JFrame. This message will be replaced with
		//important stuff later on.
		pictureListItemPanel = new JPanel();
		pictureListItemPanel.setLayout(new BoxLayout(pictureListItemPanel, BoxLayout.Y_AXIS));
		pictureListItemPanel.add(new JLabel("Finding your pictures. This may take a"
				+ " few minutes. Please be patient."));
		imageViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		imageViewer.setTitle("Just One More Backup Application (JOMBA)");
		imageViewer.getContentPane().add(pictureListItemPanel);
		imageViewer.pack();
		imageViewer.setVisible(true);

		getAllImageFiles(new File(System.getProperty("user.home")), fileNames);

		LinkedList<String> duplicateFileNames = new LinkedList<String>();

		try {
			this.getDuplicateFiles(fileNames, duplicateFileNames);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//Get rid of "Please be patient" message.
		imageViewer.getContentPane().remove(pictureListItemPanel);
		pictureListItemPanel = new JPanel();

		//Add Picture List
		pictureListItemPanel.setLayout(new BoxLayout(pictureListItemPanel, BoxLayout.Y_AXIS));

		for (String fileName : fileNames) {
			JOMBAPictureListItem jpli = new JOMBAPictureListItem(fileName);
			jpli.setBorder(BorderFactory.createEmptyBorder(
					SEPARATION_PIXELS / 2, 0, SEPARATION_PIXELS / 2, 0));
			pictureListItemPanel.add(jpli);
		}

		JScrollPane jsp = new JScrollPane(pictureListItemPanel);
		imageViewer.getContentPane().add(jsp);

		GraphicsDevice gd = 
				GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();

		int width = gd.getDisplayMode().getWidth() / 2;
		int height = gd.getDisplayMode().getHeight() / 2;
		imageViewer.setSize(width, height);

		//Tell the user what to do in order to back up their photos.
		JOptionPane.showMessageDialog(null, "Welcome to JOMBA! Any photos"
				+ " that have check marks next to them will be backed up.\n"
				+ "If you don't want to back up a photo, click the check box"
				+ " next to it. The check mark\nshould disappear when you do this."
				+ " When you are finished selecting your photos, go to\n"
				+ " \"File\" and click \"Back up photos\".\n", "Hello!",
				JOptionPane.INFORMATION_MESSAGE);

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

		jfc.setFileFilter(new FileNameExtensionFilter("Zip files (.zip)", "zip"));

		JOptionPane.showMessageDialog(null, "Hello again! JOMBA will now back up your photos by"
				+ " putting them into a zip file\nthat you can keep on your computer, save to a thumb drive,"
				+ " upload to cloud\nstorage, email to yourself, or email to other people."
				+ " To create this zip file,\n"
				+ "just choose a folder, type a name for your zip"
				+ " file in the text box, and press OK.", "Back Up Photos",
				JOptionPane.INFORMATION_MESSAGE);

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

					if (!JOMBAViewer.canWriteToDirectory(jfc.getSelectedFile().getParentFile())) {
						JOptionPane.showMessageDialog(null, "Sorry! JOMBA"
								+ " cannot create a zip archive there. Please"
								+ " choose another place.", "Sorry!",
								JOptionPane.INFORMATION_MESSAGE);
					}
					else if (new File(saveDirectory +
							File.separatorChar + saveZipFileName).exists()) {

						int retVal2 = JOptionPane.showConfirmDialog(null,
								"\"" + saveZipFileName + "\" already exists.\n"
										+ "Backing up there will get rid of any information in that file.\n"
										+ "Do you want to do that?",
										"Are you sure?", JOptionPane.YES_NO_OPTION);

						if (retVal2 == JOptionPane.YES_OPTION) {
							valid = true;
						}
					}
					else {
						valid = true;
					}
				}
				catch (NullPointerException ex) {
					JOptionPane.showMessageDialog(null, "Sorry! JOMBA cannot create"
							+ " a zip archive there. Please choose another folder.",
							"Sorry!", JOptionPane.INFORMATION_MESSAGE);
				}
				break;
			default:
				return false;
			}
		}

		return true;
	}

	protected LinkedList<String> getSelectedPhotos() {
		LinkedList<String> selectedPhotos = new LinkedList<String>();

		for (Component c : pictureListItemPanel.getComponents()) {
			if (c instanceof JOMBAPictureListItem) {
				if (((JOMBAPictureListItem)c).isSelected()) {
					selectedPhotos.add(((JOMBAPictureListItem)c).getFilePath());
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
					hasImageFileExtension(
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

	public static boolean hasImageFileExtension(String filePath) {
		if (filePath == null) {
			return false;
		}

		for (int i = 0; i < imageFileExtensions.length; i += 1) {
			if (filePath.endsWith(imageFileExtensions[i])) {
				return true;
			}
		}

		return false;
	}

	public static void main(String args[]) {
		JOMBAViewer jomivv = new JOMBAViewer();
		jomivv.createAndShowGUI();
	}
}
