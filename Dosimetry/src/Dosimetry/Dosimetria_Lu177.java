package Dosimetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Plot;
import ij.io.DirectoryChooser;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.plugin.DICOM;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.util.DicomTools;
import ij.util.FontUtil;

/**
 * @version v3 ccc
 * @author
 * @since 05 dec 2022
 */
public class Dosimetria_Lu177 implements PlugIn {

	FontUtil fu = new FontUtil();
	String fontStyle = "Arial";
	Font titleFont = FontUtil.getFont(this.fontStyle, 1, 18.0F);
	Font textFont = FontUtil.getFont(this.fontStyle, 2, 16.0F);
	Font defaultFont = FontUtil.getFont(this.fontStyle, 0, 14.0F);

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

	public void run(String paramString) {

		desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
		pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";
		desktopDosimetryFolderPath = desktopPath + File.separator + "DosimetryFolder";
		desktopImagesSubfolderPath = desktopDosimetryFolderPath + File.separator + "ImagesFolder";

		Utility.appendLog(pathVolatile, "---- LOADPATIENT -----");

		String petctviewerTitle = "";
		Double activitySomministrazione;
		String dataSomministrazione;
		String oraSomministrazione;
		Date myDate0 = null;
		boolean nuovoPaziente = false;
		boolean nuovoDistretto = false;
		File[] arrayOfFile2 = null;

		// ===========================================================
		// LEGGO CARTELLA DOSIMETRY FOLDER (E SOTTOCARTELLA IMAGES FOLDER)
		// ===========================================================
		String[] insOut = inspector(desktopDosimetryFolderPath);
		if (insOut == null) {
			IJ.log("NON ESISTE DOSIMETRY_FOLDER SUL DESKTOP");
			nuovoPaziente = true;
		} else if (insOut.length == 0) {
			IJ.log("NON ESISTONO IMMAGINI PAZIENTE");
			nuovoPaziente = true;
		} else {
			// ===========================================================
			// DIALOGO CON DATI PAZIENTE PRECEDENTE - NUOVA LESIONE
			// ===========================================================
			IJ.log("DIALOGO NUOVO PAZIENTE OPPURE NUOVA LESIONE");
			nuovoPaziente = dialogImmaginiPazientePrecedente_LP21(insOut);
			if (!nuovoPaziente) {
				// SUL PAZIENTE PRECEDENTE DOBBIAMO VEDERE SE E' UNA NUOVA LESIONE (ED ALLORA
				// ANALIZZEREMO LE MEDESIME IMMAGINI) OPPURE SE E'UN NUOVO DISTRETTO DI CUI
				// ANDARE A CARICARE LE IMMAGINI
				nuovoDistretto = dialogDistretto_LP07();
			}
		}
		if (nuovoPaziente) {
			IJ.log("NUOVO PAZIENTE, TRASFERIMENTO IMMAGINI");
			arrayOfFile2 = desktopImagesFolderFill();
			IJ.log("NUOVO PAZIENTE, INIZIALIZZAZIONE LOG E RICHIESTA DATI SOMMINISTRAZIONE");
			Utility.deleteAllLogs(desktopDosimetryFolderPath);
			Utility.initLog(pathPermanente);
			Utility.initLog(pathVolatile);
			String[] datiSomministrazione = null;
			boolean okDati = false;
			do {
				datiSomministrazione = dialogDatiSomministrazione_LP04();
				if (datiSomministrazione == null) {
					IJ.log("datiSomministrazione NON PERVENUTI");
					return;
				}
				okDati = dialogConfirmDatiSomministrazione_LP10(datiSomministrazione);
			} while (!okDati);
			dataSomministrazione = datiSomministrazione[0];
			oraSomministrazione = datiSomministrazione[1];
			activitySomministrazione = Double.parseDouble(datiSomministrazione[2]);

			IJ.log("NUOVO PAZIENTE, SCRITTURA DATI SOMMINISTRAZIONE DA PERMANENTE");
			Utility.appendLog(pathPermanente, "-- SOMMINISTRAZIONE --");
			String aux1 = "";
			aux1 = "#100#\tData= " + dataSomministrazione;
			Utility.appendLog(pathPermanente, aux1);
			aux1 = "#101#\tOra= " + oraSomministrazione;
			Utility.appendLog(pathPermanente, aux1);
			aux1 = "#102#\tActivity= " + activitySomministrazione;
			Utility.appendLog(pathPermanente, aux1);
			Utility.appendLog(pathPermanente, "--------------------");
			Utility.copiaSomministrazione(pathPermanente, pathVolatile);
			myDate0 = getDateTime(dataToDicom(dataSomministrazione), oraToDicom(oraSomministrazione));
			raccoltaDati(arrayOfFile2, myDate0);
		} else if (nuovoDistretto) {
			IJ.log("NUOVO DISTRETTO, CARICAMENTO IMMAGINI E \nRECUPERO DATI SOMMINISTRAZIONE DA PERMANENTE");
			arrayOfFile2 = desktopImagesFolderFill();
			Utility.copiaSomministrazione(pathPermanente, pathVolatile);
			dataSomministrazione = Utility.readFromLog(pathVolatile, "#100#", "=");
			oraSomministrazione = Utility.readFromLog(pathVolatile, "#101#", "=");
			activitySomministrazione = Double.parseDouble(Utility.readFromLog(pathVolatile, "#102#", "="));
			myDate0 = getDateTime(dataToDicom(dataSomministrazione), oraToDicom(oraSomministrazione));
			IJ.log("dataSomministrazione= " + dataSomministrazione);
			IJ.log("oraSomministrazione= " + oraSomministrazione);

		} else {
			IJ.log("NUOVA LESIONE, RECUPERO DATI SOMMINISTRAZIONE DA PERMANENTE");
			Utility.copiaSomministrazione(pathPermanente, pathVolatile);
			dataSomministrazione = Utility.readFromLog(pathVolatile, "#100#", "=");
			oraSomministrazione = Utility.readFromLog(pathVolatile, "#101#", "=");
			activitySomministrazione = Double.parseDouble(Utility.readFromLog(pathVolatile, "#102#", "="));
			myDate0 = getDateTime(dataToDicom(dataSomministrazione), oraToDicom(oraSomministrazione));
			IJ.log("dataSomministrazione= " + dataSomministrazione);
			IJ.log("oraSomministrazione= " + oraSomministrazione);
		}

		// ===========================================================================
		// per evitare di utilizzare il menu di scelta, dobbiamo avviare pet_ct_viewer
		// passandogli nell'argomento gli UID di due immagini stack aperte. Tali UID
		// sono il TAG Dicom "0020,000E". NOTARE che occorre fare un trim delle
		// stringhe, se si vuole che Pet_Ct_Viever le accetti senza fare storie
		// ===========================================================================

		String str1 = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "DosimetryFolder"
				+ File.separator + "ImagesFolder" + File.separator;

		// ===========================================================================
		// ELABORAZIONE 24h ed apertura PetCtViewer
		// ===========================================================================
		String startingDir1 = str1 + "24h" + File.separator + "SPECT";

		List<File> result1 = Utility.getFileListing(new File(startingDir1));
		if (result1 == null) {
			IJ.error("getFileListing.result1==null");
		}
		int len2 = result1.size();
		if (len2 != 1)
			return;
		File fil1 = result1.get(0);
		String path1 = fil1.getAbsolutePath();
		ImagePlus imp1 = openImage(path1);
		String tit1 = imp1.getTitle();
		tit1 = "A024 ## " + tit1;
		imp1.setTitle(tit1);
		imp1.show();
		int slice1 = 1;
		String meta1 = getMeta(slice1, imp1);
		petctviewerTitle = stringaLaboriosa(meta1);
		Utility.appendLog(pathVolatile, "24h=" + petctviewerTitle);

		String petUID1 = DicomTools.getTag(imp1, "0020,000E");
		petUID1 = petUID1.trim();

		String startingDir2 = str1 + "24h" + File.separator + "CT";

		List<File> result2 = Utility.getFileListing(new File(startingDir2));
		if (result2 == null) {
			IJ.error("getFileListing.result2==null");
		}
		String[] list2 = new String[result2.size()];
		int j2 = 0;
		for (File file2a : result2) {
			list2[j2++] = file2a.getPath();
		}

		ImagePlus imp2 = readStackFiles(startingDir2);

		String tit2 = imp2.getTitle();
		tit2 = "B024 ## " + tit2;
		imp2.setTitle(tit2);
		imp2.show();

		String ctUID2 = DicomTools.getTag(imp2, "0020,000E");
		ctUID2 = ctUID2.trim();

		// possiamo passare a petCtViewer una stringa con gli UID delle immagini PET e
		// CT da aprire
		String seriesUID1 = petUID1 + ", " + ctUID2;
		IJ.runPlugIn("Pet_Ct_Viewer", seriesUID1);
		IJ.wait(2000);

		// ===========================================================================
		// ELABORAZIONE 48h ed apertura PetCtViewer
		// ===========================================================================

		String startingDir3 = str1 + "48h" + File.separator + "SPECT";
		List<File> result3 = Utility.getFileListing(new File(startingDir3));
		if (result3 == null) {
			IJ.error("getFileListing.result3==null");
		}
		int len3 = result3.size();
		if (len3 != 1)
			return;

		File fil3 = result3.get(0);
		String path3 = fil3.getAbsolutePath();
		ImagePlus imp3 = openImage(path3);
		String tit3 = imp3.getTitle();
		tit3 = "A048 ## " + tit3;
		imp3.setTitle(tit3);
		imp3.show();
		String meta3 = getMeta(slice1, imp3);
		petctviewerTitle = stringaLaboriosa(meta3);
		Utility.appendLog(pathVolatile, "48h=" + petctviewerTitle);

		String petUID3 = DicomTools.getTag(imp3, "0020,000E");
		petUID3 = petUID3.trim();

		String startingDir4 = str1 + "48h" + File.separator + "CT";
		List<File> result4 = Utility.getFileListing(new File(startingDir4));
		if (result4 == null) {
			IJ.error("getFileListing.result4==null");
		}
		String[] list4 = new String[result4.size()];
		int j4 = 0;
		for (File file4a : result4) {
			list4[j4++] = file4a.getPath();
		}

		ImagePlus imp4 = readStackFiles(startingDir4);
		String tit4 = imp4.getTitle();
		tit4 = "B048 ## " + tit4;
		imp4.setTitle(tit4);
		// IJ.log("tit4= " + imp4.getTitle());
		imp4.show();

		String ctUID4 = DicomTools.getTag(imp4, "0020,000E");
		ctUID4 = ctUID4.trim();

		String seriesUID3 = petUID3 + ", " + ctUID4;
		IJ.runPlugIn("Pet_Ct_Viewer", seriesUID3);
		IJ.wait(2000);

		// ===========================================================================
		// ELABORAZIONE 120h ed apertura PetCtViewer
		// ===========================================================================

		String startingDir5 = str1 + "120h" + File.separator + "SPECT";
		List<File> result5 = Utility.getFileListing(new File(startingDir5));
		if (result5 == null) {
			IJ.error("getFileListing.result5==null");
		}
		int len5 = result5.size();
		if (len5 != 1)
			return;

		File fil5 = result5.get(0);
		String path5 = fil5.getAbsolutePath();
		ImagePlus imp5 = openImage(path5);
		String tit5 = imp5.getTitle();
		tit5 = "A120 ## " + tit5;
		imp5.setTitle(tit5);
		imp5.show();
		String meta5 = getMeta(slice1, imp5);
		petctviewerTitle = stringaLaboriosa(meta5);
		Utility.appendLog(pathVolatile, "120h=" + petctviewerTitle);

		String petUID5 = DicomTools.getTag(imp5, "0020,000E");
		petUID5 = petUID5.trim();

		String startingDir6 = str1 + "120h" + File.separator + "CT";
		List<File> result6 = Utility.getFileListing(new File(startingDir6));
		if (result6 == null) {
			IJ.error("getFileListing.result6==null");
		}
		String[] list6 = new String[result6.size()];
		int j6 = 0;
		for (File file6 : result6) {
			list6[j6++] = file6.getPath();
		}

		ImagePlus imp6 = readStackFiles(startingDir6);
		String tit6 = imp6.getTitle();
		tit6 = "B120 ## " + tit6;
		imp6.setTitle(tit6);
		// IJ.log("tit6= " + imp6.getTitle());
		imp6.show();

		// ==================================================

		String ctUID6 = DicomTools.getTag(imp6, "0020,000E");
		ctUID6 = ctUID6.trim();

		String seriesUID5 = petUID5 + ", " + ctUID6;
		IJ.runPlugIn("Pet_Ct_Viewer", seriesUID5);
		IJ.wait(2000);

		// ===========================================================================
		imp2.close();
		imp4.close();
		imp6.close();
		dialogSelection_LP30();

		// qui abbiamo la difficolta'che il PetCtViewer si mette in primo piano e
		// dobbiamo per forza cancellare i 3 dialoghi con ok/cancel ma il primo menu con
		// la scelta immagini di dosimetry_v2 finisce inevitabilmente dietro le finestre
		// di petctviewer, ho provato a metterlo in coordinate 0,0 in modo che rimanga
		// comunquie visibile e trasportabile sullo schermo

//		dialogSelection_LP31();

		IJ.runPlugIn("Dosimetry.Dosimetry_v2", "");

		Utility.endLog(pathPermanente);

		// ==========================================================================================
		// PARTE GRAFICA
		// ==========================================================================================

		double[] in1 = new double[4];
		// 24h
		in1[0] = Double.parseDouble(Utility.readFromLog(pathPermanente, "#038#", "=")); // durata
		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#112#", "=")); // conteggio
		in1[2] = Double.parseDouble(Utility.readFromLog(pathPermanente, "#102#", "=")); // activity
		in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#108#", "=")); // threshold

		double[] out24 = Utility.puntoGrafico(pathVolatile, pathPermanente, in1);
		// 48h
		in1[0] = Double.parseDouble(Utility.readFromLog(pathPermanente, "#048#", "=")); // durata
		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#122#", "=")); // conteggio
		in1[2] = Double.parseDouble(Utility.readFromLog(pathPermanente, "#102#", "=")); // activity
		in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#118#", "=")); // threshold

		double[] out48 = Utility.puntoGrafico(pathVolatile, pathPermanente, in1);
		// 120h
		in1[0] = Double.parseDouble(Utility.readFromLog(pathPermanente, "#058#", "=")); // durata
		in1[1] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#132#", "=")); // conteggio
		in1[2] = Double.parseDouble(Utility.readFromLog(pathPermanente, "#102#", "=")); // activity
		in1[3] = Double.parseDouble(Utility.readFromLog(pathVolatile, "#128#", "=")); // threshold

		double[] out120 = Utility.puntoGrafico(pathVolatile, pathPermanente, in1);

		double[] xp = new double[3];
		double[] yp = new double[3];
		xp[0] = 24.0;
		yp[0] = out24[2];
		xp[1] = 48.0;
		yp[1] = out48[2];
		xp[2] = 120.0;
		yp[2] = out120[2];

		// le accentate ce le sogniamo, si mette l'apostrofo come 40 anni fa'
		Plot plot = new Plot("Punti", "ore dalla somministrazione", "attivita' MBq");
		plot.setLineWidth(2);
		plot.setColor(Color.red);
		plot.add("circle", xp, yp);
		plot.show();

		Utility.debugDeiPoveri("---- CHE BELLIZZIMO GRAFICO -----");
		// ==========================================================================
		// PARTE REVIEW CHE DEVE RITORNARE INDIETRO PER RIFARE UNO O PIU'DEI CALCOLI
		// FINALMENTE SAREMO FELICI E GORGOGLIONI DELLE NOSTRE ELABORAZIONI
		// ==========================================================================

		Utility.battezzaLesioni(pathVolatile, pathPermanente);

//		dialogReview_LP05(aList);

	}

