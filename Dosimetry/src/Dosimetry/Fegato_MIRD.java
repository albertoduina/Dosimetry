package Dosimetry;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.util.FontUtil;

public class Fegato_MIRD implements PlugIn {

	//
	// DATI SOMMINISTRAZIONE #001#-#009#
	// IMAGE INFO 24h #010#-#029#
	// IMAGE INFO 48 h #030#-#049#
	// IMAGE INFO 120 h #050#-#069#
	// PATIENT-DOSIMETRY INFO 24 h #100#-#129#
	// PATIENT-DOSIMETRY INFO 48 h #130#-#159#
	// PATIENT-DOSIMETRY INFO 24 h #160#-#199#
	//

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

	public void run(String arg) {

		desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
		pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";
		desktopDosimetryFolderPath = desktopPath + File.separator + "DosimetryFolder";
		desktopImagesSubfolderPath = desktopDosimetryFolderPath + File.separator + "ImagesFolder";

		ArrayList<String> arrLesioni = new ArrayList<String>();

		MyLog.log("============================");
		MyLog.log("START Fegato_MIRD");
		MyLog.log("============================");
		FontUtil fu = new FontUtil();
		String fontStyle = "Arial";
		Font titleFont = fu.getFont(fontStyle, 1, 18);
		Font textFont = fu.getFont(fontStyle, 2, 16);
		Font defaultFont = fu.getFont(fontStyle, 0, 14);
		boolean ok1 = false;
		int point1 = -1;
		String pathToto = Utility.dialogFileSelection_FM01("Seleziona FEGATO IN TOTO",
				desktopPath + File.separator + "DosimetryFolder" + File.separator);
		if (pathToto != null)
			arrLesioni.add(pathToto); // nelle elemento 0 ho il pathToto
		// copio parte del logFegatoInToto in volatile.txt perche'poi servira'

		Utility.logDeleteSingle(pathVolatile);
		MyLog.waitHere("dopo delete di volatile");
		Utility.logInit(pathVolatile);
		pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";

		Utility.logCopyRange(pathToto, pathVolatile, 0, 182);
		MyLog.waitHere("dopo copia in volatile");

		String pathLesione = "";
		int count = 1;
		int out1 = 0;
		do {
			pathLesione = Utility.dialogFileSelection_FM01("LOOP selezione lesioni " + count++,
					desktopPath + File.separator + "DosimetryFolder" + File.separator);
			if (pathLesione != null)
				arrLesioni.add(pathLesione);

			out1 = Utility.dialogAltreLesioni_FM02();

		} while (out1 == 2);
		brisighello(arrLesioni);

		// Dosimetria_Lu177.processa(false);

	}

