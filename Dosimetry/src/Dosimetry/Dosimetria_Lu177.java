package Dosimetry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import flanagan.analysis.Regression;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NonBlockingGenericDialog;
import ij.measure.CurveFitter;
import ij.plugin.PlugIn;
import ij.util.DicomTools;

//
//DATI SOMMINISTRAZIONE 			#001#-#009# 
//IMAGE INFO 24h 					#010#-#029#
//IMAGE INFO 48 h					#030#-#049#
//IMAGE INFO 120 h					#050#-#069#
//PATIENT-DOSIMETRY INFO 24 h		#100#-#129#
//PATIENT-DOSIMETRY INFO 48 h		#130#-#159#
//PATIENT-DOSIMETRY INFO 24 h		#160#-#199#
//

/**
 * Programma main di dosimetria per Lu177
 * 
 * @version v3 ccc
 * @author ----
 * @since 05 dec 2022
 */
public class Dosimetria_Lu177 implements PlugIn {

	public void run(String paramString) {

		Locale.setDefault(Locale.US);
		MyGlobals.coordinates();

		MyGlobals.desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		MyGlobals.pathPermanente = MyGlobals.desktopPath + File.separator + "DosimetryFolder" + File.separator
				+ "permanente.txt";
		MyGlobals.pathVolatile = MyGlobals.desktopPath + File.separator + "DosimetryFolder" + File.separator
				+ "volatile.txt";
		MyGlobals.desktopDosimetryFolderPath = MyGlobals.desktopPath + File.separator + "DosimetryFolder";
		MyGlobals.desktopImagesSubfolderPath = MyGlobals.desktopDosimetryFolderPath + File.separator + "ImagesFolder";

		String petctviewerTitle = "";
		Double activitySomministrazione = null;
		String dataSomministrazione = null;
		String oraSomministrazione = null;
		Date dataOraSomministrazione = null;
		boolean nuovoPaziente = false;
		boolean nuovoDistretto = false;
		boolean nuoveImmagini = false;
		boolean datiSomministrazioneValidi = false;
		File[] arrayOfFile2 = null;
		String aux5 = "";
		int out1 = 0;
		String IJversion = IJ.getVersion();
		String JavaVersion = System.getProperty("java.version");
		String jarName = Utility.getJarTitle();

		// ===========================================================
		// LEGGO CARTELLA DOSIMETRY FOLDER (E SOTTOCARTELLA IMAGES FOLDER)
		// ===========================================================
		String[] insOut = inspector(MyGlobals.desktopDosimetryFolderPath);
		datiSomministrazioneValidi = Utility.datiSomministrazionePresenti(MyGlobals.pathPermanente);
		if (insOut == null) {
			MyLog.log("NON ESISTE DOSIMETRY_FOLDER SUL DESKTOP");
			nuovoPaziente = true;
		} else if (insOut.length == 0) {
			MyLog.log("NON ESISTONO IMMAGINI PAZIENTE");
			nuovoPaziente = true;
		} else if (!datiSomministrazioneValidi) {
			MyLog.log("NON ESISTONO I DATI SOMMINISTAZIONE");
			nuovoPaziente = true;
		} else {
			// ===========================================================
			// DIALOGO CON DATI PAZIENTE PRECEDENTE - NUOVA LESIONE
			// ===========================================================
			MyLog.log("DIALOGO NUOVO PAZIENTE OPPURE NUOVA LESIONE");
			out1 = MyDialog.dialogImmaginiPazientePrecedente_LP21(insOut);
			MyLog.log("out1= " + out1);

			if (out1 == 2) {
				nuovoPaziente = true;
			} else if (out1 == 0) {
				return;
			}

			if (!nuovoPaziente) {
				// SUL PAZIENTE PRECEDENTE DOBBIAMO VEDERE SE E' UNA NUOVA LESIONE (ED ALLORA
				// ANALIZZEREMO LE MEDESIME IMMAGINI) OPPURE SE E'UN NUOVO DISTRETTO DI CUI
				// ANDARE A CARICARE LE IMMAGINI
				nuovoDistretto = MyDialog.dialogDistretto_LP07();
			}
		}
		if (!datiSomministrazioneValidi)
			nuovoPaziente = true;
		if (nuovoPaziente) {
			// ============================================
			// NUOVO PAZIENTE
			// ============================================
			nuoveImmagini = true;
			MyLog.log("NUOVO PAZIENTE, TRASFERIMENTO IMMAGINI");
			arrayOfFile2 = desktopImagesFolderFill();
			if (arrayOfFile2 == null)
				return;
			MyLog.log("NUOVO PAZIENTE, INIZIALIZZAZIONE LOG E RICHIESTA DATI SOMMINISTRAZIONE");
			MyLog.logDeleteAll(MyGlobals.desktopDosimetryFolderPath);
			MyLog.logInit(MyGlobals.pathPermanente);
			MyLog.logInit(MyGlobals.pathVolatile);

			boolean ok1 = false;

			do {
				dataSomministrazione = MyDialog.dialogInputDataSomministrazione_LP11();
				if (dataSomministrazione == null) // ritorna null se premuto cancel
					return;
				if (dataSomministrazione.isEmpty()) // ritorna vuoto se errata introduzione
					continue;
				oraSomministrazione = MyDialog.dialogInputOraSomministrazione_LP12();
				if (oraSomministrazione == null) // ritorna null se premuto cancel
					return;
				if (oraSomministrazione.isEmpty()) // ritorna vuoto se errata introduzione
					continue;
				activitySomministrazione = MyDialog.dialogInputActivitySomministrazione_LP13();

				if (dataSomministrazione != null && oraSomministrazione != null) {
					dataOraSomministrazione = getDateTime(dataToDicom(dataSomministrazione),
							oraToDicom(oraSomministrazione));
					String[] in1 = new String[3];
					in1[0] = dataSomministrazione;
					in1[1] = oraSomministrazione;
					in1[2] = "" + activitySomministrazione;
					ok1 = MyDialog.dialogConfermaDatiSomministrazione_LP10(in1);
				} else
					ok1 = false;
			} while (!ok1);

			MyLog.log("NUOVO PAZIENTE, SCRITTURA DATI SOMMINISTRAZIONE SU VOLATILE");

			SimpleDateFormat sdf = new SimpleDateFormat(MyGlobals.format1);
			String myDTT = sdf.format(dataOraSomministrazione);

			String aux1 = "";
			aux1 = "#000#\tImagejVersion= " + IJversion + " JavaVersion= " + JavaVersion + " JarName= " + jarName;
			MyLog.logAppend(MyGlobals.pathVolatile, aux1);
			aux1 = "#001#\t------------ SOMMINISTRAZIONE ------------";
			MyLog.logAppend(MyGlobals.pathVolatile, aux1);
			aux1 = "#002#\tDateTime administration= " + myDTT;
			MyLog.logAppend(MyGlobals.pathVolatile, aux1);
			aux1 = "#003#\tActivity= " + activitySomministrazione;
			MyLog.logAppend(MyGlobals.pathVolatile, aux1);
			// copia da volatile a permanente i dati di SOMMINISTRAZIONE
			MyLog.logCopyRange(MyGlobals.pathVolatile, MyGlobals.pathPermanente, 0, 3);
			raccoltaDati(arrayOfFile2, dataOraSomministrazione);
			// copia da volatile a permanente i dati di IMAGE INFO 24-48-120
			MyLog.logCopyRange(MyGlobals.pathVolatile, MyGlobals.pathPermanente, 10, 60);
		} else if (nuovoDistretto)

		{ // stesso paziente nuovo distretto nuova lesione
			// ============================================
			// STESSO PAZIENTE, NUOVO DISTRETTO, NUOVA LESIONE
			// ============================================
			nuoveImmagini = true;
			MyLog.log("NUOVO DISTRETTO, CARICAMENTO IMMAGINI E \nRECUPERO DATI SOMMINISTRAZIONE DA PERMANENTE");
			arrayOfFile2 = desktopImagesFolderFill();
			if (arrayOfFile2 == null)
				return;
			Utility.deleteAllFilesWithSpecificExtension(MyGlobals.desktopDosimetryFolderPath, "tif");

			MyLog.logInit(MyGlobals.pathVolatile);
			// copia da permanente a volatile i dati di SOMMINISTRAZIONE
			MyLog.logCopyRange(MyGlobals.pathPermanente, MyGlobals.pathVolatile, 0, 3);
			// copia da permanente a volatile i dati di IMAGE INFO 24-48-120
			MyLog.logCopyRange(MyGlobals.pathPermanente, MyGlobals.pathVolatile, 10, 60);
			dataOraSomministrazione = Utility.getDateTime(MyReader.readFromLog(MyGlobals.pathVolatile, "#002#", "="),
					MyGlobals.format1);
			activitySomministrazione = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#003#", "="));
			MyLog.log("dataOraSomministrazione= " + dataOraSomministrazione);
			IJ.log("002");

			raccoltaDati(arrayOfFile2, dataOraSomministrazione);
			// copia da volatile a permanente i dati di IMAGE INFO 24-48-120
			MyLog.logCopyRange(MyGlobals.pathVolatile, MyGlobals.pathPermanente, 10, 60);
			azzeraFlags(MyGlobals.pathPermanente);

		} else {
			// ============================================
			// STESSO PAZIENTE, STESSO DISTRETTO, NUOVA LESIONE
			// ============================================
			MyLog.log("NUOVA LESIONE, RECUPERO DATI SOMMINISTRAZIONE DA PERMANENTE");
			Utility.deleteAllFilesWithSpecificExtension(MyGlobals.desktopDosimetryFolderPath, "tif");
			MyLog.logInit(MyGlobals.pathVolatile);
			// copia da permanente a volatile i dati di IMAGE INFO 24-48-120
			MyLog.logCopyRange(MyGlobals.pathPermanente, MyGlobals.pathVolatile, 0, 3);
			// copia da permanente a volatile i dati di IMAGE INFO 24-48-120
			MyLog.logCopyRange(MyGlobals.pathPermanente, MyGlobals.pathVolatile, 10, 60);
			dataOraSomministrazione = Utility.getDateTime(MyReader.readFromLog(MyGlobals.pathVolatile, "#002#", "="),
					MyGlobals.format1);
			activitySomministrazione = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#003#", "="));
			MyLog.log("dataOraSomministrazione= " + dataOraSomministrazione);
			azzeraFlags(MyGlobals.pathPermanente);
		}

		// raccoltaDatiNuova(dataOraSomministrazione);

		// ===========================================================================
		// per evitare di utilizzare il menu di scelta, dobbiamo avviare pet_ct_viewer
		// passandogli nell'argomento gli UID di due immagini stack aperte. Tali UID
		// sono il TAG Dicom "0020,000E". NOTARE che occorre fare un trim delle
		// stringhe, se si vuole che Pet_Ct_Viever le accetti senza fare storie
		// ===========================================================================

		String str1 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator + "ImagesFolder" + File.separator;

		String startingDir1 = str1 + "24h" + File.separator + "SPECT";
		String selection = "";
		double[] out24 = null;
		double[] out48 = null;
		double[] out120 = null;
		int slice1 = 1;
		ImagePlus imp1 = null;
		ImagePlus imp2 = null;
		ImagePlus imp3 = null;
		ImagePlus imp4 = null;
		ImagePlus imp5 = null;
		ImagePlus imp6 = null;
		boolean flanagan = false;

		double AA = Double.NaN;
		double aa = Double.NaN;
		double SA = Double.NaN;
		double Sa = Double.NaN;
		double mAtilde = Double.NaN;
		double disintegrazioni = Double.NaN;
		double uptake = Double.NaN;
		double massa = Double.NaN;
		double tmezzo = Double.NaN;
		double tau = Double.NaN;
		double SmAtilde = Double.NaN;
		double Sdisintegrazioni = Double.NaN;
		double Suptake = Double.NaN;
		double Smassa = Double.NaN;
		double Stmezzo = Double.NaN;
		double Stau = Double.NaN;
		double dose = Double.NaN;
		double Sdose = Double.NaN;
		double Rsquared = Double.NaN;
		double s1 = Double.NaN;
		double s2 = Double.NaN;
		double m1 = Double.NaN;
		double m2 = Double.NaN;

		// ===========================================================================
		// ELABORAZIONE 24h ed apertura PetCtViewer
		// ===========================================================================

		List<File> result1 = Utility.getFileListing(new File(startingDir1));
		if (result1 == null) {
			IJ.error("getFileListing.result1==null");
		}
		int len2 = result1.size();
		if (len2 != 1)
			return;
		File fil1 = result1.get(0);
		String path1 = fil1.getAbsolutePath();
		imp1 = Utility.openImage(path1);
		imp1.show();
		String meta1 = Utility.getMeta(slice1, imp1);

		if (nuoveImmagini) {
			petctviewerTitle = stringaLaboriosa(meta1);
			MyLog.logAppend(MyGlobals.pathPermanente, "24h=" + petctviewerTitle);
		}
		// 0020,000E Series Instance UID:
		// 1.2.840.113619.2.184.31108.1067210107.1661517437.7028981
		String petUID1 = DicomTools.getTag(imp1, "0020,000E");
		petUID1 = petUID1.trim();

		String startingDir2 = str1 + "24h" + File.separator + "CT";

		List<File> result2 = Utility.getFileListing(new File(startingDir2));
		if (result2 == null) {
			IJ.error("getFileListing.result2==null");
		}
		String[] list2 = new String[result2.size()];
		int j2 = 0;
		for (File file2a : result2) {
			list2[j2++] = file2a.getPath();
		}
		imp2 = MyStack.readStackFiles2(startingDir2);
		imp2.show();

		// 0020,000E Series Instance UID:
		// 1.2.840.113619.2.184.31108.1067210107.1661517437.7028981
		String ctUID2 = DicomTools.getTag(imp2, "0020,000E");
		ctUID2 = ctUID2.trim();

		// possiamo passare a petCtViewer una stringa con gli UID delle immagini PET e
		// CT da aprire
		String seriesUID1 = petUID1 + ", " + ctUID2;

		IJ.runPlugIn("Pet_Ct_Viewer", seriesUID1);
		IJ.wait(500);

		MyLog.log("SONO RITORNATO IN LU777");
		// ===========================================================================
		// ELABORAZIONE 48h ed apertura PetCtViewer
		// ===========================================================================

		String startingDir3 = str1 + "48h" + File.separator + "SPECT";
		List<File> result3 = Utility.getFileListing(new File(startingDir3));
		if (result3 == null) {
			IJ.error("getFileListing.result3==null");
		}
		int len3 = result3.size();
		if (len3 != 1)
			return;

		File fil3 = result3.get(0);
		String path3 = fil3.getAbsolutePath();
		imp3 = Utility.openImage(path3);
		imp3.show();
		String meta3 = Utility.getMeta(slice1, imp3);
		if (nuoveImmagini) {
			petctviewerTitle = stringaLaboriosa(meta3);
			MyLog.logAppend(MyGlobals.pathPermanente, "48h=" + petctviewerTitle);
		}

		String petUID3 = DicomTools.getTag(imp3, "0020,000E");
		petUID3 = petUID3.trim();

		String startingDir4 = str1 + "48h" + File.separator + "CT";
		List<File> result4 = Utility.getFileListing(new File(startingDir4));
		if (result4 == null) {
			IJ.error("getFileListing.result4==null");
		}
		String[] list4 = new String[result4.size()];
		int j4 = 0;
		for (File file4a : result4) {
			list4[j4++] = file4a.getPath();
		}

		imp4 = MyStack.readStackFiles2(startingDir4);
		imp4.show();

		String ctUID4 = DicomTools.getTag(imp4, "0020,000E");
		ctUID4 = ctUID4.trim();

		String seriesUID3 = petUID3 + ", " + ctUID4;

		IJ.runPlugIn("Pet_Ct_Viewer", seriesUID3);
		IJ.wait(500);

		// ===========================================================================
		// ELABORAZIONE 120h ed apertura PetCtViewer
		// ===========================================================================

		String startingDir5 = str1 + "120h" + File.separator + "SPECT";
		List<File> result5 = Utility.getFileListing(new File(startingDir5));
		if (result5 == null) {
			IJ.error("getFileListing.result5==null");
		}
		int len5 = result5.size();
		if (len5 != 1)
			return;

		File fil5 = result5.get(0);
		String path5 = fil5.getAbsolutePath();
		imp5 = Utility.openImage(path5);
		imp5.show();
		String meta5 = Utility.getMeta(slice1, imp5);
		if (nuoveImmagini) {
			petctviewerTitle = stringaLaboriosa(meta5);
			MyLog.logAppend(MyGlobals.pathPermanente, "120h=" + petctviewerTitle);
		}

		String petUID5 = DicomTools.getTag(imp5, "0020,000E");
		petUID5 = petUID5.trim();

		String startingDir6 = str1 + "120h" + File.separator + "CT";
		List<File> result6 = Utility.getFileListing(new File(startingDir6));
		if (result6 == null) {
			IJ.error("getFileListing.result6==null");
		}
		String[] list6 = new String[result6.size()];
		int j6 = 0;
		for (File file6 : result6) {
			list6[j6++] = file6.getPath();
		}

		imp6 = MyStack.readStackFiles2(startingDir6);
		if (imp6 == null)
			return;
		imp6.show();

		// ==================================================

		String ctUID6 = DicomTools.getTag(imp6, "0020,000E");
		ctUID6 = ctUID6.trim();

		String seriesUID5 = petUID5 + ", " + ctUID6;

		IJ.runPlugIn("Pet_Ct_Viewer", seriesUID5);
		IJ.wait(500);

		MyLog.log("SONO DEFINITIVAMENTE IN LU177");

		// ===========================================================================
		imp2.close();
		imp4.close();
		imp6.close();
		double MIRD_vol24 = Double.NaN;
		double MIRD_vol48 = Double.NaN;
		double MIRD_vol120 = Double.NaN;
		int decis1 = -1;

		double[] vetMin = null;
		double[] vetMax = null;
		double[] vetMed = null;
		double[] vetY = null;

		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		// PUNTO DI INIZIO DEI LOOP DI ELABORAZIONE, FINISCONO SOLO SE E QUANDO I
		// RISULTATI VENGONO DEFINITIVAMENTE ACCETTATI/APPROVATI O PER ESAURIMENTO DELLA
		// PAZIENZA
		// DELL'OPERATORE
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

		do {

			// ===========================================================================================
			// Azzeramento dei valori ottenuti in precedenza SERVE VERAMENTE
			// ===========================================================================================

			MIRD_vol24 = Double.NaN;
			MIRD_vol48 = Double.NaN;
			MIRD_vol120 = Double.NaN;
			decis1 = -1;
			out24 = null;
			out48 = null;
			out120 = null;
			slice1 = 1;
			imp1 = null;
			imp2 = null;
			imp3 = null;
			imp4 = null;
			imp5 = null;
			imp6 = null;
			flanagan = false;
			AA = Double.NaN;
			aa = Double.NaN;
			SA = Double.NaN;
			Sa = Double.NaN;
			mAtilde = Double.NaN;
			disintegrazioni = Double.NaN;
			uptake = Double.NaN;
			massa = Double.NaN;
			tmezzo = Double.NaN;
			tau = Double.NaN;
			SmAtilde = Double.NaN;
			Sdisintegrazioni = Double.NaN;
			Suptake = Double.NaN;
			Smassa = Double.NaN;
			Stmezzo = Double.NaN;
			Stau = Double.NaN;
			dose = Double.NaN;
			Sdose = Double.NaN;
			Rsquared = Double.NaN;
			s1 = Double.NaN;
			s2 = Double.NaN;
			m1 = Double.NaN;
			m2 = Double.NaN;

			int rip = -1;

			IJ.runPlugIn("Dosimetry.Dosimetry_v2", "");

			if (nuoveImmagini)
				MyLog.logEnd(MyGlobals.pathPermanente);

			// ==========================================================================================
			// Elaborazione 24/48/120h
			// ==========================================================================================

			// 24h
			// se non mi ha scritto il tag #121# di volatile vuol dire che Dosimetry_v2 non
			// ha analizzato la immagine 24h (probabile cancel dato al menu)

			if (MyReader.readFromLog(MyGlobals.pathVolatile, "#121#", "=") == null)
				return;

			double[] in1 = new double[5];
			in1[0] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#018#", "=")); // acquisition
																										// duration
			in1[1] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#121#", "=")); // pixel number
																										// over
			// threshold
			in1[2] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#003#", "=")); // activity
			in1[3] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#115#", "=")); // contouring
																										// threshold
			// level
			in1[4] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#122#", "=")); // over threshold
																										// count
			// integral
			out24 = Utility.MIRD_point(in1);
			MIRD_vol24 = out24[0];

			// 48h
			// se non mi ha scritto il tag #151# di volatile vuol dire che Dosimetry_v2 non
			// ha analizzato la immagine 24h (probabile cancel dato al menu)
			if (MyReader.readFromLog(MyGlobals.pathVolatile, "#151#", "=") == null)
				return;
			in1[0] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#038#", "=")); // acquisition
																										// duration
			in1[1] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#151#", "=")); // pixel number
																										// over
			// threshold
			in1[2] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#003#", "=")); // activity
			in1[3] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#145#", "=")); // contouring
																										// threshold
			// level
			in1[4] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#152#", "=")); // over threshold
																										// count
			// integral
			out48 = Utility.MIRD_point(in1);
			MIRD_vol48 = out48[0];

			// 120h
			// se non mi ha scritto il tag #181# di volatile vuol dire che Dosimetry_v2 non
			// ha analizzato la immagine 24h (probabile cancel dato al menu)
			if (MyReader.readFromLog(MyGlobals.pathVolatile, "#181#", "=") == null)
				return;
			in1[0] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#058#", "=")); // acquisition
																										// duration
			in1[1] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#181#", "=")); // pixel number
																										// over
			// threshold
			in1[2] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#003#", "=")); // activity
			in1[3] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#175#", "=")); // contouring
																										// threshold
			// level
			in1[4] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#182#", "=")); // over threshold
																										// count
			// integral
			out120 = Utility.MIRD_point(in1);

			MIRD_vol120 = out120[0];

			// scrivo TEMPORANEAMENTE dati in volatile.txt, in serguito verranno riscritti
			// dopo approvazione "ministeriale" definitiva

			int count5 = 200;
			aux5 = "#" + String.format("%03d", count5++) + "#\t-------- MIRD PRE-CALCULATION 24h --------";
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol24= " + out24[0];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal24= " + out24[1];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv24= " + out24[2];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			count5 = 220;
			aux5 = "#" + String.format("%03d", count5++) + "#\t-------- MIRD PRE-CALCULATION 48h --------";
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol48= " + out48[0];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal48= " + out48[1];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv48= " + out48[2];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			count5 = 240;
			aux5 = "#" + String.format("%03d", count5++) + "#\t------- MIRD PRE-CALCULATION 120h -------";
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol120= " + out120[0];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal120= " + out120[1];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv120= " + out120[2];
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);

			double[] xp1 = new double[3];
			double[] yp1 = new double[3];
			xp1[0] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#019#", "=")); // deltaT
			yp1[0] = out24[2];
			xp1[1] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#039#", "=")); // deltaT
			yp1[1] = out48[2];
			xp1[2] = Double.parseDouble(MyReader.readFromLog(MyGlobals.pathVolatile, "#059#", "=")); // deltaT
			yp1[2] = out120[2];
			for (double aux : xp1) {
				MyLog.log("xp1= " + aux);
			}
			for (double aux : yp1) {
				MyLog.log("yp1= " + aux);
			}

			// ========================================================================
			// FIT E PLOT DECISIONALI
			// ========================================================================

			double[] vetInput = null;

			MyDialog.MIRD_display_LP66(MIRD_vol24, MIRD_vol48, MIRD_vol120);

			// #########################################################################
			// QUI MOSTRERO' IL DVH
			// #########################################################################

			String pathImage = System.getProperty("user.home") + File.separator + "Desktop" + File.separator
					+ "DosimetryFolder" + File.separator;

			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// QUI TORNO PER PROBLEMI DI FIT DA LP08
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			do {

				////////////////////////////////////////////////////////////////////////
				// MyLog.waitHere("PUNTO PL01 - LP33");
				////////////////////////////////////////////////////////////////////////

				String titolo1 = "Punti";
				MyGlobals.titPL01 = MyPlot.PL01_MIRD_pointsPlotter(xp1, yp1, null, titolo1,
						"24h=red 48h=green 120h=blue");

				boolean[] puntiSelezionati = MyDialog.pointsSelection_LP33(); /// selezione dei 2 o 3 punti su cui in
																				/// seguito
				/// fare il fit
				Utility.closePlot(MyGlobals.titPL01);

				int count = 0;
				boolean threepoints = false;
				for (int i1 = 0; i1 < puntiSelezionati.length; i1++) {
					if (puntiSelezionati[i1] == true)
						count++;
				}
				if (count == 3)
					threepoints = true;

				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

				double[] vetOut4 = Utility.processaCalcolaFit2or3(xp1, yp1, MIRD_vol24, MIRD_vol48, MIRD_vol120,
						pathImage, puntiSelezionati);

				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

				AA = vetOut4[0];
				aa = vetOut4[1];
				SA = vetOut4[2];
				Sa = vetOut4[3];
				mAtilde = vetOut4[4];
				disintegrazioni = vetOut4[5];
				uptake = vetOut4[6];
				massa = vetOut4[7];
				tmezzo = vetOut4[8];
				tau = vetOut4[9];
				SmAtilde = vetOut4[10];
				Sdisintegrazioni = vetOut4[11];
				Suptake = vetOut4[12];
				Smassa = vetOut4[13];
				Stmezzo = vetOut4[14];
				Stau = vetOut4[15];
				dose = vetOut4[16];
				Sdose = vetOut4[17];
				Rsquared = vetOut4[18];
				s1 = vetOut4[19];
				s2 = vetOut4[20];
				m1 = vetOut4[21];
				m2 = vetOut4[22];
				aux5 = "#302#\tMIRD FLANAGAN FIT param 1= " + aa;
				MyLog.logAppend(MyGlobals.pathVolatile, aux5);

				ArrayList<ArrayList<Double>> xList = VoxelDosimetry.start_DVH1(pathImage, puntiSelezionati, aa);

				vetInput = new double[14];

				vetInput[0] = MIRD_vol24;
				vetInput[1] = MIRD_vol48;
				vetInput[2] = MIRD_vol120;
				vetInput[3] = uptake;
				vetInput[4] = massa;
				vetInput[5] = tmezzo;
				vetInput[6] = dose;
				vetInput[7] = 0;
				vetInput[8] = 0;
				vetInput[9] = 0;
				vetInput[10] = Suptake;
				vetInput[11] = Smassa;
				vetInput[12] = Stmezzo;
				vetInput[13] = Sdose;

				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

				double[] vetx24 = new double[0];
				double[] vety24 = new double[0];
				double[] vetx48 = new double[0];
				double[] vety48 = new double[0];
				double[] vetx120 = new double[0];
				double[] vety120 = new double[0];

				ArrayList<Double> arrList1 = null;

				arrList1 = xList.get(0);
				if (arrList1 != null) {
					vetx24 = Utility.arrayListToArrayDouble(arrList1);
					MyLog.logVector(vetx24, "vetx24");
				} else
					vetx24 = null;
				arrList1 = xList.get(2);
				if (arrList1 != null) {
					vety24 = Utility.arrayListToArrayDouble(arrList1);
					MyLog.logVector(vety24, "vety24");
				} else
					vety24 = null;
				arrList1 = xList.get(3);
				if (arrList1 != null) {
					vetx48 = Utility.arrayListToArrayDouble(arrList1);
					MyLog.logVector(vetx48, "vetx48");
				} else
					vetx48 = null;
				arrList1 = xList.get(5);
				if (arrList1 != null) {
					vety48 = Utility.arrayListToArrayDouble(arrList1);
					MyLog.logVector(vety48, "vety48");
				} else
					vety48 = null;
				arrList1 = xList.get(6);
				if (arrList1 != null) {
					vetx120 = Utility.arrayListToArrayDouble(arrList1);
					MyLog.logVector(vetx120, "vetx120");
				} else
					vetx120 = null;
				arrList1 = xList.get(8);
				if (arrList1 != null) {
					vety120 = Utility.arrayListToArrayDouble(arrList1);
					MyLog.logVector(vety120, "vety120");
				} else
					vety120 = null;

				MyGlobals.titPL11 = MyPlot.PL11_myPlotMultiple2(vetx24, vety24, vetx48, vety48, vetx120, vety120,
						"INPUT 24red,48green,120blue", "DOSE [Gy]", "VOL%");

				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§ QUI RIFACCIAMO IL FIT UNICAMENTE PER STAMPARE IL GRAFICO §§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

				if (threepoints) {
					MyLog.log("TRE PUNTI CON FLANAGAN");
					MyLog.logVector(xp1, "xp1");
					MyLog.logVector(yp1, "yp1");
					Regression rf = Utility.MIRD_curveFitterSpecialFlanagan(xp1, yp1);
					MyGlobals.titPL0405 = MyPlot.PL05_MIRD_curvePlotterSpecialFlanagan(rf, xp1, yp1, null);
				} else {
					MyLog.log("DUE PUNTI CON IMAGEJ");

					int count2 = 0;
					double[] xp2 = new double[2];
					double[] yp2 = new double[2];
					for (int i1 = 0; i1 < xp1.length; i1++) {
						if (puntiSelezionati[i1]) {
							xp2[count2] = xp1[i1];
							yp2[count2] = yp1[i1];
							count2++;
						}
					}
					MyLog.logVector(xp2, "xp2");
					MyLog.logVector(yp2, "yp2");

					CurveFitter cf = Utility.MIRD_curveFitterSpecialImageJ(xp2, yp2); // qui ci vanno xp2 e yp2
					MyGlobals.titPL0405 = MyPlot.PL04_MIRD_curvePlotterSpecialImageJ(cf, xp1, yp1, puntiSelezionati,
							null); // qui invece ci vanno xp1 e yp1
				}

				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

				double[][] matDVH2 = VoxelDosimetry.subDVH2(vetx24, vety24, vetx48, vety48, vetx120, vety120);

				vetMin = new double[matDVH2.length];
				vetMax = new double[matDVH2.length];
				vetMed = new double[matDVH2.length];
				vetY = new double[matDVH2.length];
				for (int i1 = 0; i1 < matDVH2.length; i1++) {
					vetMin[i1] = matDVH2[i1][0];
					vetMax[i1] = matDVH2[i1][1];
					vetMed[i1] = matDVH2[i1][2];
					vetY[i1] = matDVH2[i1][3];
				}

				///////////////////////////////////////////////////////////////////////////
				// MyLog.waitHere("PUNTO LP68");
				///////////////////////////////////////////////////////////////////////////
				// ==========================================================================
				// PARTE REVIEW CHE DEVE RITORNARE INDIETRO PER RIFARE UNO O PIU'DEI CALCOLI
				// FINALMENTE SAREMO FELICI E GORGOGLIONI DELLE NOSTRE ELABORAZIONI
				// ==========================================================================
//				decis1 = MIRD_display_LP67(MIRD_vol24, MIRD_vol48, MIRD_vol120, uptake, massa, tmezzo, dose);
				decis1 = MyDialog.MIRD_display_LP68(vetInput); // accetta risultati o ripeti analisi
				if (decis1 == 0)
					return;
				// boolean fit = false;
				if (decis1 == 1) {
					rip = MyDialog.dialogRipetizione_LP08(); // SCELTA PROBLEMA: CONTORNAMENTO O FIT
					if (rip == 0)
						return;
				}
				Utility.closePlot(MyGlobals.titPL0405);
				Utility.closePlot(MyGlobals.titPL11);

			} while (rip < 2 && decis1 < 2);

			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// QUI ENTRO PER PROBLEMI DI CONTORNAMENTO DA LP08
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			if (rip == 2) {
				selection = MyDialog.dialogReview_LP32();
				String aux1 = "";
				switch (selection) {
				case "24h":
//					scelta = 1;
					aux1 = "#901#\tok24= false";
					MyLog.logModify(MyGlobals.pathPermanente, "#901#", aux1);
					aux1 = "#904#\tokk= false";
					MyLog.logModify(MyGlobals.pathPermanente, "#904#", aux1);
					break;
				case "48h":
//					scelta = 2;
					aux1 = "#902#\tok48= false";
					MyLog.logModify(MyGlobals.pathPermanente, "#902#", aux1);
					aux1 = "#904#\tokk= false";
					MyLog.logModify(MyGlobals.pathPermanente, "#904#", aux1);
					break;
				case "120h":
//					scelta = 3;
					aux1 = "#903#\tok120= false";
					MyLog.logModify(MyGlobals.pathPermanente, "#903#", aux1);
					aux1 = "#904#\tokk= false";
					MyLog.logModify(MyGlobals.pathPermanente, "#904#", aux1);
					break;
				}
			}

		} while (decis1 != 2);

		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		MyGlobals.titPL08 = MyPlot.PL08_myPlotMultipleSpecial1(vetMin, vetY, vetMax, vetY, vetMed, vetY, "MEDIA",
				"DOSE [Gy]", "VOL%");

