package Dosimetry;

import java.awt.Font;
import java.io.File;
import java.util.Locale;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.util.FontUtil;

/**
 * @version v3
 * @author Date 30 jan 2023
 */
public class S_VoxelDosimetry implements PlugIn {

	static String fontStyle = "Arial";
	static Font defaultFont = FontUtil.getFont(fontStyle, Font.PLAIN, 13);
	static Font textFont = FontUtil.getFont(fontStyle, Font.ITALIC, 16);
	static Font titleFont = FontUtil.getFont(fontStyle, Font.BOLD, 16);
	String[] config = null;
	boolean loggoVoxels = false;
	int[] coordinateVoxels = null;
	static int coordX;
	static int coordY;
	static int coordZ;
	long start1;
	long start2;
	long start3;
	long start4;
	long end1;
	long end2;
	long end3;
	long end4;
	int lato;
	int mezzo;

	public void run(String arg) {

		Locale.setDefault(Locale.US);
		lato = Utility.latoCubo();
		mezzo = (lato - 1) / 2;

		String str1 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator + "ImagesFolder" + File.separator;
		String str2 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator;
		String str3 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder";
		config = Utility.leggiConfig("DosimetryConfig.txt");
		if (config == null) {
			MyLog.log("MANCA CONFIG");
			loggoVoxels = false;
		} else {
			loggoVoxels = Utility.leggiLogVoxelsConfig(config);
			MyLog.log("LOGGO VOXELS= " + loggoVoxels);

			coordinateVoxels = Utility.leggiCoordinateVoxels(config);
			MyLog.waitHere("loggoVoxels= " + loggoVoxels + "\ncoordinateVoxels[0] X= " + coordinateVoxels[0]
					+ "\ncoordinateVoxels[1] Y= " + coordinateVoxels[1] + "\ncoordinateVoxels[2] Z= "
					+ coordinateVoxels[2]);
			coordX = coordinateVoxels[0];
			coordY = coordinateVoxels[1];
			coordZ = coordinateVoxels[2];
			MyLog.log("coordX= " + coordinateVoxels[0]);
			MyLog.log("coordY= " + coordinateVoxels[1]);
			MyLog.log("coordZ= " + coordinateVoxels[2]);
			MyLog.log("lato= " + lato);
			MyLog.log("mezzo= " + mezzo);
		}

		String lesione1 = "";
		String lesione2 = "";
		String lesione3 = "";
		String lesione4 = "";
		String out1 = "";
		String startingDir1 = "";

		if (arg == "") {
			out1 = Utility.dialogSceltaLesione_SV02(str3);

		} else {
			out1 = arg;
		}

		int[] vetH = { 24, 48, 120 };
		boolean ok = false;
		for (int i1 = 0; i1 < vetH.length; i1++) {
			lesione1 = str2 + out1 + vetH[i1] + "h.tif";
			lesione2 = str2 + out1 + ".txt";
			startingDir1 = str1 + vetH[i1] + "h" + File.separator + "SPECT";
			start1 = System.currentTimeMillis();

			ok = caricaMemoriazza(startingDir1, lesione1, vetH[i1], lesione2);
			if (!ok)
				break;

		}

		File fil = new File(lesione1);
		// Utility.deleteFile(fil); // ESCLUSO PER PROVE CAXXXO MI TIRAVA SCEMO
		// Utility.chiudiTutto();

		// C:\Users\Alberto\Desktop\DosimetryFolder\ImagesFolder\48h\SPECT
		MyLog.waitHere("FINE LAVORO");
	}

