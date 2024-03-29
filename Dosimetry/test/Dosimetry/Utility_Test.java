package Dosimetry;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import flanagan.analysis.Regression;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.io.Opener;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;

public class Utility_Test {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}

	@Test
	public final void test_FM02_DialogAltreLesioni() {

		MyDialog.dialogAltreLesioni_FM02();
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_FM01_DialogFileSelection() {

		String message = "Select a fileO";
		String defaultDirectory = "\testdata";
		MyDialog.dialogFileSelection_FM01(message, defaultDirectory);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP06_DialogErrorMessage() {

		String paramString = "Messaggio di errore";
		MyDialog.dialogErrorMessage_LP06(paramString);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP09_DialogErrorMessageWithCancel() {

		String paramString = "Messaggio di errore";
		MyDialog.dialogErrorMessageWithCancel_LP09(paramString);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP27_BattezzaLesioni() {

		String pathVolatile = "Messaggio di errore";
		MyDialog.dialogBattezzaLesioni_LP27(pathVolatile);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_DD08_AltroDistretto() {

		MyDialog.dialogAltroDistretto_DD08();
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_copyInfo() {

		String pathSorgente = "testdata/sorgente.txt";
		String pathDestinazione = "testdata/destinazione.txt";
		int start = 10;
		int end = 15;

		MyLog.logCopyRange(pathSorgente, pathDestinazione, start, end);

		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_logModifyEsistente() {

		String path1 = "testdata/permanente2.txt";
		String path2 = "testdata/permanente.txt";
		File file1 = new File(path1);
		File file2 = new File(path2);

		new File(path2).delete();
		try {
			Files.copy(file1.toPath(), file2.toPath());
		} catch (IOException e) {
			MyLog.log("erroreCopia permanente2 > permanente");
			e.printStackTrace();
		}
		String pathPermanente = "testdata/permanente.txt";
		MyLog.logModify(pathPermanente, "#030#", "#030# MODIFICATO");
		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_logModifyMancante() {

		String path1 = "testdata/permanente2.txt";
		String path2 = "testdata/permanente.txt";
		File file1 = new File(path1);
		File file2 = new File(path2);

		new File(path2).delete();
		try {
			Files.copy(file1.toPath(), file2.toPath());
		} catch (IOException e) {
			MyLog.log("erroreCopia permanente2 > permanente");
			e.printStackTrace();
		}
		String pathPermanente = "testdata/permanente.txt";
		MyLog.logModify(pathPermanente, "#730#", "#730# MODIFICATO (AGGIUNTO)");
		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_MIRD_curveFitter() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 2.345, 4.897, 9.456 };

		Utility.MIRD_curveFitterImageJ(vetX, vetY);

		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_specialMIRD_curveFitterImageJ() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };
		boolean[] selected = { true, false, true };
		boolean[] vetBool = null;

		CurveFitter cf = Utility.MIRD_curveFitterSpecialImageJ(vetX, vetY);
		MyPlot.PL04_MIRD_curvePlotterSpecialImageJ(cf, vetX, vetY, selected, "STRING");

		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_specialMIRD_curveFitterCOMBINED() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };
//		double[] vetX = { 24, 120 };
//		double[] vetY = { 35.921969407999995, 9.153194127999999 };

		CurveFitter cf = Utility.MIRD_curveFitterSpecialImageJ(vetX, vetY);
		Regression reg = Utility.MIRD_curveFitterSpecialFlanagan(vetX, vetY);
		MyPlot.PL03_MIRD_curvePlotterSpecialCombined(cf, reg, vetX, vetY);

		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_specialMIRD_curveFitterFlanagan() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };

		Regression reg = Utility.MIRD_curveFitterSpecialFlanagan(vetX, vetY);
		MyPlot.PL05_MIRD_curvePlotterSpecialFlanagan(reg, vetX, vetY, "TITLE");

		MyLog.waitHere("SPETTA");
	}

//	@Test
//	public final void test_MIRD_pointsPlotter() {
//
//		double[] vetX = { 24, 48, 120 };
//		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };
//		boolean[] selected = { true, false, true };
//
//		MyPlot.PL01_MIRD_pointsPlotter(vetX, vetY, selected, "Titolo");
//
//		MyLog.waitHere("SPETTA");
//	}
//
//	@Test
//	public final void test_MIRD_closePlot() {
//
//		double[] vetX = { 24, 48, 120 };
//		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };
//		double[] vetY2 = { 35.921969407999995, 9.392739144, 9.153194127999999 };
//		boolean[] selected = { true, false, true };
//
//		String tit1 = "Punti1";
//		MyPlot.PL01_MIRD_pointsPlotter(vetX, vetY, selected, tit1);
//		String tit2 = "Punti2";
//		MyPlot.PL01_MIRD_pointsPlotter(vetX, vetY2, selected, tit2);
//		MyLog.waitHere("SPETTA");
//		Utility.closePlot(tit1);
//
//		MyLog.waitHere("SPETTA");
//	}

	@Test
	public final void test_logDedupe() {

		String path1 = "testdata/volatile.txt";
		String path2 = "testdata/volatile3.txt";
//		String path3 = "testdata/nuovo1.txt";
		File file1 = new File(path1);
		File file2 = new File(path2);

		new File(path2).delete();
		try {
			Files.copy(file1.toPath(), file2.toPath());
		} catch (IOException e) {
			MyLog.log("erroreCopia");
			e.printStackTrace();
		}
		MyLog.waitHere("SPETTA");

		MyLog.logDedupe(path2);

		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_logCoopyRange() {

		String path1 = "testdata/volatile.txt";
//		String path2 = "testdata/volatile3.txt";
		String path3 = "testdata/allacazzo.txt";
//		File file1 = new File(path1);
//		File file2 = new File(path2);
//		File file3 = new File(path3);

		MyLog.logDeleteSingle(path3);
		MyLog.logInit(path3);

		MyLog.waitHere("SPETTA");

		MyLog.logCopyRange(path1, path3, 31, 150);

		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_vetReverser() {

		double[] input1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		double[] input2 = input1;
		double[] output2 = Utility.vetReverser(input2);

		for (double aux : output2)
			MyLog.log("" + aux);

		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_isValidDate() {

		String dateIn = "07-01-2023";
		String formatIn = "dd-MM-yyyy";
		boolean ok = Utility.isValidDate(dateIn, formatIn);

		if (ok)
			MyLog.waitHere("Data= " + dateIn + " VALIDA");
		else
			MyLog.waitHere("Data= " + dateIn + " ERRATA");

	}

	@Test
	public final void test_isValidDateNew() {

		String dateIn = "12-03-23";
		boolean ok = Utility.isValidDateNew(dateIn, "dd-MM-uuuu");

		if (ok)
			MyLog.waitHere("Data= " + dateIn + " VALIDA");
		else
			MyLog.waitHere("Data= " + dateIn + " ERRATA");

	}

	@Test
	public final void test_stackSliceUpdater() {

		int width = 128;
		int height = 128;
		int depth = 128;

		String pathSorgente = "testdata/uno.tif";
		Opener opener1 = new Opener();
		ImagePlus imp1 = opener1.openImage(pathSorgente);
		imp1.show();
		ImageProcessor ip1 = imp1.getProcessor();
		MyLog.waitHere();

		ImageStack stack2 = ImageStack.create(width, height, depth, 8);
		for (int i1 = 1; i1 < stack2.size(); i1++) {
			stack2.setSliceLabel("FETTA_" + i1, i1);
		}
		ImagePlus imp2 = new ImagePlus("TITLE", stack2);
		imp2.show();
		MyLog.waitHere();

		int num = 10;
		MyStack.stackSliceUpdater(stack2, ip1, num);
		imp2.updateAndDraw();
		MyLog.waitHere("VERIFICA");

	}

	@Test
	public final void test_inCubo() {

		MyReader reader = new MyReader();

		String[] tabSvalues = reader.readTextFileSVALUESFromResources("testdata/S-values.txt", false);

		MyLog.logVector(tabSvalues, "tabSvalues");

		ImagePlus impRubik = Utility.inCubo(tabSvalues);

		impRubik.show();
		MyLog.waitHere("VERIFICA");

	}

	@Test
	public final void test_calcDVH1() {

		// double[] vetVoxel = { 1.2345, 1.3456, 1.2346, 2.234, 1.237, 2.3456, 3.4567,
		// 1.23468, 2.3459 };
		double[] vetVoxel = { 10., 12., 13., 17., 20., 12., 20., 41., 30., 17., 14., 24., 37., 27., 15., 13., 15., 12.,
				11., 10. };

		VoxelDosimetry.sub_DVH4(vetVoxel, 24);

		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_generaTabella() {

		String[] pippo = Utility.generaTabella();
		MyLog.logVector(pippo, "pippo");

		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_tabellaPuntatori() {

		String[] tabellaBella = Utility.generaTabella();

		int[][] tabellaPuntatori = Utility.tabellaPuntatori(tabellaBella);
		double[] tabellasval = Utility.tabellaSValues(tabellaBella);
		MyLog.logMatrix(tabellaPuntatori, "tabellapuntatori");
		MyLog.waitHere();
		MyLog.logVector(tabellasval, "tabellasval");
		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_tabellaSValues() {

		String[] tabellaBella = Utility.generaTabella();
		double[] tabellasval = Utility.tabellaSValues(tabellaBella);
		MyLog.waitHere("tabellaBellaLength= " + tabellaBella.length + " tabellasvalLength= " + tabellasval.length);
		for (double aux1 : tabellasval)
			IJ.log("" + aux1);
		MyLog.waitHere("GHET FRESA? POLSA!!!");

	}
	
	
	@Test
	public final void test_tabellaABC() {

		String filename="testdata2/ConvABC.txt";
		boolean intoJar=false;
		String stri="";
	
		double[][] tabella = Utility.tabellaConvABC(filename, intoJar);
		
		for (int i1 = 0; i1 < tabella.length; i1++) {
			stri = "";
			for (int i2 = 0; i2 < tabella[0].length; i2++) {
				stri += tabella[i1][i2] + ",  ";
			}
			IJ.log(stri);
		}

		
//		MyLog.logMatrix(tabella, "tabella");
		MyLog.waitHere("WANNAGANA");

	}


	@Test
	public final void test_matildeSingleVoxel() {

		double voxSignal = 0;
		double acqDuration = 0;
		double fatCal = 0;
		double deltaT = 0;
		double par_a = 0;
		boolean log1 = false;

		double vox1 = Utility.matildeSingleVoxel(voxSignal, acqDuration, fatCal, deltaT, par_a, log1);

		MyLog.waitHere("GHET FRESA? POLSA!!!");

	}

	@Test
	public final void test_calcDVH2() {

		double[] zero = { 0.000, 23.530, 23.659, 23.834, 24.438, 24.474, 25.457, 25.597, 25.602, 25.770, 25.790, 25.943,
				26.506, 26.920, 27.428, 28.069, 28.192, 28.317, 28.880, 29.173, 29.259, 29.282, 29.379, 29.907, 29.991,
				30.295, 30.503, 30.762, 31.675, 31.766, 32.018, 32.166, 32.439, 33.065, 33.270, 33.820, 34.231, 34.244,
				34.661, 35.732, 35.978, 37.210, 38.802, 39.358, 39.750, 40.252, 42.325, 42.987, 43.599, 44.485, 44.834,
				46.533, 47.544, 48.361, 48.945, 49.848, 50.516, 51.425, 54.096, 55.446, 57.763 };

		double[] uno = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0 };

		double[] due = { 100.000, 100.000, 98.333, 96.667, 95.000, 93.333, 91.667, 90.000, 88.333, 86.667, 85.000,
				83.333, 81.667, 80.000, 84.000, 86.000, 88.000, 93.000, 96.000, 94.000, 91.000, 88.000, 84.000, 81.000,
				78.000, 76.000, 71.000, 64.000, 55.000, 53.333, 51.667, 50.000, 48.333, 46.667, 45.000, 43.333, 41.667,
				40.000, 38.333, 36.667, 35.000, 33.333, 31.667, 30.000, 28.333, 26.667, 25.000, 23.333, 21.667, 20.000,
				18.333, 16.667, 15.000, 13.333, 11.667, 10.000, 8.333, 6.667, 5.000, 3.333, 1.667 };

		double[] tre = { 0.000, 21.170, 21.348, 21.438, 21.636, 21.699, 22.216, 22.303, 23.917, 23.931, 24.214, 24.659,
				24.743, 24.920, 25.307, 25.454, 25.684, 25.735, 25.892, 26.134, 26.537, 27.607, 27.736, 27.967, 28.2,
				28.553, 28.732, 29.591, 29.846, 30.225, 30.540, 30.590, 30.766, 30.7, 30.833, 31.223, 31.407, 32.068,
				32.859, 33.247, 33.627, 34.378, 34.415, 34.541, 35.225, 35.881, 36.142, 38.265, 38.537, 39.991, 40.725,
				41.576, 42.482, 43.304, 43.481, 43.702, 45.047, 46.073, 46.970, 48.668, 49.365 };
		double[] quattro = { 1., 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0 };

		double[] cinque = { 100.000, 100.000, 98.361, 96.721, 95.082, 93.443, 91.803, 90.164, 88.525, 86.885, 85.246,
				83.607, 81.967, 80.328, 76.000, 74.000, 70.000, 68.000, 66.000, 58.000, 52.000, 46.000, 42.000, 38.000,
				36.000, 32.000, 28.000, 26.000, 28.000, 34.000, 38.000, 42.000, 49.180, 47.541, 45.902, 44.262, 42.623,
				40.984, 39.344, 37.705, 36.066, 34.426, 32.787, 31.148, 29.508, 27.9, 26.230, 24.590, 22.951, 21.311,
				19.672, 18.033, 16.393, 14.754, 13.115, 11.475, 9.836, 8.197, 6.557, 4.918, 3.279, };
		double[] sei = { 0.000, 26.965, 27.433, 27.496, 27.981, 28.438, 28.967, 29.575, 29.738, 30.292, 30.465, 31.376,
				31.553, 31.715, 31.772, 31.810, 31.840, 32.122, 32.415, 32.779, 33.050, 33.603, 34.892, 35.6, 36.065,
				36.576, 36.740, 36.770, 36.950, 38.061, 38.459, 39.516, 40.119, 41.189, 42.456, 42.602, 43.226, 44.381,
				45.490, 46.282, 46.695, 48.068, 48.823, 49.388, 49.611, 49.707, 52.807, 54.502, 55.328, 56.359, 58.318,
				58.632, 62.632, 66.639 };
		double[] sette = { 1., 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
		double[] otto = { 100.000, 100.000, 98.113, 96.226, 94.340, 92.453, 90.566, 88.679, 86.792, 84.906, 83.019,
				81.132, 79.245, 77.358, 75.472, 73.585, 71.698, 69.811, 66.000, 58.000, 56.000, 52.000, 48.000, 44.000,
				46.000, 48.000, 50.000, 50.943, 50.000, 51.000, 52.000, 54.000, 56.000, 58.000, 57.000, 56.000, 55.000,
				54.000, 53.000, 51.000, 48.000, 46.000, 44.000, 42.000, 40.000, 38.000, 36.000, 34.000, 30.000, 28.000,
				26.000, 24.000, 22.000, 20.000 };

		VoxelDosimetry.subDVH2(zero, due, tre, cinque, sei, otto);

		MyLog.waitHere("GHET FRESA? POLSA!!!");

	}

	@Test
	public final void test_interpolator() {

		double[] vetxAA = { 0.0, 23.529600143432617, 23.65886878967285, 23.83372688293457, 24.43828010559082,
				24.474136352539062, 25.4566593170166, 25.597497940063477, 25.60228157043457, 25.770465850830078,
				25.789836883544922, 25.943246841430664, 26.506153106689453, 26.920307159423828, 27.428260803222656,
				28.06878662109375, 28.191898345947266, 28.31714630126953, 28.879501342773438, 29.173198699951172,
				29.25857162475586, 29.281558990478516, 29.3786678314209, 29.907485961914062, 29.99113655090332,
				30.294618606567383, 30.50263023376465, 30.76216697692871, 31.67542839050293, 31.76639747619629,
				32.017818450927734, 32.166324615478516, 32.43864440917969, 33.0648307800293, 33.26951217651367,
				33.81995391845703, 34.23105239868164, 34.244022369384766, 34.66102981567383, 35.73181915283203,
				35.978214263916016, 37.21025466918945, 38.802032470703125, 39.357852935791016, 39.75016403198242,
				40.252357482910156, 42.32545471191406, 42.987388610839844, 43.59947204589844, 44.484901428222656,
				44.83399963378906, 46.5334358215332, 47.543766021728516, 48.361473083496094, 48.944580078125,
				49.84808349609375, 50.51605224609375, 51.42494201660156, 54.09566116333008, 55.446205139160156,
				57.762760162353516 };
		double[] vetyAA = { 100.0, 100.00000000000007, 98.3333333333334, 96.66666666666673, 95.00000000000006,
				93.33333333333339, 91.66666666666671, 90.00000000000004, 88.33333333333337, 86.6666666666667,
				85.00000000000003, 83.33333333333336, 81.66666666666669, 80.00000000000001, 78.33333333333334,
				76.66666666666667, 75.0, 73.33333333333333, 71.66666666666666, 69.99999999999999, 68.33333333333331,
				66.66666666666664, 64.99999999999997, 63.3333333333333, 61.666666666666636, 59.99999999999997,
				58.33333333333331, 56.66666666666664, 54.99999999999998, 53.333333333333314, 51.66666666666665,
				49.999999999999986, 48.33333333333332, 46.66666666666666, 44.99999999999999, 43.33333333333333,
				41.666666666666664, 40.0, 38.333333333333336, 36.66666666666667, 35.00000000000001, 33.33333333333334,
				31.666666666666675, 30.000000000000007, 28.33333333333334, 26.66666666666667, 25.000000000000004,
				23.333333333333336, 21.666666666666668, 20.0, 18.333333333333332, 16.666666666666664,
				14.999999999999998, 13.333333333333332, 11.666666666666666, 10.0, 8.333333333333334, 6.666666666666667,
				5.0, 3.3333333333333335, 1.6666666666666667 };
		double[] vetxBB = { 0.0, 21.16951560974121, 21.347736358642578, 21.437503814697266, 21.635744094848633,
				21.6986026763916, 22.216442108154297, 22.303314208984375, 23.9174747467041, 23.93129539489746,
				24.21404457092285, 24.659154891967773, 24.743131637573242, 24.92019271850586, 25.30713653564453,
				25.45421600341797, 25.68378448486328, 25.735313415527344, 25.891990661621094, 26.133607864379883,
				26.536909103393555, 27.607135772705078, 27.736356735229492, 27.967065811157227, 28.286104202270508,
				28.55300521850586, 28.73162841796875, 29.590892791748047, 29.845746994018555, 30.22543716430664,
				30.540193557739258, 30.590309143066406, 30.765668869018555, 30.785680770874023, 30.832704544067383,
				31.222890853881836, 31.407180786132812, 32.06781768798828, 32.8586540222168, 33.246978759765625,
				33.627288818359375, 34.3782844543457, 34.41483688354492, 34.54070281982422, 35.22507858276367,
				35.88148880004883, 36.14151382446289, 38.26454162597656, 38.53656005859375, 39.99087905883789,
				40.724815368652344, 41.575721740722656, 42.48247528076172, 43.303550720214844, 43.480506896972656,
				43.70207977294922, 45.047149658203125, 46.07340621948242, 46.96957778930664, 48.66820526123047,
				49.364749908447266, 51.77025604248047 };
		double[] vetyBB = { 100.0, 100.00000000000004, 98.36065573770496, 96.72131147540988, 95.0819672131148,
				93.44262295081971, 91.80327868852463, 90.16393442622955, 88.52459016393446, 86.88524590163938,
				85.2459016393443, 83.60655737704921, 81.96721311475413, 80.32786885245905, 78.68852459016396,
				77.04918032786888, 75.4098360655738, 73.77049180327872, 72.13114754098363, 70.49180327868855,
				68.85245901639347, 67.21311475409838, 65.5737704918033, 63.934426229508226, 62.29508196721314,
				60.65573770491806, 59.01639344262298, 57.377049180327894, 55.73770491803281, 54.09836065573773,
				52.459016393442646, 50.81967213114756, 49.18032786885248, 47.5409836065574, 45.901639344262314,
				44.26229508196723, 42.62295081967215, 40.983606557377065, 39.34426229508198, 37.7049180327869,
				36.06557377049182, 34.426229508196734, 32.78688524590165, 31.14754098360657, 29.50819672131149,
				27.868852459016406, 26.229508196721323, 24.59016393442624, 22.950819672131157, 21.311475409836074,
				19.67213114754099, 18.03278688524591, 16.393442622950825, 14.754098360655743, 13.11475409836066,
				11.475409836065577, 9.836065573770494, 8.196721311475411, 6.557377049180328, 4.918032786885246,
				3.278688524590164, 1.639344262295082 };

		double[][] matout1 = Utility.interpolator(vetxAA, vetyAA, vetxBB, vetyBB);

		MyLog.logVector(vetxAA, "vetxAA");
		MyLog.logVector(vetyAA, "vetAA");
		MyLog.logVector(vetxBB, "vetxBB");
		MyLog.logVector(vetyBB, "vetyBB");
		MyLog.logMatrixVertical(matout1, "pippo");

		MyLog.waitHere("GHET FRESA? POLSA!!!");

	}

	@Test
	public final void test_rasegotto() {

		double[] vetx24 = { 0.0, 23.529600143432617, 23.65886878967285, 23.83372688293457, 24.43828010559082,
				24.474136352539062, 25.4566593170166, 25.597497940063477, 25.60228157043457, 25.770465850830078,
				25.789836883544922, 25.943246841430664, 26.506153106689453, 26.920307159423828, 27.428260803222656,
				28.06878662109375, 28.191898345947266, 28.31714630126953, 28.879501342773438, 29.173198699951172,
				29.25857162475586, 29.281558990478516, 29.3786678314209, 29.907485961914062, 29.99113655090332,
				30.294618606567383, 30.50263023376465, 30.76216697692871, 31.67542839050293, 31.76639747619629,
				32.017818450927734, 32.166324615478516, 32.43864440917969, 33.0648307800293, 33.26951217651367,
				33.81995391845703, 34.23105239868164, 34.244022369384766, 34.66102981567383, 35.73181915283203,
				35.978214263916016, 37.21025466918945, 38.802032470703125, 39.357852935791016, 39.75016403198242,
				40.252357482910156, 42.32545471191406, 42.987388610839844, 43.59947204589844, 44.484901428222656,
				44.83399963378906, 46.5334358215332, 47.543766021728516, 48.361473083496094, 48.944580078125,
				49.84808349609375, 50.51605224609375, 51.42494201660156, 54.09566116333008, 55.446205139160156,
				57.762760162353516 };

		double[] vety24 = { 100.0, 100.00000000000007, 98.3333333333334, 96.66666666666673, 95.00000000000006,
				93.33333333333339, 91.66666666666671, 90.00000000000004, 88.33333333333337, 86.6666666666667,
				85.00000000000003, 83.33333333333336, 81.66666666666669, 80.00000000000001, 78.33333333333334,
				76.66666666666667, 75.0, 73.33333333333333, 71.66666666666666, 69.99999999999999, 68.33333333333331,
				66.66666666666664, 64.99999999999997, 63.3333333333333, 61.666666666666636, 59.99999999999997,
				58.33333333333331, 56.66666666666664, 54.99999999999998, 53.333333333333314, 51.66666666666665,
				49.999999999999986, 48.33333333333332, 46.66666666666666, 44.99999999999999, 43.33333333333333,
				41.666666666666664, 40.0, 38.333333333333336, 36.66666666666667, 35.00000000000001, 33.33333333333334,
				31.666666666666675, 30.000000000000007, 28.33333333333334, 26.66666666666667, 25.000000000000004,
				23.333333333333336, 21.666666666666668, 20.0, 18.333333333333332, 16.666666666666664,
				14.999999999999998, 13.333333333333332, 11.666666666666666, 10.0, 8.333333333333334, 6.666666666666667,
				5.0, 3.3333333333333335, 1.6666666666666667 };
		double[] vetx48new = { 23.529600143432617, 21.169515609741214, 21.349232482910153, 21.44411182403564,
				21.63888702392578, 21.733125305175776, 22.223681449890137, 22.46473026275634, 23.919087155659994,
				23.96899528503418, 24.28081111907959, 24.67315101623535, 24.775592835744224, 24.997581481933597,
				25.339003753662112, 25.50778198242188, 25.696666717529297, 25.77709401448568, 25.960448869069424,
				26.254598236083993, 26.87581421534223, 27.650209426879886, 27.817104911804208, 28.08404655456544,
				28.38841625849407, 28.624454498291023, 29.089655240376818, 29.701329612731943, 30.016607570648205,
				30.372323481241867, 30.56441609064738, 30.677989006042484, 30.77600835164388, 30.810760116577146,
				31.04730701446534, 31.327321815490723, 31.79255231221518, 32.54231948852539, 33.09812094370524,
				33.487841796875, 34.115435981750494, 34.402652740478516, 34.50084527333578, 35.01976585388184,
				35.69550590515137, 36.072173817952475, 37.73378467559815, 38.47308909098307, 39.675776608785,
				40.57802810668946, 41.41972223917644, 42.33134969075521, 43.18038940429688, 43.456912740071616,
				43.67622960408529, 44.91264266967774, 45.987884839375816, 46.90983301798503, 48.58327388763428,
				49.34153175354004, 51.730164273579916 };
		double[] vety48new = { 100.0, 100.00000000000007, 98.3333333333334, 96.66666666666673, 95.00000000000006,
				93.33333333333339, 91.66666666666671, 90.00000000000004, 88.33333333333337, 86.6666666666667,
				85.00000000000003, 83.33333333333336, 81.66666666666669, 80.00000000000001, 78.33333333333334,
				76.66666666666667, 75.0, 73.33333333333333, 71.66666666666666, 69.99999999999999, 68.33333333333331,
				66.66666666666664, 64.99999999999997, 63.3333333333333, 61.666666666666636, 59.99999999999997,
				58.33333333333331, 56.66666666666664, 54.99999999999998, 53.333333333333314, 51.66666666666665,
				49.999999999999986, 48.33333333333332, 46.66666666666666, 44.99999999999999, 43.33333333333333,
				41.666666666666664, 40.0, 38.333333333333336, 36.66666666666667, 35.00000000000001, 33.33333333333334,
				31.666666666666675, 30.000000000000007, 28.33333333333334, 26.66666666666667, 25.000000000000004,
				23.333333333333336, 21.666666666666668, 20.0, 18.333333333333332, 16.666666666666664,
				14.999999999999998, 13.333333333333332, 11.666666666666666, 10.0, 8.333333333333334, 6.666666666666667,
				5.0, 3.3333333333333335, 1.6666666666666667 };
		double[] vetx120new = { 23.529600143432617, 26.964893341064464, 27.37821957270303, 27.481263160705563,
				27.811339569091786, 28.224964904785146, 28.65868616104125, 29.149469566345203, 29.604730669657386,
				29.775170389811187, 30.264684391021717, 30.43586508433024, 31.117763614654525, 31.48236961364746,
				31.631572755177814, 31.7362424214681, 31.781908988952637, 31.814166514078774, 31.844528420766192,
				32.09372577667236, 32.35137519836425, 32.6577517191569, 32.928099632263184, 33.289443969726555,
				34.01106637318928, 35.05100860595702, 35.717284202575684, 36.05248680114746, 36.49943542480469,
				36.6964729309082, 36.7586727142334, 36.86012268066406, 37.37587896982828, 38.16708068847656,
				38.61794261932373, 39.536567560831706, 40.06890137990315, 40.97501754760742, 42.05445308685302,
				42.53862787882487, 42.882714653015135, 43.61057917277018, 44.62102591196696, 45.56953086853027,
				46.26843318939209, 46.639601389567055, 47.72468948364257, 48.54612719217936, 49.114946111043295,
				49.47734680175781, 49.638221677144365, 50.22343953450522, 52.8921594619751, 54.389109293619796,
				55.1766690572103, 56.04948425292969, 57.50150521596272, 58.464219919840495, 60.03167953491211,
				63.56701850891113, 65.0 };
		double[] vety120new = { 100.0, 100.00000000000007, 98.3333333333334, 96.66666666666673, 95.00000000000006,
				93.33333333333339, 91.66666666666671, 90.00000000000004, 88.33333333333337, 86.6666666666667,
				85.00000000000003, 83.33333333333336, 81.66666666666669, 80.00000000000001, 78.33333333333334,
				76.66666666666667, 75.0, 73.33333333333333, 71.66666666666666, 69.99999999999999, 68.33333333333331,
				66.66666666666664, 64.99999999999997, 63.3333333333333, 61.666666666666636, 59.99999999999997,
				58.33333333333331, 56.66666666666664, 54.99999999999998, 53.333333333333314, 51.66666666666665,
				49.999999999999986, 48.33333333333332, 46.66666666666666, 44.99999999999999, 43.33333333333333,
				41.666666666666664, 40.0, 38.333333333333336, 36.66666666666667, 35.00000000000001, 33.33333333333334,
				31.666666666666675, 30.000000000000007, 28.33333333333334, 26.66666666666667, 25.000000000000004,
				23.333333333333336, 21.666666666666668, 20.0, 18.333333333333332, 16.666666666666664,
				14.999999999999998, 13.333333333333332, 11.666666666666666, 10.0, 8.333333333333334, 6.666666666666667,
				5.0, 3.3333333333333335, 1.6666666666666667 };

		double[][] matout = Utility.rasegotto(vetx24, vetx48new, vetx120new, vety24);

		MyLog.log("matout.length= " + matout.length);
		for (int i1 = 0; i1 < matout.length; i1++) {
			MyLog.log(String.format("|%20f|%20f", matout[i1][0], matout[i1][1]));
		}

		MyLog.waitHere("GHET FRESA? POLSA!!!");

	}

}
