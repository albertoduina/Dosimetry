package Dosimetry;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.ContrastEnhancer;
import ij.plugin.PlugIn;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.util.ArrayUtil;

/**
 * @version v3
 * @author Date 05 dec 2022
 */
public class S_VoxelDosimetry implements PlugIn {

	public void run(String arg) {

		String str1 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator + "ImagesFolder" + File.separator;
		String str2 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator;
		String str3 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder";

		String out1 = Utility.dialogSceltaLesione_SV02(str3);

		// iniziamo esaminando la 48h, some da spoecifiche
		String startingDir1 = str1 + "48h" + File.separator + "SPECT";

		String lesione1 = str2 + out1 + "48h.tif";
		String lesione2 = str2 + out1 + ".txt";

		caricaMemoriazza(startingDir1, lesione1, 48, lesione2);

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
	void caricaMemoriazza(String pathStackIn, String pathStackMask, int ore, String pathLesione) {

		MyLog.waitHere(
				"pathStackin= " + pathStackIn + "\npathStackMask= " + pathStackMask + "\npathLesione= " + pathLesione);

		ImagePlus impStackIn = null;
		ImagePlus impStackInCalibrated = null;
		ImagePlus impStackMask = null;
		int width2 = 0;
		int height2 = 0;
		int depth2 = 0;
		double acqDuration = 0;
		double fatCal = 0;
		double deltaT = 0;
		String aux1 = "";

		switch (ore) {
		case 24:
			acqDuration = Double.parseDouble(Utility.readFromLog(pathLesione, "#018#", "=")); // acqduration 24h // 24h
			deltaT = Double.parseDouble(Utility.readFromLog(pathLesione, "#019#", "=")); // deltaT 24h
			fatCal = Double.parseDouble(Utility.readFromLog(pathLesione, "#202#", "=")); // fatCal24h
			break;
		case 48:
			aux1 = Utility.readFromLog(pathLesione, "#038#", "="); // acqduration 48h // 48h
			acqDuration = Double.parseDouble(Utility.readFromLog(pathLesione, "#038#", "=")); // acqduration 48h // 48h
			deltaT = Double.parseDouble(Utility.readFromLog(pathLesione, "#039#", "=")); // deltaT 24h
			fatCal = Double.parseDouble(Utility.readFromLog(pathLesione, "#222#", "=")); // fatCal48h
			break;
		case 120:
			acqDuration = Double.parseDouble(Utility.readFromLog(pathLesione, "#058#", "=")); // acqduration 120h //
																								// 120h
			deltaT = Double.parseDouble(Utility.readFromLog(pathLesione, "#059#", "=")); // deltaT 24h
			fatCal = Double.parseDouble(Utility.readFromLog(pathLesione, "#242#", "=")); // fatCal120h
			break;
		}
		double par_a = Double.parseDouble(Utility.readFromLog(pathLesione, "#302#", "="));

		impStackIn = Utility.readStackFiles(pathStackIn);
		new ImageConverter(impStackIn).convertToGray32();

//		Calibration cal = impStackInCalibrated.getCalibration();
//		if (cal.isSigned16Bit()) {
//			MyLog.waitHere("calibrated");
//			impStackIn = Utility.removeCalibration(impStackInCalibrated);
//			
//			
//			cal.setSigned16BitCalibration();
//			cal.disableDensityCalibration();
//		}
//		impStackIn.updateAndDraw();

//		  cal = imp.getCalibration();
//		  if (!cal.isSigned16Bit())
//		     IJ.error("Signed 16-bit image required");
//		  cal.disableDensityCalibration();

		impStackIn.setTitle("INPUT");
		impStackIn.show();
		MyLog.waitHere("INPUT");

		impStackMask = Utility.openImage(pathStackMask);
		impStackMask.setTitle("MASK");
		impStackMask.show();
		MyLog.waitHere("MASK");

		// in pratica ora imposto il mio cuBBetto in modo che "viaggi" per tutto il
		// nostro stack, il pixel centrale del cubo, sara' la media di tutti i pixel del
		// cuBBo e verra' scritto nella corrispondente posizione delo stack di output

		ImageStack stackMask = impStackMask.getImageStack();
		ImageStack stackIn = impStackIn.getImageStack();
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

//		for (int i1 = 0; i1 < depth1; i1++) {
//			stackOut1.addSlice(ipBlack);
//			stackOut2.addSlice(ipBlack);
//		}

//		ImageStack stackOut1NoCal = stackIn.duplicate();
//		ImageStack stackOut2NoCal = stackIn.duplicate();

//		//
//		// forse non viene azzerata la prima fetta, potrebbe essere un bug di ImageJ, se
//		// si ripete riverificare e mandare mail Wayne Rasband oppure al gruppo
//		//
//		for (int z1 = 0; z1 < depth1; z1++) {
//			for (int x1 = 0; x1 < width1; x1++) {
//				for (int y1 = 0; y1 < height1; y1++) {
//					IJ.showStatus("  " + z1 + " / " + (depth1));
//					stackOut1NoCal.setVoxel(x1, y1, z1, 0);
//					stackOut2NoCal.setVoxel(x1, y1, z1, 0);
//				}
//			}
//		}
//		stackOut1NoCal.convertToFloat();
//		stackOut2NoCal.convertToFloat();

		// elaborazione pixel per pixel dell'intera immagine di input, senza quindi
		// utilizzare la mask, dopo che abbiamo applicato le formule formulate in
		// formule11012023 scriviamo il risultato nel corrispondente pixel float dello
		// stackout

		double voxSignal = 0;
		double ahhVoxel = 0;
		double aVoxel = 0;
		double aTildeVoxel = 0;
		ImageProcessor inSlice1 = null;
		ImageProcessor outSlice1 = null;
		int count2 = 0;

		for (int z1 = 0; z1 < depth1; z1++) {
			inSlice1 = stackIn.getProcessor(z1 + 1);
			outSlice1 = ipBlack.duplicate();
			for (int x1 = 0; x1 < width1; x1++) {
				for (int y1 = 0; y1 < height1; y1++) {
					IJ.showStatus("  " + z1 + " / " + (depth1));
					// voxSignal = stackIn.getVoxel(x1, y1, z1); // leggo il valore del pixel
					// voxSignal = (short) cal.getRawValue(voxSignal);
					voxSignal = inSlice1.getPixelValue(x1, y1);
					ahhVoxel = voxSignal / (acqDuration * fatCal);
					aVoxel = ahhVoxel / Math.exp(par_a * deltaT);
					aTildeVoxel = (aVoxel / par_a) * 3600;
					if (aTildeVoxel > 0.1)
						count2++;
					// stackOut1NoCal.setVoxel(x1, y1, z1, aTildeVoxel);
					outSlice1.putPixelValue(x1, y1, aTildeVoxel);
				}
			}
			stackOut1.addSlice(outSlice1);
		}

		ImagePlus impMatilde = new ImagePlus("mAtilde", stackOut1);
		impMatilde.show();
		Utility.autoAdjust(impMatilde, impMatilde.getProcessor());

		new WaitForUserDialog("MATILDE con " + count2 + " pixel >0").show();

		width2 = 6;
		height2 = 6;
		depth2 = 6;
		double voxMask = 0;
		double voxConteggi = 0;
		float[] vetVox = null;
		float[] vetTabella = null;
		float doseVoxel = 0;
		long count = 0;
		ImageProcessor inSlice2 = null;
		ImageProcessor outSlice2 = null;

		// nel gran finale facciamo una elaborazione del CUBBO

		for (int z1 = 0; z1 < depth1 - depth2; z1++) {
			inSlice2 = stackIn.getProcessor(z1 + 1);
			outSlice2 = ipBlack.duplicate();
			for (int x1 = 0; x1 < width1 - width2; x1++) {
				for (int y1 = 0; y1 < height1 - height2; y1++) {
					doseVoxel = 0;
					IJ.showStatus("  " + z1 + " / " + (depth1 - depth2));
					voxMask = stackMask.getVoxel(x1, y1, z1);
					if (voxMask > 0) {
						vetVox = stackOut1.getVoxels(x1, y1, z1, width2, height2, depth2, null);
						vetTabella = extractTabella(Utility.tabellaBella());
						doseVoxel = patataACubetti(vetVox, vetTabella);
						// IJ.log("doseVoxel= " + x1 + " " + y1 + " " + z1 + " " + doseVoxel);
						count++;
					}
					outSlice2.putPixelValue(x1, y1, doseVoxel);
					// stackOut2.setVoxel(x1 + width2 / 2, y1 + height2 / 2, z1 + depth2 / 2,
					// doseVoxel);
				}
			}
			stackOut2.addSlice(outSlice2);
		}

		ImagePlus impPatata = new ImagePlus("pAtatata", stackOut2);
		impPatata.show();

		Utility.autoAdjust(impPatata, impPatata.getProcessor());

		new WaitForUserDialog("PATATA con " + count + " pixel che dovrebbero essere con mask>0").show();

	}

	/**
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

	/***
	 * pixVectorize1 lavora sulle immagini costituite da float. Utilizza la Mask per
	 * identificare i pixel appartenenti ad una ROI circolare. Aggiunge i pixel
	 * appartenenti alla ROI con coordinate (xCenterMROI, yCenterMROI) e diametro
	 * diamMROI, all'Array contenente i valori dei pixel pixList11. Tale Array viene
	 * utilizzato tipicamente per ottenere tutti i pixels appartenenti ad una sfera
	 * 
	 * @param imp11
	 * @param xCenterMROI
	 * @param yCenterMROI
	 * @param diamMROI
	 * @param pixList11
	 */

	public static void pixVectorize2(ImagePlus imp11, double xCenterMROI, double yCenterMROI, double diamMROI,
			ArrayList<Float> pixList11) {

		imp11.setRoi(new OvalRoi(xCenterMROI - diamMROI / 2, yCenterMROI - diamMROI / 2, diamMROI, diamMROI));
		Roi roi11 = imp11.getRoi();

		ImageProcessor ip11 = imp11.getProcessor();
		ImageProcessor mask11 = roi11 != null ? roi11.getMask() : null;
		Rectangle r11 = roi11 != null ? roi11.getBounds() : new Rectangle(0, 0, ip11.getWidth(), ip11.getHeight());
		for (int y = 0; y < r11.height; y++) {
			for (int x = 0; x < r11.width; x++) {
				if (mask11 == null || mask11.getPixel(x, y) != 0) {
					pixList11.add((float) ip11.getPixelValue(x + r11.x, y + r11.y));
				}
			}
		}
	}

	// ############################################################################

	/**
	 * Calcolo della distanza tra un punto ed una circonferenza
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param r2
	 * @return
	 */
	public static double pointCirconferenceDistance(int x1, int y1, int x2, int y2, int r2) {

		double dist = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) - r2;
		return dist;
	}

