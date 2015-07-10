package jomiv;

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

public class JOMIVViewer {

	private LinkedList<String> fileNames;
	private String saveDirectory, saveZipFileName;

	public JOMIVViewer() {
		fileNames = null;
		saveDirectory = null;
		saveZipFileName = null;
	}

	public int getAllImageFiles() {
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

		//The obligatory exit button that almost no one over the age of
		//ten has used since 1997
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
				//Get location for zip file.
				JOMIVViewer.this.setSaveDirectoryAndZipFileName();
				//Then put zip file in that location.
				LinkedList<String> rejected = JOMIVViewer.this.zipUpFiles();

				if (rejected != null && rejected.isEmpty()) {
					JOptionPane.showMessageDialog(null, String.format(
							"All done backing up your photos! You can find them at %s. Thanks!",
							saveDirectory + File.separatorChar + saveZipFileName));
				}
				else {
					String msg = "All done backing up your photos! "
							+ "In your zip file, we had to rename ";

					long i = 0;
					for (String s : rejected) {
						msg = msg + i + ") \"" + s + "\" to be \"" + s + "\" ";
						i += 1;
					}
				}
			}
		});

		jmFileMenu.add(jmiTarUp);

		jmb.add(jmFileMenu);
		imageViewer.setJMenuBar(jmb);

		//Put some little "Loading, please wait."
		//message in JFrame. This message will be replaced with
		//important stuff later on.
		JPanel jp = new JPanel();
		jp.add(new JLabel("Finding your pictures. This may take a"
				+ " few minutes. Please be patient."));
		imageViewer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		imageViewer.setTitle("Just One More Image Viewer (JOMIV)");
		imageViewer.getContentPane().add(jp);
		imageViewer.pack();
		imageViewer.setVisible(true);

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

		//Get rid of "Please be patient" message.
		imageViewer.getContentPane().remove(jp);
		jp = new JPanel();

		//Add Picture List
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

		for (String fileName : fileNames) {
			jp.add(new JOMIVPictureListItem(fileName));
		}

		JScrollPane jsp = new JScrollPane(jp);
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
	public void setSaveDirectoryAndZipFileName() {
		boolean areWeDoneYet = false;

		boolean isFirstIteration = true;

		//Get user to select a directory.
		while (!areWeDoneYet) {

			//Did the user select a directory previously?
			areWeDoneYet = this.saveDirectory != null;

			//Yes
			if (areWeDoneYet) {
				areWeDoneYet = JOptionPane.YES_OPTION != JOptionPane
						.showConfirmDialog(null, "You have already selected the folder " +
								saveDirectory + " to back up your photos in. Click \"yes\""
								+ " to back up your photos in another folder. Click "
								+ "\"no\" to stick with this one.", "Question",
								JOptionPane.YES_NO_OPTION);
			}
			//No. Show the user this message if this is the first time
			//they are using this function.
			else if (isFirstIteration) {
				JOptionPane.showMessageDialog(null, "JOMIV will back up your photos"
						+ " by creating a zip file that you name in a directory "
						+ "that you choose. From there, you can transfer your zip"
						+ " file to a thumb drive, upload it to cloud storage, or attach it to "
						+ "an email and send it to yourself or others. But first,"
						+ " you must select a folder to save your zip file in.",
						"Information", JOptionPane.INFORMATION_MESSAGE);
			}

			JFileChooser saveDirChooser = new JFileChooser();
			saveDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			//The user must (or wants to) select a directory.
			//Will only print error message if user does not choose a directory.
			if (!areWeDoneYet && JFileChooser.APPROVE_OPTION == saveDirChooser.showSaveDialog(null)){
				//User did not choose a directory.
				if (saveDirChooser.getSelectedFile() == null) {
					JOptionPane.showMessageDialog(null, "Sorry!" + 
							" You must select a folder for your zip " + 
							"file to go in before JOMIV can back up your photos."
							, "Oops!", JOptionPane.INFORMATION_MESSAGE);
				}
				//User chose a directory
				else {
					//Is the directory writable?

					//Yes
					if (JOMIVViewer.canWriteToDirectory(saveDirChooser.getSelectedFile())) {
						saveDirectory = saveDirChooser.getSelectedFile().getAbsolutePath();
						areWeDoneYet = true;
					}
					//No
					else {
						JOptionPane.showMessageDialog(null,
								"Sorry! The computer will not let JOMIV create a zip file in that directory. Please choose another one.",
								"Sorry!", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			//User did not choose a directory.
			else {
				JOptionPane.showMessageDialog(null, "Sorry!" + 
						" You must select a folder for your zip " + 
						"file to go in before JOMIV can back up your photos."
						, "Oops!", JOptionPane.INFORMATION_MESSAGE);
			}
			isFirstIteration = false;
		}

		areWeDoneYet = false;
		isFirstIteration = true;

		//Get zip file name from user.
		while (!areWeDoneYet) {

			//Did the user previously select a name for the zip file?
			areWeDoneYet = saveZipFileName != null;

			//Yes
			if (areWeDoneYet) {
				areWeDoneYet = JOptionPane.YES_OPTION != JOptionPane
						.showConfirmDialog(null, "You have already selected the name " +
								this.saveZipFileName + " for a previous zip file. Click \"yes\""
								+ " to use this same name for this zip file. Click "
								+ "\"no\" to stick with this one.", "Question",
								JOptionPane.YES_NO_OPTION);
			}
			else {
				this.saveZipFileName = JOptionPane.showInputDialog(
						"Please type a name for your zip file in the box below.");
				isFirstIteration = false;
			}

			//User did not choose a name.
			if (saveZipFileName == null || saveZipFileName.compareTo("") == 0) {
				JOptionPane.showMessageDialog(null, "Sorry!" + 
						" You must give your zip " + 
						"file a name before JOMIV can back up your photos.",
						"Oops!", JOptionPane.INFORMATION_MESSAGE);
				areWeDoneYet = false;
			}
			else {
				//We are done...assuming that the user either is
				//creating a new file or wants to overwrite the old file.
				areWeDoneYet = true;
			}

			//User chose a file
			if (areWeDoneYet) {
				//Add .zip extension to file name...if file name does not
				//already have such an extension.
				if (saveZipFileName.endsWith(".zip") == false) {
					saveZipFileName += ".zip";
				}

				//Does this file exist?
				File temp = new File(this.saveDirectory + File.separatorChar +
						this.saveZipFileName);

				//Whether we are done or not depends upon 1)
				//whether the user selected a file or not 2) whether the user's
				//selected zip file exists and 3) whether the user wants to
				//write over the selected zip file's contents. If the zip file
				//does not exist, then assume that the user wants to write
				//over its (nonexistent) contents it.
				if (areWeDoneYet && temp.exists()) {
					areWeDoneYet = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,
							"It looks like that file, \"" + temp.getAbsolutePath() +
							"\", already exists. Do you want to overwrite it?", "Question",
							JOptionPane.OK_CANCEL_OPTION);
					if (areWeDoneYet == false) {
						//If user does not want to overwrite old file, then set the zip
						//file name back to null.
						saveZipFileName = null;
					}
				}
			}

			isFirstIteration = false;
		}
	}

	//Now that we have dragged a file path out of the user, zip up the files.
	//Return any files that could not be written to the zip file.
	public LinkedList<String> zipUpFiles() {
		//File names actually file paths. 
		ZipFileCreator zfc = new ZipFileCreator(fileNames,
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
		System.out.println("Processed " + jomivv.getAllImageFiles());
	}
}
