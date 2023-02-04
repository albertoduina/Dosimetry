package Dosimetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.ColorModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.plugin.DICOM;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.DicomTools;
import ij.util.FontUtil;
import ij.util.Tools;

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

	/**
	 * Legge tutte le linee di un file testo e le restituisce come vettore di
	 * stringhe
	 * 
	 * @param path1 path del file, completo di nome
	 * @return vettore stringhe
	 */
	public static String[] readSimpleText2(String path1) {

		List<String> out1 = null;
		try {
			out1 = Files.readAllLines(Path.of(path1));
			// MyLog.log("lette= " + out1.size() + " linee");
		} catch (IOException e) {
			MyLog.log("errore lettura " + path1);
			e.printStackTrace();
		}
		String[] out2 = out1.toArray(new String[0]);
		return out2;
	}

	/**
	 * Legge tutte le linee di un file testo e le restituisce come vettore di
	 * stringhe
	 * 
	 * @param path1 path del file, completo di nome
	 * @return vettore stringhe
	 */
	public static String[] readSimpleText(String path1) {

		List<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path1));
		} catch (FileNotFoundException e) {
			MyLog.waitHere("fileNotFound error= " + path1);
			e.printStackTrace();
		}
		String line = null;
		try {
			line = br.readLine();
			while (line != null) {
				lines.add(line);
				line = br.readLine();
			}
			br.close();

		} catch (IOException e) {
			MyLog.waitHere("reading error= " + path1);
			e.printStackTrace();
		}

		String[] out2 = lines.toArray(new String[0]);
		return out2;
	}

	/**
	 * Inizializza il file di log cancellando se esiste e scrivendoci INIZIO
	 * 
	 * @param path indirizzo log da utilizzare
	 */
	public static void logInit(String path) {
		File f1 = new File(path);
		if (f1.exists()) {
			f1.delete();
		}
		logAppend(path, "---- INIZIO ---------");
	}

	/**
	 * Scrive FINE nel log
	 * 
	 * @param path PATH DEL LOG
	 */
	public static void logEnd(String path) {
		logAppend(path, "---- FINE ---------");
	}

	/**
	 * Cancellazione del file, attenzione devono essere prima chiusi BufferedReader
	 * e BufferedWriter
	 * 
	 * @param path indirizzo log da utilizzare
	 */
	public static void logDeleteSingle(String path) {
		File f1 = new File(path);
		if (f1.exists()) {
			f1.delete();
		}
		if (f1.exists()) {
			Utility.dialogErrorMessage_LP06(path);
		}
	}

	/**
	 * Elimina i tag duplicati nel file
	 * 
	 * @param path1
	 */
	static void logDedupe(String path1) {
		ArrayList<String> inArrayList = new ArrayList<String>();
		ArrayList<String> outArrayList = new ArrayList<String>();
		String line1 = "";
		String line2 = "";
		String line4 = "";
		String tag1 = "";
		String tag2 = "";

		MyLog.log("eseguo dedupeLog");
		try {
			BufferedReader file1 = new BufferedReader(new FileReader(path1));
			while ((line1 = file1.readLine()) != null) {
				inArrayList.add(line1);
			}
			file1.close();
			new File(path1).delete();
			//
			// questo si chiama ALGORITMO DEL TROGLODITA, IN QUESTO CASO UN INGENNIERE BRAO
			// FESS avrebbe usato una HashList, ma modestamente nel far cazzate non mi batte
			// nessuno, infatti questa routine sbaglia spudoratamente !!
			//
			boolean dupe = false;
			for (int i1 = inArrayList.size() - 1; i1 >= 0; i1--) {
				// partendo dal fondo leggo riga per riga
				line2 = inArrayList.get(i1);
				tag1 = line2.substring(0, 5);
				dupe = false;
				for (String line3 : outArrayList) {
					// leggo i tag di tutte le linee gia'presenti nell'array di output
					tag2 = line3.substring(0, 5);
					if (tag1.equals(tag2)) {
						// se i tag sono uguali evviva, abbiamo un duplicato!
						dupe = true;
					}
				}
				if (!dupe) {
					// se non ho per le mani duplicato lo scrivo nell'array di output, che in questo
					// modo viene scritto all'incontrario!
					outArrayList.add(line2);
				}
			}

			BufferedWriter out = new BufferedWriter(new FileWriter(path1, true));
			for (int i1 = outArrayList.size() - 1; i1 >= 0; i1--) {
				// in questo modo ribalto l'array di output
				line4 = outArrayList.get(i1);
				out.write(line4);
				out.newLine();
			}
			out.close();
		} catch (Exception e) {
			MyLog.log("dedupe DISASTER");
			System.out.println("DEDUPE errore lettura/scrittura file " + path1);
		}
	}

	/**
	 * Se nel file esiste gia'una linea col tag, essa viene sostituita, se la linea
	 * non esiste, essa viene aggiunta alla fine
	 * 
	 * @param path1   path del log
	 * @param tag     tag da sostituire
	 * @param newline linea da inserire (completa di tag)
	 */
	public static void logModify(String path1, String tag, String newline) {

		boolean ok = true;
		try {
			BufferedReader file = new BufferedReader(new FileReader(path1));
			StringBuffer inputBuffer = new StringBuffer();
			String line;
			// lettura
			while ((line = file.readLine()) != null) {
				if (line.contains(tag)) {
					line = newline;
					ok = false;
				}
				inputBuffer.append(line);
				inputBuffer.append('\n');
			}
			if (ok) {
				inputBuffer.append(newline);
				inputBuffer.append('\n');
			}
			file.close();

			// riscrittura
			FileOutputStream fileOut = new FileOutputStream(path1);
			fileOut.write(inputBuffer.toString().getBytes());
			fileOut.close();

		} catch (Exception e) {
			System.out.println("errore lettura/scrittura file " + path1);
		}
	}

	/**
	 * Se nel file esiste gia'una linea col tag, essa viene cancellata
	 * 
	 * @param path1
	 * @param tag
	 */
	public static void logRemoveLine(String path1, String tag) {

		try {
			BufferedReader file = new BufferedReader(new FileReader(path1));
			StringBuffer inputBuffer = new StringBuffer();
			String line;
			// lettura
			while ((line = file.readLine()) != null) {
				if (line.contains(tag)) {
				} else {
					inputBuffer.append(line);
					inputBuffer.append('\n');
				}
			}
			file.close();

			// riscrittura
			FileOutputStream fileOut = new FileOutputStream(path1);
			fileOut.write(inputBuffer.toString().getBytes());
			fileOut.close();

		} catch (Exception e) {
			System.out.println("errore lettura/scrittura file " + path1);
		}
	}

	/**
	 * Copia tutti i dati dal log volatile.txt al log permanente.txt
	 * 
	 * @param permFile indirizzo log permanente da utilizzare
	 * @param tmpFile  indirizzo log temporaneo da utilizzare
	 */
	public static void logMove(String permFile, String tmpFile) {
		BufferedWriter out;
		BufferedReader in;
		String str = "";
		try {
			out = new BufferedWriter(new FileWriter(permFile, true));
			in = new BufferedReader(new FileReader(tmpFile));
			// out.newLine();
			while ((str = in.readLine()) != null) {
				out.write(str);
				out.newLine();
			}
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logDeleteSingle(tmpFile);
	}

	/**
	 * Scrive una riga nel log
	 * 
	 * @param path  path indirizzo log da utilizzare
	 * @param linea stringa da inserire
	 */
	public static void logAppend(String path, String linea) {

		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(path, true));
			out.write(linea);
			out.newLine();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Lettura di un tag dal log
	 * 
	 * @param path1
	 * @param code1
	 * @param separator
	 * @return
	 */
	static double readDoubleFromLog(String path1, String code1, String separator) {

		// leggo una stringa dal log
		String[] vetText = Utility.readSimpleText(path1);
		String[] vetAux1;
		String out1 = null;
		if (vetText.length > 0) {
			for (int i1 = 0; i1 < vetText.length; i1++) {
				if (vetText[i1].contains(code1)) {
					vetAux1 = vetText[i1].split(separator);
					out1 = vetAux1[1].trim();
				}
			}
		}

		return Double.parseDouble(out1);
	}

	/**
	 * Lettura di un tag dal log
	 * 
	 * @param path1
	 * @param code1
	 * @param separator
	 * @return
	 */
	static String readFromLog(String path1, String code1, String separator) {

		if (path1 == null)
			MyLog.waitHere("path1==null");
		String[] vetText = Utility.readSimpleText(path1);
		if (vetText == null)
			MyLog.waitHere("vetText==null");

		String[] vetAux1;
		String out1 = null;
		boolean trovato = false;
		if (vetText.length > 0) {
			for (int i1 = 0; i1 < vetText.length; i1++) {
				if (vetText[i1].contains(code1)) {
					vetAux1 = vetText[i1].split(separator);
					out1 = vetAux1[1].trim();
					trovato = true;
				}
			}
		}
		if (!trovato) {
			MyLog.waitHere("non trovo " + code1);
			return null;
		}
		return out1;
	}

	/**
	 * Lettura di un tag dal log
	 * 
	 * @param path1
	 * @param code1
	 * @param separator
	 * @return
	 */
	static String readFromLog(String path1, String code1, String separator, boolean error) {

		if (path1 == null)
			MyLog.waitHere("path1==null");
		String[] vetText = Utility.readSimpleText(path1);
		if (vetText == null)
			MyLog.waitHere("vetText==null");

		String[] vetAux1;
		String out1 = null;
		boolean trovato = false;
		if (vetText.length > 0) {
			for (int i1 = 0; i1 < vetText.length; i1++) {
				if (vetText[i1].contains(code1)) {
					vetAux1 = vetText[i1].split(separator);
					out1 = vetAux1[1].trim();
					trovato = true;
				}
			}
		}
		if (!trovato) {
			if (!error)
				MyLog.waitHere("non trovo " + code1);
			return null;
		}
		return out1;
	}

	/**
	 * Restituisce l'intera linea del log per il tag
	 * 
	 * @param path1
	 * @param code1
	 * @return
	 */
	static String readFromLog(String path1, String code1) {

		// leggo una stringa dal log
		String[] vetText = Utility.readSimpleText(path1);
		if (vetText.length > 0) {
			for (int i1 = 0; i1 < vetText.length; i1++) {
				if (vetText[i1].contains(code1))
					return vetText[i1];
			}
		}
		return null;
	}

	/**
	 * Copia da log sorgente a destinazione un range di tag
	 * 
	 * @param pathSorgente
	 * @param pathDestinazione
	 * @param start
	 * @param end
	 */
	static void logCopyRange(String pathSorgente, String pathDestinazione, int start, int end) {

		String aux1 = "";
		String aux2 = "";
		for (int i1 = start; i1 <= end; i1++) {
			aux1 = "#" + String.format("%03d", i1) + "#";
			aux2 = readFromLog(pathSorgente, aux1);
			if (aux2 != null) {
				Utility.logAppend(pathDestinazione, aux2);
			}
		}

	}

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
	 * Effettua il plot dei punti trovati, SENZA mostrare alcun fit
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static void MIRD_pointsPlotter(double[] vetX, double[] vetY, boolean[] selected, String title) {

		double[] minMaxX = Tools.getMinMax(vetX);
		double[] minMaxY = Tools.getMinMax(vetY);
		boolean[] neglected = { true, true, true };
		double xmin = 0;
		double xmax = minMaxX[1] * 1.1;
		double ymin = -1.0;
		double ymax = minMaxY[1] * 1.1;
		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;
		double[] xx = new double[1];
		double[] yy = new double[1];

		if (selected == null)
			selected = neglected;

		Plot plot1 = new Plot(title, "ore dalla somministrazione", "attivita' MBq");
		plot1.setLineWidth(2);
		for (int i1 = 0; i1 < selected.length; i1++) {
			if (selected[i1]) {
				plot1.setColor(Color.red);
				xx[0] = vetX[i1];
				yy[0] = vetY[i1];
				plot1.add("circle", xx, yy);
			} else {
				plot1.setColor(Color.black);
				xx[0] = vetX[i1];
				yy[0] = vetY[i1];
				plot1.add("circle", xx, yy);
			}

		}
		plot1.setFrameSize(PLOT_WIDTH, PLOT_HEIGHT);
		plot1.setLimits(xmin, xmax, ymin, ymax);
		plot1.show();
		return;

	}

	/**
	 * Effettua il plot dei punti (selezionati e non selezionati) e mostra il fit
	 * fatto sui punti selezionati
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static Plot MIRD_curvePlotterSpecialImageJ(CurveFitter cf, double[] vetx, double[] vety, boolean[] selected) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		int npoints = 1000;
		if (npoints < vetx.length)
			npoints = vetx.length; // or 2*x.length-1; for 2 values per data point
		if (npoints > 1000)
			npoints = 1000;
		double[] a = Tools.getMinMax(vetx);
		double xmin = a[0];
		double xmax = a[1] * 1.5;
		xmin = 0;
		npoints = 1000;
		double[] b = Tools.getMinMax(vety);
		double ymin = b[0];
		double ymax = b[1] * 1.1; // y range of data points
		ymin = 0;
		double[] px = new double[npoints];
		double[] py = new double[npoints];
		double inc = (xmax - xmin) / (npoints - 1);
		double tmp = xmin;
		for (int i = 0; i < npoints; i++) {
			px[i] = tmp;
			tmp += inc;
		}
		double[] params = cf.getParams();
		for (int i = 0; i < npoints; i++)
			py[i] = cf.f(params, px[i]);
		a = Tools.getMinMax(py);
		double dataRange = ymax - ymin;
		ymin = Math.max(ymin - dataRange, Math.min(ymin, a[0])); // expand y range for curve, but not too much
		ymax = Math.min(ymax + dataRange, Math.max(ymax, a[1]));

		Plot plot2 = new Plot("PLOT IMAGEJ", "ore dalla somministrazione", "attivita' MBq");
		plot2.setLineWidth(2);
		plot2.setColor(Color.BLUE);
		plot2.add("line", px, py);
		plot2.setLimits(xmin, xmax, ymin, ymax);
		plot2.setColor(Color.RED);
		double[] xx = new double[1];
		double[] yy = new double[1];

		for (int i1 = 0; i1 < selected.length; i1++) {
			if (selected[i1]) {
				plot2.setColor(Color.red);
				xx[0] = vetx[i1];
				yy[0] = vety[i1];
				plot2.add("circle", xx, yy);
			} else {
				plot2.setColor(Color.black);
				xx[0] = vetx[i1];
				yy[0] = vety[i1];
				plot2.add("circle", xx, yy);
			}

		}
		StringBuffer legend = new StringBuffer(100);
		legend.append(cf.getName());
		legend.append('\n');
		legend.append(cf.getFormula());
		legend.append('\n');
		double[] p = cf.getParams();
		int n = cf.getNumParams();
		char pChar = 'a';
		for (int i = 0; i < n; i++) {
			legend.append(pChar + " = " + IJ.d2s(p[i], 5, 9) + '\n');
			pChar++;
		}
		legend.append("R^2 = " + IJ.d2s(cf.getRSquared(), 4));
		legend.append('\n');
		plot2.addLabel(0.8, 0.1, legend.toString());
		plot2.setFrameSize(PLOT_WIDTH, PLOT_HEIGHT);
		plot2.setColor(Color.BLUE);
		plot2.show();
		return plot2;
	}

	/**
	 * Effettua il plot dei punti trovati, SENZA mostrare alcun fit
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static void MIRD_curvePlotterSpecialFlanagan(Regression reg, double[] x, double[] y) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		int npoints = 1000;
		if (npoints < x.length)
			npoints = x.length; // or 2*x.length-1; for 2 values per data point
		if (npoints > 1000)
			npoints = 1000;
		double[] a = Tools.getMinMax(x);
		double xmin = a[0], xmax = a[1] * 1.5;
		xmin = 0;
		npoints = 1000;
		double[] b = Tools.getMinMax(y);
		double ymin = b[0], ymax = b[1] * 1.1; // y range of data points
		ymin = 0;
		double[] px = new double[npoints];
		double[] py = new double[npoints];
		double inc = (xmax - xmin) / (npoints - 1);
		double tmp = xmin;
		for (int i = 0; i < npoints; i++) {
			px[i] = tmp;
			tmp += inc;
		}
		double[] params = reg.getBestEstimates();

		double aux0 = 0;
		double aux1 = 0;

		aux0 = params[1];
		aux1 = params[0];

		MyLog.log("aux0= " + aux0 + " aux1= " + aux1);
		for (int i = 0; i < npoints; i++) {
			py[i] = aux0 * Math.exp(aux1 * px[i]);
			// MyLog.log("px[" + i + "]= " + px[i] + " py[" + i + "]= " + py[i]);
		}
		a = Tools.getMinMax(py);
		double dataRange = ymax - ymin;
		ymin = Math.max(ymin - dataRange, Math.min(ymin, a[0])); // expand y range for curve, but not too much
		ymax = Math.min(ymax + dataRange, Math.max(ymax, a[1]));

		Plot plot = new Plot("PLOT FLANAGAN", "ore dalla somministrazione", "attivita' MBq");
		plot.setLineWidth(2);
		plot.setColor(Color.GREEN);
		plot.add("line", px, py);
		plot.setLimits(xmin, xmax, ymin, ymax);
		plot.setColor(Color.RED);
		plot.add("circle", x, y);
		plot.setColor(Color.GREEN);
//		plot.addLabel(0.02, 0.1, legend.toString());
		plot.setFrameSize(PLOT_WIDTH, PLOT_HEIGHT);
		plot.show();

	}

	/**
	 * Effettua il plot dei punti trovati, mostrando anche il FIT
	 * 
	 * @param vetX
	 * @param vetY
	 * @param params
	 * @param npoints
	 */
	static void MIRD_curvePlotter(double[] vetX, double[] vetY, double[] params, int npoints) {

		double[] minMaxX = Tools.getMinMax(vetX);
		double[] minMaxY = Tools.getMinMax(vetY);
		double xmin = 0;
		double xmax = minMaxX[1] * 1.1;
		double ymin = -1.0;
		double ymax = minMaxY[1] * 1.1;
		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;
		double[] px = new double[npoints];
//		double[] py = new double[npoints];

		double inc = (xmax - xmin) / (npoints - 1);
		double tmp = minMaxX[0];
		for (int i = 0; i < npoints; i++) {
			px[i] = (float) tmp;
			tmp += inc;
		}

		Plot plot1 = new Plot("Punti", "ore dalla somministrazione", "attivita' MBq");
		plot1.setLineWidth(2);
		plot1.setColor(Color.red, Color.red);
		plot1.setColor(Color.blue);
		plot1.add("circle", vetX, vetY);
		plot1.setFrameSize(PLOT_WIDTH, PLOT_HEIGHT);
		plot1.setLimits(xmin, xmax, ymin, ymax);
		plot1.show();

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

		Utility.logEnd(pathVolatile);
		Utility.logMove(pathLesione, pathVolatile);
		Utility.logInit(pathVolatile);
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
	 * Cancella tutti i file con estensione ".txt" presenti nella cartella
	 * 
	 * @param pathDir
	 */
	public static void logDeleteAll(String pathDir) {

		File folder = new File(pathDir);
		File fList[] = folder.listFiles();
		for (File f1 : fList) {
			if (f1.getName().endsWith(".txt")) {
				f1.delete();
			}
		}

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
	 * Effettua il plot dei punti trovati, MOSTRANDO i due fit ImageJ e Flanagan
	 * sovrapposti
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static void MIRD_curvePlotterSpecialCombined(CurveFitter cf, Regression reg, double[] x, double[] y) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		int npoints = 400;
		if (npoints < x.length)
			npoints = x.length; // or 2*x.length-1; for 2 values per data point
		if (npoints > 1000)
			npoints = 1000;
		double[] a = Tools.getMinMax(x);
		double xmin = a[0], xmax = a[1] * 1.5;
		xmin = 0;
		npoints = 400;
		double[] b = Tools.getMinMax(y);
		double ymin = b[0], ymax = b[1] * 1.1; // y range of data points
		ymin = 0;

		// curva di FIT ottenuta da ImageJ

		double[] pxj = new double[npoints];
		double[] pyj = new double[npoints];
		double incj = (xmax - xmin) / (npoints - 1);
		double tmpj = xmin;
		for (int i = 0; i < npoints; i++) {
			if (i % 2 != 0)
				pxj[i] = tmpj;
			tmpj += incj;
		}
		double[] paramsj = cf.getParams();
		for (int i = 0; i < npoints; i++)
			if (i % 2 != 0)
				pyj[i] = cf.f(paramsj, pxj[i]);

		// curva di FIT ottenuta da Flanagan
		double[] pxf = new double[npoints];
		double[] pyf = new double[npoints];
		double incf = (xmax - xmin) / (npoints - 1);
		double tmpf = xmin;
		for (int i = 0; i < npoints; i++) {
			if (i % 2 == 0)
				pxf[i] = tmpf;
			tmpf += incf;
		}
		double[] paramsf = reg.getBestEstimates();

		double aux0f = paramsf[1];
		double aux1f = paramsf[0];

		for (int i = 0; i < npoints; i++) {
			if (i % 2 == 0)
				pyf[i] = (aux0f * Math.exp(aux1f * pxf[i]));
		}

		a = Tools.getMinMax(pyj);
		double dataRange = ymax - ymin;
		ymin = Math.max(ymin - dataRange, Math.min(ymin, a[0])); // expand y range for curve, but not too much
		ymax = Math.min(ymax + dataRange, Math.max(ymax, a[1]));
		Plot plot = new Plot("Comparazione ImageJ BLU e Flanagan VERDE", "X", "Y");
		plot.setLineWidth(2);
		plot.setColor(Color.BLUE);
		plot.add("dot", pxj, pyj);
		plot.setColor(Color.GREEN);
		plot.add("dot", pxf, pyf);
		plot.setLimits(xmin, xmax, ymin, ymax);
		plot.setLineWidth(2);
		plot.setColor(Color.RED);
		plot.add("circle", x, y);
		plot.setColor(Color.BLUE);
		plot.setFrameSize(PLOT_WIDTH, PLOT_HEIGHT);

		plot.setColor(Color.BLUE);
		plot.show();
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
		aux1 = readFromLog(path, "#002#", "=");
		if (aux1 == null)
			return false;
		if (!Utility.isValidDateTime(aux1, "dd-MM-yyyy HH:mm:ss"))
			return false;
		aux1 = readFromLog(path, "#003#", "="); // activity
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
		double somministrata = Utility.readDoubleFromLog(pathVolatile, "#003#", "=");
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
			MyLog.here("flanagan errors");
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

	/**
	 * Utilizzato per abilitare il flag per la stampa di IJ.log solo sul mio PC, per
	 * il resto degli utenti IL BUIO.
	 * 
	 * @return
	 */
	public static boolean stampa() {

		String username = System.getProperty("user.name");
		if (username.equals("Alberto")) {
			return true;
		} else {
			return false;
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

	/**
	 * Legge le immagini da una cartella e le inserisce in uno stack. Copiato da
	 * https://github.com/ilan/fijiPlugins (Ilan Tal) Class: Read_CD. Ho disattivato
	 * alcune parti di codice riguardanti tipi di immagini di cui non disponiamo
	 * 
	 * @param myDirPath
	 * @return ImagePlus (stack)
	 */

	static ImagePlus readStackFiles(String myDirPath) {
		int j, k, n0, width = -1, height = 0, depth = 0, samplePerPixel = 0;
		int bad = 0, fails = 0;
		Opener opener;
		ImagePlus imp, imp2 = null;
		ImageStack stack;
		Calibration cal = null;
		double min, max, progVal;
		FileInfo fi = null;
		String flName, flPath, info, label1, tmp;
		String mytitle = "";

		info = null;
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		stack = null;
		File vetDirPath = new File(myDirPath);
		File checkEmpty;
		File[] results = vetDirPath.listFiles();
		if ((results == null) || (results.length == 0)) {
			MyLog.waitHere("pare non esistano files in " + myDirPath);
			return null;
		}

		boolean ok = false;
		for (int i1 = 0; i1 < results.length; i1++) {
			flName = results[i1].getName();
			flPath = results[i1].getPath();
			if (!isDicomImage(flPath))
				ok = Utility
						.dialogErrorMessageWithCancel_LP09("Il file " + flName + " non e'una immagine Dicom valida");
			if (ok)
				return null;
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

	public static boolean isDicomImage(String fileName1) {
		boolean ok = true;
		String info = new DICOM().getInfo(fileName1);
		if (info == null || info.length() == 0) {
			IJ.log("File " + fileName1 + " >>> HAS NOT DICOM INFO");
			ok = false;
		} else if (!info.contains("7FE0,0010")) {
			IJ.log("File " + fileName1 + " >>> HAS NOT PIXEL DATA");
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
			IJ.log("imageFromStack.stack== null");
			return null;
		}
		// IJ.log("stack bitDepth= "+stack.getBitDepth());
		ImageStack imaStack = stack.getImageStack();
		if (imaStack == null) {
			IJ.log("imageFromStack.imaStack== null");
			return null;
		}
		if (slice == 0) {
			IJ.log("imageFromStack.requested slice 0!");
			return null;

		}
		if (slice > stack.getStackSize()) {
			IJ.log("imageFromStack.requested slice > slices!");
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
	 * Questa tabella di taratura e'relativa ad un cubbo 6x6x6, infatti ha 216
	 * elementi! Notare che ho manipolato e taroccato i dati forniti sul file
	 * Formule, in modo che l'ultima colonna, quella relativa ad S [mGy/(MBq·s)] sia
	 * disposta analogamente ai pixel restituiti da getVoxels di ImageStack. In
	 * pratica la tabella del testo e' disposta in modo che cambi piu'velocemente la
	 * colonna z (k) e meno velocemente la colonna x (i), al contrario ImageJ cambia
	 * piu'velocemente la colonna x e meno velocemente la colonna z. Qui
	 * TabellaBella e'disposta proprio come ImageJ. Ora che ti ho ben confuso le
	 * idee possiamo continuare!!!
	 * 
	 * @return
	 */
	public static double[][] tabellaBella() {
		double[][] mat = { { 0, 0, 0, 2.26E-01 }, { 1, 0, 0, 3.39E-03 }, { 2, 0, 0, 1.88E-05 }, { 3, 0, 0, 8.43E-06 },
				{ 4, 0, 0, 4.83E-06 }, { 5, 0, 0, 3.14E-06 }, { 0, 1, 0, 3.39E-03 }, { 1, 1, 0, 1.19E-04 },
				{ 2, 1, 0, 1.51E-05 }, { 3, 1, 0, 7.60E-06 }, { 4, 1, 0, 4.56E-06 }, { 5, 1, 0, 3.04E-06 },
				{ 0, 2, 0, 1.88E-05 }, { 1, 2, 0, 1.51E-05 }, { 2, 2, 0, 9.48E-06 }, { 3, 2, 0, 5.90E-06 },
				{ 4, 2, 0, 3.89E-06 }, { 5, 2, 0, 2.74E-06 }, { 0, 3, 0, 8.43E-06 }, { 1, 3, 0, 7.60E-06 },
				{ 2, 3, 0, 5.90E-06 }, { 3, 3, 0, 4.32E-06 }, { 4, 3, 0, 3.14E-06 }, { 5, 3, 0, 2.35E-06 },
				{ 0, 4, 0, 4.83E-06 }, { 1, 4, 0, 4.56E-06 }, { 2, 4, 0, 3.89E-06 }, { 3, 4, 0, 3.14E-06 },
				{ 4, 4, 0, 2.50E-06 }, { 5, 4, 0, 1.98E-06 }, { 0, 5, 0, 3.14E-06 }, { 1, 5, 0, 3.04E-06 },
				{ 2, 5, 0, 2.74E-06 }, { 3, 5, 0, 2.35E-06 }, { 4, 5, 0, 1.98E-06 }, { 5, 5, 0, 1.61E-06 },
				{ 0, 0, 1, 3.39E-03 }, { 1, 0, 1, 1.19E-04 }, { 2, 0, 1, 1.51E-05 }, { 3, 0, 1, 7.60E-06 },
				{ 4, 0, 1, 4.56E-06 }, { 5, 0, 1, 3.04E-06 }, { 0, 1, 1, 1.19E-04 }, { 1, 1, 1, 2.82E-05 },
				{ 2, 1, 1, 1.26E-05 }, { 3, 1, 1, 6.94E-06 }, { 4, 1, 1, 4.32E-06 }, { 5, 1, 1, 2.93E-06 },
				{ 0, 2, 1, 1.51E-05 }, { 1, 2, 1, 1.26E-05 }, { 2, 2, 1, 8.46E-06 }, { 3, 2, 1, 5.49E-06 },
				{ 4, 2, 1, 3.72E-06 }, { 5, 2, 1, 2.64E-06 }, { 0, 3, 1, 7.60E-06 }, { 1, 3, 1, 6.94E-06 },
				{ 2, 3, 1, 5.49E-06 }, { 3, 3, 1, 4.09E-06 }, { 4, 3, 1, 3.04E-06 }, { 5, 3, 1, 2.29E-06 },
				{ 0, 4, 1, 4.56E-06 }, { 1, 4, 1, 4.32E-06 }, { 2, 4, 1, 3.72E-06 }, { 3, 4, 1, 3.04E-06 },
				{ 4, 4, 1, 2.42E-06 }, { 5, 4, 1, 1.92E-06 }, { 0, 5, 1, 3.04E-06 }, { 1, 5, 1, 2.93E-06 },
				{ 2, 5, 1, 2.64E-06 }, { 3, 5, 1, 2.29E-06 }, { 4, 5, 1, 1.92E-06 }, { 5, 5, 1, 1.60E-06 },
				{ 0, 0, 2, 1.88E-05 }, { 1, 0, 2, 1.51E-05 }, { 2, 0, 2, 9.48E-06 }, { 3, 0, 2, 5.90E-06 },
				{ 4, 0, 2, 3.89E-06 }, { 5, 0, 2, 2.74E-06 }, { 0, 1, 2, 1.51E-05 }, { 1, 1, 2, 1.26E-05 },
				{ 2, 1, 2, 8.46E-06 }, { 3, 1, 2, 5.49E-06 }, { 4, 1, 2, 3.72E-06 }, { 5, 1, 2, 2.64E-06 },
				{ 0, 2, 2, 9.48E-06 }, { 1, 2, 2, 8.46E-06 }, { 2, 2, 2, 6.36E-06 }, { 3, 2, 2, 4.56E-06 },
				{ 4, 2, 2, 3.28E-06 }, { 5, 2, 2, 2.42E-06 }, { 0, 3, 2, 5.90E-06 }, { 1, 3, 2, 5.49E-06 },
				{ 2, 3, 2, 4.56E-06 }, { 3, 3, 2, 3.57E-06 }, { 4, 3, 2, 2.74E-06 }, { 5, 3, 2, 2.12E-06 },
				{ 0, 4, 2, 3.89E-06 }, { 1, 4, 2, 3.72E-06 }, { 2, 4, 2, 3.28E-06 }, { 3, 4, 2, 2.74E-06 },
				{ 4, 4, 2, 2.23E-06 }, { 5, 4, 2, 1.80E-06 }, { 0, 5, 2, 2.74E-06 }, { 1, 5, 2, 2.64E-06 },
				{ 2, 5, 2, 2.42E-06 }, { 3, 5, 2, 2.12E-06 }, { 4, 5, 2, 1.80E-06 }, { 5, 5, 2, 1.51E-06 },
				{ 0, 0, 3, 8.43E-06 }, { 1, 0, 3, 7.60E-06 }, { 2, 0, 3, 5.90E-06 }, { 3, 0, 3, 4.32E-06 },
				{ 4, 0, 3, 3.14E-06 }, { 5, 0, 3, 2.35E-06 }, { 0, 1, 3, 7.60E-06 }, { 1, 1, 3, 6.94E-06 },
				{ 2, 1, 3, 5.49E-06 }, { 3, 1, 3, 4.09E-06 }, { 4, 1, 3, 3.04E-06 }, { 5, 1, 3, 2.29E-06 },
				{ 0, 2, 3, 5.90E-06 }, { 1, 2, 3, 5.49E-06 }, { 2, 2, 3, 4.56E-06 }, { 3, 2, 3, 3.57E-06 },
				{ 4, 2, 3, 2.74E-06 }, { 5, 2, 3, 2.12E-06 }, { 0, 3, 3, 4.32E-06 }, { 1, 3, 3, 4.09E-06 },
				{ 2, 3, 3, 3.57E-06 }, { 3, 3, 3, 2.92E-06 }, { 4, 3, 3, 2.35E-06 }, { 5, 3, 3, 1.88E-06 },
				{ 0, 4, 3, 3.14E-06 }, { 1, 4, 3, 3.04E-06 }, { 2, 4, 3, 2.74E-06 }, { 3, 4, 3, 2.35E-06 },
				{ 4, 4, 3, 1.97E-06 }, { 5, 4, 3, 1.63E-06 }, { 0, 5, 3, 2.35E-06 }, { 1, 5, 3, 2.29E-06 },
				{ 2, 5, 3, 2.12E-06 }, { 3, 5, 3, 1.88E-06 }, { 4, 5, 3, 1.63E-06 }, { 5, 5, 3, 1.39E-06 },
				{ 0, 0, 4, 4.83E-06 }, { 1, 0, 4, 4.56E-06 }, { 2, 0, 4, 3.89E-06 }, { 3, 0, 4, 3.14E-06 },
				{ 4, 0, 4, 2.50E-06 }, { 5, 0, 4, 1.98E-06 }, { 0, 1, 4, 4.56E-06 }, { 1, 1, 4, 4.32E-06 },
				{ 2, 1, 4, 3.72E-06 }, { 3, 1, 4, 3.04E-06 }, { 4, 1, 4, 2.42E-06 }, { 5, 1, 4, 1.92E-06 },
				{ 0, 2, 4, 3.89E-06 }, { 1, 2, 4, 3.72E-06 }, { 2, 2, 4, 3.28E-06 }, { 3, 2, 4, 2.74E-06 },
				{ 4, 2, 4, 2.23E-06 }, { 5, 2, 4, 1.80E-06 }, { 0, 3, 4, 3.14E-06 }, { 1, 3, 4, 3.04E-06 },
				{ 2, 3, 4, 2.74E-06 }, { 3, 3, 4, 2.35E-06 }, { 4, 3, 4, 1.97E-06 }, { 5, 3, 4, 1.63E-06 },
				{ 0, 4, 4, 2.50E-06 }, { 1, 4, 4, 2.42E-06 }, { 2, 4, 4, 2.23E-06 }, { 3, 4, 4, 1.97E-06 },
				{ 4, 4, 4, 1.70E-06 }, { 5, 4, 4, 1.44E-06 }, { 0, 5, 4, 1.98E-06 }, { 1, 5, 4, 1.92E-06 },
				{ 2, 5, 4, 1.80E-06 }, { 3, 5, 4, 1.63E-06 }, { 4, 5, 4, 1.44E-06 }, { 5, 5, 4, 1.25E-06 },
				{ 0, 0, 5, 3.14E-06 }, { 1, 0, 5, 3.04E-06 }, { 2, 0, 5, 2.74E-06 }, { 3, 0, 5, 2.35E-06 },
				{ 4, 0, 5, 1.98E-06 }, { 5, 0, 5, 1.61E-06 }, { 0, 1, 5, 3.04E-06 }, { 1, 1, 5, 2.93E-06 },
				{ 2, 1, 5, 2.64E-06 }, { 3, 1, 5, 2.29E-06 }, { 4, 1, 5, 1.92E-06 }, { 5, 1, 5, 1.60E-06 },
				{ 0, 2, 5, 2.74E-06 }, { 1, 2, 5, 2.64E-06 }, { 2, 2, 5, 2.42E-06 }, { 3, 2, 5, 2.12E-06 },
				{ 4, 2, 5, 1.80E-06 }, { 5, 2, 5, 1.51E-06 }, { 0, 3, 5, 2.35E-06 }, { 1, 3, 5, 2.29E-06 },
				{ 2, 3, 5, 2.12E-06 }, { 3, 3, 5, 1.88E-06 }, { 4, 3, 5, 1.63E-06 }, { 5, 3, 5, 1.39E-06 },
				{ 0, 4, 5, 1.98E-06 }, { 1, 4, 5, 1.92E-06 }, { 2, 4, 5, 1.80E-06 }, { 3, 4, 5, 1.63E-06 },
				{ 4, 4, 5, 1.44E-06 }, { 5, 4, 5, 1.25E-06 }, { 0, 5, 5, 1.61E-06 }, { 1, 5, 5, 1.60E-06 },
				{ 2, 5, 5, 1.51E-06 }, { 3, 5, 5, 1.39E-06 }, { 4, 5, 5, 1.25E-06 }, { 5, 5, 5, 1.12E-06 } };
		return mat;
	}

	/**
	 * Idea copiata da Laurent Thomas, & Pierre Trehin. (2021, July 22) github :
	 * LauLauThom/MaskFromRois-Fiji un plugin in pitonato (in pyton)
	 * 
	 * 
	 * @param impStack
	 * @return
	 */
	public static ImagePlus stackMask(ImagePlus impStack) {

		ImageStack myImageStack = impStack.getImageStack();
		int width = myImageStack.getWidth();
		int height = myImageStack.getHeight();
		int size = myImageStack.getSize();
//		ByteProcessor mask1 = new ByteProcessor(width, height); // ora ho una maschera in cui 0 significa che il pixel
//																// non fa parte di alcuna patata o buccia

		ImageStack myMaskStack = new ImageStack(width, height, size);

		for (int i1 = 0; i1 < size; i1++) {
			ImageProcessor ipSlice = myImageStack.getProcessor(i1);
			ImageProcessor maskSlice = ipSlice.getMask();
			myMaskStack.addSlice(maskSlice);
		}
		ImagePlus myMaskImage = new ImagePlus("CARNIVAL", myMaskStack);
		return myMaskImage;
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

	/**
	 * Aggiorna il contenuto di una immagine dello stack
	 * 
	 * @param stack
	 * @param ipSlice
	 * @param num
	 */
	static void stackSliceUpdater(ImageStack stack, ImageProcessor ipSlice, int num) {

		stack.deleteSlice(num);
		stack.addSlice("", ipSlice, num - 1);
		stack.setSliceLabel("MASK_" + num, num);

	}

	static double[] MyStackStatistics(ImagePlus impStackIn, ImagePlus impMask) {

		ImageStack stackIn = impStackIn.getImageStack();
		ImageStack stackMask = impMask.getImageStack();
		int width1 = stackIn.getWidth();
		int height1 = stackIn.getHeight();
		int depth1 = stackIn.getSize();
		ImageProcessor inSlice1 = null;
		ImageProcessor maskSlice1 = null;
		double pixel = 0;
		double mask = 0;
		double minStackVal = Double.MAX_VALUE;
		double maxStackVal = Double.MIN_VALUE;
		double meanStackVal = Double.NaN;
		double sumPix = 0;
		int[] minStackCoord = new int[3];
		int[] maxStackCoord = new int[3];
		long pixCount = 0;

		for (int z1 = 0; z1 < depth1; z1++) {
			inSlice1 = stackIn.getProcessor(z1 + 1);
			maskSlice1 = stackMask.getProcessor(z1 + 1);
			for (int x1 = 0; x1 < width1; x1++) {
				for (int y1 = 0; y1 < height1; y1++) {
					IJ.showStatus("DDD  " + z1 + " / " + (depth1));
					pixel = inSlice1.getPixelValue(x1, y1);
					mask = maskSlice1.getPixelValue(x1, y1);
					if (pixel > 0 && mask > 0) {
						sumPix = sumPix + pixel;
						pixCount++;
						if (pixel < minStackVal) {
							minStackVal = pixel;
							minStackCoord[0] = x1;
							minStackCoord[1] = y1;
							minStackCoord[2] = z1;
						}
						if (pixel > maxStackVal) {
							maxStackVal = pixel;
							maxStackCoord[0] = x1;
							maxStackCoord[1] = y1;
							maxStackCoord[2] = z1;
						}
					}
				}
			}

		}
		meanStackVal = sumPix / pixCount;
		double[] out1 = new double[11];
		out1[0] = (double) minStackCoord[0];
		out1[1] = (double) minStackCoord[1];
		out1[2] = (double) minStackCoord[2];
		out1[3] = minStackVal;
		out1[4] = (double) maxStackCoord[0];
		out1[5] = (double) maxStackCoord[1];
		out1[6] = (double) maxStackCoord[2];
		out1[7] = maxStackVal;
		out1[8] = pixCount;
		out1[9] = meanStackVal;
		out1[10] = sumPix;

		return out1;
	}

	static double[] MyStackStatistics(ImagePlus impStackIn) {

		ImageStack stackIn = impStackIn.getImageStack();
		int width1 = stackIn.getWidth();
		int height1 = stackIn.getHeight();
		int depth1 = stackIn.getSize();
		ImageProcessor inSlice1 = null;
		double pixel = 0;
		double minStackVal = Double.MAX_VALUE;
		double maxStackVal = Double.MIN_VALUE;
		double meanStackVal = Double.NaN;
		double sumPix = 0;
		int[] minStackCoord = new int[3];
		int[] maxStackCoord = new int[3];
		long pixCount = 0;

		for (int z1 = 0; z1 < depth1; z1++) {
			inSlice1 = stackIn.getProcessor(z1 + 1);
			for (int x1 = 0; x1 < width1; x1++) {
				for (int y1 = 0; y1 < height1; y1++) {
					IJ.showStatus("EEE  " + z1 + " / " + (depth1));
//					if (z1 == 0 && x1 == 0 && y1 == 0)
//						MyLog.waitHere();
					pixel = inSlice1.getPixelValue(x1, y1);
					if (pixel > 0) {
						sumPix = sumPix + pixel;
						pixCount++;
						if (pixel < minStackVal) {
							minStackVal = pixel;
							minStackCoord[0] = x1;
							minStackCoord[1] = y1;
							minStackCoord[2] = z1;
						}
						if (pixel > maxStackVal) {
							maxStackVal = pixel;
							maxStackCoord[0] = x1;
							maxStackCoord[1] = y1;
							maxStackCoord[2] = z1;
						}
					}
				}
			}

		}
		meanStackVal = sumPix / pixCount;
		double[] out1 = new double[11];
		out1[0] = (double) minStackCoord[0];
		out1[1] = (double) minStackCoord[1];
		out1[2] = (double) minStackCoord[2];
		out1[3] = minStackVal;
		out1[4] = (double) maxStackCoord[0];
		out1[5] = (double) maxStackCoord[1];
		out1[6] = (double) maxStackCoord[2];
		out1[7] = maxStackVal;
		out1[8] = pixCount;
		out1[9] = meanStackVal;
		out1[10] = sumPix;

		return out1;
	}

	static int MyStackCountPixels(ImagePlus impMaskStack) {

		int count = 0;
		int maxcount = 0;
		int slice = 0;
		double voxMask = 0;
		int width = impMaskStack.getWidth();
		int heigth = impMaskStack.getHeight();
		int depth = impMaskStack.getNSlices();

		ImageStack stackMask = impMaskStack.getImageStack();

		for (int z1 = 1; z1 < depth; z1++) {
			count = 0;
			for (int x1 = 0; x1 < width; x1++) {
				for (int y1 = 0; y1 < heigth; y1++) {
					voxMask = stackMask.getVoxel(x1, y1, z1);
					if (voxMask > 0)
						count++;
				}
			}

			if (count >= maxcount) {
				maxcount = count;
				slice = z1;
			}
		}

		return slice;
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
//		IJ.log("" + xcount + " " + ycount + "  " + zcount + " " + aux1);
		return aux1;
	}

	static void calculateDVH(ImagePlus patata) {

		ImageStack stack = patata.getImageStack();

		int width = stack.getWidth();
		int height = stack.getHeight();
		int depth = stack.getSize();
		double voxel = 0;
		double[] vetVoxel = null;

		ArrayList<Double> arrList = new ArrayList<Double>();
		for (int z1 = 1; z1 < depth; z1++) {
			for (int x1 = 0; x1 < width; x1++) {
				for (int y1 = 0; y1 < height; y1++) {
					voxel = stack.getVoxel(x1, y1, z1);
					if (voxel > 0)
						arrList.add(voxel);
				}
			}
		}
		vetVoxel = Utility.arrayListToArrayDouble(arrList);
		Arrays.sort(vetVoxel);

		for (int i1 = 0; i1 < vetVoxel.length; i1++) {
			IJ.log("" + vetVoxel[i1]);
		}

	}

}
