package com.op.solar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class CombineReports extends Base {

	public static CombineReports combine = new CombineReports();
	private String dir = hostDir + "/misc/solar/kickstarter/reports/";
	private String all = "ALL.csv";
	private String[] rewardTypes = { "Solar Orbits Postcard", "Solar Orbits Folded Greeting Card",
			"Solar Orbits HD Wallpaper", "Solar Orbits 4K Wallpaper", "Solar Orbits Mug", "Small Solar Orbits Poster",
			"Large Solar Orbits Poster", "2 x Solar Orbits Mugs", "2 x Small Solar Orbits Poster",
			"3 x Solar Orbits Mugs", "2 x Large Solar Orbits Poster", "Extra Large Solar Orbits Poster",
			"3 x Small Solar Orbits Poster", "3 x Large Solar Orbits Poster", "2 x Extra Large Solar Orbits Poster" };

	public static void main(String[] args) throws Exception {
		combine.make();
	}

	private void make() {
		try {
			PrintWriter writer = new PrintWriter(dir + all, "UTF-8");

			File dirReports = new File(dir);
			if (dirReports.isDirectory()) {
				File files[] = dirReports.listFiles();
				boolean firstFile = true;
				for (File file : files) {
					if (!file.getName().startsWith(all) && !file.getName().startsWith("1.00")) {
						FileReader fr = new FileReader(file.getAbsolutePath());
						BufferedReader br = new BufferedReader(fr);
						String sCurrentLine;
						boolean firstLine = true;
						while ((sCurrentLine = br.readLine()) != null) {
							if (firstFile) {
								write(writer, sCurrentLine);
								firstFile = false;
								firstLine = false;
							} else {
								if (firstLine) {
									firstLine = false;
								} else {
									write(writer, sCurrentLine);
								}
							}
						}
					}
				}
			}
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private void write(PrintWriter writer, String sCurrentLine) throws IOException {
		System.out.println(sCurrentLine);
		writer.println(sCurrentLine);
	}

}
