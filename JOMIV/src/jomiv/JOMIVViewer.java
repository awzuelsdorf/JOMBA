package jomiv;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import javax.swing.BoxLayout;
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
	
	public JOMIVViewer() {
		fileNames = null;
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
		JMenuItem jmiTarUp = new JMenuItem("Tar up files");
		
		jmiTarUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zipUpFiles();
			}
		});
		
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
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth() / 2;
		int height = gd.getDisplayMode().getHeight() / 2;
		imageViewer.setSize(width, height);
		
		//Return number of images this computer had in
		//this user's directory.
		return fileNames.size();
	}

	public void zipUpFiles() {
		try {
			// Encode a String into bytes
		     String inputString = "blahblahblah";
		     byte[] input = inputString.getBytes("UTF-8");

		     // Compress the bytes
		     byte[] output = new byte[100];
		     Deflater compresser = new Deflater();
		     compresser.setInput(input);
		     compresser.finish();
		     int compressedDataLength = compresser.deflate(output);
		     compresser.end();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
