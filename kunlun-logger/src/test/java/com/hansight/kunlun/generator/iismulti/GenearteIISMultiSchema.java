package com.hansight.kunlun.generator.iismulti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.generator.utils.GenUtils;

public class GenearteIISMultiSchema {
	private static final Logger LOG = LoggerFactory
			.getLogger(GenearteIISMultiSchema.class);

	public static void main(String[] args) throws Exception {
		if (args.length != 5) {
			System.out
					.println("Usage:<log dir> <dist dir> <log size> <date str> <interval time>");

			System.out.println("<interval time> unit:h, m, s");
			System.out
					.println("example: /root/cmb.new/sniffer-iis /grid/0/gen-sniffer-iis 400g 2014-11-27 24h");

			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		File logDir = new File(args[0]);
		String distDir = args[1];
		long dataLen = GenUtils.parseSizeWtihUnit(args[2]);
		Date startDate = sdf.parse(args[3]);
		long interval = GenUtils.parseDateWithUnit(args[4]);

		int days = 0;
		while (true) {
			Date distDate = DateUtils.addDays(startDate, days);
			gen(distDate, logDir, distDir, days, "UTF-8", dataLen);

			days++;
			LOG.info("over, sleep...");
			TimeUnit.MILLISECONDS.sleep(interval);
		}
	}

	public static void gen(Date distDate, File logDir, String distDir,
			int days, String encoding, long dataLen) throws IOException,
			ParseException {
		List<File> list = new ArrayList<>();
		GenUtils.listAll(logDir, list);
		long total = 0L;
		for (File file : list) {
			total += file.length();
		}
		int times = (int) ((dataLen + total - 1L) / total);

		BufferedWriter out = null;
		for (File file : list) {
			for (int i = 0; i < times; i++) {
				File distFile = getOutFile(i, distDir, distDate);

				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(distFile), encoding));

				gen(file, out, 1, distDate, encoding);
			}
			out.close();
		}
	}

	public static void gen(File file, BufferedWriter out, int headCounts,
			Date distDate, String encoding) throws IOException, ParseException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), encoding));

		int lineNumber = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Random random = new Random();
		Map<String, Integer> map = new HashMap<>();
		String line;
		while ((line = in.readLine()) != null) {
			lineNumber++;
			if (lineNumber == 3) {
				String[] schema = line.substring("#Fields: ".length()).split(
						" ");
				for (int j = 0; j < schema.length; j++) {
					map.put(schema[j], Integer.valueOf(j));
				}
				System.out.println(line);
			}
			if (lineNumber <= 3) {
				out.write(line);
				out.write("\n");
			} else {
				if (lineNumber > 3) {
					out.write(replace(line, sdf, distDate, random, map));
				}
				out.flush();
			}
		}
		in.close();
		LOG.debug("add file:{}", file.getName());
	}

	private static String replace(String line, SimpleDateFormat sdf,
			Date distDate, Random random, Map<String, Integer> map) {
		String[] tmp = line.split(" ");
		Integer dateIndex = (Integer) map.get("date");
		if (dateIndex != null) {
			tmp[dateIndex.intValue()] = sdf.format(distDate);
		}
		Integer timeIndex = (Integer) map.get("time");
		if (timeIndex != null) {
			tmp[timeIndex.intValue()] = GenUtils.randomTime(random);
		}
		Integer cipIndex = (Integer) map.get("c-ip");
		if (cipIndex != null) {
			tmp[cipIndex.intValue()] = GenUtils.randomIP(random);
		}
		return GenUtils.join(tmp, " ", "/n");
	}

	public static File getOutFile(int cur, String distDir, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String pre = sdf.format(date);
		String suf = GenUtils.preChar("0", cur, 6);
		File f = new File(new StringBuilder().append(distDir)
				.append(distDir.endsWith("/") ? "" : "/ex").append(pre)
				.append("_").append(suf).append(".log").toString());
		System.out.println(new StringBuilder().append("output:")
				.append(f.getName()).toString());
		return f;
	}
}