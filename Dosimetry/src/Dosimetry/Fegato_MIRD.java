package Dosimetry;

import java.awt.Font;
import java.io.File;
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

	static String fontStyle = "Arial";
	static Font defaultFont = FontUtil.getFont(fontStyle, Font.PLAIN, 13);
	static Font textFont = FontUtil.getFont(fontStyle, Font.ITALIC, 16);
	static Font titleFont = FontUtil.getFont(fontStyle, Font.BOLD, 16);

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

		Locale.setDefault(Locale.US);
		desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
		pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";
		desktopDosimetryFolderPath = desktopPath + File.separator + "DosimetryFolder";
		desktopImagesSubfolderPath = desktopDosimetryFolderPath + File.separator + "ImagesFolder";

		ArrayList<String> arrLesioni = new ArrayList<String>();

		MyLog.log("============================");
		MyLog.log("START Fegato_MIRD");
		MyLog.log("============================");
		String pathToto = Utility.dialogFileSelection_FM01("Seleziona FEGATO IN TOTO",
				desktopPath + File.separator + "DosimetryFolder" + File.separator);
		if (pathToto != null)
			arrLesioni.add(pathToto); // nelle elemento 0 ho il pathToto
		// copio parte del logFegatoInToto in volatile.txt perche'poi servira'

		Utility.logDeleteSingle(pathVolatile);
		Utility.logInit(pathVolatile);
		pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";

		Utility.logCopyRange(pathToto, pathVolatile, 0, 182);

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

		String deletepath = desktopPath + File.separator + "DosimetryFolder" + File.separator
				+ "FegatoSenzaLesioni.txt";
		Utility.logDeleteSingle(deletepath);

		anaLiver(arrLesioni);

		// Dosimetria_Lu177.processa(false);

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
		double[] out24=null;
		double[] out48=null;
		double[] out120=null;
		
		
		

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
		
		
		
		// ==========================================================================================
		// PARTE relativa alle immagini 24/48/120h
		// ==========================================================================================

		// 24h
		// se non mi ha scritto il tag #121# di volatile vuol dire che Dosimetry_v2 non
		// ha analizzato la immagine 24h (probabile cancel dato al menu)

		if (Utility.readFromLog(pathVolatile, "#121#", "=") == null)
			return;

		double[] in1 = new double[5];
		in1[0] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#018#", "=")); // acquisition duration
		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#121#", "=")); // pixel number over
																						// threshold
		in1[2] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#003#", "=")); // activity
		in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#115#", "=")); // contouring threshold
																						// level
		in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#122#", "=")); // over threshold count
																						// integral
		out24 = Utility.MIRD_point(in1);

		// 48h
		// se non mi ha scritto il tag #151# di volatile vuol dire che Dosimetry_v2 non
		// ha analizzato la immagine 24h (probabile cancel dato al menu)
		if (Utility.readFromLog(pathVolatile, "#151#", "=") == null)
			return;
		in1[0] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#038#", "=")); // acquisition duration
		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#151#", "=")); // pixel number over
																						// threshold
		in1[2] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#003#", "=")); // activity
		in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#145#", "=")); // contouring threshold
																						// level
		in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#152#", "=")); // over threshold count
																						// integral
		out48 = Utility.MIRD_point(in1);

		// 120h
		// se non mi ha scritto il tag #181# di volatile vuol dire che Dosimetry_v2 non
		// ha analizzato la immagine 24h (probabile cancel dato al menu)
		if (Utility.readFromLog(pathVolatile, "#181#", "=") == null)
			return;
		in1[0] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#058#", "=")); // acquisition duration
		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#181#", "=")); // pixel number over
																						// threshold
		in1[2] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#003#", "=")); // activity
		in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#175#", "=")); // contouring threshold
																						// level
		in1[4] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#182#", "=")); // over threshold count
																						// integral
		out120 = Utility.MIRD_point(in1);


		// Mostro i 3 volumi calcolati ed i punti, senza fit, in modo che, con LP33
		// venga scelto l'eventuale punto da togliere

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		yp1[0] = integrale24;
		yp1[1] = integrale48;
		yp1[2] = integrale120;

		int count5 = 194;
		String aux5 = "";
		aux5 = "#" + String.format("%03d", count5++) + "#\t----- POINT SELECTION ------------------";
		Utility.logAppend(pathVolatile, aux5);

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§

		do {

			double[] vetOut4 = Dosimetria_Lu177.processa(xp1, yp1, MIRD_vol24, MIRD_vol48, MIRD_vol120);

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
			decis1 = Dosimetria_Lu177.MIRD_display_LP68(vetInput); // accetta risultati o ripeti analisi
			if (decis1 == 0)
				return;
//		// boolean fit = false;
			// if (decis1 == 1) {

		} while (decis1 < 2);

		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// §§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§§
		// Vado a riscrivere i TAG #122#, #152à, #182#

		aux5 = "";
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
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol24= " + out24[0];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal24= " + out24[1];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv24= " + out24[2];
		Utility.logAppend(pathVolatile, aux5);
		count5 = 220;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---- MIRD CALCULATION 48h ----";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol48= " + out48[0];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal48= " + out48[1];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv48= " + out48[2];
		Utility.logAppend(pathVolatile, aux5);
		count5 = 240;
		aux5 = "#" + String.format("%03d", count5++) + "#\t---- MIRD CALCULATION 120h ----";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_vol120= " + out120[0];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_fatCal120= " + out120[1];
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD_attiv120= " + out120[2];
		Utility.logAppend(pathVolatile, aux5);

		if (!flanagan) {
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// CON IMAGEJ E BASTA
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

			count5 = 260;
			aux5 = "#" + String.format("%03d", count5++) + "#\t----- MIRD FIT RESULTS IMAGEJ --------";
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD IJ FIT param 0= " + AA;
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD IJ FIT param 1= " + aa;
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FIT R^2= " + Rsquared;
			Utility.logAppend(pathVolatile, aux5);
		} else {
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// CON FLANAGAN E BASTA
			// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			count5 = 270;
			aux5 = "#" + String.format("%03d", count5++) + "#\t----- MIRD FIT RESULTS FLANAGAN --------";
			Utility.logAppend(pathVolatile, aux5);

			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FLANAGAN FIT param 0= " + AA;
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FLANAGAN FIT param 1= " + aa;
			Utility.logAppend(pathVolatile, aux5);
			aux5 = "#" + String.format("%03d", count5++) + "#\tMIRD FIT R^2= " + Rsquared;
			Utility.logAppend(pathVolatile, aux5);
		}

		count5 = 300;
		if (flanagan) {
			aux5 = "#" + String.format("%03d", count5++) + "#\t---TRE PUNTI SELEZIONATI ELABORATI CON FLANAGAN------";
			Utility.logAppend(pathVolatile, aux5);
		} else {
			aux5 = "#" + String.format("%03d", count5++) + "#\t---DUE PUNTI SELEZIONATI ELABORATI CON IMAGEJ -------";
			Utility.logAppend(pathVolatile, aux5);
		}
		aux5 = "#" + String.format("%03d", count5++) + "#\tparametro A= " + AA;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tparametro a= " + aa;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tmAtilde= " + mAtilde;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tdisintegrazioni= " + disintegrazioni;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tuptake[%]= " + uptake;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tmassa= " + massa;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\ttmezzo= " + tmezzo;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\ttau= " + tau;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tdose= " + dose;
		Utility.logAppend(pathVolatile, aux5);

		// if (count3 == 3) { /// lo eseguo sempre
		aux5 = "#" + String.format("%03d", count5++) + "#\t--------- CALCOLO ERRORI ----------";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\terrore SA= " + SA;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\terrore Sa= " + Sa;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSmAtilde= " + SmAtilde;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tS# disintegrazioni= " + Sdisintegrazioni;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSuptake= " + Suptake;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSmassa= " + Smassa;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tStmezzo= " + Stmezzo;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tStau= " + Stau;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tSdose= " + Sdose;
		Utility.logAppend(pathVolatile, aux5);

		count5 = 500;
		aux5 = "#" + String.format("%03d", count5++) + "#\t-------- CALCOLO DOSE -----------";
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s1= " + s1;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose s2= " + s2;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m1= " + m1;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose m2= " + m2;
		Utility.logAppend(pathVolatile, aux5);
		aux5 = "#" + String.format("%03d", count5++) + "#\tUtility.MIRD_calcoloDose massa= " + massa;
		Utility.logAppend(pathVolatile, aux5);

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
