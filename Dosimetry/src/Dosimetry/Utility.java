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

}
