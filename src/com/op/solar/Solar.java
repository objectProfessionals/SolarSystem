package com.op.solar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
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
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class Solar extends Base {

	/*
	 * Small - 16 x 12 in (406 x 304 mm) Large - 20 x 16 in (508 x 406 mm) Extra
	 * Large - 40 x 30 in (1016 x 762 mm)
	 */
	private double wmm_S = 406;// 0
	private double hmm_S = 304;
	private double wmm_L = 508;// 1
	private double hmm_L = 406;
	private double wmm_XL = 1016;// 2
	private double hmm_XL = 762;
	private double wmm_GREETING = 210;// 3// 148;
	private double hmm_GREETNG = 148;// 105;
	private double wmm_POST = 210 * 2;// 4
	private double hmm_POST = 148 * 2;
	private double wmm_HD = 1920;// 5
	private double hmm_HD = 1080;
	private double wmm_4K = 3840;// 6
	private double hmm_4K = 2160;
	private double wmm_MUG = 420; // 7// 210
	private double hmm_MUG = 172; // 86
	private double[][] sizeArray = { { wmm_S, hmm_S }, { wmm_L, hmm_L }, { wmm_XL, hmm_XL },
			{ wmm_GREETING, hmm_GREETNG }, { wmm_POST, hmm_POST }, { wmm_HD, hmm_HD }, { wmm_4K, hmm_4K },
			{ wmm_MUG, hmm_MUG } };

	private double daysInterval = 0.1;

	private String endDateString = "2016-12-31";
	private String endTime = "18:36";
	private int selectedObject = 7;
	private int selectedStyle = 1;

	private String OBJ_S = "S";
	private String OBJ_L = "L";
	private String OBJ_XL = "X";
	private String OBJ_GREETING = "G";
	private String OBJ_POST = "P";
	private String OBJ_HD = "H";
	private String OBJ_4K = "K";
	private String OBJ_MUG = "M";
	private String[] objects = { OBJ_S, OBJ_L, OBJ_XL, OBJ_GREETING, OBJ_POST, OBJ_HD, OBJ_4K, OBJ_MUG };
	private double[] zooms = { wmm_S / wmm_L, 1.05, wmm_XL / wmm_L, wmm_GREETING / wmm_L, wmm_POST / wmm_L, 0.3, 0.6,
			0.5 };

	private String dawn = "D";
	private String midnight = "M";
	private String sunset = "S";
	private String daylight = "W";
	private String black = "B";
	private String[] styles = { dawn, midnight, sunset, daylight, black };
	private Color[] timeStampColors = { Color.WHITE, Color.WHITE, Color.WHITE, Color.GRAY, Color.WHITE };

	private Color bgStartM = Color.decode("#00000e");
	private Color bgEndM = Color.decode("#0a0a36");
	private Color bgStartD = Color.decode("#e0eaff");
	private Color bgEndD = Color.decode("#8da1cb");
	private Color bgStartS = Color.decode("#960f0f");
	private Color bgEndS = Color.decode("#7d0a0a");
	private Color bgStartW = Color.decode("#ffffff");
	private Color bgEndW = Color.decode("#dddddd");
	private Color bgStartB = Color.decode("#252525");
	private Color bgEndB = Color.decode("#00001d");
	private Color[][] bgs = { { bgStartD, bgEndD }, { bgStartM, bgEndM }, { bgStartS, bgEndS }, { bgStartW, bgEndW },
			{ bgStartB, bgEndB } };

	private String shade = null; // styles[selectedStyle];
	private Color timestampColor = null; // timeStampColors[selectedStyle];
	private double wmm = 0; // sizeArray[selectedObject][0];
	private double hmm = 0; // sizeArray[selectedObject][1];
	private String object = null; // objects[selectedObject];
	private Color bgStart = null; // bgs[selectedStyle][0];
	private Color bgEnd = null; // bgs[selectedStyle][1];
	private double zoom = 0; // zF * zooms[selectedObject];// 400 inner - 25
								// outer
	private double planetRadWhole = 0; // zoom / 2;
	private double planetRadEnd = 0; // zoom / 5;
	private double planetRadStart = 0; // zoom / 25;
	private double sunRad = 0; // (planetRadEnd * 10);

	private double zF = 80;

	private double marginOffF = 1.0 / 12.0;
	private int pageWidth = -1;
	private int pageHeight = -1;

	private double zFactor = 5; // smaller = more spread

	private String png = ".png";
	private String jpg = ".jpg";
	private String prefix = null;
	private String opFile = null; // "solar_" + endDateString + "_" + object +
									// "_" + shade + png;
	private String mw = "MilkyWay.jpg";

	private String fontFile = "FONTS/SLIMBOLD.ttf";
	// private String fontFile = "FONTS/SCRIPT_BOLD.ttf";
	private float fontScale = 100;// A5
	private String dir = null;// "misc/solar/";
	private String[] planetNames = { "Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune",
			"Pluto" };
	// private Color planetColors[] = { Color.WHITE, Color.GREEN.darker(),
	// Color.BLUE.brighter(), Color.RED, Color.YELLOW.darker(),
	// Color.CYAN, Color.PINK, Color.ORANGE, Color.GREEN };
	private String sunColor1 = "#f5be3f";
	private String sunColor = "#f5d53f";

	private Color planetColors1[] = { Color.decode("#7b6f61"), Color.decode("#b06773"), Color.decode("#3b9fbb"),
			Color.decode("#93372d"), Color.decode("#be762a"), Color.decode("#c1ab6c"), Color.decode("#94c7ac"),
			Color.decode("#2e65d9"), Color.decode("#9f9284") };
	private Color planetColors[] = { Color.decode("#7b6f61"), Color.decode("#b06773"), Color.decode("#3b77cf"),
			Color.decode("#b82617"), Color.decode("#e88011"), Color.decode("#c1ab6c"), Color.decode("#94c7ac"),
			Color.decode("#3b9fbb"), Color.decode("#9f9284") };

	private double scaling[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private double periods[] = { 0.241, 0.615, 1.0, 1.881, 11.86, 29.46, 84.01, 164.8, 248.5 };
	// https://nssdc.gsfc.nasa.gov/planetary/factsheet/planet_table_ratio.html
	// private double paletaryOrbitsActuals[] = { 0.387, 0.723, 1, 1.52, 5.20,
	// 9.58, 19.20, 30.05, 39.48 };
	private double paletaryOrbits[] = { 0.5, 1, 1.25, 1.85, 5.20, 9.58, 18.85, 30.05, 39.48 };
	private double mugPaletaryOrbitsF[] = { 2, 6, 8, 8, 5, 4.25, 3, 2.15, 1 };
	// private double mugPaletaryOrbitsF[] = { 1, 2, 3, 2.75, 1.5, 1.25, 0.9,
	// 0.85, 0.8 };
	// private double radiiActuals[] = { 0.383, 0.949, 1, 0.2724, 0.532, 11.21,
	// 9.45, 4.01, 3.88, 0.186 };
	private double planetRadii[] = { 0.5, 0.9, 1, 0.5, 1.5, 1.35, 1, 0.75, 0.75 };
	private double planetRadiiMugF[] = { 0.45, 0.75, 1, 0.75, 1.75, 1.6, 1, 0.9, 0.7 };
	private double planetRadiiFadeF[] = { 1.25, 1.25, 1.25, 1.25, 1.25, 1.25, 1.25, 1.25, 1.25 };
	// private double planetRadiiMugF[] = { 0.9, 1.5, 2, 1.5, 3.5, 3.2, 2, 1.8,
	// 1.4 };
	// private Color bgColor = Color.BLACK;
	private Color bgColor = Color.WHITE;

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
	private double dpi = 300;
	private double mm2in = 25.4;
	private Graphics2D opG;
	private Stroke defaultStroke = null;
	private double centreXF = 0.44;
	private int centerX;
	private int centerY;

	private static Solar solar;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private Date dateEnd = null;

	public static void main(String[] args) throws IOException, ParseException, FontFormatException {
		solar = new Solar("MM", "1980-09-21", "", "S", "W", "forWeb/");
		solar.drawSolar();
	}

	public Solar() {

	}

	public Solar(String prefix, String date, String time, String size, String style, String path) {
		this.prefix = prefix;
		this.endDateString = date;
		this.endTime = time;
		this.selectedObject = indexOf(objects, size);
		this.selectedStyle = indexOf(styles, style);
		dir = "misc/solar/" + path;
	}

	private int indexOf(String[] arr, String chosen) {
		int i = 0;
		for (String val : arr) {
			if (val.equals(chosen)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public void drawSolar() {
		initSelections();
		initAll();

		System.out.println("ww,hh: " + pageWidth + "," + pageHeight);
		zFactor = 999999;

		drawSun(false);
		for (int p = 0; p < 9; p++) {
			double totDays = (periods[p] * 365.25);
			System.out.println(planetNames[p]);
			drawPaths(p, totDays);
		}

		drawSun(true);
		drawTimestamp();
		if (isJpgOrPng()) {
			saveJPGFile(obi, hostDir + dir + opFile, dpi, 1f);
		} else {
			if (object.equals(OBJ_GREETING)) {
				// obi = scaleForPrint(1.40);
			}
			savePNGFile(obi, hostDir + dir + opFile, dpi);
		}
	}

	private BufferedImage scaleForPrint(double sc) {
		int pw = (int) (((double) pageWidth) * sc);
		int ph = (int) (((double) pageHeight) * sc);
		int x = (pw - pageWidth) / 2;
		int y = (ph - pageHeight) / 2;
		int imgType = BufferedImage.TYPE_INT_ARGB;
		BufferedImage obi2 = new BufferedImage(pw, ph, imgType);
		Graphics2D opG2 = (Graphics2D) obi2.getGraphics();
		opG2.drawImage(obi, x, y, null);
		return obi2;
	}

	private boolean isJpgOrPng() {
		return (object.equals(OBJ_HD) || object.equals(OBJ_4K));
	}

	private void initSelections() {
		shade = styles[selectedStyle];
		timestampColor = timeStampColors[selectedStyle];
		wmm = sizeArray[selectedObject][0];
		hmm = sizeArray[selectedObject][1];
		object = objects[selectedObject];
		bgStart = bgs[selectedStyle][0];
		bgEnd = bgs[selectedStyle][1];
		zoom = zF * zooms[selectedObject];// 400 inner - 25 outer

		planetRadWhole = zoom / 2;
		planetRadEnd = zoom / 5;
		planetRadStart = zoom / 25;
		sunRad = (planetRadEnd * 12);

		String p = prefix == "" ? "" : prefix + "_";
		String suff = png;
		if (isJpgOrPng()) {
			suff = jpg;
		}
		opFile = p + "solar_" + endDateString + "_" + object + "_" + shade + suff;

		if (!"".equals(endTime)) {
			daysInterval = 0.01;
		}
		try {
			dateEnd = formatter.parse(endDateString);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void initAll() {
		initSize();

		coord();
		horizon();
		elem();
		initFiles();
		initOrbits();
		setupFont();

		drawBackground();
	}

	private void initSize() {
		if (object.equals(OBJ_MUG)) {
			double zF = 2.0;
			planetRadWhole = zF * zoom / 2;
			planetRadEnd = zF * zoom / 5;
			planetRadStart = zF * zoom / 25;
			sunRad = (planetRadEnd * 7.5);

			pageWidth = (int) (dpi * wmm / mm2in);
			pageHeight = (int) (dpi * hmm / mm2in);
		} else if (isJpgOrPng()) {
			pageWidth = (int) (wmm);
			pageHeight = (int) (hmm);
		} else {
			pageWidth = (int) (dpi * wmm / mm2in);
			pageHeight = (int) (dpi * hmm / mm2in);
		}
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

	private void drawTimestamp() {
		String dateString = getFormattedDate(dateEnd);
		if (endTime != null && !endTime.equals("")) {
			dateString = dateString + " " + endTime;
		}
		opG.setColor(Color.WHITE);

		GlyphVector v = opG.getFont().createGlyphVector(opG.getFontMetrics().getFontRenderContext(), dateString);
		Rectangle2D bounds = v.getVisualBounds();
		if (object.equals(OBJ_MUG)) {
			double scale = 2.0;
			AffineTransform orig = opG.getTransform();

			AffineTransform rot = AffineTransform.getRotateInstance(Math.PI * 1.5);
			int off = pageWidth / 100;
			int x = (int) (pageWidth - off);
			int y = (int) (pageHeight / 2 + scale * bounds.getWidth() / 2);
			AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
			AffineTransform at = new AffineTransform();
			AffineTransform sc = AffineTransform.getScaleInstance(scale, scale);
			at.concatenate(tr);
			at.concatenate(rot);
			at.concatenate(sc);

			opG.setColor(timestampColor);
			opG.setTransform(at);
			opG.fill(v.getOutline());

			opG.setTransform(orig);
			opG.setColor(Color.WHITE);
		} else {
			int off = (int) ((double) pageWidth * marginOffF);
			int x = (int) (pageWidth - bounds.getWidth() - off);
			int y = (int) (pageHeight - off);

			AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
			AffineTransform at = new AffineTransform();
			at.concatenate(tr);

			opG.setTransform(at);
			opG.fill(v.getOutline());
		}
	}

	private void drawBackground() {
		Paint before = opG.getPaint();
		LinearGradientPaint paint = null;
		if (object.equals(OBJ_MUG)) {
			Point2D start = new Point2D.Float(pageWidth / 2, 0);
			Point2D end = new Point2D.Float(pageWidth / 2, pageHeight);
			float[] dist = { 0.0f, 1.0f };
			Color[] colors = { bgStart, bgEnd };
			paint = new LinearGradientPaint(start, end, dist, colors);
		} else {
			Point2D start = new Point2D.Float(0, 0);
			Point2D end = new Point2D.Float(pageHeight / 2, pageHeight);
			float[] dist = { 0.0f, 1.0f };
			Color[] colors = { bgStart, bgEnd };
			paint = new LinearGradientPaint(start, end, dist, colors);
		}
		opG.setPaint(paint);
		opG.fillRect(0, 0, pageWidth, pageHeight);
		opG.setPaint(before);
	}

	private void drawMWBackground() throws IOException {
		BufferedImage mwbi = ImageIO.read(new File(hostDir + dir + mw));
		opG.drawImage(mwbi, 0, 0, pageWidth, pageHeight, null);
	}

	private void initOrbits() {
		for (double i = 0; i < 9; i++) {
			scaling[(int) i] = (3 + (2.5 * i)) / paletaryOrbits[(int) i];
		}
	}

	private void initFiles() {
		int imgType = BufferedImage.TYPE_INT_ARGB;
		if (isJpgOrPng()) {
			imgType = BufferedImage.TYPE_INT_RGB;
		}
		obi = new BufferedImage(pageWidth, pageHeight, imgType);
		opG = (Graphics2D) obi.getGraphics();
		defaultStroke = opG.getStroke();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(bgColor);
		opG.fillRect(0, 0, pageWidth, pageHeight);
		centerX = (int) (((double) pageWidth) * centreXF);
		centerY = pageHeight / 2;
		opG.setColor(Color.BLACK);
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
		for (double i = totDays; i >= 0; i = i - daysInterval) {
			drawPath(i, p, totDays);
		}
	}

	private void drawSun(boolean top) {
		if (object.equals(OBJ_MUG)) {
			Color sunC = Color.decode(sunColor);
			Color flareC = getRimPlanetColor(sunC);
			int sr = (int) (sunRad);
			opG.setColor(flareC);
			opG.fillRect(0, (int) pageHeight - sr, (int) pageWidth, (int) pageHeight);
			opG.setColor(sunC);
			sr = (int) (sunRad * 0.75);
			opG.fillRect(0, (int) pageHeight - sr, (int) pageWidth, (int) pageHeight);
		} else {
			drawSunHalf(top, true);
			drawSunHalf(top, false);
		}
	}

	private void drawSunHalf(boolean top, boolean flare) {
		double[] iso = getISOCoordsFlat(0, (int) (zoom * 0.6), 0, 1);
		int x = (int) iso[0];
		int y = (int) iso[1];

		int sr = (int) sunRad;
		Color sunC = Color.decode(sunColor);
		Color flareC = getRimPlanetColor(sunC);
		if (flare) {
			sr = (int) (sunRad);
			opG.setColor(flareC);
		} else {
			sr = (int) (sunRad * 0.75);
			opG.setColor(sunC);
		}
		Ellipse2D.Double sun = new Ellipse2D.Double(centerX - sr + x, centerY - sr + y, sr * 2, sr * 2);
		Area aSun = new Area(sun);
		Rectangle2D.Double block;
		if (top) {
			block = new Rectangle2D.Double(centerX - sr + x, centerY + y, sr * 2, sr);
		} else {
			block = new Rectangle2D.Double(centerX - sr + x, centerY - sr + y, sr * 2, sr);
		}
		aSun.subtract(new Area(block));
		opG.fill(aSun);

	}

	private void setupFont() {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(hostDir + fontFile));
			Font font = Font.createFont(Font.TRUETYPE_FONT, is);
			font = font.deriveFont(fontScale * (float) (zoom / zF));
			opG.setFont(font);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (FontFormatException e) {
			throw new RuntimeException(e);
		}
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

		if (object.equals(OBJ_MUG)) {
			drawOrbitsForMug(p, x, y, z, offDaysFromStart, totDays);
		} else {
			drawOrbits(p, x, y, z, offDaysFromStart, totDays);
			// if (offDaysFromStart % 365 == 0) {
			// drawYearSheet(offDaysFromStart);
			// }
		}
	}

	private void drawOrbitsForMug(int p, double[] x, double[] y, double[] z, double offDaysFromStart, double totDays) {
		double xx = (x[p] * zoom);
		double yy = (-y[p] * zoom);
		double zz = (-z[p] * zoom);

		double prs = planetRadStart;
		double pre = planetRadEnd;
		Color planetColor = planetColors[p];

		double ang = Math.atan2(yy, xx);
		double xp = (0.5 + (Math.PI - ang) / (2 * Math.PI)) % 1.0;
		double rOrbit = Math.sqrt(xx * xx + yy * yy);

		double xPos = pageWidth * xp;
		int xxx = (int) xPos;
		int yyy = (int) (pageHeight - sunRad * 1.25 - 0.5 * rOrbit * mugPaletaryOrbitsF[p]);
		if (p == 8) {
			yyy = (int) (pageHeight - sunRad * 1.25 - pageHeight * 0.4 - 0.5 * rOrbit * mugPaletaryOrbitsF[p]);

		}
		double rrr = (prs + (pre * (totDays - offDaysFromStart) / totDays));
		int rad = (int) rrr;

		if (offDaysFromStart < daysInterval) {
			prs = planetRadWhole * planetRadiiMugF[p];
			pre = planetRadWhole * planetRadiiMugF[p];
			rrr = (prs + (pre * (totDays - offDaysFromStart) / totDays));

			rad = (int) (rrr * 0.666);
			opG.setColor(planetColor);
			opG.fillOval(xxx - rad, yyy - rad, rad * 2, rad * 2);

			rad = (int) rrr;
			planetColor = getFadePlanetColor(p);

			opG.setColor(planetColor);
			opG.fillOval(xxx - rad, yyy - rad, rad * 2, rad * 2);

			if (p == 5) {
				// saturn
				opG.setColor(planetColors[p]);
				int ringRad = (int) (((double) rad) * 0.66);
				opG.setStroke(new BasicStroke((float) (ringRad / 3)));
				opG.drawOval(xxx - ringRad * 2, yyy - ringRad / 2, ringRad * 4, ringRad);
				opG.setStroke(defaultStroke);
			} else if (p == 2) {
				// earth - moon
				Color o = opG.getColor();
				Color m = Color.decode("#aaaaaa");
				Color mO = getRimPlanetColor(m);

				int rO = rad / 2;
				opG.setColor(mO);
				opG.fillOval(xxx - rad - rO, yyy - rad - rO, rO * 2, rO * 2);

				int rr = rad / 4;
				opG.setColor(m);
				opG.fillOval(xxx - rad - rr, yyy - rad - rr, rr * 2, rr * 2);

				opG.setColor(o);
			}

		} else {
			opG.setColor(planetColor);
			opG.fillOval(xxx - rad, yyy - rad, rad * 2, rad * 2);
		}
	}

	void drawOrbits(int p, double[] x, double[] y, double[] z, double offDaysFromStart, double totDays) {
		double xx = (x[p] * zoom);
		double yy = (-y[p] * zoom);
		double zz = (-z[p] * zoom);

		double r = Math.sqrt(xx * xx + yy * yy);

		double prs = planetRadStart;
		double pre = planetRadEnd;
		Color planetColor = getFadePlanetColor(p);
		if (offDaysFromStart < daysInterval) {
			prs = planetRadWhole * planetRadii[p];
			pre = planetRadWhole * planetRadii[p];
		}
		double scale = scaling[p];
		double[] iso = getISOCoordsFlat(xx, yy, zz, scale);

		int xxx = (int) iso[0];
		int yyy = (int) (iso[1] - offDaysFromStart / zFactor);

		opG.setColor(planetColor);
		int radOrig = (int) ((prs + (pre * (totDays - offDaysFromStart) / totDays)));
		int radPlanet = (int) (0.75 * ((double) radOrig));
		int radFade = (int) (planetRadiiFadeF[p] * ((double) radOrig));
		opG.fillOval(centerX + xxx - radFade, centerY + yyy - radFade, radFade * 2, radFade * 2);
		if (offDaysFromStart < daysInterval) {
			opG.setColor(planetColors[p]);
			if (p == 5) {
				// saturn
				opG.setStroke(new BasicStroke((float) (radPlanet / 5)));
				opG.drawOval(centerX + xxx - radPlanet * 2, centerY + yyy - radPlanet / 2, radPlanet * 4, radPlanet);
				opG.setStroke(defaultStroke);
			} else if (p == 2) {
				// earth - moon
				Color o = opG.getColor();
				Color m = Color.decode("#aaaaaa");
				Color mO = getRimPlanetColor(m);

				int rO = radPlanet / 2;
				opG.setColor(mO);
				opG.fillOval(centerX + xxx - radPlanet - rO, centerY + yyy - radPlanet - rO, rO * 2, rO * 2);

				int rr = radPlanet / 4;
				opG.setColor(m);
				opG.fillOval(centerX + xxx - radPlanet - rr, centerY + yyy - radPlanet - rr, rr * 2, rr * 2);

				opG.setColor(o);
			}

			opG.fillOval(centerX + xxx - radPlanet, centerY + yyy - radPlanet, radPlanet * 2, radPlanet * 2);
		}

		if (offDaysFromStart == totDays - 1) {
			opG.setColor(planetColors[p]);
			// opG.drawString(pname[p], centerX + xxx, centerY + yyy);

			// drawName(p, xxx, yyy, zz);

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

	}

	private void drawName(int p, int xxx, int yyy, double zz) {
		GlyphVector v = opG.getFont().createGlyphVector(opG.getFontMetrics().getFontRenderContext(), planetNames[p]);
		Shape s = v.getOutline();
		Path2D.Double newShape = new Path2D.Double();

		PathIterator pathIt = s.getPathIterator(null);
		int c = 0;
		double isox1 = 0;
		double isoy1 = 0;
		while (!pathIt.isDone()) {
			float[] coords = { 0, 0, 0, 0, 0, 0, 0, 0 };
			pathIt.currentSegment(coords);
			double[] isoP = getISOCoordsStanding(coords[0], coords[1], zz, 1);
			double isox = isoP[0] + centerX + xxx;
			double isoy = isoP[1] + centerY + yyy;
			if (c == 0) {
				newShape.moveTo(isox, isoy);
				isox1 = isox;
				isoy1 = isoy;
			} else {
				newShape.lineTo(isox, isoy);
			}
			if (!pathIt.isDone()) {
				pathIt.next();
			}

			c++;
		}
		newShape.lineTo(isox1, isoy1);

		opG.fill(newShape);
	}

	private Color getFadePlanetColor(int p) {
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

	private double[] getISOCoordsStanding(double xx, double yy, double zz, double scale) {

		double a = -Math.atan(0.5);
		double s = 1; // the edge-width of one cubic cell in the world.
		double newX = (xx - yy) * Math.cos(a) * s;
		double newY = ((xx + yy) * Math.sin(a) - zz) * s;
		double[] arr = { newX * scale, -newY * scale };
		return arr;
	}

	/**
	 * 
	 */

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
		case 8: // Pluto p.i = 17.14175
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