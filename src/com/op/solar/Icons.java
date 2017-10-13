package com.op.solar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Icons extends Base {

	private static Icons icons = new Icons();

	private boolean small = false;

	private String ipFile = "grid.png";
	private String opFile = "out.png";
	private String frFile = hostDir + "misc/icons/" + ipFile;
	private String toFile = hostDir + "misc/icons/" + opFile;
	private BufferedImage ibi;
	private BufferedImage obi;
	private Graphics2D ipG;
	private Graphics2D opG;
	private double wmm = 400;
	private double hmm = 400;
	private double dpi = 600;
	private double mm2in = 25.4;
	private int w = (int) (dpi * wmm / mm2in);
	private int h = (int) (dpi * hmm / mm2in);

	public static void main(String[] args) {
		try {
			icons.doFrame();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doFrame() throws IOException {
		System.out.println("Starting Framer...");
		initFiles();
		draw();
		save();
	}

	private void draw() throws IOException {
		for (int i = 0; i < 5000; i++) {
			drawOne();
		}
	}

	private void drawOne() throws IOException {
		int x = 256 * (int) (Math.random() * 10);
		int y = 256 * (int) (Math.random() * 11);

		BufferedImage sub = ibi.getSubimage(x, y, 256, 256);

		int xx = (int) (Math.random() * (double) w);
		int yy = (int) (Math.random() * (double) h);

		double r = (Math.random() * 2 * Math.PI);
		AffineTransform at = new AffineTransform();

		AffineTransform rot = AffineTransform.getRotateInstance(r);
		AffineTransform tr = AffineTransform.getTranslateInstance(xx, yy);
		AffineTransform trB = AffineTransform.getTranslateInstance(-128, -128);
		double s = 0.9 + (Math.random() * 0.1);
		AffineTransform sc = AffineTransform.getScaleInstance(s, s);

		at.concatenate(tr);
		at.concatenate(rot);
		at.concatenate(sc);
		at.concatenate(trB);

		opG.drawImage(sub, at, null);
	}

	private void save() throws IOException {
		saveJPGFile(obi, toFile, dpi, 0.95f);
	}

	private void initFiles() throws IOException {
		ibi = ImageIO.read(new File(frFile));
		ipG = (Graphics2D) ibi.getGraphics();

		obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		opG = (Graphics2D) obi.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);
	}
}
