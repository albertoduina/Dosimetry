package Dosimetry;

import java.awt.Font;
import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
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

	public void run(String arg) {

		String str1 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator + "ImagesFolder" + File.separator;
		String str2 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator;
		String str3 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder";

		// iniziamo esaminando la 48h, some da spoecifiche

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
		for (int i1 = 0; i1 < 3; i1++) {
			lesione1 = str2 + out1 + vetH[i1] + "h.tif";
			lesione3 = str2 + out1 + "_PATATA" + vetH[i1] + "h.nii";
			lesione4 = str2 + out1 + "_MATILDE" + vetH[i1] + "h.nii";
			lesione2 = str2 + out1 + ".txt";
			startingDir1 = str1 + vetH[i1] + "h" + File.separator + "SPECT";

			caricaMemoriazza(startingDir1, lesione1, vetH[i1], lesione2, lesione3, lesione4);

			File fil = new File(lesione1);
			Utility.deleteFile(fil);
			Utility.chiudiTutto();
		}

		// C:\Users\Alberto\Desktop\DosimetryFolder\ImagesFolder\48h\SPECT
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
	void caricaMemoriazza(String pathStackIn, String pathStackMask, int ore, String pathLesione, String pathOut, String pathOut2) {

		ImagePlus impStackIn = null;
		ImagePlus impStackMask = null;
		int width2 = 0;
		int height2 = 0;
		int depth2 = 0;
		double acqDuration = 0;
		double fatCal = 0;
		double deltaT = 0;

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
		new ImageConverter(impStackIn).convertToGray32();

		impStackIn.setTitle("INPUT");

		impStackMask = Utility.openImage(pathStackMask);
		int sl = Utility.MyStackCountPixels(impStackMask);
		impStackMask.setDisplayRange(50, 255);
		impStackMask.setSlice(sl);

		impStackMask.setTitle("MASK");
		impStackMask.show();

		// in pratica ora imposto il mio cuBBetto in modo che "viaggi" per tutto il
		// nostro stack, il pixel centrale del cubo, sara' la media di tutti i pixel del
		// cuBBo e verra' scritto nella corrispondente posizione delo stack di output

		ImageStack stackMask = impStackMask.getImageStack();
		ImageStack stackIn = impStackIn.getImageStack();

		double[] tapata1 = Utility.MyStackStatistics(impStackIn, impStackMask);

		impStackIn.setDisplayRange(tapata1[3], tapata1[7]);
		impStackIn.setSlice((int) tapata1[6]);

		impStackIn.show();

		int width1 = stackIn.getWidth();
		int height1 = stackIn.getHeight();
		int depth1 = stackIn.getSize();

		ImageStack stackOut1 = new ImageStack(width1, height1);
		ImageStack stackOut2 = new ImageStack(width1, height1);
		stackOut1.setBitDepth(32);
		stackOut2.setBitDepth(32);
		ImagePlus impBlack = NewImage.createShortImage("NERA", width1, height1, 1, NewImage.FILL_BLACK);
		new ImageConverter(impBlack).convertToGray32();
		ImageProcessor ipBlack = impBlack.getProcessor();

		// elaborazione pixel per pixel dell'intera immagine di input, senza quindi
		// utilizzare la mask, dopo che abbiamo applicato le formule formulate in
		// formule11012023 scriviamo il risultato nel corrispondente pixel float dello
		// stackout
		// ####################################################
		// MATILDE
		// ####################################################

		double voxSignal = 0;
		double ahhVoxel = 0;
		double aVoxel = 0;
		double aTildeVoxel = 0;
		ImageProcessor inSlice1 = null;
		ImageProcessor outSlice1 = null;

		for (int z1 = 0; z1 < depth1; z1++) {
			inSlice1 = stackIn.getProcessor(z1 + 1);
			outSlice1 = ipBlack.duplicate();
			for (int x1 = 0; x1 < width1; x1++) {
				for (int y1 = 0; y1 < height1; y1++) {
					IJ.showStatus("BBB " + z1 + " / " + (depth1));
//					if (z1 == 0 && x1 == 0 && y1 == 0)
//						MyLog.waitHere();

					voxSignal = inSlice1.getPixelValue(x1, y1);
					ahhVoxel = voxSignal / (acqDuration * fatCal);
					aVoxel = ahhVoxel / Math.exp(par_a * deltaT);
					aTildeVoxel = (aVoxel / par_a) * 3600;
					if (aTildeVoxel > 0.1)
						outSlice1.putPixelValue(x1, y1, aTildeVoxel);
				}
			}
			stackOut1.addSlice(outSlice1);
		}

		ImagePlus impMatilde = new ImagePlus("mAtilde", stackOut1);
		double[] tapata2 = Utility.MyStackStatistics(impMatilde, impStackMask);

		impMatilde.setDisplayRange(tapata2[3], tapata2[7]);
		impMatilde.setSlice((int) tapata2[6]);

		impMatilde.show();
		
		
		IJ.run(impMatilde, "NIfTI-1", "save=" + pathOut2);


		// ####################################################
		// PATATA
		// ####################################################

		width2 = 6;
		height2 = 6;
		depth2 = 6;
		double voxMask = 0;
		float[] vetVox = null;
		float[] vetTabella = null;
		float doseVoxel = 0;
		ImageProcessor outSlice2 = null;

		// nel gran finale facciamo una elaborazione del CUBBO

		for (int z1 = 0; z1 < depth1 - depth2; z1++) {
			outSlice2 = ipBlack.duplicate();
			for (int x1 = 0; x1 < width1 - width2; x1++) {
				for (int y1 = 0; y1 < height1 - height2; y1++) {
					doseVoxel = 0;
					IJ.showStatus("CCC " + z1 + " / " + (depth1 - depth2));
//					if (z1 == 0 && x1 == 0 && y1 == 0)
//						MyLog.waitHere("");

					voxMask = stackMask.getVoxel(x1, y1, z1);
					vetVox = stackOut1.getVoxels(x1, y1, z1, width2, height2, depth2, null);
					vetTabella = extractTabella(Utility.tabellaBella());
					doseVoxel = patataACubetti(vetVox, vetTabella);
					if (voxMask > 0) {
						outSlice2.putPixelValue(x1, y1, doseVoxel);
					}
				}
			}
			stackOut2.addSlice(outSlice2);
		}

		for (int z1 = 0; z1 < depth2; z1++) {
			outSlice2 = ipBlack.duplicate();
			stackOut2.addSlice(outSlice2);
		}

		ImagePlus impPatata = new ImagePlus("PATATA", stackOut2);
		double[] tapata3 = Utility.MyStackStatistics(impPatata);

		impPatata.setDisplayRange(tapata3[3], tapata3[7]);
		impPatata.setSlice((int) tapata3[6]);
		impPatata.show();
		IJ.saveAsTiff(impPatata, pathOut);
		IJ.run(impPatata, "NIfTI-1", "save=" + pathOut);

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
		resultsDialog.addMessage("Results", titleFont);
		resultsDialog.setFont(defaultFont);

		resultsDialog.addMessage("============ IMMAGINE MASCHERATA ===============");
		resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal3) + "    x= " + minStackX3
				+ "    y= " + minStackY3 + "    z= " + minStackZ3);
		resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal3) + "    x= " + maxStackX3
				+ "    y= " + maxStackY3 + "    z= " + maxStackZ3);

		resultsDialog
				.addMessage("meanStackVal= " + String.format("%.4f", meanStackVal3) + "        pixCount= " + pixCount3);
		resultsDialog.addMessage("integral= " + String.format("%.4f", integral3));
		resultsDialog.addMessage("============ MATILDE MASCHERATA ===============");
		resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal1) + "    x= " + minStackX1
				+ "    y= " + minStackY1 + "    z= " + minStackZ1);
		resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal1) + "    x= " + maxStackX1
				+ "    y= " + maxStackY1 + "    z= " + maxStackZ1);

		resultsDialog
				.addMessage("meanStackVal= " + String.format("%.4f", meanStackVal1) + "        pixCount= " + pixCount1);
		resultsDialog.addMessage("integral= " + String.format("%.4f", integral1));

		resultsDialog.addMessage("================== PATATA =====================");
		resultsDialog.addMessage("minStackVal= " + String.format("%.4f", minStackVal2) + "    x= " + minStackX2
				+ "    y= " + minStackY2 + "    z= " + minStackZ2);
		resultsDialog.addMessage("maxStackVal= " + String.format("%.4f", maxStackVal2) + "    x= " + maxStackX2
				+ "    y= " + maxStackY2 + "    z= " + maxStackZ2);

		resultsDialog
				.addMessage("meanStackVal= " + String.format("%.4f", meanStackVal2) + "        pixCount= " + pixCount2);
		resultsDialog.addMessage("integral= " + String.format("%.4f", integral2));
		resultsDialog.showDialog();

		Utility.calculateDVH(impPatata);
		}

	/**
	 * Estrazione dell'array dalla tabella
	 * 
	 * @param tabellaBella
	 * @return
	 */
	float[] extractTabella(double[][] tabellaBella) {

		float[] vetTabella = new float[tabellaBella.length];
		for (int i1 = 0; i1 < tabellaBella.length; i1++) {
			vetTabella[i1] = (float) tabellaBella[i1][3];
		}

		return vetTabella;
	}

	/**
	 * Elabora i pixel "mascherati" dell'immagine calcolata utilizzando un cubo e
	 * moltiplicando i col corrispondente cubo della tabella
	 * 
	 * @param vetVox
	 * @param vetTabella
	 * @return
	 */
	float patataACubetti(float[] vetVox, float[] vetTabella) {

		float voxOut = 0;
		float aux1 = 0;

		for (int i1 = 0; i1 < vetVox.length; i1++) {

			aux1 = aux1 + (vetVox[i1] * vetTabella[i1]);
			voxOut = aux1 / 1000;
		}
		return voxOut;
	}

}