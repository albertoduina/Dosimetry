package Dosimetry;

import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileFilter;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.util.DicomTools;

public class MyStack {

	/**
	 * Segnale che uno stack risulta vuoto, tutti i pixel a zero in tutte le
	 * immagini
	 * 
	 * @param imp
	 * @return
	 */
	static boolean stackIsEmpty(ImagePlus imp) {

		ImageStack stack = imp.getStack();
		int width = stack.getHeight();
		int height = stack.getHeight();
		int depth = stack.getSize();
		float[] vetValues = stack.getVoxels(0, 0, 0, width, height, depth, null);
		float sum = 0;
		for (float value : vetValues) {
			sum += value;
		}
		if (sum == 0.0)
			return true;
		else
			return false;
	}

	/**
	 * Segnale che uno stack risulta vuoto, tutti i pixel a zero in tutte le
	 * immagini
	 * 
	 * @param stack
	 * @return
	 */
	static boolean stackIsEmpty(ImageStack stack) {

		int width = stack.getHeight();
		int height = stack.getHeight();
		int depth = stack.getSize();
		float[] vetValues = stack.getVoxels(0, 0, 0, width, height, depth, null);
		float sum = 0;
		for (float value : vetValues) {
			sum += value;
		}
		if (sum == 0.0)
			return true;
		else
			return false;
	}

	/**
	 * Idea copiata da Laurent Thomas, & Pierre Trehin. (2021, July 22) github :
	 * LauLauThom/MaskFromRois-Fiji un plugin in pitonato (in pyton)
	 * 
	 * 
	 * @param impStack
	 * @return
	 */
	public static ImagePlus stackMask(ImagePlus impStack) {

		ImageStack myImageStack = impStack.getImageStack();
		int width = myImageStack.getWidth();
		int height = myImageStack.getHeight();
		int size = myImageStack.getSize();
		// ByteProcessor mask1 = new ByteProcessor(width, height); // ora ho una
		// maschera in cui 0 significa che il pixel
		// // non fa parte di alcuna patata o buccia

		ImageStack myMaskStack = new ImageStack(width, height, size);

		for (int i1 = 0; i1 < size; i1++) {
			ImageProcessor ipSlice = myImageStack.getProcessor(i1);
			ImageProcessor maskSlice = ipSlice.getMask();
			myMaskStack.addSlice(maskSlice);
		}
		ImagePlus myMaskImage = new ImagePlus("CARNIVAL", myMaskStack);
		return myMaskImage;
	}

	/**
	 * Aggiorna il contenuto di una immagine dello stack
	 * 
	 * @param stack
	 * @param ipSlice
	 * @param num
	 */
	static void stackSliceUpdater(ImageStack stack, ImageProcessor ipSlice, int num) {

		stack.deleteSlice(num);
		stack.addSlice("", ipSlice, num - 1);
		stack.setSliceLabel("MASK_" + num, num);

	}

	/**
	 * Legge le immagini da una cartella e le inserisce in uno stack. Copiato da
	 * https://github.com/ilan/fijiPlugins (Ilan Tal) Class: Read_CD. Ho disattivato
	 * alcune parti di codice riguardanti tipi di immagini di cui non disponiamo
	 * 
	 * @param myDirPath
	 * @return ImagePlus (stack)
	 */

	static ImagePlus readStackFiles2(String myDirPath) {
		int j1, k, n0, width = -1, height = 0, depth = 0, samplePerPixel = 0;
		int bad = 0, fails = 0;
		int good = 0;
		int count1 = 0;
		Opener opener;
		ImagePlus imp, imp2 = null;
		ImageStack stack;
		Calibration cal = null;
		double min, max, progVal;
		FileInfo fi = null;
		String flName, flPath, info, label1, tmp;
		String mytitle = "";
		boolean isStack = false;

		MyLog.log("readStackFiles2");

		info = null;
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		stack = null;
		File vetDirPath = new File(myDirPath);
		File checkEmpty;
		FileFilter filter = file -> {
			if (file.isFile()) {
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(".gr2") || fileName.endsWith(".txt") || fileName.endsWith(".xls")
						|| fileName.endsWith(".cvs")) {
					return false;
				}
			}
			return true;
		};

		File[] results = vetDirPath.listFiles(filter);

		if ((results == null) || (results.length == 0)) {
			MyLog.waitHere("pare non esistano files in " + myDirPath);
			return null;
		}

