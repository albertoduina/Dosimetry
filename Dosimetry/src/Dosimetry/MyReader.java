package Dosimetry;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 ****************************
 * LETTURA DI FILE DI TESTO *
 ****************************
 */

public class MyReader {


	/**
	 * Legge i file in qualsiasi posizione siano: dentro un file jar, fuori dal file
	 * jar, dentro junit utilizza getResourceAsStream()
	 * 
	 * @param fileName
	 * @param intoJar
	 * @return
	 */
	public String[] readTextFileFromResources(String fileName, boolean intoJar) {
		String thisLine;
		String myFile = "";
		List<String> out1 = new ArrayList<String>();
		if (intoJar)
			myFile = "/" + fileName;
		else
			myFile = fileName;
		try {

			InputStream is = this.getClass().getResourceAsStream(myFile);
			if (is == null)
				if (is == null)
					MyLog.waitHere("il file " + fileName + " non esiste");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while ((thisLine = br.readLine()) != null) {
				out1.add(thisLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] out2 = out1.toArray(new String[0]);
		return out2;
	}

	/**
	 * Legge i file S-value scaricati dal sito
	 * https://www.medphys.it/down_svoxel.htm . Il formato del file non contiene
	 * solo caratteri puramente di testo, comunque questi caratteri speciali vengono
	 * filtrati e sostituiti, in modo da ottenere stringhe normali.
	 * 
	 * @param fileName
	 * @param intoJar
	 * @return
	 */
	public String[] readTextFileSVALUESFromResources(String fileName, boolean intoJar) {

		String myFile = "";
		String out1 = "";
		if (intoJar)
			myFile = "/" + fileName;
		else
			myFile = fileName;
		try {
			InputStream is = Utility.class.getResourceAsStream(myFile);
			if (is == null)
				MyLog.waitHere("il file " + fileName + " non esiste");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			int s1 = 0;
			String formattedString = null;
			while ((s1 = br.read()) != -1) {

				char character = (char) s1;
				if (character == '\n') {
					formattedString += "\n";
				} else if (character == (char) 9) {
					formattedString += ";";
				} else if (character == (char) 194) {
					formattedString += "*";
				} else if (character == (char) 183) {
					formattedString += "";
				} else {
					formattedString += character;
				}
			}
			out1 = out1 + formattedString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		MyLog.log("<" + out1 + ">");

		String[] out3 = out1.split("\n");
		int teoricLen = (int) Math.pow(MyGlobals.latoCubo() + 1, 3) + 2;
		if (out3.length != teoricLen)
			MyLog.waitHere("ATTENZIONE:\nla lughezza del file S-values deve essere di \n" + teoricLen
					+ " righe, contando anche le 2 di intestazione");

		String[] out4 = new String[out3.length - 2];
		int count1 = 0;
		for (int i1 = 2; i1 < out3.length; i1++) {
			out4[count1] = out3[i1];
			count1++;
		}

		return out4;
	}

	/**
	 * Lettura di un tag con valore double dal log
	 * 
	 * @param path1
	 * @param code1
	 * @param separator
	 * @return
	 */
	static double readDoubleFromLog(String path1, String code1, String separator) {

		// leggo una stringa dal log
		String[] vetText = readSimpleText(path1);
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
	 * Lettura di un tag con valore stringa dal log
	 * 
	 * @param path1
	 * @param code1
	 * @param separator
	 * @return
	 */
	static String readFromLog(String path1, String code1, String separator, boolean error) {

		if (path1 == null)
			MyLog.waitHere("path1==null");
		String[] vetText = readSimpleText(path1);
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
	 * Lettura di un tag con valore stringa dal log
	 * 
	 * @param path1
	 * @param code1
	 * @param separator
	 * @return
	 */
	static String readFromLog(String path1, String code1, String separator) {

		if (path1 == null)
			MyLog.waitHere("path1==null");
		String[] vetText = readSimpleText(path1);
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
	 * Restituisce l'intera linea del log per il tag
	 * 
	 * @param path1
	 * @param code1
	 * @return
	 */
	static String readFromLog(String path1, String code1) {

		// leggo una stringa dal log
		String[] vetText = readSimpleText(path1);
		if (vetText.length > 0) {
			for (int i1 = 0; i1 < vetText.length; i1++) {
				if (vetText[i1].contains(code1))
					return vetText[i1];
			}
		}
		return null;
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

}
