package jomiv;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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

	//Returns a check box that is selected by default and whose text
	//is this image's file path.
	private JCheckBox getNewCheckedCheckBox() {
		JCheckBox jcb = new JCheckBox();
		jcb.setText(this.filePath);
		jcb.setSelected(true);
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
				
				ImageIcon imageIcon = null;

				try {
					imageIcon = new ImageIcon(
							JOMIVPictureListItem.this.filePath);
					JOptionPane.showMessageDialog(null, "",
							JOMIVPictureListItem.this.filePath,
							JOptionPane.OK_OPTION, imageIcon);
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
