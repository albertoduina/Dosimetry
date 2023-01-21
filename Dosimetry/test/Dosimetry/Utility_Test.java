package Dosimetry;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import flanagan.analysis.Regression;
import ij.IJ;
import ij.ImageJ;
import ij.measure.CurveFitter;

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
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}
	
	@Test
	public final void test_LP06_DialogErrorMessage() {

		String paramString = "Messaggio di errore";
		Utility.dialogErrorMessage_LP06(paramString);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP27_BattezzaLesioni() {

		String pathVolatile = "Messaggio di errore";
		Utility.dialogBattezzaLesioni_LP27(pathVolatile);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_DD08_AltroDistretto() {

		Utility.dialogAltroDistretto_DD08();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_copyInfo() {

		String pathSorgente = "testdata/sorgente.txt";
		String pathDestinazione = "testdata/destinazione.txt";
		int start = 10;
		int end = 15;

		Utility.logCopyRange(pathSorgente, pathDestinazione, start, end);

		Utility.debugDeiPoveri("SPETTA");

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
//		Utility.debugDeiPoveri("SPETTA");
//
//	}

	@Test
	public final void test_logModifyEsistente() {

		String pathPermanente = "testdata/permanente.txt";
		Utility.logModify(pathPermanente, "#030#", "#030# !!!!!!!!!!!!!!!");
		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_logModifyMancante() {

		String pathPermanente = "testdata/permanente.txt";
		Utility.logModify(pathPermanente, "#730#", "#730# !!!!!!!!!!!!!!!");
		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_readSimpleText() {

		String pathSorgente = "testdata/sorgente.txt";
		String[] out1 = Utility.readSimpleText(pathSorgente);
		for (String aux : out1) {
			MyLog.log(aux);
		}
		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_readFromLog() {

		String pathSorgente = "testdata/sorgente.txt";

		String code1 = "#003#";
		String out1 = Utility.readFromLog(pathSorgente, code1);
		MyLog.log("TROVATO out1= " + out1);

		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_MIRD_curveFitter() {

		String pathSorgente = "testdata/sorgente.txt";

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 2.345, 4.897, 9.456 };

		Utility.MIRD_curveFitterImageJ(vetX, vetY);

		Utility.debugDeiPoveri("SPETTA");
	}

	@Test
	public final void test_specialMIRD_curveFitterImageJ() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };
		boolean[] selected = { true, false, true };

		CurveFitter cf = Utility.MIRD_curveFitterSpecialImageJ(vetX, vetY);
		Utility.MIRD_curvePlotterSpecialImageJ(cf, vetX, vetY, selected);

		Utility.debugDeiPoveri("SPETTA");
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

		Utility.debugDeiPoveri("SPETTA");
	}

	@Test
	public final void test_specialMIRD_curveFitterFlanagan() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };

		Regression reg = Utility.MIRD_curveFitterSpecialFlanagan(vetX, vetY);
		Utility.MIRD_curvePlotterSpecialFlanagan(reg, vetX, vetY);

		Utility.debugDeiPoveri("SPETTA");
	}

	@Test
	public final void test_MIRD_pointsPlotter() {

		double[] vetX = { 24, 48, 120 };
		double[] vetY = { 35.921969407999995, 18.392739144, 9.153194127999999 };
		boolean[] selected = { true, false, true };

		Utility.MIRD_pointsPlotter(vetX, vetY, selected, "Titolo");

		Utility.debugDeiPoveri("SPETTA");
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
		Utility.debugDeiPoveri("SPETTA");
		Utility.closePlot(tit1);

		Utility.debugDeiPoveri("SPETTA");
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
			MyLog.log("errore 001");
			e.printStackTrace();
		}
		Utility.debugDeiPoveri("SPETTA");

		Utility.logDedupe(path2);

		Utility.debugDeiPoveri("SPETTA");
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
		
		Utility.debugDeiPoveri("SPETTA");

		Utility.logCopyRange(path1, path3, 31, 150);

		Utility.debugDeiPoveri("SPETTA");
	}

	@Test
	public final void test_vetReverser() {

		double[] input1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		double[] input2 = input1;
		double[] output2 = Utility.vetReverser(input2);

		for (double aux : output2)
			MyLog.log("" + aux);

		Utility.debugDeiPoveri("SPETTA");

	}

}
