package Dosimetry;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

/**
 * @version v3
 * @author Date 05 dec 2022
 */
public class S_VoxelDosimetry implements PlugIn {

	// =================================================================================
	// =================================================================================
	// =====================PROVA PER
	// GITHUB============================================
	// =================================================================================
	// =================================================================================
	// VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV

	public void run(String arg) {

//		String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
//		String pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
//		String pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";

		String str1 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator + "ImagesFolder" + File.separator;
		String str2 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator;

		String startingDir1 = str1 + "24h" + File.separator + "SPECT";
		caricaMemoriazza(startingDir1, str2);
		MyLog.waitHere();
	}

	void caricaMemoriazza(String startingDir1, String str2) {
		// la nostra memoriazza e'costituita da una matrice double[][] in cui vengono
		// caricati i dati delle immagini ed in seguito verranno pure scritti i dati
		// delle elabotrazioni

		ImagePlus imp1 = null;
		ImageProcessor ip1 = null;
		int slice1 = 0;

		imp1 = Utility.readStackFiles(startingDir1);
		imp1.show();
		new FileSaver(imp1).saveAsTiff(str2 + "stackDelCass");

		// ora cerco di estrarre un voxel, parametrizzato. Come ho scritto in una cosa
		// che ho lettio 2 giorni fa e non riesco piu'a trovare MaremmaMaiala, la
		// funzione getVoxel di ImageStack promette molto bene: nessuna descrizione,
		// nessuno che la usa: FA AL CASO NOSTRO SICURAMENTE!!!

		ImageStack sta1 = imp1.getImageStack();
//		sta1.convertToFloat();

		// confermato sperimentalmente;
		// si fornisconi le coordinate xyz del pixel 0 (in alto a sx prima fetta) e poi
		// si forniscono le dimensioni del voxel. I valori, nel vettore vengono messi,
		// nel nostro caso: x0,y0,z0; x1,y0,z0; ... x0, y1, z0; x1,y1,z0; .... x0, y0,
		// z1; x1, y0, z1 ecc
		// quanto al null fornito, sarebbe il posto di un vettore float in input, che
		// deve coincidere come dimensionicon WxHxD, non ho chiaro a che scopo

		int x0 = 52;
		int y0 = 60;
		int z0 = 26;
		int width = 60;
		int height = 60;
		int depth = 60;
		

		float[] vetVox1 = sta1.getVoxels(x0, y0, z0, width, height, depth, null);

//		int count = 0;
//		for (float aux1 : vetVox1) {
//			MyLog.log("" + count++ + " " + aux1);
//		}

		// ed ora provo la riscrittura dell'intero cubetto 3x3, per esperimento aggiungo
		// 3000 al segnale di ognuno dei pixel

		for (int i1 = 0; i1 < vetVox1.length; i1++) {
			vetVox1[i1] = vetVox1[i1] + 3000;
		}
		sta1.setVoxels(x0, y0, z0, width, height, depth, vetVox1);

		// ed ora facciamo un cubetto nero, coassiale
		for (int i1 = 0; i1 < vetVox1.length; i1++) {
			vetVox1[i1] = vetVox1[i1] - 3000;
		}

		sta1.setVoxels(x0 + 25, y0 + 25, z0 + 25, 10, 10, 10, vetVox1);

		// ed ora gli impianto un singolo pixel, che si vede benissimo in fetta 57, si
		// apprezza che la coordinata e' in integer, e' appena appena spostato rispetto
		// al centro perfeetto !!

		sta1.setVoxel(x0 + 30, y0 + 30, z0 + 30, 33500.0);

		MyLog.waitHere("STICAZZI");

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

}