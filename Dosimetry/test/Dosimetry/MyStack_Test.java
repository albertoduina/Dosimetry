package Dosimetry;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.ImageJ;
import ij.ImagePlus;

public class MyStack_Test {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}

	@Test
	public final void test_stackIsEmpty() {

		String path1 = "testdata/mAtilde24h.tif";
		ImagePlus imp1 = Utility.openImage(path1);
		imp1.show();
		boolean empty = MyStack.stackIsEmpty(imp1);
		MyLog.waitHere("empty= " + empty);
	}

}
