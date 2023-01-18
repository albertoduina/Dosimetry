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
		String in2 = "143834.0";
		Date out1 = dosimetria_Lu177.getDateTime(in1, in2);
		String out2 = out1.toString();
		String exp1 = "Mon Jan 02 14:38:34 CET 2023";
		assertEquals(exp1, out2);
	}

	@Test
	public final void test_dataToDicom() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String in1 = "02-01-2023";
		String out1 = dosimetria_Lu177.dataToDicom(in1);
		String exp1 = "20230102";
		assertEquals(exp1, out1);
	}

	@Test
	public final void test_oraToDicom() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String in1 = "14:38:33";
		String out1 = dosimetria_Lu177.oraToDicom(in1);
		String exp1 = "143833";
		assertEquals(exp1, out1);
	}

	@Test
	public final void test_LP33_PointsSelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean[] selection = dosimetria_Lu177.pointsSelection_LP33();
		IJ.log("LP33 restituisce " + selection);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP32_DialogReview() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String selection = dosimetria_Lu177.dialogReview_LP32();
		IJ.log("LP32 restituisce " + selection);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP31_DialogSelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogSelection_LP31();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP30_DialogInstructions() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogInstructions_LP30();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP21_DialogImmaginiPazientePrecedente() {

		String[] in1 = { "aa", "bb" };
		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
//		boolean ok = dosimetria_Lu177.dialogImmaginiPazientePrecedente_LP21(in1);
		int out = dosimetria_Lu177.dialogImmaginiPazientePrecedente_LP21(in1);

		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP20_DialogDirectorySelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String out1 = dosimetria_Lu177.directorySelection_LP_20();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP10_DialogConfermaDatiSomministrazione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String paramString = "Messaggio di errore";
		String[] in1 = { "02-01-2022", "12:44:00", "3.456" };
		boolean ok = dosimetria_Lu177.dialogConfermaDatiSomministrazione_LP10(in1);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP08_DialogRipetizione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		int aa = dosimetria_Lu177.dialogRipetizione_LP08();
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(true);
	}
	
	@Test
	public final void test_LP07_dialogDistretto() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		dosimetria_Lu177.dialogDistretto_LP07();
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
		ArrayList<ArrayList<String>> alist = new ArrayList<ArrayList<String>>();
		ArrayList<String> blist = new ArrayList<String>();
		blist.add("pippo");
		blist.add("pluto");
		blist.add("paperino");
		alist.add(blist);

		boolean ok = dosimetria_Lu177.dialogReview_LP05(alist);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP04_DialogInputDatiSomministrazione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String[] out1 = dosimetria_Lu177.dialogInputDatiSomministrazione_LP04();
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

	@Test
	public final void test_LP66_MIRD_display() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		double vol24 = 124.5001;
		double vol48 = 95.5190;
		double vol120 = 212.6678;

		dosimetria_Lu177.MIRD_display_LP66(vol24, vol48, vol120);
		Utility.debugDeiPoveri("SPETTA");
		assertTrue(false);
	}

	@Test
	public final void test_LP67_MIRD_display() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		double vol24 = 35.921969;
		double vol48 = 18.392739;
		double vol120 = 9.153194;
		double uptake = 41.119521;
		double massa = 21.155967;
		double tmezzo = 33.580609;
		double dose = 10.147774;

		dosimetria_Lu177.MIRD_display_LP67(vol24, vol48, vol120, uptake, massa, tmezzo, dose);

		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_LP68_MIRD_display() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();

		double[] out2 = { 50.741488, 0.020641, 2458.251552, 24.582515, 41.119521, 21.155967, 33.580609, 1992.100123,
				10.147774, 10.539239, 0.005856, 864.416111, 8.644161, 8.540712, 13.596632, 9.528169, 700.499280,
				5.016710 };

		dosimetria_Lu177.MIRD_display_LP68(out2);

		Utility.debugDeiPoveri("SPETTA");

	}

	@Test
	public final void test_LP67_FUNNY_display() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		double vol24 = 12.334;
		double vol48 = 15.4467;
		double vol120 = 18.3452;
		double uptake = 1.345;
		double massa = 6.789;
		double tmezzo = 0.23344;
		double dose = 0.3456;

		dosimetria_Lu177.FUNNY_display_LP67(vol24, vol48, vol120, uptake, massa, tmezzo, dose);

		Utility.debugDeiPoveri("SPETTA");

	}

}
