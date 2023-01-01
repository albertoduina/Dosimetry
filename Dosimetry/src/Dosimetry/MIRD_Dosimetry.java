package Dosimetry;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

//import ReadCdStudies.CD_dirInfo;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.Prefs;
import ij.Undo;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.util.DicomTools;
import ij.util.FontUtil;

/**
 * @version v3
 * @author Date 05 dec 2022
 */
public class MIRD_Dosimetry implements PlugIn {

	private int stackSize = 1;
	private boolean referenceRadioButtonIndex = false;
	static String desktopPath;
	static String pathPermanente;
	static String pathVolatile;

	public void run(String arg) {

		desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
		pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";

		Utility.debugDeiPoveri(
				"ATTENZIONE: il 26/12 ho sgamato un overlay contornato in azzurro, che era identico ad uno giallo,\n"
						+ " ma spostato di alcuni pixels a simnistra, LA ROI AZZURRA CORRISPONDE A QUELLA "
						+ "(FORSE) DELLA FETTA CENTRALE, FACENDOLE SCORRERE SI VEDE BENISSIMO CHE COINCIDE COI PIXEL ROSSI");

//		String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
//		String pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
//		String pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";

		// -------------------------------------------------
		// Variabili al sicuro dai guai
		// -------------------------------------------------
		boolean ok24 = false;
		boolean ok48 = false;
		boolean ok120 = false;
		String sTime = "";
		boolean resetflag = true;
		int oreProcessate = 0;

		int posizioneInput = 0;
		int numeroLesione = 0;
		int fettaCranioCaudale = 0;
		int fettaCaudoCraniale = 0;
		int distanzaCranioCaudale = 4;
		int distanzaCaudoCraniale = 4;
		int radioButtonIndex = 0;
		int posizioneLesione = -1;
		int contornamento = -1;
		double threshold = 0.4;
		boolean flagPersonalizedMaximum = false;
		List<Roi> roiPerFetta = null;
		List<Integer> posizioniRoiPerFetta = null;
		RoiManager roiManager = new RoiManager(false);
		Roi guscioEsterno = null;
		String histogramFileName = "histogram";
		float[] pos = null;
		float max = 0;
		int personalizedMaximum = 0;
		int prosegui = 100;
		int point1 = -1;
		double[][] matTable = { { 0.3, 0.4, 0.5 }, { 2.0050800, 1.4416900, 1.1119950 },
				{ 1.0008740, 1.0009900, 1.0016370 }, { 0.0838640, 0.1330990, 0.1528385 } };

		ImageProcessor ip = null;
		boolean flagContornamentoManuale = false;
		boolean flagROIPETCT = false;

		String fontStyle = "Arial";
		Font titleFont = FontUtil.getFont(fontStyle, 1, 18);
		Font textFont = FontUtil.getFont(fontStyle, 2, 16);
		Font defaultFont = FontUtil.getFont(fontStyle, 0, 14);

		// leggo volatile.txt e poi isolo i dati che mi interessano, che erano scritti
		// da Load_Patient
		String[] vetLogList = Utility.readLog(pathVolatile);
		String[] vetPath = new String[3];
		for (int i1 = 0; i1 < vetLogList.length; i1++) {
			vetPath[0] = Utility.readFromLog(pathVolatile, "24h=", "=");
			vetPath[1] = Utility.readFromLog(pathVolatile, "48h=", "=");
			vetPath[2] = Utility.readFromLog(pathVolatile, "120h=", "=");
		}

		ImagePlus dicomImage = null;

		// =====================================================================================================
		// LOOP PRINCIPALE
		// =====================================================================================================
		while (prosegui > 0) {
			if (prosegui == 90) {
				resetflag = true; // resetta la roi sovrapposta all'immagine, nonchè scale ed overlay
			}
//				IJ.log("RITORNO PER SCELTA IMMAGINE SUCCESSIVA");
//			if (prosegui == 95)
//				IJ.log("RITORNO RIPETIZIONE CON LESIONE SUCCESSIVA");
//			if (prosegui == 80)
//				IJ.log("RITORNO PER START AGAIN");

			// =========================================================
			// QUI ENTRA NEI SEGUENTI CASI
			// prosegui = 100 al primo loop
			// prosegui = 95 RITORNO RIPETIZIONE CON LESIONE SUCCESSIVA
			// START NORMALE DOPO SCELTA 24/48/120
			// =========================================================

			if (prosegui > 80) { // START NORMALE, dopo scelta 24/48/120

				threshold = 0.4; // iw2ayv 241222 riporta il valore di default ad ogni cambio di slice come punto
									// 3 di nuove modifiche 20/12/22

				// ottengo i nomi delle immagini aperte
				String[] lista1 = WindowManager.getImageTitles();
				dicomImage = sceltaAutomaticaImmagine(ok24, ok48, ok120, defaultFont);

				pos = imageMaximum(dicomImage);
				max = pos[3];
				personalizedMaximum = (int) max;
				ip = dicomImage.getProcessor();

				radioButtonIndex = 0; // resetta la scelta in 888

				// cerco di determinare che scelta hanno fatto
				String title1 = dicomImage.getTitle();
				for (int i1 = 0; i1 < lista1.length; i1++) {
					if (lista1[i1].equals(title1)) {
						point1 = i1;
					}
				}

				Utility.imageToFront(title1);

				Utility.nonImageToFront(vetPath[point1]);

				WaitForUserDialog waitForUserDialog = new WaitForUserDialog("OPERATORE",
						"Localizza la nuova lesione in PET-CT Viewer, vedi il numero di slice attivando Edit/BrownFatRoi e ricordalo, poi quando sei soddisfatto premi OK");
				waitForUserDialog.show();

				Utility.imageToFront(title1);

//				dicomImage.getWindow().toFront();

				// determino quale immagine hanno scelto, per poi, alla memorizzazione
				// risultato, cambiare colore alla scritta nel menu

				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				// =================================================================================
				// =================================================================================
				// =================================================================================
				// =================================================================================
				// =================================================================================

//		ImagePlus dicomImage = WindowManager.getCurrentImage();
//		this.dicomImage = dicomImage;

				while (dicomImage == null) { // scelta manuale
					dicomImage = sceltaManualeImmagine(titleFont, defaultFont);
					dicomImage.show();
				}

				ImageStack stack = dicomImage.getStack();
				stackSize = stack.getSize();
				Undo.reset();

//				String name1 = dicomImage.getTitle();
//				FileInfo fileInfo1 = dicomImage.getFileInfo();

				Dimension screen = IJ.getScreenSize();
				ImageWindow window = dicomImage.getWindow();
				window.setLocationAndSize(0, 0, (int) (((double) screen.height) / 2),
						(int) (((double) screen.height) / 2));

			}

			int fettaCranioCaudaleFinal = -1;
			int fettaCaudoCranialeFinal = -1;

			roiManager.reset();

			if (resetflag) {
				IJ.run("Set Scale...", "distance=0 known=0 pixel=1");
				IJ.run("Select None");
				IJ.run("Remove Overlay");
				IJ.resetThreshold();
			} else {
				dicomImage.setRoi(guscioEsterno);
				Roi aaa = dicomImage.getRoi();
			}

			IJ.setTool("oval");

			String[] scelteReferenceSystem = new String[] { "ImageJ Reference System",
					"Pet-Ct Viewer Reference System" };

			// sappiamo che se prosegui > 80 arriviamo da start o da scelta 24/48/120
			// invece prosegui <=80 significa Start Again

			do {

				NonBlockingGenericDialog dialogSlice = new NonBlockingGenericDialog("DD00 - Required Parameters");
				dialogSlice.addMessage("Slice selection", titleFont);
				dialogSlice.addMessage("Image slices number: " + stackSize + ".", textFont);
				dialogSlice.addNumericField("Insert slice index: ", posizioneInput, 0);
				dialogSlice.addRadioButtonGroup("Select reference system:", scelteReferenceSystem, 2, 1,
						scelteReferenceSystem[referenceRadioButtonIndex ? 0 : 1]);
				dialogSlice.setCancelLabel("Quit");
				dialogSlice.setOKLabel("Next");
				dialogSlice.setFont(defaultFont);
				dialogSlice.showDialog();

				if (!dialogSlice.wasOKed()) {
					IJ.run("Select None");
					IJ.run("Remove Overlay");
					IJ.resetThreshold();
					// Utility.debugDeiPoveri("001 resetThreshold");
					return;
				}

				Vector referenceRadioButtonsRaw = dialogSlice.getRadioButtonGroups();
				CheckboxGroup referenceRadioButtons = (CheckboxGroup) referenceRadioButtonsRaw.get(0);
				Checkbox referenceRadioButtonSelection = referenceRadioButtons.getSelectedCheckbox();
				String referenceRadioButtonSelectionString = referenceRadioButtonSelection.getLabel();

				if (referenceRadioButtonSelectionString == scelteReferenceSystem[0])
					referenceRadioButtonIndex = true;
				else
					referenceRadioButtonIndex = false;

				posizioneInput = (int) dialogSlice.getNextNumber();
				if (referenceRadioButtonIndex)
					posizioneLesione = posizioneInput;
				else
					posizioneLesione = convertFromPETCTReference(posizioneInput);

				if (posizioneLesione <= 0 | posizioneLesione > stackSize)
					IJ.error("Non Valid Input",
							"Hint: the input index is the slice number where the\n lesion is located in the selected reference system.\n \nIt must be an integer between 1 and "
									+ stackSize + ".\n \nPlease try again.");

			} while (posizioneLesione <= 0 | posizioneLesione > stackSize);

			dicomImage.setSlice(posizioneLesione);

			fettaCaudoCraniale = posizioneLesione + distanzaCaudoCraniale;
			fettaCranioCaudale = posizioneLesione - distanzaCranioCaudale;

			NonBlockingGenericDialog dialog = new NonBlockingGenericDialog("DD01 -  Parameter Selection");
			dialog.addMessage("ROI definition and parameter selection", titleFont);
			dialog.addMessage("Fill the required information and draw a reference ROI around the lesion.", textFont);
			dialog.addNumericField("Select threshold for segmentation (use -1 for imported ROI):", threshold, 2);
			dialog.addMessage("Select the Range of Slices for ROI drawing and calculation. (center slice "
					+ posizioneLesione + ").\n", textFont);
			dialog.addNumericField("First slice in Cranio-Caudal direction", fettaCranioCaudale, 0);
			dialog.addNumericField("Last slice in Caudo-Cranial direction", fettaCaudoCraniale, 0);

			// scelteContornamento dopo start o cambio 24/48/120 deve essere selezionato
			// con startAgain deve essere con la scelta memorizzata

			String[] scelteContornamento = new String[] { "Same reference ROI for all slices",
					"ROI imported from Pet-Ct Viewer (.csv file)", "Slice by slice manual conturing." };
			dialog.addRadioButtonGroup("Select Contouring Method", scelteContornamento, 3, 1,
					scelteContornamento[radioButtonIndex]);

			dialog.addCheckbox("Select to use a personalized maximum.", false);
			dialog.addNumericField("", 1500, 0);
			dialog.setFont(defaultFont);
			dialog.setCancelLabel("Quit");
			dialog.setOKLabel("Next");
			dialog.showDialog();

			if (!dialog.wasOKed()) {
				IJ.run("Select None");
				IJ.run("Remove Overlay");
				IJ.resetThreshold();
				// Utility.debugDeiPoveri("002 resetThreshold");
				return;
			}
			prosegui = 70;
			numeroLesione++;

			threshold = dialog.getNextNumber();
			fettaCranioCaudale = (int) dialog.getNextNumber();
			fettaCaudoCraniale = (int) dialog.getNextNumber();
			personalizedMaximum = (int) dialog.getNextNumber();
			flagPersonalizedMaximum = dialog.getNextBoolean();

			distanzaCaudoCraniale = fettaCaudoCraniale - posizioneLesione;
			distanzaCranioCaudale = posizioneLesione - fettaCranioCaudale;

			Vector radioButtonsRaw = dialog.getRadioButtonGroups();
			CheckboxGroup radioButtons = (CheckboxGroup) radioButtonsRaw.get(0);
			Checkbox radioButtonSelection = radioButtons.getSelectedCheckbox();
			String radioButtonSelectionString = radioButtonSelection.getLabel();

			for (int choice = 0; choice < scelteContornamento.length; choice++) {
				if (radioButtonSelectionString == scelteContornamento[choice]) {
					radioButtonIndex = choice;
					break;
				}
			}

			switch (radioButtonIndex) {
			case 0:
				roiManager.runCommand(dicomImage, "Add");
				contornamento = 0;
				flagROIPETCT = false;
				flagContornamentoManuale = false;

				break;
			case 1:
				roiPerFetta = new ArrayList<Roi>();
				posizioniRoiPerFetta = new ArrayList<Integer>();
				flagROIPETCT = true;
				flagContornamentoManuale = false;
				contornamento = 1;
				break;
			case 2:
				roiPerFetta = new ArrayList<Roi>();
				flagROIPETCT = false;
				flagContornamentoManuale = true;
				contornamento = 2;
				break;
			}
//			IJ.log("Dopo scelte contornamento ho= " + contornamento + " flagContornamentoManuale= "
//					+ flagContornamentoManuale + " flagROIPETCT= " + flagROIPETCT);

			if (!isSelectionEmpty() && !flagContornamentoManuale || flagContornamentoManuale || flagROIPETCT) {
				if (!flagROIPETCT && !flagContornamentoManuale) {
					guscioEsterno = roiManager.getRoi(0);
					roiManager.reset();
					dicomImage.setRoi(guscioEsterno);
				} else if (flagROIPETCT) {
					GenericDialog importRoiDialog = new GenericDialog("DD02 - Import ROI from Pet-Ct Viewer");
					importRoiDialog.addMessage("Import ROI", titleFont);
					importRoiDialog.addMessage("Select .csv file exported from Pet-Ct Viewer Plugin", defaultFont);
					importRoiDialog.setOKLabel("Browse");
					importRoiDialog.showDialog();

					if (importRoiDialog.wasCanceled())
						continue;

					try {
						String path = IJ.getFilePath("Select csv file");
						Scanner csvScanner = new Scanner(new File(path));

						int pointsIndex = 0;

						while (csvScanner.hasNextLine()) {
							String singleLine = csvScanner.nextLine();
							if (singleLine.contains("num points")) {

								float[][] points = pointsEstractionFromCSV(singleLine);
								float[] xPoints = extractPointCoordinates(points, 'x');
								float[] yPoints = extractPointCoordinates(points, 'y');
								float zPoints = points[0][2];

								guscioEsterno = new PolygonRoi(xPoints, yPoints, Roi.POLYGON);
								roiPerFetta.add(guscioEsterno);
								posizioniRoiPerFetta.add((int) zPoints);
							}
						}
					} catch (FileNotFoundException e) {
						IJ.error("Error", "File Not Found. Please Try Again");
						continue;
					}

				}

				double roiMax = -Double.MAX_VALUE;
				int posizioneMax = posizioneLesione;

				if (flagROIPETCT) {
					fettaCranioCaudale = 1;
					fettaCaudoCraniale = stackSize;
				}

				for (int fetta = fettaCranioCaudale; fetta <= fettaCaudoCraniale; fetta++) {
					double tmpMax;
					dicomImage.setSlice(fetta);

					if (flagContornamentoManuale) {
						do {
							NonBlockingGenericDialog manualContourDialog = new NonBlockingGenericDialog(
									"DD03 - Verify Contour");
							manualContourDialog.addMessage("Draw manual contour.");
							manualContourDialog.enableYesNoCancel("Next Slice", "Quit");
							manualContourDialog.setCancelLabel("End Contouring");
							manualContourDialog.showDialog();
							roiManager.runCommand(dicomImage, "Add");
							guscioEsterno = roiManager.getRoi(0);
							roiManager.reset();
							roiPerFetta.add(guscioEsterno);
							if (manualContourDialog.wasCanceled()) {
								fettaCaudoCraniale = fetta - 1;
								distanzaCaudoCraniale = fettaCaudoCraniale - posizioneLesione;
								break;
							} else if (!manualContourDialog.wasOKed())
								return;
						} while (guscioEsterno == null);
					} else if (flagROIPETCT) {
						int posizioneFetta = posizioniRoiPerFetta.indexOf(fetta);
						if (posizioneFetta >= 0) {
							guscioEsterno = roiPerFetta.get(posizioneFetta);
						}
					} else {
						dicomImage.setRoi(guscioEsterno);
					}

					ImageProcessor mask = guscioEsterno.getMask();
					Rectangle r = guscioEsterno.getBounds();
					tmpMax = calculateMax(r, ip, mask);

					if (roiMax < tmpMax) {
						posizioneMax = fetta;
						roiMax = tmpMax;
					}
				}

				if (flagPersonalizedMaximum) {
					IJ.setThreshold(personalizedMaximum * threshold, max, "red");
				}

				else {
					IJ.setThreshold(roiMax * threshold, max, "red");
				}

				double[] stat;
				int integrale = 0;
				int conteggio = 0;

				Overlay overlay = new Overlay();
				int[] totalHistogram = new int[0];
				for (int fetta = fettaCranioCaudale; fetta <= fettaCaudoCraniale; fetta++) {

					roiManager.reset();
					IJ.run("Select None");

					dicomImage.setSlice(fetta);
					IJ.run("Create Selection");
					if (!isSelectionEmpty()) {
						roiManager.runCommand(dicomImage, "Add");
						if (flagContornamentoManuale)
							guscioEsterno = roiPerFetta.get(fetta - fettaCranioCaudale);
						else if (flagROIPETCT) {
							int posizioneFetta = posizioniRoiPerFetta.indexOf(fetta);
							if (posizioneFetta >= 0)
								guscioEsterno = roiPerFetta.get(posizioneFetta);
							else
								guscioEsterno = null;
						}

						dicomImage.setRoi(guscioEsterno);
						// Utility.debugDeiPoveri("B001");

						if (!isSelectionEmpty()) {
							roiManager.runCommand(dicomImage, "Add");
							roiManager.runCommand(dicomImage, "AND");
							// Utility.debugDeiPoveri("B002");

							if (!isSelectionEmpty()) {
								roiManager.runCommand(dicomImage, "Add");
								Roi[] roisArray = roiManager.getRoisAsArray();
								Roi thresholdRoi = roisArray[roisArray.length - 1];
								thresholdRoi.setPosition(fetta);
								overlay.add(thresholdRoi, numeroLesione + "_" + posizioneLesione);
								ImageProcessor mask = thresholdRoi.getMask();
								Rectangle r = thresholdRoi.getBounds();
								stat = calculateStat(r, ip, mask);
								conteggio += (int) stat[0];
								integrale += (int) stat[1];
								// Utility.debugDeiPoveri("B003");

								int[] histogram = ip.getHistogram();
								if (totalHistogram.length <= 0)
									totalHistogram = histogram;
								else
									totalHistogram = sumHistograms(totalHistogram, histogram);
								if (fettaCranioCaudaleFinal == -1)
									fettaCranioCaudaleFinal = fetta;
								fettaCaudoCranialeFinal = fetta;
								// Utility.debugDeiPoveri("B004");

							}
						}
					}
				}

				Utility.debugDeiPoveri("Di seguito appare la ROI azzurra, magari va bene!");

				dicomImage.setSlice(posizioneMax);
				Roi roi1 = dicomImage.getRoi();
				dicomImage.setOverlay(overlay);

				Utility.debugDeiPoveri("Eccola qui sulla nostra immagine!");

				boolean memorize = false;
				boolean startAgain = false;
				boolean histogram = false;

				// =================================================================================
				// QUI ENTRA SOLO SE IN PRECEDENZA, sopra ABBIMO MESSO
				// prosegui = 70
				// =================================================================================
				if (prosegui == 70) {
					// StartAgain

					NonBlockingGenericDialog resultsDialog = new NonBlockingGenericDialog("DD04 - Results");
					resultsDialog.addMessage("Results", titleFont);
					resultsDialog.addMessage("Maximum lesion count: " + (int) roiMax);
					resultsDialog.addMessage("Contouring threshold level: " + threshold);
					resultsDialog.addMessage("Contouring count threshold level: " + (int) (roiMax * threshold));
					resultsDialog.addMessage("Processed Slices Position: " + posizioneMax + " ("
							+ fettaCranioCaudaleFinal + "-" + fettaCaudoCranialeFinal + ")");
					resultsDialog.addMessage("Pet-Ct Viewer slice: " + convertFromPETCTReference(posizioneMax) + " ("
							+ convertFromPETCTReference(fettaCranioCaudaleFinal) + "-"
							+ convertFromPETCTReference(fettaCaudoCranialeFinal) + ")");
					resultsDialog.addMessage("Pixel number over threshold: " + conteggio);
					resultsDialog.addMessage("Over threshold count integral: " + integrale);
					resultsDialog.addCheckbox("Select to reset image selection and threshold.", resetflag);
					resultsDialog.setCancelLabel("MemorizeResults");
					resultsDialog.enableYesNoCancel("Start Again", "Show Histogram");
					resultsDialog.showDialog();

					if (resultsDialog.wasCanceled()) {
						if (threshold == -1) {
							IJ.resetThreshold();
						}
						IJ.run("Select None");
						dicomImage.repaintWindow();
						memorize = true;
						prosegui = 3;
						IJ.log("MemorizeResults");

					} else if (resultsDialog.wasOKed()) {
						startAgain = true;
						resetflag = resultsDialog.getNextBoolean();
						if (threshold == -1) {
							IJ.resetThreshold();
						}
						IJ.run("Select None");
//						IJ.log("Differenza");
						dicomImage.repaintWindow();

						prosegui = 80;
						IJ.log("StartAgain");
					} else {
						if (threshold == -1) {
							IJ.resetThreshold();
						}
						IJ.run("Select None");
//						IJ.log("Differenza");
						dicomImage.repaintWindow();

						prosegui = 1;
						IJ.log("ShowHistogram");
					}
				}

				if (prosegui == 3) {

					// point1=0 sono 24h
					// point1=1 sono 48h
					// point1=2 sono 120h
					String aux3 = "";
					if (point1 == 0)
						aux3 = "#038#";
					if (point1 == 1)
						aux3 = "#048#";
					if (point1 == 2)
						aux3 = "#058#";
					// uso aux3 per leggere la durata appropriata 24/48/120
					double durata = Double.parseDouble(Utility.readFromLog(pathPermanente, aux3, ":"));
					double[] out2 = calcoliMIRD_primoMetodo(matTable, threshold, conteggio, integrale, durata);

					// Se il risultato viene accettato lo vado a scrivere nel file volatile.txt
					IJ.log("ESEGUO MemorizeResults con point1= " + point1);

					Utility.appendLog(pathVolatile, "---- DOSIMETRY-----");
					Utility.appendLog(pathVolatile, "#020#\tAcquisizione: " + sTime);
					Utility.appendLog(pathVolatile, "");

					String aux1 = "";
					aux1 = "#021#\tMaximum lesion count: " + (int) roiMax;
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#022#\tContouring threshold level: " + threshold;
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#023#\tContouring count threshold level: " + (int) (roiMax * threshold);
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#024#\tProcessed Slices Position: " + posizioneMax + " (" + fettaCranioCaudaleFinal + "-"
							+ fettaCaudoCranialeFinal + ")";
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#025#\tPet-Ct Viewer slice: " + convertFromPETCTReference(posizioneMax) + " ("
							+ convertFromPETCTReference(fettaCranioCaudaleFinal) + "-"
							+ convertFromPETCTReference(fettaCaudoCranialeFinal) + ")";
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#026#\tPixel number over threshold: " + conteggio;
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#027#\tOver threshold count integral: " + integrale;
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#028#\tMIRD volume: " + out2[0];
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#029#\tMIRD fatCal: " + out2[1];
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#030#\tMIRD activity: " + out2[2];
					Utility.appendLog(pathVolatile, aux1);

					Utility.appendLog(pathVolatile, "");
					Utility.endLog(pathVolatile);
					Utility.appendLog(pathVolatile, "--- OTHER INFOS ---");
					Utility.appendLog(pathVolatile, "");
					aux1 = "#001#\tPatient MachineName: " + DicomTools.getTag(dicomImage, "0010,0010");
					Utility.appendLog(pathVolatile, aux1);
					aux1 = DicomTools.getTag(dicomImage, "0010,0010");
					String[] vetName = aux1.split("\\^");
					String surname = "------";
					String name = "------";
					if (vetName.length == 2) {
						surname = vetName[0];
						name = vetName[1];
					}
					aux1 = "#002#\tPatient Surname: " + surname;
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#003#\tPatient Name: " + name;
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#004#\tPatient ID: " + DicomTools.getTag(dicomImage, "0010,0020");
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#005#\tPatient birth date: " + DicomTools.getTag(dicomImage, "0010,0030");
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#006#\tPatient weight: " + DicomTools.getTag(dicomImage, "0010,1030");
					Utility.appendLog(pathVolatile, aux1);
					aux1 = "#007#\tPatient sex: " + DicomTools.getTag(dicomImage, "0010,1040");
					Utility.appendLog(pathVolatile, aux1);
					Utility.appendLog(pathVolatile, "");
					Utility.endLog(pathVolatile);

					ArrayList<Integer> pixList = new ArrayList<Integer>();
					ArrayList<Integer> xPosList = new ArrayList<Integer>();
					ArrayList<Integer> yPosList = new ArrayList<Integer>();

					IJ.run("Select None");
					IJ.run("Remove Overlay");
					IJ.resetThreshold();

					if (point1 == 0) {
						ok24 = true;
					}
					if (point1 == 1) {
						ok48 = true;
					}
					if (point1 == 2) {
						ok120 = true;
					}
					if (ok24 && ok48 && ok120) {
						prosegui = 50; // fine lesione (NON FINE LAVORO, PIRLA!!!)
					} else {
						prosegui = 90; // RITORNO PER SCELTA IMMAGINE SUCCESSIVA
					}
				}
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				// =================================================================================
				// =================================================================================
				// =================================================================================
				// =================================================================================
				// =================================================================================

				if (prosegui == 2) {
					// Utility.debugDeiPoveri("histogram");
					boolean histogramProsegui = false;
					boolean cancelButtonFlag = true;
					boolean signedIntegerFlag = dicomImage.getCalibration().isSigned16Bit();
					boolean histDifferentialFlag = true;
					// boolean histabsoluteFlag = true;
					boolean histRebinFlag = true;

					double[] xHistData = new double[0];
					double[] yHistData = new double[0];

					while (!histogramProsegui) {
						String[] scelteBit = new String[] { "Signed", "Unsigned" };
						String[] scelteHistogram = new String[] { "Differential Histogram", "Cumulative Histogram" };
						// String[] scelteAbsoluteRelative = new String[] {"Absolute", "Relative"};
						String[] scelteBin = new String[] { "Auto Rebinning", "Raw Bin" };

						int radioGroupIndex = 0;

						NonBlockingGenericDialog histDialog = new NonBlockingGenericDialog("DD05 - Required Parameter");
						histDialog.addMessage("Histogram Paramters", titleFont);
						if (!signedIntegerFlag) {
							histDialog.addMessage("Image Bit Depth: " + ip.getBitDepth() + ".", textFont);
							histDialog.addRadioButtonGroup("Select encoding:", scelteBit, 1, 2,
									scelteBit[signedIntegerFlag ? 0 : 1]);
						}
						histDialog.addRadioButtonGroup("Select histogram type:", scelteHistogram, 1, 2,
								scelteHistogram[histDifferentialFlag ? 0 : 1]);
						histDialog.addRadioButtonGroup("Select bin number:", scelteBin, 1, 2,
								scelteBin[histRebinFlag ? 0 : 1]);
						histDialog.setCancelLabel("Save");
						if (cancelButtonFlag) {
							histDialog.hideCancelButton();
						}

						histDialog.enableYesNoCancel("Show Histogram", "Back");
						histDialog.setFont(defaultFont);
						histDialog.showDialog();

						Vector histRadioButtonsRaw = histDialog.getRadioButtonGroups();
						CheckboxGroup histRadioButtons;
						Checkbox histRadioButtonSelection;
						String histRadioButtonSelectionString;

						if (!signedIntegerFlag) {
							histRadioButtons = (CheckboxGroup) histRadioButtonsRaw.get(radioGroupIndex++);
							histRadioButtonSelection = histRadioButtons.getSelectedCheckbox();
							histRadioButtonSelectionString = histRadioButtonSelection.getLabel();
							if (histRadioButtonSelectionString == scelteBit[0])
								signedIntegerFlag = true;
							else
								signedIntegerFlag = false;
						}

						histRadioButtons = (CheckboxGroup) histRadioButtonsRaw.get(radioGroupIndex++);
						histRadioButtonSelection = histRadioButtons.getSelectedCheckbox();
						histRadioButtonSelectionString = histRadioButtonSelection.getLabel();
						if (histRadioButtonSelectionString == scelteHistogram[0])
							histDifferentialFlag = true;
						else
							histDifferentialFlag = false;

						histRadioButtons = (CheckboxGroup) histRadioButtonsRaw.get(radioGroupIndex++);
						histRadioButtonSelection = histRadioButtons.getSelectedCheckbox();
						histRadioButtonSelectionString = histRadioButtonSelection.getLabel();
						if (histRadioButtonSelectionString == scelteBin[0])
							histRebinFlag = true;
						else
							histRebinFlag = false;

						String yLabel = "";
						if (histRebinFlag)
							yLabel += "_autoRebinned";
						else
							yLabel += "_raw";
						if (histDifferentialFlag)
							yLabel += "_differential";
						else
							yLabel += "_cumulative";

						if (histDialog.wasOKed()) {
							double[][] histData = getTotalHistogram(totalHistogram, (int) (roiMax * threshold),
									(int) roiMax, signedIntegerFlag);
							xHistData = extractPointCoordinates(histData, 'x');
							yHistData = extractPointCoordinates(histData,
									(histDifferentialFlag || histRebinFlag) ? 'y' : 'z');

							if (histRebinFlag) {
								histData = rebinHistogram(xHistData, yHistData, conteggio);
								xHistData = extractPointCoordinates(histData, 'x');
								yHistData = extractPointCoordinates(histData, histDifferentialFlag ? 'y' : 'z');
							}

							Plot plot = new Plot("Histogram" + yLabel, "Counts", "Number of Pixels");
							plot.setColor(new Color(0x3333ee));
							plot.add("bar", xHistData, yHistData);
							plot.show();

							cancelButtonFlag = false;

						} else if (histDialog.wasCanceled()) {
							GenericDialog fileNameDialog = new GenericDialog("DD06 - Saving Process");
							fileNameDialog.addMessage("Insert file name without extension", defaultFont);
							fileNameDialog.addStringField("Save As: ", histogramFileName, 20);
							fileNameDialog.addMessage("The file will be saved in 'Downloads'.", defaultFont);
							fileNameDialog.setOKLabel("Save");
							fileNameDialog.setFont(defaultFont);

							fileNameDialog.showDialog();

							histogramFileName = fileNameDialog.getNextString();

							if (fileNameDialog.wasOKed())
								saveToFile(xHistData, yHistData, yLabel, IJ.getDirectory("downloads"),
										histogramFileName, signedIntegerFlag);
						} else
							histogramProsegui = true;
					}
					prosegui = 70;
				} // termine histogram esce con prosegui = 10 per tornare al menu

				// =================================================================================
				// =================================================================================
				// =================================================================================
				// =================================================================================
				// =================================================================================
				// VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV

				String lesionName = "";
				if (prosegui == 50) {
					GenericDialog compliments1 = new GenericDialog("DD07 - Compliments1");
					compliments1.addMessage("COMPLIMENTI, HAI COMPLETATO L'ANALISI DELLA LESIONE");
					compliments1.addMessage("SENZA SCLERARE TROPPO");
					compliments1.addStringField("NomeLesione per memorizzazione", "");
					compliments1.showDialog();
					lesionName = compliments1.getNextString();
					Utility.appendLog(pathPermanente, "---- IMPORTATO DA VOLATILE-----");
					Utility.appendLog(pathPermanente, "----- START LESIONE--------");
					Utility.appendLog(pathPermanente, "---" + lesionName + "---");
					Utility.appendLog(pathPermanente, "---------------------------");
					Utility.moveLog(pathPermanente, pathVolatile);
					Utility.appendLog(pathPermanente, "----- END LESIONE--------");
					File vola1 = new File(pathVolatile);
					vola1.delete();
					Utility.initLog(pathVolatile);
					ok24 = false;
					ok48 = false;
					ok120 = false;

					GenericDialog finished1 = new GenericDialog("DD08 - Finished1");
					finished1.addMessage("HAI TERMINATO ANALISI DISTRETTO?");
					finished1.addMessage("se rispondi ALTRA LESIONE vuoi analizzare un altra lesione");
					finished1.addMessage(
							"se rispondi FINITO vuoi passare in LoadPatient e caricare un altro distretto anatomico OPPURE HAI TERMINATO");
					finished1.setOKLabel("FINITO");
					finished1.setCancelLabel("ALTRA LESIONE");

					finished1.showDialog();
					boolean avanti = finished1.wasCanceled();
					boolean finito = finished1.wasOKed();
					if (avanti)
						prosegui = 95; // RITORNO RIPETIZIONE CON LESIONE SUCCESSIVA
					if (finito)
						prosegui = 0;
				}
			}
		} /// primo while in assoluto prosegui>0

	} // prosegui==2;

	// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// =================================================================================
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// =================================================================================
	// =================================================================================

	/**
	 * Determina se esiste una selezione
	 * 
	 * @return true se selectionType= -1
	 */
	boolean isSelectionEmpty() {
//		Macro macroRunner = new Macro();
		String output = Macro.eval("selectionType");
		String emptySelection = "-1";
		return emptySelection.equals(output);

	}

	/**
	 * Calcola alcuni valori statistici per i pixel della ROI attiva
	 * 
	 * @param r
	 * @param ip
	 * @param mask
	 * @return
	 */
	double[] calculateStat(Rectangle r, ImageProcessor ip, ImageProcessor mask) {
		double sum = 0;
		int count = 0;
		double[] stat = new double[3];
		for (int y = 0; y < r.height; y++) {
			for (int x = 0; x < r.width; x++) {
				if (mask == null || mask.getPixel(x, y) != 0) {
					count++;
					sum += ip.getPixelValue(x + r.x, y + r.y);
				}
			}
		}
		stat[0] = count;
		stat[1] = sum;
		stat[2] = sum / count;
		return stat;
	}

	/**
	 * Calcola somma valori pixel della ROI attiva
	 * 
	 * @param r
	 * @param ip
	 * @param mask
	 * @return
	 */
	double calculateIntegral(Rectangle r, ImageProcessor ip, ImageProcessor mask) {
		double sum = 0;
		for (int y = 0; y < r.height; y++) {
			for (int x = 0; x < r.width; x++) {
				if (mask == null || mask.getPixel(x, y) != 0) {
					sum += ip.getPixelValue(x + r.x, y + r.y);
				}
			}
		}
		return sum;
	}

