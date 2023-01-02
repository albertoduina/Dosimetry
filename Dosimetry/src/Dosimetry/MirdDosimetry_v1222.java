package Dosimetry;

import java.io.File;

//import ReadCdStudies.CD_dirInfo;
import ij.IJ;
import ij.plugin.PlugIn;

/**
 * @version v3
 * @author Date 05 dec 2022
 */
public class MirdDosimetry_v1222 implements PlugIn {

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

		// =====================================================
		// 24h
		// =====================================================
		// per ora metto le variabili come string, poi le tipizzo

		String[] vetDurata = { "#038#", "#048#", "#058#" };

		for (int i1 = 0; i1 < 3; i1++) {

			int durataAcquisizione = Utility.parseInt(Utility.readFromLog(pathPermanente, vetDurata[i1], "="));

			int deltaT = 0;
			String vol = null;
			String fatCal = null;
			String attivita = null;
			String[][] tabellina = null;

		}
	}
	// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// =================================================================================
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// =================================================================================
	// =================================================================================

}