		// boolean ok = false;
		// for (int i1 = 0; i1 < results.length; i1++) {
		// flName = results[i1].getName();
		// flPath = results[i1].getPath();
		// if (!isDicomImage(flPath))
		// ok = Utility
		// .dialogErrorMessageWithCancel_LP09("Il file " + flName + " non e'una immagine
		// Dicom valida");
		// if (ok)
		// return null;
		// }

		n0 = results.length;

		for (j1 = 1; j1 <= n0; j1++) {

			progVal = ((double) j1) / n0;
			IJ.showStatus("readStack " + j1 + "/" + n0);
			IJ.showProgress(progVal);
			opener = new Opener();

			flName = results[j1 - 1].getPath();
			checkEmpty = new File(flName); // remember for possible dicomdir
			if (checkEmpty.length() == 0)
				continue;
			// 020323
			// iw2ayv
			if (!Utility.isDicomImage(flName)) {
				MyLog.log("la immagine " + flName + " non sembra dicom");
				continue;
			}

			tmp = results[j1 - 1].getName();
			if (tmp.equalsIgnoreCase("dirfile"))
				continue;
			k = opener.getFileType(flName);
			opener.setSilentMode(true);
			imp = opener.openImage(flName);
			if (imp == null) {
				fails++;
				if (fails > 2) {
					IJ.showProgress(1.0);
					return null;
				}
				continue;
			}
			info = (String) imp.getProperty("Info");
			mytitle = imp.getTitle();
			good++;

			k = Utility.parseInt(DicomTools.getTag(imp, "0028,0002"));
			if (stack == null) {
				samplePerPixel = k;
				width = imp.getWidth();
				height = imp.getHeight();
				depth = imp.getStackSize();
				cal = imp.getCalibration();
				fi = imp.getOriginalFileInfo();
				ColorModel cm = imp.getProcessor().getColorModel();
				stack = new ImageStack(width, height, cm);
			}
			if ((depth > 1 && n0 > 1) || width != imp.getWidth() || height != imp.getHeight() || k != samplePerPixel) {
				if (k <= 0)
					continue;
				stack = null;
				depth = 0;
				continue;
			}
			label1 = null;
			if (depth == 1) {
				label1 = imp.getTitle();
				if (info != null)
					label1 += "\n" + info;
			}
			ImageStack inputStack = imp.getStack();
			for (int slice = 1; slice <= inputStack.getSize(); slice++) {
				count1++;
				ImageProcessor ip = inputStack.getProcessor(slice);
				if (ip.getMin() < min)
					min = ip.getMin();
				if (ip.getMax() > max)
					max = ip.getMax();
				stack.addSlice(label1, ip);
			}
		}

		if (stack != null && stack.getSize() > 0) {
			if (fi != null) {
				fi.fileFormat = FileInfo.UNKNOWN;
				fi.fileName = "";
				fi.directory = "";
			}
			imp2 = new ImagePlus(mytitle, stack);
			imp2.getProcessor().setMinAndMax(min, max);
			if (n0 == 1 + bad || depth > 1)
				imp2.setProperty("Info", info);
			if (fi != null)
				imp2.setFileInfo(fi);
			double voxelDepth = DicomTools.getVoxelDepth(stack);
			if (voxelDepth > 0.0 && cal != null)
				cal.pixelDepth = voxelDepth;
			imp2.setCalibration(cal);
		}
		IJ.showProgress(1.0);