	/**
	 * Calcola max valori pixel della ROI attiva
	 * 
	 * @param r
	 * @param ip
	 * @param mask
	 * @return
	 */
	double calculateMax(Rectangle r, ImageProcessor ip, ImageProcessor mask) {
		double max = -Double.MAX_VALUE;
		double v = -Double.MAX_VALUE;
		for (int y = 0; y < r.height; y++) {
			for (int x = 0; x < r.width; x++) {
				if (mask == null || mask.getPixel(x, y) != 0) {
					v = ip.getPixelValue(x + r.x, y + r.y);
					if (v > max)
						max = v;
				}
			}
		}
		return max;
	}

	/**
	 * Calcola max pixel della immagine / stack
	 * 
	 * @param imp
	 * @return
	 */
	public float[] imageMaximum(ImagePlus imp) {
		int indexMax = 0, zMax = 0;
		float max = -Float.MAX_VALUE;
		float min = Float.MAX_VALUE;
		ImageStack stack = imp.getStack();
		int width = imp.getWidth();
		int height = imp.getHeight();
		int n = width * height;
		int images = imp.getStackSize();
		for (int img = 1; img <= images; img++) {
			ImageProcessor ip = stack.getProcessor(img);
			for (int i = 0; i < n; i++) {
				float v = ip.getf(i);
				if (v > max) {
					max = v;
					indexMax = i;
					zMax = img - 1;
				}
			}
		}
		int xMax = indexMax % width;
		int yMax = indexMax / width;

		float[] pos = new float[4];
		pos[0] = xMax;
		pos[1] = yMax;
		pos[2] = zMax;
		pos[3] = max;
		return pos;
	}

