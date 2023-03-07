package Dosimetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import flanagan.analysis.Regression;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.NewImage;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.plugin.DICOM;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.DicomTools;
import ij.util.FontUtil;

//
// DATI SOMMINISTRAZIONE 			#001#-#009# 
// IMAGE INFO 24h 					#010#-#029#
// IMAGE INFO 48 h					#030#-#049#
// IMAGE INFO 120 h					#050#-#069#
// PATIENT-DOSIMETRY INFO 24 h		#100#-#129#
// PATIENT-DOSIMETRY INFO 48 h		#130#-#159#
// PATIENT-DOSIMETRY INFO 24 h		#160#-#199#
//

public class Utility {

//	private static final Object[][] matrice = null;
	static String fontStyle = "Arial";
	static Font defaultFont = FontUtil.getFont(fontStyle, Font.PLAIN, 13);
	static Font textFont = FontUtil.getFont(fontStyle, Font.ITALIC, 16);
	static Font titleFont = FontUtil.getFont(fontStyle, Font.BOLD, 16);
	long start1;
	long start2;
	long start3;
	long start4;
	long end1;
	long end2;
	long end3;
	long end4;
	static int coordX;
	static int coordY;
	static int coordZ;

	public static int latoCubo() {
		// ATTENZIONE il lato DEVE essere dispari
		// in questo modo avremo un pixel centrale
		int lato = 5;

		return lato;
	}

//	/**
//	 * Introdotto il file DosimetryConfig.txt
//	 * 
//	 * @return
//	 */
//	public static boolean stampa() {
//
//		String[] config = Utility.leggiConfig("DosimetryConfig.txt");
//		if (config == null) {
//			return true;
//		}
//
//		String strIn = config[0];
//		if (strIn.equalsIgnoreCase("SI")) {
//			return true;
//		} else {
//			return false;
//		}
//	}

	/**
	 * Porta petctviewr toFront
	 * 
	 * @param name1 nome finestra
	 */
	static void nonImageToFront(String name1) {
		Window win1 = WindowManager.getWindow(name1);
		WindowManager.toFront(win1);
	}

	/**
	 * Porta l'immagine toFront e la ridimensiona
	 * 
	 * @param name1 nome immagine
	 */
	static void imageToFront(String name1) {
		Window win1 = WindowManager.getWindow(name1);
		// win1.setVisible(false);
		WindowManager.toFront(win1);
		IJ.wait(100);
		Dimension screen = IJ.getScreenSize();
		ImageWindow window = WindowManager.getCurrentWindow();
		window.setLocationAndSize(0, 0, (int) (((double) screen.height) / 1.5), (int) (((double) screen.height) / 1.5));
	}

	/**
	 * Test per validazione data
	 * 
	 * @param date
	 * @return
	 */
	static boolean isValidDate(String date, String format) {

		try {
			DateFormat sdfrmt = new SimpleDateFormat(format);
			sdfrmt.setLenient(false);
			sdfrmt.parse(date);
			// MyLog.log("isValidDate TEST SUPERATO");
			return true;
		} catch (ParseException e) {
			// MyLog.log("isValidDate ERRORE");
			return false;
		}
	}

	/**
	 * Test per validazione ora
	 * 
	 * @param date
	 * @return
	 */
	static boolean isValidTime(String time, String format) {

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try {
			sdf.parse(time);
			// MyLog.log("isValidTime TEST SUPERATO");
			return true;
		} catch (ParseException e) {
			// MyLog.log("isValidTime ERRORE");
			return false;
		}
	}

