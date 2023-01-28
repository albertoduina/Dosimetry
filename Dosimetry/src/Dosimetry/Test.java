package Dosimetry;

import ij.IJ;
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


	public static void main(String[] args) {
		

	}

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		
	}

}
