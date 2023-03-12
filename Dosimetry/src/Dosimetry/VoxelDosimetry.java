package Dosimetry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NonBlockingGenericDialog;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * @version v3
 * @author Date 30 jan 2023
 */
public class VoxelDosimetry {

	/**
	 * Calcolo iniziale immagini per DVH
	 * 
	 * @param dosimetryFolder
	 * @param puntiSelezionati
	 * @param par_a
	 * @return
	 */
	static ArrayList<ArrayList<Double>> start_DVH1(String dosimetryFolder, boolean[] puntiSelezionati, double par_a) {

		String lesione1 = "";
		String logVolatile = dosimetryFolder + File.separator + "volatile.txt";
		String out1 = "volatile";
		String startingDir1 = "";

		MyLog.here();

		ArrayList<ArrayList<Double>> xList = new ArrayList<ArrayList<Double>>();
		int[] vetH = { 24, 48, 120 };
		for (int i1 = 0; i1 < puntiSelezionati.length; i1++) {
			if (puntiSelezionati[i1]) {
				ArrayList<ArrayList<Double>> yList = new ArrayList<ArrayList<Double>>();
				lesione1 = dosimetryFolder + out1 + vetH[i1] + "h.tif";
				MyLog.log("lesione1= " + lesione1);
				startingDir1 = dosimetryFolder + File.separator + "ImagesFolder" + File.separator + vetH[i1] + "h"
						+ File.separator + "SPECT";
				MyLog.log("startingDir1= " + startingDir1);
				yList = creazioneImmagini_DVH2(startingDir1, lesione1, vetH[i1], par_a, logVolatile);
				if (yList == null) {
					xList.add(null);
					xList.add(null);
					xList.add(null);
				}
				for (int i2 = 0; i2 < yList.size(); i2++) {
					xList.add(yList.get(i2));
				}
			} else {
				xList.add(null);
				xList.add(null);
				xList.add(null);
			}
		}

		return xList;

	}