		double[] vetErrSup = VoxelDosimetry.calcoliDVHerrSup(vetMed, vetMax);
		double[] vetErrInf = VoxelDosimetry.calcoliDVHerrInf(vetMed, vetMin);

		double[] vetErrDose = VoxelDosimetry.calcoliDVHerrDose2(vetMin, vetMax);
		MyLog.logVector(vetErrDose, "vetErrDose");
		MyLog.logVector(vetErrSup, "vetErrSup");
		MyLog.logVector(vetErrInf, "vetErrInf");

		double errFin = VoxelDosimetry.calcoliDVHerrFinale(vetMed, vetErrDose);

		int percent = 98;
		double[] vetOut98 = VoxelDosimetry.calcoliDVH(vetErrDose, vetMed, vetY, percent);

		double valD98 = vetOut98[0];
		double errD98 = vetOut98[1];
		percent = 2;
		double[] vetOut2 = VoxelDosimetry.calcoliDVH(vetErrDose, vetMed, vetY, percent);

		double valD2 = vetOut2[0];
		double errD2 = vetOut2[1];

		double Dmedia = Utility.vetMeanSecond(vetMed);

		double errMedia = Utility.vetMeanSecond(vetErrDose);

		double[][] export1 = Utility.samplerDVH(vetMed, vetY);
		double[][] export2 = Utility.samplerDVH(vetErrSup, vetY);
		double[][] export3 = Utility.samplerDVH(vetErrInf, vetY);

