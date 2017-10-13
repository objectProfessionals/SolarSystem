package com.op.solar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.jhlabs.image.ShadowFilter;

//import com.op.paint.services.SharedUtils;

public class SolarLayers extends Base {

	private String opFile = "solarLAY";
	private String png = ".png";
	private String mw = "MilkyWay.jpg";
	private String endDate = "2014-03-30";
	private String fontFile = "FONTS/SLIMBOLD.ttf";
	// private String fontFile = "FONTS/SCRIPT_BOLD.ttf";
	private float fontScale = 100;// A5
	private String dir = "misc/solar/";
	private String[] planetNames = { "Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune",
			"Pluto" };
	// private Color planetColors[] = { Color.WHITE, Color.GREEN.darker(),
	// Color.BLUE.brighter(), Color.RED, Color.YELLOW.darker(),
	// Color.CYAN, Color.PINK, Color.ORANGE, Color.GREEN };
	private String sunColor = "#f5be3f";
	private Color planetColors[] = { Color.decode("#7b6f61"), Color.decode("#b06773"), Color.decode("#3b9fbb"),
			Color.decode("#93372d"), Color.decode("#be762a"), Color.decode("#c1ab6c"), Color.decode("#94c7ac"),
			Color.decode("#2e65d9"), Color.decode("#9f9284") };
	private double scaling[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private double periods[] = { 0.241, 0.615, 1.0, 1.881, 11.86, 29.46, 84.01, 164.8, 247.9 };
	private double maxRads[] = { 1, 1, 1, 1, 1, 1, 1, 1, 1 };
	private double orbits[] = { 0.387, 0.723, 1, 1.52, 5.20, 9.58, 19.20, 30.05, 39.48 };
	private double radii1[] = { 0.383, 0.949, 1, 0.2724, 0.532, 11.21, 9.45, 4.01, 3.88, 0.186 };
	private double radii[] = { 0.5, 0.9, 1, 0.5, 2, 1.75, 1.5, 1.5, 0.75 };
	// private Color bgColor = Color.BLACK;
	private Color bgColor = Color.WHITE;
	private Color bgStart = Color.decode("#e0eaff");// Color.BLUE;00000e
	private Color bgEnd = Color.decode("#8da1cb");// Color.BLUE;

	private double DEGS = 180 / Math.PI; // convert radians to degrees
	private double RADS = Math.PI / 180; // convert degrees to radians
	private double EPS = 1.0e-12; // machine error constant

	private double ra;
	private double dec;
	private double rvec;
	private double alt;
	private double az;

	private double a;
	private double e;
	private double i;
	private double O;
	private double w;
	private double L;

	private BufferedImage obi;
	private double dpi = 600;
	private double mm2in = 25.4;
	private double wmm = 297;
	private double hmm = 210;
	private int pageWidth = 0;
	private int pageHeight = 0;
	private Graphics2D opG;
	private int centerX;
	private int centerY;

	private static SolarLayers solar;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private Date dateEnd = new Date();

	private double zoom = 90;// 400 inner - 25 outer
	private double planetRadWhole = zoom / 2.0;
	private double planetRadEnd = zoom / 3.0;
	private double planetRadStart = zoom / 50.0;
	private double sunRad = (planetRadEnd * 5);
	private double zFactor = 5; // smaller = more spread

	ArrayList<Point3D> insides = new ArrayList<Point3D>();
	ArrayList<Point3D> outsides = new ArrayList<Point3D>();

	public static void main(String[] args) throws IOException, ParseException, FontFormatException {
		solar = new SolarLayers();
		// solar.start();
		solar.startSingle();
	}

	private void startSingle() throws IOException, ParseException, FontFormatException {
		initAll();

		System.out.println("ww,hh: " + pageWidth + "," + pageHeight);

		zFactor = 999999;
		dateEnd = formatter.parse(endDate);
		drawTimestamp();

		drawSun();

		for (int i = 0; i < 9; i++) {
			insides = new ArrayList<Point3D>();
			outsides = new ArrayList<Point3D>();

			double totDays = (periods[i] * 365.25);
			drawPaths(i, totDays);
			// System.out.println(planetNames[i] + " : " + maxRads[i]);

			Polygon poly = new Polygon();
			for (int j = 0; j < insides.size(); j++) {
				Point3D in = insides.get(j);
				double[] iso = getISOCoordsFlat(in.x, in.y, in.z, 1);
				int yyy = (int) (iso[1] - j / zFactor);
				poly.addPoint(centerX + (int) iso[0], centerY + yyy);
			}

			Collections.reverse(outsides);
			for (int j = 0; j < outsides.size(); j++) {
				Point3D out = outsides.get(j);
				double[] iso = getISOCoordsFlat(out.x, out.y, out.z, 1);
				int yyy = (int) (iso[1] - j / zFactor);
				poly.addPoint(centerX + (int) iso[0], centerY + yyy);
			}

			opG.setColor(planetColors[i]);

			Area all = new Area(poly);

			double rr = 2 * planetRadEnd * radii[i];
			Point3D in = insides.get(0);
			double[] iso = getISOCoordsFlat(in.x, in.y, in.z, 1);
			double yyy = (iso[1]);
			Ellipse2D pla = new Ellipse2D.Double(centerX + iso[0] - rr, centerY + yyy - rr, rr * 2, rr * 2);
			all.add(new Area(pla));

			if (i == 5) {
				// saturn
				rr = 2 * planetRadEnd * radii[i];
				double rr2 = 1.5 * planetRadEnd * radii[i];
				in = insides.get(0);
				iso = getISOCoordsFlat(in.x, in.y, in.z, 1);
				yyy = (iso[1]);
				Ellipse2D sati = new Ellipse2D.Double(centerX + iso[0] - 2 * rr2, centerY + yyy - rr2 / 2, 4 * rr2,
						rr2);
				Ellipse2D sat = new Ellipse2D.Double(centerX + iso[0] - 2 * rr, centerY + yyy - rr / 2, 4 * rr, rr);

				Area allSat = new Area(sat);
				allSat.subtract(new Area(sati));
				all.add(new Area(allSat));
			} else if (i == 2) {
				// earth - moon
				double rad = 2 * planetRadEnd * radii[i];
				rr = 2 * 0.33 * planetRadEnd * radii[i];
				in = insides.get(0);
				iso = getISOCoordsFlat(in.x, in.y, in.z, 1);
				yyy = (iso[1]);
				Ellipse2D moon = new Ellipse2D.Double(centerX + iso[0] - rad - rr, centerY + yyy - rad - rr, 2 * rr,
						2 * rr);
				all.add(new Area(moon));

				// Color o = opG.getColor();
				// Color m = Color.decode("#aaaaaa");

			}

			paintShadow(i, all);
			opG.fill(all);
		}

		save();
	}

	private void drawTimestamp() throws ParseException {
		Date endDateD = formatter.parse(endDate);

		String dateString = getFormattedDate(endDateD);
		opG.setColor(Color.WHITE);
		GlyphVector v = opG.getFont().createGlyphVector(opG.getFontMetrics().getFontRenderContext(), dateString);
		Rectangle2D bounds = v.getVisualBounds();
		int off = pageWidth / 20;
		int x = (int) (pageWidth - bounds.getWidth() - off);
		int y = (int) (pageHeight - off);
		// opG.drawString(dateString, x, y);

		AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
		for (int ii = 0; ii < v.getNumGlyphs(); ii++) {
			v.setGlyphTransform(ii, tr);
		}
		Area text = new Area(v.getOutline());
		paintShadow(4, text);
		opG.fill(text);
	}

	private void drawSun() {
		opG.setColor(Color.decode(sunColor));
		double[] iso = getISOCoordsFlat(0, (int) (zoom * 0.6), 0, 1);
		int x = (int) iso[0];
		int y = (int) iso[1];
		int sr = (int) sunRad;
		Ellipse2D s = new Ellipse2D.Double(centerX + x - sr, centerY + y - sr, sr * 2, sr * 2);
		Area sun = new Area(s);

		paintShadow(0, sun);
		opG.fill(sun);
	}

	private void paintShadow(int p, Area a) {
		double shrinkage = 100 + (p + 1) * 10;
		double shadowX = -shrinkage / 2.0;
		double shadowY = -shrinkage / 2.0;
		double filterShadowRad = shrinkage / 2.0;
		float filterShadowAlpha = 0.75f;

		double filterRad = Math.sqrt(shadowX * shadowX + shadowY * shadowY);
		if (filterShadowRad > -1) {
			filterRad = filterShadowRad;
		}
		ShadowFilter filter = new ShadowFilter((int) filterRad, (int) shadowX, (int) shadowY, filterShadowAlpha);
		BufferedImage bi = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		g.setPaint(getGradientPaint(planetColors[p]));
		g.fill(a);

		System.out.println("Start filter: =" + filterRad + ":" + p);
		filter.filter(bi, obi);
		System.out.println("filtered: =" + p);
	}

	private GradientPaint getGradientPaint(Color col) {
		Color col1 = col.brighter();
		Color col2 = col.darker();
		GradientPaint gp = new GradientPaint(pageWidth, 0, col1, 0, pageHeight, col2);
		return gp;
	}

	private void initAll() throws IOException, ParseException, FontFormatException {
		pageWidth = (int) (dpi * wmm / mm2in);
		pageHeight = (int) (dpi * hmm / mm2in);

		coord();
		horizon();
		elem();
		initFiles();
		initOrbits();
		setupFont();

		drawBackground();
		// drawMWBackground();
	}

	public TreeMap<Integer, String> romanNumerals(int year) {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();

		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");

		return map;
	}

	public String toRoman(int year) {
		TreeMap<Integer, String> map = romanNumerals(year);
		int l = map.floorKey(year);
		if (year == l) {
			return map.get(year);
		}
		return map.get(l) + toRoman(year - l);
	}

	public static String getFormattedDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		// 2nd of march 2015
		int day = cal.get(Calendar.DATE);

		String f = "EEEEEE MMMMM d'%s' yyyy";

		if (!((day > 10) && (day < 19)))
			switch (day % 10) {
			case 1:
				return new SimpleDateFormat(String.format(f, "st")).format(date);
			case 2:
				return new SimpleDateFormat(String.format(f, "nd")).format(date);
			case 3:
				return new SimpleDateFormat(String.format(f, "rd")).format(date);
			default:
				return new SimpleDateFormat(String.format(f, "th")).format(date);
			}

		return new SimpleDateFormat(String.format(f, "th")).format(date);
	}

