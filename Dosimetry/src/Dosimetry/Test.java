package Dosimetry;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.WaitForUserDialog;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.DICOM;
import ij.plugin.PlugIn;

public class Test implements PlugIn {

	public void run(String arg) {

		int aa = WindowManager.getWindowCount();
		int bb = WindowManager.getImageCount();
//		Utility.debugDeiPoveri("windowCount aa= " + aa + " imageCount bb= " + bb);
	}

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

}
