package Dosimetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.io.DirectoryChooser;
import ij.io.OpenDialog;

public class MyDialog {

	/**
	 * Dialogo conferma dati iniezione
	 * 
	 * @param in1
	 * @return
	 */
	public static boolean dialogConfermaDatiSomministrazione_LP10(String[] in1) {

		MyLog.log("dialogConfermaDatiSomministrazione_LP10");
		String data11 = in1[0];
		String ora11 = in1[1];
		String activity11 = in1[2];

		GenericDialog conf11 = new GenericDialog("LP10 - CONFERMA DATI INSERITI");

		conf11.addMessage("CONFERMA DATI SOMMINISTRAZIONE", MyGlobals.titleFont);
		conf11.setFont(MyGlobals.defaultFont);
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
	 * Dialogo inserimento dati iniezione
	 * 
	 * @return
	 */
	static String dialogInputDataSomministrazione_LP11() {

		MyLog.log("dialogInputDataSomministrazione_LP11");
		String data0;

		int day;
		int month;
		int year;
		int year4;

		MyLog.log("LP11 start");
		GenericDialog gd11 = new GenericDialog("LP11 - Date input");
		gd11.addMessage("Giorno della somministrazione: ", MyGlobals.titleFont);
		gd11.setFont(MyGlobals.defaultFont);
		String label11 = "Giorno: ";
		String format11 = "0";
		int digits11 = 4;
		gd11.addStringField(label11, format11, digits11);
		label11 = "Mese: ";
		gd11.addStringField(label11, format11, digits11);
		label11 = "Anno: ";
		gd11.addStringField(label11, format11, digits11);

		gd11.setCancelLabel("Cancel");
		gd11.showDialog();
		if (gd11.wasCanceled()) {
			MyLog.log("Cancel");
			return null;
		}

		String aux1 = "";
		day = Utility.parseInt(gd11.getNextString());
		month = Utility.parseInt(gd11.getNextString());
		aux1 = gd11.getNextString();
		year = Utility.parseInt(aux1);
		if (aux1.length() == 4) {
			year4 = year;
		} else {
			year4 = year + 2000;
		}
		// dopo varie prove NON fidandomi di java, ricostruisco da solo la data nel
		// formato
		// desiderato, in modo da non avere sgradite sorprese
		data0 = String.format("%02d", day) + "-" + String.format("%02d", month) + "-" + String.format("%04d", year4);
		format11 = "dd-MM-yyyy";
		boolean ok1 = Utility.isValidDate(data0, format11);
		if (ok1) {
			return data0;
		} else {
			MyDialog.dialogErrorMessage_LP06("LP11 " + data0 + " Data inserita ERRATA");
			return "";
		}
	}

	/**
	 * Conferma cartelle selezionate
	 * 
	 * @param str24
	 * @param str48
	 * @param str120
	 * @return
	 */
	static boolean dialogConfirmFolder_LP03(String str24, String str48, String str120) {

		MyLog.log("dialogConfirmFolder_LP03");
		GenericDialog genericDialog1 = new GenericDialog("LP03 - Confirm images Folder");
		genericDialog1.addMessage("Confirm Image Selection", MyGlobals.titleFont);
		genericDialog1.addMessage("Check Image Folder Auto-Selection", MyGlobals.defaultFont);
		genericDialog1.addMessage("24h folder path:", MyGlobals.textFont);
		genericDialog1.addMessage(str24, MyGlobals.defaultFont);
		genericDialog1.addMessage("48h folder path:", MyGlobals.textFont);
		genericDialog1.addMessage(str48, MyGlobals.defaultFont);
		genericDialog1.addMessage("120h folder path:", MyGlobals.textFont);
		genericDialog1.addMessage(str120, MyGlobals.defaultFont);
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
	 * 
	 * @return
	 */
	static boolean dialogDistretto_LP07() {

		MyLog.log("dialogo LP07");
		GenericDialog genericDialog3 = new GenericDialog("LP07 - ALTRO DISTRETTO");
		genericDialog3.setFont(MyGlobals.defaultFont);
		genericDialog3.addMessage("Posizione lesione", MyGlobals.titleFont);
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

	/**
	 * Presenza immagini nel dosimetry folder all'avvio Testato, esegue cancel &
	 * cross
	 * 
	 * @param str20
	 * @return
	 */
	static int dialogImmaginiPazientePrecedente_LP21(String[] str20) {

		MyLog.log("dialogImmaginiPazientePrecedente_LP21");
		NonBlockingGenericDialog nonBlockingGenericDialog = new NonBlockingGenericDialog(
				"LP21 - Presenza immagini paziente precedente");
		nonBlockingGenericDialog.addMessage("Presenza immagini paziente precedente", MyGlobals.titleFont);
		nonBlockingGenericDialog.setFont(MyGlobals.defaultFont);
		nonBlockingGenericDialog.addMessage(
				"Attenzione: in DosimetryFolder sul Desktop ci sono le immagini \n" + str20[0] + " di " + str20[1]);
		nonBlockingGenericDialog.enableYesNoCancel("PASSA A NUOVO PAZIENTE", "CONTINUA CON ALTRE LESIONI");
		// nonBlockingGenericDialog.setCancelLabel("");
		// nonBlockingGenericDialog.setOKLabel("PASSA A NUOVO PAZIENTE");
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
	 * Presenza immagini nel dosimetry folder all'avvio
	 * 
	 * @param str20
	 * @return
	 */
	static boolean dialogImmaginiPazientePrecedente_LP21_OLD(String[] str20) {

		MyLog.log("dialogImmaginiPazientePrecedente_LP21");
		NonBlockingGenericDialog nonBlockingGenericDialog = new NonBlockingGenericDialog(
				"LP21 - Immagini paziente precedente");
		nonBlockingGenericDialog.addMessage("Presenza immagini paziente precedente", MyGlobals.titleFont);
		nonBlockingGenericDialog.setFont(MyGlobals.defaultFont);
		nonBlockingGenericDialog.addMessage(
				"Attenzione: in DosimetryFolder sul Desktop ci sono le immagini \n" + str20[0] + " di " + str20[1],
				MyGlobals.defaultFont);
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
	 * Dialogo che mostra dati immagini del dosimetryFolder
	 * 
	 * @return
	 */
	static boolean dialogInitialize_LP00() {

		MyLog.log("dialogInitialize_LP00");
		GenericDialog genericDialog3 = new GenericDialog("LP00 - INIZIALIZZA PER NUOVO PAZIENTE");
		genericDialog3.addMessage("Inizializza per nuovo paziente", MyGlobals.titleFont);
		genericDialog3.setFont(MyGlobals.defaultFont);
		genericDialog3.addMessage("File PERMANENTE salvataggio dati", MyGlobals.defaultFont);
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
	 * Dialogo inserimento dati iniezione
	 * 
	 * @return
	 */
	static double dialogInputActivitySomministrazione_LP13() {

		MyLog.log("dialogInputActivitySomministrazione_LP13");
		// String activity2;

		double activity1;

		MyLog.log("LP13 start");
		GenericDialog gd11 = new GenericDialog("LP13 - Activity input");
		gd11.addMessage("Introduci i seguenti dati per il nuovo paziente", MyGlobals.titleFont);
		gd11.setFont(MyGlobals.defaultFont);
		String label13 = "Attivita' somministrata";
		double default13 = 0.0;
		int digits13 = 8;
		gd11.addNumericField(label13, default13, digits13, 10, "[MBq]");
		gd11.setCancelLabel("Cancel");
		gd11.showDialog();
		if (gd11.wasCanceled()) {
			MyLog.waitHere("Cancel");
			return Double.NaN;
		}
		activity1 = gd11.getNextNumber();

		return activity1;
	}

	/**
	 * Dialogo inserimento dati iniezione
	 * 
	 * @return
	 */
	static String dialogInputOraSomministrazione_LP12() {

		MyLog.log("dialogInputOraSomministrazione_LP12");
		String ora0;

		int hour;
		int minute;
		int second = 0;

		MyLog.log("LP12 start");
		GenericDialog gd11 = new GenericDialog("LP12 - Hour input");
		gd11.addMessage("Orario somministrazione: ", MyGlobals.titleFont);
		gd11.setFont(MyGlobals.defaultFont);
		String label11 = "Ora: ";
		String format11 = "0";
		int digits11 = 4;
		gd11.addStringField(label11, format11, digits11);
		label11 = "Minuto: ";
		gd11.addStringField(label11, format11, digits11);
		gd11.setCancelLabel("Cancel");
		gd11.showDialog();
		if (gd11.wasCanceled()) {
			MyLog.waitHere("Cancel");
			return null;
		}

		hour = Utility.parseInt(gd11.getNextString());
		minute = Utility.parseInt(gd11.getNextString());
		// dopo varie prove fidandomi di java, ricostruisco da solo la data nel formato
		// desiderato, in modo da non avere sgradite sorprese
		ora0 = String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
		format11 = "HH:mm:ss";
		boolean ok1 = Utility.isValidDate(ora0, format11);
		if (ok1) {
			return ora0;
		} else {
			MyDialog.dialogErrorMessage_LP06("LP12 " + ora0 + " Ora inserita ERRATA");
			return "";
		}
	}

	/**
	 * Dialogo non modale selezione immagini 24/48/120 Onora il Cancel
	 * 
	 * @return
	 */
	static boolean dialogInstructions_LP30() {

		MyLog.log("dialogInstructions_LP30");
		Dimension screen = IJ.getScreenSize();
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP30 - ISTRUZIONI");
		gd1.setFont(MyGlobals.defaultFont);
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
	 * Dialogo cancellazione immagini e dati paziente precedente
	 * 
	 * @param str20
	 * @return
	 */
	static boolean dialogNonBlockingDelete_LP01(String str20) {

		MyLog.log("dialogNonBlockingDelete_LP01");
		NonBlockingGenericDialog nonBlockingGenericDialog = new NonBlockingGenericDialog("LP01 - Command Confirmation");
		nonBlockingGenericDialog.addMessage("Confirmation Dialog", MyGlobals.titleFont);
		nonBlockingGenericDialog.setFont(MyGlobals.defaultFont);
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
	 * Dialogo con dati delle cartelle 24/48/120
	 * 
	 * @param aList
	 */
	static boolean dialogReview_LP05(ArrayList<ArrayList<String>> aList) {

		MyLog.log("dialogReview_LP05");
		GenericDialog reviewDialog = new GenericDialog("LP05 - Review Dicom Tags");
		reviewDialog.addMessage("Check the Dicom tags", MyGlobals.titleFont);
		reviewDialog.setFont(MyGlobals.defaultFont);
		reviewDialog.addMessage("Please review if the acquisition settings used are correct.");
		for (int a4 = 0; a4 < aList.size(); a4++) {
			reviewDialog.addMessage(MyGlobals.arrayOfString[a4] + " folder path:");
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
	 * Dialogo non modale di selezione
	 * 
	 * @return
	 */
	static String dialogReview_LP32() {

		MyLog.log("dialogReview_LP32");
		String[] items = { "24h", "48h", "120h" };
		int rows = 3;
		int columns = 1;
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP32 - REVIEW");
		gd1.setFont(MyGlobals.defaultFont);
		gd1.addRadioButtonGroup("IMMAGINE DA RIANALIZZARE", items, rows, columns, "VA BENE COSI'");
		gd1.addMessage("SELEZIONARE E POI PREMERE OK");
		gd1.showDialog();
		MyLog.log("LP32 - true PREMUTO OK");
		String selection = gd1.getNextRadioButton();
		return selection;
	}

	/**
	 * 
	 * @return
	 */
	static int dialogRipetizione_LP08() {

		MyLog.log("dialogo LP08");
		GenericDialog genericDialog3 = new GenericDialog("LP08 - COSA RIPETERE");
		genericDialog3.setFont(MyGlobals.defaultFont);
		genericDialog3.addMessage("Scelta tipo di problema:", MyGlobals.titleFont);
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
	 * Dialo selezione cartella 24h nuovo paziente
	 * 
	 * @return
	 */
	static boolean dialogSelection_LP02() {

		MyLog.log("dialogSelection_LP02");
		GenericDialog genericDialog = new GenericDialog("LP02 - Select images Folder");
		genericDialog.addMessage("24h Folder Selection", MyGlobals.titleFont);
		genericDialog.setFont(MyGlobals.defaultFont);
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
	 * Dialogo non modale di selezione
	 * 
	 * @return
	 */
	static boolean dialogSelection_LP31() {

		MyLog.log("dialogSelection_LP31");
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP31 - START");
		gd1.setFont(MyGlobals.defaultFont);
		gd1.addMessage("PREMERE OK");
		gd1.showDialog();
		MyLog.log("LP31 - true PREMUTO OK");
		return true;
	}

	/**
	 * Selezione cartella immagini
	 * 
	 * @return
	 */
	static String directorySelection_LP_20() {

		MyLog.log("directorySelection_LP_20");
		DirectoryChooser directoryChooser = new DirectoryChooser("LP20 Directory Selection");
		String str3 = directoryChooser.getDirectory();
		if (str3 == null) {
			MyDialog.dialogErrorMessage_LP06("Wrong selection. Please try again.");
			MyLog.log("LP20 - null  ERROR MESSAGE Wrong selection. Please try again ");
			return null;
		} else {
			MyLog.log("LP20 - selezione effettuata");
		}
		return str3;
	}

	/**
	 * Mostra i volumi 24h 48h e 120h
	 * 
	 * @param vol24
	 * @param vol48
	 * @param vol120
	 */
	static void MIRD_display_LP66(double vol24, double vol48, double vol120) {

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
		gd1.setFont(MyGlobals.defaultFont);

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

		// String[] items = { "24h", "48h", "120h" };
		String aux1 = "";
		MyLog.log("MIRD_display_LP67");
		GenericDialog gd1 = new GenericDialog("LP67 - RISULTATI");
		gd1.addMessage("RISULTATI  OTTENUTI", MyGlobals.titleFont);
		gd1.setFont(MyGlobals.defaultFont);

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
		double Suptake = vetInput[10];
		double Smassa = vetInput[11];
		double Stmezzo = vetInput[12];
		double Sdose = vetInput[13];

		String aux1 = "";

		MyLog.log("MIRD_display_LP68");
		GenericDialog gd1 = new GenericDialog("LP68 - RISULTATI");
		gd1.addMessage("RISULTATI  OTTENUTI", MyGlobals.titleFont);
		gd1.setFont(MyGlobals.defaultFont);

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
	 * selezione di un file da parte dell'utilizzatore
	 *
	 * @return
	 */
	public static int dialogAltreLesioni_FM02() {

		MyLog.log("FM02 start");
		GenericDialog gd1 = new GenericDialog("FM02 - AltreLesioni");
		gd1.addMessage("Ci sono altre lesioni nel fegato?", MyGlobals.titleFont);
		gd1.setFont(MyGlobals.defaultFont);
		gd1.enableYesNoCancel("ALTRE LESIONI", "FINITE");

		gd1.setCancelLabel("Cancel");
		gd1.showDialog();
		if (gd1.wasCanceled()) {
			MyLog.log("FM02 0 Cancel");
			return 0;
		} else if (gd1.wasOKed()) {
			MyLog.log("FM02 2 ALTRE LESIONI");
			return 2;
		} else {
			MyLog.log("FM02 1 FINITE ");
			return 1;
		}

	}

	/**
	 * Selezione altro distretto anatomico
	 * 
	 */
	static void dialogAltroDistretto_DD08() {
		MyLog.log("DD08_altroDistretto");
		GenericDialog finished1 = new GenericDialog("DD08 - Altro distretto");
		finished1.setFont(MyGlobals.defaultFont);

		finished1.addMessage("HAI TERMINATO ANALISI DISTRETTO?");
		finished1.addMessage("se rispondi ALTRA LESIONE vuoi analizzare un altra lesione");
		finished1.addMessage(
				"se rispondi FINITO vuoi passare in LoadPatient e caricare un altro distretto anatomico OPPURE HAI TERMINATO");
		finished1.setOKLabel("FINITO");
		finished1.setCancelLabel("ALTRA LESIONE");

		finished1.showDialog();
		if (finished1.wasCanceled())
			MyLog.log("DD08 premuto ALTRA LESIONE");
		if (finished1.wasOKed())
			MyLog.log("DD08 premuto FINITO");
	}

	/**
	 * Assegnazione nome alle lesioni
	 * 
	 * @param pathVolatile
	 * @param pathPermanente
	 */
	static String dialogBattezzaLesioni_LP27(String pathVolatile) {
		// alla fine del nostro reiterativo lavoro decidiamo che dobbiamo salvare il
		// tutto CHE COSA POTRA'MAI ANDARE STORTO???
		NonBlockingGenericDialog compliments1 = new NonBlockingGenericDialog("LP27 - Battezza lesioni");
		compliments1.setFont(MyGlobals.defaultFont);
		compliments1.addMessage("COMPLIMENTI, HAI COMPLETATO L'ANALISI DELLA LESIONE");
		compliments1.addStringField("NomeLesione per memorizzazione", "");
		compliments1.showDialog();
		String lesionName = compliments1.getNextString();
		MyLog.log("eseguo battezzaLesioni con LP27 lesionName= " + lesionName);

		// ora i nostri dati verrano battezzati col nome fornito dal ... PADRINO !!!
		// il nome del nuovo file diverra' lesionName.txt, non occorre un controllo che
		// l'operatore non ci abbia CASUALMENTE fornito lo stesso nome di una altra
		// lesione, in tal caso gli verra'cantata tutta la canzone "Il gorilla" di
		// Fabrizio de Andre', ovviamente con esempi pratici.

		int pos = pathVolatile.lastIndexOf(File.separator);
		String pathBase = pathVolatile.substring(0, pos);
		String pathLesione = pathBase + File.separator + lesionName + ".txt";

		MyLog.logEnd(pathVolatile);
		MyLog.logMove(pathLesione, pathVolatile);
		MyLog.logInit(pathVolatile);
		return lesionName;
	}

	/**
	 * Visualizzazione messaggi di errore
	 * 
	 * @param paramString
	 */
	static void dialogErrorMessage_LP06(String paramString) {

		MyLog.log("dialogErrorMessage_LP06");
		GenericDialog genericDialog = new GenericDialog("LP06 - Error");
		genericDialog.setFont(MyGlobals.defaultFont);
		genericDialog.addMessage(paramString);
		genericDialog.hideCancelButton();
		genericDialog.showDialog();
	}

	/**
	 * Visualizzazione messaggi di errore
	 * 
	 * @param paramString
	 */
	static boolean dialogErrorMessageWithCancel_LP09(String paramString) {

		MyLog.log("dialogErrorMessagWithCancel_LP09");
		GenericDialog gd1 = new GenericDialog("LP09 - Error");
		gd1.setFont(MyGlobals.defaultFont);
		gd1.addMessage(paramString);
		gd1.showDialog();
		if (gd1.wasCanceled())
			return true;
		else
			return false;
	}

	/**
	 * selezione di un file da parte dell'utilizzatore
	 * 
	 * @param message messaggio per l'utilizzatore
	 * @return path dell'immagine selezionata
	 */
	public static String dialogFileSelection_FM01(String message, String defaultPath) {

		OpenDialog od1 = new OpenDialog(message);
		OpenDialog.setDefaultDirectory(defaultPath);
		return od1.getPath();

	}

	/**
	 * Utilizzata da Dosimetry_v2
	 * 
	 * @param ok24
	 * @param ok48
	 * @param ok120
	 * @param defaultFont
	 * @return
	 */
	static ImagePlus dialogSceltaAutomaticaImmagine_DD10(boolean ok24, boolean ok48, boolean ok120, Font defaultFont) {

		boolean[] choice = new boolean[3];
		String default1 = "";
		Color color24 = Color.red;
		Color color48 = Color.red;
		Color color120 = Color.red;
		if (ok24) {
			color24 = Color.green;
			choice[0] = true;
		}
		if (ok48) {
			color48 = Color.green;
			choice[1] = true;
		}
		if (ok120) {
			color120 = Color.green;
			choice[2] = true;
		}

		String[] lista1 = WindowManager.getImageTitles();
		//
		// cerco di gestire la scelta di default in modo che presenti la prossima
		// immagine "rossa" non ancora elaborata, in questo modo l'operatore può
		// limitarsi ad accettare l'immagine proposta
		//

		for (int i1 = 0; i1 < 3; i1++) {
			if (!choice[i1]) {
				default1 = lista1[i1];
				break;
			}
		}

		Dimension screen = IJ.getScreenSize();
		// ImageWindow window = WindowManager.getCurrentWindow();
		// window.setLocationAndSize(0, 0, (int) (((double) screen.height) / 2), (int)
		// (((double) screen.height) / 2));

		NonBlockingGenericDialog scelta1 = new NonBlockingGenericDialog("DD10 - Immagine da analizzare");
		scelta1.addImageChoice("scelta immagine", default1);
		scelta1.addMessage(lista1[0], defaultFont, color24);
		scelta1.addMessage(lista1[1], defaultFont, color48);
		scelta1.addMessage(lista1[2], defaultFont, color120);
		scelta1.setLocation(screen.width * 2 / 3, screen.height * 2 / 3);
		scelta1.showDialog();

		if (scelta1.wasCanceled())
			return null;
		ImagePlus dicomImage1 = scelta1.getNextImage();
		return dicomImage1;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	static String dialogSceltaLesione_SV02(String path) {

		File path1 = new File(path);
		File[] filesAndDirs = path1.listFiles();
		File file = null;
		String name;
		ArrayList<String> scelta = new ArrayList<String>();

		if (filesAndDirs == null)
			return null;
		for (int i1 = 0; i1 < filesAndDirs.length; i1++) {
			file = filesAndDirs[i1];
			if (file.isFile()) {
				name = file.getName();
				if (name.contains(".")) {
					String[] parts = name.split("\\.");
					if (parts[1].equals("txt")) {
						if ((!parts[0].equals("volatile")) && (!parts[0].equals("permanente")))
							scelta.add(parts[0]);
					}
				} else {
					dialogErrorMessage_LP06("NON CI SONO LESIONI SALVATE");
					return null;
				}

			}
		}

		// se è un file guardo se l'estensione e' txt

		String[] scelta2 = Utility.arrayListToArrayString(scelta);

		NonBlockingGenericDialog scelta1 = new NonBlockingGenericDialog("SV02 - Selezionare nome lesione");
		scelta1.addChoice("scelta lesione", scelta2, scelta2[0]);
		scelta1.showDialog();

		if (scelta1.wasCanceled())
			return null;
		String out = scelta1.getNextChoice();
		return out;
	}

}
