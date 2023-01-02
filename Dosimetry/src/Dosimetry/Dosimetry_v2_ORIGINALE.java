package Dosimetry;

import ij.WindowManager;
import ij.ImagePlus;
import ij.ImageStack;
import ij.IJ;
import ij.Macro;
import ij.Undo;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.WaitForUserDialog;
import ij.gui.ImageWindow;
import ij.gui.Plot;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.io.FileInfo;
import ij.measure.ResultsTable;
import ij.plugin.CanvasResizer;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.Prefs;
import ij.process.ImageProcessor;
import ij.util.FontUtil;

import java.awt.Font;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Scanner;

public class Dosimetry_v2_ORIGINALE implements PlugIn {

	private ImagePlus dicomImage = null;
	private String imagePath = null;
	private int stackSize = 1;
	private boolean referenceRadioButtonIndex = false;

	public void run(String arg) {

		FontUtil fu = new FontUtil();
		String fontStyle = "Arial";
		Font titleFont = fu.getFont(fontStyle, 1, 18);
		Font textFont = fu.getFont(fontStyle, 2, 16);
		Font defaultFont = fu.getFont(fontStyle, 0, 14);

		ImagePlus dicomImage = WindowManager.getCurrentImage();
		this.dicomImage = dicomImage;

		while (dicomImage == null) {
			GenericDialog importImage = new GenericDialog("Image import");
			importImage.addMessage("Image import", titleFont);
			importImage.addMessage("Browse to open a DICOM image", defaultFont);
			importImage.setCancelLabel("Quit");
			importImage.setOKLabel("Browse");
			importImage.showDialog();

			if (importImage.wasCanceled())
				return;
			dicomImage = IJ.openImage(null);
		}

		dicomImage.show();

		ImageStack stack = dicomImage.getStack();
		stackSize = stack.getSize();
		Undo.reset();

		Dimension screen = IJ.getScreenSize();
		ImageWindow window = dicomImage.getWindow();
		window.setLocationAndSize(0, 0, (int) (((double) screen.height) / 2), (int) (((double) screen.height) / 2));

		float[] pos = imageMaximum(dicomImage);
		int xMax = (int) pos[0], yMax = (int) pos[1], zMax = (int) pos[2];
		float max = pos[3];

		int posizioneInput = 0;
		int numeroLesione = 0;
		int fettaCranioCaudale = 0;
		int fettaCaudoCraniale = 0;
		int distanzaCranioCaudale = 4;
		int distanzaCaudoCraniale = 4;
		int personalizedMaximum = (int) max;
		int radioButtonIndex = 0;
		int posizioneLesione = -1;
		double threshold = 0.4;
		boolean flagPersonalizedMaximum = false;
		boolean resetflag = true;
		ImageProcessor ip = dicomImage.getProcessor();
		List<Roi> roiPerFetta = null;
		List<Integer> posizioniRoiPerFetta = null;
		RoiManager roiManager = new RoiManager(false);
		Roi guscioEsterno = null;
		String histogramFileName = "histogram";
		do {

			int fettaCranioCaudaleFinal = -1;
			int fettaCaudoCranialeFinal = -1;

			roiManager.reset();

			boolean flagContornamentoManuale = false;
			boolean flagROIPETCT = false;

			if (resetflag) {
				IJ.run("Set Scale...", "distance=0 known=0 pixel=1");
				IJ.run("Select None");
				IJ.run("Remove Overlay");
				IJ.resetThreshold();
			} else {
				dicomImage.setRoi(guscioEsterno);
			}

			IJ.setTool("oval");

			String[] scelteReferenceSystem = new String[] { "ImageJ Reference System",
					"Pet-Ct Viewer Reference System" };

			do {
				NonBlockingGenericDialog dialogSlice = new NonBlockingGenericDialog("Required Parameters");
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

			NonBlockingGenericDialog dialog = new NonBlockingGenericDialog("Parameter Selection");
			dialog.addMessage("ROI definition and parameter selection", titleFont);
			dialog.addMessage("Fill the required information and draw a reference ROI around the lesion.", textFont);
			dialog.addNumericField("Select threshold for segmentation (use -1 for imported ROI):", threshold, 2);
			dialog.addMessage("Select the Range of Slices for ROI drawing and calculation. (center slice "
					+ posizioneLesione + ").\n", textFont);
			dialog.addNumericField("First slice in Cranio-Caudal direction", fettaCranioCaudale, 0);
			dialog.addNumericField("Last slice in Caudo-Cranial direction", fettaCaudoCraniale, 0);

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
				return;
			}

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
				break;
			case 1:
				roiPerFetta = new ArrayList<Roi>();
				posizioniRoiPerFetta = new ArrayList<Integer>();
				flagROIPETCT = true;
				break;
			case 2:
				roiPerFetta = new ArrayList<Roi>();
				flagContornamentoManuale = true;
				break;
			}

			if (!isSelectionEmpty() && !flagContornamentoManuale || flagContornamentoManuale || flagROIPETCT) {
				if (!flagROIPETCT && !flagContornamentoManuale) {
					guscioEsterno = roiManager.getRoi(0);
					roiManager.reset();
					dicomImage.setRoi(guscioEsterno);
				} else if (flagROIPETCT) {
					GenericDialog importRoiDialog = new GenericDialog("Import ROI from Pet-Ct Viewer");
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
									"Verify Contour");
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
						if (posizioneFetta >= 0)
							guscioEsterno = roiPerFetta.get(posizioneFetta);
					} else
						dicomImage.setRoi(guscioEsterno);

					ImageProcessor mask = guscioEsterno.getMask();
					Rectangle r = guscioEsterno.getBounds();
					tmpMax = calculateMax(r, ip, mask);

					if (roiMax < tmpMax) {
						posizioneMax = fetta;
						roiMax = tmpMax;
					}
				}