	/**
	 * Test per validazione dataora
	 * 
	 * @param timestamp
	 * @return
	 */
	static boolean isValidDateTime(String timestamp, String format) {

		SimpleDateFormat format1 = new SimpleDateFormat(format);
		format1.setLenient(false);
		try {
			format1.parse(timestamp);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	static boolean isValidDateNew(String date, String format) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

		try {
			formatter.parse(date);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	/**
	 * Test per validazione dataora
	 * 
	 * @param timestamp
	 * @return
	 */
	static Date getDateTime(String timestamp, String format) {

		Date dateTime = null;
		SimpleDateFormat format1 = new SimpleDateFormat(format);
		try {
			dateTime = format1.parse(timestamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateTime;
	}

	/**
	 * Legge ricorsivamente la directory e relative sottodirectory
	 * 
	 * copied from www.javapractices.com (Alex Wong
	 * 
	 * @param startingDir directory "radice"
	 * @return lista dei path dei file
	 */
	public static List<File> getFileListing(File startingDir) {

		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = startingDir.listFiles();
		if (filesAndDirs == null)
			return null;
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for (File file : filesDirs) {
			if (!file.isFile()) {
				// must be a directory
				// recursive call !!
				List<File> deeperList = getFileListing(file);
				result.addAll(deeperList);
			} else {
				if (isDicomImage(file.getPath())) // evita i file *.py
					result.add(file);
			}
		}
		return result;
	}

	/***
	 * Verifica la disponibilita' di una directory
	 * 
	 * @param name
	 * @return
	 */
	public static boolean checkDir(String name) {
		File dirCheck = new File(name);
		if (!dirCheck.exists())
			return false;
		else
			return true;
	}

	/**
	 * Verifica la disponibilita' di una directory
	 * 
	 * @param dirCheck
	 * @return
	 */
	public static boolean checkDir(File dirCheck) {
		if (!dirCheck.exists())
			return false;
		else
			return true;
	}

	/**
	 * Cancella tutti i file contenuti nella directory
	 * 
	 * @param dir
	 */
	public static void purgeDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (!file.isDirectory())
				file.delete();
		}
	}

	/**
	 * 
	 * 
	 * @param dir
	 */
	public static void deleteFile(File file1) {

		file1.delete();
	}

	/**
	 * From stackOverflow.com
	 * 
	 * @param pathToDir
	 * @param extension
	 * @return
	 */
	public static boolean deleteAllFilesWithSpecificExtension(String pathToDir, String extension) {
		boolean success = false;
		File folder = new File(pathToDir);
		File[] fList = folder.listFiles();
		for (File file : fList) {
			String pes = file.getName();
			if (pes.endsWith("." + extension)) {
				success = (new File(String.valueOf(file)).delete());
			}
		}
		return success;
	}

	/**
	 * Cancella directory pure se piena
	 * 
	 * @param file
	 */
	public static void deleteDirectory(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f1 : contents) {
				deleteDirectory(f1);
			}
		}
		file.delete();
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
//		ImageWindow window = WindowManager.getCurrentWindow();
//		window.setLocationAndSize(0, 0, (int) (((double) screen.height) / 2), (int) (((double) screen.height) / 2));

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
					Utility.dialogErrorMessage_LP06("NON CI SONO LESIONI SALVATE");
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

	/**
	 * Legge un intero da una stringa.
	 * 
	 * @param tmp1
	 * @return
	 */
	static int parseInt(String tmp1) {
		int ret1 = 0;
		double dbl1;
		if (tmp1 != null && !tmp1.isEmpty()) {
			try {
				dbl1 = Double.parseDouble(tmp1);
				ret1 = (int) dbl1;
			} catch (Exception e) {
				ret1 = 0;
			}
		}
		return ret1;
	}

	/**
	 * Generazione tabella
	 * 
	 * @return
	 */
	static double[][] matTable() {
		double[][] myTable = { { 0.3, 0.4, 0.5 }, { 2.0050800, 1.4416900, 1.1119950 },
				{ 1.0008740, 1.0009900, 1.0016370 }, { 0.0838640, 0.1330990, 0.1528385 } };
		return myTable;
	}

	/**
	 * Calcola i parametri di plottaggio di un punto, modificato come indicato in
	 * "FORMULE CORREZIONE" del 040123
	 * 
	 * @param pathVolatile
	 * @param pathPermanente
	 */
	static double[] MIRD_point(double[] in1) {

		double durata = in1[0]; // #018# acquisition duration
		double conteggio = in1[1]; // #119# // pixel number over threshold
//		double activity = in1[2]; // #003# activity
		double threshold = in1[3]; // #115# contouring threshold level
		double integral = in1[4]; // #120# over threshold count integral

		double[][] myMatTable = matTable();
//		double t1 = 0;
		double a1 = 0;
		double b1 = 0;
		double c1 = 0;

		if (threshold <= 0.30) {
//			t1 = myMatTable[0][0];
			a1 = myMatTable[1][0];
			b1 = myMatTable[2][0];
			c1 = myMatTable[3][0];
		} else if (threshold > 0.30 && threshold < 0.50) {
//			t1 = myMatTable[0][1];
			a1 = myMatTable[1][1];
			b1 = myMatTable[2][1];
			c1 = myMatTable[3][1];
		} else {
//			t1 = myMatTable[0][2];
			a1 = myMatTable[1][2];
			b1 = myMatTable[2][2];
			c1 = myMatTable[3][2];
		}

		double MIRD_vol = conteggio * Math.pow(4.42, 3) / 1000.;
		double MIRD_fatCal = a1 * Math.pow(b1, MIRD_vol) * Math.pow(MIRD_vol, c1);
		double MIRD_attiv = integral / (durata * MIRD_fatCal);
		double[] MIRD_out1 = new double[3];
		MIRD_out1[0] = MIRD_vol; // #201# MIRD_vol24
		MIRD_out1[1] = MIRD_fatCal; // #202# MIRD_fatCal24
		MIRD_out1[2] = MIRD_attiv; // #203# MIRD_attiv24

		return MIRD_out1;
	}

	/**
	 * Calcolo Fit esponenziale
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static double[] MIRD_curveFitterImageJ(double[] vetX, double[] vetY) {

		MyLog.log("=== CURVE FITTER IMAGEJ ====");
		CurveFitter cf1 = new CurveFitter(vetX, vetY);
		cf1.doFit(CurveFitter.EXPONENTIAL);
		String status = cf1.getStatusString();
		MyLog.log("STATUS del fit= " + status);
		double[] params = cf1.getParams();
		int numParams = cf1.getNumParams();
		double goodness = cf1.getFitGoodness();
		String res1 = cf1.getResultString();
		int iterations = cf1.getIterations();
		double sumResidualSqr1 = cf1.getSumResidualsSqr();
		double rSquared = cf1.getRSquared();
		String formula = cf1.getFormula();
		double[] ff1 = new double[256];
		for (int i1 = 0; i1 < 256; i1++) {
			ff1[i1] = cf1.f(i1);
			MyLog.log("x= " + i1 + "ff1=" + ff1[i1]);
		}

		double[] out1 = new double[numParams];
		for (int i1 = 0; i1 < numParams; i1++) {
			MyLog.log("MIRD FIT param " + i1 + " =" + params[i1]);
			out1[i1] = params[i1];
		}

		MyLog.log("MIRD FIT iterations= " + iterations);
		MyLog.log("MIRD FIT goodness=  " + goodness);
		MyLog.log("MIRD FIT sumResidualSqr=  " + sumResidualSqr1);
		MyLog.log("MIRD FIT R^2=  " + rSquared);
		MyLog.log("MIRD FIT numParams=  " + numParams);
		MyLog.log("MIRD FIT resultString=  " + res1);
		MyLog.log("MIRD FIT formula=  " + formula);
		MyLog.log("=====================");
		return out1;
	}

	/**
	 * Calcolo Fit esponenziale
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static CurveFitter MIRD_curveFitterSpecialImageJ(double[] vetX, double[] vetY) {

		MyLog.log("=== CURVE FITTER SPECIAL IMAGEJ ====");
		CurveFitter cf1 = new CurveFitter(vetX, vetY);
		cf1.doFit(CurveFitter.EXPONENTIAL);
		return cf1;
	}

	/**
	 * Calcolo Fit esponenziale
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static Regression MIRD_curveFitterSpecialFlanagan(double[] vetX, double[] vetY) {

		MyLog.log("=== CURVE FITTER SPECIAL FLANAGAN 002 ====");

		Regression reg = new Regression(vetX, vetY);

		reg.exponentialSimple();

		return reg;
	}

	/**
	 * Calcolo Fit esponenziale
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static double[] MIRD_curveFitterFlanagan(double[] vetX, double[] vetY) {

		MyLog.log("=== CURVE FITTER FLANAGAN 001 ===");

		Regression reg = new Regression(vetX, vetY);

		// reg.exponentialSimple();
		reg.exponentialSimplePlot();
		MyLog.waitHere("SPETTA");
		double[] bestEstimates = reg.getBestEstimates();
		for (double est : bestEstimates) {
			MyLog.log("FLANAGAN bestEstimates= " + est);
		}
		MyLog.log("--------------");
		double[] bestEstErrors = reg.getBestEstimatesErrors();
		for (double err : bestEstErrors) {
			MyLog.log("FLANAGAN bestErrors= " + err);
		}
		double coeffOfDetermination = reg.getCoefficientOfDetermination();
		MyLog.log("FLANAGAN coeffOfDetermination= " + coeffOfDetermination);

		double adjustedCoeffOfDetermination = reg.getAdjustedCoefficientOfDetermination();
		MyLog.log("FLANAGAN adjustedCoeffOfDetermination= " + adjustedCoeffOfDetermination);

		MyLog.waitHere("SPETTA");

		MyLog.log("===============");

		return null;
	}

	/**
	 * Calcolo della durata dell'acquisizione in secondi
	 * 
	 * @param imp1 immagine da analizzare
	 * @return durata
	 */
	static int MIRD_calcoloDurataAcquisizione(ImagePlus imp1) {

		int numFrames = Utility.parseInt(DicomTools.getTag(imp1, "0054,0053"));
		int durationFrame = Utility.parseInt(DicomTools.getTag(imp1, "0018,1242"));
		int durata = numFrames * (durationFrame / 1000);

		return durata;
	}

	/**
	 * Calcolo delta T in millisecondi
	 * 
	 * @param dateTime0
	 * @param dateTime24
	 * @return
	 */
	static long MIRD_calcoloDeltaT(Date dateTime0, Date dateTime24) {

		long diff = dateTime24.getTime() - dateTime0.getTime();
		return diff;
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
		compliments1.setFont(defaultFont);
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
	 * Selezione altro distretto anatomico
	 * 
	 */
	static void dialogAltroDistretto_DD08() {
		MyLog.log("DD08_altroDistretto");
		GenericDialog finished1 = new GenericDialog("DD08 - Altro distretto");
		finished1.setFont(defaultFont);

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
	 * Ammazza tutte le finestre aperte: in pratica un teppista col tirasassi!
	 */
	static void chiudiTutto() {

		MyLog.log("eseguo chiudiTutto");
		ImagePlus imp1 = null;
		while (WindowManager.getCurrentImage() != null) {
			imp1 = WindowManager.getCurrentImage();
			imp1.changes = false;
			imp1.close();
		}
		for (final Window w : WindowManager.getAllNonImageWindows()) {
			w.dispose();
		}
	}

	/**
	 * Verifica esistenza dei dati di somministrazione
	 * 
	 * @param path
	 * @return
	 */
	static boolean datiSomministrazionePresenti(String path) {

		File fil = new File(path);
		if (!fil.exists())
			return false;

		String aux1 = "";
		double aux2 = 0;
		aux1 = MyLog.readFromLog(path, "#002#", "=");
		if (aux1 == null)
			return false;
		if (!Utility.isValidDateTime(aux1, "dd-MM-yyyy HH:mm:ss"))
			return false;
		aux1 = MyLog.readFromLog(path, "#003#", "="); // activity
		if (aux1 == null)
			return false;
		aux2 = Double.parseDouble(aux1);
		if (aux2 <= 0)
			return false;

		return true;
	}

	/*
	 * Letture dei titoli delle finestre nonImmagine, usato per tests
	 * 
	 */
	public String[] titoli() {
		String[] all = WindowManager.getNonImageTitles();
		return all;

	}

	/**
	 * Inverte un vettore, per ovviare al fatto che tra Flanagan e ImageJ i dati di
	 * output sono invertiti tra loro
	 * 
	 * @param parameters
	 * @return
	 */
	static double[] vetReverser(double[] parameters) {
		double[] out = new double[parameters.length];
		int count = 0;
		for (int i1 = parameters.length - 1; i1 >= 0; i1--) {
			out[count++] = parameters[i1];
		}

		return out;
	}

	/**
	 * Calcolo di vari valori dosimetrici
	 * 
	 * @param params
	 * @param errors
	 * @param Rsquared     serve solo per riportarlo in uscita
	 * @param vol24
	 * @param vol48
	 * @param vol120
	 * @param pathVolatile
	 * @return
	 */
	static double[] calcoliDosimetrici(double[] params, double[] errors, double Rsquared, double vol24, double vol48,
			double vol120, String pathVolatile) {

		double[] vetVol = new double[3];
		vetVol[0] = vol24;
		vetVol[1] = vol48;
		vetVol[2] = vol120;

		double AA = Math.abs(params[0]);
		double aa = Math.abs(params[1]);
		double mAtilde = AA / aa;
		double disintegrazioni = mAtilde / 100;
		double somministrata = MyLog.readDoubleFromLog(pathVolatile, "#003#", "=");
		double uptake = AA / somministrata;
		double massa = vetMean(vetVol);
		double tmezzo = Math.log(2) / aa;
		double tau = mAtilde / somministrata;

		double SA = Double.NaN;
		double Sa = Double.NaN;
		double SmAtilde = Double.NaN;
		double Sdisintegrazioni = Double.NaN;
		double Suptake = Double.NaN;
		double Smassa = vetSdKnuth(vetVol);
		double Stmezzo = Double.NaN;
		double Stau = Double.NaN;
//		double Sdose = Double.NaN;

		if (errors != null) {
			SA = errors[0];
			Sa = errors[1];
			SmAtilde = Math.sqrt(Math.pow(aa, 2) * Math.pow(SA, 2) + Math.pow(AA, 2) * Math.pow(Sa, 2))
					/ (Math.pow(aa, 2));
			Sdisintegrazioni = SmAtilde / 100;
			Suptake = SA / somministrata;

			Stmezzo = (Math.log(2) * Sa) / Math.pow(aa, 2);
			Stau = SmAtilde / somministrata;
			MyLog.log("flanagan errors");
		}

		double[] vetDose = MIRD_calcoloDose(massa, mAtilde, SmAtilde, Smassa, pathVolatile);

		MyLog.log("AA= " + AA);
		MyLog.log("aa= " + aa);
		MyLog.log("SA= " + SA);
		MyLog.log("Sa= " + Sa);
		MyLog.log("mAtilde= " + mAtilde);
		MyLog.log("disintegrazioni= " + disintegrazioni);
		MyLog.log("uptake= " + uptake);
		MyLog.log("massa= " + massa);
		MyLog.log("tmezzo= " + tmezzo);
		MyLog.log("tau= " + tau);
		MyLog.log("dose= " + vetDose[0]);
		MyLog.log("Sdose= " + vetDose[1]);

		double[] out1 = new double[23];
		out1[0] = AA;
		out1[1] = aa;
		out1[2] = SA;
		out1[3] = Sa;
		out1[4] = mAtilde;
		out1[5] = disintegrazioni;
		out1[6] = uptake;
		out1[7] = massa;
		out1[8] = tmezzo;
		out1[9] = tau;
		out1[10] = SmAtilde;
		out1[11] = Sdisintegrazioni;
		out1[12] = Suptake;
		out1[13] = Smassa;
		out1[14] = Stmezzo;
		out1[15] = Stau;
		out1[16] = vetDose[0];
		out1[17] = vetDose[1];
		out1[18] = Rsquared;
		out1[19] = vetDose[2]; // s1;
		out1[20] = vetDose[3]; // s2;
		out1[21] = vetDose[4]; // m1;
		out1[22] = vetDose[5]; // m2;

		return out1;
	}

	/**
	 * Calculates the standard deviation of an array of numbers. see Knuth's The Art
	 * Of Computer Programming Volume II: Seminumerical Algorithms This algorithm is
	 * slower, but more resistant to error propagation.
	 * 
	 * @param data Numbers to compute the standard deviation of. Array must contain
	 *             two or more numbers.
	 * @return standard deviation estimate of population ( to get estimate of
	 *         sample, use n instead of n-1 in last line )
	 */
	public static double vetSdKnuth(double[] data) {
		final int n = data.length;
		if (n < 2) {
			return Double.NaN;
		}
		double avg = data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n - 1));
	}

	/**
	 * Calcola la media di un vettore
	 * 
	 * @param data
	 * @return
	 */

	public static double vetMean(double[] data) {
		final int n = data.length;
		if (n < 1) {
			return Double.NaN;
		}
		double sum = 0;
		for (int i1 = 0; i1 < data.length; i1++) {
			sum += data[i1];
		}
		double mean = sum / data.length;
		return mean;
	}

	/**
	 * Calcola la media di un vettore
	 * 
	 * @param data
	 * @return
	 */

	public static double vetMeanSecond(double[] data) {
		final int n = data.length;
		if (n < 1) {
			return Double.NaN;
		}
		double sum = 0;
		for (int i1 = 1; i1 < data.length; i1++) {
			sum += data[i1];
		}
		double mean = sum / (data.length - 1);
		return mean;
	}

	/**
	 * Calcola la media di un vettore
	 * 
	 * @param data
	 * @return
	 */

	public static float vetMean(float[] data) {
		final int n = data.length;
		if (n < 1) {
			return Float.NaN;
		}
		float sum = 0;
		for (int i1 = 0; i1 < data.length; i1++) {
			sum += data[i1];
		}
		float mean = sum / data.length;
		return mean;
	}

	/**
	 * Calcolo della dose e scrittura risultati nel file di log
	 * 
	 * @param massa
	 * @param mAtilde
	 * @param SmAtilde
	 * @param Smassa
	 * @param pathVolatile
	 * @return
	 */
	static double[] MIRD_calcoloDose(double massa, double mAtilde, double SmAtilde, double Smassa,
			String pathVolatile) {

		// inserisco la tabella

		double[][] sFactor = {
				{ 0.01, 0.10, 0.50, 1.00, 2.00, 4.00, 6.00, 8.00, 10.00, 20.00, 40.00, 60.00, 80.00, 100.00, 300.00,
						400.00, 500.00, 600.00, 1000.00, 2000.00, 3000.00, 4000.00, 5000.00, 6000.00 },
				{ 7.85E+03, 8.17E+02, 1.66E+02, 8.39E+01, 4.21E+01, 2.11E+01, 1.41E+01, 1.06E+01, 8.50E+00, 4.25E+00,
						2.14E+00, 1.43E+00, 1.07E+00, 8.60E-01, 2.89E-01, 2.18E-01, 1.75E-01, 1.46E-01, 8.82E-02,
						4.46E-02, 2.99E-02, 2.26E-02, 1.81E-02, 1.52E-02 } };
		// cerco in tabella i valori inferiori e superiori
		double s1 = Double.NaN;
		double s2 = Double.NaN;
		double m1 = Double.NaN;
		double m2 = Double.NaN;

		for (int i1 = 0; i1 < sFactor[1].length - 1; i1++) {

			if (sFactor[0][i1] <= massa && sFactor[0][i1 + 1] >= massa) {
				m2 = sFactor[0][i1 + 1];
				s2 = sFactor[1][i1 + 1];
				m1 = sFactor[0][i1];
				s1 = sFactor[1][i1];
				break;
			}
		}

		// per trovare la dose faccio una interpolazione lineare,

		double dose = (mAtilde / 1000) * (((s2 - s1) / (m2 - m1)) * (massa - m1) + s1);

//		int count5 = 550;
//		String aux5 = "#" + String.format("%03d", count5++) + "#\t-------- CALCOLO DOSE -----------";
//		Utility.logAppend(pathVolatile, aux5);
//		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s1= " + s1;
//		Utility.logAppend(pathVolatile, aux5);
//		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s2= " + s2;
//		Utility.logAppend(pathVolatile, aux5);
//		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m1= " + m1;
//		Utility.logAppend(pathVolatile, aux5);
//		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m2= " + m2;
//		Utility.logAppend(pathVolatile, aux5);
//		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose massa= " + massa;
//		Utility.logAppend(pathVolatile, aux5);

		double Sdose = Double.NaN;

		if (SmAtilde != Double.NaN) {
			Sdose = Math.sqrt(Math.pow((SmAtilde / 1000) * (((s2 - s1) / (m2 - m1)) * (massa - m1) + s1), 2)
					+ Math.pow((mAtilde / 1000) * ((s2 - s1) / (m2 - m1)) * Smassa, 2));

		}
//		MyLog.waitHere("s1= " + s1 + " s2= " + s2 + " m1= " + m1 + " m2= " + m2);

		double[] out1 = new double[6];
		out1[0] = dose;
		out1[1] = Sdose;
		out1[2] = s1;
		out1[3] = s2;
		out1[4] = m1;
		out1[5] = m2;

		return out1;
	}

	/**
	 * Chiude le immagini di plot, utilizzandone il titolo
	 * 
	 * @param title
	 */
	public static void closePlot(String title) {
		String[] vetNames = WindowManager.getImageTitles();
		for (int i1 = 0; i1 < vetNames.length; i1++) {
			ImagePlus impx = WindowManager.getImage(vetNames[i1]);
			// if (impx.getInfoProperty() == null && impx.getBitDepth() == 8)
			if (impx.getTitle().equals(title))
				impx.close();
		}
	}

	/**
	 * Visualizzazione messaggi di errore
	 * 
	 * @param paramString
	 */
	static void dialogErrorMessage_LP06(String paramString) {

		MyLog.log("dialogErrorMessage_LP06");
		GenericDialog genericDialog = new GenericDialog("LP06 - Error");
		genericDialog.setFont(defaultFont);
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
		gd1.setFont(defaultFont);
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
	 * selezione di un file da parte dell'utilizzatore
	 *
	 * @return
	 */
	public static int dialogAltreLesioni_FM02() {

		MyLog.log("FM02 start");
		GenericDialog gd1 = new GenericDialog("FM02 - AltreLesioni");
		gd1.addMessage("Ci sono altre lesioni nel fegato?", titleFont);
		gd1.setFont(defaultFont);
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

	public static String getJarTitle() {

		String jarName = "unknown";
		try {
			String jarPath = Utility.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			jarName = jarPath.substring(jarPath.lastIndexOf("/") + 1);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return jarName;

	}

	/**
	 * Apre una immagine dal path
	 * 
	 * @param path immagine da aprire
	 * @return imageplus aperta
	 */
	public static ImagePlus openImage(String path) {

		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(path);
		if (imp == null) {
			Utility.dialogErrorMessage_LP06(path);
			return null;
		}
		return imp;
	}

	public static ImagePlus openImage(File file) {
		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(file.getPath());
		if (imp == null) {
			Utility.dialogErrorMessage_LP06(file.getPath());
			return null;
		}
		return imp;
	}

	/***
	 * Testa se fileName1 e' un file dicom ed e' un immagine visualizzabile da
	 * ImageJ, eventualmente scrive a log nome file e tipo di errore
	 * 
	 * @param fileName1
	 * @return boolean
	 */

	public static boolean isDicomImage(String fileName1) {
		boolean ok = true;
		String info = new DICOM().getInfo(fileName1);
		if (info == null || info.length() == 0) {
			MyLog.log("File " + fileName1 + " >>> HAS NOT DICOM INFO");
			ok = false;
		} else if (!info.contains("7FE0,0010")) {
			MyLog.log("File " + fileName1 + " >>> HAS NOT PIXEL DATA");
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
	 * estrae una singola slice da uno stack. Estrae anche i dati header
	 * 
	 * @param stack stack contenente le slices
	 * @param slice numero della slice da estrarre, deve partire da 1, non e'
	 *              ammesso lo 0
	 * @return ImagePlus della slice estratta
	 */
	public static ImagePlus imageFromStack(ImagePlus stack, int slice) {

		if (stack == null) {
			MyLog.log("imageFromStack.stack== null");
			return null;
		}
		// MyLog.log("stack bitDepth= "+stack.getBitDepth());
		ImageStack imaStack = stack.getImageStack();
		if (imaStack == null) {
			MyLog.log("imageFromStack.imaStack== null");
			return null;
		}
		if (slice == 0) {
			MyLog.log("imageFromStack.requested slice 0!");
			return null;

		}
		if (slice > stack.getStackSize()) {
			MyLog.log("imageFromStack.requested slice > slices!");
			return null;
		}

		ImageProcessor ipStack = imaStack.getProcessor(slice);

		String titolo = "** " + slice + " **";
		// String titolo = imaStack.getShortSliceLabel(slice);
		String sliceInfo1 = imaStack.getSliceLabel(slice);

		ImagePlus imp = new ImagePlus(titolo, ipStack);
		imp.setProperty("Info", sliceInfo1);
		return imp;
	}

	/**
	 * Inserisce la mask ottenuta all'interno di una immagine, utilizzando il
	 * boundingRectangle
	 * 
	 * @param ipMask
	 * @param r1
	 * @param width
	 * @param height
	 * @return
	 */
	static ImageProcessor patatizeMask(ImageProcessor ipMask, Rectangle r1, int width, int height) {

		ImagePlus impMyPatata = NewImage.createByteImage("Simulata", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ipMyPatata = impMyPatata.getProcessor();
		if (ipMask == null)
			MyLog.waitHere("input ipMask=null");
		if (r1 == null)
			MyLog.waitHere("r1=null");

		short pix1 = 0;
		for (int y = 0; y < r1.height; y++) {
			for (int x = 0; x < r1.width; x++) {
				pix1 = (short) ipMask.getPixelValue(x, y);
				ipMyPatata.putPixelValue(x + r1.x, y + r1.y, pix1);
			}
		}
		return ipMyPatata;

	}

	/**
	 * 
	 * @param oldname
	 * @param newname
	 */
	static void rinominaImmagini(String oldname, String newname) {

		File old1 = new File(oldname);
		File new1 = new File(newname);
		old1.renameTo(new1);
	}

	public static String[] arrayListToArrayString(ArrayList<String> inArrayList) {

		Object[] objArr = inArrayList.toArray();
		String[] outStrArr = new String[objArr.length];
		for (int i1 = 0; i1 < objArr.length; i1++) {
			outStrArr[i1] = objArr[i1].toString();
		}
		return outStrArr;
	}

	public static double[] arrayListToArrayDouble(List<Double> inArrayList) {

		double[] outIntArr = new double[inArrayList.size()];
		int i1 = 0;
		for (Double n : inArrayList) {
			outIntArr[i1++] = n;
		}
		return outIntArr;
	}

	public static double[] concatArrays(double[] array1, double[] array2) {
		double[] result = Arrays.copyOf(array1, array1.length + array2.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	public static ImagePlus removeCalibration(ImagePlus imp1) {

		ImagePlus imp2 = NewImage.createShortImage("uncalibrated", imp1.getWidth(), imp1.getHeight(), 1,
				NewImage.FILL_BLACK);
		ImageProcessor ip2 = imp2.getProcessor();
		short[] pixels1 = truePixels(imp1);
		short[] pixels2 = (short[]) ip2.getPixels();
		for (int i1 = 0; i1 < pixels1.length; i1++) {
			pixels2[i1] = pixels1[i1];
		}
		ip2.resetMinAndMax();
		imp2.updateImage();

		return imp2;
	}

	public static short[] truePixels(ImagePlus imp) {

		ImageProcessor ip = imp.getProcessor();
		Calibration cal = imp.getCalibration();
		short[] pixels = (short[]) ip.getPixelsCopy();
		for (int i1 = 0; i1 < pixels.length; i1++) {
			pixels[i1] = (short) cal.getRawValue(pixels[i1]);
		}
		return (pixels);
	} // truePixels

	/**
	 * esegue l'autoAdjust del contrasto immagine
	 * 
	 * Author Terry Wu, Ph.D., University of Minnesota, <JavaPlugins@yahoo.com>
	 * (from ij.plugin.frame. ContrastAdjuster by Wayne Rasband
	 * <wayne@codon.nih.gov>)*** modified version *** Alberto Duina - Spedali Civili
	 * di Brescia - Servizio di Fisica Sanitaria 2006
	 * 
	 * 
	 * @param imp ImagePlus da regolare
	 * @param ip  ImageProcessor dell'immagine
	 * 
	 */
	public static void autoAdjust2(ImagePlus imp, ImageProcessor ip) {
		double min, max;

		Calibration cal = imp.getCalibration();
		imp.setCalibration(null);
		ImageStatistics stats = imp.getStatistics();
		imp.setCalibration(cal);
		int[] histogram = stats.histogram;
		int threshold = stats.pixelCount / 5000;
		int i = -1;
		boolean found = false;
		do {
			i++;
			found = histogram[i] > threshold;
		} while (!found && i < 255);
		int hmin = i;
		i = 256;
		do {
			i--;
			found = histogram[i] > threshold;
		} while (!found && i > 0);
		int hmax = i;
		if (hmax > hmin) {
			imp.killRoi();
			min = stats.histMin + hmin * stats.binSize;
			max = stats.histMin + hmax * stats.binSize;
			ip.setMinAndMax(min, max);
		}
		Roi roi = imp.getRoi();
		if (roi != null) {
			ImageProcessor mask = roi.getMask();
			if (mask != null)
				ip.reset(mask);
		}
	}

	static void myScalaColori() {

		int countx = 0;
		int county = 0;
		int countz = 0;
		int maxcount = 0;
		int slice = 0;
		double voxMask = 0;
		int width = 128;
		int height = 128;
		int depth = 128;
		int lato = 6;
		int bitdepth = 24;

		ImageStack stack = ImageStack.create(width, height, depth, bitdepth);

		for (int z1 = 1; z1 < depth; z1 = z1 + 4) {
			countz++;
			countx = 0;
			for (int x1 = 0; x1 < width; x1 = x1 + 4) {
				countx++;
				county = 0;
				for (int y1 = 0; y1 < height; y1 = y1 + 4) {
					county++;
					int pippo = Utility.rainbowPixel(countx, county, countz);
					float[] puppo = new float[8 * 8 * 8];
					for (int i1 = 0; i1 < puppo.length; i1++)
						puppo[i1] = (float) pippo;

					stack.setVoxels(z1, x1, y1, 8, 8, 8, puppo);
				}
			}
		}

		ImagePlus imp1 = new ImagePlus("TITOLO", stack);
		imp1.show();

		return;
	}

	private static int rainbowPixel2(double xspan, double yspan) {

		double red = 255. - yspan * 255. * (1.0 + Math.sin(6.3 * xspan)) / 2.;
		double green = 255. - yspan * 255. * (1.0 + Math.cos(6.3 * xspan)) / 2.;
		double blue = 255. - yspan * 255. * (1.0 - Math.sin(6.3 * xspan)) / 2.;

		return ((int) red << 16) + ((int) green << 8) + (int) blue;

	}

	private static int rainbowPixel(int xcount, int ycount, int zcount) {

		double red = 256 / xcount;
		double green = 255 / ycount;
		double blue = 255 / zcount;
		int aux1 = ((int) red << 16) + ((int) green << 8) + (int) blue;
//		MyLog.log("" + xcount + " " + ycount + "  " + zcount + " " + aux1);
		return aux1;
	}

	/**
	 * Calcolo del DVH
	 * 
	 * @param patataMascherata in patata mascherata abbiamo segnale solo nei voxels
	 *                         selezionati nella mask, quindi usando solo i voxels >
	 *                         0 sono a posto
	 * @param ore
	 */
	static ArrayList<ArrayList<Double>> calculateDVH(ImagePlus patataMascherata, int ore) {

		MyLog.log("eseguo calculateDVH " + ore + " ore");
		MyLog.here();

		// patataMascherata.show();

		if (patataMascherata == null)
			MyLog.waitHere("patataMascherata == null");
		ImageStack stack = patataMascherata.getImageStack();
		if (stack == null)
			MyLog.waitHere("stack == null");

		int width = stack.getWidth();
		int height = stack.getHeight();
		int depth = stack.getSize();
		double voxel = 0;
		double[] vetVoxel = null;
		MyLog.here();

		ArrayList<Double> arrList = new ArrayList<Double>();
		for (int z1 = 1; z1 < depth; z1++) {
			for (int x1 = 0; x1 < width; x1++) {
				for (int y1 = 0; y1 < height; y1++) {
					voxel = stack.getVoxel(x1, y1, z1);
					if (voxel > 0) {
						arrList.add(voxel);
					}
				}
			}
		}

		MyLog.logArrayList(arrList, "arrList");
		MyLog.here();

		vetVoxel = Utility.arrayListToArrayDouble(arrList);
		ArrayList<ArrayList<Double>> pippo = calcDVH1(vetVoxel, ore);
		MyLog.here();

		return pippo;
	}

	/**
	 * Parte 1 calcolo DVH
	 * 
	 * @param vetVoxel
	 * @return
	 */
	static ArrayList<ArrayList<Double>> calcDVH1(double[] vetVoxel, int ore) {
		// ---------------------------------

		MyLog.log("eseguo calcDVH1 " + ore + " ore");
		// sort array voxels
		MyLog.here();
		MyLog.logVector(vetVoxel, "vetVoxel input");

		MyLog.here();
		Arrays.sort(vetVoxel);
		MyLog.logVector(vetVoxel, "vetVoxel sortato");
		MyLog.here();

		// --------------------------------
		// rimozione dei doppioni e creazione array
		int n1 = vetVoxel.length;
		double[] temp = new double[n1];
		int j1 = 0;
		for (int i1 = 0; i1 < n1 - 1; i1++) {
			if (Double.compare(vetVoxel[i1], vetVoxel[i1 + 1]) != 0) {
				temp[j1++] = vetVoxel[i1];
			}
		}
		temp[j1++] = vetVoxel[n1 - 1];

		double[] vetRemoved = new double[j1];
		for (int i1 = 0; i1 < j1; i1++) {
			vetRemoved[i1] = temp[i1];
		}
		// --------------------------------
		// conteggio numerosita'
		double[][] vetNum = new double[vetRemoved.length + 1][3];
		for (int i1 = 0; i1 < vetRemoved.length; i1++) {
			vetNum[i1 + 1][0] = vetRemoved[i1]; // parto da posizione [1[0] perche' in seguito vado a forzare 100 in
												// posizione [0][0]
		}
		double aux1 = 0;
		for (int i1 = 0; i1 < vetVoxel.length; i1++) {
			aux1 = vetVoxel[i1];
			for (int i2 = 1; i2 < vetNum.length; i2++) {
				int comp = Double.compare(aux1, vetNum[i2][0]);
				if (comp == 0) {
					vetNum[i2][1] = vetNum[i2][1] + 1.0; // in posizione [][1] effettuo il conteggio numerosita'
				}
			}

		}
		// --------------------------------
		// calcolo della % volume
		for (int i2 = vetNum.length - 1; i2 >= 1; i2--) {
			if (i2 == vetNum.length - 1) {
				vetNum[i2][2] = vetNum[i2][1] / vetVoxel.length * 100;
			} else {
				vetNum[i2][2] = vetNum[i2][1] / vetVoxel.length * 100 + vetNum[i2 + 1][2]; // in posizione [][2]
																							// mettiamo la % volume
			}
		}

		//
		// Questo e'il punto dove vioene forzato il 100% in posizione [0][2]
		//
		vetNum[0][0] = 0;
		vetNum[0][1] = 1;
		vetNum[0][2] = 100;

		MyLog.logMatrixVertical(vetNum, "vetNum");

		// ------------------------------------------------------
		// A questo punto vorrei provare a restituire un Arraylist<ArrayList>, questo
		// potrebbe permettermi di aggiungere i dati ad un ArrayList<ArrayList>>
		// esterno, ma non ne sono troppo sicurobisogna testarlo molto ma molto bene
		// ------------------------------------------------------
		ArrayList<ArrayList<Double>> arrList1 = new ArrayList<ArrayList<Double>>();

		for (int i2 = 0; i2 < vetNum[0].length; i2++) {
			ArrayList<Double> arrList2 = new ArrayList<Double>();
			for (int i1 = 0; i1 < vetNum.length; i1++) {
				arrList2.add(vetNum[i1][i2]);
			}
			arrList1.add(arrList2);
		}

		return arrList1;

	}

	static double[][] calcDVH2(ArrayList<ArrayList<Double>> arrIn1) {

		double[] vetx24 = null;
		double[] vety24 = null;
		double[] vetx48 = null;
		double[] vety48 = null;
		double[] vetx120 = null;
		double[] vety120 = null;

		MyLog.log("eseguoCalcDVH2");
		int len1 = arrIn1.size();
		// separo i vari array in modo da facilitare il successivo lavoro
		// NOTA BENE i diversi array 24, 48 e 120 avranno lunghezze diverse a seconda
		// delle numerosita'

		ArrayList<Double> arrList1 = null;

		arrList1 = arrIn1.get(0);
		vetx24 = Utility.arrayListToArrayDouble(arrList1);
		arrList1 = arrIn1.get(2);
		vety24 = Utility.arrayListToArrayDouble(arrList1);
		arrList1 = arrIn1.get(3);
		vetx48 = Utility.arrayListToArrayDouble(arrList1);
		arrList1 = arrIn1.get(5);
		vety48 = Utility.arrayListToArrayDouble(arrList1);
		arrList1 = arrIn1.get(6);
		vetx120 = Utility.arrayListToArrayDouble(arrList1);
		arrList1 = arrIn1.get(8);
		vety120 = Utility.arrayListToArrayDouble(arrList1);

//		MyPlot.PL11_myPlotMultiple2(vetx24, vety24, vetx48, vety48, vetx120, vety120,
//				"INPUT 24red,48green,120blue", "assexx", "asseyy");

		MyLog.log("=============== dati in input ===============");
		MyLog.logVector(vetx24, "vetX24");
		MyLog.logVector(vety24, "vetY24");
		MyLog.logVector(vetx48, "vetX48");
		MyLog.logVector(vety48, "vetY48");
		MyLog.logVector(vetx120, "vetX120");
		MyLog.logVector(vety120, "vetY120");

		double[][] matout48 = Utility.interpolator(vetx24, vety24, vetx48, vety48);

		double[] vetx48new = new double[matout48.length];
		double[] vety48new = new double[matout48.length];
		for (int i1 = 0; i1 < matout48.length; i1++) {
			vetx48new[i1] = matout48[i1][0];
			vety48new[i1] = matout48[i1][1];
		}
		MyLog.log("=============== dati interpolati 48 ===============");
		MyLog.logVector(vetx48new, "vetx48new");
//		MyLog.logVector(vety48new, "vety48new");

		double[][] matout120 = Utility.interpolator(vetx24, vety24, vetx120, vety120);

		double[] vetx120new = new double[matout120.length];
		double[] vety120new = new double[matout120.length];
		for (int i1 = 0; i1 < matout48.length; i1++) {
			vetx120new[i1] = matout120[i1][0];
			vety120new[i1] = matout120[i1][1];
		}
		MyLog.log("=============== dati interpolati 120 ===============");
		MyLog.logVector(vetx120new, "vetx120new");
//		MyLog.logVector(vety120new, "vety120new");

//		Plot plot3 = MyPlot.PL06_myPlotSingle2(vetx24, vety24, "INPUT24", "grafX24", "grafY24", Color.red);
//		plot3.show();
//		Plot plot4 = MyPlot.PL10_myPlotMultiple2(vetx48, vety48, vetx48new, vety48new, "INTERP48 green=interp", "grafX48new",
//				"Y48new");
//		plot4.show();
//		Plot plot5 = MyPlot.PL10_myPlotMultiple2(vetx120, vety120, vetx120new, vety120new, "INTERP120 green=interp", "grafX120",
//				"grafY120");
//		plot5.show();
//		MyLog.waitHere();
//	

		int a1 = vetx24.length;
		int a2 = vetx48new.length;
		int a3 = vetx120new.length;
		int a4;

		if (a1 > a2)
			a4 = a1;
		else
			a4 = a2;
		if (a4 < a3)
			a4 = a3;

		IJ.log("========================================");

		int f1 = 0;
		int f2 = 0;
		int f3 = 0;

		for (int i1 = 0; i1 < a4; i1++) {
			if (f1 < a1 - 1)
				f1++;
			if (f2 < a2 - 1)
				f2++;
			if (f3 < a3 - 1)
				f3++;

//			String aux1 = String.format("||  %10f |  %10f ||  %10f |  %10f ||  %10f |  %10f ||", vetx24[f1], vety24[f1],
//					vetx48new[f2], vety48new[f2], vetx120new[f3], vety120new[f3]);
//			IJ.log(aux1);

		}
		MyLog.logVector(vety24, "vetY24 prima di rasegotto");
		double[][] matout1 = Utility.rasegotto(vetx24, vetx48new, vetx120new, vety24);

		double[] vetMin = new double[matout1.length];
		double[] vetMax = new double[matout1.length];
		double[] vetY = new double[matout1.length];
		for (int i1 = 0; i1 < matout1.length; i1++) {
			vetMin[i1] = matout1[i1][0];
			vetMax[i1] = matout1[i1][1];
			vetY[i1] = matout1[i1][2];
		}

//		Plot plot6 = MyPlot.PL12_myPlotMultiple(vetMin, vetY, vetMax, vetY, "RASEGATO", "grafX", "grafY");
//		plot6.show();
		MyLog.log("=============== dati rasegati ===============");
		MyLog.logVector(vetMin, "vetMin");
//		MyLog.logVector(vetY, "vetY");
		MyLog.logVector(vetMax, "vetMax");
//		MyLog.logVector(vetY, "vetY");

//		MyLog.waitHere();

		double[][] matout2 = Utility.mediolotto(vetx24, vetx48new, vetx120new, vety24);

		double[] vetMedia = new double[matout2.length];
		double[] vetY2 = new double[matout2.length];
		for (int i1 = 0; i1 < matout2.length; i1++) {
			vetMedia[i1] = matout2[i1][0];
			vetY2[i1] = matout2[i1][1];
		}
		MyLog.logVector(vetMedia, "vetmedia");
//		MyLog.logVector(vetY2, "vetY2 boh");

//		Plot plot7 = myPlotSingle2(vetMedia, vetY, "MEDIA", "grafX", "grafY", Color.RED);
//		plot7.show();

		// verifico che le lunghezze degli array bidimensionali ottenuti in output
		// rimangano uguali
		int len2 = vety24.length;
		int len3 = matout1.length;
		int len4 = matout2.length;
		if (len2 != len3 || len3 != len4)
			MyLog.waitHere("ATTENZIONE lunghezze matrici di output differenti");

		double[][] matout3 = new double[vety24.length][4];
		for (int i1 = 0; i1 < vety24.length; i1++) {
			matout3[i1][0] = matout1[i1][0];
			matout3[i1][1] = matout1[i1][1];
			matout3[i1][2] = matout2[i1][0];
			matout3[i1][3] = matout2[i1][1];
		}

		return matout3;
	}

	static double[] calcoliDVHerrDose2(double[] vetMin, double[] vetMax) {
		double[] vetErrDose = new double[vetMax.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetMax.length; i1++) {
			errDose = (vetMax[i1] - vetMin[i1]) / 2.;
			vetErrDose[i1] = errDose;
		}
		return vetErrDose;
	}

	static double[] calcoliDVHerrSup(double[] vetMed, double[] vetMax) {
		double[] vetErrSup = new double[vetMed.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetMed.length; i1++) {
			errDose = vetMax[i1] - vetMed[i1];
			vetErrSup[i1] = errDose;
		}
		return vetErrSup;
	}

	static double[] calcoliDVHerrInf(double[] vetMed, double[] vetMin) {
		double[] vetErrInf = new double[vetMed.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetMed.length; i1++) {
			errDose = vetMed[i1] - vetMin[i1];
			vetErrInf[i1] = errDose;
		}
		return vetErrInf;
	}

	static double calcoliDVHerrFinale(double[] vetMedia, double[] errMedia) {
		double[] vetErrDose = new double[vetMedia.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetMedia.length; i1++) {
			errDose = (vetMedia[i1] / vetMedia.length) * errMedia[i1];
			vetErrDose[i1] = errDose;
		}
		double sumErrDose = 0;
		for (int i1 = 0; i1 < vetErrDose.length; i1++) {
			sumErrDose = sumErrDose + vetErrDose[i1] * vetErrDose[i1];
		}

		double errOut = Math.sqrt(sumErrDose);

		return errOut;
	}

	static double[] calcoliDVH(double[] vetErrDose, double[] vetMedia, double[] vetY, int percent) {

		double valPercent = 0;
		double errPercent = 0;
		double aux1 = 0;
		// calcolo la differenza tra Y e la percentuale cercata
		double[] vetDelta1 = new double[vetY.length];
		for (int i1 = 0; i1 < vetY.length; i1++) {
			vetDelta1[i1] = Math.abs(vetY[i1] - (double) percent);
			// IJ.log("" + i1 + " vetDelta1= " + vetDelta1[i1]);
		}
		// cerco la posizione del minimo sul vettore differenza
		double min = Double.MAX_VALUE;
		double value;
		int minpos = 0;
		for (int i1 = 0; i1 < vetY.length; i1++) {
			value = vetDelta1[i1];
			if (value < min) {
				min = value;
				minpos = i1;
			}
		}

		MyLog.log("minpos= " + minpos + " / " + vetY.length + " per percentuale= " + percent);

		// ora minpos contiene la posizione del minimo
		valPercent = vetMedia[minpos];
		errPercent = vetErrDose[minpos];
		double[] vetOut = new double[2];
		vetOut[0] = valPercent;
		vetOut[1] = errPercent;
		return vetOut;
	}

	static int searchPercentPosition(double[] vetIn, double[] vetY, int percent) {

		// calcolo la differenza tra Y e la percentuale cercata
		double[] vetDelta1 = new double[vetY.length];
		for (int i1 = 0; i1 < vetY.length; i1++) {
			vetDelta1[i1] = Math.abs(vetY[i1] - (double) percent);
			// IJ.log("" + i1 + " vetDelta1= " + vetDelta1[i1]);
		}
		// cerco la posizione del minimo sul vettore differenza
		double min = Double.MAX_VALUE;
		double value;
		int minpos = 0;
		for (int i1 = 0; i1 < vetY.length; i1++) {
			value = vetDelta1[i1];
			if (value < min) {
				min = value;
				minpos = i1;
			}
		}

		MyLog.log("minpos= " + minpos + " / " + vetY.length + " per percentuale= " + percent);
		return minpos;
	}

	static double[][] samplerDVH(double[] vetIn, double[] vetY) {

		double[][] vetOut = new double[21][2];

		int pos1 = searchPercentPosition(vetIn, vetY, 2);
		int count = 0;
		vetOut[count][0] = vetIn[pos1];
		vetOut[count][1] = 2;

		for (int i1 = 1; i1 < 20; i1++) {
			count = count + 1;
			pos1 = searchPercentPosition(vetIn, vetY, i1 * 5);
			vetOut[count][0] = vetIn[pos1];
			vetOut[count][1] = i1 * 5;
		}
		pos1 = searchPercentPosition(vetIn, vetY, 98);
		count = count + 1;
		vetOut[count][0] = vetIn[pos1];
		vetOut[count][1] = 98;

		return vetOut;
	}

	static double[] calcoliDVHOLD(double[] vetMin, double[] vetMax, double[] vetMedia, double[] vetY, int percent) {

		double[] vetErrDose = new double[vetY.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetY.length; i1++) {
			errDose = (vetMax[i1] - vetMin[i1]) / 2.;
			vetErrDose[i1] = errDose;
		}

		double valPercent = 0;
		double errPercent = 0;
		double aux1 = 0;
		// calcolo la differenza tra Y e la percentuale cercata
		double[] vetDelta1 = new double[vetY.length];
		for (int i1 = 0; i1 < vetY.length; i1++) {
			vetDelta1[i1] = Math.abs(vetY[i1] - (double) percent);
			// IJ.log("" + i1 + " vetDelta1= " + vetDelta1[i1]);
		}
		// cerco la posizione del minimo sul vettore differenza
		double min = Double.MAX_VALUE;
		double value;
		int minpos = 0;
		for (int i1 = 0; i1 < vetY.length; i1++) {
			value = vetDelta1[i1];
			if (value < min) {
				min = value;
				minpos = i1;
			}
		}

		MyLog.log("minpos= " + minpos + " / " + vetY.length + " per percentuale= " + percent);

		// ora minpos contiene la posizione del minimo
		valPercent = vetMedia[minpos];
		errPercent = vetErrDose[minpos];
		double[] vetOut = new double[2];
		vetOut[0] = valPercent;
		vetOut[1] = errPercent;
		return vetOut;
	}

	static double[][] rasegotto(double[] vetx24, double[] vetx48, double[] vetx120, double[] vety24) {

		MyLog.logVector(vety24, "vetY24 dentro rasegotto");

		double matout[][] = new double[vetx24.length][3];
		for (int i1 = 0; i1 < vetx24.length; i1++) {
			matout[i1][0] = minore(vetx24[i1], vetx48[i1], vetx120[i1]);
			matout[i1][1] = maggiore(vetx24[i1], vetx48[i1], vetx120[i1]);
			matout[i1][2] = vety24[i1];
		}
		return matout;
	}

	static double[][] mediolotto(double[] vetx24, double[] vetx48, double[] vetx120, double[] vety24) {

		double matout[][] = new double[vetx24.length][2];
		for (int i1 = 0; i1 < vetx24.length; i1++) {
			matout[i1][0] = media(vetx24[i1], vetx48[i1], vetx120[i1]);
			matout[i1][1] = vety24[i1];
		}
		return matout;
	}

	static double minore(double aa, double bb, double cc) {

		return Math.min(Math.min(aa, bb), cc);
	}

	static double maggiore(double aa, double bb, double cc) {

		return Math.max(Math.max(aa, bb), cc);
	}

	static double media(double aa, double bb, double cc) {

		double media2 = (aa + bb + cc) / 3.0;

		return media2;
	}

	/**
	 * Esegue l'interpolazione sulla x di due grafici, vengono utilizzati i punti su
	 * Y del primo ed interpolati i valori di X del secondo
	 * 
	 * @param vetxAA
	 * @param vetyAA
	 * @param vetxBB
	 * @param vetyBB
	 * @return
	 */
	static double[][] interpolator(double[] vetxAA, double[] vetyAA, double[] vetxBB, double[] vetyBB) {

		ArrayList<Double> arrxCC = new ArrayList<Double>();
		ArrayList<Double> arryCC = new ArrayList<Double>();
		double xAA = 0;
		double xBB = 0;
		double xCC = 0;
		double yAA = 0;
		double yBB = 0;
		double yCC = 0;
		double xPrec = 0;
		double yPrec = 0;
		double xSeg = 0;
		double ySeg = 0;
		int comp1 = 0;
		boolean ok = false;

		arrxCC.add(vetxBB[0]);
		arryCC.add(vetyAA[0]);

		for (int i2 = 1; i2 < vetxAA.length; i2++) {
			yAA = vetyAA[i2];
			xAA = vetxAA[i2];
			ok = false;
			for (int i1 = 1; i1 < vetxBB.length; i1++) {
				yBB = vetyBB[i1];
				xBB = vetxBB[i1];
				comp1 = Double.compare(yAA, yBB);
				// comparo su uguale, escludendo il primo punto (0) sempre 100, che ho copiato
				// ed
				// inserito prima del loop
				if (comp1 == 0 && i1 > 0) {
					arryCC.add(yAA);
					arrxCC.add(xBB);
					ok = true;
					break;
				}
			}
			if (!ok) {
				// le due coordinate Y NON sono uguali, quindi devo usare la Y24, allora cerco
				// Xprec ed Xseg per poi trovare tramite interpolazione la Xnn corrispondente a
				// Y24
				for (int i1 = 1; i1 < vetxBB.length; i1++) {
					xBB = vetxBB[i1];
					yBB = vetyBB[i1];
					comp1 = Double.compare(yAA, yBB);
					if (comp1 > 0 || i1 == vetxBB.length - 1) {
						// se diventa maggiore interrompo
						xSeg = xBB;
						ySeg = yBB;
						break;
					}
					// qui ho la coordinata precedente
					xPrec = xBB;
					yPrec = yBB;

				}
				// MyLog.log("target= " + yAA + " prec= " + yPrec + " seg= " + ySeg);

				yCC = yAA;
				xCC = Utility.linearInterpolationX(xPrec, yPrec, xSeg, ySeg, yCC);
				arrxCC.add(xCC);
				arryCC.add(yCC);
			}

		}

		// MyLog.log("gli array 24 sono di " + vetxAA.length + " elementi, l'array
		// interpolato e' di " + arrxCC.size()
		// + " elemanti");

		double[] vetxCC = Utility.arrayListToArrayDouble(arrxCC);
		double[] vetyCC = Utility.arrayListToArrayDouble(arryCC);
		double[][] matout = new double[vetxCC.length][2];
		for (int i1 = 0; i1 < vetxCC.length; i1++) {
			matout[i1][0] = vetxCC[i1];
			matout[i1][1] = vetyCC[i1];
		}
		return matout;

	}

	static String[] leggiConfig(String target) {

		URL url3 = Utility.class.getResource("Dosimetria_Lu177.class");
		String myString = url3.toString();
		int start = myString.indexOf("plugins");
		int end = myString.lastIndexOf("!");
		if (start < 0 || end < 0)
			return null;
		String myPart1 = myString.substring(start, end);
		int end2 = myPart1.lastIndexOf("/");
		String myPart2 = myPart1.substring(0, end2);
		String myPath = myPart2 + File.separator + target;
		File f1 = new File(myPath);
		if (!f1.isFile())
			return null;
		String[] vetOut = new String[3];
		vetOut[0] = MyLog.readFromLog(myPath, "#001#", "=");
		vetOut[1] = MyLog.readFromLog(myPath, "#002#", "=");
		vetOut[2] = MyLog.readFromLog(myPath, "#003#", "=");

		coordX = parseInt(vetOut[0]);
		coordY = parseInt(vetOut[1]);
		coordZ = parseInt(vetOut[2]);

		return vetOut;
	}

	static int[] leggiCoordinateVoxels(String[] puffi) {

		String strIn = puffi[2];
		String[] vet = strIn.split(",");
		int[] vetOut = new int[vet.length];
		for (int i1 = 0; i1 < vet.length; i1++) {
			vetOut[i1] = Utility.parseInt(vet[i1]);
		}

		return vetOut;
	}

	static boolean leggiLogVoxelsConfig(String[] puffi) {
		String strIn = puffi[1];
		if (strIn.equalsIgnoreCase("SI"))
			return true;
		else
			return false;
	}

	static void loggoVoxels2(ImagePlus impStack, int x1, int y1, int z1) {

		Calibration cal = impStack.getCalibration();
		ImageStack imagestack = impStack.getImageStack();
		double calSignal = imagestack.getVoxel(x1, y1, z1);
		double voxSignal = cal.getCValue(calSignal);

		MyLog.log("#############################################");
		MyLog.log("immagine_" + impStack.getTitle() + "_cal= " + voxSignal + " at " + x1 + ", " + y1 + ", " + z1);
	}

	static void loggoCuxels2(ImagePlus impStack, int x1, int y1, int z1, int lato, int mezzo) {

		ImageStack imagestack = impStack.getImageStack();
		float[] calSignal = imagestack.getVoxels(x1, y1, z1, lato, lato, lato, null);
		MyLog.log("###### CUXELS2  immagine_" + impStack.getTitle() + "_CUBE #############");

		String aux2 = "";
		for (int i3 = x1 - mezzo; i3 < x1 + mezzo + 1; i3++) {
			aux2 = aux2 + "; coordX " + String.format("%03d", i3);
		}

		String aux1 = "";
		int count = y1 - mezzo;
		int count2 = 0;
		int slice = z1 - mezzo;
		MyLog.log("slice " + slice + "+++" + aux2);
		for (int i1 = 0; i1 < calSignal.length - lato - 1; i1 = i1 + lato) {
			aux1 = "";
			for (int i2 = 0; i2 < lato; i2++) {
				aux1 = aux1 + String.format("%+08.2f", calSignal[i1 + i2]) + ";  ";
			}
			MyLog.log("coordY " + String.format("%03d", count) + ";   " + aux1);
			count++;
			count2++;
			if (count2 == lato) {
				count2 = 0;
				slice++;
				if (i1 < (calSignal.length - lato))
					MyLog.log("slice " + slice + "+++" + aux2);
				count = y1 - mezzo;
			}
		}
	}

	static void loggoCuxels3(ImagePlus impStack, int x1, int y1, int z1, int lato, int mezzo) {

		int a1;
		int b1;
		int c1;
		a1 = x1 - mezzo;
		b1 = y1 - mezzo;
		c1 = z1 - mezzo;
		MyLog.log("a1= " + a1 + " b1= " + b1 + " c1= " + c1);

		ImageStack imagestack = impStack.getImageStack();
		float[] calSignal = imagestack.getVoxels(a1, b1, c1, lato, lato, lato, null);

		MyLog.log("###### CUXELS3  immagine_" + impStack.getTitle() + "_CUBE #############");

		String aux2 = "";
		for (int i3 = x1 - mezzo; i3 < x1 + mezzo + 1; i3++) {
			aux2 = aux2 + "; coordX " + String.format("%03d", i3);
		}

		String aux1 = "";
		int count = y1 - mezzo;
		int count2 = 0;
		int slice = z1 - mezzo;
		MyLog.log("slice " + slice + "+++" + aux2);
		for (int i1 = 0; i1 < (calSignal.length - lato) + 1; i1 = i1 + lato) {
			aux1 = "";
			for (int i2 = 0; i2 < lato; i2++) {
				aux1 = aux1 + String.format("%+08.2f", calSignal[i1 + i2]) + ";  ";
			}
			MyLog.log("coordY " + String.format("%03d", count) + ";   " + aux1);
			count++;
			count2++;
			if (count2 == lato) {
				count2 = 0;
				slice++;
				if (i1 < (calSignal.length - lato))
					MyLog.log("slice " + slice + "+++" + aux2);
				count = y1 - mezzo;
			}
		}
	}

	static void loggoCuxels4(ImagePlus impStack, int x1, int y1, int z1, int lato, int mezzo) {

		int a1;
		int b1;
		int c1;
		a1 = x1 - mezzo;
		b1 = y1 - mezzo;
		c1 = z1 - mezzo;
		MyLog.log("a1= " + a1 + " b1= " + b1 + " c1= " + c1);

		ImageStack imagestack = impStack.getImageStack();
		float[] calSignal = imagestack.getVoxels(a1, b1, c1, lato, lato, lato, null);

		MyLog.log("###### CUXELS3  immagine_" + impStack.getTitle() + "_CUBE #############");

		String aux2 = "";
		for (int i3 = x1 - mezzo; i3 < x1 + mezzo + 1; i3++) {
			aux2 = aux2 + "; coordX " + String.format("%03d", i3);
		}

		String aux1 = "";
		int count = y1 - mezzo;
		int count2 = 0;
		int slice = z1 - mezzo;
		MyLog.log("slice " + slice + "+++" + aux2);
		for (int i1 = 0; i1 < (calSignal.length - lato) + 1; i1 = i1 + lato) {
			aux1 = "";
			for (int i2 = 0; i2 < lato; i2++) {
				aux1 = aux1 + String.format("%+.2E", calSignal[i1 + i2]) + ";  ";
			}
			MyLog.log("coordY " + String.format("%03d", count) + ";   " + aux1);
			count++;
			count2++;
			if (count2 == lato) {
				count2 = 0;
				slice++;
				if (i1 < (calSignal.length - lato))
					MyLog.log("slice " + slice + "+++" + aux2);
				count = y1 - mezzo;
			}
		}
	}

	/**
	 * fa in modo che anche le stampe di Java siano coìn il separatore decimale
	 * punto, come per ImageJ
	 */
	void decimalFormatSymbols() {
		Locale.setDefault(Locale.US);
	}

	/**
	 * Questa e'la tabella fornita nel file txt, solo trasformata in file csv e
	 * messe le " e le {} per averla in formato stringa, senza dover rileggere il
	 * file testo
	 * 
	 * @return
	 */
	public static String[] generaTabella() {

		String[] tabellaBella = { "0;0;0;2.26E-01", "0;0;1;3.39E-03", "0;0;2;1.88E-05", "0;0;3;8.43E-06",
				"0;0;4;4.83E-06", "0;0;5;3.14E-06", "0;1;0;3.39E-03", "0;1;1;1.19E-04", "0;1;2;1.51E-05",
				"0;1;3;7.60E-06", "0;1;4;4.56E-06", "0;1;5;3.04E-06", "0;2;0;1.88E-05", "0;2;1;1.51E-05",
				"0;2;2;9.48E-06", "0;2;3;5.90E-06", "0;2;4;3.89E-06", "0;2;5;2.74E-06", "0;3;0;8.43E-06",
				"0;3;1;7.60E-06", "0;3;2;5.90E-06", "0;3;3;4.32E-06", "0;3;4;3.14E-06", "0;3;5;2.35E-06",
				"0;4;0;4.83E-06", "0;4;1;4.56E-06", "0;4;2;3.89E-06", "0;4;3;3.14E-06", "0;4;4;2.50E-06",
				"0;4;5;1.98E-06", "0;5;0;3.14E-06", "0;5;1;3.04E-06", "0;5;2;2.74E-06", "0;5;3;2.35E-06",
				"0;5;4;1.98E-06", "0;5;5;1.61E-06", "1;0;0;3.39E-03", "1;0;1;1.19E-04", "1;0;2;1.51E-05",
				"1;0;3;7.60E-06", "1;0;4;4.56E-06", "1;0;5;3.04E-06", "1;1;0;1.19E-04", "1;1;1;2.82E-05",
				"1;1;2;1.26E-05", "1;1;3;6.94E-06", "1;1;4;4.32E-06", "1;1;5;2.93E-06", "1;2;0;1.51E-05",
				"1;2;1;1.26E-05", "1;2;2;8.46E-06", "1;2;3;5.49E-06", "1;2;4;3.72E-06", "1;2;5;2.64E-06",
				"1;3;0;7.60E-06", "1;3;1;6.94E-06", "1;3;2;5.49E-06", "1;3;3;4.09E-06", "1;3;4;3.04E-06",
				"1;3;5;2.29E-06", "1;4;0;4.56E-06", "1;4;1;4.32E-06", "1;4;2;3.72E-06", "1;4;3;3.04E-06",
				"1;4;4;2.42E-06", "1;4;5;1.92E-06", "1;5;0;3.04E-06", "1;5;1;2.93E-06", "1;5;2;2.64E-06",
				"1;5;3;2.29E-06", "1;5;4;1.92E-06", "1;5;5;1.60E-06", "2;0;0;1.88E-05", "2;0;1;1.51E-05",
				"2;0;2;9.48E-06", "2;0;3;5.90E-06", "2;0;4;3.89E-06", "2;0;5;2.74E-06", "2;1;0;1.51E-05",
				"2;1;1;1.26E-05", "2;1;2;8.46E-06", "2;1;3;5.49E-06", "2;1;4;3.72E-06", "2;1;5;2.64E-06",
				"2;2;0;9.48E-06", "2;2;1;8.46E-06", "2;2;2;6.36E-06", "2;2;3;4.56E-06", "2;2;4;3.28E-06",
				"2;2;5;2.42E-06", "2;3;0;5.90E-06", "2;3;1;5.49E-06", "2;3;2;4.56E-06", "2;3;3;3.57E-06",
				"2;3;4;2.74E-06", "2;3;5;2.12E-06", "2;4;0;3.89E-06", "2;4;1;3.72E-06", "2;4;2;3.28E-06",
				"2;4;3;2.74E-06", "2;4;4;2.23E-06", "2;4;5;1.80E-06", "2;5;0;2.74E-06", "2;5;1;2.64E-06",
				"2;5;2;2.42E-06", "2;5;3;2.12E-06", "2;5;4;1.80E-06", "2;5;5;1.51E-06", "3;0;0;8.43E-06",
				"3;0;1;7.60E-06", "3;0;2;5.90E-06", "3;0;3;4.32E-06", "3;0;4;3.14E-06", "3;0;5;2.35E-06",
				"3;1;0;7.60E-06", "3;1;1;6.94E-06", "3;1;2;5.49E-06", "3;1;3;4.09E-06", "3;1;4;3.04E-06",
				"3;1;5;2.29E-06", "3;2;0;5.90E-06", "3;2;1;5.49E-06", "3;2;2;4.56E-06", "3;2;3;3.57E-06",
				"3;2;4;2.74E-06", "3;2;5;2.12E-06", "3;3;0;4.32E-06", "3;3;1;4.09E-06", "3;3;2;3.57E-06",
				"3;3;3;2.92E-06", "3;3;4;2.35E-06", "3;3;5;1.88E-06", "3;4;0;3.14E-06", "3;4;1;3.04E-06",
				"3;4;2;2.74E-06", "3;4;3;2.35E-06", "3;4;4;1.97E-06", "3;4;5;1.63E-06", "3;5;0;2.35E-06",
				"3;5;1;2.29E-06", "3;5;2;2.12E-06", "3;5;3;1.88E-06", "3;5;4;1.63E-06", "3;5;5;1.39E-06",
				"4;0;0;4.83E-06", "4;0;1;4.56E-06", "4;0;2;3.89E-06", "4;0;3;3.14E-06", "4;0;4;2.50E-06",
				"4;0;5;1.98E-06", "4;1;0;4.56E-06", "4;1;1;4.32E-06", "4;1;2;3.72E-06", "4;1;3;3.04E-06",
				"4;1;4;2.42E-06", "4;1;5;1.92E-06", "4;2;0;3.89E-06", "4;2;1;3.72E-06", "4;2;2;3.28E-06",
				"4;2;3;2.74E-06", "4;2;4;2.23E-06", "4;2;5;1.80E-06", "4;3;0;3.14E-06", "4;3;1;3.04E-06",
				"4;3;2;2.74E-06", "4;3;3;2.35E-06", "4;3;4;1.97E-06", "4;3;5;1.63E-06", "4;4;0;2.50E-06",
				"4;4;1;2.42E-06", "4;4;2;2.23E-06", "4;4;3;1.97E-06", "4;4;4;1.70E-06", "4;4;5;1.44E-06",
				"4;5;0;1.98E-06", "4;5;1;1.92E-06", "4;5;2;1.80E-06", "4;5;3;1.63E-06", "4;5;4;1.44E-06",
				"4;5;5;1.25E-06", "5;0;0;3.14E-06", "5;0;1;3.04E-06", "5;0;2;2.74E-06", "5;0;3;2.35E-06",
				"5;0;4;1.98E-06", "5;0;5;1.61E-06", "5;1;0;3.04E-06", "5;1;1;2.93E-06", "5;1;2;2.64E-06",
				"5;1;3;2.29E-06", "5;1;4;1.92E-06", "5;1;5;1.60E-06", "5;2;0;2.74E-06", "5;2;1;2.64E-06",
				"5;2;2;2.42E-06", "5;2;3;2.12E-06", "5;2;4;1.80E-06", "5;2;5;1.51E-06", "5;3;0;2.35E-06",
				"5;3;1;2.29E-06", "5;3;2;2.12E-06", "5;3;3;1.88E-06", "5;3;4;1.63E-06", "5;3;5;1.39E-06",
				"5;4;0;1.98E-06", "5;4;1;1.92E-06", "5;4;2;1.80E-06", "5;4;3;1.63E-06", "5;4;4;1.44E-06",
				"5;4;5;1.25E-06", "5;5;0;1.61E-06", "5;5;1;1.60E-06", "5;5;2;1.51E-06", "5;5;3;1.39E-06",
				"5;5;4;1.25E-06", "5;5;5;1.12E-06" };
		return tabellaBella;
	}

	/**
	 * Estrae da tabellaBella i tre puntatori i,j,k e li mette in un array
	 * bidimensionale che e' appaiato a quello degli Svalues
	 * 
	 * @param tabellaBella
	 * @return
	 */
	static int[][] tabellaPuntatori(String[] tabellaBella) {

		String[] parsed = null;
		String riga = "";
		int[][] tabPuntatori = new int[tabellaBella.length][3];
		for (int i1 = 0; i1 < tabellaBella.length; i1++) {
			riga = tabellaBella[i1];
			parsed = riga.split(";");
			tabPuntatori[i1][0] = Utility.parseInt(parsed[0]);
			tabPuntatori[i1][1] = Utility.parseInt(parsed[1]);
			tabPuntatori[i1][2] = Utility.parseInt(parsed[2]);
		}
		return tabPuntatori;
	}

	/**
	 * Estrae da tabellaBella gli Svalues e li mette in un array double, che e'
	 * appaiato a quello dei puntatori
	 * 
	 * @param tabellaBella
	 * @return
	 */
	static double[] tabellaSValues(String[] tabellaBella) {

		String[] parsed = null;
		String riga = "";
		double[] vetValori = new double[tabellaBella.length];
		for (int i1 = 0; i1 < tabellaBella.length; i1++) {
			riga = tabellaBella[i1];
			parsed = riga.split(";");
			vetValori[i1] = Double.parseDouble(parsed[3]);
		}
		return vetValori;
	}

	/**
	 * Date le coordinate del voxel centrale del walking cube e il lato, calcola il
	 * valore da assegnare al pixel corrispondente in output
	 * 
	 * @param vetVoxels
	 * @param vetSvalues
	 * @param x9
	 * @param y9
	 * @param z9
	 * @param mezzo
	 * @param vetAuxInputs
	 * @return
	 */
	public static double myProcessVoxels11x11(float[] vetVoxels, float[] vetSvalues, int x9, int y9, int z9, int mezzo,
			double[] vetAuxInputs, boolean log2) {

		double voxel = 0;
		double svalue = 0;
		double matilde = 0;
		double acqDuration = vetAuxInputs[0];
		double fatCal = vetAuxInputs[1];
		double deltaT = vetAuxInputs[2];
		double par_a = vetAuxInputs[3];
		boolean log1 = false;
		double aux1 = 0;

		int offset = -1;
		double patatadolce = 0;

		for (int i9 = -mezzo; i9 <= mezzo; i9++) {
			for (int j9 = -mezzo; j9 <= mezzo; j9++) {
				for (int k9 = -mezzo; k9 <= mezzo; k9++) {
					if (i9 == 0 && j9 == 0 && k9 == 0)
						log1 = log2;
					else
						log1 = false;

					// ###############################################################################à
					offset++;
					voxel = vetVoxels[offset];
					matilde = Utility.matildeSingleVoxel(voxel, acqDuration, fatCal, deltaT, par_a, log1);
					svalue = vetSvalues[offset];
					aux1 = matilde * svalue;
					if (log1)
						MyLog.log("matilde= " + matilde + "\nsvalue= " + svalue + "\npatata= " + aux1);
					patatadolce = patatadolce + aux1;
					// ##################################################################################
				}
			}
		}
		return patatadolce;
	}

	/**
	 * Genera un cubo 11x11x11 con i corretti valori di S_value
	 * 
	 * @return
	 */
	public static ImagePlus inCubo() {

		String[] tabellaStringhe = Utility.generaTabella();
		int[][] tabellaBella = Utility.tabellaPuntatori(tabellaStringhe);
		double[] valuesBella = Utility.tabellaSValues(tabellaStringhe);
		int lato = Utility.latoCubo();
		int mezzo = (lato - 1) / 2;
		int bitdepth = 32;
		ImageStack stackRubik = ImageStack.create(lato, lato, lato, bitdepth);
		double svalue = 0;
		int counti = -1;
		int countj = -1;
		int countk = -1;
		for (int i9 = -mezzo; i9 <= mezzo; i9++) {
			counti++;
			countj = -1;
			for (int j9 = -mezzo; j9 <= mezzo; j9++) {
				countj++;
				countk = -1;
				for (int k9 = -mezzo; k9 <= mezzo; k9++) {
					countk++;
					svalue = Utility.searchValue(tabellaBella, valuesBella, i9, j9, k9);
					stackRubik.setVoxel(counti, countj, countk, svalue);
				}
			}
		}
		ImagePlus impSvalue = new ImagePlus("SRubik11x11x11", stackRubik);
		return impSvalue;

	}

	public static double matildeSingleVoxel(double voxSignal, double acqDuration, double fatCal, double deltaT,
			double par_a, boolean log1) {

		double ahhVoxel = voxSignal / (acqDuration * fatCal);
		double aVoxel = ahhVoxel / Math.exp(-(par_a * deltaT));
		double aTildeVoxel = (aVoxel / par_a) * 3600;
		if (log1)
			MyLog.log("matildeSingleVoxel inputs: \nvoxSignal= " + voxSignal + "\nacqDuration= " + acqDuration
					+ "\nfatCal= " + fatCal + "\ndeltaT= " + deltaT + "\npar_a= " + par_a
					+ "\nmatildeSingleVoxel calculations: \nahhVoxel= " + ahhVoxel + "\naVoxel= " + aVoxel
					+ "\naTildeVoxel= " + aTildeVoxel);

		return aTildeVoxel;
	}

	public static double searchValue(int[][] tabellaBella, double[] valuesBella, int i9, int j9, int k9) {

		int i8 = Math.abs(i9);
		int j8 = Math.abs(j9);
		int k8 = Math.abs(k9);

		double sValue = 0;

		for (int i1 = 0; i1 < tabellaBella.length; i1++) {
			if (tabellaBella[i1][2] == k8) {
				if (tabellaBella[i1][1] == j8) {
					if (tabellaBella[i1][0] == i8) {
						sValue = valuesBella[i1];
//						MyLog.log("search "+i9+" "+j9+" "+k9+ " trovato a = "+i8+" "+j8+" "+k8 +" value= "+sValue);
						break;
					}
				}
			}
		}

		return sValue;
	}

	public static void matrixEnlarger(ImagePlus impMatrix) {

		ImageStack stack = impMatrix.getStack();

	}

	/**
	 * Interpolazione lineare di un punto su di un segmento
	 * 
	 * @param x0 coordinata X inizio
	 * @param y0 coordinata Y inizio
	 * @param x1 coordinata X fine
	 * @param y1 coordinata X fine
	 * @param x2 valore X di cui calcolare la Y
	 * @return valore Y calcolato
	 */
	public static double linearInterpolationY(double x0, double y0, double x1, double y1, double x2) {

		double y2 = y0 + (((x2 - x0) * y1 - (x2 - x0) * y0) / (x1 - x0));
		if (y2 == Double.NaN) {
			MyLog.log(
					"NaN_1 linearInterpolationX x0= " + x0 + " y0= " + y0 + " x1= " + x1 + " y1= " + y1 + " x2= " + x2);
			MyLog.waitHere("NaN_1");
		}

		return y2;
	}

	/**
	 * Interpolazione lineare di un punto su di un segmento
	 * 
	 * @param x0 coordinata X inizio
	 * @param y0 coordinata Y inizio
	 * @param x1 coordinata X fine
	 * @param y1 coordinata X fine
	 * @param y2 valore Y di cui calcolare la X
	 * @return valore X calcolato
	 */
	public static double linearInterpolationX(double x0, double y0, double x1, double y1, double y2) {

		double x2 = x0 + (((y2 - y0) * x1 - (y2 - y0) * x0) / (y1 - y0));

		if (Double.isNaN(x2)) {
			MyLog.log(
					"NaN_2 linearInterpolationX x0= " + x0 + " y0= " + y0 + " x1= " + x1 + " y1= " + y1 + " y2= " + y2);
			MyLog.waitHere("NaN_2");
		}
		if (Double.compare(y1, x0) == 0) {
			MyLog.log("ZERO_2 linearInterpolationX x0= " + x0 + " y0= " + y0 + " x1= " + x1 + " y1= " + y1 + " y2= "
					+ y2);
			MyLog.waitHere("ZERO_2");
		}

		return x2;
	}

}
