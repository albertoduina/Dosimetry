package Dosimetry;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.WaitForUserDialog;

/**
 * @author Alberto
 *
 */
public class MyLog {

	public static void log(String str) {

		boolean stampa = false;

		String[] config = Utility.leggiConfig("DosimetryConfig.txt");
		if (config == null) {
			IJ.log(str);
			return;
		}
		String strIn = config[0];
		if (strIn.equalsIgnoreCase("SI")) {
			stampa = true;
		} else {
			// IJ.log("stampa=false");
			stampa = false;
		}
		if (stampa) {
			IJ.log(str); /// CAZZATA !!! DEVE ASSOLUTAMENTE essere IJ.log senno' hai voglia a ricorsioni
							/// infinite!!!
		}
		return;
	}

	public static void here() {
		IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " class="
				+ Thread.currentThread().getStackTrace()[2].getClassName() + " method="
				+ Thread.currentThread().getStackTrace()[2].getMethodName());

	}

	public static void here(String str) {
		MyLog.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " class="
				+ Thread.currentThread().getStackTrace()[2].getClassName() + " method="
				+ Thread.currentThread().getStackTrace()[2].getMethodName() + " " + str);
	}

	public static void waitHere() {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber()).show();
	}

	public static void waitHere(String str) {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n \n" + str).show();
	}

	public static void logArrayList(ArrayList<String> arrList) {
		if (arrList == null) {
			MyLog.log("Warning vector = null");
		} else {
			MyLog.log("----------- [ " + arrList.size() + " ] -----------");
			for (int j1 = 0; j1 < arrList.size(); j1++) {
				MyLog.log(arrList.get(j1));
			}
			MyLog.log("---------------------------------------------");
		}
	}

	public static void logArrayList(ArrayList<Double> arrList, String title) {
		if (arrList == null) {
			MyLog.log("Warning vector " + title + " = null");
		} else {
			MyLog.log("----------- " + title + "  [ " + arrList.size() + " ] -----------");
			String logRiga = "";
			for (int j1 = 0; j1 < arrList.size(); j1++) {
				logRiga += arrList.get(j1) + ",  ";
			}
			MyLog.log(logRiga);
		}
	}

	public static void logVector(double[] vect, String nome) {
		String stri = "";
		if (vect == null) {
			MyLog.log("Warning vector " + nome + " = null");
		} else {
			MyLog.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			MyLog.log(stri);
		}
		MyLog.log("---------------------------------------------");
	}

	public static String logElapsed(long start, long end) {

		long elapsed = end - start;

		int fractions = (int) elapsed % 60;
		int seconds = (int) (elapsed / 1000) % 60;
		int minutes = (int) (elapsed / (1000 * 60));
		int hours = (int) (elapsed / (1000 * 60 * 60));

		String aux1 = "elapsed [hh:mm:ss] " + String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, fractions);

		MyLog.log("elapsed [hh:mm:ss] " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
		return aux1;
	}

	public static void logVector(String vect[], String nome) {
		String stri = "";
		if (vect == null) {
			MyLog.log("Warning vector " + nome + " = null");
		} else {
			MyLog.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			MyLog.log(stri);
		}
		MyLog.log("---------------------------------------------");
	}

	public static void logMatrix(int mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			MyLog.waitHere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				MyLog.waitHere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// MyLog.log("rows=" + rows + " columns= " + columns);

			MyLog.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {
					stri += mat[i1][i2] + ",  ";
				}
				MyLog.log(stri);
			}
		}
		MyLog.log("---------------------------------------------");
	}

	public static void logMatrix(double mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			MyLog.waitHere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				MyLog.waitHere("Warning matrix " + nome + " length=0");
				return;
			}
			columns = mat[0].length;
			// MyLog.waitThere("rows=" + rows + " columns= " + columns);

			MyLog.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {
					stri += mat[i1][i2] + ",  ";
				}
				MyLog.log(stri);
			}
		}
		MyLog.log("---------------------------------------------");
	}

	public static void logMatrixVertical(double mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			MyLog.waitHere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				MyLog.waitHere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// MyLog.log("rows=" + rows + " columns= " + columns);

			MyLog.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < columns; i1++) {
				stri = "";
				for (int i2 = 0; i2 < rows; i2++) {
					stri += mat[i2][i1] + ",  ";
				}
				MyLog.log(stri);
			}
		}
		MyLog.log("---------------------------------------------");
	}

}