		String str11 = "";
		String str12 = "";
		String str13 = "";
		String str14 = "";
		// esperimento esportazione
		for (int i1 = 0; i1 < export1.length; i1++) {
			str11 = str11 + export1[i1][0] + "; ";
			str12 = str12 + export2[i1][0] + "; ";
			str13 = str13 + export3[i1][0] + "; ";
			str14 = str14 + export3[i1][1] + "; ";
		}

		NonBlockingGenericDialog resultsDialog = new NonBlockingGenericDialog("SV07 - Results");
		resultsDialog.addMessage("Riassunto dati DVH ", MyGlobals.titleFont);
		resultsDialog.setFont(MyGlobals.defaultFont);

		resultsDialog.addMessage("=============");
		resultsDialog.addMessage(
				"D98= " + String.format("%.4f", valD98) + " \u00B1 " + String.format("%.4f", errD98) + " Gy");
		resultsDialog
				.addMessage("D2= " + String.format("%.4f", valD2) + " \u00B1 " + String.format("%.4f", errD2) + " Gy");
		resultsDialog.addMessage(
				"Dmedia= " + String.format("%.4f", Dmedia) + " \u00B1 " + String.format("%.4f", errMedia) + " Gy");
		resultsDialog.showDialog();

		// ================= POSTSCRITTURA ===========================================
		// UNA VOLTA CHE L'OPERATORE HA DETTO SI, SCRIVIAMO TUTTA LA MONNEZZA IN
		// VOLATILE, IN ATTESA DI BATTEZZARE LA LESIONE
		// ============================================================================

