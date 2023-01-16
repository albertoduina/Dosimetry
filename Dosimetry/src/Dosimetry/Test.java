package Dosimetry;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.DICOM;
import ij.plugin.PlugIn;

/**
 * Test
 * 
 * @author Alberto
 *
 */
public class Test implements PlugIn {

//	public static void main(String[] args) {
//		SwingUtilities.invokeLater(() -> {
//			GridPage mainPanel = new GridPage();
//
//			IJ.log("eseguo grid");
//			JFrame frame = new JFrame("GUI");
//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			frame.add(mainPanel);
//			frame.pack();
//			frame.setLocationRelativeTo(null);
//			frame.setVisible(true);
//		});
//	}

	public boolean isDicomImage(String fileName1) {
		boolean fail = false;
		int type = (new Opener()).getFileType(fileName1);
		if (type != Opener.DICOM) {
			IJ.log("il file " + fileName1 + " risulta indigesto ad ImageJ ");
			fail = true;
		}
		String info = new DICOM().getInfo(fileName1);
		if (info == null || info.length() == 0) {
			IJ.log("il file " + fileName1 + " risulta indigesto ad ImageJ NOT DICOM INFO");
			fail = true;
		}
		if (!info.contains("7FE0,0010")) {
			IJ.log("il file " + fileName1 + " risulta indigesto ad ImageJ NOT IMAGE / TEXT REPORT");
			fail = true;
		}
		return !fail;
	}

	private void debugDeiPoveri(String text) {
		WaitForUserDialog wait = new WaitForUserDialog("Debug", text);
		wait.show();
	}

	public static void main(String[] args) {
		
		IJ.log("Eseguo labeldemo1");

		LabelDemo label = new LabelDemo();
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);

				label.createAndShowGUI();
			}
		});
	}

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		IJ.log("Eseguo labeldemo2");
		LabelDemo label = new LabelDemo();
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);

				label.createAndShowGUI();
			}
		});
	
	}

}
