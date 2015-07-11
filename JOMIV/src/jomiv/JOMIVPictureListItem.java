package jomiv;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class JOMIVPictureListItem extends JPanel {

	private static final long serialVersionUID = 5805157205657888477L;
	private String filePath;
	private JCheckBox backup;
	private JButton viewPictureButton;
	
	public JOMIVPictureListItem(String filePath) {
		super();
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setAlignmentX(Component.LEFT_ALIGNMENT);
		
		if (filePath == null) {
			throw new IllegalArgumentException("null file path!");
		}
		else {
			if (new File(filePath).exists() == false) {
				throw new IllegalArgumentException(
						"Nonexistent file path: \"" + filePath + "\".");
			}
			this.filePath = new File(filePath).getAbsolutePath();
		}
		backup = getNewCheckedCheckBox();
		viewPictureButton = getNewViewPictureButton();
		add(backup);
		add(viewPictureButton);
	}

	public boolean isSelected() {
		return backup != null ? backup.isSelected() : false;
	}
	
	public String getFilePath() {
		return String.format("%s", this.filePath);
	}
	
	public String getFileName() {
		//Find location of last separatorChar in our file path.
		int i = filePath.length();
		
		while (i > 0 && filePath.charAt(--i) != File.separatorChar);

		//The file path ends in a separator character. So whether
		//there is a file name is academic. Return null.
		if (i == filePath.length() - 1) {
			return null;
		}
		//No separator char. The file path *is* the file name.
		//Return a copy of it.
		else if (i == 0) {
			return String.format("%s", filePath);
		}
		//The last separator char is somewhere in the file path.
		//Return the substring from i + 1 (character at i is the
		//separator. Is not part of the file name) to
		//filePath.length() - 1
		else {
			return filePath.substring(i + 1, filePath.length());
		}
	}

	//Returns a check box that is selected by default and whose text
	//is this image's file path.
	private JCheckBox getNewCheckedCheckBox() {
		JCheckBox jcb = new JCheckBox();
		jcb.setText(this.filePath);
		jcb.setSelected(true);
		jcb.setToolTipText("Checking this box means that this picture"
				+ " will be backed up if you choose \"Zip Up Photos\""
				+ " from the menu above.");

		jcb.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.err.println(JOMIVPictureListItem.this.isSelected());
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
		});
		
		
		return jcb;
	}
	
	private JButton getNewViewPictureButton() {
		JButton viewPictureButton = new JButton("View Picture");
		
		viewPictureButton.setToolTipText("See this picture in another window");
		
		viewPictureButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				//Create imageIcon only if the user wants to view it; do
				//not store an ImageIcon as an instance variable.
				
				ImageIcon imageIcon = null; //To show the picture
				JLabel jl = null; //To hold the ImageIcon
				JScrollPane jsp = null; //To hold the JLabel

				try {
					imageIcon = new ImageIcon(
							JOMIVPictureListItem.this.filePath);
				
					jl = new JLabel(imageIcon);
					jsp = new JScrollPane(jl);
					
					//Create window to show picture in.
					JFrame imageBrowser = new JFrame();
					imageBrowser.getContentPane().add(jsp);
					imageBrowser.setTitle(JOMIVPictureListItem.this.filePath);
					imageBrowser.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					
					//Do not pack or else bottom scroll bar will be hidden
					//by task bar on Windows 8.1. Instead, arbitrarily set to 1/4 area of screen.
					GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
					int width = gd.getDisplayMode().getWidth() / 2;
					int height = gd.getDisplayMode().getHeight() / 2;
					
					imageBrowser.pack();
					imageBrowser.setSize(width, height);
					imageBrowser.setVisible(true);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null,
							"Sorry! Due to a rendering error, we"
							+ "could not show this image.", "Sorry!",
							JOptionPane.INFORMATION_MESSAGE);
				}
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}
		});
		
		return viewPictureButton; 
	}
}