		if (Double.isNaN(SmAtilde))
			flanagan = false;
		else
			flanagan = true;

		int count5 = 200;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---------- MIRD CALCULATION 24h ----------";
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol24= " + out24[0];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal24= " + out24[1];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv24= " + out24[2];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		count5 = 220;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---------- MIRD CALCULATION 48h ----------";
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol48= " + out48[0];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal48= " + out48[1];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv48= " + out48[2];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		count5 = 240;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---------- MIRD CALCULATION 120h ---------";
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol120= " + out120[0];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal120= " + out120[1];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv120= " + out120[2];
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);

		if (!flanagan) {
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// CON IMAGEJ E BASTA
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

			count5 = 260;
			aux5 = "#" + String.format("%03d", count5++) + "#\t------- MIRD FIT RESULTS IMAGEJ ----------";
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD IJ FIT param 0= " + AA;
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD IJ FIT param 1= " + aa;
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FIT R^2= " + Rsquared;
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		} else {
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// CON FLANAGAN E BASTA
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			count5 = 270;
			aux5 = "#" + String.format("%03d", count5++) + "#\t------- MIRD FIT RESULTS FLANAGAN --------";
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);

			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FLANAGAN FIT param 0= " + AA;
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FLANAGAN FIT param 1= " + aa;
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FIT R^2= " + Rsquared;
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		}

		count5 = 300;
		if (flanagan) {
			aux5 = "#" + String.format("%03d", count5++) + "#\t---TRE PUNTI SELEZIONATI ELABORATI CON FLANAGAN------";
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		} else {
			aux5 = "#" + String.format("%03d", count5++) + "#\t---DUE PUNTI SELEZIONATI ELABORATI CON IMAGEJ -------";
			MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		}
		aux5 = "#" + String.format("%03d", count5++) + "#\tparametro A= " + AA;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tparametro a= " + aa;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tmAtilde= " + mAtilde;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tdisintegrazioni= " + disintegrazioni;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tuptake[%]= " + uptake;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tmassa= " + massa;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\ttmezzo= " + tmezzo;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\ttau= " + tau;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tdose= " + dose;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);

		aux5 = "#" + String.format("%03d", count5++) + "#\t------------- CALCOLO ERRORI -------------";
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\terrore SA= " + SA;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\terrore Sa= " + Sa;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSmAtilde= " + SmAtilde;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tS# disintegrazioni= " + Sdisintegrazioni;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSuptake= " + Suptake;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSmassa= " + Smassa;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tStmezzo= " + Stmezzo;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tStau= " + Stau;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSdose= " + Sdose;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);

		count5 = 500;
		aux5 = "#" + String.format("%03d", count5++) + "#\t------------- CALCOLO DOSE ---------------";
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s1= " + s1;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s2= " + s2;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m1= " + m1;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m2= " + m2;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose massa= " + massa;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);

		count5 = 600;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---------- ESPORTAZIONE GRAFICI ----------";
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tDVH MEDIO= " + str11;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tERRORE SUPERIORE= " + str12;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tERRORE INFERIORE= " + str13;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tPERCENTUALE= " + str14;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);

		count5 = 700;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---------- DVH VOXEL DOSIMETRY -----------";
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tDOSE MEDIA= " + Dmedia;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tDOSE 2%= " + valD2;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tDOSE 98%= " + valD98;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		count5 = 711;
		aux5 = "#" + String.format("%03d", count5++) + "#\tERRORE DOSE MEDIA= " + errMedia;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tERRORE DOSE 2%= " + errD2;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tERRORE DOSE 98%= " + errD98;
		MyLog.logAppend(MyGlobals.pathVolatile, aux5);

		// ==============================================================
		// BATTESIMO DELLA LESIONE
		// ==============================================================

		MyLog.logDedupe(MyGlobals.pathVolatile); // ELIMINAZIONE DOPPIONI

		MyDialog.dialogBattezzaLesioni_LP27(MyGlobals.pathVolatile);
