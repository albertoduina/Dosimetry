package Dosimetry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;

import flanagan.analysis.Regression;
import ij.IJ;
import ij.WindowManager;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import ij.util.Tools;

public class MyPlot {

	/**
	 * 
	 * @param profilex1
	 * @param profiley1
	 * @param profilex2
	 * @param profiley2
	 * @param profilex3
	 * @param profiley3
	 * @param title
	 * @param xlabel
	 * @param ylabel
	 * @return
	 */
	public static Plot PL13_myPlotMultiple(double[] profilex1, double[] profiley1, double[] profilex2,
			double[] profiley2, double[] profilex3, double[] profiley3, String title, String xlabel, String ylabel) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		Plot plot = new Plot("PL13 " + title, xlabel, ylabel);
		plot.setColor(Color.red);
		plot.add("line", profilex1, profiley1);
		plot.setColor(Color.green);
		plot.add("line", profilex2, profiley2);
		plot.setColor(Color.blue);
		plot.add("line", profilex3, profiley3);

		double[] appx2 = Utility.concatArrays(profilex1, profilex2);
		double[] appx3 = Utility.concatArrays(appx2, profilex3);
		double[] a = Tools.getMinMax(appx3);
		double[] appy2 = Utility.concatArrays(profiley1, profiley2);
		double[] appy3 = Utility.concatArrays(appy2, profiley3);
		double[] b = Tools.getMinMax(appy3);
		plot.setLimits(0, a[1] * 1.05, 0, b[1] * 1.05);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		return plot;
	}

	/**
	 * 
	 * @param profilex1
	 * @param profiley1
	 * @param profilex2
	 * @param profiley2
	 * @param title
	 * @param xlabel
	 * @param ylabel
	 * @return
	 */
	public static Plot PL12_myPlotMultiple(double[] profilex1, double[] profiley1, double[] profilex2,
			double[] profiley2, String title, String xlabel, String ylabel) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		Plot plot = new Plot("P12 " + title, xlabel, ylabel);
		plot.setColor(Color.red);
		plot.add("line", profilex1, profiley1);
		plot.setColor(Color.green);
		plot.add("line", profilex2, profiley2);

		double[] appx2 = Utility.concatArrays(profilex1, profilex2);
		double[] a = Tools.getMinMax(appx2);
		double[] appy2 = Utility.concatArrays(profiley1, profiley2);
		double[] b = Tools.getMinMax(appy2);
		plot.setLimits(0, a[1] * 1.05, 0, b[1] * 1.05);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		return plot;
	}

	/**
	 * Permette il ploti di fino a 3 curve, se anche solo uno degli array di una
	 * curva e'null, la curva non viene mostrata
	 * 
	 * @param profilex1
	 * @param profiley1
	 * @param profilex2
	 * @param profiley2
	 * @param profilex3
	 * @param profiley3
	 * @param title
	 * @param xlabel
	 * @param ylabel
	 * @return
	 */
	public static void PL11_myPlotMultiple2(double[] profilex1, double[] profiley1, double[] profilex2,
			double[] profiley2, double[] profilex3, double[] profiley3, String title, String xlabel, String ylabel) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;
		int p1 = 0;
		int p2 = 0;
		int p3 = 0;

		if (profilex1 != null)
			p1 = profilex1.length;
		if (profilex2 != null)
			p2 = profilex2.length;
		if (profilex3 != null)
			p3 = profilex3.length;

		double[] xx = new double[p1+p2+p3];
		double[] yy = new double[p1+p2+p3];
		int l1 = 0;
		int l2 = 0;
		int prec=0;

		// Plot plot = new Plot("P11 " + title, xlabel, ylabel);
		Plot plot = new Plot("PL11 GRAFICO ", xlabel, ylabel);
		plot.setLineWidth(2);
		plot.setColor(Color.red);
		if (profilex1 != null && profiley1 != null) {
			l1 = profilex1.length;
			l2 = xx.length;
			IJ.log("AAA l1= " + l1 + " l2= " + l2);
			System.arraycopy(profilex1, 0, xx, prec, l1);
			System.arraycopy(profiley1, 0, yy, prec, l1);
			prec=prec+l1;
			plot.add("line", profilex1, profiley1);
		}

		plot.setColor(Color.green);
		if (profilex2 != null && profiley2 != null) {
			l1 = profilex2.length;
			l2 = xx.length;
			IJ.log("BBB l1= " + l1 + " l2= " + l2);
			System.arraycopy(profilex2, 0, xx, prec, l1);
			System.arraycopy(profiley2, 0, yy, prec, l1);
			prec=prec+l1;
		plot.add("line", profilex2, profiley2);
		}
		plot.setColor(Color.blue);
		if (profilex3 != null && profiley3 != null) {
			l1 = profilex3.length;
			l2 = xx.length;
			IJ.log("CCC l1= " + l1 + " l2= " + l2);
			System.arraycopy(profilex3, 0, xx, prec, l1 - 1);
			System.arraycopy(profiley3, 0, yy, prec, l1 - 1);
			prec=prec+l1;
			plot.add("line", profilex3, profiley3);
		}

		double[] a = Tools.getMinMax(xx);
		double[] b = Tools.getMinMax(yy);

		plot.setLimits(0, a[1] * 1.05, 0, b[1] * 1.05);
		plot.setColor(Color.BLUE);
