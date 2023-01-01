package Dosimetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import ReadCdStudies.CD_dirInfo;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

/**
 * @version v3
 * @author Date 05 dec 2022
 */
public class S_VoxelDosimetry implements PlugIn {


	// =================================================================================
	// =================================================================================
	// =====================PROVA PER
	// GITHUB============================================
	// =================================================================================
	// =================================================================================
	// VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV

	public void run(String arg) {

		String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
		String pathPermanente = desktopPath + File.separator + "DosimetryFolder" + File.separator + "permanente.txt";
		String pathVolatile = desktopPath + File.separator + "DosimetryFolder" + File.separator + "volatile.txt";

	}

	// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// =================================================================================
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
	// =================================================================================
	// =================================================================================


}