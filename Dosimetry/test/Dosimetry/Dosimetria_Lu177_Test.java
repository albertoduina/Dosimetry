package Dosimetry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;

public class Dosimetria_Lu177_Test {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}
	
	
	
	

	@Test
	public final void test_getDateTime() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String in1 = "20230102";
		String in2= "143834.0";
		Date out1 = dosimetria_Lu177.getDateTime(in1, in2);
		String out2= out1.toString();
		String exp1= "Mon Jan 02 14:38:34 CET 2023";
		assertEquals(exp1, out2);
	}
	
	@Test
	public final void test_dataToDicom() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String in1 = "02-01-2023";
		String out1 = dosimetria_Lu177.dataToDicom(in1);
		String exp1= "20230102";
		assertEquals(exp1, out1);
	}

	@Test
	public final void test_oraToDicom() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String in1 = "14:38:33";
		String out1 = dosimetria_Lu177.oraToDicom(in1);
		String exp1= "143833";
		assertEquals(exp1, out1);
	}

	@Test
	public final void test_LP31_DialogSelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogSelection_LP31();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP30_DialogSelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogSelection_LP30();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP21_DialogImmaginiPazientePrecedente() {

		String[] in1 = { "aa", "bb" };
		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogImmaginiPazientePrecedente_LP21(in1);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP20_DialogDirectorySelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String out1 = dosimetria_Lu177.directorySelection_LP_20();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP10_DialogConfirmDatiSomministrazione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String paramString = "Messaggio di errore";
		String[] in1 = { "02-01-2022", "12:44:00", "3.456" };
		boolean ok = dosimetria_Lu177.dialogConfirmDatiSomministrazione_LP10(in1);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP06_DialogErrorMessage() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String paramString = "Messaggio di errore";
		dosimetria_Lu177.dialogErrorMessage_LP06(paramString);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP05_DialogReview() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		ArrayList<ArrayList<String>> aList = null;
		boolean ok = dosimetria_Lu177.dialogReview_LP05(aList);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP04_DialogDatiSomministrazione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String[] out1 = dosimetria_Lu177.dialogDatiSomministrazione_LP04();
		for (String aux : out1) {
			IJ.log("" + aux);
		}
		Utility.debugDeiPoveri("SPETTA ");
		assertTrue(true);
	}

	@Test
	public final void test_LP03_DialogConfirmFolder() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogConfirmFolder_LP03("Path 24h", "Path 48h", "Path 120h");
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP02_DialogSelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogSelection_LP02();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP01_DialogNonBlockingDelete() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogNonBlockingDelete_LP01("Path della cartella");
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP00_DialogInitialize() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogInitialize_LP00();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

}
