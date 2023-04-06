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

public class MyReader_Test {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

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
	public  void test_readTextFileFromResources() {
	
		// ATTENZIONE funziona solo se il file da leggere viene messo nella cartella di
		// test
		String pathSorgente = "testdata2/permanente3.txt";
		boolean intoJar = false;
		MyReader reader = new MyReader();
	
		String[] out1 = reader.readTextFileFromResources(pathSorgente, intoJar);
		for (String aux : out1) {
			IJ.log(aux);
		}
		MyLog.waitHere("SPETTA");
	
	}

	@Test
	public  final void test_readSimpleText() {
	
		String pathSorgente = "testdata/sorgente.txt";
		String[] out1 = MyReader.readSimpleText(pathSorgente);
		for (String aux : out1) {
			IJ.log(aux);
		}
		MyLog.waitHere("SPETTA");
	
	}

	@Test
	public final void test_readFromLog() {
	
		String pathSorgente = "testdata/sorgente.txt";
	
		String code1 = "#003#";
		String out1 = MyReader.readFromLog(pathSorgente, code1);
		IJ.log("TROVATO out1= " + out1);
	
		MyLog.waitHere("SPETTA");
	
	}

}