	/**
	 * Copia i file immagine da una directory all'altra
	 * 
	 * @param infile  Directory sorgente
	 * @param outfile Directory destinazione
	 * @throws IOException
	 */
	private void copyDirectory(File infile, File outfile) throws IOException {
		if (infile.isDirectory()) {
			if (!outfile.exists())
				outfile.mkdir();
			String[] arrayOfString = infile.list();
			for (byte b = 0; b < arrayOfString.length; b++)
				copyDirectory(new File(infile, arrayOfString[b]), new File(outfile, arrayOfString[b]));
		} else {
			FileInputStream fileInputStream = new FileInputStream(infile);
			FileOutputStream fileOutputStream = new FileOutputStream(outfile);
			byte[] arrayOfByte = new byte[1024];
			int i;
			while ((i = fileInputStream.read(arrayOfByte)) > 0)
				fileOutputStream.write(arrayOfByte, 0, i);
			fileInputStream.close();
			fileOutputStream.close();
		}
	}

	/**
	 * Cancellazione directory
	 * 
	 * @param deleteFolder
	 */
	private void deleteFolderFiles(File deleteFolder) {

		if (Utility.checkDir(deleteFolder)) {
			File[] arrayOfFile = deleteFolder.listFiles();
			for (File file : arrayOfFile) {
				if (file.isDirectory())
					deleteFolderFiles(file);
				file.delete();
			}
		}
	}

