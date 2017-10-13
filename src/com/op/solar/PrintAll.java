package com.op.solar;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PrintAll extends Base {

	public static PrintAll combine = new PrintAll();
	private String dir = hostDir + "/misc/solar/kickstarter/reports/";
	private String all = "ALL.csv";
	// private String[] rewardTypes = { "Solar Orbits Folded Greeting Card" };
	private String[] rewardTypes = { "Solar Orbits Postcard", "12.00 GBP Solar Orbits Folded Greeting Card",
			"Solar Orbits HD Wallpaper", "Solar Orbits 4K Wallpaper", "16.00 GBP Solar Orbits Mug",
			"20.00 GBP Small Solar Orbits Poster", "25.00 GBP Large Solar Orbits Poster", "2 x Solar Orbits Mugs",
			"2 x Small Solar Orbits Poster", "3 x Solar Orbits Mugs", "2 x Large Solar Orbits Poster",
			"45.00 GBP Extra Large Solar Orbits Poster", "3 x Small Solar OrbitsPoster",
			"3 x Large Solar Orbits Poster", "2 x Extra Large Solar Orbits Poster" };
	private long start = new Date().getTime();
	private boolean create = false;

	public static void main(String[] args) {
		try {
			combine.make();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void make() throws Exception {

		int lineCount = 0;
		File dirReports = new File(dir);
		if (dirReports.isDirectory()) {
			for (String reward : rewardTypes) {
				File[] files = dirReports.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File arg0, String arg1) {
						return arg1.contains(reward);
					}
				});
				if (files.length != 1) {
					System.out.println("***MISSED=" + reward);
					continue;
				}
				File file = files[0];
				System.out.println(file.getName());
				List<List<String>> data = ReadCSV.read(file.getAbsolutePath());

				boolean firstLine = true;
				int startCol = 0;
				for (List<String> row : data) {
					List<String> cols = row;
					if (firstLine) {
						int i = 0;
						for (String col : cols) {
							if (col.equals("date1")) {
								startCol = i;
								break;
							}
							i++;
						}
						firstLine = false;
					} else {
						lineCount = lineCount + readFromDateAndDraw(create, cols, startCol, 0, row);

						lineCount = lineCount + readFromDateAndDraw(create, cols, startCol, 1, row);
						lineCount = lineCount + readFromDateAndDraw(create, cols, startCol, 2, row);
						lineCount = lineCount + readFromDateAndDraw(create, cols, startCol, 3, row);
					}
				}
			}
			long now = new Date().getTime();
			System.out.println("lineCount=" + lineCount + " TAKEN " + ((now - start) / 1000));
		}
		System.out.println("COMPLETED");
	}

	private int readFromDateAndDraw(boolean create, List<String> cols, int startCol, int offset, List<String> row)
			throws ParseException {
		int off = offset * 3;
		SimpleDateFormat iformatter = new SimpleDateFormat("dd/MM/yy");
		SimpleDateFormat oformatter = new SimpleDateFormat("yyyy-MM-dd");
		if (startCol + off >= cols.size()) {
			// System.out.println("****NO MORE DATES" + row.get(0));
			return 0;
		}
		String dateStr = cols.get(startCol + off);
		if (dateStr == null || dateStr.isEmpty()) {
			// System.out.println("****NO DATE FOUND " + row.get(0) + " BLOCK="
			// + (offset + 1));
			return 0;
		}
		String style = cols.get(startCol + off + 1);
		String size = cols.get(startCol + off + 2);
		// if (!size.equals("X")) {
		// return 0;
		// }

		Date d1 = iformatter.parse(dateStr);
		String date = oformatter.format(d1);

		int timePos = dateStr.indexOf(";");
		String time = "";
		if (timePos > 0) {
			time = dateStr.substring(timePos + 1);
		}
		System.out.println(row.get(0) + "_solar_" + date + "_" + size + "_" + style);
		long now = new Date().getTime();
		// System.out.println("TAKEN " + ((now - start) / 1000));

		if (create) {
			String prefix = cols.get(0);
			Solar solar = new Solar(prefix, date, time, size, style, "Kickstarter/backers/");
			solar.drawSolar();
		}
		return 1;

	}

}