		return imp2;
	}

	/**
	 * Legge le immagini da una cartella e le inserisce in uno stack. Copiato da
	 * https://github.com/ilan/fijiPlugins (Ilan Tal) Class: Read_CD. Ho disattivato
	 * alcune parti di codice riguardanti tipi di immagini di cui non disponiamo
	 * 
	 * @param myDirPath
	 * @return ImagePlus (stack)
	 */

	static ImagePlus readStackFiles(String myDirPath) {
		int j, k, n0, width = -1, height = 0, depth = 0, samplePerPixel = 0;
		int bad = 0, fails = 0;
		Opener opener;
		ImagePlus imp, imp2 = null;
		ImageStack stack;
		Calibration cal = null;
		double min, max, progVal;
		FileInfo fi = null;
		String flName, flPath, info, label1, tmp;
		String mytitle = "";

		MyLog.log("readStackFiles");

		info = null;
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		stack = null;
		File vetDirPath = new File(myDirPath);
		File checkEmpty;
		File[] results = vetDirPath.listFiles();
		if ((results == null) || (results.length == 0)) {
			MyLog.waitHere("pare non esistano files in " + myDirPath);
			return null;
		}

		boolean ok = false;
		for (int i1 = 0; i1 < results.length; i1++) {
			flName = results[i1].getName();
			flPath = results[i1].getPath();
			if (!Utility.isDicomImage(flPath))
				ok = Utility
						.dialogErrorMessageWithCancel_LP09("Il file " + flName + " non e'una immagine Dicom valida");
			if (ok)
				return null;
		}

		n0 = results.length;

		for (j = 1; j <= n0; j++) {
			progVal = ((double) j) / n0;
			IJ.showStatus("readStack " + j + "/" + n0);
			IJ.showProgress(progVal);
			opener = new Opener();
			flName = results[j - 1].getPath();
			checkEmpty = new File(flName); // remember for possible dicomdir
			if (checkEmpty.length() == 0)
				continue;
			tmp = results[j - 1].getName();
			if (tmp.equalsIgnoreCase("dirfile"))
				continue;
			k = opener.getFileType(flName);
			opener.setSilentMode(true);
			imp = opener.openImage(flName);
			if (imp == null) {
				fails++;
				if (fails > 2) {
					IJ.showProgress(1.0);
					return null;
				}
				continue;
			}
			info = (String) imp.getProperty("Info");
			mytitle = imp.getTitle();

			k = Utility.parseInt(DicomTools.getTag(imp, "0028,0002"));
			if (stack == null) {
				samplePerPixel = k;
				width = imp.getWidth();
				height = imp.getHeight();
				depth = imp.getStackSize();
				cal = imp.getCalibration();
				fi = imp.getOriginalFileInfo();
				ColorModel cm = imp.getProcessor().getColorModel();
				stack = new ImageStack(width, height, cm);
			}
			if ((depth > 1 && n0 > 1) || width != imp.getWidth() || height != imp.getHeight() || k != samplePerPixel) {
				if (k <= 0)
					continue;
				stack = null;
				depth = 0;
				continue;
			}
			label1 = null;
			if (depth == 1) {
				label1 = imp.getTitle();
				if (info != null)
					label1 += "\n" + info;
			}
			ImageStack inputStack = imp.getStack();
			for (int slice = 1; slice <= inputStack.getSize(); slice++) {
				ImageProcessor ip = inputStack.getProcessor(slice);
				if (ip.getMin() < min)
					min = ip.getMin();
				if (ip.getMax() > max)
					max = ip.getMax();
				stack.addSlice(label1, ip);
			}
		}

		if (stack != null && stack.getSize() > 0) {
			if (fi != null) {
				fi.fileFormat = FileInfo.UNKNOWN;
				fi.fileName = "";
				fi.directory = "";
			}
			imp2 = new ImagePlus(mytitle, stack);
			imp2.getProcessor().setMinAndMax(min, max);
			if (n0 == 1 + bad || depth > 1)
				imp2.setProperty("Info", info);
			if (fi != null)
				imp2.setFileInfo(fi);
			double voxelDepth = DicomTools.getVoxelDepth(stack);
			if (voxelDepth > 0.0 && cal != null)
				cal.pixelDepth = voxelDepth;
			imp2.setCalibration(cal);
		}
		IJ.showProgress(1.0);
		return imp2;
	}

	/**
	 * A scopo di test cerco massimo, minimo e relative posizioni all'interno dello
	 * stack. Le usero' anche per impostare l'adjust delle immagini (ma MALE!)
	 * Utilizza anche la mask, contenuta in un secondo stack
	 * 
	 * @param impStackIn
	 * @param impMask
	 * @return
	 */
	static double[] MyStackStatistics(ImagePlus impStackIn, ImagePlus impMask) {

		ImageStack stackIn = impStackIn.getImageStack();
		ImageStack stackMask = impMask.getImageStack();
		int width1 = stackIn.getWidth();
		int height1 = stackIn.getHeight();
		int depth1 = stackIn.getSize();
		ImageProcessor inSlice1 = null;
		ImageProcessor maskSlice1 = null;
		double pixel = 0;
		double mask = 0;
		double minStackVal = Double.MAX_VALUE;
		double maxStackVal = Double.MIN_VALUE;
		double meanStackVal = Double.NaN;
		double sumPix = 0;
		int[] minStackCoord = new int[3];
		int[] maxStackCoord = new int[3];
		long pixCount = 0;

		for (int z1 = 0; z1 < depth1; z1++) {
			inSlice1 = stackIn.getProcessor(z1 + 1);
			maskSlice1 = stackMask.getProcessor(z1 + 1);
			for (int x1 = 0; x1 < width1; x1++) {
				for (int y1 = 0; y1 < height1; y1++) {
					IJ.showStatus("StackStatisticsA " + z1 + " / " + (depth1));
					pixel = inSlice1.getPixelValue(x1, y1);
					mask = maskSlice1.getPixelValue(x1, y1);
					if (pixel > 0 && mask > 0) {
						sumPix = sumPix + pixel;
						pixCount++;
						if (pixel < minStackVal) {
							minStackVal = pixel;
							minStackCoord[0] = x1;
							minStackCoord[1] = y1;
							minStackCoord[2] = z1;
						}
						if (pixel > maxStackVal) {
							maxStackVal = pixel;
							maxStackCoord[0] = x1;
							maxStackCoord[1] = y1;
							maxStackCoord[2] = z1;
						}
					}
				}
			}

		}
		meanStackVal = sumPix / pixCount;
		double[] out1 = new double[11];
		out1[0] = (double) minStackCoord[0];
		out1[1] = (double) minStackCoord[1];
		out1[2] = (double) minStackCoord[2];
		out1[3] = minStackVal;
		out1[4] = (double) maxStackCoord[0];
		out1[5] = (double) maxStackCoord[1];
		out1[6] = (double) maxStackCoord[2];
		out1[7] = maxStackVal;
		out1[8] = pixCount;
		out1[9] = meanStackVal;
		out1[10] = sumPix;

		return out1;
	}

	/**
	 * A scopo di test cerco massimo, minimo e relative posizioni all'interno dello
	 * stack. Le usero' anche per impostare l'adjust delle immagini (ma MALE!)
	 * 
	 * @param impStackIn
	 * @return
	 */
	static double[] MyStackStatistics(ImagePlus impStackIn) {

		ImageStack stackIn = impStackIn.getImageStack();
		int width1 = stackIn.getWidth();
		int height1 = stackIn.getHeight();
		int depth1 = stackIn.getSize();
		ImageProcessor inSlice1 = null;
		double pixel = 0;
		double minStackVal = Double.MAX_VALUE;
		double maxStackVal = Double.MIN_VALUE;
		double meanStackVal = Double.NaN;
		double sumPix = 0;
		int[] minStackCoord = new int[3];
		int[] maxStackCoord = new int[3];
		long pixCount = 0;

		for (int z1 = 0; z1 < depth1; z1++) {
			inSlice1 = stackIn.getProcessor(z1 + 1);
			for (int x1 = 0; x1 < width1; x1++) {
				for (int y1 = 0; y1 < height1; y1++) {
					IJ.showStatus("stackStatisticsB " + z1 + " / " + (depth1));
					// if (z1 == 0 && x1 == 0 && y1 == 0)
					// MyLog.waitHere();
					pixel = inSlice1.getPixelValue(x1, y1);
					if (pixel > 0) {
						sumPix = sumPix + pixel;
						pixCount++;
						if (pixel < minStackVal) {
							minStackVal = pixel;
							minStackCoord[0] = x1;
							minStackCoord[1] = y1;
							minStackCoord[2] = z1;
						}
						if (pixel > maxStackVal) {
							maxStackVal = pixel;
							maxStackCoord[0] = x1;
							maxStackCoord[1] = y1;
							maxStackCoord[2] = z1;
						}
					}
				}
			}

		}
		meanStackVal = sumPix / pixCount;
		double[] out1 = new double[11];
		out1[0] = (double) minStackCoord[0];
		out1[1] = (double) minStackCoord[1];
		out1[2] = (double) minStackCoord[2];
		out1[3] = minStackVal;
		out1[4] = (double) maxStackCoord[0];
		out1[5] = (double) maxStackCoord[1];
		out1[6] = (double) maxStackCoord[2];
		out1[7] = maxStackVal;
		out1[8] = pixCount;
		out1[9] = meanStackVal;
		out1[10] = sumPix;

		return out1;
	}

	/**
	 * Conta i pixel impostati ( maggiori di zero, valgono 255) in uno stackMask
	 * 
	 * @param impMaskStack
	 * @return
	 */
	static int MyStackCountPixels(ImagePlus impMaskStack) {

		int count = 0;
		int maxcount = 0;
		int slice = 0;
		double voxMask = 0;
		int width = impMaskStack.getWidth();
		int heigth = impMaskStack.getHeight();
		int depth = impMaskStack.getNSlices();

		ImageStack stackMask = impMaskStack.getImageStack();

		for (int z1 = 1; z1 < depth; z1++) {
			count = 0;
			for (int x1 = 0; x1 < width; x1++) {
				for (int y1 = 0; y1 < heigth; y1++) {
					voxMask = stackMask.getVoxel(x1, y1, z1);
					if (voxMask > 0)
						count++;
				}
			}

			if (count >= maxcount) {
				maxcount = count;
				slice = z1;
			}
		}

		return slice;
	}

}
