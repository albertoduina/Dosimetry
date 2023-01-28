package Dosimetry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.ImageJ;


/**
 * @author ----
 *
 */
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

		String in1 = "20230102";
		String in2 = "143834.0";
		Date out1 = Dosimetria_Lu177.getDateTime(in1, in2);
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

		boolean[] selection = Dosimetria_Lu177.pointsSelection_LP33();
		MyLog.log("LP33 restituisce " + selection);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP32_DialogReview() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String selection = dosimetria_Lu177.dialogReview_LP32();
		MyLog.log("LP32 restituisce " + selection);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP31_DialogSelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogSelection_LP31();
		MyLog.waitHere("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP30_DialogInstructions() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogInstructions_LP30();
		MyLog.waitHere("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP21_DialogImmaginiPazientePrecedente() {

		String[] in1 = { "aa", "bb" };
		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		dosimetria_Lu177.dialogImmaginiPazientePrecedente_LP21(in1);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP20_DialogDirectorySelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		dosimetria_Lu177.directorySelection_LP_20();
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP10_DialogConfermaDatiSomministrazione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String[] in1 = { "02-01-2022", "12:44:00", "3.456" };
		dosimetria_Lu177.dialogConfermaDatiSomministrazione_LP10(in1);
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP08_DialogRipetizione() {

		Dosimetria_Lu177.dialogRipetizione_LP08();
		MyLog.waitHere("SPETTA");
		assertTrue(true);
	}

	@Test
	public final void test_LP07_DialogDistretto() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		dosimetria_Lu177.dialogDistretto_LP07();
		MyLog.waitHere("SPETTA");
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
		MyLog.waitHere("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP11_DialogInputDataSomministrazione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String out1 = dosimetria_Lu177.dialogInputDataSomministrazione_LP11();
		if (out1 == null) {
			MyLog.waitHere("out1==null");
		} else {
			MyLog.log("Data inserita " + out1 + " CORRETTA");
		}
		MyLog.waitHere("WAIT-HALT-SPETTA");
		assertTrue(true);
	}
	
	@Test
	public final void test_LP12_DialogInputOraSomministrazione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		String out1 = dosimetria_Lu177.dialogInputOraSomministrazione_LP12();
		if (out1 == null) {
			MyLog.waitHere("out1==null");
		} else {
			MyLog.log("Ora inserita " + out1 + " CORRETTA");
		}
		MyLog.waitHere("WAIT-HALT-SPETTA");
		assertTrue(true);
	}
	
	
	@Test
	public final void test_LP13_DialogInputActivitySomministrazione() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		double out1 = dosimetria_Lu177.dialogInputActivitySomministrazione_LP13();
		if (out1 == Double.NaN) {
			MyLog.waitHere("out1==null");
		} else {
			MyLog.log("Attivita' inserita " + out1);
		}
		MyLog.waitHere("WAIT-HALT-SPETTA");
		assertTrue(true);
	}


	@Test
	public final void test_LP03_DialogConfirmFolder() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogConfirmFolder_LP03("Path 24h", "Path 48h", "Path 120h");
		MyLog.waitHere("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP02_DialogSelection() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogSelection_LP02();
		MyLog.waitHere("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP01_DialogNonBlockingDelete() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogNonBlockingDelete_LP01("Path della cartella");
		MyLog.waitHere("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP00_DialogInitialize() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		boolean ok = dosimetria_Lu177.dialogInitialize_LP00();
		MyLog.waitHere("SPETTA");
		assertTrue(ok);
	}

	@Test
	public final void test_LP66_MIRD_display() {

		Dosimetria_Lu177 dosimetria_Lu177 = new Dosimetria_Lu177();
		double vol24 = 124.5001;
		double vol48 = 95.5190;
		double vol120 = 212.6678;
		dosimetria_Lu177.MIRD_display_LP66(vol24, vol48, vol120);
		MyLog.waitHere("SPETTA");
		assertTrue(false);
	}

	@Test
	public final void test_LP67_MIRD_display() {

		double vol24 = 35.921969;
		double vol48 = 18.392739;
		double vol120 = 9.153194;
		double uptake = 41.119521;
		double massa = 21.155967;
		double tmezzo = 33.580609;
		double dose = 10.147774;
		Dosimetria_Lu177.MIRD_display_LP67(vol24, vol48, vol120, uptake, massa, tmezzo, dose);
		MyLog.waitHere("SPETTA");
	}

	@Test
	public final void test_LP68_MIRD_display() {

		double[] out2 = { 50.741488, 0.020641, 2458.251552, 24.582515, 41.119521, 21.155967, 33.580609, 1992.100123,
				10.147774, 10.539239, 0.005856, 864.416111, 8.644161, 8.540712, 13.596632, 9.528169, 700.499280,
				5.016710 };
		Dosimetria_Lu177.MIRD_display_LP68(out2);
		MyLog.waitHere("SPETTA");
	}


}
