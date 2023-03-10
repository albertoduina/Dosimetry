package Dosimetry;

import java.awt.Font;
import java.util.Date;

import ij.util.FontUtil;

public class MyGlobals {

	public static String fontStyle = "Arial";
	public static Font defaultFont = FontUtil.getFont(fontStyle, Font.PLAIN, 13);
	public static Font textFont = FontUtil.getFont(fontStyle, Font.ITALIC, 16);
	public static Font titleFont = FontUtil.getFont(fontStyle, Font.BOLD, 16);
	public static String m_patName;
	public static Date m_patBirthday = null;
	public static String m_patYears;
	public static String m_patID;
	public static String m_serDate;
	public static String m_styDate;
	public static String m_styName;
	public static String petSeriesName;
	public static String desktopPath;
	public static String desktopDosimetryFolderPath;
	public static String desktopImagesSubfolderPath;
	public static String pathPermanente;
	public static String[] arrayOfString = { "24h", "48h", "120h" };
	public static String format1 = "dd-MM-yyyy HH:mm:ss";
//	public static String[] config = null;
	public static String titPL01 = null;
	public static String titPL04 = null;
	public static String titPL08 = null;
	public static String titPL11 = null;
	public static String titPL0405 = null;

	public static boolean loggoVoxels = false;
	public static int[] coordinateVoxels = null;

	public static int coordX;
	public static int coordY;
	public static int coordZ;

	static void coordinates() {

		String[] config = Utility.leggiConfig("DosimetryConfig.txt");
		coordinateVoxels = Utility.leggiCoordinateVoxels(config);

		MyLog.log("loggoVoxels= " + loggoVoxels + "\ncoordinateVoxels[0] X= " + coordinateVoxels[0]
				+ "\ncoordinateVoxels[1] Y= " + coordinateVoxels[1] + "\ncoordinateVoxels[2] Z= "
				+ coordinateVoxels[2]);
		coordX = coordinateVoxels[0];
		coordY = coordinateVoxels[1];
		coordZ = coordinateVoxels[2];
	}

}
