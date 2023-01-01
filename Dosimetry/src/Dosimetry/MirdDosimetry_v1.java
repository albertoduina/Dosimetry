package Dosimetry;

import java.io.File;

//import ReadCdStudies.CD_dirInfo;
import ij.IJ;
import ij.plugin.PlugIn;

/**
 * @version v3
 * @author Date 05 dec 2022
 */
public class MirdDosimetry_v1 implements PlugIn {

	// =================================================================================
	// =================================================================================
	// VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV

	public void run(String arg) {

		String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		String pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
		String pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";
		boolean compresso = true;
		String[] tutteStringhe1 = Utility.readLog(pathPermanente, compresso);
		String[] tutteStringhe2 = Utility.readLog(pathVolatile, compresso);
		IJ.log("Stampo le stringhe trovate in Permanente");
		for (String aux : tutteStringhe1) {
			IJ.log(aux);
		}
		IJ.log("Stampo le stringhe trovate in Volatile");
		for (String aux : tutteStringhe2) {
			IJ.log(aux);
		}
		Utility.nonImageToFront("Log");
	}

	// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// =================================================================================
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// =================================================================================
	// =================================================================================

}