package Dosimetry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.WaitForUserDialog;

/**
 * @author Alberto
 *
 */
public class MyLog {

	public static void log(String str) {

		boolean stampa = false;

		if (MyGlobals.attivaLog) {
			stampa = true;
		} else {
			// IJ.log("stampa=false");
			stampa = false;
		}
		if (stampa) {
			IJ.log(str); /// CAZZATA !!! DEVE ASSOLUTAMENTE essere IJ.log senno' hai voglia a ricorsioni
							/// infinite!!!
		}
		return;
	}

	public static String here1() {
		String qui = "file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber();
		return qui;
	}

	public static String here() {
		String qui = "file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " class="
				+ Thread.currentThread().getStackTrace()[2].getClassName() + " method="
				+ Thread.currentThread().getStackTrace()[2].getMethodName();
		MyLog.log(qui);
		return qui;
	}

	public static void here(String str) {
		MyLog.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " class="
				+ Thread.currentThread().getStackTrace()[2].getClassName() + " method="
				+ Thread.currentThread().getStackTrace()[2].getMethodName() + " " + str);
	}

	public static void waitHere() {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber()).show();
	}

	public static void waitHere(String str) {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n \n" + str).show();
	}

	public static void waitThere(String str) {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n" + "file="
				+ Thread.currentThread().getStackTrace()[3].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[3].getLineNumber() + "\n \n" + str).show();
	}

	public static void logArrayList(ArrayList<String> arrList) {
		if (arrList == null) {
			MyLog.log("Warning vector = null");
		} else {
			MyLog.log("----------- [ " + arrList.size() + " ] -----------");
			for (int j1 = 0; j1 < arrList.size(); j1++) {
				MyLog.log(arrList.get(j1));
			}
			MyLog.log("---------------------------------------------");
		}
	}

	public static void logArrayList(ArrayList<Double> arrList, String title) {
		if (arrList == null) {
			MyLog.log("Warning vector " + title + " = null");
		} else {
			MyLog.log("----------- " + title + "  [ " + arrList.size() + " ] -----------");
			String logRiga = "";
			for (int j1 = 0; j1 < arrList.size(); j1++) {
				logRiga += arrList.get(j1) + ",  ";
			}
			MyLog.log(logRiga);
		}
	}

	public static void logVector(double[] vect, String nome) {
		String stri = "";
		if (vect == null) {
			MyLog.log("Warning vector " + nome + " = null");
		} else {
			MyLog.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "  ----------- " + nome + " [ "
					+ vect.length + "] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			MyLog.log(stri);
		}
		MyLog.log("----------------------------------");
	}

	public static void logVector(float[] vect, String nome) {
		String stri = "";
		if (vect == null) {
			MyLog.log("Warning vector " + nome + " = null");
		} else {
			MyLog.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			MyLog.log(stri);
		}
		MyLog.log("---------------------------------------------");
	}

	public static String logElapsed(long start, long end) {

		long elapsed = end - start;

		int fractions = (int) elapsed % 60;
		int seconds = (int) (elapsed / 1000) % 60;
		int minutes = (int) (elapsed / (1000 * 60));
		int hours = (int) (elapsed / (1000 * 60 * 60));

		String aux1 = "elapsed [hh:mm:ss] " + String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, fractions);

		MyLog.log("elapsed [hh:mm:ss] " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
		return aux1;
	}

	public static void logVector(String vect[], String nome) {
		String stri = "";
		if (vect == null) {
			MyLog.log("Warning vector " + nome + " = null");
		} else {
			MyLog.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			MyLog.log(stri);
		}
		MyLog.log("---------------------------------------------");
	}

	public static void logMatrix(int mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			MyLog.waitHere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				MyLog.waitHere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// MyLog.log("rows=" + rows + " columns= " + columns);

			MyLog.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {
					stri += mat[i1][i2] + ",  ";
				}
				MyLog.log(stri);
			}
		}
		MyLog.log("---------------------------------------------");
	}

	public static void logMatrix(double mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			MyLog.waitHere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				MyLog.waitHere("Warning matrix " + nome + " length=0");
				return;
			}
			columns = mat[0].length;
			// MyLog.waitThere("rows=" + rows + " columns= " + columns);

			MyLog.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {
					stri += mat[i1][i2] + ",  ";
				}
				MyLog.log(stri);
			}
		}
		MyLog.log("---------------------------------------------");
	}

	public static void logMatrixVertical(double mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			MyLog.waitHere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				MyLog.waitHere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// MyLog.log("rows=" + rows + " columns= " + columns);

			MyLog.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < columns; i1++) {
				stri = "";
				for (int i2 = 0; i2 < rows; i2++) {
					stri += mat[i2][i1] + ",  ";
				}
				MyLog.log(stri);
			}
		}
		MyLog.log("---------------------------------------------");
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
		MyLog.logAppend(path, "---- INIZIO ---------");
	}

	/**
	 * Scrive FINE nel log
	 * 
	 * @param path PATH DEL LOG
	 */
	public static void logEnd(String path) {
		MyLog.logAppend(path, "---- FINE ---------");
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
			aux2 = MyLog.readFromLog(pathSorgente, aux1);
			if (aux2 != null) {
				logAppend(pathDestinazione, aux2);
			}
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

		log("eseguo dedupeLog");
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
			log("dedupe DISASTER");
			System.out.println("DEDUPE errore lettura/scrittura file " + path1);
		}
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
			MyDialog.dialogErrorMessage_LP06(path);
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
	 * Lettura di un tag dal log
	 * 
	 * @param path1
	 * @param code1
	 * @param separator
	 * @return
	 */
	static double readDoubleFromLog(String path1, String code1, String separator) {

		// leggo una stringa dal log
		String[] vetText = MyLog.readSimpleText(path1);
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
	 * Restituisce l'intera linea del log per il tag
	 * 
	 * @param path1
	 * @param code1
	 * @return
	 */
	static String readFromLog(String path1, String code1) {

		// leggo una stringa dal log
		String[] vetText = MyLog.readSimpleText(path1);
		if (vetText.length > 0) {
			for (int i1 = 0; i1 < vetText.length; i1++) {
				if (vetText[i1].contains(code1))
					return vetText[i1];
			}
		}
		return null;
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
			waitHere("path1==null");
		String[] vetText = MyLog.readSimpleText(path1);
		if (vetText == null)
			waitHere("vetText==null");

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
			waitHere("non trovo " + code1);
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
			waitHere("path1==null");
		String[] vetText = MyLog.readSimpleText(path1);
		if (vetText == null)
			waitHere("vetText==null");

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
				waitHere("non trovo " + code1);
			return null;
		}
		return out1;
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
			waitHere("fileNotFound error= " + path1);
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
			waitHere("reading error= " + path1);
			e.printStackTrace();
		}

		String[] out2 = lines.toArray(new String[0]);
		return out2;
	}

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
			log("errore lettura " + path1);
			e.printStackTrace();
		}
		String[] out2 = out1.toArray(new String[0]);
		return out2;
	}

}
