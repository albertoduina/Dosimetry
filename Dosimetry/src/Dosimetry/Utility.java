package Dosimetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Plot;
import ij.gui.WaitForUserDialog;
import ij.measure.CurveFitter;
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

	/**
	 * Legge tutte le linee di un file testo e le restituisce come vettore di
	 * stringhe
	 * 
	 * @param path1 path del file, completo di nome
	 * @return vettore stringhe
	 */
	public static String[] readSimpleText(String path1) {

		List<String> out1 = null;
		try {
			out1 = Files.readAllLines(Paths.get(path1));
			// IJ.log("lette= " + out1.size() + " linee");
		} catch (IOException e) {
			IJ.log("errore lettura " + path1);
			e.printStackTrace();
		}
		String[] out2 = out1.toArray(new String[0]);
		return out2;
	}

	/**
	 * Inizializza il file di log cancellando se esiste e scrivendoci INIZIO
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
	 * Elimina i tag duplicati nel file
	 * 
	 * @param path1
	 */
	static void dedupeLog(String path1) {
		ArrayList<String> inArrayList = new ArrayList<String>();
		ArrayList<String> outArrayList = new ArrayList<String>();
		String line1 = "";
		String line2 = "";
		String line4 = "";
		String tag1 = "";
		String tag2 = "";

		IJ.log("eseguo dedupeLog");
		try {
			BufferedReader file1 = new BufferedReader(new FileReader(path1));
			while ((line1 = file1.readLine()) != null) {
				inArrayList.add(line1);
			}
			file1.close();
			new File(path1).delete();
			// questo si chiama ALGORITMO DEL TROGLODITA, IN QUESTO CASO UN INGENNIERE BRAO
			// FESS avrebbe usato una HashList
			for (int i1 = inArrayList.size() - 1; i1 >= 0; i1--) {
				line2 = inArrayList.get(i1);
				tag1 = line2.substring(0, 5);
				boolean dupe = false;
				for (String line3 : outArrayList) {
					tag2 = line3.substring(0, 5);
					if (tag1.equals(tag2)) {
						dupe = true;
					}
				}
				if (!dupe) {
					outArrayList.add(line2);
				}
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(path1, true));
			for (int i1 = outArrayList.size() - 1; i1 >= 0; i1--) {
				line4 = outArrayList.get(i1);
				out.write(line4);
				out.newLine();
			}
			out.close();
		} catch (Exception e) {
			IJ.log("dedupe DISASTER");
			System.out.println("DEDUPE errore lettura/scrittura file " + path1);
		}
	}

	/**
	 * Se nel file esiste gia'una linea col tag, essa viene sostituita, se la linea
	 * non esiste, essa viene aggiunta alla fine
	 * 
	 * @param path1
	 * @param tag
	 * @param newline
	 */
	public static void modifyLog(String path1, String tag, String newline) {

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
	 * Copia tutti i dati dal log volatile.txt al log permanente.txt
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
			// out.newLine();
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
	 * Lettura di un tag dal log
	 * 
	 * @param path1
	 * @param code1
	 * @param separator
	 * @return
	 */
	static String readFromLog(String path1, String code1, String separator) {

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
	static void copyLogInfo(String pathSorgente, String pathDestinazione, int start, int end) {

		String aux1 = "";
		String aux2 = "";
		for (int i1 = start; i1 <= end; i1++) {
			aux1 = "#" + String.format("%03d", i1) + "#";
			aux2 = readFromLog(pathSorgente, aux1);
			if (aux2 != null) {
				Utility.appendLog(pathDestinazione, aux2);
			}
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
		window.setLocationAndSize(0, 0, (int) (((double) screen.height) / 1.5), (int) (((double) screen.height) / 1.5));
	}

	/**
	 * Test per validazione data
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
			// IJ.log("isValidTime TEST SUPERATO");
			return true;
		} catch (ParseException e) {
			// IJ.log("isValidTime ERRORE");
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
	static ImagePlus sceltaAutomaticaImmagine_DD10(boolean ok24, boolean ok48, boolean ok120, Font defaultFont) {

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
		double activity = in1[2]; // #003# activity
		double threshold = in1[3]; // #115# contouring threshold level
		double integral = in1[4]; // #120# over threshold count integral

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
		} else if (threshold > 0.30 && threshold < 0.50) {
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
	static double[] MIRD_curveFitter(double[] vetX, double[] vetY) {

		CurveFitter cf1 = new CurveFitter(vetX, vetY);
		cf1.doFit(CurveFitter.EXPONENTIAL);
		String status = cf1.getStatusString();
		IJ.log("STATUS del fit= " + status);
		double[] params = cf1.getParams();
		int numParams = cf1.getNumParams();
		double goodness = cf1.getFitGoodness();
		String res1 = cf1.getResultString();
//		Plot zz = cf1.getPlot(128);
//		zz.setLineWidth(2);
//		zz.show();
//		Utility.debugDeiPoveri("SPETTA");
		double[] out1 = new double[4];
		for (int i1 = 0; i1 < params.length; i1++) {
			IJ.log("MIRD FIT param " + i1 + " =" + params[i1]);
			out1[i1] = params[i1];
		}
		IJ.log("MIRD FIT goodness=  " + goodness);
		IJ.log("MIRD FIT numParams=  " + numParams);
		IJ.log("MIRD FIT resultString=  " + res1);
		out1[3] = goodness;
		return out1;
	}

	/**
	 * Effettua il plot dei punti trovati, SENZA mostrare alcun fit
	 * 
	 * @param vetX
	 * @param vetY
	 */

	static void MIRD_curvePlotter(double[] vetX, double[] vetY) {

		double[] minMaxX = Tools.getMinMax(vetX);
		double[] minMaxY = Tools.getMinMax(vetY);
		double xmin = 0;
		double xmax = minMaxX[1] * 1.1;
		double ymin = -1.0;
		double ymax = minMaxY[1] * 1.1;

		Plot plot1 = new Plot("Punti", "ore dalla somministrazione", "attivita' MBq");
		plot1.setLineWidth(2);
		plot1.setColor(Color.red);
		plot1.add("circle", vetX, vetY);
		plot1.setLimits(xmin, xmax, ymin, ymax);
		plot1.show();

	}

	/**
	 * Assegnazione nome alle lesioni
	 * 
	 * @param pathVolatile
	 * @param pathPermanente
	 */
	static void battezzaLesioni_DD07(String pathVolatile) {
		// alla fine del nostro reiterativo lavoro decidiamo che dobbiamo salvare il
		// tutto CHE COSA POTRA'MAI ANDARE STORTO???
		GenericDialog compliments1 = new GenericDialog("DD07 - Compliments1");
		compliments1.addMessage("COMPLIMENTI, HAI COMPLETATO L'ANALISI DELLA LESIONE");
		compliments1.addMessage("SENZA SCLERARE TROPPO");
		compliments1.addStringField("NomeLesione per memorizzazione", "");
		compliments1.showDialog();
		String lesionName = compliments1.getNextString();
		IJ.log("eseguo battezzaLesioni con DD07 lesionName= " + lesionName);

		// ora i nostri dati verrano battezzati col nome fornito dal ... PADRINO !!!
		// il nome del nuovo file diverra' lesionName.txt, non occorre un controllo che
		// l'operatore non ci abbia CASUALMENTE fornito lo stesso nome di una altra
		// lesione, in tal caso gli verra'cantata tutta la canzone "Il gorilla" di
		// Fabrizio de Andre', ovviamente con esempi pratici.

		int pos = pathVolatile.lastIndexOf(File.separator);
		String pathBase = pathVolatile.substring(0, pos);
		String pathLesione = pathBase + File.separator + lesionName + ".txt";

		Utility.endLog(pathVolatile);
		Utility.moveLog(pathLesione, pathVolatile);
		Utility.initLog(pathVolatile);
	}

	/**
	 * Selezione altro distretto anatomico
	 * 
	 */
	void altroDistretto_DD08() {
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

	/**
	 * Ammazza tutte le finestre aperte: in pratica un teppista col tirasassi!
	 */
	static void chiudiTutto() {

		IJ.log("eseguo chiudiTutto");
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

}