//		String path22 = MyGlobals.desktopPath + File.separator + "DosimetryFolder" + File.separator;

//		Utility.rinominaImmagini(path22 + "volatile24h.tif", path22 + lesionName + "24h.tif");
//		Utility.rinominaImmagini(path22 + "volatile48h.tif", path22 + lesionName + "48h.tif");
//		Utility.rinominaImmagini(path22 + "volatile120h.tif", path22 + lesionName + "120h.tif");

		Utility.deleteAllFilesWithSpecificExtension(MyGlobals.desktopDosimetryFolderPath, "tif");
		Utility.chiudiTutto();
		IJ.showMessage("FINE LAVORO");
	}

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	/**
	 * Copia i file immagine da una directory all'altra
	 * 
	 * @param infile  Directory sorgente
	 * @param outfile Directory destinazione
	 * @throws IOException
	 */
	private void copyDirectory(File infile, File outfile) throws IOException {
		if (infile.isDirectory()) {
			if (!outfile.exists())
				outfile.mkdir();
			String[] arrayOfString = infile.list();
			for (byte b = 0; b < arrayOfString.length; b++)
				copyDirectory(new File(infile, arrayOfString[b]), new File(outfile, arrayOfString[b]));
		} else {
			FileInputStream fileInputStream = new FileInputStream(infile);
			FileOutputStream fileOutputStream = new FileOutputStream(outfile);
			byte[] arrayOfByte = new byte[1024];
			int i;
			while ((i = fileInputStream.read(arrayOfByte)) > 0)
				fileOutputStream.write(arrayOfByte, 0, i);
			fileInputStream.close();
			fileOutputStream.close();
		}
	}

	/**
	 * Tentativo di partorire la laboriosa stringa usata come titolo del PetCtViewer
	 * 
	 * @param meta header dicom della immagine o dello stack
	 * @return parto della stringa
	 */
	static String stringaLaboriosa(String meta) {

		MyGlobals.m_patName = compressPatName(getDicomValue(meta, "0010,0010"));
		String aux1 = "#600#\tm_patname= " + MyGlobals.m_patName;
		MyLog.logAppend(MyGlobals.pathPermanente, aux1);
		String tmp = getDicomValue(meta, "0010,0030");
		MyGlobals.m_patBirthday = getDateTime(tmp, null);
		if (MyGlobals.m_patBirthday != null) {
			long sdyTime, birthTime, currDiff;
			Integer years;
			int type2 = -1;
			Date studyDateTime = getStudyDateTime(meta, type2);
			sdyTime = studyDateTime.getTime();
			birthTime = MyGlobals.m_patBirthday.getTime();
			currDiff = (sdyTime - birthTime) / (24 * 60 * 60 * 1000); // number of days
			years = (int) (currDiff / 365.242199);
			MyGlobals.m_patYears = years.toString() + "y";
		}
		MyGlobals.m_patID = compressID(getDicomValue(meta, "0010,0020"));
		int type3 = -1;
		Date studyDateTime1 = getStudyDateTime(meta, type3);
		MyGlobals.m_serDate = UsaDateFormat(studyDateTime1);
		MyGlobals.m_styName = getDicomValue(meta, "0008,1030");
		MyGlobals.petSeriesName = getDicomValue(meta, "0008,103E");
		String laboriosa = "Pet-Ct: " + MyGlobals.m_patName + "   " + MyGlobals.m_patYears + "   " + MyGlobals.m_patID
				+ "   " + MyGlobals.m_serDate + "   " + MyGlobals.m_styName + "   "
				+ MyGlobals.petSeriesName.toLowerCase();

		return laboriosa;
	}

	/**
	 * Lettura di un dato Dicom da header immagine
	 * 
	 * @param meta
	 * @param key1
	 * @return
	 */
	static String getDicomValue(String meta, String key1) {
		String tmp1, key2 = key1, ret1 = null;
		int k1, k0 = 0;
		if (meta == null)
			return ret1;
		if (key1 != null) {
			k0 = meta.indexOf(key1);
			if (k0 <= 0)
				key2 = key1.toLowerCase();
			k0 = meta.indexOf(key2);
		}
		if (k0 > 0 || key2 == null) {
			// here we have a problem that the key may appear more than once.
			// for example a SeriesUID may appear in a sequence. Look for ">".
			if (k0 > 0) {
				tmp1 = meta.substring(k0 + 4, k0 + 16);
				k1 = tmp1.indexOf(">");
				while (k1 > 0) { // do search last value
					k1 = meta.indexOf(key2, k0 + 4);
					if (k1 > 0)
						k0 = k1;
				}
			}
			k1 = meta.indexOf("\n", k0);
			if (k1 < 0)
				return null;
			tmp1 = meta.substring(k0, k1);
			k1 = tmp1.indexOf(": ");
			if (k1 > 0)
				ret1 = tmp1.substring(k1 + 2);
			else
				ret1 = tmp1;
			ret1 = ret1.trim();
			if (ret1.isEmpty())
				ret1 = null;
		}
		return ret1;
	}

	/**
	 * Presenta patName secondo un suo standard
	 * 
	 * @param inName
	 * @return
	 */
	static String compressPatName(Object inName) {
		String retVal = checkEmpty(inName);
		if (retVal == null)
			return null;
		retVal = retVal.trim();
		int i = retVal.indexOf('^');
		if (i < 0)
			return retVal;
		retVal = retVal.substring(0, i) + "," + retVal.substring(i + 1);
		retVal = retVal.replace('^', ' ').trim();
		return retVal;
	}

	/**
	 * Restituisce solo se riceve stringa non vuota
	 * 
	 * @param in1
	 * @return
	 */
	static String checkEmpty(Object in1) {
		if (in1 == null || !(in1 instanceof String))
			return null;
		String out1 = (String) in1;
		if (out1.isEmpty())
			return null;
		return out1;
	}

	/**
	 * Helper routine to convert from Dicom style date-time to Java date-time. Watch
	 * out, sometimes the date uses periods, 2008.10.04 NOTA BENE: ANCORA DA METTERE
	 * A POSTO
	 * 
	 * @param inDate Dicom date format
	 * @param inTime Dicom time format
	 * @return Java Date object
	 */
	public static Date getDateTime(String inDate, String inTime) {

		Date retDate;
		GregorianCalendar dat1 = new GregorianCalendar();
		int off, year, month, day, hour = 0, min1 = 0, sec = 0;
		if (inDate == null || inDate.length() < 8)
			return null;
		off = 0; // normal case with no period
		if (inDate.charAt(4) == '.')
			off = 1;
		// watch out for bad date 01.01.1900
		if (inDate.charAt(2) == '.')
			return null;

		year = Integer.valueOf(inDate.substring(0, 4));
		month = Integer.valueOf(inDate.substring(4 + off, 6 + off)) - 1; // month 0 based
		day = Integer.valueOf(inDate.substring(6 + 2 * off, 8 + 2 * off));
		if (inDate.length() >= 14) {
			hour = Integer.valueOf(inDate.substring(8, 10));
			min1 = Integer.valueOf(inDate.substring(10, 12));
			sec = Integer.valueOf(inDate.substring(12, 14));
		} else if (inTime != null && inTime.length() >= 6) {
			hour = Integer.valueOf(inTime.substring(0, 2));
			min1 = Integer.valueOf(inTime.substring(2, 4));
			sec = Integer.valueOf(inTime.substring(4, 6));
		}
		dat1.set(year, month, day, hour, min1, sec);
		retDate = dat1.getTime();
		return retDate;
	}

	/**
	 * Acquisizione DateTime per vari tipi di immagini
	 * 
	 * @param meta
	 * @param type
	 * @return
	 */
	static Date getStudyDateTime(String meta, int type) {
		String key1 = null, key2 = null, time1 = null, time2, tmp1;
		String key0 = "0008,0021"; // series date
		switch (type) {
		case 0:
			key1 = "0008,0030"; // study time
			key0 = "0008,0020"; // study date
			break;

		case 1:
			key2 = "0008,002A"; // acquisition date time
			key1 = "0008,0031"; // series time
			key0 = "0008,0021"; // series date
			break;

		case 2:
			key1 = "0008,0032"; // acquisition time
			key0 = "0008,0022"; // acquisition date
			break;

		case 3:
			key2 = "0018,1078"; // date time
			key1 = "0018,1072"; // injection time
			key0 = "0009,103B"; // GE Advance Scan.admin_datetime PRIVATE TAG
			break;

		case 4:
			key1 = "0008,0033"; // image time
			key0 = "0008,0023"; // image date
			break;
		}
		if (key1 != null) {
			time1 = getDicomValue(meta, key1);
		}
		if (key2 != null && (type == 3 || time1 == null)) {
			time2 = getDicomValue(meta, key2);
			if (time2 != null)
				time1 = time2; // prefer key2
			if (time1 != null && time1.length() >= 14)
				return getDateTime(time1, null);
		}
		// use study date since the injection may be 24 or 48 hours earlier
		tmp1 = getDicomValue(meta, key0);
		if (tmp1 == null) {
			tmp1 = getDicomValue(meta, "0008,0020");
			if (tmp1 == null)
				return null;
			// be careful of bad study dates like 1899
			if (Integer.valueOf(tmp1.substring(0, 4)) < 1980) {
				tmp1 = getDicomValue(meta, "0008,0021");
			}
		}

		return getDateTime(tmp1, time1);
	}

	/**
	 * Modifiche per adattarsi a quello che viene fatto in Pet-Ct-Viewer
	 * 
	 * @param in1
	 * @return
	 */
	static String compressID(Object in1) {
		String ret1, ret0 = checkEmpty(in1);
		if (ret0 == null)
			return "0";
		ret0 = ret0.toLowerCase();
		int i, i1, n = ret0.length();
		char a1;
		ret1 = "";
		for (i = i1 = 0; i < n; i++) {
			a1 = ret0.charAt(i);
			if (Character.isDigit(a1) || Character.isLetter(a1)) {
				if (i1 == 0 && a1 == '0')
					continue;
				ret1 = ret1 + a1;
				i1++;
			}
		}
		if (i1 == 0)
			return "0";
		return ret1;
	}

	/**
	 * Impone il formato data da usare
	 * 
	 * @param inDate
	 * @return
	 */
	static String UsaDateFormat(Date inDate) {
		if (inDate == null)
			return "";
		return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(inDate);
	}

	/**
	 * Inizializzazione dei file di log
	 * 
	 * @param init
	 */
	void initializeLogs(boolean init) {
		// --------------------------------------------------------------------------------------
		// definisco ed inizializzo (se richiesto) i file log di esportazione dati
		// --------------------------------------------------------------------------------------
		Date now = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy - HH:mm.ss");
		MyLog.logInit(MyGlobals.pathVolatile);
		MyLog.logAppend(MyGlobals.pathVolatile, "INITIALIZED " + dateformat.format(now));
		File f1 = new File(MyGlobals.pathPermanente);
		if (init || !f1.exists()) {
			MyLog.logInit(MyGlobals.pathPermanente);
			MyLog.logAppend(MyGlobals.pathPermanente, "INITIALIZED " + dateformat.format(now));
		} else {
			MyLog.logAppend(MyGlobals.pathPermanente, "PRESERVED " + dateformat.format(now));
		}

	}

	/**
	 * Copia delle immagini dalla cartella sorgente al dosimetryFolder
	 * 
	 * @param arrayOfFile1
	 * @param arrayOfFile2
	 * @param arrayOfBoolean
	 * @param desktopImagesSubfolderPath
	 */
	boolean copyFilesToDesktopDosimetryFolder(File[] arrayOfFile1, File[] arrayOfFile2, boolean[] arrayOfBoolean,
			String desktopImagesSubfolderPath) {
		// ----------------------------------------
		// Copia delle immagini dalla sorgente al DosimetryFolder situato sul desktop
		// ----------------------------------------

		int len1 = 0;
		int count1 = 0;
		int count2 = 0;
		int count3 = 0;

		for (int b2 = 0; b2 < arrayOfFile1.length; b2++) {
			if (arrayOfBoolean[b2]) {
				File file = arrayOfFile1[b2];
				File[] arrayOfFile = file.listFiles();
				len1 = len1 + arrayOfFile.length;
			}
		}
//		MyLog.waitHere("len1= " + len1);

		for (int b2 = 0; b2 < arrayOfFile1.length; b2++) {
			count2 = 0;
			if (arrayOfBoolean[b2]) {
				File file = arrayOfFile1[b2];
				File[] arrayOfFile = file.listFiles();
				for (File file5 : arrayOfFile) {
					count1++;
					IJ.showStatus("AAA " + count1 + "/" + len1);
//					if (b2 == 0 && count1 == 1)
//						MyLog.waitHere("");

					File file6 = null;
					if (file5.getName().contains("IRAC")) {
						count2++;
						file6 = new File(desktopImagesSubfolderPath + File.separator + MyGlobals.arrayOfString[b2]
								+ File.separator + "SPECT" + File.separator + file5.getName());
						try {
							copyDirectory(file5, file6);
						} catch (Exception exception) {
							MyDialog.dialogErrorMessage_LP06(
									"An Error occurred while coping the SPECT image. Please try again!");
							return false;
						}
					} else if (file5.getName().contains("CTAC")) {
						file6 = new File(desktopImagesSubfolderPath + File.separator + MyGlobals.arrayOfString[b2]
								+ File.separator + "CT" + File.separator + file5.getName());
						try {
							copyDirectory(file5, file6);
						} catch (Exception exception) {
							MyDialog.dialogErrorMessage_LP06(
									"An Error occurred while coping CT images. Please try again!");
							return false;
						}
					} else if (file5.getName().contains("EM001")) {
						arrayOfFile2[b2] = file5;
						count3++;
					}
				}
			} else {
				MyDialog.dialogErrorMessage_LP06(
						"It was not possible to import files for " + MyGlobals.arrayOfString[b2] + " folder.");
			}
			if (count2 > 1) {
				MyLog.waitHere(
						"ATTENZIONE i file IRACSC possono essere solo 1 per ogni cartella (24-48-120).\n \n>>> CANCELLARE DALLE CARTELLE SORGENTI I FILE ECCEDENTI <<<\n \n>>>>>>> CANCELLARE anche la cartella DosimetryFolder del Desktop <<<<<\n \n>>>>>>> POI RIPROVARE <<<<<<");
				return false;
			}
		}
		
		if (count3 != 3) {
			MyLog.waitHere(
					"count3= "+count3+" ATTENZIONE i file EM001 devono ASSOLUTAMENTE essere uno per ogni cartella (24-48-120).\n \n>>>>>>>>>>> PROVVEDERE A RECUPERARLI <<<<<<<<<<<\n \n>>>>>>> CANCELLARE anche la cartella DosimetryFolder del Desktop <<<<<\n \n>>>>>>> POI RIPROVARE <<<<<<");
			return false;
		}

		return true;
	}

	/**
	 * Ispeziona DosimetryFolder/ImagesFolder per leggere i dati Dicom di una delle
	 * immagini. Verrano restituiti seriesDescription e nome paziente, oppure null,
	 * in caso di immagine non trovata.
	 * 
	 * @param path1
	 * @return
	 */
	String[] inspector(String path1) {

		MyLog.log("INSPECTOR cerca nel folder= " + path1);
		File file1 = new File(path1);
		File file2 = new File(path1 + File.separator + "ImagesFolder");
		List<File> list1 = null;
		boolean ok1 = Utility.checkDir(file1);
		if (!ok1) {
			MyLog.log("INSPECTOR return null MANCA DOSIMETRY_FOLDER");
			return null;
		}
		boolean ok2 = Utility.checkDir(file2);
		if (ok2) {
			list1 = Utility.getFileListing(file2);
			if (list1 == null) {
				MyLog.log("INSPECTOR return null MANCA IMAGES_FOLDER");
			} else if (list1.size() == 0) {
				MyLog.log("INSPECTOR list1 vuota NESSUNA IMMAGINE");
			} else {
//				for (int i1 = 0; i1 < list1.size(); i1++) {
//					MyLog.log("INSPECTOR ha trovato " + list1.get(i1).toString());
//				}
			}
		} else {
			MyLog.log("INSPECTOR return null MANCA IMAGES_FOLDER");
			return null;
		}
		if (list1.size() == 0)
			return null;
		String path2 = list1.get(0).toString();
		ImagePlus imp2 = Utility.openImage(path2);
		if (imp2 == null)
			return null;
		String nome = DicomTools.getTag(imp2, "0010,0010");
		String seriesDescription = DicomTools.getTag(imp2, "0008,103E");
		String[] out1 = new String[2];
		out1[0] = seriesDescription;
		out1[1] = nome;

		return out1;
	}

	/**
	 * Cancellazione e creazione dosimetry folder e images folder, loro riempimento
	 * 
	 * @return lista delle tre cartelle create in imagesFolder (contenuto in
	 *         dosimetryFolder)
	 */
	public File[] desktopImagesFolderFill() {

		String strDir24h = null;
		String strDir48h = null;
		String strDir120h = null;

		// chiede di identificare la cartella 24h sorgente
		do {
			boolean ok = MyDialog.dialogSelection_LP02();
			if (!ok)
				return null;
			strDir24h = MyDialog.directorySelection_LP_20(); // nome directory 24h
		} while (strDir24h == null);
		// costruisce i due path 48h e 120h
		strDir48h = strDir24h.replace(MyGlobals.arrayOfString[0], MyGlobals.arrayOfString[1]); // nome directory 48h
		strDir120h = strDir24h.replace(MyGlobals.arrayOfString[0], MyGlobals.arrayOfString[2]); // nome directory 120h
		File file24 = new File(strDir24h);
		File file48 = new File(strDir48h);
		File file120 = new File(strDir120h);
		boolean[] arrayOfBoolean = { false, false, false };
		if (file24.exists()) {
			arrayOfBoolean[0] = true;
		} else {
			strDir24h = "Not Found";
		}
		if (file48.exists()) {
			arrayOfBoolean[1] = true;
		} else {
			strDir48h = "Not Found";
		}
		if (file120.exists()) {
			arrayOfBoolean[2] = true;
		} else {
			strDir120h = "Not Found";
		}
		// chiede conferma della selezione effettuata e solo dopo la conferma cancella
		// l'esistente

		boolean ok = MyDialog.dialogConfirmFolder_LP03(strDir24h, strDir48h, strDir120h);
		if (ok) {

			File file1 = new File(MyGlobals.desktopImagesSubfolderPath);
			// cancella cartella anco se piena
			Utility.deleteDirectory(file1);

			// crea le cartelle destinazione
			for (int b1 = 0; b1 < MyGlobals.arrayOfString.length; b1++) {

				File file5 = new File(
						MyGlobals.desktopImagesSubfolderPath + File.separator + MyGlobals.arrayOfString[b1]);
				if (file5.mkdirs()) {
				} else {
					MyLog.log("cartella non creata= " + file5.toString());
				}

				File file6 = new File(MyGlobals.desktopImagesSubfolderPath + File.separator
						+ MyGlobals.arrayOfString[b1] + File.separator + "CT");
				if (file6.mkdirs()) {
				} else {
					MyLog.log("cartella non creata= " + file6.toString());
				}

				File file7 = new File(MyGlobals.desktopImagesSubfolderPath + File.separator
						+ MyGlobals.arrayOfString[b1] + File.separator + "SPECT");
				if (file7.mkdirs()) {
				} else {
					MyLog.log("cartella non creata= " + file7.toString());
				}

			}
		} else {
			return null;
		}

		// ----------------------------------------
		// Copia delle immagini dalla sorgente al DosimetryFolder situato sul desktop
		// ----------------------------------------

		File[] arrayOfFile1 = { file24, file48, file120 };
		File[] arrayOfFile2 = new File[arrayOfFile1.length];
		boolean ok1 = copyFilesToDesktopDosimetryFolder(arrayOfFile1, arrayOfFile2, arrayOfBoolean,
				MyGlobals.desktopImagesSubfolderPath);

		if (ok1) {
			return arrayOfFile2;
		} else
			return null;

	}

	/**
	 * Trasformazione input Data da tastiera in formato dicom
	 * 
	 * @param data0 data da trasformare
	 * @return data in formato dicom
	 */
	public String dataToDicom(String data0) {

		String day = data0.substring(0, 2);
		String month = data0.substring(3, 5);
		String year = data0.substring(6, 10);
		String data1 = year + month + day;

		return data1;
	}

	/**
	 * Trasformazione input Ora da tastiera in formato dicom
	 * 
	 * @param ora0 ora da trasformare
	 * @return ora in formato dicom
	 */
	public String oraToDicom(String ora0) {

		String ora = ora0.substring(0, 2);
		String min = ora0.substring(3, 5);
		String sec = ora0.substring(6, 8);
		String ora1 = ora + min + sec;

		return ora1;
	}

	/**
	 * Lettura dati dalle immagini per NUOVO PAZIENTE, scrittura in volatile.txt
	 * 
	 * @param vetFile immagini da analizzare
	 * @param myDate0 dataOra somministrazione per calcolo deltaT
	 */
	public void raccoltaDati(File[] vetFile, Date myDate0) {

//		String deb1 = "";

		if (vetFile == null)
			MyLog.waitHere("vetFile==null");

		ArrayList<ArrayList<String>> aList = new ArrayList<ArrayList<String>>();
		ArrayList<Long> eList = new ArrayList<Long>();
		for (byte b3 = 0; b3 < MyGlobals.arrayOfString.length; b3++) {
			MyLog.log("raccoltaDati b3= " + b3);
			IJ.log(vetFile[b3].toString());
//			deb1 = vetFile[b3].getName();
//			IJ.log("b3= " + b3 + " deb1= " + deb1);
			ImagePlus imp8 = IJ.openImage(vetFile[b3].getAbsolutePath());
			int durata = Utility.MIRD_calcoloDurataAcquisizione(imp8);
			String acqDate = DicomTools.getTag(imp8, "0008,0022").trim();
			String acqTime = DicomTools.getTag(imp8, "0008,0032").trim();
			// MyLog.log("getDateTime: date= " + acqDate + " time=" + acqTime);
			Date myDateTime1 = getDateTime(acqDate, acqTime);
			SimpleDateFormat sdf = new SimpleDateFormat(MyGlobals.format1);
			String myDateTime2 = sdf.format(myDateTime1);

			long myDelta1 = Utility.MIRD_calcoloDeltaT(myDate0, myDateTime1);
			eList.add(myDelta1);
			ArrayList<String> bList = new ArrayList<String>();
			bList.add("\tImage Name= " + vetFile[b3].getName());
			bList.add("\tAcquisition DateTime= " + myDateTime2);
			bList.add("\tDummy");
			bList.add("\tIsotope= " + DicomTools.getTag(imp8, "0011,100D"));
			bList.add("\tCollimator= " + DicomTools.getTag(imp8, "0018,1180"));
			bList.add("\tNumber of frames= " + DicomTools.getTag(imp8, "0054,0053"));
			bList.add("\tActual frame duration= " + DicomTools.getTag(imp8, "0018,1242"));
			bList.add("\tAcquisition duration= " + durata);
//			bList.add("\tPathCompleto: " + arrayOfFile2[b3].getAbsolutePath() + "\n"); /// solo per test
			aList.add(bList);
		}
		// --------------------------------------------------------------------------------------
		// Nel file volatile vengono scritti dal seguente loop i seguenti TAG e i
		// relativio dati
		// #011#,#012#,#013#,#014#,#015#,#016#,#017#,#018#,#019#
		// #031#,#032#,#033#,#034#,#035#,#036#,#037#,#038#,#039#
		// #051#,#052#,#053#,#054#,#055#,#056#,#057#,#058#,#059#
		// --------------------------------------------------------------------------------------
//		String aux1 = "";
		String aux2 = "";
		String aux3 = "";
		int count2 = 0;
		for (int a4 = 0; a4 < aList.size(); a4++) {
			count2 = a4 * 20 + 10;
//			aux1 = "#010#\t---- IMAGE INFO " + arrayOfString[a4] + " ----";
			aux2 = "#" + String.format("%03d", count2++) + "#\t---- IMAGE INFO " + MyGlobals.arrayOfString[a4]
					+ " ----";
			MyLog.logAppend(MyGlobals.pathVolatile, aux2);
			String str22 = "";
			ArrayList<String> cList = aList.get(a4);
			for (int b4 = 0; b4 < cList.size(); b4++) {
				aux2 = "#" + String.format("%03d", count2++) + "#";
				String str9 = cList.get(b4);
				str22 = aux2 + str9;
				MyLog.logAppend(MyGlobals.pathVolatile, str22);
			}
			aux3 = "#" + String.format("%03d", count2++) + "#" + "\tDeltaT= "
					+ (double) eList.get(a4) / (1000 * 60 * 60);
			MyLog.logAppend(MyGlobals.pathVolatile, aux3);
		}
	}

	/**
	 * Lettura dati dalle immagini per NUOVO PAZIENTE, scrittura in volatile.txt.
	 * Questa nuova versione dovrebbe leggere i dati dalla DosimetryFolder, anziche'
	 * da quella sorgente
	 * 
	 * @param vetFile immagini da analizzare
	 * @param myDate0 dataOra somministrazione per calcolo deltaT
	 */
	public void raccoltaDatiNuova(Date myDate0) {

		ArrayList<ArrayList<String>> aList = new ArrayList<ArrayList<String>>();

		String str1 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator + "ImagesFolder" + File.separator;

		String startingDir1 = "";

		ArrayList<Long> eList = new ArrayList<Long>();
		for (byte b3 = 0; b3 < MyGlobals.arrayOfString.length; b3++) {
			startingDir1 = str1 + MyGlobals.arrayOfString[b3] + File.separator + "SPECT";
			List<File> result1 = Utility.getFileListing(new File(startingDir1));
			if (result1 == null) {
				IJ.error("getFileListing.result1==null");
			}
			int len2 = result1.size();
			if (len2 != 1)
				return;
			File fil1 = result1.get(0);
			String path1 = fil1.getAbsolutePath();
			ImagePlus imp8 = Utility.openImage(path1);
//			IJ.log(vetFile[b3].toString());
////			IJ.log("b3= " + b3 + " deb1= " + deb1);
//			ImagePlus imp8 = IJ.openImage(vetFile[b3].getAbsolutePath());
			int durata = Utility.MIRD_calcoloDurataAcquisizione(imp8);
			String acqDate = DicomTools.getTag(imp8, "0008,0022").trim();
			String acqTime = DicomTools.getTag(imp8, "0008,0032").trim();
			// MyLog.log("getDateTime: date= " + acqDate + " time=" + acqTime);
			Date myDateTime1 = getDateTime(acqDate, acqTime);
			SimpleDateFormat sdf = new SimpleDateFormat(MyGlobals.format1);
			String myDateTime2 = sdf.format(myDateTime1);

			long myDelta1 = Utility.MIRD_calcoloDeltaT(myDate0, myDateTime1);
			eList.add(myDelta1);
			ArrayList<String> bList = new ArrayList<String>();
			bList.add("\tImage Name= " + imp8.getTitle());
			bList.add("\tAcquisition DateTime= " + myDateTime2);
			bList.add("\tDummy");
			bList.add("\tIsotope= " + DicomTools.getTag(imp8, "0011,100D"));
			bList.add("\tCollimator= " + DicomTools.getTag(imp8, "0018,1180"));
			bList.add("\tNumber of frames= " + DicomTools.getTag(imp8, "0054,0053"));
			bList.add("\tActual frame duration= " + DicomTools.getTag(imp8, "0018,1242"));
			bList.add("\tAcquisition duration= " + durata);
//			bList.add("\tPathCompleto: " + arrayOfFile2[b3].getAbsolutePath() + "\n"); /// solo per test
			aList.add(bList);
		}
		// --------------------------------------------------------------------------------------
		// Nel file volatile vengono scritti dal seguente loop i seguenti TAG e i
		// relativio dati
		// #011#,#012#,#013#,#014#,#015#,#016#,#017#,#018#,#019#
		// #031#,#032#,#033#,#034#,#035#,#036#,#037#,#038#,#039#
		// #051#,#052#,#053#,#054#,#055#,#056#,#057#,#058#,#059#
		// --------------------------------------------------------------------------------------
//		String aux1 = "";
		String aux2 = "";
		String aux3 = "";
		int count2 = 0;
		for (int a4 = 0; a4 < aList.size(); a4++) {
			count2 = a4 * 20 + 10;
//			aux1 = "#010#\t---- IMAGE INFO " + arrayOfString[a4] + " ----";
			aux2 = "#" + String.format("%03d", count2++) + "#\t---- IMAGE INFO " + MyGlobals.arrayOfString[a4]
					+ " ----";
			MyLog.logAppend(MyGlobals.pathVolatile, aux2);
			String str22 = "";
			ArrayList<String> cList = aList.get(a4);
			for (int b4 = 0; b4 < cList.size(); b4++) {
				aux2 = "#" + String.format("%03d", count2++) + "#";
				String str9 = cList.get(b4);
				str22 = aux2 + str9;
				MyLog.logAppend(MyGlobals.pathVolatile, str22);
			}
			aux3 = "#" + String.format("%03d", count2++) + "#" + "\tDeltaT= "
					+ (double) eList.get(a4) / (1000 * 60 * 60);
			MyLog.logAppend(MyGlobals.pathVolatile, aux3);
		}
	}

	/**
	 * SErve ad azzerare, mettendoli a false i tag #901#, #902#, #903 e #904#,
	 * utilizzati per comunicare tra Dosimetria_Lu17 e Dosimetry_v2,
	 * 
	 * @param path
	 */
	void azzeraFlags(String path) {

		String aux1 = "";
		aux1 = "#901#\tokk= false";
		MyLog.logModify(path, "#901#", aux1);
		aux1 = "#902#\tokk= false";
		MyLog.logModify(path, "#902#", aux1);
		aux1 = "#903#\tokk= false";
		MyLog.logModify(path, "#903#", aux1);
		aux1 = "#904#\tokk= false";
		MyLog.logModify(path, "#904#", aux1);

	}

}