	public static int[][] numeroPixelsColori(ImagePlus imp1, int[] myColor) {

		if (imp1 == null) {
			IJ.error("numeroPixelClassi ricevuto null");
			return (null);
		}
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		int offset = 0;
		int[][] vetClassi = new int[myColor.length + 1][2];
		boolean manca = true;
		for (int i1 = 0; i1 < myColor.length; i1++) {
			vetClassi[i1][0] = myColor[i1];
		}
		if (imp1.getImageStackSize() > 1) {
			for (int z1 = 0; z1 < imp1.getImageStackSize(); z1++) {
				ImagePlus imp2 = Utility.imageFromStack(imp1, z1 + 1);
				if (imp2 == null)
					continue;
				ImageProcessor ip2 = imp2.getProcessor();
				int[] pixels2 = (int[]) ip2.getPixels();
				int pix2 = 0;
				for (int y1 = 0; y1 < height; y1++) {
					for (int x1 = 0; x1 < width; x1++) {
						offset = y1 * width + x1;
						pix2 = pixels2[offset];
						manca = true;
						for (int i1 = 0; i1 < myColor.length; i1++)
							if (pix2 == vetClassi[i1][0]) {
								vetClassi[i1][1] = vetClassi[i1][1] + 1;
								manca = false;
								break;
							}
						if (manca) {
							vetClassi[5][1] = vetClassi[5][1] + 1;
							manca = false;
						}
					}
				}
			}
		} else {
			ImageProcessor ip1 = imp1.getProcessor();
			int[] pixels1 = (int[]) ip1.getPixels();
			int pix1 = 0;
			for (int y1 = 0; y1 < height; y1++) {
				for (int x1 = 0; x1 < width; x1++) {
					offset = y1 * width + x1;
					pix1 = pixels1[offset];
					manca = true;
					for (int i1 = 0; i1 < myColor.length; i1++)
						if (pix1 == vetClassi[i1][0]) {
							vetClassi[i1][1] = vetClassi[i1][1] + 1;
							manca = false;
							break;
						}
					if (manca) {
						vetClassi[5][1] = vetClassi[5][1] + 1;
						manca = false;
					}
				}
			}
		}
		return (vetClassi);

	} // classi

	// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// =================================================================================
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// =================================================================================
	// =================================================================================

	// This script implements the Plugins>Filters>Signed 16-bit
	// to Unsigned command, which converts signed 16-bit
	// images and stacks to unsigned.

//	appunto 
//	
//	  imp = IJ.getImage();
//	  stack = imp.getStack();
//	  if (stack.isVirtual())
//	     IJ.error("Non-virtual stack required");
//	  cal = imp.getCalibration();
//	  if (!cal.isSigned16Bit())
//	     IJ.error("Signed 16-bit image required");
//	  cal.disableDensityCalibration();
//	  ip = imp.getProcessor();
//	  min = ip.getMin();
//	  max = ip.getMax();
//	  stats = new StackStatistics(imp);
//	  minv = stats.min;
//	  for (i=1; i<=stack.getSize(); i++) {
//	     ip = stack.getProcessor(i);
//	     ip.add(-minv);
//	  }
//	  imp.setStack(stack);
//	  ip = imp.getProcessor();
//	  ip.setMinAndMax(min-minv, max-minv);
//	  imp.updateAndDraw();

}