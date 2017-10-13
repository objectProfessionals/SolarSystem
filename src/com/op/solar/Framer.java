package com.op.solar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.PerspectiveFilter;

public class Framer extends Base {

	private static Framer framer = new Framer();

	private boolean small = false;

	private String mockFile0 = "A2";
	private String mockFile1 = "A2_ANGLE";
	private String mockFile2 = "A5";
	private String mockFile3 = "PC";
	private String mockFile4 = "WALL";
	private String mockFile5 = "WALL_B";
	private String mockFile6 = "A2_forWeb";
	private String mockFiles[] = { mockFile0, mockFile1, mockFile2, mockFile3, mockFile4, mockFile5, mockFile6 };

	private int mockType = 6;

	private String ipFileName = "MM_solar_2016-12-25_S_M";
	private String ipFile = ipFileName + ".png";
	private String srcDir = hostDir + "misc/solar/forWeb/";
	private String mockFile = mockFiles[mockType];
	private String opFile = ipFileName + "_" + mockFile + ".jpg";
	private int xMock = -1;
	private int yMock = -1;
	private int wMock = -1;
	private int hMock = -1;

	private String frFile = hostDir + "misc/solar/kickstarter/mocks/" + mockFile + ".jpg";

	private BufferedImage frbi;
	private BufferedImage ibi;
	private BufferedImage obi;
	private Graphics2D opG;
	private int w = -1;
	private int h = -1;
	private double dpi = 254;

	public static void main(String[] args) {
		try {
			framer.doFrame();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doFrame() throws IOException {
		System.out.println("Starting Framer...");
		initFiles();
		draw();
		if (mockType == 99) {
			// saveWeb();
		} else {
			save();
		}

		if (small) {
			opFile = ipFileName + "_" + mockFile + "_SMALL.jpg";
			BufferedImage orig = ibi;
			int ww = w / 8;
			int hh = h / 8;
			obi = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
			opG = (Graphics2D) obi.getGraphics();
			opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			opG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			opG.setColor(Color.WHITE);
			opG.fillRect(0, 0, ww, hh);

			opG.drawImage(orig, 0, 0, ww, hh, null);
			saveJPGFile(obi, srcDir + opFile, dpi, 0.95f);
		}
	}

	private void draw() throws IOException {

		if (mockType == 0) {
			xMock = 119;
			yMock = 116;
			wMock = 561;
			hMock = 444;
			double frx = (double) wMock / (double) w;
			double fry = (double) hMock / (double) h;
			AffineTransform at = new AffineTransform();
			at.translate(xMock, yMock);
			at.scale(frx, fry);
			opG.drawImage(blur(5), at, null);
		} else if (mockType == 1) {
			PerspectiveFilter pf = new PerspectiveFilter();
			pf.setCorners(246, 302, 1326, 302, 1434, 998, 315, 1148);

			AffineTransform at = new AffineTransform();
			at.translate(246, 302);
			BufferedImage im = pf.filter(ibi, null);

			GaussianFilter gf = new GaussianFilter(2);
			opG.drawImage(gf.filter(im, null), at, null);

		} else if (mockType == 2) {
			PerspectiveFilter pf = new PerspectiveFilter();
			pf.setCorners(383, 421, 1305, 420, 1316, 1035, 360, 1035);

			AffineTransform at = new AffineTransform();
			at.translate(360, 420);
			BufferedImage im = pf.filter(ibi, null);

			GaussianFilter gf = new GaussianFilter(2);
			opG.drawImage(gf.filter(im, null), at, null);
		} else if (mockType == 3) {
			xMock = 118;
			yMock = 155;
			wMock = 669;
			hMock = 377;
			double frx = (double) wMock / (double) w;
			double fry = (double) hMock / (double) h;
			AffineTransform at = new AffineTransform();
			at.translate(xMock, yMock);
			at.scale(frx, fry);
			opG.drawImage(blur(5), at, null);
		} else if (mockType == 4) {
			xMock = 64;
			yMock = 190;
			wMock = 399;
			hMock = 271;
			double frx = (double) wMock / (double) w;
			double fry = (double) hMock / (double) h;
			AffineTransform at = new AffineTransform();
			at.translate(xMock, yMock);
			at.scale(frx, fry);
			opG.drawImage(blur(5), at, null);
		} else if (mockType == 5) {
			PerspectiveFilter pf = new PerspectiveFilter();
			pf.setCorners(176, 430, 1122, 426, 1124, 1132, 180, 1128);

			AffineTransform at = new AffineTransform();
			at.translate(174, 424);
			BufferedImage im = pf.filter(ibi, null);

			GaussianFilter gf = new GaussianFilter(2);
			opG.drawImage(gf.filter(im, null), at, null);
		} else if (mockType == 99) {
			xMock = 344;
			yMock = 361;
			wMock = 797;
			double fr = (double) wMock / (double) w;
			AffineTransform at = new AffineTransform();
			at.translate(xMock, yMock);
			at.scale(fr, fr);
			opG.drawImage(blur(1), at, null);
		} else if (mockType == 6) {
			xMock = 224;
			yMock = 233;
			wMock = 1059;
			hMock = 742;
			double frx = (double) wMock / (double) w;
			double fry = (double) hMock / (double) h;
			AffineTransform at = new AffineTransform();
			at.translate(xMock, yMock);
			at.scale(frx, fry);
			opG.drawImage(blur(5), at, null);

		}
	}

	private void save() throws IOException {
		// savePNGFile(obi, srcDir + opFile, dpi);
		saveJPGFile(obi, srcDir + opFile, dpi, 0.95f);
	}

	// private void saveWeb() throws IOException {
	// // savePNGFile(obi, srcDir + opFile, dpi);
	// saveJPGFile(obi, opDirWeb + opFile, dpi, 0.75f);
	// }
	//
	private void initFiles() throws IOException {
		System.out.println("frFile=" + frFile);
		frbi = ImageIO.read(new File(frFile));
		int frw = frbi.getWidth();
		int frh = frbi.getHeight();

		ibi = ImageIO.read(new File(srcDir + ipFile));
		w = ibi.getWidth();
		h = ibi.getHeight();

		obi = new BufferedImage(frw, frh, BufferedImage.TYPE_INT_RGB);
		opG = (Graphics2D) obi.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, frw, frh);

		opG.drawImage(frbi, 0, 0, null);
	}

	private BufferedImage blur(int radius) {
		int size = radius * 2 + 1;
		float weight = 1.0f / (size * size);
		float[] data = new float[size * size];

		for (int i = 0; i < data.length; i++) {
			data[i] = weight;
		}

		Kernel kernel = new Kernel(size, size, data);
		ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		return op.filter(ibi, null);
	}
}
