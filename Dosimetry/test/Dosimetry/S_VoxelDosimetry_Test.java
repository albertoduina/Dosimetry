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
public class S_VoxelDosimetry_Test {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();
	}

	@Test
	public final void test_stoCazzoDiTest() {

		double voxSignal = 142.0;
		double acqDuration = 1200;
		double fatCal = 1.73;
		double par_a = 0.00628;
		double deltaT = 23.26;

		double aTildeVoxel = S_VoxelDosimetry.stoCazzoDiTest(voxSignal, acqDuration, fatCal, par_a, deltaT);

		MyLog.waitHere("SPETTA");
	}

}
