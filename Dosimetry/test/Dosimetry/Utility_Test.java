package Dosimetry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;

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
	public final void test_copyInfo() {

		String pathSorgente = "testdata/sorgente.txt";
		String pathDestinazione = "testdata/destinazione.txt";
		int start = 10;
		int end = 15;

		Utility.copyLogInfo(pathSorgente, pathDestinazione, start, end);

		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_modifyLogEsistente() {

		String pathPermanente = "testdata/permanente.txt";
		Utility.modifyLog(pathPermanente, "#030#", "#030# !!!!!!!!!!!!!!!");
		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_modifyLogMancante() {

		String pathPermanente = "testdata/permanente.txt";
		Utility.modifyLog(pathPermanente, "#730#", "#730# !!!!!!!!!!!!!!!");
		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_readSimpleText() {

		String pathSorgente = "testdata/sorgente.txt";
		String[] out1 = Utility.readSimpleText(pathSorgente);
		for (String aux : out1) {
			IJ.log(aux);
		}
		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_readFromLog() {

		String pathSorgente = "testdata/sorgente.txt";

		String code1 = "#003#";
		String out1 = Utility.readFromLog(pathSorgente, code1);
		IJ.log("TROVATO out1= " + out1);

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
	public final void test_dedupeLog() {

		String path1 = "testdata/volatile.txt";
		String path2 = "testdata/volatile3.txt";
//		String path3 = "testdata/nuovo1.txt";
		File file1 = new File(path1);
		File file2 = new File(path2);

		new File(path2).delete();
		try {
			Files.copy(file1.toPath(), file2.toPath());
		} catch (IOException e) {
			IJ.log("errore 001");
			e.printStackTrace();
		}
		Utility.debugDeiPoveri("SPETTA");

		Utility.dedupeLog(path2);

		Utility.debugDeiPoveri("SPETTA");
	}

}
