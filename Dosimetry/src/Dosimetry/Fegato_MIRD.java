package Dosimetry;

import java.awt.Font;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
	// non capisco che succede

	public static String pathVolatile;


	public void run(String arg) {

		
		Locale.setDefault(Locale.US);
		MyGlobals.coordinates();

		MyGlobals.desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		MyGlobals.pathPermanente = MyGlobals.desktopPath + File.separator + "DosimetryFolder" + File.separator
				+ "permanente.txt";
		pathVolatile = MyGlobals.desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";
		MyGlobals.desktopDosimetryFolderPath = MyGlobals.desktopPath + File.separator + "DosimetryFolder";
		MyGlobals.desktopImagesSubfolderPath = MyGlobals.desktopDosimetryFolderPath + File.separator + "ImagesFolder";

		ArrayList<String> arrLesioni = new ArrayList<String>();

		MyLog.log("============================");
		MyLog.log("START Fegato_MIRD");
		MyLog.log("============================");
		String pathToto = MyDialog.dialogFileSelection_FM01("Seleziona FEGATO IN TOTO", null);
		if (pathToto != null)
			arrLesioni.add(pathToto); // nelle elemento 0 ho il pathToto

		// VUOTO PATH VOLATILE PER POTERLO USARE
		MyLog.logDeleteSingle(pathVolatile);
		MyLog.logInit(pathVolatile);
		// copio parte del logFegatoInToto in volatile.txt perche'poi servira'
		MyLog.logCopyRange(pathToto, pathVolatile, 0, 182);

		String pathLesione = "";
		int count = 1;
		int out1 = 0;

		// String path1 = pathToto.substring(0, pathToto.lastIndexOf(File.separator));

		do {
			pathLesione = MyDialog.dialogFileSelection_FM01("LOOP selezione lesioni " + count++, null);
			if (pathLesione != null)
				arrLesioni.add(pathLesione);

			out1 = MyDialog.dialogAltreLesioni_FM02();

		} while (out1 == 2);

		String deletepath = MyGlobals.desktopDosimetryFolderPath + File.separator + "FegatoSenzaLesioni.txt";
		MyLog.logDeleteSingle(deletepath);

		anaLiver(arrLesioni);

		// Dosimetria_Lu177.processa(false);
		Utility.deleteAllFilesWithSpecificExtension(MyGlobals.desktopDosimetryFolderPath, "tif");
		Utility.chiudiTutto();


	}

	/**
	 * Elaborazione dei valori immagini fegato, ricavati unicamente dai log
	 * 
	 * @param arrLesioni lista delle lesioni, la posizione zero e' il fegato in toto
	 */
	public void anaLiver(ArrayList<String> arrLesioni) {

		String aux1 = "";
		// leggiamo i valori dal fegato in toto e dai vari file lesione e mettiamo il
		// tutto in un array in cui l'elemento0 e' il fegato in toto

		ArrayList<Integer> arrRoiMax24 = new ArrayList<Integer>();
		ArrayList<Integer> arrRoiMax48 = new ArrayList<Integer>();
		ArrayList<Integer> arrRoiMax120 = new ArrayList<Integer>();
		ArrayList<Double> arrThreshold24 = new ArrayList<Double>();
		ArrayList<Double> arrThreshold48 = new ArrayList<Double>();
		ArrayList<Double> arrThreshold120 = new ArrayList<Double>();
		ArrayList<Integer> arrConteggio24 = new ArrayList<Integer>();
		ArrayList<Integer> arrConteggio48 = new ArrayList<Integer>();
		ArrayList<Integer> arrConteggio120 = new ArrayList<Integer>();
		ArrayList<Integer> arrIntegrale24 = new ArrayList<Integer>();
		ArrayList<Integer> arrIntegrale48 = new ArrayList<Integer>();
		ArrayList<Integer> arrIntegrale120 = new ArrayList<Integer>();
		double[] xp1 = new double[3];
		double[] yp1 = new double[3];
		double MIRD_vol24 = 0;
		double MIRD_vol48 = 0;
		double MIRD_vol120 = 0;

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

		int decis1 = 0;
		double[] out24 = null;
		double[] out48 = null;
		double[] out120 = null;

		for (int i1 = 0; i1 < arrLesioni.size(); i1++) {
			aux1 = arrLesioni.get(i1);
			if (i1 == 0)
				MyLog.log("------ FEGATO IN TOTO ------");
			else
				MyLog.log("---- LESIONE NUMERO= " + i1 + " ----");

			int roiMax24 = Utility.parseInt(MyLog.readFromLog(aux1, "#114#", "=")); // #114#
			MyLog.log("#114# roiMax= " + roiMax24);
			int roiMax48 = Utility.parseInt(MyLog.readFromLog(aux1, "#144#", "=")); // #144#
			MyLog.log("#114# roiMax= " + roiMax48);
			int roiMax120 = Utility.parseInt(MyLog.readFromLog(aux1, "#174#", "=")); // #174#
			MyLog.log("#114# roiMax= " + roiMax120);
			double threshold24 = Double.parseDouble(MyLog.readFromLog(aux1, "#115#", "=")); // #115#
			MyLog.log("#115# threshold= " + threshold24);
			double threshold48 = Double.parseDouble(MyLog.readFromLog(aux1, "#145#", "=")); // #145#
			MyLog.log("#115# threshold= " + threshold24);
			double threshold120 = Double.parseDouble(MyLog.readFromLog(aux1, "#175#", "=")); // #175#
			MyLog.log("#115# threshold= " + threshold24);
			int conteggio24 = Utility.parseInt(MyLog.readFromLog(aux1, "#121#", "=")); // #121#
			MyLog.log("#121# conteggio24= " + conteggio24);
			int conteggio48 = Utility.parseInt(MyLog.readFromLog(aux1, "#151#", "=")); // #151#
			MyLog.log("#151# conteggio48= " + conteggio48);
			int conteggio120 = Utility.parseInt(MyLog.readFromLog(aux1, "#181#", "=")); // #181#
			MyLog.log("#181# conteggio120= " + conteggio120);
			int integrale24 = Utility.parseInt(MyLog.readFromLog(aux1, "#122#", "=")); // #122#
			MyLog.log("#122# integrale24= " + integrale24);
			int integrale48 = Utility.parseInt(MyLog.readFromLog(aux1, "#152#", "=")); // #152#
			MyLog.log("#152# integrale48= " + integrale48);
			int integrale120 = Utility.parseInt(MyLog.readFromLog(aux1, "#182#", "=")); // #182#
			MyLog.log("#182# integrale120= " + integrale120);
			arrRoiMax24.add(roiMax24);
			arrRoiMax48.add(roiMax48);
			arrRoiMax120.add(roiMax120);
			arrThreshold24.add(threshold24);
			arrThreshold48.add(threshold48);
			arrThreshold120.add(threshold120);
			arrConteggio24.add(conteggio24);
			arrConteggio48.add(conteggio48);
			arrConteggio120.add(conteggio120);
			arrIntegrale24.add(integrale24);
			arrIntegrale48.add(integrale48);
			arrIntegrale120.add(integrale120);
		}
		MyLog.log(" --------------------------------");

		aux1 = arrLesioni.get(0);
		xp1[0] = Double.parseDouble(MyLog.readFromLog(aux1, "#019#", "=")); // deltaT
		xp1[1] = Double.parseDouble(MyLog.readFromLog(aux1, "#039#", "=")); // deltaT
		xp1[2] = Double.parseDouble(MyLog.readFromLog(aux1, "#059#", "=")); // deltaT
		MIRD_vol24 = Double.parseDouble(MyLog.readFromLog(aux1, "#201#", "="));
		MIRD_vol48 = Double.parseDouble(MyLog.readFromLog(aux1, "#221#", "="));
		MIRD_vol120 = Double.parseDouble(MyLog.readFromLog(aux1, "#241#", "="));

		// nella routine subtract sottraiamo all'elemento 0 tutti i successivi elementi
		// e poi restituiamo il risultato
		int roiMaxPulito24 = subtractInteger(arrRoiMax24);
		MyLog.log("roiMaxPulito24=" + roiMaxPulito24);
		int roiMaxPulito48 = subtractInteger(arrRoiMax48);
		MyLog.log("roiMaxPulito48=" + roiMaxPulito48);
		int roiMaxPulito120 = subtractInteger(arrRoiMax120);
		MyLog.log("roiMaxPulito120=" + roiMaxPulito120);
		double thresholdPulito24 = subtractDouble(arrThreshold24);
		MyLog.log("thresholdPulito24=" + thresholdPulito24);
		double thresholdPulito48 = subtractDouble(arrThreshold48);
		MyLog.log("thresholdPulito48=" + thresholdPulito48);
		double thresholdPulito120 = subtractDouble(arrThreshold120);
		MyLog.log("thresholdPulito120=" + thresholdPulito120);
		int conteggioPulito24 = subtractInteger(arrConteggio24);
		MyLog.log("conteggioPulito24=" + conteggioPulito24);
		int conteggioPulito48 = subtractInteger(arrConteggio48);
		MyLog.log("conteggioPulito48=" + conteggioPulito48);
		int conteggioPulito120 = subtractInteger(arrConteggio120);
		MyLog.log("conteggioPulito120=" + conteggioPulito120);
		int integralePulito24 = subtractInteger(arrIntegrale24);
		MyLog.log("integralePulito24=" + integralePulito24);
		int integralePulito48 = subtractInteger(arrIntegrale48);
		MyLog.log("integralePulito48=" + integralePulito48);
		int integralePulito120 = subtractInteger(arrIntegrale120);
		MyLog.log("integralePulito120=" + integralePulito120);

		// ==========================================================================================
		// PARTE relativa alle immagini 24/48/120h
		// ==========================================================================================

		// 24h
		// se non mi ha scritto il tag #121# di volatile vuol dire che Dosimetry_v2 non
		// ha analizzato la immagine 24h (probabile cancel dato al menu)

		if (MyLog.readFromLog(pathVolatile, "#121#", "=") == null) {
			MyLog.waitHere();
			return;
		}

		double[] in24 = new double[5];
//		double[] in1 = new double[5];
//		double[] in1 = new double[5];
		in24[0] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#018#", "=")); // acquisition duration
//		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#121#", "=")); // pixel number over
		// threshold
		in24[1] = conteggioPulito24;

		in24[2] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#003#", "=")); // activity
		in24[3] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#115#", "=")); // contouring threshold
																						// level
//		in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#122#", "=")); // over threshold count
		in24[4] = integralePulito24;
		// integral
		MyLog.logVector(in24, "in24");

		out24 = Utility.MIRD_point(in24);
		MyLog.logVector(out24, "out24");

		// 48h
		// se non mi ha scritto il tag #151# di volatile vuol dire che Dosimetry_v2 non
		// ha analizzato la immagine 24h (probabile cancel dato al menu)
		if (MyLog.readFromLog(pathVolatile, "#151#", "=") == null)
			return;
		double[] in48 = new double[5];
		in48[0] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#038#", "=")); // acquisition duration
//		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#151#", "=")); // pixel number over
		// threshold
		in48[1] = conteggioPulito48;
		in48[2] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#003#", "=")); // activity
		in48[3] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#145#", "=")); // contouring threshold
																						// level
//		in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#152#", "=")); // over threshold count
		in48[4] = integralePulito48;
		// integral
		MyLog.logVector(in48, "in48");
		out48 = Utility.MIRD_point(in48);
		MyLog.logVector(out48, "out48");

		// 120h
		// se non mi ha scritto il tag #181# di volatile vuol dire che Dosimetry_v2 non
		// ha analizzato la immagine 24h (probabile cancel dato al menu)
		if (MyLog.readFromLog(pathVolatile, "#181#", "=") == null)
			return;
		double[] in120 = new double[5];
		in120[0] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#058#", "=")); // acquisition duration
//		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#181#", "=")); // pixel number over
		in120[1] = conteggioPulito120;
		// threshold
		in120[2] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#003#", "=")); // activity
		in120[3] = Double.parseDouble(MyLog.readFromLog(pathVolatile, "#175#", "=")); // contouring threshold
																						// level
//		in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#182#", "=")); // over threshold count
		in120[4] = integralePulito120;
		// integral
		MyLog.logVector(in120, "in120");
		out120 = Utility.MIRD_point(in120);

		MyLog.logVector(out120, "out120");

		// Mostro i 3 volumi calcolati ed i punti, senza fit, in modo che, con LP33
		// venga scelto l'eventuale punto da togliere

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		// yp1[0] = integralePulito24;
		// yp1[1] = integralePulito48;
		// yp1[2] = integralePulito120;

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		yp1[0] = out24[2];
		yp1[1] = out48[2];
		yp1[2] = out120[2];

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		int count5 = 194;
		String aux5 = "";
		aux5 = "#" + String.format("%03d", count5++) + "#\t----- POINT SELECTION ------------------";
		MyLog.logAppend(pathVolatile, aux5);

		String pathImage = System.getProperty("user.home") + File.separator + "Desktop" + File.separator
				+ "DosimetryFolder" + File.separator;

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		do {

			MyLog.logVector(xp1, "xp1");
			MyLog.logVector(yp1, "yp1");
			////////////////////////////////////////////////////////////////////////
			// MyLog.waitHere("PUNTO PL01 - LP33");
			////////////////////////////////////////////////////////////////////////

			String titolo1 = "Punti";
			MyGlobals.titPL01 = MyPlot.PL01_MIRD_pointsPlotter(xp1, yp1, null, titolo1, "24h=red 48h=green 120h=blue");

			boolean[] puntiSelezionati = MyDialog.pointsSelection_LP33(); /// selezione dei 2 o 3 punti su cui in
																			/// seguito
			/// fare il fit
			Utility.closePlot(MyGlobals.titPL01);

			int count = 0;
			boolean threepoints = false;
			for (int i1 = 0; i1 < puntiSelezionati.length; i1++) {
				if (puntiSelezionati[i1] == true)
					count++;
			}
			if (count == 3)
				threepoints = true;

			// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
			// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
			// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

			double[] vetOut4 = Utility.processaCalcolaFit2or3(xp1, yp1, MIRD_vol24, MIRD_vol48, MIRD_vol120, pathImage,
					puntiSelezionati);

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

			double[] vetInput = new double[14];

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

			/// qui bisogna introdurre la decisione e l'eventuale ritorno
			decis1 = MyDialog.MIRD_display_LP68(vetInput); // accetta risultati o ripeti analisi
			if (decis1 == 0)
				return;
//		// boolean fit = false;
			// if (decis1 == 1) {

		} while (decis1 < 2);

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// Vado a riscrivere i TAG #122#, #152, #182#

		aux5 = "";

//		aux5 = "#114#\tMaximum lesion count AGGIORNATO= " + roiMaxPulito24;
//		Utility.logModify(pathVolatile, "#114#", aux5);
//		aux5 = "#144#\tMaximum lesion count AGGIORNATO= " + roiMaxPulito48;
//		Utility.logModify(pathVolatile, "#144#", aux5);
//		aux5 = "#174#\tMaximum lesion count AGGIORNATO= " + roiMaxPulito120;
//		Utility.logModify(pathVolatile, "#174#", aux5);
//
//		aux5 = "#115#\tContouring threshold level AGGIORNATO= " + thresholdPulito24;
//		Utility.logModify(pathVolatile, "#115#", aux5);
//		aux5 = "#145#\tContouring threshold level AGGIORNATO= " + thresholdPulito48;
//		Utility.logModify(pathVolatile, "#145#", aux5);
//		aux5 = "#175#\tContouring threshold level AGGIORNATO= " + thresholdPulito120;
//		Utility.logModify(pathVolatile, "#175#", aux5);

		aux5 = "#121#\tPixel number over threshold level AGGIORNATO= " + conteggioPulito24;
		MyLog.logModify(pathVolatile, "#121#", aux5);
		aux5 = "#151#\tPixel number over threshold level AGGIORNATO= " + conteggioPulito48;
		MyLog.logModify(pathVolatile, "#151#", aux5);
		aux5 = "#181#\tPixel number over threshold level AGGIORNATO= " + conteggioPulito120;
		MyLog.logModify(pathVolatile, "#181#", aux5);

		aux5 = "#122#\tintegrale24 AGGIORNATO= " + integralePulito24;
		MyLog.logModify(pathVolatile, "#122#", aux5);
		aux5 = "#152#\tintegrale48 AGGIORNATO= " + integralePulito48;
		MyLog.logModify(pathVolatile, "#152#", aux5);
		aux5 = "#182#\tintegrale120 AGGIORNATO= " + integralePulito120;
		MyLog.logModify(pathVolatile, "#182#", aux5);

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// qui si devono inserire gli ulteriori calcoli

		// ================= POSTSCRITTURA ===========================================
		// UNA VOLTA CHE L'OPERATORE HA DETTO SI, SCRIVIAMO TUTTA LA MONNEZZA IN
		// VOLATILE, IN ATTESA DI CONOSCERE IL NOME CHE DARANNO ALLA LESIONE
		// ============================================================================

		boolean flanagan = false;

		if (Double.isNaN(SmAtilde))
			flanagan = false;
		else
			flanagan = true;

		count5 = 200;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---- MIRD CALCULATION 24h ----";
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol24= " + out24[0];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal24= " + out24[1];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv24= " + out24[2];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		count5 = 220;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---- MIRD CALCULATION 48h ----";
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol48= " + out48[0];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal48= " + out48[1];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv48= " + out48[2];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		count5 = 240;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---- MIRD CALCULATION 120h ----";
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol120= " + out120[0];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal120= " + out120[1];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv120= " + out120[2];
		MyLog.logAppend(pathVolatile, aux5);
		MyLog.log(aux5);

		if (!flanagan) {
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// CON IMAGEJ E BASTA
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

			count5 = 260;
			aux5 = "#" + String.format("%03d", count5++) + "#\t----- MIRD FIT RESULTS IMAGEJ --------";
			MyLog.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD IJ FIT param 0= " + AA;
			MyLog.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD IJ FIT param 1= " + aa;
			MyLog.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FIT R^2= " + Rsquared;
			MyLog.logAppend(pathVolatile, aux5);
		} else {
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// CON FLANAGAN E BASTA
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			count5 = 270;
			aux5 = "#" + String.format("%03d", count5++) + "#\t----- MIRD FIT RESULTS FLANAGAN --------";
			MyLog.logAppend(pathVolatile, aux5);

			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FLANAGAN FIT param 0= " + AA;
			MyLog.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FLANAGAN FIT param 1= " + aa;
			MyLog.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FIT R^2= " + Rsquared;
			MyLog.logAppend(pathVolatile, aux5);
		}

		count5 = 300;
		if (flanagan) {
			aux5 = "#" + String.format("%03d", count5++) + "#\t---TRE PUNTI SELEZIONATI ELABORATI CON FLANAGAN------";
			MyLog.logAppend(pathVolatile, aux5);
		} else {
			aux5 = "#" + String.format("%03d", count5++) + "#\t---DUE PUNTI SELEZIONATI ELABORATI CON IMAGEJ -------";
			MyLog.logAppend(pathVolatile, aux5);
		}
		aux5 = "#" + String.format("%03d", count5++) + "#\tparametro A= " + AA;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tparametro a= " + aa;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tmAtilde= " + mAtilde;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tdisintegrazioni= " + disintegrazioni;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tuptake[%]= " + uptake * 100;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tmassa= " + massa;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\ttmezzo= " + tmezzo;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\ttau= " + tau;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tdose= " + dose;
		MyLog.logAppend(pathVolatile, aux5);

		// if (count3 == 3) { /// lo eseguo sempre
		aux5 = "#" + String.format("%03d", count5++) + "#\t--------- CALCOLO ERRORI ----------";
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\terrore SA= " + SA;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\terrore Sa= " + Sa;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSmAtilde= " + SmAtilde;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tS# disintegrazioni= " + Sdisintegrazioni;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSuptake= " + Suptake;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSmassa= " + Smassa;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tStmezzo= " + Stmezzo;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tStau= " + Stau;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSdose= " + Sdose;
		MyLog.logAppend(pathVolatile, aux5);

		count5 = 500;
		aux5 = "#" + String.format("%03d", count5++) + "#\t-------- CALCOLO DOSE -----------";
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s1= " + s1;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s2= " + s2;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m1= " + m1;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m2= " + m2;
		MyLog.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose massa= " + massa;
		MyLog.logAppend(pathVolatile, aux5);

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		int pos = pathVolatile.lastIndexOf(File.separator);
		String pathBase = pathVolatile.substring(0, pos);
		String pathLesione = pathBase + File.separator + "FegatoSenzaLesioni.txt";
		MyLog.logEnd(pathVolatile);
		MyLog.logMove(pathLesione, pathVolatile);
		MyLog.logInit(pathVolatile);

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