	/**
	 * Nella DosimetryFolder abbiamo creato gli stackMask 24h, 48h e 120h in formato
	 * tiff e col nome della lesione. Per ogni pixel che ha la mask>0 (255) effettuo
	 * i calcoli descritti in Formule11012023 e metto il risultato all'interno di
	 * uno stack float. A questo punto inizieremo con le cubbature
	 * 
	 * @param pathStackIn
	 * @param pathStackMask
	 * @param ore
	 * @param pathLesione
	 */
	boolean caricaMemoriazza(String pathStackIn, String pathStackMask, int ore, String pathLesione) {

		ImagePlus impStackIn = null;
		ImagePlus impStackMask = null;
		int width2 = 0;
		int height2 = 0;
		int depth2 = 0;
		double acqDuration = 0;
		double fatCal = 0;
		double deltaT = 0;
		int x2 = 0;
		int y2 = 0;
		int z2 = 0;
		int x3 = 0;
		int y3 = 0;
		int z3 = 0;

		switch (ore) {
		case 24:
			acqDuration = Double.parseDouble(Utility.readFromLog(pathLesione, "#018#", "=", true)); // acqduration 24h
																									// // // 24h
			deltaT = Double.parseDouble(Utility.readFromLog(pathLesione, "#019#", "=", true)); // deltaT 24h
			fatCal = Double.parseDouble(Utility.readFromLog(pathLesione, "#202#", "=", true)); // fatCal24h
			break;
		case 48:
			acqDuration = Double.parseDouble(Utility.readFromLog(pathLesione, "#038#", "=", true)); // acqduration 48 //
																									// // 48h
			deltaT = Double.parseDouble(Utility.readFromLog(pathLesione, "#039#", "=", true)); // deltaT 24h
			fatCal = Double.parseDouble(Utility.readFromLog(pathLesione, "#222#", "=", true)); // fatCal48h
			break;
		case 120:
			acqDuration = Double.parseDouble(Utility.readFromLog(pathLesione, "#058#", "=", true)); // acqduration 120h
			deltaT = Double.parseDouble(Utility.readFromLog(pathLesione, "#059#", "=", true)); // deltaT 24h
			fatCal = Double.parseDouble(Utility.readFromLog(pathLesione, "#242#", "=", true)); // fatCal120h
			break;
		}
		double par_a = Double.parseDouble(Utility.readFromLog(pathLesione, "#302#", "=", true));

		impStackIn = Utility.readStackFiles(pathStackIn);
		// convertendo a 32 bit viene eliminata la calibrazione e la noia di avere il
		// valore 0 rappresentato con 32768
		new ImageConverter(impStackIn).convertToGray32();

		impStackIn.setTitle("INPUT " + ore + "h");
		impStackMask = Utility.openImage(pathStackMask);
		int sl = Utility.MyStackCountPixels(impStackMask);
		impStackMask.setDisplayRange(50, 255);
		impStackMask.setSlice(sl);
		impStackMask.setTitle("MASK " + ore + "h");
		impStackMask.show();

		if (loggoVoxels) {
			// serve solo per DEBUG durante le prove
			x2 = coordinateVoxels[0];
			y2 = coordinateVoxels[1];
			z2 = coordinateVoxels[2];
			x3 = x2 - mezzo;
			y3 = y2 - mezzo;
			z3 = z2 - mezzo;

			Utility.loggoVoxels2(impStackIn, x2, y2, z2);
		}
		//
		// Imposto il mio cubetto in modo che "viaggi" per tutto lo stack cubico, il
		// pixel centrale del cubetto, sara'il risultato dei calcoli su tutti i pixel
		// del cubetto e verra' scritto nella corrispondente posizione dello stack
		// cubico di output.
		//
		ImageStack stackMask = impStackMask.getImageStack();
		ImageStack stackIn = impStackIn.getImageStack();

		double[] tapata1 = Utility.MyStackStatistics(impStackIn, impStackMask);
		impStackIn.setDisplayRange(tapata1[3], tapata1[7]);
		impStackIn.setSlice((int) tapata1[6]);

		impStackIn.show();

		int width1 = stackIn.getWidth();
		int height1 = stackIn.getHeight();
		int depth1 = stackIn.getSize();

		// elaborazione pixel per pixel dell'intera immagine di input, senza quindi
		// utilizzare la mask, dopo che abbiamo applicato le formule formulate in
		// formule11012023 scriviamo il risultato nel corrispondente pixel float dello
		// stackout
		// ####################################################
		// MATILDE
		// ####################################################

		double[] valIn = new double[4];
		valIn[0] = acqDuration;
		valIn[1] = fatCal;
		valIn[2] = deltaT;
		valIn[3] = par_a;

		MyLog.log("------------------------");
		MyLog.log("acqDuration= " + acqDuration);
		MyLog.log("fatCal= " + fatCal);
		MyLog.log("deltaT= " + deltaT);
		MyLog.log("par_a= " + par_a);
		MyLog.log("------------------------");

		// ####################################################################################
		// ELABORAZIONE IMMAGINE COMPLETA, SENZA MASCHERA E SENZA CUBI DI ALCUN GENERE
		// ####################################################################################
		// creazione dello stack nero di output
		int bitdepth1 = 32;
		ImageStack stackMatilde = ImageStack.create(width1, height1, depth1, bitdepth1);

		// ####################################################
		// MATILDE SENZA CUBI E SENZA PAURA
		// ####################################################

		double voxSignal = 0;
		double aTildeVoxel = 0;
		ImageProcessor inSlice1 = null;
		ImageProcessor outSlice1 = null;

		for (int z1 = 1; z1 <= depth1; z1++) {
			inSlice1 = stackIn.getProcessor(z1);
			outSlice1 = stackMatilde.getProcessor(z1);
			for (int x1 = 0; x1 < width1; x1++) {
				for (int y1 = 0; y1 < height1; y1++) {
					IJ.showStatus("aTilde " + z1 + " / " + (depth1));
					voxSignal = inSlice1.getPixelValue(x1, y1);
					aTildeVoxel = mAtildeSingleVoxel(voxSignal, acqDuration, fatCal, deltaT, par_a);
					outSlice1.putPixelValue(x1, y1, aTildeVoxel);
					if (z1 == coordZ && x1 == coordX && y1 == coordY)
						MyLog.log("voxSignal= " + voxSignal + "\nacqDuration= " + acqDuration + "\nfatCal= " + fatCal
								+ "\ndeltaT= " + deltaT + "\npar_a= " + par_a + "\naTildeVoxel= " + aTildeVoxel);
				}
			}
			Utility.stackSliceUpdater(stackMatilde, outSlice1, z1);
		}

		ImagePlus impMatilde = new ImagePlus("mAtilde " + ore + "h", stackMatilde);
		impMatilde.show();

		if (loggoVoxels) {

			Utility.loggoVoxels2(impStackIn, x2, y2, z2);
			Utility.loggoCuxels3(impStackIn, x2, y2, z2, lato, mezzo);

			Utility.loggoVoxels2(impMatilde, x2, y2, z2);
			Utility.loggoCuxels3(impMatilde, x2, y2, z2, lato, mezzo);
		}
		double[] tapata2 = Utility.MyStackStatistics(impMatilde, impStackMask);

		// -------------------------------------
		// ELABORAZIONI DEI CUBI
		// -------------------------------------

		// -------------------------------------
		// CUBO CON SVALUES
		// -------------------------------------
		ImagePlus impRubik = Utility.inCubo();
		impRubik.show();
		ImageStack stackRubik = impRubik.getImageStack();
		// -------------------------------------

		boolean log2;
		// ####################################################
		// PATATA COMPLETA CON SVALUES
		// ####################################################
		ImageStack stackPatataCompleta = ImageStack.create(width1, height1, depth1, bitdepth1);
		for (int z1 = 1 + mezzo; z1 < (depth1 - mezzo) - 2; z1++) {
			for (int y1 = mezzo; y1 < (height1 - mezzo); y1++) {
				IJ.showStatus("patataCompleta " + z1 + " / " + depth1);
				for (int x1 = mezzo; x1 < (width1 - mezzo); x1++) {
					if (x1 == coordX && y1 == coordY && z1 == coordZ)
						log2 = true;
					else
						log2 = false;
					// -------------------------------------
					float[] vetVoxels = stackMatilde.getVoxels(x1 - mezzo, y1 - mezzo, z1 - mezzo, lato, lato, lato,
							null);
					float[] vetSvalues = stackRubik.getVoxels(0, 0, 0, lato, lato, lato, null);
					// -------------------------------------
					double valPatataCompleta = 0;
					for (int i1 = 0; i1 < vetVoxels.length; i1++) {
						valPatataCompleta = valPatataCompleta + (vetVoxels[i1] * vetSvalues[i1]) / 1000.;
					}
					stackPatataCompleta.setVoxel(x1, y1, z1, valPatataCompleta);
				}
			}
		}

		ImagePlus impPatataCompleta = new ImagePlus("PatataCompleta " + ore + "h", stackPatataCompleta);
		impPatataCompleta.show();

		if (loggoVoxels) {
			Utility.loggoVoxels2(impPatataCompleta, x2, y2, z2);
			Utility.loggoCuxels4(impRubik, mezzo, mezzo, mezzo, lato, mezzo);
			Utility.loggoCuxels3(impPatataCompleta, x2, y2, z2, lato, mezzo);
		}

		double[] tapata3 = Utility.MyStackStatistics(impPatataCompleta, impStackMask);
		impPatataCompleta.setDisplayRange(tapata3[3], tapata3[7]);
		impPatataCompleta.setSlice((int) tapata3[6]);
		impPatataCompleta.show();

		// ####################################################
		// PATATA MASCHERATA
		// ####################################################

		width2 = lato;
		height2 = lato;
		depth2 = lato;
		double voxMask = 0;
		double voxPatataCompleta = 0;

		ImageStack stackPatataMascherata = ImageStack.create(width1, height1, depth1, bitdepth1);

		for (int z1 = 0; z1 < depth1 - depth2; z1++) {
			for (int x1 = 0; x1 < width1 - width2; x1++) {
				for (int y1 = 0; y1 < height1 - height2; y1++) {
					IJ.showStatus("patataMascherata " + z1 + " / " + (depth1 - depth2));
					voxMask = stackMask.getVoxel(x1, y1, z1);
					voxPatataCompleta = stackPatataCompleta.getVoxel(x1, y1, z1);
					if (voxMask > 0) {
						stackPatataMascherata.setVoxel(x1, y1, z1, voxPatataCompleta);
					}
				}
			}
		}

		ImagePlus impPatataMascherata = new ImagePlus("PATATA_MASCHERATA  " + ore + "h", stackPatataMascherata);
		if (loggoVoxels) {
			Utility.loggoVoxels2(impStackMask, x2, y2, z2);
			Utility.loggoCuxels3(impStackMask, x2, y2, z2, lato, mezzo);

			Utility.loggoVoxels2(impPatataMascherata, x2, y2, z2);
			Utility.loggoCuxels3(impPatataMascherata, x2, y2, z2, lato, mezzo);
		}
		impPatataMascherata.show();
		tapata3 = Utility.MyStackStatistics(impPatataMascherata, impStackMask);
		impPatataMascherata.setDisplayRange(tapata3[3], tapata3[7]);
		impPatataMascherata.setSlice((int) tapata3[6]);
		impPatataMascherata.show();

		end1 = System.currentTimeMillis();

//		end1 = System.nanoTime();

		String time1 = MyLog.logElapsed(start1, end1);

		int minStackX3 = (int) tapata1[0];
		int minStackY3 = (int) tapata1[1];
		int minStackZ3 = (int) tapata1[2];
		double minStackVal3 = tapata1[3];
		int maxStackX3 = (int) tapata1[4];
		int maxStackY3 = (int) tapata1[5];
		int maxStackZ3 = (int) tapata1[6];
		double maxStackVal3 = tapata1[7];
		long pixCount3 = (long) tapata1[8];
		double meanStackVal3 = tapata1[9];
		double integral3 = tapata1[10];

		int minStackX1 = (int) tapata2[0];
		int minStackY1 = (int) tapata2[1];
		int minStackZ1 = (int) tapata2[2];
		double minStackVal1 = tapata2[3];
		int maxStackX1 = (int) tapata2[4];
		int maxStackY1 = (int) tapata2[5];
		int maxStackZ1 = (int) tapata2[6];
		double maxStackVal1 = tapata2[7];
		long pixCount1 = (long) tapata2[8];
		double meanStackVal1 = tapata2[9];
		double integral1 = tapata2[10];

		int minStackX2 = (int) tapata3[0];
		int minStackY2 = (int) tapata3[1];
		int minStackZ2 = (int) tapata3[2];
		double minStackVal2 = tapata3[3];
		int maxStackX2 = (int) tapata3[4];
		int maxStackY2 = (int) tapata3[5];
		int maxStackZ2 = (int) tapata3[6];
		double maxStackVal2 = tapata3[7];
		long pixCount2 = (long) tapata3[8];
		double meanStackVal2 = tapata3[9];
		double integral2 = tapata3[10];

		NonBlockingGenericDialog resultsDialog = new NonBlockingGenericDialog("SV05 - Results");
		resultsDialog.addMessage("Results " + ore + "h", titleFont);
		resultsDialog.setFont(defaultFont);

		resultsDialog.addMessage("======== IMMAGINE INPUT  MASCHERATA ======");
		resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal3) + "     x= " + minStackX3
				+ "    y= " + minStackY3 + "    z= " + minStackZ3);
		resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal3) + "    x= " + maxStackX3
				+ "    y= " + maxStackY3 + "    z= " + maxStackZ3);

		resultsDialog
				.addMessage("meanStackVal= " + String.format("%.4f", meanStackVal3) + "        pixCount= " + pixCount3);
		resultsDialog.addMessage("integral= " + String.format("%.4f", integral3));
		resultsDialog.addMessage("======== MATILDE MASCHERATA =============");
		resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal1) + "       x= " + minStackX1
				+ "    y= " + minStackY1 + "    z= " + minStackZ1);
		resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal1) + "    x= " + maxStackX1
				+ "    y= " + maxStackY1 + "    z= " + maxStackZ1);

		resultsDialog
				.addMessage("meanStackVal= " + String.format("%.4f", meanStackVal1) + "        pixCount= " + pixCount1);
		resultsDialog.addMessage("integral= " + String.format("%.4f", integral1));

		resultsDialog.addMessage("======== PATATA MASCHERATA ==============");
		resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal2) + "     x= " + minStackX2
				+ "    y= " + minStackY2 + "    z= " + minStackZ2);
		resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal2) + "    x= " + maxStackX2
				+ "    y= " + maxStackY2 + "    z= " + maxStackZ2);

		resultsDialog
				.addMessage("meanStackVal= " + String.format("%.4f", meanStackVal2) + "        pixCount= " + pixCount2);
		resultsDialog.addMessage("integral= " + String.format("%.4f", integral2));
		resultsDialog.addMessage("\n\n\nTempo impiegato= " + time1);
		resultsDialog.showDialog();

		if (resultsDialog.wasCanceled())
			return false;

		Utility.calculateDVH(impPatataMascherata, ore);
		return true;
	}

	void loggoTabellaBella(float[] vetTabella) {

		MyLog.log("############## vetTabella VALORI CONVERSIONE ##############");
		String aux1 = "";
		for (int i3 = 0; i3 < lato; i3++) {
			aux1 = aux1 + String.format("%04d", i3) + ";______";
		}
		MyLog.log("____pixel;_________" + aux1);
		int count = 0;
		for (int i1 = 0; i1 < vetTabella.length - lato + 1; i1 = i1 + lato) {
			aux1 = "";
			for (int i2 = 0; i2 < lato; i2++) {
				aux1 = aux1 + String.format("%010.4e", vetTabella[i1 + i2]) + ";  ";
			}
			MyLog.log("riga " + String.format("%04d", count) + "    " + aux1);
			count++;
		}
	}

	void creoStackTabellaBella(float[] vetTabella) {

		int width = lato;
		int height = lato;
		int depth = lato;
		int bitdepth = 32;

		ImageStack stack = ImageStack.create(width, height, depth, bitdepth);

//		MyLog.waitHere("length= " + vetTabella.length);
		stack.setVoxels(0, 0, 0, lato, lato, lato, vetTabella);

		ImagePlus impStack = new ImagePlus("tabella", stack);
		impStack.show();
	}

	/**
	 * Calcolo eseguito per ogni singolo voxel
	 * 
	 * @param voxSignal
	 * @param acqDuration
	 * @param fatCal
	 * @param deltaT
	 * @param par_a
	 * @return
	 */
	static double mAtildeSingleVoxel(double voxSignal, double acqDuration, double fatCal, double deltaT, double par_a) {
		double ahhVoxel = voxSignal / (acqDuration * fatCal);
		double aVoxel = ahhVoxel / Math.exp(-(par_a * deltaT));
		double aTildeVoxel = (aVoxel / par_a) * 3600;
		return aTildeVoxel;
	}

}