				if (flagPersonalizedMaximum)
					IJ.setThreshold(personalizedMaximum * threshold, max, "red");
				else
					IJ.setThreshold(roiMax * threshold, max, "red");

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

						if (!isSelectionEmpty()) {

							roiManager.runCommand(dicomImage, "Add");
							roiManager.runCommand(dicomImage, "AND");

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

								int[] histogram = ip.getHistogram();
								if (totalHistogram.length <= 0)
									totalHistogram = histogram;
								else
									totalHistogram = sumHistograms(totalHistogram, histogram);
								if (fettaCranioCaudaleFinal == -1)
									fettaCranioCaudaleFinal = fetta;
								fettaCaudoCranialeFinal = fetta;
							}
						}
					}
				}

				dicomImage.setSlice(posizioneMax);
				dicomImage.setOverlay(overlay);
				if (threshold == -1)
					IJ.resetThreshold();
				IJ.run("Select None");

				boolean prosegui = false;
				while (!prosegui) {
					NonBlockingGenericDialog resultsDialog = new NonBlockingGenericDialog("Results");
					resultsDialog.addMessage("Results", titleFont);
					resultsDialog.addMessage("Maximum lesion count: " + (int) roiMax);
					resultsDialog.addMessage("Contouring threshold level: " + threshold);
					resultsDialog.addMessage("Contouring count threshold level: " + (int) (roiMax * threshold));
					resultsDialog.addMessage("Processed Slices Position: " + posizioneMax + " ("
							+ fettaCranioCaudaleFinal + "-" + fettaCaudoCranialeFinal + ")");
					resultsDialog.addMessage("Pet-Ct Viewer slice: " + convertFromPETCTReference(posizioneMax) + " ("
							+ convertFromPETCTReference(fettaCranioCaudaleFinal) + "-"
							+ convertFromPETCTReference(fettaCaudoCranialeFinal) + ")");
					;
					resultsDialog.addMessage("Pixel number over threshold: " + conteggio);
					resultsDialog.addMessage("Over threshold count integral: " + integrale);
					resultsDialog.addCheckbox("Select to reset image selection and threshold.", resetflag);
					resultsDialog.setCancelLabel("Quit");
					resultsDialog.enableYesNoCancel("Start Again", "Show Histogram");
					resultsDialog.showDialog();

					if (resultsDialog.wasCanceled()) {
						IJ.run("Select None");
						IJ.run("Remove Overlay");
						IJ.resetThreshold();
						return;
					} else if (!resultsDialog.wasOKed()) {

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
							String[] scelteHistogram = new String[] { "Differential Histogram",
									"Cumulative Histogram" };
							// String[] scelteAbsoluteRelative = new String[] {"Absolute", "Relative"};
							String[] scelteBin = new String[] { "Auto Rebinning", "Raw Bin" };

							int radioGroupIndex = 0;

							NonBlockingGenericDialog histDialog = new NonBlockingGenericDialog("Required Parameter");
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
								GenericDialog fileNameDialog = new GenericDialog("Saving Process");
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
					} else
						prosegui = true;
					resetflag = resultsDialog.getNextBoolean();
				}
			}
		} while (true);
	}

	boolean isSelectionEmpty() {
		Macro macroRunner = new Macro();
		String output = macroRunner.eval("selectionType");
		String emptySelection = "-1";
		return emptySelection.equals(output);

	}

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

	private int convertFromPETCTReference(int inputPosition) {
		this.stackSize = stackSize;
		return stackSize - inputPosition + 1;
	}

	private void saveToFile(double[] x, double[] y, String ylabel, String path, String fileName,
			boolean signedIntegerFlag) {
		try {
			String filePath = (path + Prefs.getFileSeparator() + fileName + ".csv");
			File tmpFile = new File(filePath);
			boolean exists = tmpFile.exists();

			if (exists) {
				GenericDialog replaceFileDialog = new GenericDialog("File already exists");
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

	private int[] sumHistograms(int[] hist1, int[] hist2) {
		int[] hist3 = new int[hist1.length];
		for (int index = 0; index < hist1.length; index++)
			hist3[index] = hist1[index] + hist2[index];
		return hist3;
	}

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

	private void debugDeiPoveri(String text) {
		WaitForUserDialog wait = new WaitForUserDialog("Debug", text);
		wait.show();
	}
}