	public void brisighello(ArrayList<String> arrLesioni) {

		String aux1 = "";
		// leggiamo i valori dal fegato in toto e dai vari file lesione e mettiamo il
		// tutto in un array in cui l'elemento0 e' il fegato in toto

		ArrayList<Integer> arrRoiMax = new ArrayList<Integer>();
		ArrayList<Double> arrThreshold = new ArrayList<Double>();
		ArrayList<Integer> arrConteggio = new ArrayList<Integer>();
		ArrayList<Integer> arrIntegrale24 = new ArrayList<Integer>();
		ArrayList<Integer> arrIntegrale48 = new ArrayList<Integer>();
		ArrayList<Integer> arrIntegrale120 = new ArrayList<Integer>();
		double[] xp1 = new double[3];
		double[] yp1 = new double[3];
		double MIRD_vol24 = 0;
		double MIRD_vol48 = 0;
		double MIRD_vol120 = 0;

		for (int i1 = 0; i1 < arrLesioni.size(); i1++) {
			aux1 = arrLesioni.get(i1);
			int roiMax = Utility.parseInt(Utility.readFromLog(aux1, "#114#", "=")); // #114#
			MyLog.log("#114# roiMax= " + roiMax);
			double threshold = Double.parseDouble(Utility.readFromLog(aux1, "#115#", "=")); // #115#
			MyLog.log("#115# threshold= " + threshold);
			int conteggio = Utility.parseInt(Utility.readFromLog(aux1, "#121#", "=")); // #121#
			MyLog.log("#121# conteggio= " + conteggio);
			int integrale24 = Utility.parseInt(Utility.readFromLog(aux1, "#122#", "=")); // #122#
			MyLog.log("#122# integrale24= " + integrale24);
			int integrale48 = Utility.parseInt(Utility.readFromLog(aux1, "#152#", "=")); // #152#
			MyLog.log("#152# integrale48= " + integrale48);
			int integrale120 = Utility.parseInt(Utility.readFromLog(aux1, "#182#", "=")); // #182#
			MyLog.log("#182# integrale120= " + integrale120);
			arrRoiMax.add(roiMax);
			arrThreshold.add(threshold);
			arrConteggio.add(conteggio);
			arrIntegrale24.add(integrale24);
			arrIntegrale48.add(integrale48);
			arrIntegrale120.add(integrale120);

		}

		aux1 = arrLesioni.get(0);
		xp1[0] = Double.parseDouble(Utility.readFromLog(aux1, "#019#", "=")); // deltaT
		xp1[1] = Double.parseDouble(Utility.readFromLog(aux1, "#039#", "=")); // deltaT
		xp1[2] = Double.parseDouble(Utility.readFromLog(aux1, "#059#", "=")); // deltaT
		MIRD_vol24 = Double.parseDouble(Utility.readFromLog(aux1, "#201#", "="));
		MIRD_vol48 = Double.parseDouble(Utility.readFromLog(aux1, "#221#", "="));
		MIRD_vol120 = Double.parseDouble(Utility.readFromLog(aux1, "#241#", "="));

		// nella routine subtract sottraiamo all'elemento 0 tutti i successivi elementi
		// e poi restituiamo il risultato
		int roiMaxPulito = subtractInteger(arrRoiMax);
		MyLog.log("roiMaxPulito=" + roiMaxPulito);
		double thresholdPulito = subtractDouble(arrThreshold);
		MyLog.log("thresholdPulito=" + thresholdPulito);
		int conteggioPulito = subtractInteger(arrConteggio);
		MyLog.log("conteggioPulito=" + conteggioPulito);
		int integrale24 = subtractInteger(arrIntegrale24);
		MyLog.log("integrale24=" + integrale24);
		int integrale48 = subtractInteger(arrIntegrale48);
		MyLog.log("integrale48=" + integrale48);
		int integrale120 = subtractInteger(arrIntegrale120);
		MyLog.log("integrale120=" + integrale120);

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		yp1[0] = Math.abs(integrale24);
		yp1[1] = Math.abs(integrale48);
		yp1[2] = Math.abs(integrale120);

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		Dosimetria_Lu177.processa(xp1, yp1, MIRD_vol24, MIRD_vol48, MIRD_vol120);
		
		///  qui bisogna introdurre la decisione e l'eventuale ritorno
		
		
		
		

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// Vado a riscrivere i TAG #122#, #152à, #182#

		String aux5 = "";
		aux5 = "#122#\tintegrale24 AGGIORNATO= " + integrale24;
//		Utility.logAppend(pathVolatile, aux5);
		Utility.logModify(pathVolatile, "#122#", aux5);
		aux5 = "#152#\tintegrale48 AGGIORNATO= " + integrale48;
//		Utility.logAppend(pathVolatile, aux5);
		Utility.logModify(pathVolatile, "#152#", aux5);
		aux5 = "#182#\tintegrale120 AGGIORNATO= " + integrale120;
//		Utility.logAppend(pathVolatile, aux5);
		Utility.logModify(pathVolatile, "#182#", aux5);

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		
		
		int pos = pathVolatile.lastIndexOf(File.separator);
		String pathBase = pathVolatile.substring(0, pos);
		String pathLesione = pathBase + File.separator + "FegatoSenzaLesioni.txt";
		Utility.logEnd(pathVolatile);
		Utility.logMove(pathLesione, pathVolatile);
		Utility.logInit(pathVolatile);



		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

	}

	int subtractInteger(ArrayList<Integer> arrIn1) {
		int toto = arrIn1.get(0);
		for (int i1 = 1; i1 < arrIn1.size(); i1++) {
			toto = toto - arrIn1.get(i1);
		}
		return toto;
	}

	double subtractDouble(ArrayList<Double> arrIn1) {
		double toto = arrIn1.get(0);
		for (int i1 = 1; i1 < arrIn1.size(); i1++) {
			toto = toto - arrIn1.get(i1);
		}
		return toto;
	}

}
