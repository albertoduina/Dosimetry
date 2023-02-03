package Dosimetry;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import flanagan.analysis.Regression;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
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

		Utility.dialogAltreLesioni_FM02();
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_FM01_DialogFileSelection() {

		String message = "Select a fileO";
		String defaultDirectory = "\testdata";
		Utility.dialogFileSelection_FM01(message, defaultDirectory);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP06_DialogErrorMessage() {

		String paramString = "Messaggio di errore";
		Utility.dialogErrorMessage_LP06(paramString);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}
	
	@Test
	public final void test_LP09_DialogErrorMessageWithCancel() {

		String paramString = "Messaggio di errore";
		Utility.dialogErrorMessageWithCancel_LP09(paramString);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP27_BattezzaLesioni() {

		String pathVolatile = "Messaggio di errore";
		Utility.dialogBattezzaLesioni_LP27(pathVolatile);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_DD08_AltroDistretto() {

		Utility.dialogAltroDistretto_DD08();
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_copyInfo() {

		String pathSorgente = "testdata/sorgente.txt";
		String pathDestinazione = "testdata/destinazione.txt";
		int start = 10;
		int end = 15;

		Utility.logCopyRange(pathSorgente, pathDestinazione, start, end);

		MyLog.waitHere("SPETTA");

	}

//	@Test
//	public final void test_removeLineLog() {
//
//		File source= new File("testdata/permanente2.txt");
//		File dest= new File("testdata/permanente.txt");
//		try {
//			if (dest.exists())  
//					dest.delete();
//			
//			Files.copy(source.toPath(), dest.toPath());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		String pathPermanente = "testdata/permanente.txt";
//		Utility.removeLineLog(pathPermanente, "#030#");
//		MyLog.waitHere("SPETTA");
//
//	}

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
		Utility.logModify(pathPermanente, "#030#", "#030# MODIFICATO");
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
		Utility.logModify(pathPermanente, "#730#", "#730# MODIFICATO (AGGIUNTO)");
		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_readSimpleText() {

		String pathSorgente = "testdata/sorgente.txt";
		String[] out1 = Utility.readSimpleText(pathSorgente);
		for (String aux : out1) {
			MyLog.log(aux);
		}
		MyLog.waitHere("SPETTA");

	}

	@Test
	public final void test_readFromLog() {

		String pathSorgente = "testdata/sorgente.txt";

		String code1 = "#003#";
		String out1 = Utility.readFromLog(pathSorgente, code1);
		MyLog.log("TROVATO out1= " + out1);

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

		CurveFitter cf = Utility.MIRD_curveFitterSpecialImageJ(vetX, vetY);
		Utility.MIRD_curvePlotterSpecialImageJ(cf, vetX, vetY, selected);

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
		Utility.MIRD_curvePlotterSpecialCombined(cf, reg, vetX, vetY);

		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_specialMIRD_curveFitterFlanagan() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };

		Regression reg = Utility.MIRD_curveFitterSpecialFlanagan(vetX, vetY);
		Utility.MIRD_curvePlotterSpecialFlanagan(reg, vetX, vetY);

		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_MIRD_pointsPlotter() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };
		boolean[] selected = { true, false, true };

		Utility.MIRD_pointsPlotter(vetX, vetY, selected, "Titolo");

		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_MIRD_closePlot() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };
		double[] vetY2 = { 35.921969407999995, 9.392739144, 9.153194127999999 };
		boolean[] selected = { true, false, true };

		String tit1 = "Punti1";
		Utility.MIRD_pointsPlotter(vetX, vetY, selected, tit1);
		String tit2 = "Punti2";
		Utility.MIRD_pointsPlotter(vetX, vetY2, selected, tit2);
		MyLog.waitHere("SPETTA");
		Utility.closePlot(tit1);

		MyLog.waitHere("SPETTA");
	}

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

		Utility.logDedupe(path2);

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

		Utility.logDeleteSingle(path3);
		Utility.logInit(path3);

		MyLog.waitHere("SPETTA");

		Utility.logCopyRange(path1, path3, 31, 150);

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

		int width=128;
		int height=128;
		int depth=128;
		
		String pathSorgente = "testdata/uno.tif";
		Opener opener1 = new Opener();
		ImagePlus imp1 = opener1.openImage(pathSorgente);
		imp1.show();
		ImageProcessor ip1=imp1.getProcessor();
		MyLog.waitHere();
	
		ImageStack stack2= ImageStack.create(width, height, depth, 8);
		for (int i1=1; i1 < stack2.size(); i1++) {
			stack2.setSliceLabel("FETTA_"+i1, i1);
		}
		ImagePlus imp2= new ImagePlus("TITLE", stack2);
		imp2.show();
		MyLog.waitHere();
		
		int num=10;
		Utility.stackSliceUpdater(stack2, ip1, num);
		imp2.updateAndDraw();
		MyLog.waitHere("VERIFICA");


	}


}