	/**
	 * Apre una immagine dal path
	 * 
	 * @param path
	 * @return
	 */
	public ImagePlus openImage(String path) {

		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(path);
		if (imp == null) {
			Utility.debugDeiPoveri("Immagine " + path + " inesistente o non visualizzabile");
			return null;
		}
		return imp;
	}

	/**
	 * Legge le immagini da una cartella e le inserisce in uno stack. Copiato da
	 * https://github.com/ilan/fijiPlugins (Ilan Tal) Class: Read_CD
	 * 
	 * @param myPath
	 * @return ImagePlus (stack)
	 */

	ImagePlus readStackFiles(String myPath) {
		int j, k, n0, n, width = -1, height = 0, depth = 0, samplePerPixel = 0;
		int bad = 0, fails = 0;
		Opener opener;
		ImagePlus imp, imp2 = null;
		ImageStack stack;
		Calibration cal = null;
		double min, max, progVal;
		FileInfo fi = null;
		String parName, flName, info, label1, tmp;
		String[] frameText = null;
		ArrayList<ImagePlus> imgList = null;
		String mytitle = "";
		// BI_dbSaveInfo curr1 = null;

		info = null;
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		stack = null;
//		parName = currRow.flName.getParent();
		File flPath = new File(myPath);
		File checkEmpty;
		File[] results = flPath.listFiles();
		// CONTROLLO CHE IL FILE PIACCIA AD IMAGEJ

		for (int i1 = 0; i1 < results.length; i1++) {
			flName = results[i1].getPath();
			isDicomImage(flName);
		}

//		if(currRow.flList != null && !currRow.flList.isEmpty()) {
//			results = new File[currRow.flList.size()];
//			currRow.flList.toArray(results);
//		}
		n0 = results.length;
		// look for graphics files
//		if( n0 <= 4) for( j = 0; j < n0; j++) {
//			opener = new Opener();
//			flName = results[j].getPath();
//			k = opener.getFileType(flName);
//			if( k == Opener.UNKNOWN || k == Opener.TEXT) bad++;
//		}

		// BI_dbSaveInfo fa riferimento alle immagini
		// immagazzinate sul loro database ospedaliero
		for (j = 1; j <= n0; j++) {
//			curr1 = new BI_dbSaveInfo();
			progVal = ((double) j) / n0;
			IJ.showStatus(j + "/" + n0);
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
//			if( k == Opener.UNKNOWN || k == Opener.TEXT) {
//				if( tmp.startsWith("graphic") && tmp.endsWith("gr1")) {
//					frameText = ChoosePetCt.getFrameText(flName);
//				}
//				continue;
//			}
//			tmp = currRow.dicomDirPath;
//			curr1.isDicomDir = (tmp != null && !tmp.isEmpty());
//			curr1.flName = checkEmpty;
//			curr1.patName = currRow.patName;
//			curr1.patID = currRow.patID;
//			curr1.styName = currRow.styName;
//			curr1.serName = currRow.serName;
//			curr1.styDate = currRow.styDate;
//			curr1.accession = currRow.accession;
			opener.setSilentMode(true);
			imp = opener.openImage(flName);
			if (imp == null) {
				fails++;
				if (fails > 2) {
//					tmp = "Cannot read this data.\n";
//					tmp += "For Bio-Formats data, use Import -> Bio-Formats";
//					JOptionPane.showMessageDialog(this, tmp);
					IJ.showProgress(1.0);
					return null;
				}
				continue;
			}
			info = (String) imp.getProperty("Info");
			mytitle = imp.getTitle();

			k = Utility.parseInt(DicomTools.getTag(imp, "0028,0002"));

//			k = ChoosePetCt.parseInt(ChoosePetCt.getDicomValue(info, "0028,0002"));
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
//				imp.setProperty("bidb", curr1);
				// imp.show(); // show a normal stack
				// imgList.add(imp);
//				curr1 = null;
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
			// stack = ChoosePetCt.mySort(stack);
			if (fi != null) {
				fi.fileFormat = FileInfo.UNKNOWN;
				fi.fileName = "";
				fi.directory = "";
			}
//			imp2 = new ImagePlus(getTitleInfo(currRow), stack);
			imp2 = new ImagePlus(mytitle, stack);
			imp2.getProcessor().setMinAndMax(min, max);
//			imp2.setProperty("bidb", curr1);
			if (n0 == 1 + bad || depth > 1)
				imp2.setProperty("Info", info);
			if (fi != null)
				imp2.setFileInfo(fi);
			double voxelDepth = DicomTools.getVoxelDepth(stack);
			if (voxelDepth > 0.0 && cal != null)
				cal.pixelDepth = voxelDepth;
			imp2.setCalibration(cal);
//			if (frameText != null)
//				for (j = 0; j < frameText.length; j++) {
//					label1 = frameText[j];
//					if (label1 != null) {
//						int i1 = j + 1;
//						tmp = stack.getSliceLabel(i1);
//						if (tmp != null) {
//							// the slices are counted from the bottom up
//							i1 = stack.getSize() - j;
//							tmp = stack.getSliceLabel(i1);
//							label1 += "\n" + tmp;
//						}
//						stack.setSliceLabel(label1, i1);
//					}
//				}
//			imp2 = myMakeMontage(imp2, info, frameText != null, stack.getSize());
//			imgList.add(imp2); // keep track of images loaded

		}
		IJ.showProgress(1.0);
		return imp2;
	}