	/**
	 * 
	 * @param fileLine
	 * @return
	 */
	private float[][] pointsEstractionFromCSV(String fileLine) {
		float[][] points = null;
		int pointsIndex = 0;
		float zPoints = 0;
		List<String> elementsList = Arrays.asList(fileLine.split(","));
		int pointsPositionInList = elementsList.size();
		for (int listIndex = 0; listIndex < elementsList.size(); listIndex++) {
			String singleElement = elementsList.get(listIndex).trim();

			if (singleElement.contains("num points")) {
				String[] numberOfPointsArray = singleElement.split(" ");
				String numberOfPointsString = numberOfPointsArray[numberOfPointsArray.length - 1];
				int numberOfPoints = Integer.parseInt(numberOfPointsString);
				pointsPositionInList = listIndex;

				int fettaInizio = Integer.parseInt(elementsList.get(listIndex - 2).trim());
				int fettaFine = Integer.parseInt(elementsList.get(listIndex - 1).trim());
				int zPointsPETCT = (fettaFine - fettaInizio) / 2 + fettaInizio;
				zPoints = convertFromPETCTReference(zPointsPETCT);

				points = new float[numberOfPoints + 1][3];
				points[pointsIndex][0] = numberOfPoints;
				points[pointsIndex][1] = numberOfPoints;
				points[pointsIndex][2] = zPoints;
				pointsIndex++;
			}

			if (listIndex > pointsPositionInList) {
				String[] coordinates = singleElement.split(" ");
				points[pointsIndex][0] = (float) Integer.parseInt(coordinates[0]);
				points[pointsIndex][1] = (float) Integer.parseInt(coordinates[coordinates.length - 1]);
				points[pointsIndex][2] = zPoints;

				pointsIndex++;
			}
		}
		return points;
	}