//		plot.setFont(FontUtil.getFont("Times New Roman", Font.TRUETYPE_FONT, 16));
		plot.addLabel(0.05, 0.95, title);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);

		plot.show();
		Frame lw = WindowManager.getFrame(plot.getTitle());

		Dimension screen = IJ.getScreenSize();
//		MyLog.waitHere("SCREEN= " + screen.toString());

		if (lw != null) {
			lw.setLocation(10, screen.height / 2);
		}

		return;
	}

	/**
	 * 
	 * @param profilex1
	 * @param profiley1
	 * @param profilex2
	 * @param profiley2
	 * @param title
	 * @param xlabel
	 * @param ylabel
	 * @return
	 */
	public static Plot PL10_myPlotMultiple2(double[] profilex1, double[] profiley1, double[] profilex2,
			double[] profiley2, String title, String xlabel, String ylabel) {
		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		Plot plot = new Plot("PL10 " + title, xlabel, ylabel);

		plot.setColor(Color.red);
		plot.setLineWidth(1);
		plot.add("line", profilex1, profiley1);
		plot.add("circle", profilex1, profiley1);
		plot.setColor(Color.green);
		plot.add("line", profilex2, profiley2);
		plot.add("circle", profilex2, profiley2);

		double[] appx2 = Utility.concatArrays(profilex1, profilex2);
		double[] a = Tools.getMinMax(appx2);
		double[] appy2 = Utility.concatArrays(profiley1, profiley2);
		double[] b = Tools.getMinMax(appy2);
		plot.setLimits(0, a[1] * 1.05, 0, b[1] * 1.05);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		return plot;
	}

	/**
	 * 
	 * @param profilex1
	 * @param profiley1
	 * @param profilex2
	 * @param profiley2
	 * @param profilex3
	 * @param profiley3
	 * @param title
	 * @param xlabel
	 * @param ylabel
	 * @return
	 */
	public static Plot PL09_myPlotMultiple3(double[] profilex1, double[] profiley1, double[] profilex2,
			double[] profiley2, double[] profilex3, double[] profiley3, String title, String xlabel, String ylabel) {
		int PLOT_WIDTH = 600;

		int PLOT_HEIGHT = 350;

		Plot plot = new Plot("PL09 " + title, xlabel, ylabel);
		plot.setColor(Color.red);
		plot.add("line", profilex1, profiley1);
		plot.add("circle", profilex1, profiley1);
		plot.setColor(Color.green);
		plot.add("line", profilex2, profiley2);
		plot.add("circle", profilex2, profiley2);
		plot.setColor(Color.blue);
		plot.add("line", profilex3, profiley3);
		plot.add("circle", profilex3, profiley3);

		double[] appx2 = Utility.concatArrays(profilex1, profilex2);
		double[] appx3 = Utility.concatArrays(appx2, profilex3);
		double[] a = Tools.getMinMax(appx3);
		double[] appy2 = Utility.concatArrays(profiley1, profiley2);
		double[] appy3 = Utility.concatArrays(appy2, profiley3);
		double[] b = Tools.getMinMax(appy3);
		plot.setLimits(0, a[1] * 1.05, 0, b[1] * 1.05);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		return plot;
	}

	/**
	 * 
	 * @param profilex1
	 * @param profiley1
	 * @param profilex2
	 * @param profiley2
	 * @param profilex3
	 * @param profiley3
	 * @param title
	 * @param xlabel
	 * @param ylabel
	 * @return
	 */
	public static Plot PL08_myPlotMultipleSpecial1(double[] profilex1, double[] profiley1, double[] profilex2,
			double[] profiley2, double[] profilex3, double[] profiley3, String title, String xlabel, String ylabel) {
		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		Plot plot = new Plot("PL08 " + title, xlabel, ylabel);
		plot.setColor(Color.gray);
		plot.add("filled", profilex2, profiley2);
		plot.setColor(Color.red);
		plot.add("line", profilex2, profiley2);
		plot.setColor(Color.blue);
		plot.setLineWidth(4);
		plot.add("line", profilex3, profiley3);
		plot.setLineWidth(1);
		plot.setColor(Color.white);
		plot.add("filled", profilex1, profiley1);
		plot.setColor(Color.red);
		plot.add("line", profilex1, profiley1);

		double[] appx2 = Utility.concatArrays(profilex1, profilex2);
		double[] appx3 = Utility.concatArrays(appx2, profilex3);
		double[] a = Tools.getMinMax(appx3);
		double[] appy2 = Utility.concatArrays(profiley1, profiley2);
		double[] appy3 = Utility.concatArrays(appy2, profiley3);
		double[] b = Tools.getMinMax(appy3);
		plot.setLimits(0, a[1] * 1.05, 0, b[1] * 1.05);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		return plot;
	}

	/**
	 * 
	 * @param profilex
	 * @param profiley
	 * @param title
	 * @param xlabel
	 * @param ylabel
	 * @param color
	 * @return
	 */
	public static Plot PL07_myPlotSingle(double[] profilex, double[] profiley, String title, String xlabel,
			String ylabel, Color color) {
		double[] a = Tools.getMinMax(profilex);
		double[] b = Tools.getMinMax(profiley);

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		// Plot plot = new Plot(title, "pixel", "valore", profilex, profiley);
		Plot plot = new Plot("PL07 " + title, xlabel, ylabel);
		plot.setColor(color);
		plot.add("line", profilex, profiley);

		plot.setLimits(0, a[1] * 1.05, 0, b[1] * 1.05);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		return plot;
	}

	/**
	 * 
	 * @param profilex
	 * @param profiley
	 * @param title
	 * @param xlabel
	 * @param ylabel
	 * @param color
	 * @return
	 */
	public static Plot PL06_myPlotSingle2(double[] profilex, double[] profiley, String title, String xlabel,
			String ylabel, Color color) {
		double[] a = Tools.getMinMax(profilex);
		double[] b = Tools.getMinMax(profiley);
		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		// Plot plot = new Plot(title, "pixel", "valore", profilex, profiley);
		Plot plot = new Plot("PL06 " + title, xlabel, ylabel);
		plot.setColor(color);
		plot.add("line", profilex, profiley);
		plot.add("circle", profilex, profiley);

		plot.setLimits(0, a[1] * 1.05, 0, b[1] * 1.05);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		return plot;
	}

	/**
	 * Effettua il plot dei punti trovati, SENZA mostrare alcun fit
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static void PL05_MIRD_curvePlotterSpecialFlanagan(Regression reg, double[] x, double[] y, String title) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		int npoints = 1000;
		if (npoints < x.length)
			npoints = x.length; // or 2*x.length-1; for 2 values per data point
		if (npoints > 1000)
			npoints = 1000;
		double[] a = Tools.getMinMax(x);
		double xmin = a[0], xmax = a[1] * 1.5;
		xmin = 0;
		npoints = 1000;
		double[] b = Tools.getMinMax(y);
		double ymin = b[0], ymax = b[1] * 1.1; // y range of data points
		ymin = 0;
		double[] px = new double[npoints];
		double[] py = new double[npoints];
		double inc = (xmax - xmin) / (npoints - 1);
		double tmp = xmin;
		for (int i = 0; i < npoints; i++) {
			px[i] = tmp;
			tmp += inc;
		}
		double[] params = reg.getBestEstimates();

		double aux0 = 0;
		double aux1 = 0;

		aux0 = params[1];
		aux1 = params[0];

		MyLog.log("aux0= " + aux0 + " aux1= " + aux1);
		for (int i = 0; i < npoints; i++) {
			py[i] = aux0 * Math.exp(aux1 * px[i]);
			// MyLog.log("px[" + i + "]= " + px[i] + " py[" + i + "]= " + py[i]);
		}
		a = Tools.getMinMax(py);
		double dataRange = ymax - ymin;
		ymin = Math.max(ymin - dataRange, Math.min(ymin, a[0])); // expand y range for curve, but not too much
		ymax = Math.min(ymax + dataRange, Math.max(ymax, a[1]));

		double[] xx = new double[1];
		double[] yy = new double[1];

		Plot plot = new Plot("PL05 PLOT FLANAGAN", "ore dalla somministrazione", "attivita' MBq");
		plot.setLineWidth(2);
		plot.setColor(Color.ORANGE);
		plot.add("line", px, py);
		plot.setLimits(xmin, xmax, ymin, ymax);
		plot.setLineWidth(2);
		plot.setColor(Color.RED);
		xx[0] = x[0];
		yy[0] = y[0];
		plot.add("circle", xx, yy);
		plot.setColor(Color.GREEN);
		xx[0] = x[1];
		yy[0] = y[1];
		plot.add("circle", xx, yy);
		plot.setColor(Color.BLUE);
		xx[0] = x[2];
		yy[0] = y[2];
		plot.add("circle", xx, yy);

		plot.addLabel(0.05, 0.95, title);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);

		plot.show();
		Frame lw = WindowManager.getFrame(plot.getTitle());

		Dimension screen = IJ.getScreenSize();
//		MyLog.waitHere("SCREEN= " + screen.toString());

		if (lw != null) {
			lw.setLocation((screen.width - lw.getWidth()) - 10, screen.height / 2);
		}

	}

	/**
	 * Effettua il plot dei punti (selezionati e non selezionati) e mostra il fit
	 * fatto sui punti selezionati
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static void PL04_MIRD_curvePlotterSpecialImageJ(CurveFitter cf, double[] vetx, double[] vety, boolean[] selected,
			String title) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		int npoints = 1000;
		if (npoints < vetx.length)
			npoints = vetx.length; // or 2*x.length-1; for 2 values per data point
		if (npoints > 1000)
			npoints = 1000;
		double[] a = Tools.getMinMax(vetx);
		double xmin = a[0];
		double xmax = a[1] * 1.5;
		xmin = 0;
		npoints = 1000;
		double[] b = Tools.getMinMax(vety);
		double ymin = b[0];
		double ymax = b[1] * 1.1; // y range of data points
		ymin = 0;
		double[] px = new double[npoints];
		double[] py = new double[npoints];
		double inc = (xmax - xmin) / (npoints - 1);
		double tmp = xmin;
		for (int i = 0; i < npoints; i++) {
			px[i] = tmp;
			tmp += inc;
		}
		double[] params = cf.getParams();
		for (int i = 0; i < npoints; i++)
			py[i] = cf.f(params, px[i]);
		a = Tools.getMinMax(py);
		double dataRange = ymax - ymin;
		ymin = Math.max(ymin - dataRange, Math.min(ymin, a[0])); // expand y range for curve, but not too much
		ymax = Math.min(ymax + dataRange, Math.max(ymax, a[1]));

		Plot plot = new Plot("PL04 PLOT IMAGEJ", "ore dalla somministrazione", "attivita' MBq");
		plot.setLineWidth(2);
		plot.setColor(Color.ORANGE);
		plot.add("line", px, py);
		plot.setLimits(xmin, xmax, ymin, ymax);
		double[] xx = new double[1];
		double[] yy = new double[1];
		Color col = null;

		if (selected == null) {
			selected = new boolean[3];
			selected[0] = true;
			selected[1] = true;
			selected[2] = true;
		}

		for (int i1 = 0; i1 < selected.length; i1++) {
			if (selected[i1]) {
				if (i1 == 0)
					col = Color.red;
				if (i1 == 1)
					col = Color.green;
				if (i1 == 2)
					col = Color.blue;

				plot.setColor(col);
				xx[0] = vetx[i1];
				yy[0] = vety[i1];
				plot.add("circle", xx, yy);
			} else {
				plot.setColor(Color.black);
				xx[0] = vetx[i1];
				yy[0] = vety[i1];
				plot.add("circle", xx, yy);
			}

		}
		plot.setColor(Color.blue);
		StringBuffer legend = new StringBuffer(100);
		legend.append(cf.getName());
		legend.append('\n');
		legend.append(cf.getFormula());
		legend.append('\n');
		double[] p = cf.getParams();
		int n = cf.getNumParams();
		char pChar = 'a';
		for (int i = 0; i < n; i++) {
			legend.append(pChar + " = " + IJ.d2s(p[i], 5, 9) + '\n');
			pChar++;
		}
		legend.append("R^2 = " + IJ.d2s(cf.getRSquared(), 4));
		legend.append('\n');
		plot.addLabel(0.8, 0.1, legend.toString());

		plot.addLabel(0.05, 0.95, title);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);

		plot.show();

		Frame lw = WindowManager.getFrame(plot.getTitle());
		Dimension screen = IJ.getScreenSize();
		if (lw != null) {
			lw.setLocation((screen.width - lw.getWidth()) - 10, screen.height / 2 - lw.getHeight());
		}

		plot.show();

		return;
	}

	/**
	 * Effettua il plot dei punti trovati, MOSTRANDO i due fit ImageJ e Flanagan
	 * sovrapposti
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static void PL03_MIRD_curvePlotterSpecialCombined(CurveFitter cf, Regression reg, double[] x, double[] y) {

		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;

		int npoints = 400;
		if (npoints < x.length)
			npoints = x.length; // or 2*x.length-1; for 2 values per data point
		if (npoints > 1000)
			npoints = 1000;
		double[] a = Tools.getMinMax(x);
		double xmin = a[0], xmax = a[1] * 1.5;
		xmin = 0;
		npoints = 400;
		double[] b = Tools.getMinMax(y);
		double ymin = b[0], ymax = b[1] * 1.1; // y range of data points
		ymin = 0;

		// curva di FIT ottenuta da ImageJ

		double[] pxj = new double[npoints];
		double[] pyj = new double[npoints];
		double incj = (xmax - xmin) / (npoints - 1);
		double tmpj = xmin;
		for (int i = 0; i < npoints; i++) {
			if (i % 2 != 0)
				pxj[i] = tmpj;
			tmpj += incj;
		}
		double[] paramsj = cf.getParams();
		for (int i = 0; i < npoints; i++)
			if (i % 2 != 0)
				pyj[i] = cf.f(paramsj, pxj[i]);

		// curva di FIT ottenuta da Flanagan
		double[] pxf = new double[npoints];
		double[] pyf = new double[npoints];
		double incf = (xmax - xmin) / (npoints - 1);
		double tmpf = xmin;
		for (int i = 0; i < npoints; i++) {
			if (i % 2 == 0)
				pxf[i] = tmpf;
			tmpf += incf;
		}
		double[] paramsf = reg.getBestEstimates();

		double aux0f = paramsf[1];
		double aux1f = paramsf[0];

		for (int i = 0; i < npoints; i++) {
			if (i % 2 == 0)
				pyf[i] = (aux0f * Math.exp(aux1f * pxf[i]));
		}

		a = Tools.getMinMax(pyj);
		double dataRange = ymax - ymin;
		ymin = Math.max(ymin - dataRange, Math.min(ymin, a[0])); // expand y range for curve, but not too much
		ymax = Math.min(ymax + dataRange, Math.max(ymax, a[1]));
		Plot plot = new Plot("PL03 Comparazione ImageJ BLU e Flanagan VERDE", "X", "Y");
		plot.setLineWidth(2);
		plot.setColor(Color.BLUE);
		plot.add("dot", pxj, pyj);
		plot.setColor(Color.GREEN);
		plot.add("dot", pxf, pyf);
		plot.setLimits(xmin, xmax, ymin, ymax);
		plot.setLineWidth(2);
		plot.setColor(Color.RED);
		plot.add("circle", x, y);
		plot.setColor(Color.BLUE);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);

		plot.setColor(Color.BLUE);
		plot.show();
	}

	/**
	 * Effettua il plot dei punti trovati, mostrando anche il FIT
	 * 
	 * @param vetX
	 * @param vetY
	 * @param params
	 * @param npoints
	 */
	static void PL02_MIRD_curvePlotter(double[] vetX, double[] vetY, double[] params, int npoints) {

		double[] minMaxX = Tools.getMinMax(vetX);
		double[] minMaxY = Tools.getMinMax(vetY);
		double xmin = 0;
		double xmax = minMaxX[1] * 1.1;
		double ymin = -1.0;
		double ymax = minMaxY[1] * 1.1;
		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;
		double[] px = new double[npoints];
		// double[] py = new double[npoints];

		double inc = (xmax - xmin) / (npoints - 1);
		double tmp = minMaxX[0];
		for (int i = 0; i < npoints; i++) {
			px[i] = (float) tmp;
			tmp += inc;
		}

		Plot plot = new Plot("PL02 Punti", "ore dalla somministrazione", "attivita' MBq");
		plot.setLineWidth(2);
		plot.setColor(Color.red, Color.red);
		plot.setColor(Color.blue);
		plot.add("circle", vetX, vetY);
		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		plot.setLimits(xmin, xmax, ymin, ymax);
		plot.show();

	}

	/**
	 * Effettua il plot dei punti trovati, SENZA mostrare alcun fit
	 * 
	 * @param vetX
	 * @param vetY
	 */
	static void PL01_MIRD_pointsPlotter(double[] vetX, double[] vetY, boolean[] selected, String title, String label) {

		double[] minMaxX = Tools.getMinMax(vetX);
		double[] minMaxY = Tools.getMinMax(vetY);
		boolean[] neglected = { true, true, true };
		double xmin = 0;
		double xmax = minMaxX[1] * 1.1;
		double ymin = -1.0;
		double ymax = minMaxY[1] * 1.1;
		int PLOT_WIDTH = 600;
		int PLOT_HEIGHT = 350;
		double[] xx = new double[1];
		double[] yy = new double[1];

		if (selected == null)
			selected = neglected;

		Plot plot = new Plot("PL01 " + title, "ore dalla somministrazione", "attivita' MBq");
		plot.setLineWidth(2);
		Color col = null;

		for (int i1 = 0; i1 < selected.length; i1++) {
			if (selected[i1]) {
				if (i1 == 0)
					col = Color.red;
				if (i1 == 1)
					col = Color.green;
				if (i1 == 2)
					col = Color.blue;
				plot.setColor(col);
				xx[0] = vetX[i1];
				yy[0] = vetY[i1];
				plot.add("circle", xx, yy);
			} else {
				plot.setColor(Color.black);
				xx[0] = vetX[i1];
				yy[0] = vetY[i1];
				plot.add("circle", xx, yy);
			}

		}

		plot.setColor(Color.BLUE);
		plot.addLabel(0.05, 0.95, label);

		plot.setWindowSize(PLOT_WIDTH, PLOT_HEIGHT);
		plot.setLimits(xmin, xmax, ymin, ymax);
		plot.show();
		return;

	}

}