	/***
	 * Testa se fileName1 e' un file dicom ed e' un immagine visualizzabile da
	 * ImageJ, eventualmente scrive a log nome file e tipo di errore
	 * 
	 * @param fileName1
	 * @return boolean
	 */
	public boolean isDicomImage(String fileName1) {
		boolean ok = true;
		String info = new DICOM().getInfo(fileName1);
		if (info == null || info.length() == 0) {
			IJ.log("il file " + fileName1 + " risulta INDIGESTO ad ImageJ >>> NOT DICOM INFO");
			ok = false;
		} else if (!info.contains("7FE0,0010")) {
			IJ.log("il file " + fileName1 + " risulta INDIGESTO ad ImageJ >>> NOT IMAGE");
			ok = false;
		}
		return ok;
	}

	/**
	 * Prende i dati header
	 * 
	 * @param slice
	 * @param img1
	 * @return
	 */
	static String getMeta(int slice, ImagePlus img1) {
		// first check that the user hasn't closed the study
		if (img1.getImage() == null)
			return null;
		String meta = img1.getStack().getSliceLabel(slice);
		// meta will be null for SPECT studies
		if (meta == null)
			meta = (String) img1.getProperty("Info");
		return meta;
	}

	/**
	 * Tentativo di partorire la laboriosa stringa usata come titolo del PetCtViewer
	 * 
	 * @param meta header dicom della immagine o dello stack
	 * @return parto della stringa
	 */
	static String stringaLaboriosa(String meta) {

// 		dobbiamo usare l'immagine corretta, in quella taroccata dovrebbe chiamarsi CAVLO
		m_patName = compressPatName(getDicomValue(meta, "0010,0010"));
//		IJ.log("m_patName >>>> CAVLO= " + m_patName);
		String tmp = getDicomValue(meta, "0010,0030");
//		IJ.log("getDateTime: date= " + tmp + " time=" + null);

		m_patBirthday = getDateTime(tmp, null);
//		IJ.log("m_patBirthday= " + m_patBirthday);
		if (m_patBirthday != null) {
			long sdyTime, birthTime, currDiff;
			Integer years;
			int type2 = -1;
			Date studyDateTime = getStudyDateTime(meta, type2);
			sdyTime = studyDateTime.getTime();
//			sdyTime = getStudyDate().getTime();
			birthTime = m_patBirthday.getTime();
			currDiff = (sdyTime - birthTime) / (24 * 60 * 60 * 1000); // number of days
			years = (int) (currDiff / 365.242199);
			m_patYears = years.toString() + "y";
		}
//		IJ.log("m_patYears= " + m_patYears);
		m_patID = compressID(getDicomValue(meta, "0010,0020"));
//		IJ.log("m_patID= " + m_patID);
		int type3 = -1;
		Date studyDateTime1 = getStudyDateTime(meta, type3);
//		IJ.log("type3= " + type3 + " studyDateTime= " + studyDateTime1.toString());
		m_serDate = UsaDateFormat(studyDateTime1);
		m_styName = getDicomValue(meta, "0008,1030");
		petSeriesName = getDicomValue(meta, "0008,103E");
		String laboriosa = "Pet-Ct: " + m_patName + "   " + m_patYears + "   " + m_patID + "   " + m_serDate + "   "
				+ m_styName + "   " + petSeriesName.toLowerCase();

		return laboriosa;
	}

	/**
	 * Lettura di un dato Dicom da headere immagine
	 * 
	 * @param meta
	 * @param key1
	 * @return
	 */
	static String getDicomValue(String meta, String key1) {
		String tmp1, key2 = key1, ret1 = null;
		int k1, k0 = 0;
		if (meta == null)
			return ret1;
		if (key1 != null) {
			k0 = meta.indexOf(key1);
			if (k0 <= 0)
				key2 = key1.toLowerCase();
			k0 = meta.indexOf(key2);
		}
		if (k0 > 0 || key2 == null) {
			// here we have a problem that the key may appear more than once.
			// for example a SeriesUID may appear in a sequence. Look for ">".
			if (k0 > 0) {
				tmp1 = meta.substring(k0 + 4, k0 + 16);
				k1 = tmp1.indexOf(">");
				while (k1 > 0) { // do search last value
					k1 = meta.indexOf(key2, k0 + 4);
					if (k1 > 0)
						k0 = k1;
				}
			}
			k1 = meta.indexOf("\n", k0);
			if (k1 < 0)
				return null;
			tmp1 = meta.substring(k0, k1);
			k1 = tmp1.indexOf(": ");
			if (k1 > 0)
				ret1 = tmp1.substring(k1 + 2);
			else
				ret1 = tmp1;
			ret1 = ret1.trim();
			if (ret1.isEmpty())
				ret1 = null;
		}
		return ret1;
	}

	/**
	 * Presenta patName secondo un suo standard
	 * 
	 * @param inName
	 * @return
	 */
	static String compressPatName(Object inName) {
		String retVal = checkEmpty(inName);
		if (retVal == null)
			return null;
		retVal = retVal.trim();
		int i = retVal.indexOf('^');
		if (i < 0)
			return retVal;
		retVal = retVal.substring(0, i) + "," + retVal.substring(i + 1);
		retVal = retVal.replace('^', ' ').trim();
		return retVal;
	}

	/**
	 * 
	 * @param in1
	 * @return
	 */
	static String checkEmpty(Object in1) {
		if (in1 == null || !(in1 instanceof String))
			return null;
		String out1 = (String) in1;
		if (out1.isEmpty())
			return null;
		return out1;
	}

