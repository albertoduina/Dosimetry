package Dosimetry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
