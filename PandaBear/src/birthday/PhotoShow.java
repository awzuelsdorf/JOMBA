/*	This file is part of Photo Synthesis.

	Copyright 2015 Andrew Zuelsdorf. Licensed under GNU GPL

    Photo Synthesis is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PhotoShow is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PhotoShow.  If not, see <http://www.gnu.org/licenses/>.
*/

package birthday;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PhotoShow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4700348374006729144L;

	private String photoFolderPath, separator;
	private String photoFileNames[];
	private JPanel photoPanel;
	private int photoNumber;
	private boolean showReminder;

	public PhotoShow(String title) {
		this(true, title);
	}

	public PhotoShow(boolean showReminder, String title) {
		super(title);
		this.showReminder = showReminder;
		photoPanel = null;
		photoNumber = 0;

		this.setVisible(false); //Do not reveal GUI yet.
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false); // disable the "All files" option.

		do {
			jfc.showOpenDialog(this);
			if (jfc.getSelectedFile() != null && jfc.getSelectedFile().isDirectory()) {
				photoFolderPath = jfc.getSelectedFile().getAbsolutePath();
			}
			else {
				JOptionPane.showMessageDialog(null, "Sorry, you need to select a DIRECTORY of photos.");
				photoFolderPath = null;
			}
		} while (photoFolderPath == null);

		photoFileNames = new File(photoFolderPath).list();
		separator = File.separatorChar + "";
		this.addKeyListener(new PhotoShowEventHandler());
	}

	public void createAndDisplayGUI() {
		getIthPhoto(photoNumber++);
		this.setVisible(true);
		if (this.showReminder) {
			JOptionPane.showMessageDialog(null,
			"Use the left arrow key to view the previous image again,"
			+ " and use the right arrow key to view the next image.", "How to use PhotoShow",
			JOptionPane.INFORMATION_MESSAGE);
		}
	}

	protected void getIthPhoto(int i) {
		if (i < 0) {
			i = 0;
		}
		else if (i >= this.photoFileNames.length) {
			i = i % photoFileNames.length;
		}


		int originalValue = i; //So we can detect when we have reached
		//the end of the circular buffer.

		while (!photoFileNames[i].toLowerCase().endsWith(".jpg") &&
				!photoFileNames[i].toLowerCase().endsWith(".png")) {
			i = (i + 1) % photoFileNames.length;

			if (i == originalValue) {
				JOptionPane.showMessageDialog(null, "No image files (.jpg, .png) found! Exiting now", "Message from PhotoShow", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}

		if (this.photoPanel != null) {
			this.remove(this.photoPanel);
		}

		this.photoPanel = new Photo("", "",
				this.photoFolderPath + this.separator +
				this.photoFileNames[i]).getPictureRepresentation();

		this.add(this.photoPanel);
		this.pack();
		this.photoNumber = i;
	}

	private class Photo {
		private ImageIcon photo;
		private String title, caption, fileName;	
		private String text;

		public Photo(String title, String caption, String fileName) {
			this.fileName = fileName;

			if (fileName != null) {
				photo = new ImageIcon(this.fileName, caption);
				if (photo == null) {
					text = String.format("%s is not a recognized image file.", this.fileName);
				}
				else {
					text = "";
				}
			}
			this.caption = caption;
			this.title = title;
		}

		public JPanel getPictureRepresentation() {
			JLabel title = new JLabel(this.title);
			JLabel caption = new JLabel(this.caption);
			JLabel photo = new JLabel(this.text, this.photo, JLabel.CENTER);

			JPanel representation = new JPanel();
			representation.add(title);
			representation.add(photo);
			representation.add(caption);

			return representation;
		}
	}

	private class PhotoShowEventHandler implements KeyListener {

		public PhotoShowEventHandler() {}

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				PhotoShow.this.getIthPhoto(--PhotoShow.this.photoNumber);
				break;
			case KeyEvent.VK_RIGHT:
				PhotoShow.this.getIthPhoto(++PhotoShow.this.photoNumber);
				break;
			default:
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}

		@Override
		public void keyTyped(KeyEvent e) {}
	}
}