	/**
	 * Helper routine to convert from Dicom style date-time to Java date-time. Watch
	 * out, sometimes the date uses periods, 2008.10.04 NOTA BENE: ANCORA DA METTERE
	 * A POSTO
	 * 
	 * @param inDate Dicom date format
	 * @param inTime Dicom time format
	 * @return Java Date object
	 */
	public static Date getDateTime(String inDate, String inTime) {

		Date retDate;
		GregorianCalendar dat1 = new GregorianCalendar();
		int off, year, month, day, hour = 0, min1 = 0, sec = 0;
		if (inDate == null || inDate.length() < 8)
			return null;
		off = 0; // normal case with no period
		if (inDate.charAt(4) == '.')
			off = 1;
		// watch out for bad date 01.01.1900
		if (inDate.charAt(2) == '.')
			return null;

		year = Integer.valueOf(inDate.substring(0, 4));
		month = Integer.valueOf(inDate.substring(4 + off, 6 + off)) - 1; // month 0 based
		day = Integer.valueOf(inDate.substring(6 + 2 * off, 8 + 2 * off));
		if (inDate.length() >= 14) {
			hour = Integer.valueOf(inDate.substring(8, 10));
			min1 = Integer.valueOf(inDate.substring(10, 12));
			sec = Integer.valueOf(inDate.substring(12, 14));
		} else if (inTime != null && inTime.length() >= 6) {
			hour = Integer.valueOf(inTime.substring(0, 2));
			min1 = Integer.valueOf(inTime.substring(2, 4));
			sec = Integer.valueOf(inTime.substring(4, 6));
		}
		dat1.set(year, month, day, hour, min1, sec);
		retDate = dat1.getTime();
		return retDate;
	}

	/**
	 * Acquisizione DateTime per vari tipi di immagini
	 * 
	 * @param meta
	 * @param type
	 * @return
	 */
	static Date getStudyDateTime(String meta, int type) {
		String key1 = null, key2 = null, time1 = null, time2, tmp1;
		String key0 = "0008,0021"; // series date
		switch (type) {
		case 0:
			key1 = "0008,0030"; // study time
			key0 = "0008,0020"; // study date
			break;

		case 1:
			key2 = "0008,002A"; // acquisition date time
			key1 = "0008,0031"; // series time
			key0 = "0008,0021"; // series date
			break;

		case 2:
			key1 = "0008,0032"; // acquisition time
			key0 = "0008,0022"; // acquisition date
			break;

		case 3:
			key2 = "0018,1078"; // date time
			key1 = "0018,1072"; // injection time
			key0 = "0009,103B"; // GE Advance Scan.admin_datetime PRIVATE TAG
			break;

		case 4:
			key1 = "0008,0033"; // image time
			key0 = "0008,0023"; // image date
			break;
		}
		if (key1 != null) {
			time1 = getDicomValue(meta, key1);
		}
		if (key2 != null && (type == 3 || time1 == null)) {
			time2 = getDicomValue(meta, key2);
			if (time2 != null)
				time1 = time2; // prefer key2
			if (time1 != null && time1.length() >= 14)
				return getDateTime(time1, null);
		}
		// use study date since the injection may be 24 or 48 hours earlier
		tmp1 = getDicomValue(meta, key0);
		if (tmp1 == null) {
			tmp1 = getDicomValue(meta, "0008,0020");
			if (tmp1 == null)
				return null;
			// be careful of bad study dates like 1899
			if (Integer.valueOf(tmp1.substring(0, 4)) < 1980) {
				tmp1 = getDicomValue(meta, "0008,0021");
			}
		}

		return getDateTime(tmp1, time1);
	}

	/**
	 * 
	 * @param in1
	 * @return
	 */
	static String compressID(Object in1) {
		String ret1, ret0 = checkEmpty(in1);
		if (ret0 == null)
			return "0";
		ret0 = ret0.toLowerCase();
		int i, i1, n = ret0.length();
		char a1;
		ret1 = "";
		for (i = i1 = 0; i < n; i++) {
			a1 = ret0.charAt(i);
			if (Character.isDigit(a1) || Character.isLetter(a1)) {
				if (i1 == 0 && a1 == '0')
					continue;
				ret1 = ret1 + a1;
				i1++;
			}
		}
		if (i1 == 0)
			return "0";
		return ret1;
	}

	static String UsaDateFormat(Date inDate) {
		if (inDate == null)
			return "";
		return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(inDate);
	}

	/**
	 * Calcolo della durata dell'acquisizione in secondi
	 * 
	 * @param imp1 immagine da analizzare
	 * @return durata
	 */
	int CalcoloDurataAcquisizione(ImagePlus imp1) {

		int numFrames = Utility.parseInt(DicomTools.getTag(imp1, "0054,0053"));
		int durationFrame = Utility.parseInt(DicomTools.getTag(imp1, "0018,1242"));
		int durata = numFrames * (durationFrame / 1000);
//		IJ.log("numberOfFrames= " + numFrames + " durationFrame= " + durationFrame + " durata= " + durata);
		String aux1 = "";
		aux1 = "#110#\tMird durata acquisuizione= " + durata;
		Utility.appendLog(pathPermanente, aux1);

		return durata;
	}

	/**
	 * Visualizzazione messaggi di errore
	 * 
	 * @param paramString
	 */
	void dialogErrorMessage_LP06(String paramString) {

		IJ.log("dialogErrorMessage_LP06");
		GenericDialog genericDialog = new GenericDialog("LP06 - Error");
		genericDialog.addMessage(paramString, this.defaultFont);
		genericDialog.hideCancelButton();
		genericDialog.showDialog();
	}

	/**
	 * Dialogo inserimento dati iniezione
	 * 
	 * @return
	 */
	String[] dialogDatiSomministrazione_LP04() {

		IJ.log("dialogDatiSomministrazione_LP04");
		String[] out1 = new String[3];
		String data0;
		String ora0;
		Double activity0;
		String activity1;

		IJ.log("LP04 start");
		GenericDialog gd11 = new GenericDialog("LP04 - Date/Time/Activity");
		gd11.addMessage("Introduci i seguenti dati per il nuovo paziente", this.titleFont);
		gd11.setFont(this.defaultFont);
		String label11 = "Data [dd-mm-yyyy]";
		String format11 = "dd-mm-yyyy";
		int digits11 = 8;
		gd11.addStringField(label11, format11, digits11);
		String label12 = "Ora [HH:mm:ss]";
		String format12 = "HH:mm:ss";
		int digits12 = 8;
		gd11.addStringField(label12, format12, digits12);

		String label13 = "Attivita' somministrata";
		double default13 = 0.00;
		int digits13 = 8;
		gd11.addNumericField(label13, default13, digits13, 10, "[MBq]");
		gd11.setCancelLabel("Annulla");
		gd11.showDialog();
		if (gd11.wasCanceled()) {
			IJ.error("Cancel");
			return null;
		}

		data0 = gd11.getNextString();
		boolean ok1 = Utility.isValidDate(data0, format11);
		if (!ok1) {
			IJ.error("Data sbagliata");
			return null;
		}

		ora0 = gd11.getNextString();
		boolean ok2 = Utility.isValidTime(ora0, format12);
		if (!ok2) {
			IJ.error("Ora sbagliata");
			return null;
		}

		activity0 = gd11.getNextNumber();
		activity1 = "" + activity0;
		out1[0] = data0;
		out1[1] = ora0;
		out1[2] = activity1;
		IJ.log("LP04 end");
		return out1;
	}

