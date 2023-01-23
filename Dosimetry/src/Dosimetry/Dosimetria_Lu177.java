package Dosimetry;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.image.ColorModel;
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
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Plot;
import ij.io.DirectoryChooser;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.plugin.DICOM;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.util.DicomTools;
import ij.util.FontUtil;

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
 * @author
 * @since 05 dec 2022
 */
public class Dosimetria_Lu177 implements PlugIn {

	static String fontStyle = "Arial";
	static Font defaultFont = FontUtil.getFont(fontStyle, Font.PLAIN, 13);
	static Font textFont = FontUtil.getFont(fontStyle, Font.ITALIC, 16);
	static Font titleFont = FontUtil.getFont(fontStyle, Font.BOLD, 16);

	public String[] lista;
	public int numFile;
	public int numTotal;
	public int count2;
	public int count;

	static String m_patName;
	static Date m_patBirthday = null;
	static String m_patYears;
	static String m_patID;
	static String m_serDate;
	static String m_styDate;
	static String m_styName;
	static String petSeriesName;
	static String desktopPath;
	static String desktopDosimetryFolderPath;
	static String desktopImagesSubfolderPath;
	static String pathPermanente;
	static String pathVolatile;
	static String[] arrayOfString = { "24h", "48h", "120h" };
	String format1 = "dd-MM-yyyy HH:mm:ss";

	public void run(String paramString) {

		desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
		pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";
		desktopDosimetryFolderPath = desktopPath + File.separator + "DosimetryFolder";
		desktopImagesSubfolderPath = desktopDosimetryFolderPath + File.separator + "ImagesFolder";

		String petctviewerTitle = "";
		Double activitySomministrazione;
		String dataSomministrazione;
		String oraSomministrazione;
		Date dataOraSomministrazione = null;
		boolean nuovoPaziente = false;
		boolean nuovoDistretto = false;
		boolean nuoveImmagini = false;
		boolean datiSomministrazioneValidi = false;
		File[] arrayOfFile2 = null;
		String aux5 = "";
		int out1 = 0;

		// ===========================================================
		// LEGGO CARTELLA DOSIMETRY FOLDER (E SOTTOCARTELLA IMAGES FOLDER)
		// ===========================================================
		String[] insOut = inspector(desktopDosimetryFolderPath);
		datiSomministrazioneValidi = Utility.datiSomministrazionePresenti(pathPermanente);
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
			out1 = dialogImmaginiPazientePrecedente_LP21(insOut);
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
				nuovoDistretto = dialogDistretto_LP07();
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
			Utility.logDeleteAll(desktopDosimetryFolderPath);
			Utility.logInit(pathPermanente);
			Utility.logInit(pathVolatile);
			String[] datiSomministrazione = null;
			boolean okDati = false;
			do {
				datiSomministrazione = dialogInputDatiSomministrazione_LP04();
				if (datiSomministrazione == null) {
					MyLog.log("datiSomministrazione NON PERVENUTI");
					return;
				}
				okDati = dialogConfermaDatiSomministrazione_LP10(datiSomministrazione);
			} while (!okDati);
			dataSomministrazione = datiSomministrazione[0];
			oraSomministrazione = datiSomministrazione[1];
			activitySomministrazione = Double.parseDouble(datiSomministrazione[2]);
			dataOraSomministrazione = getDateTime(dataToDicom(dataSomministrazione), oraToDicom(oraSomministrazione));

			MyLog.log("NUOVO PAZIENTE, SCRITTURA DATI SOMMINISTRAZIONE SU VOLATILE");
			Utility.logAppend(pathVolatile, "#000#\t-- SOMMINISTRAZIONE --");

			SimpleDateFormat sdf = new SimpleDateFormat(format1);
			String myDTT = sdf.format(dataOraSomministrazione);

			String aux1 = "";
			aux1 = "#001#\tDateTime administration= " + myDTT;
			Utility.logAppend(pathVolatile, aux1);
			aux1 = "#002#\tDummy";
			Utility.logAppend(pathVolatile, aux1);
			aux1 = "#003#\tActivity= " + activitySomministrazione;
			Utility.logAppend(pathVolatile, aux1);
			// copia da volatile a permanente i dati di SOMMINISTRAZIONE
			Utility.logCopyRange(pathVolatile, pathPermanente, 0, 3);
			raccoltaDati(arrayOfFile2, dataOraSomministrazione);
			// copia da volatile a permanente i dati di IMAGE INFO 24-48-120
			Utility.logCopyRange(pathVolatile, pathPermanente, 10, 60);
		} else if (nuovoDistretto) { // stesso paziente nuovo distretto nuova lesione
			// ============================================
			// STESSO PAZIENTE, NUOVO DISTRETTO, NUOVA LESIONE
			// ============================================
			nuoveImmagini = true;
			MyLog.log("NUOVO DISTRETTO, CARICAMENTO IMMAGINI E \nRECUPERO DATI SOMMINISTRAZIONE DA PERMANENTE");
			arrayOfFile2 = desktopImagesFolderFill();
			if (arrayOfFile2 == null)
				return;
			Utility.logInit(pathVolatile);
			// copia da permanente a volatile i dati di SOMMINISTRAZIONE
			Utility.logCopyRange(pathPermanente, pathVolatile, 0, 3);
			// copia da permanente a volatile i dati di IMAGE INFO 24-48-120
			Utility.logCopyRange(pathPermanente, pathVolatile, 10, 60);
			dataOraSomministrazione = Utility.getDateTime(Utility.readFromLog(pathVolatile, "#001#", "="), format1);
			// oraSomministrazione = Utility.readFromLog(pathVolatile, "#002#", "=");
			activitySomministrazione = Double.parseDouble(Utility.readFromLog(pathVolatile, "#003#", "="));
//			dataOraSomministrazione = getDateTime(dataToDicom(dataSomministrazione), oraToDicom(oraSomministrazione));
			MyLog.log("dataOraSomministrazione= " + dataOraSomministrazione);
//			MyLog.log("oraSomministrazione= " + oraSomministrazione);
			azzeraFlags(pathPermanente);
		} else {
			// ============================================
			// STESSO PAZIENTE, STESSO DISTRETTO, NUOVA LESIONE
			// ============================================
			MyLog.log("NUOVA LESIONE, RECUPERO DATI SOMMINISTRAZIONE DA PERMANENTE");
			Utility.logInit(pathVolatile);
			// copia da permanente a volatile i dati di IMAGE INFO 24-48-120
			Utility.logCopyRange(pathPermanente, pathVolatile, 0, 3);
			// copia da permanente a volatile i dati di IMAGE INFO 24-48-120
			Utility.logCopyRange(pathPermanente, pathVolatile, 10, 60);
			dataOraSomministrazione = Utility.getDateTime(Utility.readFromLog(pathVolatile, "#001#", "="), format1);
			// oraSomministrazione = Utility.readFromLog(pathVolatile, "#002#", "=");
			activitySomministrazione = Double.parseDouble(Utility.readFromLog(pathVolatile, "#003#", "="));
//			dataOraSomministrazione = getDateTime(dataToDicom(dataSomministrazione), oraToDicom(oraSomministrazione));
			MyLog.log("dataOraSomministrazione= " + dataOraSomministrazione);
//			MyLog.log("oraSomministrazione= " + oraSomministrazione);
			azzeraFlags(pathPermanente);
		}

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
		int numParams = 0;
		double[] outCF = null;
		double[] paramsIJ = null;
		double[] paramsFLA = null;
		double fitGoodnessIJ = 0;
		double rSquaredIJ = 0;
		double rSquaredFLA = 0;
		double rSquaredFLAadjusted = 0;

		int scelta = 0;
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
		imp1 = openImage(path1);
		String tit1 = imp1.getTitle();
		tit1 = "A024 ## " + tit1;
		imp1.setTitle(tit1);
		imp1.show();
		String meta1 = getMeta(slice1, imp1);

