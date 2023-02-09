package Dosimetry;

import java.util.ArrayList;

import ij.IJ;
import ij.gui.WaitForUserDialog;

/**
 * @author Alberto
 *
 */
public class MyLog {

	public static void here() {
		IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " class="
				+ Thread.currentThread().getStackTrace()[2].getClassName() + " method="
				+ Thread.currentThread().getStackTrace()[2].getMethodName());

	}

	public static void here(String str) {
		IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
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

	public static void log(String str) {
		if (Utility.stampa())
			IJ.log(str);
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
	
	
	public static void logVector(double vect[], String nome) {
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


}