	private void drawBackground() {
		Paint before = opG.getPaint();
		Point2D start = new Point2D.Float(pageWidth, 0);
		Point2D end = new Point2D.Float(pageHeight / 2, pageHeight);
		float[] dist = { 0.0f, 1.0f };
		Color[] colors = { bgStart, bgEnd };
		LinearGradientPaint paint = new LinearGradientPaint(start, end, dist, colors);

		opG.setPaint(paint);
		opG.fillRect(0, 0, pageWidth, pageHeight);
		opG.setPaint(before);
	}

	private void drawMWBackground() throws IOException {
		BufferedImage mwbi = ImageIO.read(new File(hostDir + dir + mw));
		opG.drawImage(mwbi, 0, 0, pageWidth, pageHeight, null);
	}

	private void initOrbits() {
		double off = 3;
		double mult = 2.5;
		for (double i = 0; i < 9; i++) {
			scaling[(int) i] = (off + (mult * i)) / orbits[(int) i];
		}
	}

	private void initFiles() throws IOException {
		obi = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB);
		opG = (Graphics2D) obi.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(bgColor);
		opG.fillRect(0, 0, pageWidth, pageHeight);
		centerX = -400 + pageWidth / 2;
		centerY = pageHeight / 2;
		opG.setColor(Color.BLACK);
	}

	private void save() throws IOException {
		String src = hostDir + dir;
		// String src = "out/";
		File op1 = new File(src + opFile + "_" + endDate + png);
		System.out.println(op1.getAbsolutePath());
		ImageIO.write(obi, "png", op1);
		System.out.println("Saved " + op1.getPath());
	}

	double parseFloat(String val) {
		return Double.parseDouble(val);
	}

	// right ascension, declination coordinate structure
	void coord() {
		ra = parseFloat("0"); // right ascension [deg]
		dec = parseFloat("0"); // declination [deg]
		rvec = parseFloat("0"); // distance [AU]
	}

	// altitude, azimuth coordinate structure
	void horizon() {
		alt = parseFloat("0"); // altitude [deg]
		az = parseFloat("0"); // azimuth [deg]
	}

	// orbital element structure
	void elem() {
		a = parseFloat("0"); // semi-major axis [AU]
		e = parseFloat("0"); // eccentricity of orbit
		i = parseFloat("0"); // inclination of orbit [deg]
		O = parseFloat("0"); // longitude of the ascending node [deg]
		w = parseFloat("0"); // longitude of perihelion [deg]
		L = parseFloat("0"); // mean longitude [deg]
	}

	private void drawPaths(int p, double totDays) {
		for (double i = totDays; i >= 0; i = i - 0.25) {
			drawPath(i, p, totDays);
		}
	}

	private void setupFont() throws FontFormatException, IOException {
		// Map<TextAttribute, Object> attributes = new HashMap<TextAttribute,
		// Object>();
		// Font currentFont = opG.getFont();
		// attributes.put(TextAttribute.FAMILY, currentFont.getFamily());
		// attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
		// attributes.put(TextAttribute.SIZE, (int) (currentFont.getSize() *
		// fontScale));
		// Font myFont = Font.getFont(attributes);
		// opG.setFont(myFont);

		InputStream is = new BufferedInputStream(new FileInputStream(hostDir + fontFile));
		Font font = Font.createFont(Font.TRUETYPE_FONT, is);
		font = font.deriveFont(fontScale);

		opG.setFont(font);

	}

	void drawPath(double offDaysFromStart, int p, double totDays) {
		int year = dateEnd.getYear() + 1900;
		int month = dateEnd.getMonth() + 1;
		int day = dateEnd.getDate();
		int hour = (int) (dateEnd.getHours() - 24.0 * offDaysFromStart);
		int mins = dateEnd.getMinutes();
		int secs = dateEnd.getSeconds();

		// compute day number for date/time
		double dn = day_number(year, month, day, hour, mins);

		// compute location of objects
		double[] x = new double[9];
		double[] y = new double[9];
		double[] z = new double[9];

		Planet obj = new Planet();
		get_coord(obj, p, dn);

		x[p] = obj.xh;
		y[p] = obj.yh;
		z[p] = obj.zh;

		// drawOrbits(first, last, x, y, z, offDaysFromStart, totDays);
		drawOrbitsLayers(p, x, y, z, offDaysFromStart, totDays);
		if (offDaysFromStart % 365 == 0) {
			// drawYearSheet(offDaysFromStart);
		}
	}

	void drawYearSheet(double offDaysFromStart) {
		int l = 500;
		double[] isoBR = getISOCoordsFlat(l, -l, 0, 1);
		double[] isoTR = getISOCoordsFlat(l, l, 0, 1);
		double[] isoTL = getISOCoordsFlat(-l, l, 0, 1);
		double[] isoBL = getISOCoordsFlat(-l, -l, 0, 1);

		int x1 = (int) isoBR[0];
		int y1 = (int) (isoBR[1] - offDaysFromStart / zFactor);
		int x2 = (int) isoTR[0];
		int y2 = (int) (isoTR[1] - offDaysFromStart / zFactor);
		int x3 = (int) isoTL[0];
		int y3 = (int) (isoTL[1] - offDaysFromStart / zFactor);
		int x4 = (int) isoBL[0];
		int y4 = (int) (isoBL[1] - offDaysFromStart / zFactor);

		Path2D.Double p = new Path2D.Double();
		p.moveTo(centerX + x1, centerY + y1);
		p.lineTo(centerX + x2, centerY + y2);
		p.lineTo(centerX + x3, centerY + y3);
		p.lineTo(centerX + x4, centerY + y4);
		opG.setColor(new Color(200, 200, 200, 25));

		opG.fill(p);

		// if (offDaysFromStart == totDays - 1) {
		// opG.setColor(Color.BLACK);
		// opG.drawString(pname[p], centerX + xxx, centerY + yyy);
		// }
	}

	void drawOrbitsLayers(int p, double[] x, double[] y, double[] z, double offDaysFromStart, double totDays) {

		double prs = planetRadStart;
		double pre = planetRadEnd;
		// if (offDaysFromStart < 0.25) {
		// prs = planetRadWhole * radii[p];
		// pre = planetRadWhole * radii[p];
		// }
		double xx = (x[p] * zoom);
		double yy = (-y[p] * zoom);
		double zz = (-z[p] * zoom);

		double r = Math.sqrt(xx * xx + yy * yy);
		if (r > maxRads[p]) {
			maxRads[p] = r;
		}

		double rad = (prs + (pre * (totDays - offDaysFromStart) / totDays));
		double orbitRad = scaling[p];

		double angRad = Math.atan2(yy, xx);
		double x1 = orbitRad * xx + rad * Math.cos(angRad);
		double y1 = orbitRad * yy + rad * Math.sin(angRad);
		double x2 = orbitRad * xx - rad * Math.cos(angRad);
		double y2 = orbitRad * yy - rad * Math.sin(angRad);

		insides.add(new Point3D(x1, y1, zz));
		outsides.add(new Point3D(x2, y2, zz));
	}

	void drawOrbits(int first, int last, double[] x, double[] y, double[] z, double offDaysFromStart, double totDays) {
		for (int p = first; p < last; p++) {
			double xx = (x[p] * zoom);
			double yy = (-y[p] * zoom);
			double zz = (-z[p] * zoom);

			double r = Math.sqrt(xx * xx + yy * yy);
			if (r > maxRads[p]) {
				maxRads[p] = r;
			}

			double prs = planetRadStart;
			double pre = planetRadEnd;
			Color planetColor = getPlanetColor(p);
			if (offDaysFromStart < 0.25) {
				prs = planetRadWhole * radii[p];
				pre = planetRadWhole * radii[p];
			}
			double scale = scaling[p];

			double[] iso = getISOCoordsFlat(xx, yy, zz, scale);

			int xxx = (int) iso[0];
			int yyy = (int) (iso[1] - offDaysFromStart / zFactor);

			opG.setColor(planetColor);
			int rad = (int) (prs + (pre * (totDays - offDaysFromStart) / totDays));
			opG.fillOval(centerX + xxx - rad, centerY + yyy - rad, rad * 2, rad * 2);
			if (offDaysFromStart < 0.25) {
				opG.setColor(planetColors[p]);
				rad = rad / 2;
				if (p == 5) {
					Stroke o = opG.getStroke();
					// saturn
					opG.setStroke(new BasicStroke((float) (rad / 5)));
					opG.drawOval(centerX + xxx - rad * 2, centerY + yyy - rad / 2, rad * 4, rad);
					opG.setStroke(o);
				} else if (p == 2) {
					// earth - moon
					Color o = opG.getColor();
					Color m = Color.decode("#aaaaaa");
					Color mO = getRimPlanetColor(m);

					int rO = rad / 2;
					opG.setColor(mO);
					opG.fillOval(centerX + xxx - rad - rO, centerY + yyy - rad - rO, rO * 2, rO * 2);

					int rr = rad / 4;
					opG.setColor(m);
					opG.fillOval(centerX + xxx - rad - rr, centerY + yyy - rad - rr, rr * 2, rr * 2);

					opG.setColor(o);
				}

				opG.fillOval(centerX + xxx - rad, centerY + yyy - rad, rad * 2, rad * 2);
			}

			if (offDaysFromStart == totDays - 1) {
				opG.setColor(planetColors[p]);
				// opG.drawString(pname[p], centerX + xxx, centerY + yyy);

				// drawName(p, xxx, yyy, zz);

			}
		}
	}

	private Color getPlanetColor(int p) {
		Color orig = planetColors[p];
		return getRimPlanetColor(orig);
	}

	private Color getRimPlanetColor(Color c) {
		int alpha = 50;
		Color newC = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		return newC;
	}

	private double[] getISOCoordsFlat(double xx, double yy, double zz, double scale) {

		double a = -Math.atan(0.5);
		double s = 1; // the edge-width of one cubic cell in the world.
		double newX = (xx - yy) * Math.cos(a) * s;
		double newY = ((xx + yy) * Math.sin(a) - zz) * s;
		double[] arr = { newX * scale, -newY * scale };
		return arr;
	}

	// day number to/from J2000 (Jan 1.5, 2000)
	double day_number(double y, double m, double d, double hour, double mins) {
		double h = hour + mins / 60;
		double rv = 367 * y - Math.floor(7 * (y + Math.floor((m + 9) / 12)) / 4) + Math.floor(275 * m / 9) + d
				- 730531.5 + h / 24;
		// rv = d - 32075 + 1461 * (y + 4800 + (m - 14) / 12) / 4 + 367
		// * (m - 2 - (m - 14) / 12 * 12) / 12 - 3
		// * ((y + 4900 + (m - 14) / 12) / 100) / 4;
		return rv;
	}

	// compute RA, DEC, and distance of planet-p for day number-d
	// result returned in structure obj in degrees and astronomical units
	void get_coord(Planet planet, int p, double d) {
		mean_elements(planet, p, d);
		double ap = planet.a;
		double ep = planet.e;
		double ip = planet.i;
		double op = planet.O;
		double pp = planet.w;
		double lp = planet.L;

		Planet earth = new Planet();
		mean_elements(earth, 2, d);
		double ae = earth.a;
		double ee = earth.e;
		double ie = earth.i;
		double oe = earth.O;
		double pe = earth.w;
		double le = earth.L;

		// position of Earth in its orbit
		double me = mod2pi(le - pe);
		double ve = true_anomaly(me, ee);
		double re = ae * (1 - ee * ee) / (1 + ee * Math.cos(ve));

		// heliocentric rectangular coordinates of Earth
		double xe = re * Math.cos(ve + pe);
		double ye = re * Math.sin(ve + pe);
		double ze = 0.0;

		// position of planet in its orbit
		double mp = mod2pi(lp - pp);
		double vp = true_anomaly(mp, planet.e);
		double rp = ap * (1 - ep * ep) / (1 + ep * Math.cos(vp));

		// heliocentric rectangular coordinates of planet
		double xh = rp * (Math.cos(op) * Math.cos(vp + pp - op) - Math.sin(op) * Math.sin(vp + pp - op) * Math.cos(ip));
		double yh = rp * (Math.sin(op) * Math.cos(vp + pp - op) + Math.cos(op) * Math.sin(vp + pp - op) * Math.cos(ip));
		double zh = rp * (Math.sin(vp + pp - op) * Math.sin(ip));

		// if (p == 2) // earth --> compute sun
		// {
		// xh = 0;
		// yh = 0;
		// zh = 0;
		// }

		// convert to geocentric rectangular coordinates
		double xg = xh - xe;
		double yg = yh - ye;
		double zg = zh - ze;

		// rotate around x axis from ecliptic to equatorial coords
		double ecl = 23.439281 * RADS; // value for J2000.0 frame
		double xeq = xg;
		double yeq = yg * Math.cos(ecl) - zg * Math.sin(ecl);
		double zeq = yg * Math.sin(ecl) + zg * Math.cos(ecl);

		// find the RA and DEC from the rectangular equatorial coords
		planet.ra = mod2pi(Math.atan2(yeq, xeq)) * DEGS;
		planet.dec = Math.atan(zeq / Math.sqrt(xeq * xeq + yeq * yeq)) * DEGS;
		planet.rvec = Math.sqrt(xeq * xeq + yeq * yeq + zeq * zeq);

		planet.xh = xh;
		planet.yh = yh;
		planet.zh = zh;
	}

	// Compute the elements of the orbit for planet-i at day number-d
	// result is returned in structure p
	void mean_elements(Planet p, int i, double d) {
		double cy = d / 36525; // centuries since J2000

		switch (i) {
		case 0: // Mercury
			p.a = 0.38709893 + 0.00000066 * cy;
			p.e = 0.20563069 + 0.00002527 * cy;
			p.i = (7.00487 - 23.51 * cy / 3600) * RADS;
			p.O = (48.33167 - 446.30 * cy / 3600) * RADS;
			p.w = (77.45645 + 573.57 * cy / 3600) * RADS;
			p.L = mod2pi((252.25084 + 538101628.29 * cy / 3600) * RADS);
			break;
		case 1: // Venus
			p.a = 0.72333199 + 0.00000092 * cy;
			p.e = 0.00677323 - 0.00004938 * cy;
			p.i = (3.39471 - 2.86 * cy / 3600) * RADS;
			p.O = (76.68069 - 996.89 * cy / 3600) * RADS;
			p.w = (131.53298 - 108.80 * cy / 3600) * RADS;
			p.L = mod2pi((181.97973 + 210664136.06 * cy / 3600) * RADS);
			break;
		case 2: // Earth/Sun
			p.a = 1.00000011 - 0.00000005 * cy;
			p.e = 0.01671022 - 0.00003804 * cy;
			p.i = (0.00005 - 46.94 * cy / 3600) * RADS;
			p.O = (-11.26064 - 18228.25 * cy / 3600) * RADS;
			p.w = (102.94719 + 1198.28 * cy / 3600) * RADS;
			p.L = mod2pi((100.46435 + 129597740.63 * cy / 3600) * RADS);
			break;
		case 3: // Mars
			p.a = 1.52366231 - 0.00007221 * cy;
			p.e = 0.09341233 + 0.00011902 * cy;
			p.i = (1.85061 - 25.47 * cy / 3600) * RADS;
			p.O = (49.57854 - 1020.19 * cy / 3600) * RADS;
			p.w = (336.04084 + 1560.78 * cy / 3600) * RADS;
			p.L = mod2pi((355.45332 + 68905103.78 * cy / 3600) * RADS);
			break;
		case 4: // Jupiter
			p.a = 5.20336301 + 0.00060737 * cy;
			p.e = 0.04839266 - 0.00012880 * cy;
			p.i = (1.30530 - 4.15 * cy / 3600) * RADS;
			p.O = (100.55615 + 1217.17 * cy / 3600) * RADS;
			p.w = (14.75385 + 839.93 * cy / 3600) * RADS;
			p.L = mod2pi((34.40438 + 10925078.35 * cy / 3600) * RADS);
			break;
		case 5: // Saturn
			p.a = 9.53707032 - 0.00301530 * cy;
			p.e = 0.05415060 - 0.00036762 * cy;
			p.i = (2.48446 + 6.11 * cy / 3600) * RADS;
			p.O = (113.71504 - 1591.05 * cy / 3600) * RADS;
			p.w = (92.43194 - 1948.89 * cy / 3600) * RADS;
			p.L = mod2pi((49.94432 + 4401052.95 * cy / 3600) * RADS);
			break;
		case 6: // Uranus
			p.a = 19.19126393 + 0.00152025 * cy;
			p.e = 0.04716771 - 0.00019150 * cy;
			p.i = (0.76986 - 2.09 * cy / 3600) * RADS;
			p.O = (74.22988 - 1681.40 * cy / 3600) * RADS;
			p.w = (170.96424 + 1312.56 * cy / 3600) * RADS;
			p.L = mod2pi((313.23218 + 1542547.79 * cy / 3600) * RADS);
			break;
		case 7: // Neptune
			p.a = 30.06896348 - 0.00125196 * cy;
			p.e = 0.00858587 + 0.00002510 * cy;
			p.i = (1.76917 - 3.64 * cy / 3600) * RADS;
			p.O = (131.72169 - 151.25 * cy / 3600) * RADS;
			p.w = (44.97135 - 844.43 * cy / 3600) * RADS;
			p.L = mod2pi((304.88003 + 786449.21 * cy / 3600) * RADS);
			break;
		case 8: // Pluto
			p.a = 39.48168677 - 0.00076912 * cy;
			p.e = 0.24880766 + 0.00006465 * cy;
			p.i = (17.14175 + 11.07 * cy / 3600) * RADS;
			p.O = (110.30347 - 37.33 * cy / 3600) * RADS;
			p.w = (224.06676 - 132.25 * cy / 3600) * RADS;
			p.L = mod2pi((238.92881 + 522747.90 * cy / 3600) * RADS);
			break;
		default:
			//
		}
	}

	// compute the true anomaly from mean anomaly using iteration
	// M - mean anomaly in radians
	// e - orbit eccentricity
	double true_anomaly(double M, double e) {
		double V, E1;

		// initial approximation of eccentric anomaly
		double E = M + e * Math.sin(M) * (1.0 + e * Math.cos(M));

		do // iterate to improve accuracy
		{
			E1 = E;
			E = E1 - (E1 - e * Math.sin(E1) - M) / (1 - e * Math.cos(E1));
		} while (Math.abs(E - E1) > EPS);

		// convert eccentric anomaly to true anomaly
		V = 2 * Math.atan(Math.sqrt((1 + e) / (1 - e)) * Math.tan(0.5 * E));

		if (V < 0)
			V = V + (2 * Math.PI); // modulo 2pi

		return V;
	}

	// return an angle in the range 0 to 2pi radians
	double mod2pi(double x) {
		return x % (2 * Math.PI);
		// double b = x / (2 * Math.PI);
		// double a = (2 * Math.PI) * (b - Math.floor((b)));
		// if (a < 0)
		// a = (2 * Math.PI) + a;
		// return a;
	}

	// format two digits with leading zero if needed
	String d2(double n) {
		if ((n < 0) || (99 < n))
			return "xx";
		return "" + ((n < 10) ? ("0" + n) : n);
	}

	public class Planet {

		public double xh;
		public double yh;
		public double zh;
		public double a;
		public double e;
		public double i;
		public double w;
		public double O;
		public double L;
		public double ra;
		public double dec;
		public double rvec;

	}
}