		if (nuoveImmagini) {
			petctviewerTitle = stringaLaboriosa(meta1);
			Utility.logAppend(pathPermanente, "24h=" + petctviewerTitle);
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
		imp2 = readStackFiles(startingDir2);

		String tit2 = imp2.getTitle();
		tit2 = "B024 ## " + tit2;
		imp2.setTitle(tit2);
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
		imp3 = openImage(path3);
		String tit3 = imp3.getTitle();
		tit3 = "A048 ## " + tit3;
		imp3.setTitle(tit3);
		imp3.show();
		String meta3 = getMeta(slice1, imp3);
		if (nuoveImmagini) {
			petctviewerTitle = stringaLaboriosa(meta3);
			Utility.logAppend(pathPermanente, "48h=" + petctviewerTitle);
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

		imp4 = readStackFiles(startingDir4);
		String tit4 = imp4.getTitle();
		tit4 = "B048 ## " + tit4;
		imp4.setTitle(tit4);
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
		imp5 = openImage(path5);
		String tit5 = imp5.getTitle();
		tit5 = "A120 ## " + tit5;
		imp5.setTitle(tit5);
		imp5.show();
		String meta5 = getMeta(slice1, imp5);
		if (nuoveImmagini) {
			petctviewerTitle = stringaLaboriosa(meta5);
			Utility.logAppend(pathPermanente, "120h=" + petctviewerTitle);
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

		imp6 = readStackFiles(startingDir6);
		String tit6 = imp6.getTitle();
		tit6 = "B120 ## " + tit6;
		imp6.setTitle(tit6);
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
		double MIRD_fatCal24 = Double.NaN;
		double MIRD_attiv24 = Double.NaN;
		double MIRD_vol48 = Double.NaN;
		double MIRD_fatCal48 = Double.NaN;
		double MIRD_attiv48 = Double.NaN;
		double MIRD_vol120 = Double.NaN;
		double MIRD_fatCal120 = Double.NaN;
		double MIRD_attiv120 = Double.NaN;
		double[] out2 = null;
		int decis1 = -1;
		int count3 = -1;
		boolean solodue = false;
		// ===========================================================================

		boolean ok = dialogInstructions_LP30();
		if (!ok)
			return;

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
			MIRD_fatCal24 = Double.NaN;
			MIRD_attiv24 = Double.NaN;
			MIRD_vol48 = Double.NaN;
			MIRD_fatCal48 = Double.NaN;
			MIRD_attiv48 = Double.NaN;
			MIRD_vol120 = Double.NaN;
			MIRD_fatCal120 = Double.NaN;
			MIRD_attiv120 = Double.NaN;
			decis1 = -1;
			out24 = null;
			out48 = null;
			out120 = null;
			numParams = 0;
			outCF = null;
			paramsIJ = null;
			paramsFLA = null;
			fitGoodnessIJ = 0;
			rSquaredIJ = 0;
			rSquaredFLA = 0;
			rSquaredFLAadjusted = 0;

			scelta = 0;
			slice1 = 1;
			imp1 = null;
			imp2 = null;
			imp3 = null;
			imp4 = null;
			imp5 = null;
			imp6 = null;
			flanagan = false;
			solodue = false;

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
				Utility.logEnd(pathPermanente);

			// ==========================================================================================
			// Elaborazione 24/48/120h
			// ==========================================================================================

			// 24h
			// se non mi ha scritto il tag #121# di volatile vuol dire che Dosimetry_v2 non
			// ha analizzato la immagine 24h (probabile cancel dato al menu)

			if (Utility.readFromLog(pathVolatile, "#121#", "=") == null)
				return;

			double[] in1 = new double[5];
			in1[0] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#018#", "=")); // acquisition duration
			in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#121#", "=")); // pixel number over
																							// threshold
			in1[2] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#003#", "=")); // activity
			in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#115#", "=")); // contouring threshold
																							// level
			in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#122#", "=")); // over threshold count
																							// integral
			out24 = Utility.MIRD_point(in1);
			MIRD_vol24 = out24[0];
			MIRD_fatCal24 = out24[1];
			MIRD_attiv24 = out24[2];

			// 48h
			// se non mi ha scritto il tag #151# di volatile vuol dire che Dosimetry_v2 non
			// ha analizzato la immagine 24h (probabile cancel dato al menu)
			if (Utility.readFromLog(pathVolatile, "#151#", "=") == null)
				return;
			in1[0] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#038#", "=")); // acquisition duration
			in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#151#", "=")); // pixel number over
																							// threshold
			in1[2] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#003#", "=")); // activity
			in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#145#", "=")); // contouring threshold
																							// level
			in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#152#", "=")); // over threshold count
																							// integral
			out48 = Utility.MIRD_point(in1);
			MIRD_vol48 = out48[0];
			MIRD_fatCal48 = out48[1];
			MIRD_attiv48 = out48[2];

			// 120h
			// se non mi ha scritto il tag #181# di volatile vuol dire che Dosimetry_v2 non
			// ha analizzato la immagine 24h (probabile cancel dato al menu)
			if (Utility.readFromLog(pathVolatile, "#181#", "=") == null)
				return;
			in1[0] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#058#", "=")); // acquisition duration
			in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#181#", "=")); // pixel number over
																							// threshold
			in1[2] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#003#", "=")); // activity
			in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#175#", "=")); // contouring threshold
																							// level
			in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#182#", "=")); // over threshold count
																							// integral
			out120 = Utility.MIRD_point(in1);


			MIRD_vol120 = out120[0];
			MIRD_fatCal120 = out120[1];
			MIRD_attiv120 = out120[2];

			double[] xp1 = new double[3];
			double[] yp1 = new double[3];
			xp1[0] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#019#", "=")); // deltaT
			yp1[0] = out24[2];
			xp1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#039#", "=")); // deltaT
			yp1[1] = out48[2];
			xp1[2] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#059#", "=")); // deltaT
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

			// Mostro i 3 volumi calcolati ed i punti, senza fit, in modo che, con LP33
			// venga scelto l'eventuale punto da togliere
			double[] vetInput = null;

			MIRD_display_LP66(MIRD_vol24, MIRD_vol48, MIRD_vol120);


			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// QUI TORNO PER PROBLEMI DI FIT DA LP08
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			do {

				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
				// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

				double[] vetOut4 = processa(xp1, yp1, MIRD_vol24, MIRD_vol48, MIRD_vol120);

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
				s1 = vetOut4[18];
				s2 = vetOut4[19];
				m1 = vetOut4[20];
				m2 = vetOut4[21];


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

				// ==========================================================================
				// PARTE REVIEW CHE DEVE RITORNARE INDIETRO PER RIFARE UNO O PIU'DEI CALCOLI
				// FINALMENTE SAREMO FELICI E GORGOGLIONI DELLE NOSTRE ELABORAZIONI
				// ==========================================================================
//				decis1 = MIRD_display_LP67(MIRD_vol24, MIRD_vol48, MIRD_vol120, uptake, massa, tmezzo, dose);
				decis1 = MIRD_display_LP68(vetInput); // accetta risultati o ripeti analisi
				if (decis1 == 0)
					return;
				// boolean fit = false;
				if (decis1 == 1) {
					rip = dialogRipetizione_LP08(); // SCELTA PROBLEMA: CONTORNAMENTO O FIT
					if (rip == 0)
						return;
				}
				Utility.closePlot("PLOT FLANAGAN");
				Utility.closePlot("PLOT IMAGEJ");

				/// ritorno da selezione 2 o 3 punti
			} while (rip < 2 && decis1 < 2);

			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// QUI ENTRO PER PROBLEMI DI CONTORNAMENTO DA LP08
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			if (rip == 2) {
				selection = dialogReview_LP32();
				String aux1 = "";
				switch (selection) {
				case "24h":
					scelta = 1;
					aux1 = "#901#\tok24= false";
					Utility.logModify(pathPermanente, "#901#", aux1);
					aux1 = "#904#\tokk= false";
					Utility.logModify(pathPermanente, "#904#", aux1);
					break;
				case "48h":
					scelta = 2;
					aux1 = "#902#\tok48= false";
					Utility.logModify(pathPermanente, "#902#", aux1);
					aux1 = "#904#\tokk= false";
					Utility.logModify(pathPermanente, "#904#", aux1);
					break;
				case "120h":
					scelta = 3;
					aux1 = "#903#\tok120= false";
					Utility.logModify(pathPermanente, "#903#", aux1);
					aux1 = "#904#\tokk= false";
					Utility.logModify(pathPermanente, "#904#", aux1);
					break;
				}
			}

		} while (decis1 != 2);
		
		

		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

		// ================= POSTSCRITTURA ===========================================
		// UNA VOLTA CHE L'OPERATORE HA DETTO SI, SCRIVIAMO TUTTA LA MONNEZZA IN
		// VOLATILE, IN ATTESA DI BATTEZZARE LA LESIONE
		// ============================================================================

		if (Double.isNaN(SmAtilde))
			flanagan = false;
		else
			flanagan = true;

		int count5 = 200;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---- MIRD CALCULATION 24h ----";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol24= " + out24[0];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal24= " + out24[1];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv24= " + out24[2];
		Utility.logAppend(pathVolatile, aux5);
		count5 = 220;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---- MIRD CALCULATION 48h ----";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol48= " + out48[0];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal48= " + out48[1];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv48= " + out48[2];
		Utility.logAppend(pathVolatile, aux5);
		count5 = 240;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---- MIRD CALCULATION 120h ----";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol120= " + out120[0];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal120= " + out120[1];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv120= " + out120[2];
		Utility.logAppend(pathVolatile, aux5);

		if (!flanagan) {
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// CON IMAGEJ E BASTA
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

			count5 = 260;
			aux5 = "#" + String.format("%03d", count5++) + "#\t----- MIRD FIT RESULTS IMAGEJ --------";
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD IJ FIT param 0= " + AA;
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD IJ FIT param 1= " + aa;
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FIT R^2= " + Rsquared;
			Utility.logAppend(pathVolatile, aux5);
		} else {
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// CON FLANAGAN E BASTA
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			count5 = 270;
			aux5 = "#" + String.format("%03d", count5++) + "#\t----- MIRD FIT RESULTS FLANAGAN --------";
			Utility.logAppend(pathVolatile, aux5);

			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FLANAGAN FIT param 0= " + AA;
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FLANAGAN FIT param 1= " + aa;
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FIT R^2= " + Rsquared;
			Utility.logAppend(pathVolatile, aux5);
		}

		count5 = 300;
		if (flanagan) {
			aux5 = "#" + String.format("%03d", count5++) + "#\t---TRE PUNTI SELEZIONATI ELABORATI CON FLANAGAN------";
			Utility.logAppend(pathVolatile, aux5);
		} else {
			aux5 = "#" + String.format("%03d", count5++) + "#\t---DUE PUNTI SELEZIONATI ELABORATI CON IMAGEJ -------";
			Utility.logAppend(pathVolatile, aux5);
		}
		aux5 = "#" + String.format("%03d", count5++) + "#\tparametro A= " + AA;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tparametro a= " + aa;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tmAtilde= " + mAtilde;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tdisintegrazioni= " + disintegrazioni;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tuptake[%]= " + uptake;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tmassa= " + massa;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\ttmezzo= " + tmezzo;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\ttau= " + tau;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tdose= " + dose;
		Utility.logAppend(pathVolatile, aux5);

		// if (count3 == 3) { /// lo eseguo sempre
		aux5 = "#" + String.format("%03d", count5++) + "#\t--------- CALCOLO ERRORI ----------";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\terrore SA= " + SA;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\terrore Sa= " + Sa;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSmAtilde= " + SmAtilde;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tS# disintegrazioni= " + Sdisintegrazioni;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSuptake= " + Suptake;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSmassa= " + Smassa;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tStmezzo= " + Stmezzo;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tStau= " + Stau;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSdose= " + Sdose;
		Utility.logAppend(pathVolatile, aux5);

		count5 = 500;
		aux5 = "#" + String.format("%03d", count5++) + "#\t-------- CALCOLO DOSE -----------";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s1= " + s1;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s2= " + s2;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m1= " + m1;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m2= " + m2;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose massa= " + massa;
		Utility.logAppend(pathVolatile, aux5);

		// ==============================================================
		// BATTESIMO DELLA LESIONE
		// ==============================================================
		
		Utility.logDedupe(pathVolatile);   // ELIMINAZIONE DOPPIONI
		
		Utility.dialogBattezzaLesioni_LP27(pathVolatile);
		Utility.chiudiTutto();
		IJ.showMessage("FINE LAVORO");
	}

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
	 * Cancellazione directory
	 * 
	 * @param deleteFolder
	 */
	private void deleteFolderFiles(File deleteFolder) {

		if (Utility.checkDir(deleteFolder)) {
			File[] arrayOfFile = deleteFolder.listFiles();
			for (File file : arrayOfFile) {
				if (file.isDirectory())
					deleteFolderFiles(file);
				file.delete();
			}
		}
	}

	/**
	 * Apre una immagine dal path
	 * 
	 * @param path
	 * @return
	 */
	public ImagePlus openImage(String path) {

		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(path);
		if (imp == null) {
			Utility.debugDeiPoveri("Immagine " + path + " inesistente o non visualizzabile");
			return null;
		}
		return imp;
	}

	/**
	 * Legge le immagini da una cartella e le inserisce in uno stack. Copiato da
	 * https://github.com/ilan/fijiPlugins (Ilan Tal) Class: Read_CD. Ho disattivato
	 * alcune parti di codice riguardanti tipi di immagini di cui non disponiamo
	 * 
	 * @param myPath
	 * @return ImagePlus (stack)
	 */

	ImagePlus readStackFiles(String myPath) {
		int j, k, n0, width = -1, height = 0, depth = 0, samplePerPixel = 0;
//		int n;
		int bad = 0, fails = 0;
		Opener opener;
		ImagePlus imp, imp2 = null;
		ImageStack stack;
		Calibration cal = null;
		double min, max, progVal;
		FileInfo fi = null;
		String flName, info, label1, tmp;
//		String parName;
//		String[] frameText = null;
//		ArrayList<ImagePlus> imgList = null;
		String mytitle = "";
		// BI_dbSaveInfo curr1 = null;

		info = null;
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		stack = null;
//		parName = currRow.flName.getParent();
		File flPath = new File(myPath);
		File checkEmpty;
		File[] results = flPath.listFiles();
		// CONTROLLO CHE IL FILE PIACCIA AD IMAGEJ

		for (int i1 = 0; i1 < results.length; i1++) {
			flName = results[i1].getPath();
			isDicomImage(flName);
		}

		n0 = results.length;

		for (j = 1; j <= n0; j++) {
			progVal = ((double) j) / n0;
			IJ.showStatus(j + "/" + n0);
			IJ.showProgress(progVal);
			opener = new Opener();
			flName = results[j - 1].getPath();
			checkEmpty = new File(flName); // remember for possible dicomdir
			if (checkEmpty.length() == 0)
				continue;
			tmp = results[j - 1].getName();
			if (tmp.equalsIgnoreCase("dirfile"))
				continue;
			k = opener.getFileType(flName);
			opener.setSilentMode(true);
			imp = opener.openImage(flName);
			if (imp == null) {
				fails++;
				if (fails > 2) {
					IJ.showProgress(1.0);
					return null;
				}
				continue;
			}
			info = (String) imp.getProperty("Info");
			mytitle = imp.getTitle();

			k = Utility.parseInt(DicomTools.getTag(imp, "0028,0002"));
			if (stack == null) {
				samplePerPixel = k;
				width = imp.getWidth();
				height = imp.getHeight();
				depth = imp.getStackSize();
				cal = imp.getCalibration();
				fi = imp.getOriginalFileInfo();
				ColorModel cm = imp.getProcessor().getColorModel();
				stack = new ImageStack(width, height, cm);
			}
			if ((depth > 1 && n0 > 1) || width != imp.getWidth() || height != imp.getHeight() || k != samplePerPixel) {
				if (k <= 0)
					continue;
				stack = null;
				depth = 0;
				continue;
			}
			label1 = null;
			if (depth == 1) {
				label1 = imp.getTitle();
				if (info != null)
					label1 += "\n" + info;
			}
			ImageStack inputStack = imp.getStack();
			for (int slice = 1; slice <= inputStack.getSize(); slice++) {
				ImageProcessor ip = inputStack.getProcessor(slice);
				if (ip.getMin() < min)
					min = ip.getMin();
				if (ip.getMax() > max)
					max = ip.getMax();
				stack.addSlice(label1, ip);
			}
		}

		if (stack != null && stack.getSize() > 0) {
			// stack = ChoosePetCt.mySort(stack);
			if (fi != null) {
				fi.fileFormat = FileInfo.UNKNOWN;
				fi.fileName = "";
				fi.directory = "";
			}
			imp2 = new ImagePlus(mytitle, stack);
			imp2.getProcessor().setMinAndMax(min, max);
			if (n0 == 1 + bad || depth > 1)
				imp2.setProperty("Info", info);
			if (fi != null)
				imp2.setFileInfo(fi);
			double voxelDepth = DicomTools.getVoxelDepth(stack);
			if (voxelDepth > 0.0 && cal != null)
				cal.pixelDepth = voxelDepth;
			imp2.setCalibration(cal);
		}
		IJ.showProgress(1.0);
		return imp2;
	}

	/***
	 * Testa se fileName1 e' un file dicom ed e' un immagine visualizzabile da
	 * ImageJ, eventualmente scrive a log nome file e tipo di errore
	 * 
	 * @param fileName1
	 * @return boolean
	 */
	public boolean isDicomImage(String fileName1) {
		boolean ok = true;
		String info = new DICOM().getInfo(fileName1);
		if (info == null || info.length() == 0) {
			MyLog.log("il file " + fileName1 + " risulta INDIGESTO ad ImageJ >>> NOT DICOM INFO");
			ok = false;
		} else if (!info.contains("7FE0,0010")) {
			MyLog.log("il file " + fileName1 + " risulta INDIGESTO ad ImageJ >>> NOT IMAGE");
			ok = false;
		}
		return ok;
	}

	/**
	 * Legge i dati header
	 * 
	 * @param slice
	 * @param img1
	 * @return
	 */
	static String getMeta(int slice, ImagePlus img1) {
		// first check that the user hasn't closed the study
		if (img1.getImage() == null)
			return null;
		String meta = img1.getStack().getSliceLabel(slice);
		// meta will be null for SPECT studies
		if (meta == null)
			meta = (String) img1.getProperty("Info");
		return meta;
	}

	/**
	 * Tentativo di partorire la laboriosa stringa usata come titolo del PetCtViewer
	 * 
	 * @param meta header dicom della immagine o dello stack
	 * @return parto della stringa
	 */
	static String stringaLaboriosa(String meta) {

		m_patName = compressPatName(getDicomValue(meta, "0010,0010"));
		String aux1 = "#600#\tm_patname= " + m_patName;
		Utility.logAppend(pathPermanente, aux1);
		String tmp = getDicomValue(meta, "0010,0030");
		m_patBirthday = getDateTime(tmp, null);
		if (m_patBirthday != null) {
			long sdyTime, birthTime, currDiff;
			Integer years;
			int type2 = -1;
			Date studyDateTime = getStudyDateTime(meta, type2);
			sdyTime = studyDateTime.getTime();
			birthTime = m_patBirthday.getTime();
			currDiff = (sdyTime - birthTime) / (24 * 60 * 60 * 1000); // number of days
			years = (int) (currDiff / 365.242199);
			m_patYears = years.toString() + "y";
		}
		m_patID = compressID(getDicomValue(meta, "0010,0020"));
		int type3 = -1;
		Date studyDateTime1 = getStudyDateTime(meta, type3);
		m_serDate = UsaDateFormat(studyDateTime1);
		m_styName = getDicomValue(meta, "0008,1030");
		petSeriesName = getDicomValue(meta, "0008,103E");
		String laboriosa = "Pet-Ct: " + m_patName + "   " + m_patYears + "   " + m_patID + "   " + m_serDate + "   "
				+ m_styName + "   " + petSeriesName.toLowerCase();

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
	 * Dialogo inserimento dati iniezione
	 * 
	 * @return
	 */
	String[] dialogInputDatiSomministrazione_LP04() {

		MyLog.log("dialogInputDatiSomministrazione_LP04");
		String[] out1 = new String[3];
		String data0;
		String ora0;
		Double activity0;
		String activity1;

		MyLog.log("LP04 start");
		GenericDialog gd11 = new GenericDialog("LP04 - Date/Time/Activity");
		gd11.addMessage("Introduci i seguenti dati per il nuovo paziente", titleFont);
		gd11.setFont(defaultFont);
		String label11 = "Data [dd-mm-yyyy]";
		String format11 = "dd-mm-yyyy";
		int digits11 = 8;
		gd11.addStringField(label11, format11, digits11);
		String label12 = "Ora [HH:mm:ss]";
		String format12 = "HH:mm:ss";
		int digits12 = 8;
		gd11.addStringField(label12, format12, digits12);

		String label13 = "Attivita' somministrata";
		double default13 = 0.00;
		int digits13 = 8;
		gd11.addNumericField(label13, default13, digits13, 10, "[MBq]");

		gd11.setCancelLabel("Cancel");
		gd11.showDialog();
		if (gd11.wasCanceled()) {
			MyLog.log("LP04 null Cancel");
			return null;
		}

		data0 = gd11.getNextString();
		boolean ok1 = Utility.isValidDate(data0, format11);
		if (!ok1) {
			MyLog.log("LP04 null Data sbagliata");
			return null;
		}

		ora0 = gd11.getNextString();
		boolean ok2 = Utility.isValidTime(ora0, format12);
		if (!ok2) {
			MyLog.log("LP04 null Ora sbagliata");
			return null;
		}

		activity0 = gd11.getNextNumber();
		if (activity0 == 0)
			return null;
		activity1 = "" + activity0;
		out1[0] = data0;
		out1[1] = ora0;
		out1[2] = activity1;
		MyLog.log("LP04 end");
		return out1;
	}

	boolean dialogDistretto_LP07() {

		MyLog.log("dialogo LP07");
		GenericDialog genericDialog3 = new GenericDialog("LP07 - ALTRO DISTRETTO");
		genericDialog3.setFont(defaultFont);
		genericDialog3.addMessage("Posizione lesione", titleFont);
		genericDialog3.addMessage("La lesione si trova in questo o altro distretto?");
		genericDialog3.setOKLabel("STESSO DISTRETTO");
		genericDialog3.setCancelLabel("ALTRO DISTRETTO");
		genericDialog3.showDialog();
		if (genericDialog3.wasCanceled()) {
			MyLog.log("LP07 - true STESSO DISTRETTO");
			return true;
		} else {
			MyLog.log("LP07 - false ALTRO DISTRETTO");
			return false;
		}
	}

	static int dialogRipetizione_LP08() {

		MyLog.log("dialogo LP08");
		GenericDialog genericDialog3 = new GenericDialog("LP08 - COSA RIPETERE");
		genericDialog3.setFont(defaultFont);
		genericDialog3.addMessage("Scelta tipo di problema:", titleFont);
		genericDialog3.enableYesNoCancel("CONTORNAMENTO", "FIT");

		genericDialog3.setCancelLabel("Cancel");
		genericDialog3.showDialog();

		if (genericDialog3.wasCanceled()) {
			MyLog.log("LP08 0 Cancel");
			return 0;
		} else if (genericDialog3.wasOKed()) {
			MyLog.log("LP08 2 CONTORNAMENTO");
			return 2;
		} else {
			MyLog.log("LP08 1 FIT");
			return 1;
		}
	}

	/**
	 * Dialogo conferma dati iniezione
	 * 
	 * @param in1
	 * @return
	 */
	boolean dialogConfermaDatiSomministrazione_LP10(String[] in1) {

		MyLog.log("dialogConfermaDatiSomministrazione_LP10");
		String data11 = in1[0];
		String ora11 = in1[1];
		String activity11 = in1[2];

		GenericDialog conf11 = new GenericDialog("LP10 - CONFERMA DATI INSERITI");

		conf11.addMessage("CONFERMA DATI SOMMINISTRAZIONE", titleFont);
		conf11.setFont(defaultFont);
		conf11.addMessage("Data " + data11 + "   [dd-mm-yyyy]");
		conf11.addMessage("Ora " + ora11 + "   [hh:mm:ss]");
		conf11.addMessage("Attivita' introdotta " + activity11 + "   [MBq]");
		conf11.showDialog();

		if (conf11.wasOKed()) {
			MyLog.log("LP10 - true PREMUTO OK");
			return true;
		} else {
			MyLog.log("LP00 - true PREMUTO Cancel");
			return false;
		}
	}

	/**
	 * Dialogo con dati delle cartelle 24/48/120
	 * 
	 * @param aList
	 */
	boolean dialogReview_LP05(ArrayList<ArrayList<String>> aList) {

		MyLog.log("dialogReview_LP05");
		GenericDialog reviewDialog = new GenericDialog("LP05 - Review Dicom Tags");
		reviewDialog.addMessage("Check the Dicom tags", titleFont);
		reviewDialog.setFont(defaultFont);
		reviewDialog.addMessage("Please review if the acquisition settings used are correct.");
		for (int a4 = 0; a4 < aList.size(); a4++) {
			reviewDialog.addMessage(arrayOfString[a4] + " folder path:");
			String str22 = "";
			ArrayList<String> cList = aList.get(a4);
			for (int b4 = 0; b4 < cList.size(); b4++) {
				String str9 = cList.get(b4);
				str22 = str22 + str9;
			}
			reviewDialog.addMessage(str22);
		}
		reviewDialog.showDialog();
		if (reviewDialog.wasCanceled()) {
			return false;
		} else {
			reviewDialog.dispose();
			return true;
		}

	}

	/**
	 * Dialogo che mostra dati immagini del dosimetryFolder
	 * 
	 * @return
	 */
	boolean dialogInitialize_LP00() {

		MyLog.log("dialogInitialize_LP00");
		GenericDialog genericDialog3 = new GenericDialog("LP00 - INIZIALIZZA PER NUOVO PAZIENTE");
		genericDialog3.addMessage("Inizializza per nuovo paziente", titleFont);
		genericDialog3.setFont(defaultFont);
		genericDialog3.addMessage("File PERMANENTE salvataggio dati", defaultFont);
		genericDialog3.setOKLabel("MANTIENI");
		genericDialog3.setCancelLabel("INIZIALIZZA");
		genericDialog3.showDialog();
		if (genericDialog3.wasCanceled()) {
			MyLog.log("LP00 - true PREMUTO INIZIALIZZA");
			return true; // EBBENE SI, DA BENE COSI'
		} else {
			MyLog.log("LP00 - false PREMUTO MANTIENI");
			return false;
		}
	}

	/**
	 * Dialogo cancellazione immagini e dati paziente precedente
	 * 
	 * @param str20
	 * @return
	 */
	boolean dialogNonBlockingDelete_LP01(String str20) {

		MyLog.log("dialogNonBlockingDelete_LP01");
		NonBlockingGenericDialog nonBlockingGenericDialog = new NonBlockingGenericDialog("LP01 - Command Confirmation");
		nonBlockingGenericDialog.addMessage("Confirmation Dialog", titleFont);
		nonBlockingGenericDialog.setFont(defaultFont);
		nonBlockingGenericDialog.addMessage(
				"Are you sure to delete all files in the following folder?\nThis action is irreversible.\n" + str20);
		nonBlockingGenericDialog.setCancelLabel("ANNULLA");
		nonBlockingGenericDialog.showDialog();
		if (nonBlockingGenericDialog.wasCanceled()) {
			MyLog.log("LP01 - false PREMUTO ANNULLA");
			return false;
		} else {
			MyLog.log("LP01 - true PREMUTO OK");
			return true;
		}
	}

	/**
	 * Dialo selezione cartella 24h nuovo paziente
	 * 
	 * @return
	 */
	boolean dialogSelection_LP02() {

		MyLog.log("dialogSelection_LP02");
		GenericDialog genericDialog = new GenericDialog("LP02 - Select images Folder");
		genericDialog.addMessage("24h Folder Selection", titleFont);
		genericDialog.setFont(defaultFont);
		genericDialog.addMessage("Select folder of the 24h acquisition");
		genericDialog.setOKLabel("BROWSE");
		genericDialog.setCancelLabel("Cancel");
		genericDialog.showDialog();
		if (genericDialog.wasCanceled()) {
			MyLog.log("LP02 - false PREMUTO Cancel");
			return false;
		} else {
			MyLog.log("LP02 - true PREMUTO BROWSE");
			genericDialog.dispose();
			return true;
		}
	}

	/**
	 * Selezione cartella immagini
	 * 
	 * @return
	 */
	String directorySelection_LP_20() {

		MyLog.log("directorySelection_LP_20");
		DirectoryChooser directoryChooser = new DirectoryChooser("LP20 Directory Selection");
		String str3 = directoryChooser.getDirectory();
		if (str3 == null) {
			Utility.dialogErrorMessage_LP06("Wrong selection. Please try again.");
			MyLog.log("LP20 - null  ERROR MESSAGE Wrong selection. Please try again ");
			return null;
		} else {
			MyLog.log("LP20 - selezione effettuata");
		}
		return str3;
	}

	/**
	 * Conferma cartelle selezionate
	 * 
	 * @param str24
	 * @param str48
	 * @param str120
	 * @return
	 */
	boolean dialogConfirmFolder_LP03(String str24, String str48, String str120) {

		MyLog.log("dialogConfirmFolder_LP03");
		GenericDialog genericDialog1 = new GenericDialog("LP03 - Confirm images Folder");
		genericDialog1.addMessage("Confirm Image Selection", titleFont);
		genericDialog1.addMessage("Check Image Folder Auto-Selection", defaultFont);
		genericDialog1.addMessage("24h folder path:", textFont);
		genericDialog1.addMessage(str24, defaultFont);
		genericDialog1.addMessage("48h folder path:", textFont);
		genericDialog1.addMessage(str48, defaultFont);
		genericDialog1.addMessage("120h folder path:", textFont);
		genericDialog1.addMessage(str120, defaultFont);
		genericDialog1.setOKLabel("CONFIRM");
		genericDialog1.setCancelLabel("Cancel");
		genericDialog1.showDialog();
		if (genericDialog1.wasCanceled()) {
			MyLog.log("LP03 - false PREMUTO Cancel");
			return false;
		} else {
			MyLog.log("LP03 - true PREMUTO CONFIRM");
			genericDialog1.dispose();
			return true;
		}

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
		Utility.logInit(pathVolatile);
		Utility.logAppend(pathVolatile, "INITIALIZED " + dateformat.format(now));
		File f1 = new File(pathPermanente);
		if (init || !f1.exists()) {
			Utility.logInit(pathPermanente);
			Utility.logAppend(pathPermanente, "INITIALIZED " + dateformat.format(now));
		} else {
			Utility.logAppend(pathPermanente, "PRESERVED " + dateformat.format(now));
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
	void copyFilesToDesktopDosimetryFolder(File[] arrayOfFile1, File[] arrayOfFile2, boolean[] arrayOfBoolean,
			String desktopImagesSubfolderPath) {
		// ----------------------------------------
		// Copia delle immagini dalla sorgente al DosimetryFolder situato sul desktop
		// ----------------------------------------

		int len1 = 0;
		int count1 = 0;

		for (int b2 = 0; b2 < arrayOfFile1.length; b2++) {
			if (arrayOfBoolean[b2]) {
				File file = arrayOfFile1[b2];
				File[] arrayOfFile = file.listFiles();
				len1 = len1 + arrayOfFile.length;
			}
		}

		for (int b2 = 0; b2 < arrayOfFile1.length; b2++) {
			if (arrayOfBoolean[b2]) {
				File file = arrayOfFile1[b2];
				File[] arrayOfFile = file.listFiles();
				for (File file5 : arrayOfFile) {
					count1++;
					IJ.showStatus(count1 + "/" + len1);
					File file6 = null;
					if (file5.getName().contains("IRAC")) {
						file6 = new File(desktopImagesSubfolderPath + File.separator + arrayOfString[b2]
								+ File.separator + "SPECT" + File.separator + file5.getName());
						try {
							copyDirectory(file5, file6);
						} catch (Exception exception) {
							Utility.dialogErrorMessage_LP06(
									"An Error occurred while coping the SPECT image. Please try again!");
							return;
						}
					} else if (file5.getName().contains("CTAC")) {
						file6 = new File(desktopImagesSubfolderPath + File.separator + arrayOfString[b2]
								+ File.separator + "CT" + File.separator + file5.getName());
						try {
							copyDirectory(file5, file6);
						} catch (Exception exception) {
							Utility.dialogErrorMessage_LP06(
									"An Error occurred while coping CT images. Please try again!");
							return;
						}
					} else if (file5.getName().contains("EM001")) {
						arrayOfFile2[b2] = file5;
					}
				}
			} else {
				Utility.dialogErrorMessage_LP06(
						"It was not possible to import files for " + arrayOfString[b2] + " folder.");
			}

		}

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
		ImagePlus imp2 = openImage(path2);
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
	 * Presenza immagini nel dosimetry folder all'avvio
	 * 
	 * @param str20
	 * @return
	 */
	boolean dialogImmaginiPazientePrecedente_LP21_OLD(String[] str20) {

		MyLog.log("dialogImmaginiPazientePrecedente_LP21");
		NonBlockingGenericDialog nonBlockingGenericDialog = new NonBlockingGenericDialog(
				"LP21 - Immagini paziente precedente");
		nonBlockingGenericDialog.addMessage("Presenza immagini paziente precedente", titleFont);
		nonBlockingGenericDialog.setFont(defaultFont);
		nonBlockingGenericDialog.addMessage(
				"Attenzione: in DosimetryFolder sul Desktop ci sono le immagini \n" + str20[0] + " di " + str20[1],
				defaultFont);
		nonBlockingGenericDialog.setCancelLabel("CONTINUA CON ALTRE LESIONI");
		nonBlockingGenericDialog.setOKLabel("PASSA A NUOVO PAZIENTE");
		nonBlockingGenericDialog.showDialog();
		if (nonBlockingGenericDialog.wasCanceled()) {
			MyLog.log("LP21 false CONTINUA CON ALTRE LESIONI");
			return false;
		} else {
			MyLog.log("LP21 true PASSA A NUOVO PAZIENTE");
			return true;
		}
	}

	/**
	 * Presenza immagini nel dosimetry folder all'avvio Testato, esegue cancel &
	 * cross
	 * 
	 * @param str20
	 * @return
	 */
	int dialogImmaginiPazientePrecedente_LP21(String[] str20) {

		MyLog.log("dialogImmaginiPazientePrecedente_LP21");
		NonBlockingGenericDialog nonBlockingGenericDialog = new NonBlockingGenericDialog(
				"LP21 - Presenza immagini paziente precedente");
		nonBlockingGenericDialog.addMessage("Presenza immagini paziente precedente", titleFont);
		nonBlockingGenericDialog.setFont(defaultFont);
		nonBlockingGenericDialog.addMessage(
				"Attenzione: in DosimetryFolder sul Desktop ci sono le immagini \n" + str20[0] + " di " + str20[1]);
		nonBlockingGenericDialog.enableYesNoCancel("PASSA A NUOVO PAZIENTE", "CONTINUA CON ALTRE LESIONI");
//		nonBlockingGenericDialog.setCancelLabel("");
//		nonBlockingGenericDialog.setOKLabel("PASSA A NUOVO PAZIENTE");
		nonBlockingGenericDialog.showDialog();
		if (nonBlockingGenericDialog.wasCanceled()) {
			MyLog.log("LP21 0 Cancel");
			return 0;
		} else if (nonBlockingGenericDialog.wasOKed()) {
			MyLog.log("LP21 2 PASSA A NUOVO PAZIENTE");
			return 2;
		} else {
			MyLog.log("LP21 1 CONTINUA CON ALTRE LESIONI");
			return 1;
		}
	}

	/**
	 * Cancellazione e creazione dosimetry folder e images folder, loro riempimento
	 * 
	 * @return
	 */
	public File[] desktopImagesFolderFill() {

		String strDir24h = null;
		String strDir48h = null;
		String strDir120h = null;

		// chiede di identificare la cartella 24h sorgente
		do {
			boolean ok = dialogSelection_LP02();
			if (!ok)
				return null;
			strDir24h = directorySelection_LP_20(); // nome directory 24h
		} while (strDir24h == null);
		// costruisce i due path 48h e 120h
		strDir48h = strDir24h.replace(arrayOfString[0], arrayOfString[1]); // nome directory 48h
		strDir120h = strDir24h.replace(arrayOfString[0], arrayOfString[2]); // nome directory 120h
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

		boolean ok = dialogConfirmFolder_LP03(strDir24h, strDir48h, strDir120h);
		if (ok) {

			File file1 = new File(desktopImagesSubfolderPath);
			// cancella cartella anco se piena
			Utility.deleteDirectory(file1);

			// crea le cartelle destinazione
			for (int b1 = 0; b1 < arrayOfString.length; b1++) {

				File file5 = new File(desktopImagesSubfolderPath + File.separator + arrayOfString[b1]);
				if (file5.mkdirs()) {
				} else {
					MyLog.log("cartella non creata= " + file5.toString());
				}

				File file6 = new File(
						desktopImagesSubfolderPath + File.separator + arrayOfString[b1] + File.separator + "CT");
				if (file6.mkdirs()) {
				} else {
					MyLog.log("cartella non creata= " + file6.toString());
				}

				File file7 = new File(
						desktopImagesSubfolderPath + File.separator + arrayOfString[b1] + File.separator + "SPECT");
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

		copyFilesToDesktopDosimetryFolder(arrayOfFile1, arrayOfFile2, arrayOfBoolean, desktopImagesSubfolderPath);
		return arrayOfFile2;

	}

	/**
	 * Trasformazione input Data da tastiera in formato dicom
	 * 
	 * @param data0
	 * @return
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
	 * @param ora0
	 * @return
	 */
	public String oraToDicom(String ora0) {

		String ora = ora0.substring(0, 2);
		String min = ora0.substring(3, 5);
		String sec = ora0.substring(6, 8);
		String ora1 = ora + min + sec;

		return ora1;
	}

	/**
	 * Lettura dati dalle immagini per NUOVO PAZIENTE, scrittura in permanente.txt
	 * 
	 * @param vetFile immagini da analizzare
	 * @param myDate0 dataOra somministrazione per calcolo deltaT
	 */
	public void raccoltaDati(File[] vetFile, Date myDate0) {

		ArrayList<ArrayList<String>> aList = new ArrayList<ArrayList<String>>();
		ArrayList<Long> eList = new ArrayList<Long>();
		for (byte b3 = 0; b3 < arrayOfString.length; b3++) {
			ImagePlus imp8 = IJ.openImage(vetFile[b3].getAbsolutePath());
			int durata = Utility.MIRD_calcoloDurataAcquisizione(imp8);
			String acqDate = DicomTools.getTag(imp8, "0008,0022").trim();
			String acqTime = DicomTools.getTag(imp8, "0008,0032").trim();
			// MyLog.log("getDateTime: date= " + acqDate + " time=" + acqTime);
			Date myDateTime1 = getDateTime(acqDate, acqTime);
			SimpleDateFormat sdf = new SimpleDateFormat(format1);
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
		// Nel file volatile vengono scritti dal seguente loop:
		// #031#,#032#,#033#,#034#,#035#,#036#,#037#,#038#,
		// #041#,#042#,#043#,#044#,#045#,#046#,#047#,#048#,
		// #051#,#052#,#053#,#054#,#055#,#056#,#057#,#058#,
		// --------------------------------------------------------------------------------------
		String aux1 = "";
		String aux2 = "";
		String aux3 = "";
		int count2 = 0;
		for (int a4 = 0; a4 < aList.size(); a4++) {
			count2 = a4 * 20 + 10;
//			aux1 = "#010#\t---- IMAGE INFO " + arrayOfString[a4] + " ----";
			aux2 = "#" + String.format("%03d", count2++) + "#\t---- IMAGE INFO " + arrayOfString[a4] + " ----";
			Utility.logAppend(pathVolatile, aux2);
			String str22 = "";
			ArrayList<String> cList = aList.get(a4);
			for (int b4 = 0; b4 < cList.size(); b4++) {
				aux2 = "#" + String.format("%03d", count2++) + "#";
				String str9 = cList.get(b4);
				str22 = aux2 + str9;
				Utility.logAppend(pathVolatile, str22);
			}
			aux3 = "#" + String.format("%03d", count2++) + "#" + "\tDeltaT= "
					+ (double) eList.get(a4) / (1000 * 60 * 60);
			Utility.logAppend(pathVolatile, aux3);
		}
	}

	/**
	 * Dialogo non modale selezione immagini 24/48/120 Onora il Cancel
	 * 
	 * @return
	 */
	boolean dialogInstructions_LP30() {

		MyLog.log("dialogInstructions_LP30");
		Dimension screen = IJ.getScreenSize();
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP30 - ISTRUZIONI");
		gd1.setFont(defaultFont);
		gd1.addMessage("Trova le lesioni su PET-CT Viewer su tutte e tre le \nimmagini 24h, 48h e 120h, poi premi OK");
		gd1.setLocation(screen.width * 2 / 3, screen.height * 1 / 3);
		gd1.showDialog();

		if (gd1.wasCanceled()) {
			MyLog.log("LP30 - false PREMUTO CANCEL");
			return false;
		} else {
			MyLog.log("LP30 - true PREMUTO OK");
			return true;
		}
	}

	/**
	 * Dialogo non modale di selezione
	 * 
	 * @return
	 */
	String dialogReview_LP32() {

		MyLog.log("dialogReview_LP32");
		String[] items = { "24h", "48h", "120h" };
		int rows = 3;
		int columns = 1;
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP32 - REVIEW");
		gd1.setFont(defaultFont);
		gd1.addRadioButtonGroup("IMMAGINE DA RIANALIZZARE", items, rows, columns, "VA BENE COSI'");
		gd1.addMessage("SELEZIONARE E POI PREMERE OK");
		gd1.showDialog();
		MyLog.log("LP32 - true PREMUTO OK");
		String selection = gd1.getNextRadioButton();
		return selection;
	}

	/**
	 * Dialogo non modale di selezione
	 * 
	 * @return
	 */
	static boolean[] pointsSelection_LP33() {

		MyLog.log("dialogReview_LP33");
		String[] items = { "   24h", "   48h", "   120h" };
		boolean[] def = { true, true, true };
		int rows = 4;
		int columns = 1;
		int count = 0;
		boolean[] out1 = null;
		NonBlockingGenericDialog gd1 = null;
		do {
			gd1 = new NonBlockingGenericDialog("LP33 - SELECTION");
			gd1.addCheckboxGroup(rows, columns, items, def);
			gd1.addMessage("Selezionare ALMENO\ndue punti su cui\nfare il FIT", defaultFont);
			gd1.showDialog();
			out1 = new boolean[def.length];
			for (int i1 = 0; i1 < def.length; i1++) {
				out1[i1] = gd1.getNextBoolean();
				if (out1[i1] == true)
					count++;
			}
			if (count < 2)
				IJ.error("Dovevi selezionare ALMENO due punti,\nRIPROVACI ..... BYE-BYE!");
		} while (count < 2);
		if (gd1.wasCanceled()) {
			MyLog.log("LP33 - false PREMUTO CANCEL");
			return null;
		} else {
			MyLog.log("LP33 - true PREMUTO OK");
			return out1;
		}
	}

	/**
	 * Dialogo non modale di selezione
	 * 
	 * @return
	 */
	boolean dialogSelection_LP31() {

		MyLog.log("dialogSelection_LP31");
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP31 - START");
		gd1.setFont(defaultFont);
		gd1.addMessage("PREMERE OK");
		gd1.showDialog();
		MyLog.log("LP31 - true PREMUTO OK");
		return true;
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
		Utility.logModify(path, "#901#", aux1);
		aux1 = "#902#\tokk= false";
		Utility.logModify(path, "#902#", aux1);
		aux1 = "#903#\tokk= false";
		Utility.logModify(path, "#903#", aux1);
		aux1 = "#904#\tokk= false";
		Utility.logModify(path, "#904#", aux1);

	}

	/**
	 * Mostra i volumi 24h 48h e 120h
	 * 
	 * @param vol24
	 * @param vol48
	 * @param vol120
	 */
	void MIRD_display_LP66(double vol24, double vol48, double vol120) {

		double media = (vol24 + vol48 + vol120) / 3.0;
		double per24 = ((vol24 - media) * 100) / media;
		double per48 = ((vol48 - media) * 100) / media;
		double per120 = ((vol120 - media) * 100) / media;
		String aux24 = "Volume24 h= " + String.format("%.2f", vol24) + " cm3    (" + String.format("%+,.1f%%", per24)
				+ " rispetto a media)";
		String aux48 = "Volume48 h= " + String.format("%.2f", vol48) + " cm3    (" + String.format("%+,.1f%%", per48)
				+ " rispetto a media)";
		String aux120 = "Volume120 h= " + String.format("%.2f", vol120) + " cm3    ("
				+ String.format("%+,.1f%%", per120) + " rispetto a media)";

		MyLog.log("MIRD_display_LP66");
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP66 - VOLUMI CALCOLATI");
		gd1.setFont(defaultFont);

		gd1.addMessage(aux24);
		gd1.addMessage(aux48);
		gd1.addMessage(aux120);

		gd1.showDialog();
		MyLog.log("LP66 - true PREMUTO OK");
		return;
	}

	/**
	 * Mostra i risultati ottenuti
	 * 
	 * @param vol24
	 * @param vol48
	 * @param vol120
	 */
	static int MIRD_display_LP67(double vol24, double vol48, double vol120, double uptake, double massa, double tmezzo,
			double dose) {

		String[] items = { "24h", "48h", "120h" };
		String aux1 = "";
		MyLog.log("MIRD_display_LP67");
		GenericDialog gd1 = new GenericDialog("LP67 - RISULTATI");
		gd1.addMessage("RISULTATI  OTTENUTI", titleFont);
		gd1.setFont(defaultFont);

		// (" + String.format("%+,.1f%%", per24)

		double errr = 8.34567;

		aux1 = "uptake%= " + String.format("%.2f%%", uptake) + "    \u00B1 " + String.format("%.2f%%", errr);
		gd1.addMessage(aux1);

		gd1.addMessage("vol24= " + String.format("%.2f", vol24) + " cm3    vol48= " + String.format("%.2f", vol48)
				+ " cm3    vol120= " + String.format("%.2f", vol120) + " cm3");
		aux1 = "massa= " + String.format("%.2f", massa) + " g";
		gd1.addMessage(aux1);
		aux1 = "t1/2= " + String.format("%.2f", tmezzo) + " ore" + String.format("%.2f", tmezzo / 24) + " giorni";
		gd1.addMessage(aux1);
		aux1 = "dose= " + String.format("%.2f", dose) + " Gy";
		gd1.addMessage(aux1);
		gd1.enableYesNoCancel("ACCETTA RISULTATI", "RIPETI ANALISI");

		gd1.showDialog();

		if (gd1.wasCanceled()) {
			MyLog.log("LP67= 0 Cancel");
			return 0;
		} else if (gd1.wasOKed()) {
			MyLog.log("LP67= 2 ACCETTA RISULTATI");
			return 2;
		} else {
			MyLog.log("LP67= 1 RIPETI CONTORNATURA");
			return 1;
		}
	}

	/**
	 * Mostra i risultati ottenuti
	 * 
	 * @param vol24
	 * @param vol48
	 * @param vol120
	 */
	static int MIRD_display_LP68(double[] vetInput) {

		double vol24 = vetInput[0];
		double vol48 = vetInput[1];
		double vol120 = vetInput[2];
		double uptake = vetInput[3];
		double massa = vetInput[4];
		double tmezzo = vetInput[5];
		double dose = vetInput[6];
//		double Svol24 = vetInput[7];
//		double Svol48 = vetInput[8];
//		double Svol120 = vetInput[9];
		double Suptake = vetInput[10];
		double Smassa = vetInput[11];
		double Stmezzo = vetInput[12];
		double Sdose = vetInput[13];

		// String[] items = { "24h", "48h", "120h" };
		String aux1 = "";

		MyLog.log("MIRD_display_LP68");
		GenericDialog gd1 = new GenericDialog("LP68 - RISULTATI");
		gd1.addMessage("RISULTATI  OTTENUTI", titleFont);
		gd1.setFont(defaultFont);

		// (" + String.format("%+,.1f%%", per24)

		aux1 = "uptake%= " + String.format("%.2f", uptake * 100) + " \u00B1 " + String.format("%.2f%%", Suptake * 100);
		gd1.addMessage(aux1);

		gd1.addMessage("vol24= " + String.format("%.2f", vol24) + " cm3    vol48= " + String.format("%.2f", vol48)
				+ " cm3    vol120= " + String.format("%.2f", vol120) + " cm3");
		aux1 = "massa= " + String.format("%.2f", massa) + " \u00B1 " + String.format("%.2f", Smassa) + " g";
		gd1.addMessage(aux1);
		aux1 = "t1/2= " + String.format("%.2f", tmezzo) + " \u00B1 " + String.format("%.2f", Stmezzo) + " ore ...... "
				+ String.format("%.2f", tmezzo / 24) + " \u00B1 " + String.format("%.2f", Stmezzo / 24) + " giorni";
		gd1.addMessage(aux1);
		aux1 = "dose= " + String.format("%.2f", dose) + " \u00B1 " + String.format("%.2f", Sdose) + " Gy";
		gd1.addMessage(aux1);
		gd1.enableYesNoCancel("ACCETTA RISULTATI", "RIPETI ANALISI");

		gd1.showDialog();

		if (gd1.wasCanceled()) {
			MyLog.log("LP68= 0 Cancel");
			return 0;
		} else if (gd1.wasOKed()) {
			MyLog.log("LP68= 2 ACCETTA RISULTATI");
			return 2;
		} else {
			MyLog.log("LP68= 1 RIPETI CONTORNATURA");
			return 1;
		}
	}

	/**
	 * Tentativo di creare un dialogo con dati disposti su una griglia, in modo da
	 * occupare meno posto
	 * 
	 * @param vol24
	 * @param vol48
	 * @param vol120
	 */
	static int FUNNY_display_LP67(double vol24, double vol48, double vol120, double uptake, double massa, double tmezzo,
			double dose) {

		int gridWidth = 3;
		int gridHeight = 8;
		int gridSize = gridWidth * gridHeight;
		TextArea[] tf = new TextArea[gridSize];
		double[] value = new double[gridSize];

		value[0] = vol24;
		value[1] = vol48;
		value[2] = vol120;
		value[3] = uptake;
		value[4] = massa;
		value[5] = tmezzo;
		value[6] = dose;

//		Panel panel = new Panel();
//		panel.setLayout(new GridLayout(gridHeight, gridWidth));
//		for (int i = 0; i < gridSize; i++) {
//			tf[i] = new TextArea("abc");
//			panel.add(tf[i]);
//		}

//		GenericDialog gd = new GenericDialog("Grid Example");
//		gd.addPanel(panel);
//		gd.showDialog();
//		if (gd.wasCanceled())
//			return 0;

		String[] items = { "24h", "48h", "120h" };
		MyLog.log("MIRD_display_LP67");
		GenericDialog gd1 = new GenericDialog("LP67 - RISULTATI");
		gd1.setFont(textFont);
		gd1.addStringField("pippo", " ", 3);
		gd1.addToSameRow();
		gd1.addMessage("RISULTATI  OTTENUTI2");
		gd1.addToSameRow();
		gd1.addMessage("RISULTATI  OTTENUTI3");
		gd1.addMessage("RISULTATI  OTTENUTI10");
		gd1.addToSameRow();
		gd1.addMessage("RISULTATI  OTTENUTI20");
		gd1.addToSameRow();
		gd1.addMessage("RISULTATI  OTTENUTI30");

		gd1.enableYesNoCancel("ACCETTA RISULTATI", "RIPETI ANALISI");

		gd1.showDialog();

		if (gd1.wasCanceled()) {
			MyLog.log("LP67= 0 Cancel");
			return 0;
		} else if (gd1.wasOKed()) {
			MyLog.log("LP67= 2 ACCETTA RISULTATI");
			return 2;
		} else {
			MyLog.log("LP67= 1 RIPETI CONTORNATURA");
			return 1;
		}
	}

	/**
	 * 
	 * @param gd
	 * @param gridWidth
	 * @param gridHeight
	 * @param value
	 * @return
	 */
	static Panel makePanel(GenericDialog gd, int gridWidth, int gridHeight, double[] value) {
		Panel panel = new Panel();
		TextField[] tf = new TextField[gridWidth * gridHeight];
		panel.setLayout(new GridLayout(gridHeight, gridWidth));
		for (int i = 0; i < gridWidth * gridHeight; i++) {
			tf[i] = new TextField("" + value[i]);
			panel.add(tf[i]);
		}
		return panel;
	}

//	static void datiPaziente(String meta) {
//
//		String patName = getDicomValue(meta, "0010,0010");
//		String patID = getDicomValue(meta, "0010,0020");
//		String patBirthDate = getDicomValue(meta, "0010,0030");
//		String patSex = getDicomValue(meta, "0010,0040");
//		String aux1 = "#600#\t------- PAZIENTE --------";
//		Utility.appendLog(pathPermanente, aux1);
//		Utility.appendLog(pathVolatile, aux1);
//		aux1 = "#601#\tpatName= " + patName;
//		Utility.appendLog(pathPermanente, aux1);
//		Utility.appendLog(pathVolatile, aux1);
//		aux1 = "#602#\tpatID= " + patID;
//		Utility.appendLog(pathPermanente, aux1);
//		Utility.appendLog(pathVolatile, aux1);
//		aux1 = "#603#\tpatBirthDate= " + patBirthDate;
//		Utility.appendLog(pathPermanente, aux1);
//		Utility.appendLog(pathVolatile, aux1);
//		aux1 = "#603#\tpatSex= " + patSex;
//		Utility.appendLog(pathPermanente, aux1);
//		Utility.appendLog(pathVolatile, aux1);
//	}

	/**
	 * Questa routine e' void, poiche' i risultati di output vengono scritti sul
	 * file di log
	 * 
	 * @param xp1
	 * @param yp1
	 * @param MIRD_vol24
	 * @param MIRD_vol48
	 * @param MIRD_vol120
	 * 
	 * 
	 * 
	 */

	/**
	 * Esecuzione del fit mediante ImageJ o Flanagan a seconda se 2 o 3 punti
	 * selezionati
	 * 
	 * @param xp1
	 * @param yp1
	 * @param MIRD_vol24
	 * @param MIRD_vol48
	 * @param MIRD_vol120
	 * @return
	 */
	static double[] processa(double[] xp1, double[] yp1, double MIRD_vol24, double MIRD_vol48, double MIRD_vol120) {

		Regression rf = null;
		CurveFitter cf = null;
		// int count3 = 0;
		int count5 = 0;
		String aux5 = "";

		// boolean flanagan;

		double[] out2 = null;
		double[] out24 = null;
		double[] out48 = null;
		double[] out120 = null;
		int numParams = 0;
		double[] outCF = null;
		double[] paramsIJ = null;
		double[] paramsFLA = null;
		double fitGoodnessIJ = 0;
		double rSquaredIJ = 0;
		double rSquaredFLA = 0;
		double rSquaredFLAadjusted = 0;

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

		for (int i1 = 0; i1 < xp1.length; i1++) {

			MyLog.log("in " + i1 + " xp1= " + xp1[i1] + " yp1= " + yp1[i1]);

		}

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
		pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		String titolo1 = "Punti_PP01";
		Utility.MIRD_pointsPlotter(xp1, yp1, null, titolo1);

		int count = 0;
		boolean[] puntiSelezionatiFit = pointsSelection_LP33(); /// selezione dei 2 o 3 punti su cui fare il fit
		for (boolean aux : puntiSelezionatiFit) {
			if (aux)
				count++;
		}
		Utility.closePlot(titolo1);

		String titolo2 = "Punti_PP02";
		Utility.MIRD_pointsPlotter(xp1, yp1, puntiSelezionatiFit, titolo2);
		
		aux5 = "#194#\t----- POINT SELECTION ------------------";
		Utility.logModify(pathVolatile, "#194#", aux5);
		
		aux5 = "#195#\t Selezionati i punti 24h= " + puntiSelezionatiFit[0] + " 48h= " + puntiSelezionatiFit[1]
				+ " 120h= " + puntiSelezionatiFit[2];

		Utility.logModify(pathVolatile, "#195#", aux5);
		
		
		int count2 = 0;
		double[] xp2 = new double[count];
		double[] yp2 = new double[count];
		for (int i1 = 0; i1 < xp1.length; i1++) {
			if (puntiSelezionatiFit[i1]) {
				xp2[count2] = xp1[i1];
				yp2[count2] = yp1[i1];
				count2++;
			}
		}
		Utility.closePlot(titolo2);

		if (count2 == 2) {
			cf = Utility.MIRD_curveFitterSpecialImageJ(xp2, yp2);
			Utility.MIRD_curvePlotterSpecialImageJ(cf, xp1, yp1, puntiSelezionatiFit);
			// -------- recupero i dati da stampare ---------------
			paramsIJ = cf.getParams();
			numParams = cf.getNumParams();
			outCF = new double[numParams];
			for (int i1 = 0; i1 < numParams; i1++) {
				MyLog.log("MIRD FIT param " + i1 + " =" + paramsIJ[i1]);
				outCF[i1] = paramsIJ[i1];
			}
			fitGoodnessIJ = cf.getFitGoodness();
			rSquaredIJ = cf.getRSquared();

			out2 = Utility.calcoliDosimetrici(paramsIJ, null, rSquaredIJ, MIRD_vol24, MIRD_vol48, MIRD_vol120,
					pathVolatile);

			AA = out2[0];
			aa = out2[1];
			SA = out2[2];
			Sa = out2[3];
			mAtilde = out2[4];
			disintegrazioni = out2[5];
			uptake = out2[6];
			massa = out2[7];
			tmezzo = out2[8];
			tau = out2[9];
			SmAtilde = out2[10];
			Sdisintegrazioni = out2[11];
			Suptake = out2[12];
			Smassa = out2[13];
			Stmezzo = out2[14];
			Stau = out2[15];
			dose = out2[16];
			Sdose = out2[17];
			Rsquared = out2[18];
			s1 = out2[19];
			s2 = out2[20];
			m1 = out2[21];
			m2 = out2[22];

			MyLog.log("==== PRIMA DI REVIEW =====");
			MyLog.log("count2= " + count2);
			MyLog.log("==== VALORE MEDIO DOPO IMAGEJ =====");
			MyLog.log("parametro A= " + AA);
			MyLog.log("parametro a= " + aa);
			MyLog.log("mAtilde= " + mAtilde);
			MyLog.log("# disintegrazioni= " + disintegrazioni);
			MyLog.log("uptake[%]= " + uptake);
			MyLog.log("massa= " + massa);
			MyLog.log("tmezzo= " + tmezzo);
			MyLog.log("tau= " + tau);
			MyLog.log("dose= " + dose);
			MyLog.log("==== ERRORI ==========");
			MyLog.log("errore SA= " + SA);
			MyLog.log("errore Sa= " + Sa);
			MyLog.log("SmAtilde= " + SmAtilde);
			MyLog.log("S# disintegrazioni= " + Sdisintegrazioni);
			MyLog.log("Suptake= " + Suptake);
			MyLog.log("Smassa= " + Smassa);
			MyLog.log("Stmezzo= " + Stmezzo);
			MyLog.log("Stau= " + Stau);
			MyLog.log("Sdose= " + Sdose);
			MyLog.log("Rsquared= " + Rsquared);
			MyLog.log("s1= " + s1);
			MyLog.log("s2= " + s2);
			MyLog.log("m1= " + m1);
			MyLog.log("m2= " + m2);
			MyLog.log("====================================");

		} else if (count2 == 3) {

//			flanagan = true;
			rf = Utility.MIRD_curveFitterSpecialFlanagan(xp2, yp2);
			MyLog.log("FLA001");
			Utility.MIRD_curvePlotterSpecialFlanagan(rf, xp2, yp2);
			MyLog.log("FLA002");
			// -------- recupero i dati da stampare ---------------

			rSquaredFLAadjusted = rf.getAdjustedCoefficientOfDetermination();
			rSquaredFLA = rf.getCoefficientOfDetermination();
//			Utility.MIRD_curvePlotterSpecialCombined(cf, rf, xp2, yp2);
//			MyLog.log("FLA003");

			paramsFLA = rf.getBestEstimates();
			paramsFLA = Utility.vetReverser(paramsFLA);
			double[] errorsFLA = rf.getBestEstimatesErrors();
			errorsFLA = Utility.vetReverser(errorsFLA);

			out2 = Utility.calcoliDosimetrici(paramsFLA, errorsFLA, rSquaredFLA, MIRD_vol24, MIRD_vol48, MIRD_vol120,
					pathVolatile);

			AA = out2[0];
			aa = out2[1];
			SA = out2[2];
			Sa = out2[3];
			mAtilde = out2[4];
			disintegrazioni = out2[5];
			uptake = out2[6];
			massa = out2[7];
			tmezzo = out2[8];
			tau = out2[9];
			SmAtilde = out2[10];
			Sdisintegrazioni = out2[11];
			Suptake = out2[12];
			Smassa = out2[13];
			Stmezzo = out2[14];
			Stau = out2[15];
			dose = out2[16];
			Sdose = out2[17];
			Rsquared = out2[18];
			s1 = out2[19];
			s2 = out2[20];
			m1 = out2[21];
			m2 = out2[22];

			MyLog.log("==== PRIMA DI REVIEW =====");
			MyLog.log("count2= " + count2);
			MyLog.log("==== VALORE MEDIO DOPO FLANAGAN =====");
			MyLog.log("parametro A= " + AA);
			MyLog.log("parametro a= " + aa);
			MyLog.log("mAtilde= " + mAtilde);
			MyLog.log("# disintegrazioni= " + disintegrazioni);
			MyLog.log("uptake[%]= " + uptake);
			MyLog.log("massa= " + massa);
			MyLog.log("tmezzo= " + tmezzo);
			MyLog.log("tau= " + tau);
			MyLog.log("dose= " + dose);
			MyLog.log("==== ERRORI ==========");
			MyLog.log("errore SA= " + SA);
			MyLog.log("errore Sa= " + Sa);
			MyLog.log("SmAtilde= " + SmAtilde);
			MyLog.log("S# disintegrazioni= " + Sdisintegrazioni);
			MyLog.log("Suptake= " + Suptake);
			MyLog.log("Smassa= " + Smassa);
			MyLog.log("Stmezzo= " + Stmezzo);
			MyLog.log("Stau= " + Stau);
			MyLog.log("Sdose= " + Sdose);
			MyLog.log("Rsquared= " + Rsquared);
			MyLog.log("s1= " + s1);
			MyLog.log("s2= " + s2);
			MyLog.log("m1= " + m1);
			MyLog.log("m2= " + m2);
			MyLog.log("====================================");

		}
		return out2;

	}

}
