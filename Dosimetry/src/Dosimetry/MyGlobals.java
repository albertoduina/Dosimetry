package Dosimetry;

import java.awt.Font;
import java.io.File;
import java.net.URL;
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
	public static String pathVolatile;
	public static String fegatoPath;
	public static String[] arrayOfString = { "24h", "48h", "120h" };
	public static String format1 = "dd-MM-yyyy HH:mm:ss";
//	public static String[] config = null;
	public static String titPL01 = null;
	public static String titPL04 = null;
	public static String titPL08 = null;
	public static String titPL11 = null;
	public static String titPL0405 = null;
	

	public static boolean attivaLog = false;
	public static boolean loggoVoxels = false;
	public static boolean mostraResultsSV05 = false;
	public static int[] coordinateVoxels = null;

	public static int coordX;
	public static int coordY;
	public static int coordZ;

	/**
	 * Coordinate del voxel da utilizzare per il logDebug
	 */
	static void coordinates() {

		URL url3 = Utility.class.getResource("Dosimetria_Lu177.class");
		String myString = url3.toString();
		int start = myString.indexOf("plugins");
		int end = myString.lastIndexOf("!");
		if (start < 0 || end < 0)
			return;
		String myPart1 = myString.substring(start, end);
		int end2 = myPart1.lastIndexOf("/");
		String myPart2 = myPart1.substring(0, end2);
		String myPath = myPart2 + File.separator + "DosimetryConfig.txt";
		File f1 = new File(myPath);
		if (!f1.isFile())
			return;
		String aux1 = MyReader.readFromLog(myPath, "#001#", "=");
		String aux2 = MyReader.readFromLog(myPath, "#002#", "=");
		String aux3 = MyReader.readFromLog(myPath, "#003#", "=");
		String aux4 = MyReader.readFromLog(myPath, "#004#", "=");

		attivaLog = aux1.equalsIgnoreCase("SI");
		loggoVoxels = aux2.equalsIgnoreCase("SI");
		mostraResultsSV05 = aux4.equalsIgnoreCase("SI");
		coordinateVoxels = MyGlobals.leggiCoordinateVoxels(aux3);

		MyLog.log("attivaLog= " + attivaLog + "\nloggoVoxels= " + loggoVoxels + "\ncoordinateVoxels[0] X= "
				+ coordinateVoxels[0] + "\ncoordinateVoxels[1] Y= " + coordinateVoxels[1] + "\ncoordinateVoxels[2] Z= "
				+ coordinateVoxels[2]);
		coordX = coordinateVoxels[0];
		coordY = coordinateVoxels[1];
		coordZ = coordinateVoxels[2];
	}
	

	/**
	 * lato del cubo da utilizzare per i calcoli
	 * 
	 * @return
	 */
	public static int latoCubo() {
		// ATTENZIONE il lato DEVE essere dispari
		// in questo modo avremo un pixel centrale
		int lato = 5;

		return lato;
	}

	public static int mezzoLato() {
		int mezzo = (latoCubo() - 1) / 2;
		return mezzo;
	}

	static int[] leggiCoordinateVoxels(String in1) {

		String[] vet = in1.split(",");
		int[] vetOut = new int[vet.length];
		for (int i1 = 0; i1 < vet.length; i1++) {
			vetOut[i1] = Utility.parseInt(vet[i1]);
		}
		return vetOut;
	}

}