	/**
	 * Calcolo del vettore errore
	 * 
	 * @param vetIn
	 * @return
	 */
	static double vetErr(double[] vetIn) {

		double sum = 0;
		for (int i1 = 0; i1 < vetIn.length; i1++) {
			sum = sum + vetIn[i1];
		}
		double out1 = sum / Math.sqrt(vetIn.length);

		return out1;
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
	static ArrayList<ArrayList<Double>> creazioneImmagini_DVH2(String pathStackIn, String pathStackMask, int ore,
			double par_a, String pathLesione) {

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

		MyLog.here();

		long start1 = System.currentTimeMillis();
		int lato = MyGlobals.latoCubo();
		int mezzo = MyGlobals.mezzoLato();
		if (Double.isNaN(par_a))
			MyLog.waitHere("par_a= NaN");

		switch (ore) {
		case 24:
			acqDuration = Double.parseDouble(MyLog.readFromLog(pathLesione, "#018#", "=", true)); // acqduration 24h
																									// // // 24h
			deltaT = Double.parseDouble(MyLog.readFromLog(pathLesione, "#019#", "=", true)); // deltaT 24h
			fatCal = Double.parseDouble(MyLog.readFromLog(pathLesione, "#202#", "=", true)); // fatCal24h
			break;
		case 48:
			acqDuration = Double.parseDouble(MyLog.readFromLog(pathLesione, "#038#", "=", true)); // acqduration 48 //
																									// // 48h
			deltaT = Double.parseDouble(MyLog.readFromLog(pathLesione, "#039#", "=", true)); // deltaT 24h
			fatCal = Double.parseDouble(MyLog.readFromLog(pathLesione, "#222#", "=", true)); // fatCal48h
			break;
		case 120:
			acqDuration = Double.parseDouble(MyLog.readFromLog(pathLesione, "#058#", "=", true)); // acqduration 120h
			deltaT = Double.parseDouble(MyLog.readFromLog(pathLesione, "#059#", "=", true)); // deltaT 24h
			fatCal = Double.parseDouble(MyLog.readFromLog(pathLesione, "#242#", "=", true)); // fatCal120h
			break;
		}
//		double par_a = Double.parseDouble(MyLog.readFromLog(pathLesione, "#302#", "=", true));

		impStackIn = MyStack.readStackFiles2(pathStackIn);
		// convertendo a 32 bit viene eliminata la calibrazione e la noia di avere il
		// valore 0 rappresentato con 32768
		new ImageConverter(impStackIn).convertToGray32();

		impStackIn.setTitle("INPUT " + ore + "h");
		impStackMask = Utility.openImage(pathStackMask);
		int sl = MyStack.MyStackCountPixels(impStackMask);
		impStackMask.setDisplayRange(50, 255);
		impStackMask.setSlice(sl);
		impStackMask.setTitle("MASK " + ore + "h");

		if (MyGlobals.loggoVoxels) {
			// serve solo per DEBUG durante le prove
			x2 = MyGlobals.coordinateVoxels[0];
			y2 = MyGlobals.coordinateVoxels[1];
			z2 = MyGlobals.coordinateVoxels[2];

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

		double[] tapata1 = MyStack.MyStackStatistics(impStackIn, impStackMask);
		impStackIn.setDisplayRange(tapata1[3], tapata1[7]);
		impStackIn.setSlice((int) tapata1[6]);

//		impStackIn.show();

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
		// int conta1 = 0;

		for (int z1 = 1; z1 <= depth1; z1++) {
			inSlice1 = stackIn.getProcessor(z1);
			outSlice1 = stackMatilde.getProcessor(z1);
			for (int x1 = 0; x1 < width1; x1++) {
				for (int y1 = 0; y1 < height1; y1++) {
//					conta1++;
					IJ.showStatus("aTilde " + z1 + " / " + (depth1));
					voxSignal = inSlice1.getPixelValue(x1, y1);
					aTildeVoxel = mAtildeSingleVoxel(voxSignal, acqDuration, fatCal, deltaT, par_a);
					outSlice1.putPixelValue(x1, y1, aTildeVoxel);
				}
			}

			MyStack.stackSliceUpdater(stackMatilde, outSlice1, z1);
		}

		ImagePlus impMatilde = new ImagePlus("mAtilde " + ore + "h", stackMatilde);
//		impMatilde.show();

		if (MyStack.stackIsEmpty(impMatilde))
			MyLog.waitHere("impMatilde vuota");

		if (MyGlobals.loggoVoxels) {

			Utility.loggoVoxels2(impStackIn, x2, y2, z2);
			Utility.loggoCuxels3(impStackIn, x2, y2, z2, lato, mezzo);

			Utility.loggoVoxels2(impMatilde, x2, y2, z2);
			Utility.loggoCuxels3(impMatilde, x2, y2, z2, lato, mezzo);
		}

		double[] tapata2 = MyStack.MyStackStatistics(impMatilde, impStackMask);

		// -------------------------------------
		// ELABORAZIONI DEI CUBI
		// -------------------------------------
		// -------------------------------------
		// CUBO CON SVALUES
		// -------------------------------------
		ImagePlus impRubik = Utility.inCubo();
		if (MyStack.stackIsEmpty(impRubik))
			MyLog.waitHere("impRubik vuota");

//		impRubik.show();

		ImageStack stackRubik = impRubik.getImageStack();
		// -------------------------------------

		boolean log2;
		// ####################################################
		// PATATA COMPLETA CON SVALUES
		// ####################################################
//		int conta2 = 0;

		if (MyStack.stackIsEmpty(stackMatilde))
			MyLog.waitHere("stackMatilde vuoto");
		if (MyStack.stackIsEmpty(stackRubik))
			MyLog.waitHere("stackRubik vuoto");

		ImageStack stackPatataCompleta = ImageStack.create(width1, height1, depth1, bitdepth1);
		for (int z1 = 1 + mezzo; z1 < (depth1 - mezzo) - 2; z1++) {
			for (int y1 = mezzo; y1 < (height1 - mezzo); y1++) {
				IJ.showStatus("patataCompleta " + z1 + " / " + depth1);
				for (int x1 = mezzo; x1 < (width1 - mezzo); x1++) {
					if (x1 == MyGlobals.coordX && y1 == MyGlobals.coordY && z1 == MyGlobals.coordZ)
						log2 = true;
					else
						log2 = false;
					// -------------------------------------
					float[] vetVoxels = stackMatilde.getVoxels(x1 - mezzo, y1 - mezzo, z1 - mezzo, lato, lato, lato,
							null);
					float[] vetSvalues = stackRubik.getVoxels(0, 0, 0, lato, lato, lato, null);
					// -------------------------------------
					double valPatataCompleta = 0;
					double pixPatata = 0;

					for (int i1 = 0; i1 < vetVoxels.length; i1++) {
						// conta2++;
						pixPatata = (vetVoxels[i1] * vetSvalues[i1]) / 1000.;
						valPatataCompleta = valPatataCompleta + pixPatata;
					}
					stackPatataCompleta.setVoxel(x1, y1, z1, valPatataCompleta);
				}
			}
		}

		ImagePlus impPatataCompleta = new ImagePlus("PatataCompleta " + ore + "h", stackPatataCompleta);
		// impPatataCompleta.show();

		if (MyGlobals.loggoVoxels) {
			Utility.loggoVoxels2(impPatataCompleta, x2, y2, z2);
			Utility.loggoCuxels4(impRubik, mezzo, mezzo, mezzo, lato, mezzo);
			Utility.loggoCuxels3(impPatataCompleta, x2, y2, z2, lato, mezzo);
		}

		double[] tapata3 = MyStack.MyStackStatistics(impPatataCompleta, impStackMask);
		impPatataCompleta.setDisplayRange(tapata3[3], tapata3[7]);
		impPatataCompleta.setSlice((int) tapata3[6]);
		// impPatataCompleta.show();

		if (MyStack.stackIsEmpty(impPatataCompleta))
			MyLog.waitHere("impPatataCompleta vuota");

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
		if (MyGlobals.loggoVoxels) {
			Utility.loggoVoxels2(impStackMask, x2, y2, z2);
			Utility.loggoCuxels3(impStackMask, x2, y2, z2, lato, mezzo);

			Utility.loggoVoxels2(impPatataMascherata, x2, y2, z2);
			Utility.loggoCuxels3(impPatataMascherata, x2, y2, z2, lato, mezzo);
		}
//		impPatataMascherata.show();

		if (MyStack.stackIsEmpty(impPatataMascherata))
			MyLog.waitHere("impPatataMascherata vuota");

		tapata3 = MyStack.MyStackStatistics(impPatataMascherata, impStackMask);
		impPatataMascherata.setDisplayRange(tapata3[3], tapata3[7]);
		impPatataMascherata.setSlice((int) tapata3[6]);
		impPatataMascherata.show();

		long end1 = System.currentTimeMillis();

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
//
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
//
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

		if (MyGlobals.mostraResultsSV05) {
			NonBlockingGenericDialog resultsDialog = new NonBlockingGenericDialog("SV05 - Results");
			resultsDialog.addMessage("Results " + ore + "h", MyGlobals.titleFont);
			resultsDialog.setFont(MyGlobals.defaultFont);

			resultsDialog.addMessage("======== IMMAGINE INPUT  MASCHERATA ======");
			resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal3) + "     x= " + minStackX3
					+ "    y= " + minStackY3 + "    z= " + minStackZ3);
			resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal3) + "    x= " + maxStackX3
					+ "    y= " + maxStackY3 + "    z= " + maxStackZ3);

