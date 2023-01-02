package Dosimetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.WaitForUserDialog;

public class Utility {

	/**
	 * Legge il log e mette in un vettore le stringhe, salta le vuote
	 * 
	 * @param path1     indirizzo log da leggere
	 * @param compress1 eliminare linee vuote
	 * @return
	 */
	public static String[] readLog(String path1, boolean compress1) {

		String[] out1 = readLogCompress(path1, compress1);
		return out1;
	}

	/**
	 * Legge il log e mette in un vettore tutte le strinhe
	 * 
	 * @param path indirizzo log da leggere
	 */
	public static String[] readLog(String path1) {

		String[] out1 = readLogCompress(path1, false);
		return out1;
	}

	/**
	 * Legge il log e mette in un vettore le stringhe, salta le vuote
	 * 
	 * @param path     indirizzo log da leggere
	 * @param compress eliminare linee vuote
	 * @return
	 */
	public static String[] readLogCompress(String path, boolean compress) {

		ArrayList<String> inArrayList = new ArrayList<String>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
			while (br.ready()) {
				String line = br.readLine();
				inArrayList.add(line);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> outArrayList = new ArrayList<String>();
		String aux11 = "";
		for (int i1 = 0; i1 < inArrayList.size(); i1++) {
			aux11 = inArrayList.get(i1);
			if (!aux11.isEmpty()) {
				outArrayList.add(aux11);
			}
		}
		Object[] objArray = outArrayList.toArray();
		String[] vetOut = new String[objArray.length];
		for (int i1 = 0; i1 < objArray.length; i1++) {
			vetOut[i1] = objArray[i1].toString();
		}
		return vetOut;
	}

	/**
	 * Inizializza il file di log
	 * 
	 * @param path indirizzo log da utilizzare
	 */
	public static void initLog(String path) {
		File f1 = new File(path);
		if (f1.exists()) {
			f1.delete();
		}
		appendLog(path, "---- INIZIO ---------");
	}

	/**
	 * Scrive FINE nel log
	 * 
	 * @param path
	 */
	public static void endLog(String path) {
		appendLog(path, "---- FINE ---------");
	}

	/**
	 * Cancellazione del file, attenzione devono essere prima chiusi BufferedReader
	 * e BufferedWriter
	 * 
	 * @param path indirizzo log da utilizzare
	 */
	public static void deleteLog(String path) {
		File f1 = new File(path);
		if (f1.exists()) {
			f1.delete();
		}
		if (f1.exists()) {
			debugDeiPoveri("NON RIESCO A CANCELLARE " + path);
		}
	}

	/**
	 * Copia i dati dal log volatile.txt al log permanente.txt
	 * 
	 * @param permFile indirizzo log permanente da utilizzare
	 * @param tmpFile  indirizzo log temporaneo da utilizzare
	 */
	public static void moveLog(String permFile, String tmpFile) {
		BufferedWriter out;
		BufferedReader in;
		String str = "";
		try {
			out = new BufferedWriter(new FileWriter(permFile, true));
			in = new BufferedReader(new FileReader(tmpFile));
			out.newLine();
			while ((str = in.readLine()) != null) {
				out.write(str);
				out.newLine();
			}
			out.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		deleteLog(tmpFile);

	}

	/**
	 * Scrive una riga nel log
	 * 
	 * @param path  path indirizzo log da utilizzare
	 * @param linea stringa da inserire
	 */
	public static void appendLog(String path, String linea) {

		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(path, true));
			out.write(linea);
			out.newLine();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Sofisticatissimo strumento di test
	 * 
	 * @param paramString stringa da mostrare nel dialogo
	 */
	public static void debugDeiPoveri(String text) {
		WaitForUserDialog wait = new WaitForUserDialog("Debug", text);
		wait.show();
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
		window.setLocationAndSize(0, 0, (int) (((double) screen.height) / 2), (int) (((double) screen.height) / 2));
	}

	static String readFromLog(String path1, String code1, String separator) {

		// leggo una stringa dal log
		String[] vetText = Utility.readLog(path1);
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
		return out1;
	}
	
	/**
	 * Restituisce l'intera linea del parametro
	 * 
	 * @param path1
	 * @param code1
	 * @return
	 */
	static String readFromLog(String path1, String code1) {

		// leggo una stringa dal log
		String[] vetText = Utility.readLog(path1);
		String[] vetAux1;
		String out1 = null;
		if (vetText.length > 0) {
			for (int i1 = 0; i1 < vetText.length; i1++) {
				if (vetText[i1].contains(code1)) {
					out1 = vetText[i1];
				}
			}
		}
		return out1;
	}


	/**
	 * Test per data valida
	 * 
	 * @param date
	 * @return
	 */
	static boolean isValidDate(String date, String format) {

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try {
			sdf.parse(date);
			// IJ.log("isValidDate TEST SUPERATO");
			return true;
		} catch (ParseException e) {
			// IJ.log("isValidDate ERRORE");
			return false;
		}
	}

	/**
	 * Test per data valida
	 * 
	 * @param date
	 * @return
	 */
	static boolean isValidTime(String time, String format) {

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try {
			sdf.parse(time);
			// IJ.log("isValidTime TEST SUPERATO");
			return true;
		} catch (ParseException e) {
			// IJ.log("isValidTime ERRORE");
			return false;
		}
	}

	/**
	 * Test per data ora valide
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

	public static void purgeDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (!file.isDirectory())
				file.delete();
		}
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
	static ImagePlus sceltaAutomaticaImmagine(boolean ok24, boolean ok48, boolean ok120, Font defaultFont) {

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
		ImagePlus dicomImage1 = scelta1.getNextImage();
		return dicomImage1;

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
	 * Calcolo delta T in millisecondi
	 * 
	 * @param dateTime0
	 * @param dateTime24
	 * @return
	 */
	static long CalcoloDeltaT(Date dateTime0, Date dateTime24) {

//		IJ.log("dateTime0= " +dateTime0);
//		IJ.log("dateTime24= " +dateTime24);
		long diff = dateTime24.getTime() - dateTime0.getTime();
//		IJ.log("difference= " + diff / (1000 * 60 * 60) + " hours");
//		IJ.log("difference= " + diff / (1000 * 60 * 60 * 24) + " days");
		return diff;
	}

	
	static double[][] matTable() {
		double[][] myTable = { { 0.3, 0.4, 0.5 }, { 2.0050800, 1.4416900, 1.1119950 },
				{ 1.0008740, 1.0009900, 1.0016370 }, { 0.0838640, 0.1330990, 0.1528385 } };
		return myTable;
	}

	/**
	 * Ricava i dati dai file permanente e volatile, calcolando i parametri di
	 * plottaggio di un punto
	 * 
	 * @param pathVolatile
	 * @param pathPermanente
	 */
	static double[] puntoGrafico(String pathVolatile, String pathPermanente, double[] in1) {

//		double durata = Double.parseDouble(Utility.readFromLog(pathPermanente, "#028#", "="));
//		double conteggio = Double.parseDouble(Utility.readFromLog(pathVolatile, "#112#", "="));
//		double activity = Double.parseDouble(Utility.readFromLog(pathVolatile, "#102#", "="));
//		double threshold = Double.parseDouble(Utility.readFromLog(pathVolatile, "#118#", "="));

		double durata = in1[0];
		double conteggio = in1[1];
		double activity = in1[2];
		double threshold = in1[3];

		double[][] myMatTable = matTable();
		double t1 = 0;
		double a1 = 0;
		double b1 = 0;
		double c1 = 0;

		if (threshold <= 0.30) {
			t1 = myMatTable[0][0];
			a1 = myMatTable[1][0];
			b1 = myMatTable[2][0];
			c1 = myMatTable[3][0];
		} else if (threshold > 0.30 && threshold <= 0.50) {
			t1 = myMatTable[0][1];
			a1 = myMatTable[1][1];
			b1 = myMatTable[2][1];
			c1 = myMatTable[3][1];
		} else {
			t1 = myMatTable[0][2];
			a1 = myMatTable[1][2];
			b1 = myMatTable[2][2];
			c1 = myMatTable[3][2];
		}

		double vol = conteggio * (Math.pow(4.43, 3) / 1000.);
		double fatCal = a1 * Math.pow(b1, vol) * Math.pow(vol, c1);
		double attiv = conteggio / (durata * fatCal);
		double[] out1 = new double[3];
		out1[0] = vol;
		out1[1] = fatCal;
		out1[2] = attiv;

		return out1;

	}

	static void battezzaLesioni(String pathVolatile, String pathPermanente) {
		// alla fine del nostro reiterativo lavoro decidiamo che dobbiamo salvare il
		// tutto CHE COSA POTRA'MAI ANDARE STORTO???
		GenericDialog compliments1 = new GenericDialog("DD07 - Compliments1");
		compliments1.addMessage("COMPLIMENTI, HAI COMPLETATO L'ANALISI DELLA LESIONE");
		compliments1.addMessage("SENZA SCLERARE TROPPO");
		compliments1.addStringField("NomeLesione per memorizzazione", "");
		compliments1.showDialog();
		String lesionName = compliments1.getNextString();
		IJ.log("lesionName= " + lesionName);

		// ora i nostri dati verrano battezzati col nome fornito dal ... PADRINO !!!
		// il nome del nuovo file diverra' lesionName.txt, non occorre un controllo che
		// l'operatore non ci abbia CASUALMENTE fornito lo stesso nome di una altra
		// lesione, in tal caso gli verra'cantata tutta la canzone "Il gorilla" di
		// Fabrizio de Andre', ovviamente con esempio pratico.

		int pos = pathVolatile.lastIndexOf(File.separator);
		IJ.log("pathVolatile= " + pathVolatile);
		IJ.log("pos= " + pos);
		String pathBase = pathVolatile.substring(0, pos);
		IJ.log("pathBase= " + pathBase);
		String pathLesione = pathBase + File.separator + lesionName + ".txt";

		Utility.moveLog(pathLesione, pathVolatile);
		Utility.initLog(pathVolatile);

	}

	void altroDistretto() {
		IJ.log("DD08_altroDistretto");
		GenericDialog finished1 = new GenericDialog("DD08 - Finished1");
		finished1.addMessage("HAI TERMINATO ANALISI DISTRETTO?");
		finished1.addMessage("se rispondi ALTRA LESIONE vuoi analizzare un altra lesione");
		finished1.addMessage(
				"se rispondi FINITO vuoi passare in LoadPatient e caricare un altro distretto anatomico OPPURE HAI TERMINATO");
		finished1.setOKLabel("FINITO");
		finished1.setCancelLabel("ALTRA LESIONE");

		finished1.showDialog();
		boolean avanti = finished1.wasCanceled();
		boolean finito = finished1.wasOKed();

	}

	/**
	 * Copia i dati somministrazione in volatile da permanente
	 * 
	 */
	static void copiaSomministrazione(String pathPermanente, String pathDestinazione) {
		
		String aux1="";
		Utility.appendLog(pathDestinazione, "-- SOMMINISTRAZIONE --");
		aux1 = readFromLog(pathPermanente, "#100#");
		Utility.appendLog(pathDestinazione, aux1);
		aux1 = readFromLog(pathPermanente, "#101#");
		Utility.appendLog(pathDestinazione, aux1);
		aux1 = readFromLog(pathPermanente, "#102#");
		Utility.appendLog(pathDestinazione, aux1);
		Utility.appendLog(pathDestinazione, "-------------------");

	}

	/**
	 * Cancella tutti i file con estensione ".txt" presenti nella cartella
	 * 
	 * @param pathDir
	 */
	public static void deleteAllLogs(String pathDir) {

		File folder = new File(pathDir);
		File fList[] = folder.listFiles();
		for (File f1 : fList) {
			if (f1.getName().endsWith(".txt")) {
				f1.delete();
			}
		}

	}


}
