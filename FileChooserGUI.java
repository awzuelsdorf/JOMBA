package main;
/*********************************************************************
*
*      Copyright (C) 2015 Andrew Zuelsdorf

*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
***************************************************************************/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChooserGUI extends JFrame {
	private static final long serialVersionUID = -5792703945370631752L;
	private static final String DEFAULT_FILENAME = "___________";
	private static final String LEGALESE =
"This library is free software; you can redistribute it and/or\n"+
"modify it under the terms of the GNU Lesser General Public\n"+
"License as published by the Free Software Foundation; either\n"+
"version 2.1 of the License, or (at your option) any later version.\n"+
"This library is distributed in the hope that it will be useful,\n"+
"but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU\n"+
"Lesser General Public License for more details.\n"+
"You should have received a copy of the GNU Lesser General Public\n"+
"License along with this library; if not, write to the Free Software\n"+
"Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA\n"
+ "\nYou MUST be at least eighteen (18) years of age to use this software.\n\n"
+ "If you click \"OK\", then you and your legal guardians, if any, agree\n"+
"to the above terms and conditions\n";



	private JPanel bottomPanelPtr, topPanelPtr,
	browseButtonsPanel, fileNamesPanel, instructionsPanel, guiPanel;
	private ArrayList<JLabel> fileNamePtrs;
	private JFileChooser jfc;
	private static final String NAME_OF_PROJECT = "Merge Without Macros (MWM)";
	private JButton moreSpreadsheetsButton, mergeButton;

	public FileChooserGUI() {
		super(NAME_OF_PROJECT);
		jfc = new JFileChooser();
		//browseButtonPtrs = new Vector<JButton>();
		fileNamePtrs = new ArrayList<JLabel>();
		//Not sure that jxl.jar can process .xlsx files.
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Microsoft Excel 97/2000/XP/2003 (.xls)", "xls");
		jfc.setFileFilter(filter);
	}

	protected void addAnotherFileSlot() {
		addAnotherFileSlot(DEFAULT_FILENAME);
	}

	protected void initInstructionsPanel() {
		instructionsPanel = new JPanel();
		instructionsPanel.setSize(new Dimension(
				instructionsPanel.getHeight(),
				topPanelPtr.getWidth()));
		instructionsPanel.setLayout(new BoxLayout(instructionsPanel,
				BoxLayout.Y_AXIS));
		instructionsPanel.add(new JLabel("Greetings! Welcome to Merge Without Macros."));
		instructionsPanel.add(new JLabel("Here's how to use this program:"));
		instructionsPanel.add(new JLabel(" ")); //Whitespace
		instructionsPanel.add(new JLabel(String.format("1) Click any of the"
				+ " \"browse\" buttons that have \"%s\"",
				FileChooserGUI.DEFAULT_FILENAME)));
		instructionsPanel.add(new JLabel(" to the left of them."));
		instructionsPanel.add(new JLabel(" ")); //Whitespace

		instructionsPanel.add(new JLabel("2) Select a .xls file from the file"
				+ " chooser that pops up."));
		instructionsPanel.add(new JLabel("After this, you should see the file name on the"
				+ " left side"));
		instructionsPanel.add(new JLabel("of the window."));
		instructionsPanel.add(new JLabel(" ")); //Whitespace

		instructionsPanel.add(new JLabel("3) Repeat steps 1 and 2 until you "
				+ "have selected all the"));
		instructionsPanel.add(new JLabel("spreadsheets that you want to join."
				+ " If you want to remove a file"));
		instructionsPanel.add(new JLabel("from the list of files to join, "
				+ "then click the corresponding"));
		instructionsPanel.add(new JLabel("browse button. Then, "
				+ "click \"Cancel\". If you need to join more than          "));
		instructionsPanel.add(new JLabel("three sp"
				+ "readsheets, then press the \"More Files\" button."));
		instructionsPanel.add(new JLabel(" ")); //Whitespace

		instructionsPanel.add(new JLabel("4) When you have selected all the"
				+ " files that you want to merge,"));
		instructionsPanel.add(new JLabel("press the \"Merge\" "
				+ "button. Click on or type the name of the file"));
		instructionsPanel.add(new JLabel("that you"
				+ " want to save your merged spreadsheet info to."));
		instructionsPanel.add(new JLabel(" ")); //Whitespace

		initMoreSpreadsheetsButton();
		instructionsPanel.add(moreSpreadsheetsButton);
		initMergeButton();
		instructionsPanel.add(mergeButton);
	}

	//Note: we pass in output file name because we have no reference
	//to it in fileNamePtrs and only have a reference to it in the merge
	//button's mouse listener.
	protected ArrayList<String> getFileNames(String outputFileName) {
		if (outputFileName == null) throw new 
		NullPointerException("Output file name cannot be null!");

		ArrayList<String> fileNames = new ArrayList<String>();

		for (JLabel fileName : fileNamePtrs) {
			if (fileName.getText().compareTo(
					DEFAULT_FILENAME) != 0) {
				fileNames.add(fileName.getText());
			}
		}

		fileNames.add(outputFileName);

		return fileNames;
	}

	protected boolean verifySufficientNumberOfFiles() {
		int numberOfRealFileNames = 0;
		ArrayList<String> filesFoundSoFar =
		new ArrayList<String>(fileNamePtrs.size());

		for (JLabel fileName : fileNamePtrs) {
			if (fileName.getText().compareTo(DEFAULT_FILENAME) != 0
					&& !filesFoundSoFar.contains(fileName.getText())) {
				filesFoundSoFar.add(fileName.getText());

				if (++numberOfRealFileNames >= 1) {
					return true;
				}
			}
		}

		return false;
	}

	protected void initMergeButton() {
		mergeButton = new JButton("Merge");
		mergeButton.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				if (!verifySufficientNumberOfFiles()) {
					JOptionPane.showMessageDialog(null,
							"Please select at least one .xls file\n(each of which may have multiple sheets)\nto merge.");
					return;
				}

				String outputFileName = null;

				if (JFileChooser.APPROVE_OPTION == 
						jfc.showSaveDialog(null)) {

					if (jfc.getSelectedFile().exists()) {
						int overwrite = 
								JOptionPane.showConfirmDialog(null, 
										"File " +
												jfc.getSelectedFile().getName() + 
												" already exists.\nSelecting this file will"
												+ " replace its contents\nwith the results "
												+ "of the merge.\nAre you sure you want to "
												+ "proceed?", "Are you sure?",
												JOptionPane.YES_NO_OPTION);

						if (overwrite != JOptionPane.OK_OPTION) {
							outputFileName = null;
						}
						else {
							outputFileName = 
									jfc.getSelectedFile().getAbsolutePath();
						}
					}
					else {
						outputFileName = 
								jfc.getSelectedFile().getAbsolutePath();
					}
				}

				if (outputFileName != null) {
					try {
						if (new Combine().joinSpreadsheets(getFileNames(outputFileName))) {
							JOptionPane.showMessageDialog(null,
									"Merging succeeded. Output file"
											+ " is located at " + outputFileName);
						}
						else {
							
							JOptionPane.showMessageDialog(null,
									"Merging failed. Please make sure all spreadsheets\nthat you want to merge are closed. Then, please try\nagain or visit the MWM wiki for help\n");
						}
					}
					catch (DiscrepancyException e) {
						JOptionPane.showMessageDialog(null,
						"Merging failed.\n" + e.getMessage(),
						"Discrepancy detected.",
						JOptionPane.ERROR_MESSAGE);
					}
					catch (RuntimeException e) {
						JOptionPane.showMessageDialog(null,
								"Merging failed. All spreadsheets must have at\nleast one"
										+ " column of unique values (such as an ID\nnumber or username).\nPlease "
										+ "visit the MWM wiki if you\nrequire further"
										+ " assistance.");
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Merging "
								+ "failed for unknown reasons. Please"
								+ "\nvisit the MWM wiki for "
								+ "assistance.");
					}
				}
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});
	}

	protected void addAnotherFileSlot(String s) {
		final JLabel temp2 = new JLabel(s);
		temp2.setAlignmentX(LEFT_ALIGNMENT);
		temp2.setAlignmentY(TOP_ALIGNMENT);

		JButton temp = new JButton("Browse");
		temp.setAlignmentX(RIGHT_ALIGNMENT);
		temp.setAlignmentY(TOP_ALIGNMENT);
		temp2.setSize(temp.getSize());

		temp.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				//Not sure that jxl.jar can process .xlsx files.
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Microsoft Excel 97/2000/XP/2003 (.xls)", "xls");
				jfc.setFileFilter(filter);

				if (JFileChooser.APPROVE_OPTION ==
						jfc.showOpenDialog(null)) {
					temp2.setText(jfc.getSelectedFile().getAbsolutePath());
				}
				else {
					temp2.setText(DEFAULT_FILENAME);
				}
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});

		fileNamePtrs.add(temp2);
		fileNamesPanel.add(temp2);
		fileNamesPanel.add(new JLabel(" "));
		browseButtonsPanel.add(temp);
		//browseButtonsPanel.add(new JLabel(" "));
		validate();
	}

	protected void initMoreSpreadsheetsButton() {
		moreSpreadsheetsButton = new JButton("More Files");
		moreSpreadsheetsButton.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				addAnotherFileSlot();
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});
	}

	protected void createAndDisplayGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiPanel = new JPanel();
		guiPanel.setLayout(new GridLayout(2, 1));

		topPanelPtr = new JPanel();
		topPanelPtr.setLayout(new BoxLayout(topPanelPtr,
				BoxLayout.Y_AXIS));
		topPanelPtr.setBackground(Color.CYAN);
		initInstructionsPanel();

		bottomPanelPtr = new JPanel(
				new GridLayout(1, 2));

		fileNamesPanel = new JPanel();
		fileNamesPanel.setLayout(new BoxLayout(fileNamesPanel,
				BoxLayout.Y_AXIS));
		//fileNamesPanel.setBackground(Color.WHITE);

		browseButtonsPanel = new JPanel();
		browseButtonsPanel.setLayout(new BoxLayout(
				browseButtonsPanel, BoxLayout.Y_AXIS));
		//browseButtonsPanel.setBackground(Color.GREEN);

		bottomPanelPtr.add(fileNamesPanel);
		bottomPanelPtr.add(browseButtonsPanel);

		addAnotherFileSlot();
		addAnotherFileSlot();
		addAnotherFileSlot();
		guiPanel.add(instructionsPanel);
		guiPanel.add(bottomPanelPtr);
		getContentPane().add(new JScrollPane(guiPanel));
		pack();
		instructionsPanel.setMaximumSize(new Dimension(
				Integer.MAX_VALUE, instructionsPanel.getWidth()));

		//Allow user to use this software only if they agree to the terms.
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,
				FileChooserGUI.LEGALESE, "Important Legal Info",
				JOptionPane.OK_CANCEL_OPTION)) {
			
			setVisible(true);
		}
		else {
			dispose();
		}
	}

	public static void main(String args[]) {
		new FileChooserGUI().createAndDisplayGUI();
	}
}
