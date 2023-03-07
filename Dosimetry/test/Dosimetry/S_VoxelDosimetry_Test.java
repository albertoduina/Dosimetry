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
	public final void test_mAtildeSingleVoxel() {

		double voxSignal = 364.0;
		double acqDuration = 1200;
		double fatCal = 1.8037930743094028;
		double deltaT = 23.3480425;
		double par_a = 0.0063102568457841105;
		double aTildeVoxelExpected = 111166.625880776;

		double aTildeVoxel = VoxelDosimetry.mAtildeSingleVoxel(voxSignal, acqDuration, fatCal, deltaT, par_a);

		MyLog.log("aTildeVoxel_______ = " + aTildeVoxel + "\naTildeVoxelExpected= " + aTildeVoxelExpected);
		MyLog.waitHere();

		assertEquals(aTildeVoxel, aTildeVoxelExpected );
	}

}
