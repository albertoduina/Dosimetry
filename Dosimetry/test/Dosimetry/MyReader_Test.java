package Dosimetry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;

public class MyReader_Test {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}

	@Test
	public void test_readTextFileFromResources() {

		// ATTENZIONE da Junit funziona solo se il file da leggere viene messo nella
		// cartella test.Dosimetry oppure test.Dosmimetry.testdata2
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
	public final void test_readSimpleText() {

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

	@Test
	public void test_readTextFileSVALUESFromResources() {
	
		// ATTENZIONE da Junit funziona solo se il file da leggere viene messo nella
		// cartella test.Dosimetry oppure test.Dosmimetry.testdata2
		String pathSorgente = "testdata2/S-values.txt";
		boolean intoJar = false;
		MyReader reader = new MyReader();
	
		String[] out1 = reader.readTextFileSVALUESFromResources(pathSorgente, intoJar);
		for (String aux : out1) {
			IJ.log(aux);
		}
		MyLog.waitHere("SPETTA");
	
	}

}