			resultsDialog.addMessage(
					"meanStackVal= " + String.format("%.4f", meanStackVal3) + "        pixCount= " + pixCount3);
			resultsDialog.addMessage("integral= " + String.format("%.4f", integral3));
			resultsDialog.addMessage("======== MATILDE MASCHERATA =============");
			resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal1) + "       x= " + minStackX1
					+ "    y= " + minStackY1 + "    z= " + minStackZ1);
			resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal1) + "    x= " + maxStackX1
					+ "    y= " + maxStackY1 + "    z= " + maxStackZ1);

			resultsDialog.addMessage(
					"meanStackVal= " + String.format("%.4f", meanStackVal1) + "        pixCount= " + pixCount1);
			resultsDialog.addMessage("integral= " + String.format("%.4f", integral1));

			resultsDialog.addMessage("======== PATATA MASCHERATA ==============");
			resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal2) + "     x= " + minStackX2
					+ "    y= " + minStackY2 + "    z= " + minStackZ2);
			resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal2) + "    x= " + maxStackX2
					+ "    y= " + maxStackY2 + "    z= " + maxStackZ2);

			resultsDialog.addMessage(
					"meanStackVal= " + String.format("%.4f", meanStackVal2) + "        pixCount= " + pixCount2);
			resultsDialog.addMessage("integral= " + String.format("%.4f", integral2));
			resultsDialog.addMessage("\n\n\nTempo impiegato= " + time1);
			resultsDialog.showDialog();

			if (resultsDialog.wasCanceled())
				return null;
		}

		ArrayList<ArrayList<Double>> out1 = VoxelDosimetry.sub_DVH3(impPatataMascherata, ore);

		return out1;
	}

	void loggoTabellaBella(float[] vetTabella, int lato) {

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

	void creoStackTabellaBella(float[] vetTabella, int lato) {

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
	 * Gestione calocoli DVH a seconda dei punti selezionati
	 * 
	 * @param vetx24
	 * @param vety24
	 * @param vetx48
	 * @param vety48
	 * @param vetx120
	 * @param vety120
	 * @return
	 */
	static double[][] subDVH2(double[] vetx24, double[] vety24, double[] vetx48, double[] vety48, double[] vetx120,
			double[] vety120) {

		MyLog.log("eseguoCalcDVH2");

		double[] vetx48new = null;
		double[] vety48new = null;
		double[] vetx120new = null;
		double[] vety120new = null;

		double[] vetxLow = null;
		double[] vetyLow = null;
		double[] vetxHigh = null;
		double[] vetyHigh = null;

		int situa = 0;
		if (vetx24 != null && vetx48 != null && vetx120 != null)
			situa = 0;
		if (vetx24 != null && vetx48 != null && vetx120 == null)
			situa = 1;
		if (vetx24 != null && vetx48 == null && vetx120 != null)
			situa = 2;
		if (vetx24 == null && vetx48 != null && vetx120 != null)
			situa = 3;

		double[][] matout1 = null;
		double[][] matout2 = null;
		double[][] matout3 = null;
		double[][] matout4 = null;
		double[] vetMin = null;
		double[] vetMax = null;
		double[] vetMedia = null;
		double[] vetY = null;

		switch (situa) {
		case 1:
			vetxLow = vetx24;
			vetyLow = vety24;
			vetxHigh = vetx48;
			vetyHigh = vety48;
			matout1 = Utility.interpolator(vetxLow, vetyLow, vetxHigh, vetyHigh);
			vetx48new = Utility.matToVect(matout1, 0);
			vety48new = Utility.matToVect(matout1, 1);
			matout3 = Utility.rasegotto2(vetx24, vetx48new, vety24);
			vetMin = Utility.matToVect(matout3, 0);
			vetMax = Utility.matToVect(matout3, 1);
			vetY = Utility.matToVect(matout3, 2);
			matout4 = Utility.mediolotto2(vetMin, vetMax, vetY);
			vetMedia = Utility.matToVect(matout4, 0);
			break;
		case 2:
			vetxLow = vetx24;
			vetyLow = vety24;
			vetxHigh = vetx120;
			vetyHigh = vety120;
			matout1 = Utility.interpolator(vetxLow, vetyLow, vetxHigh, vetyHigh);
			vetx120new = Utility.matToVect(matout1, 0);
			vety120new = Utility.matToVect(matout1, 1);
			matout3 = Utility.rasegotto2(vetx24, vetx120new, vety24);
			vetMin = Utility.matToVect(matout3, 0);
			vetMax = Utility.matToVect(matout3, 1);
			vetY = Utility.matToVect(matout3, 2);
			matout4 = Utility.mediolotto2(vetMin, vetMax, vetY);
			vetMedia = Utility.matToVect(matout4, 0);
			break;
		case 3:
			vetxLow = vetx48;
			vetyLow = vety48;
			vetxHigh = vetx120;
			vetyHigh = vety120;
			matout1 = Utility.interpolator(vetxLow, vetyLow, vetxHigh, vetyHigh);
			vetx120new = Utility.matToVect(matout1, 0);
			vety120new = Utility.matToVect(matout1, 1);
			matout3 = Utility.rasegotto2(vetx48, vetx120new, vety48);
			vetMin = Utility.matToVect(matout3, 0);
			vetMax = Utility.matToVect(matout3, 1);
			vetY = Utility.matToVect(matout3, 2);
			matout4 = Utility.mediolotto2(vetMin, vetMax, vetY);
			break;
		default:
			vetxLow = vetx24;
			vetyLow = vety24;
			vetxHigh = vetx48;
			vetyHigh = vety48;
			matout1 = Utility.interpolator(vetxLow, vetyLow, vetxHigh, vetyHigh);
			vetx48new = Utility.matToVect(matout1, 0);
			vety48new = Utility.matToVect(matout1, 1);
			vetxLow = vetx24;
			vetyLow = vety24;
			vetxHigh = vetx120;
			vetyHigh = vety120;
			matout2 = Utility.interpolator(vetxLow, vetyLow, vetxHigh, vetyHigh);
			vetx120new = Utility.matToVect(matout2, 0);
			vety120new = Utility.matToVect(matout2, 1);
			matout3 = Utility.rasegotto(vetx24, vetx48new, vetx120new, vety24);
			vetMin = Utility.matToVect(matout3, 0);
			vetMax = Utility.matToVect(matout3, 1);
			vetY = Utility.matToVect(matout3, 2);
			matout4 = Utility.mediolotto2(vetMin, vetMax, vetY);
			vetMedia = Utility.matToVect(matout4, 0);
		}

		double[][] matout5 = new double[vetY.length][4];
		for (int i1 = 0; i1 < vetY.length; i1++) {
			matout5[i1][0] = matout3[i1][0];
			matout5[i1][1] = matout3[i1][1];
			matout5[i1][2] = matout4[i1][0];
			matout5[i1][3] = matout4[i1][1];
		}
		return matout5;
	}

	/**
	 * Calcolo DVH errore dose superiore per grafico
	 * 
	 * @param vetMed
	 * @param vetMax
	 * @return
	 */
	static double[] calcoliDVHerrSup(double[] vetMed, double[] vetMax) {
		double[] vetErrSup = new double[vetMed.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetMed.length; i1++) {
			errDose = vetMax[i1] - vetMed[i1];
			vetErrSup[i1] = errDose;
		}
		return vetErrSup;
	}

	/**
	 * Calcolo DVH errore dose inferiore per grafico
	 * 
	 * @param vetMed
	 * @param vetMin
	 * @return
	 */
	static double[] calcoliDVHerrInf(double[] vetMed, double[] vetMin) {
		double[] vetErrInf = new double[vetMed.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetMed.length; i1++) {
			errDose = vetMed[i1] - vetMin[i1];
			vetErrInf[i1] = errDose;
		}
		return vetErrInf;
	}

	/**
	 * Calcolo DVH errore
	 * 
	 * @param vetMedia
	 * @param errMedia
	 * @return
	 */
	static double calcoliDVHerrFinale(double[] vetMedia, double[] errMedia) {
		double[] vetErrDose = new double[vetMedia.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetMedia.length; i1++) {
			errDose = (vetMedia[i1] / vetMedia.length) * errMedia[i1];
			vetErrDose[i1] = errDose;
		}
		double sumErrDose = 0;
		for (int i1 = 0; i1 < vetErrDose.length; i1++) {
			sumErrDose = sumErrDose + vetErrDose[i1] * vetErrDose[i1];
		}

		double errOut = Math.sqrt(sumErrDose);

		return errOut;
	}

	/**
	 * Calolo DVH errore
	 * 
	 * @param vetMin
	 * @param vetMax
	 * @return
	 */
	static double[] calcoliDVHerrDose2(double[] vetMin, double[] vetMax) {
		double[] vetErrDose = new double[vetMax.length];
		double errDose = 0;
		for (int i1 = 0; i1 < vetMax.length; i1++) {
			errDose = (vetMax[i1] - vetMin[i1]) / 2.;
			vetErrDose[i1] = errDose;
		}
		return vetErrDose;
	}

	/**
	 * Calcolo DVH ricerca approssimata di una percentuale, facendo il minimo della
	 * differenza
	 * 
	 * @param vetErrDose
	 * @param vetMedia
	 * @param vetY
	 * @param percent
	 * @return
	 */
	static double[] calcoliDVH(double[] vetErrDose, double[] vetMedia, double[] vetY, int percent) {

		double valPercent = 0;
		double errPercent = 0;
		// double aux1 = 0;
		// calcolo la differenza tra Y e la percentuale cercata
		double[] vetDelta1 = new double[vetY.length];
		for (int i1 = 0; i1 < vetY.length; i1++) {
			vetDelta1[i1] = Math.abs(vetY[i1] - (double) percent);
			// IJ.log("" + i1 + " vetDelta1= " + vetDelta1[i1]);
		}
		// cerco la posizione del minimo sul vettore differenza
		double min = Double.MAX_VALUE;
		double value;
		int minpos = 0;
		for (int i1 = 0; i1 < vetY.length; i1++) {
			value = vetDelta1[i1];
			if (value < min) {
				min = value;
				minpos = i1;
			}
		}

		MyLog.log("minpos= " + minpos + " / " + vetY.length + " per percentuale= " + percent);

		// ora minpos contiene la posizione del minimo
		valPercent = vetMedia[minpos];
		errPercent = vetErrDose[minpos];
		double[] vetOut = new double[2];
		vetOut[0] = valPercent;
		vetOut[1] = errPercent;
		return vetOut;
	}

	/**
	 * Calcolo DVH rimozione doppioni e creazione array numerosita'
	 * 
	 * @param vetVoxel
	 * @param ore
	 * @return
	 */
	static ArrayList<ArrayList<Double>> sub_DVH4(double[] vetVoxel, int ore) {
		// ---------------------------------

		MyLog.log("eseguo calcDVH1 " + ore + " ore");

		Arrays.sort(vetVoxel);

		// --------------------------------
		// rimozione dei doppioni e creazione array
		int n1 = vetVoxel.length;
		double[] temp = new double[n1];
		int j1 = 0;
		for (int i1 = 0; i1 < n1 - 1; i1++) {
			if (Double.compare(vetVoxel[i1], vetVoxel[i1 + 1]) != 0) {
				temp[j1++] = vetVoxel[i1];
			}
		}
		temp[j1++] = vetVoxel[n1 - 1];

		double[] vetRemoved = new double[j1];
		for (int i1 = 0; i1 < j1; i1++) {
			vetRemoved[i1] = temp[i1];
		}
		// --------------------------------
		// conteggio numerosita'
		double[][] vetNum = new double[vetRemoved.length + 1][3];
		for (int i1 = 0; i1 < vetRemoved.length; i1++) {
			vetNum[i1 + 1][0] = vetRemoved[i1]; // parto da posizione [1[0] perche' in seguito vado a forzare 100 in
												// posizione [0][0]
		}
		double aux1 = 0;
		for (int i1 = 0; i1 < vetVoxel.length; i1++) {
			aux1 = vetVoxel[i1];
			for (int i2 = 1; i2 < vetNum.length; i2++) {
				int comp = Double.compare(aux1, vetNum[i2][0]);
				if (comp == 0) {
					vetNum[i2][1] = vetNum[i2][1] + 1.0; // in posizione [][1] effettuo il conteggio numerosita'
				}
			}
		}
		// --------------------------------
		// calcolo della % volume
		for (int i2 = vetNum.length - 1; i2 >= 1; i2--) {
			if (i2 == vetNum.length - 1) {
				vetNum[i2][2] = vetNum[i2][1] / vetVoxel.length * 100;
			} else {
				vetNum[i2][2] = vetNum[i2][1] / vetVoxel.length * 100 + vetNum[i2 + 1][2]; // in posizione [][2]
																							// mettiamo la % volume
			}
		}

		//
		// Questo e'il punto dove viene forzato il 100% in posizione [0][2]
		//
		vetNum[0][0] = 0;
		vetNum[0][1] = 1;
		vetNum[0][2] = 100;

//		MyLog.logMatrixVertical(vetNum, "vetNum" + MyLog.here1());

		// ------------------------------------------------------
		// A questo punto vorrei provare a restituire un Arraylist<ArrayList>, questo
		// potrebbe permettermi di aggiungere i dati ad un ArrayList<ArrayList>>
		// esterno, ma non ne sono troppo sicurobisogna testarlo molto ma molto bene
		// ------------------------------------------------------
		ArrayList<ArrayList<Double>> arrList1 = new ArrayList<ArrayList<Double>>();

//		MyLog.log("Ore= "+ore);

		for (int i2 = 0; i2 < vetNum[0].length; i2++) {
			ArrayList<Double> arrList2 = new ArrayList<Double>();
			for (int i1 = 0; i1 < vetNum.length; i1++) {
				arrList2.add(vetNum[i1][i2]);
//				MyLog.log(MyLog.here1() + "    " + vetNum[i1][i2]);
			}
			arrList1.add(arrList2);
		}

		return arrList1;

	}

	/**
	 * Calcolo del DVH Elenco del segnale nei voxel con maschera >0
	 * 
	 * @param patataMascherata in patata mascherata abbiamo segnale solo nei voxels
	 *                         selezionati nella mask, quindi usando solo i voxels >
	 *                         0 sono a posto
	 * @param ore
	 */
	public static ArrayList<ArrayList<Double>> sub_DVH3(ImagePlus patataMascherata, int ore) {

		// patataMascherata.show();

		if (patataMascherata == null)
			MyLog.waitHere("patataMascherata == null");
		ImageStack stack = patataMascherata.getImageStack();
		if (stack == null)
			MyLog.waitHere("stack == null");

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
					if (voxel > 0) {
						arrList.add(voxel);
					}
				}
			}
		}

//		MyLog.logArrayList(arrList, "arrList " + MyLog.here1());

		vetVoxel = Utility.arrayListToArrayDouble(arrList);
		ArrayList<ArrayList<Double>> pippo = VoxelDosimetry.sub_DVH4(vetVoxel, ore);

		return pippo;
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