package Dosimetry;

import ij.IJ;
import ij.WindowManager;
import ij.plugin.PlugIn;

/**
 * Test
 * 
 * @author Alberto
 *
 */
public class Titles implements PlugIn {

	public void run(String arg) {

		IJ.log("eseguo titles");
		String[] lista = WindowManager.getNonImageTitles();
		for (String aux4 : lista)
			IJ.log(aux4);
	}
}