	boolean dialogDistretto_LP07() {

		IJ.log("dialogo LP07");
		GenericDialog genericDialog3 = new GenericDialog("LP07 - ALTRO DISTRETTO");
		genericDialog3.addMessage("Posizione lesione", titleFont);
		genericDialog3.addMessage("La lesione si trova in questo o altro distretto?", textFont);
		genericDialog3.setOKLabel("STESSO DISTRETTO");
		genericDialog3.setCancelLabel("ALTRO DISTRETTO");
		genericDialog3.showDialog();
		if (genericDialog3.wasCanceled()) {
			IJ.log("LP07 - true STESSO DISTRETTO");
			return true;
		} else {
			IJ.log("LP07 - false ALTRO DISTRETTO");
			return false;
		}
	}

	/**
	 * Dialogo conferma dati iniezione
	 * 
	 * @param in1
	 * @return
	 */
	boolean dialogConfirmDatiSomministrazione_LP10(String[] in1) {

		IJ.log("dialogConfirmDatiSomministrazione_LP10");
		String data11 = in1[0];
		String ora11 = in1[1];
		String activity11 = in1[2];

		IJ.log(in1[0]);
		IJ.log(in1[1]);
		IJ.log(in1[2]);

		GenericDialog conf11 = new GenericDialog("LP10 - CONFIRM");

		conf11.addMessage("CONFERMA DATI SOMMINISTRAZIONE", this.titleFont);
		conf11.setFont(this.defaultFont);
		conf11.addMessage("Data " + data11 + "   [dd-mm-yyyy]");
		conf11.addMessage("Ora " + ora11 + "   [hh:mm:ss]");
		conf11.addMessage("Attivita' introdotta " + activity11 + "   [MBq]");
		conf11.showDialog();

		if (conf11.wasOKed()) {
			IJ.log("LP10 - true PREMUTO OK");
			return true;
		} else {
			IJ.log("LP00 - true PREMUTO Cancel");
			return false;
		}
	}

	/**
	 * Dialogo con dati delle cartelle 24/48/120
	 * 
	 * @param aList
	 */
	boolean dialogReview_LP05(ArrayList<ArrayList<String>> aList) {

		IJ.log("dialogReview_LP05");
		GenericDialog reviewDialog = new GenericDialog("LP05 - Review Dicom Tags");
		reviewDialog.addMessage("Check the Dicom tags", this.titleFont);
		reviewDialog.addMessage("Please review if the acquisition settings used are correct.", this.defaultFont);
		for (int a4 = 0; a4 < aList.size(); a4++) {
			reviewDialog.addMessage(arrayOfString[a4] + " folder path:", this.textFont);
			String str22 = "";
			ArrayList<String> cList = aList.get(a4);
			for (int b4 = 0; b4 < cList.size(); b4++) {
				String str9 = cList.get(b4);
				str22 = str22 + str9;
			}
			reviewDialog.addMessage(str22, this.defaultFont);
		}
		reviewDialog.showDialog();
		if (reviewDialog.wasCanceled()) {
			return false;
		} else {
			reviewDialog.dispose();
			return true;
		}

	}

	boolean dialogInitialize_LP00() {

		IJ.log("dialogInitialize_LP00");
		GenericDialog genericDialog3 = new GenericDialog("LP00 - INIZIALIZZA PER NUOVO PAZIENTE");
		genericDialog3.addMessage("Inizializza per nuovo paziente", titleFont);
		genericDialog3.addMessage("File PERMANENTE salvataggio dati", textFont);
		genericDialog3.setOKLabel("MANTIENI");
		genericDialog3.setCancelLabel("INIZIALIZZA");
		genericDialog3.showDialog();
		if (genericDialog3.wasCanceled()) {
			IJ.log("LP00 - true PREMUTO INIZIALIZZA");
			return true; // EBBENE SI, DA BENE COSI'
		} else {
			IJ.log("LP00 - false PREMUTO MANTIENI");
			return false;
		}
	}

	boolean dialogNonBlockingDelete_LP01(String str20) {

		IJ.log("dialogNonBlockingDelete_LP01");
		NonBlockingGenericDialog nonBlockingGenericDialog = new NonBlockingGenericDialog("LP01 - Command Confirmation");
		nonBlockingGenericDialog.addMessage("Confirmation Dialog", this.titleFont);
		nonBlockingGenericDialog.addMessage(
				"Are you sure to delete all files in the following folder?\nThis action is irreversible.\n" + str20,
				this.defaultFont);
		nonBlockingGenericDialog.setCancelLabel("ANNULLA");
		nonBlockingGenericDialog.showDialog();
		if (nonBlockingGenericDialog.wasCanceled()) {
			IJ.log("LP01 - false PREMUTO ANNULLA");
			return false;
		} else {
			IJ.log("LP01 - true PREMUTO OK");
			return true;
		}
	}

	boolean dialogSelection_LP02() {

		IJ.log("dialogSelection_LP02");
		GenericDialog genericDialog = new GenericDialog("LP02 - Select images Folder");
		genericDialog.addMessage("24h Folder Selection", this.titleFont);
		genericDialog.addMessage("Select folder of the 24h acquisition", this.defaultFont);
		genericDialog.setOKLabel("BROWSE");
		genericDialog.setCancelLabel("QUIT");
		genericDialog.showDialog();
		if (genericDialog.wasCanceled()) {
			IJ.log("LP02 - true PREMUTO QUIT");
			return true;
		} else {
			IJ.log("LP02 - false PREMUTO BROWSE");
			genericDialog.dispose();
			return false;
		}
	}

	String directorySelection_LP_20() {

		IJ.log("directorySelection_LP_20");
		DirectoryChooser directoryChooser = new DirectoryChooser("LP20 Directory Selection");
		String str3 = directoryChooser.getDirectory();
		if (str3 == null) {
			dialogErrorMessage_LP06("Wrong selection. Please try again.");
			IJ.log("LP20 - null  ERROR MESSAGE Wrong selection. Please try again ");
			return null;
		} else {
			IJ.log("LP20 - selezione effettuata");
		}
		return str3;
	}