	/**
	 * 
	 * @param points
	 * @param coordinate
	 * @return
	 */
	private float[] extractPointCoordinates(float[][] points, char coordinate) {
		float[] wPoints = null;
		int coord = -1;
		int pointsIndex = 0;
		int pointsSize = 0;

		switch (coordinate) {
		case 'x':
		case 'X':
			coord = 0;
			break;

		case 'y':
		case 'Y':
			coord = 1;
			break;

		case 'z':
		case 'Z':
			coord = 2;
			break;

		default:
			coord = coordinate;
		}

		pointsSize = (int) points[pointsIndex][coord];
		pointsIndex++;
		wPoints = new float[pointsSize];

		while (pointsIndex <= pointsSize) {
			wPoints[pointsIndex - 1] = points[pointsIndex][coord];
			pointsIndex++;
		}

		return wPoints;
	}

	/**
	 * 
	 * @param points
	 * @param coordinate
	 * @return
	 */
	private double[] extractPointCoordinates(double[][] points, char coordinate) {
		double[] wPoints = null;
		int coord = -1;
		int pointsIndex = 0;
		int pointsSize = 0;

		switch (coordinate) {
		case 'x':
		case 'X':
			coord = 0;
			break;

		case 'y':
		case 'Y':
			coord = 1;
			break;

		case 'z':
		case 'Z':
			coord = 2;
			break;
		}

		pointsSize = (int) points[pointsIndex][coord];
		pointsIndex++;
		wPoints = new double[pointsSize];

		while (pointsIndex <= pointsSize) {
			wPoints[pointsIndex - 1] = points[pointsIndex][coord];
			pointsIndex++;
		}

		return wPoints;
	}