	boolean dialogConfirmFolder_LP03(String str24, String str48, String str120) {

		IJ.log("dialogConfirmFolder_LP03");
		GenericDialog genericDialog1 = new GenericDialog("LP03 - Confirm images Folder");
		genericDialog1.addMessage("Confirm Image Selection", this.titleFont);
		genericDialog1.addMessage("Check Image Folder Auto-Selection", this.defaultFont);
		genericDialog1.addMessage("24h folder path:", this.textFont);
		genericDialog1.addMessage(str24, this.defaultFont);
		genericDialog1.addMessage("48h folder path:", this.textFont);
		genericDialog1.addMessage(str48, this.defaultFont);
		genericDialog1.addMessage("120h folder path:", this.textFont);
		genericDialog1.addMessage(str120, this.defaultFont);
		genericDialog1.setOKLabel("CONFIRM");
		genericDialog1.setCancelLabel("QUIT");
		genericDialog1.showDialog();
		if (genericDialog1.wasCanceled()) {
			IJ.log("LP03 - false PREMUTO QUIT");
			return false;
		} else {
			IJ.log("LP03 - true PREMUTO CONFIRM");
			genericDialog1.dispose();
			return true;
		}

	}

	void initializeLogs(boolean init) {
		// --------------------------------------------------------------------------------------
		// definisco ed inizializzo (se richiesto) i file log di esportazione dati
		// --------------------------------------------------------------------------------------
		Date now = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy - HH:mm.ss");
		Utility.initLog(pathVolatile);
		Utility.appendLog(pathVolatile, "INITIALIZED " + dateformat.format(now));
		Utility.appendLog(pathVolatile, "---------------------");
		File f1 = new File(pathPermanente);
		if (init || !f1.exists()) {
			Utility.initLog(pathPermanente);
			Utility.appendLog(pathPermanente, "INITIALIZED " + dateformat.format(now));
		} else {
			Utility.appendLog(pathPermanente, "PRESERVED " + dateformat.format(now));
		}

		Utility.appendLog(pathPermanente, "--- LOAD PATIENT---");

	}

	void copyFilesToDesktopDosimetryFolder(File[] arrayOfFile1, File[] arrayOfFile2, boolean[] arrayOfBoolean,
			String desktopImagesSubfolderPath) {
		// ----------------------------------------
		// Copia delle immagini dalla sorgente al DosimetryFolder situato sul desktop
		// ----------------------------------------

		int len1 = 0;
		int count1 = 0;

		for (int b2 = 0; b2 < arrayOfFile1.length; b2++) {
			if (arrayOfBoolean[b2]) {
				File file = arrayOfFile1[b2];
				File[] arrayOfFile = file.listFiles();
				len1 = len1 + arrayOfFile.length;
			}
		}

		for (int b2 = 0; b2 < arrayOfFile1.length; b2++) {
			if (arrayOfBoolean[b2]) {
				File file = arrayOfFile1[b2];
				File[] arrayOfFile = file.listFiles();
				for (File file5 : arrayOfFile) {
					count1++;
					IJ.showStatus(count1 + "/" + len1);
					File file6 = null;
					if (file5.getName().contains("IRAC")) {
						file6 = new File(desktopImagesSubfolderPath + File.separator + arrayOfString[b2]
								+ File.separator + "SPECT" + File.separator + file5.getName());
						try {
							copyDirectory(file5, file6);
						} catch (Exception exception) {
							dialogErrorMessage_LP06(
									"An Error occurred while coping the SPECT image. Please try again!");
							return;
						}
					} else if (file5.getName().contains("CTAC")) {
						file6 = new File(desktopImagesSubfolderPath + File.separator + arrayOfString[b2]
								+ File.separator + "CT" + File.separator + file5.getName());
						try {
							copyDirectory(file5, file6);
						} catch (Exception exception) {
							dialogErrorMessage_LP06("An Error occurred while coping CT images. Please try again!");
							return;
						}
					} else if (file5.getName().contains("EM001")) {
						arrayOfFile2[b2] = file5;
					}
				}
			} else {
				dialogErrorMessage_LP06("It was not possible to import files for " + arrayOfString[b2] + " folder.");
			}

		}

	}

	/**
	 * Ispeziona DosimetryFolder/ImagesFolder
	 * 
	 * @param path1
	 * @return
	 */
	String[] inspector(String path1) {

		IJ.log("INSPECTOR");
		File file1 = new File(path1);
		File file2 = new File(path1 + File.separator + "ImagesFolder");
		List<File> list1 = null;
		boolean ok1 = Utility.checkDir(file1);
		if (!ok1) {
			IJ.log("INSPECTOR return null MANCA DOSIMETRY_FOLDER");
			return null;
		}
		boolean ok2 = Utility.checkDir(file2);
		if (ok2) {
			list1 = Utility.getFileListing(file2);
			if (list1 == null) {
				IJ.log("INSPECTOR return null MANCA IMAGES_FOLDER");
			} else if (list1.size() == 0) {
				IJ.log("INSPECTOR list1 vuota NESSUNA IMMAGINE");
			}
		} else {
			IJ.log("INSPECTOR return null MANCA IMAGES_FOLDER");
			return null;
		}
		String path2 = list1.get(0).toString();
		ImagePlus imp2 = openImage(path2);
		if (imp2 == null)
			return null;
		String nome = DicomTools.getTag(imp2, "0010,0010");
		String seriesDescription = DicomTools.getTag(imp2, "0008,103E");
		String[] out1 = new String[2];
		out1[0] = seriesDescription;
		out1[1] = nome;

		return out1;
	}

	/**
	 * Presenza immagini nel dosimetry folder all'avvio
	 * 
	 * @param str20
	 * @return
	 */
	boolean dialogImmaginiPazientePrecedente_LP21(String[] str20) {

		IJ.log("dialogImmaginiPazientePrecedente_LP21");
		NonBlockingGenericDialog nonBlockingGenericDialog = new NonBlockingGenericDialog(
				"LP21 - Immagini paziente precedente");
		nonBlockingGenericDialog.addMessage("Presenza immagini paziente precedente", this.titleFont);
		nonBlockingGenericDialog.addMessage(
				"Attenzione: in DosimetryFolder sul Desktop ci sono le immagini \n" + str20[0] + " di " + str20[1],
				this.defaultFont);
		nonBlockingGenericDialog.setCancelLabel("CONTINUA CON ALTRE LESIONI");
		nonBlockingGenericDialog.setOKLabel("PASSA A NUOVO PAZIENTE");
		nonBlockingGenericDialog.showDialog();
		if (nonBlockingGenericDialog.wasCanceled()) {
			IJ.log("LP21 false CONTINUA CON ALTRE LESIONI");
			return false;
		} else {
			IJ.log("LP21 true PASSA A NUOVO PAZIENTE");
			return true;
		}
	}

	/**
	 * Cancellazione e creazione dosimetry folder e images folder, loro riempimento
	 * 
	 * @return
	 */
	public File[] desktopImagesFolderFill() {

		File file1 = new File(desktopImagesSubfolderPath);
		// cancella cartella anco se piena
		Utility.deleteDirectory(file1);

		// crea le cartelle destinazione
		for (int b1 = 0; b1 < arrayOfString.length; b1++) {

			File file5 = new File(desktopImagesSubfolderPath + File.separator + arrayOfString[b1]);
			if (file5.mkdirs()) {
			} else {
				IJ.log("cartella non creata= " + file5.toString());
			}

			File file6 = new File(
					desktopImagesSubfolderPath + File.separator + arrayOfString[b1] + File.separator + "CT");
			if (file6.mkdirs()) {
			} else {
				IJ.log("cartella non creata= " + file6.toString());
			}

			File file7 = new File(
					desktopImagesSubfolderPath + File.separator + arrayOfString[b1] + File.separator + "SPECT");
			if (file7.mkdirs()) {
			} else {
				IJ.log("cartella non creata= " + file7.toString());
			}
		}
		String strDir24h = null;
		String strDir48h = null;
		String strDir120h = null;

		// chiede di identificare la cartella 24h sorgente
		do {
			boolean quit1 = dialogSelection_LP02();
			if (quit1)
				return null;
			strDir24h = directorySelection_LP_20(); // nome directory 24h
		} while (strDir24h == null);
		// costruisce i due path 48h e 120h
		strDir48h = strDir24h.replace(arrayOfString[0], arrayOfString[1]); // nome directory 48h
		strDir120h = strDir24h.replace(arrayOfString[0], arrayOfString[2]); // nome directory 120h
		File file24 = new File(strDir24h);
		File file48 = new File(strDir48h);
		File file120 = new File(strDir120h);
		boolean[] arrayOfBoolean = { false, false, false };
		if (file24.exists()) {
			arrayOfBoolean[0] = true;
		} else {
			strDir24h = "Not Found";
		}
		if (file48.exists()) {
			arrayOfBoolean[1] = true;
		} else {
			strDir48h = "Not Found";
		}
		if (file120.exists()) {
			arrayOfBoolean[2] = true;
		} else {
			strDir120h = "Not Found";
		}
		// chiede conferma della selezione effettuata
		dialogConfirmFolder_LP03(strDir24h, strDir48h, strDir120h);

		// ----------------------------------------
		// Copia delle immagini dalla sorgente al DosimetryFolder situato sul desktop
		// ----------------------------------------

		File[] arrayOfFile1 = { file24, file48, file120 };
		File[] arrayOfFile2 = new File[arrayOfFile1.length];

		copyFilesToDesktopDosimetryFolder(arrayOfFile1, arrayOfFile2, arrayOfBoolean, desktopImagesSubfolderPath);
		return arrayOfFile2;

	}

	/**
	 * Trasformazione input Data da tastiera in formato dicom
	 * 
	 * @param data0
	 * @return
	 */
	public String dataToDicom(String data0) {

		String day = data0.substring(0, 2);
		String month = data0.substring(3, 5);
		String year = data0.substring(6, 10);
		String data1 = year + month + day;

		return data1;
	}

	/**
	 * Trasformazione input Ora da tastiera in formato dicom
	 * 
	 * @param ora0
	 * @return
	 */
	public String oraToDicom(String ora0) {

		String ora = ora0.substring(0, 2);
		String min = ora0.substring(3, 5);
		String sec = ora0.substring(6, 8);
		String ora1 = ora + min + sec;

		return ora1;
	}

	/**
	 * Lettura dati dalle immagini per NUOVO PAZIENTE, scrittura in permanente.txt
	 * 
	 * @param vetFile immagini da analizzare
	 * @param myDate0 dataOra somministrazione per calcolo deltaT
	 */
	public void raccoltaDati(File[] vetFile, Date myDate0) {

		ArrayList<ArrayList<String>> aList = new ArrayList<ArrayList<String>>();
		ArrayList<Long> eList = new ArrayList<Long>();
		for (byte b3 = 0; b3 < arrayOfString.length; b3++) {
			ImagePlus imp8 = IJ.openImage(vetFile[b3].getAbsolutePath());
			int durata = CalcoloDurataAcquisizione(imp8);
			String acqDate = DicomTools.getTag(imp8, "0008,0022").trim();
			String acqTime = DicomTools.getTag(imp8, "0008,0032").trim();
			// IJ.log("getDateTime: date= " + acqDate + " time=" + acqTime);
			Date myDate1 = getDateTime(acqDate, acqTime);
			long myDelta1 = Utility.CalcoloDeltaT(myDate0, myDate1);
			eList.add(myDelta1);
			ArrayList<String> bList = new ArrayList<String>();
			bList.add("\tImage Name= " + vetFile[b3].getName());
			bList.add("\tAcquisition Date= " + acqDate);
			bList.add("\tAcquisition Time= " + acqTime);
			bList.add("\tIsotope= " + DicomTools.getTag(imp8, "0011,100D"));
			bList.add("\tCollimator= " + DicomTools.getTag(imp8, "0018,1180"));
			bList.add("\tNumber of frames= " + DicomTools.getTag(imp8, "0054,0053"));
			bList.add("\tActual frame duration= " + DicomTools.getTag(imp8, "0018,1242"));
			bList.add("\tAcquisition duration= " + durata);
//			bList.add("\tPathCompleto: " + arrayOfFile2[b3].getAbsolutePath() + "\n"); /// solo per test
			aList.add(bList);
		}
		// --------------------------------------------------------------------------------------
		// Nel file permanente vengono scritti dal seguente loop:
		// #031#,#032#,#033#,#034#,#035#,#036#,#037#,#038#,
		// #041#,#042#,#043#,#044#,#045#,#046#,#047#,#048#,
		// #051#,#052#,#053#,#054#,#055#,#056#,#057#,#058#,
		// --------------------------------------------------------------------------------------
		String aux1 = "";
		String aux2 = "";
		String aux3 = "";
		int count2 = 0;
		for (int a4 = 0; a4 < aList.size(); a4++) {
			count2 = a4 * 10 + 30;
			aux1 = "====== " + arrayOfString[a4] + " =======";
			Utility.appendLog(pathPermanente, aux1);
			String str22 = "";
			ArrayList<String> cList = aList.get(a4);
			for (int b4 = 0; b4 < cList.size(); b4++) {
				aux2 = "#" + String.format("%03d", ++count2) + "#";
				String str9 = cList.get(b4);
				str22 = aux2 + str9;
				Utility.appendLog(pathPermanente, str22);
			}
			aux3 = "#" + String.format("%03d", ++count2) + "#" + "\tDeltaT= " + (double) eList.get(a4) / (1000 * 60 * 60);
			Utility.appendLog(pathPermanente, aux3);
		}
	}

	boolean dialogSelection_LP30() {

		IJ.log("dialogSelection_LP30");
		Dimension screen = IJ.getScreenSize();
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP30 - Dialog Close");
		gd1.addMessage("Trova le lesioni su PET-CT Viewer su tutte e tre le \nimmagini 24h, 48h e 120h, poi premi OK",
				this.defaultFont);
		gd1.setLocation(screen.width * 2 / 3, screen.height * 1 / 3);
		gd1.showDialog();
		IJ.log("LP30 - true PREMUTO OK");
		return true;
	}

	boolean dialogSelection_LP31() {

		IJ.log("dialogSelection_LP31");
		NonBlockingGenericDialog gd1 = new NonBlockingGenericDialog("LP31 - START");
		gd1.addMessage("PREMERE OK", this.defaultFont);
		gd1.showDialog();
		IJ.log("LP31 - true PREMUTO OK");
		return true;
	}

}