	/**
	 * 
	 * @param inputPosition
	 * @return
	 */
	private int convertFromPETCTReference(int inputPosition) {
		this.stackSize = stackSize;
		return stackSize - inputPosition + 1;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param ylabel
	 * @param path
	 * @param fileName
	 * @param signedIntegerFlag
	 */
	private void saveToFile(double[] x, double[] y, String ylabel, String path, String fileName,
			boolean signedIntegerFlag) {
		try {
			String filePath = (path + Prefs.getFileSeparator() + fileName + ".csv");
			File tmpFile = new File(filePath);
			boolean exists = tmpFile.exists();

			if (exists) {
				GenericDialog replaceFileDialog = new GenericDialog("DD09 - File already exists");
				replaceFileDialog.addMessage("File already exists. Do you wish to replace it?");
				replaceFileDialog.enableYesNoCancel();
				replaceFileDialog.showDialog();

				if (replaceFileDialog.wasOKed())
					tmpFile.delete();
				else if (!replaceFileDialog.wasCanceled()) {
					int copyIndex = 0;
					while (tmpFile.exists()) {
						copyIndex++;
						filePath = (path + Prefs.getFileSeparator() + fileName + "_" + copyIndex + ".csv");
						tmpFile = new File(filePath);
					}
				} else
					return;
			}

			FileWriter fileW = new FileWriter(filePath, true);
			fileW.write("counts_value;number_of_pixels" + ylabel + "\n");
			int counterIndex = 1;
			int cumulativo = 0;
			for (int index = 0; index < x.length; index++)
				fileW.write(x[index] + ";" + y[index] + "\n");
			fileW.close();
		} catch (IOException e) {
			IJ.error("Error", "Ops, something went wrong.\nPlease try again.");
		}
	}

	/**
	 * 
	 * @param data
	 * @param soglia
	 * @param max
	 * @param signedIntegerFlag
	 * @return
	 */
	private double[][] getTotalHistogram(int[] data, int soglia, int max, boolean signedIntegerFlag) {
		int startingPixelValue = signedIntegerFlag ? -data.length / 2 : 0;
		double[][] outputHist = new double[max - soglia + 2][3];
		int min = Math.max(0, soglia);
		outputHist[0][0] = max - min + 1;
		outputHist[0][1] = max - min + 1;
		outputHist[0][2] = max - min + 1;
		int counterIndex = 1;
		double cumulativo = 0;
		for (int index = 0; index < data.length; index++) {
			int conteggioAscissa = startingPixelValue + index;
			if (conteggioAscissa >= min && conteggioAscissa <= max) {
				cumulativo += (double) data[index];
				outputHist[counterIndex][0] = (double) startingPixelValue + index;
				outputHist[counterIndex][1] = (double) data[index];
				outputHist[counterIndex][2] = cumulativo;
				counterIndex++;
			}
		}
		return outputHist;
	}

	/**
	 * 
	 * @param hist1
	 * @param hist2
	 * @return
	 */
	private int[] sumHistograms(int[] hist1, int[] hist2) {
		int[] hist3 = new int[hist1.length];
		for (int index = 0; index < hist1.length; index++)
			hist3[index] = hist1[index] + hist2[index];
		return hist3;
	}

	/**
	 * 
	 * @param xHistData
	 * @param yHistData
	 * @param conteggio
	 * @return
	 */
	private double[][] rebinHistogram(double[] xHistData, double[] yHistData, int conteggio) {
		int numberOfBin = (int) Math.round(Math.sqrt(conteggio));
		double binWidth = Math.floor((xHistData[xHistData.length - 1] - xHistData[0]) / numberOfBin);
		numberOfBin += 1;
		double[][] histData = new double[numberOfBin + 1][3];
		double startPoint = xHistData[0] >= 0 ? xHistData[0] : 0;

		histData[0][0] = numberOfBin;
		histData[0][1] = numberOfBin;
		histData[0][2] = numberOfBin;

		int cumulativo = 0;
		for (int i = 1; i < numberOfBin + 1; i++) {
			histData[i][0] = startPoint + binWidth * (i - 1);
			int tmpSum = 0;
			for (int j = 0; j < xHistData.length; j++) {
				if (xHistData[j] < (histData[i][0] + binWidth / 2) && xHistData[j] >= (histData[i][0] - binWidth / 2)) {
					tmpSum += yHistData[j];
				}
			}
			cumulativo += tmpSum;
			histData[i][1] = tmpSum;
			histData[i][2] = cumulativo;
		}
		return histData;
	}

//	/**
//	 * Scrive una riga nel log
//	 * 
//	 * @param path
//	 * @param linea
//	 */
//	public static void appendLog(String path, String linea) {
//
//		BufferedWriter out;
//		try {
//			out = new BufferedWriter(new FileWriter(path, true));
//			out.write(linea);
//			out.newLine();
//			out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	/**
//	 * Legge il log e mette in un vettore tutte le strinhe
//	 * 
//	 * @param path
//	 */
//	public static String[] readLog(String path) {
//
//		ArrayList<String> inArrayList = new ArrayList<String>();
//
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(new FileReader(path));
//			while (br.ready()) {
//				String line = br.readLine();
//				inArrayList.add(line);
//			}
//			br.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Object[] objArr = inArrayList.toArray();
//		String[] outStrArr = new String[objArr.length];
//		for (int i1 = 0; i1 < objArr.length; i1++) {
//			outStrArr[i1] = objArr[i1].toString();
//		}
//		return outStrArr;
//	}
//
//	/**
//	 * Inizializza il file di log
//	 * 
//	 * @param path
//	 */
//	public static void initLog(String path) {
//		File f1 = new File(path);
//		if (f1.exists()) {
//			f1.delete();
//		}
//		appendLog(path, "---- INIZIO ---------");
//	}
//
//	/**
//	 * Scrive FINE nel log
//	 * 
//	 * @param path
//	 */
//	public static void endLog(String path) {
//		appendLog(path, "---- FINE ---------");
//	}
//
//	/**
//	 * Cancellazione del file, attenzione devono essere prima chiusi BufferedReader
//	 * e BufferedWriter
//	 * 
//	 * @param path
//	 */
//	public void deleteLog(String path) {
//		File f1 = new File(path);
//		if (f1.exists()) {
//			f1.delete();
//		}
//		if (f1.exists()) {
//			debugDeiPoveri("NON RIESCO A CANCELLARE " + path);
//		}
//	}
//
//	/**
//	 * Copia i dati dal log volatile.txt al log permanente.txt
//	 * 
//	 * @param permFile
//	 * @param tmpFile
//	 */
//	public void moveLog(String permFile, String tmpFile) {
//		BufferedWriter out;
//		BufferedReader in;
//		String str = "";
//		try {
//			out = new BufferedWriter(new FileWriter(permFile, true));
//			in = new BufferedReader(new FileReader(tmpFile));
//			out.newLine();
//			while ((str = in.readLine()) != null) {
//				out.write(str);
//				out.newLine();
//			}
//			out.close();
//			in.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		deleteLog(tmpFile);
//
//	}
//
//	/**
//	 * Siamo dei poveracci!!
//	 * 
//	 * @param paramString
//	 */
//	private void debugDeiPoveri(String text) {
//		WaitForUserDialog wait = new WaitForUserDialog("Debug", text);
//		wait.show();
//	}

	/**
	 * Apertura di una immagine dal path
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
	 * Legge un intero da una stringa
	 * 
	 * @param tmp1
	 * @return
	 */
	static int parseInt(String tmp1) {
		int ret1 = 0;
		double dbl1;
		if (tmp1 != null && !tmp1.isEmpty()) {
			try {
				dbl1 = Double.parseDouble(tmp1);
				ret1 = (int) dbl1;
			} catch (Exception e) {
				ret1 = 0;
			}
		}
		return ret1;
	}

	/**
	 * 
	 * @param ok24
	 * @param ok48
	 * @param ok120
	 * @param defaultFont
	 * @return
	 */
	ImagePlus sceltaAutomaticaImmagine(boolean ok24, boolean ok48, boolean ok120, Font defaultFont) {

		boolean[] choice = new boolean[3];
		String default1 = "";
		Color color24 = Color.red;
		Color color48 = Color.red;
		Color color120 = Color.red;
		if (ok24) {
			color24 = Color.green;
			choice[0] = true;
		}
		if (ok48) {
			color48 = Color.green;
			choice[1] = true;
		}
		if (ok120) {
			color120 = Color.green;
			choice[2] = true;
		}

		String[] lista1 = WindowManager.getImageTitles();
		//
		// cerco di gestire la scelta di default in modo che presenti la prossima
		// immagine "rossa" non ancora elaborata, in questo modo l'operatore può
		// limitarsi ad accettare l'immagine proposta
		//

		for (int i1 = 0; i1 < 3; i1++) {
			if (!choice[i1]) {
				default1 = lista1[i1];
				break;
			}
		}

//		if (!choice[0]) {
//			default1 = lista1[0];
//		} else if (choice[0] && !choice[1]) {
//			default1 = lista1[1];
//		} else {
//			default1 = lista1[2];
//		}

		NonBlockingGenericDialog scelta1 = new NonBlockingGenericDialog("DD10 - Immagine da analizzare");
		scelta1.addImageChoice("scelta immagine", default1);
		scelta1.addMessage(lista1[0], defaultFont, color24);
		scelta1.addMessage(lista1[1], defaultFont, color48);
		scelta1.addMessage(lista1[2], defaultFont, color120);
		scelta1.showDialog();
		ImagePlus dicomImage = scelta1.getNextImage();
		return dicomImage;

	}

	/**
	 * 
	 * @param titleFont
	 * @param defaultFont
	 * @return
	 */
	ImagePlus sceltaManualeImmagine(Font titleFont, Font defaultFont) {

		OpenDialog openDial = new OpenDialog("Browse to open a DICOM image", "TITLE");
//		String directory = openDial.getDirectory();
		String path1 = openDial.getPath();
		if (path1 == null) {
			return null;
		}
		Opener opener = new Opener();
		ImagePlus imp1 = opener.openImage(path1);
		if (imp1 == null) {
			return null;
		}
		return imp1;

	}

	/**
	 * Analizza i pixel appartenenti alla ROI attiva sull'immagine (va bene anche
	 * per ROI "sparnegate". Potremo poi fare i nostri calcoli sui pixel elencati
	 * nell arrayList pixList1, di coordinate xPosList1 e yPosList1
	 * 
	 * @param imp1      immagine da analizzare
	 * @param pixList1  lista dei pixelx (output)
	 * @param xPosList1 coordinata x dei pixels nell'immagine (output)
	 * @param yPosList1 coordinata y dei pixels nell'immagine (output)
	 */
	public static void pixelAnalyzer(ImagePlus imp1, ArrayList<Integer> pixList1, ArrayList<Integer> xPosList1,
			ArrayList<Integer> yPosList1) {

		// Utility.debugDeiPoveri("A001");
		Roi roi1 = imp1.getRoi();
		Overlay ov1 = imp1.getOverlay();
		ImageProcessor ip1 = imp1.getProcessor();
		ImageProcessor mask1 = roi1 != null ? roi1.getMask() : null;
		Rectangle r1 = roi1 != null ? roi1.getBounds() : new Rectangle(0, 0, ip1.getWidth(), ip1.getHeight());
		for (int y1 = 0; y1 < r1.height; y1++) {
			for (int x1 = 0; x1 < r1.width; x1++) {
				if (mask1 == null || mask1.getPixel(x1, y1) != 0) {
					pixList1.add((int) ip1.getPixelValue(x1 + r1.x, y1 + r1.y));
					xPosList1.add(x1 + r1.x);
					yPosList1.add(y1 + r1.y);
					setOverlayPixel(ov1, imp1, x1 + r1.x, y1 + r1.y, Color.blue, Color.green, false);
				}
			}
		}
//		Utility.debugDeiPoveri("A002");
	}

	/**
	 * Serve a stabilire se sto facendo una minchiata, contorna i pixel
	 * dell'overlay, con pazienza, uno ad uno, in modo da mostrare se i pixel su cui
	 * faccio i calcoli sono tutti i pixel giusti
	 * 
	 * @param over1 overlay in uso per l'immagine
	 * @param imp1  immagine
	 * @param x1    coordinata x pixel
	 * @param y1    coordinata y pixel
	 * @param col1  colore 1
	 * @param col2  colore 2
	 * @param ok    scelta colore
	 */
	public static void setOverlayPixel(Overlay over1, ImagePlus imp1, int x1, int y1, Color col1, Color col2,
			boolean ok) {
		imp1.setRoi(x1, y1, 1, 1);
		if (ok) {
			imp1.getRoi().setStrokeColor(col1);
//			imp1.getRoi().setFillColor(col1);
		} else {
			imp1.getRoi().setStrokeColor(col2);
//			imp1.getRoi().setFillColor(col2);
		}
		over1.addElement(imp1.getRoi());
		imp1.deleteRoi();
	}

	/**
	 * Copiato da pixelAnalyzer E PURE MALE!!!
	 * 
	 * @param imp1
	 * @param pixList1
	 * @param xPosList1
	 * @param yPosList1
	 */
	public static void pixelAnalyzer_MIRD_primoMetodo(ImagePlus imp1, double[][] matTable, ArrayList<Integer> pixList1,
			ArrayList<Integer> xPosList1, ArrayList<Integer> yPosList1) {

		IJ.log("sono in MIRD per " + imp1.getTitle());
		Roi roi1 = imp1.getRoi();
//		Overlay ov1 = imp1.getOverlay();
		ImageProcessor ip1 = imp1.getProcessor();
		ImageProcessor mask1 = roi1 != null ? roi1.getMask() : null;

		// long integ1 = 0;
		Rectangle r1 = roi1 != null ? roi1.getBounds() : new Rectangle(0, 0, ip1.getWidth(), ip1.getHeight());
		for (int y1 = 0; y1 < r1.height; y1++) {
			for (int x1 = 0; x1 < r1.width; x1++) {
				// if (mask1 == null || mask1.getPixel(x1, y1) != 0) {
				if (mask1 == null || mask1.getPixel(x1, y1) != 0) {
					pixList1.add((int) ip1.getPixelValue(x1 + r1.x, y1 + r1.y));
					xPosList1.add(x1 + r1.x);
					yPosList1.add(y1 + r1.y);
					// integ1 = integ1 + (long) ip1.getPixelValue(x1 + r1.x, y1 + r1.y);
				}
			}
		}

		String roiMax1 = Utility.readFromLog(pathVolatile, "#021#", ":");
		String threshold1 = Utility.readFromLog(pathVolatile, "#022#", ":");
		String processedSlices = Utility.readFromLog(pathVolatile, "#024#", ":");
		String petCtViewerSlice1 = Utility.readFromLog(pathVolatile, "#025#", ":");
		String conteggio1 = Utility.readFromLog(pathVolatile, "#026#", ":");
		String integrale1 = Utility.readFromLog(pathVolatile, "#027#", ":");

		double pixOver = Double.parseDouble(conteggio1);
		double vol = pixOver * (Math.pow(4.43, 3) / 1000.);
		double threshold = Double.parseDouble(threshold1);

		IJ.log("pixOver= " + pixOver + " vol= " + vol);

		double t1;
		double a1;
		double b1;
		double c1;
		if (threshold <= 0.30) {
			t1 = matTable[0][0];
			a1 = matTable[1][0];
			b1 = matTable[2][0];
			c1 = matTable[3][0];
		} else if (threshold > 0.30 && threshold <= 0.50) {
			t1 = matTable[0][1];
			a1 = matTable[1][1];
			b1 = matTable[2][1];
			c1 = matTable[3][1];
		} else {
			t1 = matTable[0][2];
			a1 = matTable[1][2];
			b1 = matTable[2][2];
			c1 = matTable[3][2];
		}

		double fatCal = a1 * Math.pow(b1, vol) * Math.pow(vol, c1);

		IJ.log("image= " + imp1.getTitle() + " threshold= " + threshold + " t1= " + t1 + " a1= " + a1 + " b1= " + b1
				+ " c1= " + c1);
		Utility.debugDeiPoveri("MIRD");

	}

	/**
	 * Copiato da pixelAnalyzer E PURE MALE!!!
	 * 
	 * @param imp1
	 * @param pixList1
	 * @param xPosList1
	 * @param yPosList1
	 */
	public static double[] calcoliMIRD_primoMetodo(double[][] matTable, double threshold, double conteggio,
			double integrale, double durata) {

		double t1;
		double a1;
		double b1;
		double c1;
		if (threshold <= 0.30) {
			t1 = matTable[0][0];
			a1 = matTable[1][0];
			b1 = matTable[2][0];
			c1 = matTable[3][0];
		} else if (threshold > 0.30 && threshold <= 0.50) {
			t1 = matTable[0][1];
			a1 = matTable[1][1];
			b1 = matTable[2][1];
			c1 = matTable[3][1];
		} else {
			t1 = matTable[0][2];
			a1 = matTable[1][2];
			b1 = matTable[2][2];
			c1 = matTable[3][2];
		}

		double vol = conteggio * (Math.pow(4.43, 3) / 1000.);
		double fatCal = a1 * Math.pow(b1, vol) * Math.pow(vol, c1);
		double attiv = conteggio / (durata * fatCal);
		double[] out1 = new double[3];
		out1[0] = vol;
		out1[1] = fatCal;
		out1[2] = attiv;

		Utility.debugDeiPoveri("MIRD");
		return out1;

	}

}