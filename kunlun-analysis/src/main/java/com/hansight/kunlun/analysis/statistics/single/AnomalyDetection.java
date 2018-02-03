package com.hansight.kunlun.analysis.statistics.single;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.hansight.kunlun.analysis.statistics.exception.ParserException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.hansight.kunlun.analysis.statistics.model.LogWritable;
import com.hansight.kunlun.analysis.statistics.model.SessionId;
import com.hansight.kunlun.analysis.statistics.model.TimeTotalMaps;
import com.hansight.kunlun.analysis.utils.IPFilter;
import com.hansight.kunlun.analysis.utils.PropertiesUtils;
import com.hansight.kunlun.analysis.utils.quantile.QDigest;

/**
 * Author:zhhui DateTime:2014/5/20 15:31 .
 */
public class AnomalyDetection {
	private static final int FIELD_date = 0;
	private static final int FIELD_time = 1;
	private static final int FIELD_cs_uri_stem = 6;
	private static final int FIELD_cs_uri_query = 7;
	private static final int FIELD_s_port = 8;
	private static final int FIELD_c_ip = 10;
	private static final int FIELD_cs_User_Agent_ = 12;
	private static final int FIELD_cs_Cookie_ = 13;
	private static final int FIELD_cs_Referer_ = 14;
	private static final int FIELD_s_host = 15;
	private static final int FIELD_sc_status = 16;

	public static void map(String readFilePath, String mapTO)
			throws IOException, ParserException, ParseException {
		com.hansight.kunlun.analysis.statistics.model.SessionSplitter splitter = new com.hansight.kunlun.analysis.statistics.model.SessionSplitter();
		PropertiesUtils.loadFile("session_splitter.properties");
		Properties properties = PropertiesUtils.getDatas();
		for (Object entry : properties.values()) {
			String[] vale = entry.toString().split("//");
			splitter.setIDFields(Boolean.parseBoolean(vale[1]), vale[0]);
		}
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(readFilePath)));
		BufferedWriter out;
		String line;
		// int i = 0;
		while ((line = in.readLine()) != null) {
			// i++;
			if (line.startsWith("#")) {
				continue;
			}
			String[] log = line.split(" ");
			// 过滤内网IP
			if (IPFilter.isInnerIP(log[FIELD_c_ip]))
				continue;
			SessionId ids = splitter.getSessionId(log[FIELD_c_ip],
					log[FIELD_cs_Cookie_]);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(format.parse(log[FIELD_date] + " "
					+ log[FIELD_time]));
			Long time = calendar.getTimeInMillis();
			File file = new File(mapTO + "/" + ids.getIp() + ".map");
			if (!file.exists()) {
				file.createNewFile();
			}
			StringBuilder url = new StringBuilder();
			url.append("443".equals(log[FIELD_s_port]) ? "https://" : "http://");
			String host = log[FIELD_s_host];

			url.append(log[FIELD_s_host]);
			url.append(log[FIELD_cs_uri_stem]);
			url.append("-".equals(log[FIELD_cs_uri_query]) ? ""
					: ("?" + log[FIELD_cs_uri_query]));

			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));
			out.append(String.valueOf(ids.getIp())).append("--_")
					.append(time.toString()).append("--_").append(url)
					.append("--_").append(log[FIELD_sc_status]).append("--_")
					.append(log[FIELD_cs_User_Agent_]).append("--_")
					.append(String.valueOf(ids.getCookeId())).append("--_")
					.append(log[FIELD_c_ip]).append("--_");

			out.newLine();
			out.flush();
			out.close();
		}
		in.close();
	}

	public static void reduce(String reduceFrom, String outFilePath)
			throws IOException, ParserException {
		File dir = new File(reduceFrom);
		if (!dir.isDirectory())
			throw new IllegalArgumentException("must be a Directory");
		File outFile = new File(outFilePath);
		if (!outFile.exists()) {
			outFile.createNewFile();
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outFile)));
		String line;
		File[] files = dir.listFiles();
		if (files == null)
			return;
		// -----------------------------------------------------
		for (File file : files) {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			ArrayList<LogWritable> logs = new ArrayList<>();
			ArrayList<LogWritable> roughs = new ArrayList<>();
			while ((line = in.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				String[] log = line.split("--_");
				// System.out.println("file = " + file.getName()+":"+log[5]);
				LogWritable log1 = new LogWritable();

				log1.setCip(Long.valueOf(log[0]));
				String time = log[1];
				if (time != null && !"".equals(time)) {
					log1.setTime(Long.parseLong(time));
				}

				String refererValue = log[2];

				if (refererValue != null && !"null".equals(refererValue)) {
					log1.setUrl(refererValue);
				}
				String statusValue = log[3];
				if (statusValue != null) {
					log1.setStatus(Integer.parseInt(statusValue));
				}
				refererValue = log[4];
				if (refererValue != null) {
					log1.setUserAgent(refererValue);
				}
				logs.add(log1);
				if (!"0".equals(log[5])) {
					log1.setCookieId(Long.valueOf(log[5]));
					boolean noRoughLogs = true;
					for (int i = 0; i < roughs.size(); i++) {
						LogWritable p = roughs.get(i);
						if (p == null)
							continue;
						// System.out.println("p = " + p.getUserAgent() + ":" +
						// log.getUserAgent());
						if (LogWritable.equal(p.getUserAgent(),
								log1.getUserAgent())) {
							p.setCookieId(log1.getCookieId());
							roughs.set(i, null);
						} else {
							noRoughLogs = false;
						}
					}
					if (noRoughLogs)
						roughs.clear();
				} else {
					roughs.add(log1);
				}

			}
			roughs.clear();
			for (int i = 0; i < logs.size(); i++) {
				LogWritable first = logs.get(i);
				if (first == null) {
					continue;
				}
				TimeTotalMaps timeTotalMaps = new TimeTotalMaps();
				timeTotalMaps.setTime(first.getTime());
				timeTotalMaps.setIP(first.getCip() + "");
				if (first.getUrl() != null) {
					Integer value = timeTotalMaps.getUniqueUrls().get(
							first.getUrl());
					timeTotalMaps.getUniqueUrls().put(first.getUrl(),
							value == null ? 1 : value + 1);
				}
				if (first.getStatus() != 0) {
					Integer value = timeTotalMaps.getResponseCodes().get(
							first.getStatus());
					timeTotalMaps.getResponseCodes().put(first.getStatus(),
							value == null ? 1 : value + 1);
				}
				timeTotalMaps.inc();
				for (int j = i + 1; j < logs.size(); j++) {
					LogWritable next = logs.get(j);
					if (next == null)
						continue;
					if (LogWritable.equal(first.getCookieId(),
							next.getCookieId())) {
						if (next.getUrl() != null) {
							Integer value = timeTotalMaps.getUniqueUrls().get(
									next.getUrl());
							timeTotalMaps.getUniqueUrls().put(next.getUrl(),
									value == null ? 1 : value + 1);
						}
						if (next.getStatus() != 0) {
							Integer value = timeTotalMaps.getResponseCodes()
									.get(next.getStatus());
							timeTotalMaps.getResponseCodes().put(
									next.getStatus(),
									value == null ? 1 : value + 1);
						}
						timeTotalMaps.inc();
						logs.set(j, null);
					}
				}
				double urlRate = 0.0, sc404Rate = 0.0, sc500Rate = 0.0;
				// System.out.println("timeTotalMaps = " + timeTotalMaps);
				if (timeTotalMaps.getUniqueUrls().size() > 0) {
					QDigest digest = new QDigest(timeTotalMaps.getUniqueUrls()
							.size() + 1);

					for (Map.Entry<String, Integer> num : timeTotalMaps
							.getUniqueUrls().entrySet()) {
						digest.offer(num.getValue().longValue());
					}
					long l = digest.getQuantile(0.1);
					urlRate = new BigDecimal((double) l
							/ timeTotalMaps.getTotal()).setScale(5,
							BigDecimal.ROUND_CEILING).doubleValue();

				}
				if (timeTotalMaps.getResponseCodes().get(404) != null)
					sc404Rate = new BigDecimal((double) timeTotalMaps
							.getResponseCodes().get(404)
							/ timeTotalMaps.getTotal()).setScale(5,
							BigDecimal.ROUND_CEILING).doubleValue();
				if (timeTotalMaps.getResponseCodes().get(500) != null)
					sc500Rate = new BigDecimal((double) timeTotalMaps
							.getResponseCodes().get(500)
							/ timeTotalMaps.getTotal()).setScale(5,
							BigDecimal.ROUND_CEILING).doubleValue();
				JSONObject json = new JSONObject();
				try {
					json.put("session",
							UUID.randomUUID().toString().replace("-", ""));
					json.put("c_ip", timeTotalMaps.getIP());
					// json.put("format_ip", timeTotalMaps.getIP());
					/* json.put("format_time", timeTotalMaps.getDate()); */
					json.put("time", timeTotalMaps.getTime());
					json.put("total", timeTotalMaps.getTotal());
					if (urlRate > 0.0)
						json.put("urlRate", urlRate);
					if (sc404Rate > 0.0)
						json.put("sc404", sc404Rate);
					if (sc500Rate > 0.0)
						json.put("sc500", sc500Rate);
					out.write(json.toString());
					out.newLine();
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
			in.close();
		}
		out.flush();
		out.close();
		// ----------------------------------------------------

		/*
		 * for (File file : files1) { BufferedReader in = new BufferedReader(new
		 * InputStreamReader( new FileInputStream(file)));
		 * 
		 * String[] IPTimePair = file.getName().split("_"); Long time =
		 * Long.parseLong(IPTimePair[1]); TimeTotalMaps timeTotalMaps = new
		 * TimeTotalMaps(time); while ((line = in.readLine()) != null) { if
		 * (line.length() == 0) { continue; } if (timeTotalMaps == null) {
		 * timeTotalMaps = new TimeTotalMaps(); } String[] logs =
		 * line.split("--_"); String k = logs[3];//c_ip一段时间内请求一个地址的次数 if (k !=
		 * null && !k.trim().equals("") && !k.trim().equals("null")) { Integer
		 * value = timeTotalMaps.getUniqueUrls().get(k);
		 * timeTotalMaps.getUniqueUrls().put(k, value == null ? 1 : value + 1);
		 * } String statusValue = logs[4]; if (statusValue != null &&
		 * !statusValue.trim().equals("") && !statusValue.trim().equals("null"))
		 * { Integer k1 = Integer.parseInt(statusValue); Integer value =
		 * timeTotalMaps.getResponseCodes().get(k1);
		 * timeTotalMaps.getResponseCodes().put(k1, value == null ? 1 : value +
		 * 1); } timeTotalMaps.inc(); } double urlRate = 0.0, sc404Rate = 0.0,
		 * sc500Rate = 0.0; if (timeTotalMaps.getUniqueUrls().size() > 0) {
		 * QDigest digest = new QDigest(timeTotalMaps.getUniqueUrls().size() +
		 * 1); for (Integer num : timeTotalMaps.getUniqueUrls().values()) { //
		 * s_logger.info("UniqueUrls: " + num); digest.offer(num.longValue()); }
		 * long l = digest.getQuantile(0.1); urlRate = new BigDecimal((double) l
		 * / timeTotalMaps.getTotal()).setScale(5,
		 * BigDecimal.ROUND_CEILING).doubleValue();
		 * 
		 * } if (timeTotalMaps.getResponseCodes().get(404) != null) sc404Rate =
		 * new BigDecimal((double) timeTotalMaps.getResponseCodes().get(404) /
		 * timeTotalMaps.getTotal()).setScale(5,
		 * BigDecimal.ROUND_CEILING).doubleValue(); if
		 * (timeTotalMaps.getResponseCodes().get(500) != null) sc500Rate = new
		 * BigDecimal((double) timeTotalMaps.getResponseCodes().get(500) /
		 * timeTotalMaps.getTotal()).setScale(5,
		 * BigDecimal.ROUND_CEILING).doubleValue(); JSONObject json = new
		 * JSONObject(); try { json.put("session",
		 * UUID.randomUUID().toString().replace("-", "")); json.put("c_ip",
		 * IPTimePair[0]); json.put("time", timeTotalMaps.getTime());
		 * json.put("total", timeTotalMaps.getTotal()); if (urlRate > 0.0)
		 * json.put("urlRate", urlRate); if (sc404Rate > 0.0) json.put("sc404",
		 * sc404Rate); if (sc500Rate > 0.0) json.put("sc500", sc500Rate);
		 * out.write(json.toString()); out.newLine(); } catch (JSONException e)
		 * { e.printStackTrace(); } in.close(); } out.flush(); out.close();
		 */

	}

	public static void reduceModel(String reduceFrom, String outFilePath)
			throws IOException, ParserException {
		File file = new File(reduceFrom);
		if (!file.exists())
			throw new FileNotFoundException("must be a having ");
		File outFile = new File(outFilePath);
		if (!outFile.exists()) {
			outFile.createNewFile();
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outFile)));
		String log;

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		QDigest urlDigest = new QDigest(Long.MAX_VALUE);
		QDigest sc404Digest = new QDigest(Long.MAX_VALUE);
		QDigest sc500Digest = new QDigest(Long.MAX_VALUE);
		// We assume total url number and total sc-status count are the same as
		// log line number
		Integer totalNumber = 0;
		boolean urlFlag = false;
		boolean flag404 = false;
		boolean flag500 = false;
		while ((log = in.readLine()) != null) {
			totalNumber++;
			JSONObject json;
			try {
				json = new JSONObject(log);
			} catch (JSONException e) {
				continue;
			}
			Object vl;
			try {
				vl = json.get("urlRate");
			} catch (JSONException e) {
				vl = null;
			}

			if (vl != null) {
				urlFlag = true;
				urlDigest
						.offer((long) (Double.parseDouble(vl.toString()) * 100_000));
			}
			try {
				vl = json.get("sc404");
			} catch (JSONException e) {
				vl = null;
			}
			if (vl != null) {
				flag404 = true;
				sc404Digest
						.offer((long) (Double.parseDouble(vl.toString()) * 100_000));
			}
			try {
				vl = json.get("sc500");
			} catch (JSONException e) {
				vl = null;
			}
			if (vl != null) {
				flag500 = true;
				sc500Digest
						.offer((long) (Double.parseDouble(vl.toString()) * 100_000));
			}
		}

		// Get the final model by day, (median)
		double urlRate = 0;
		double sc404Rate = 0;
		double sc500Rate = 0;
		if (urlFlag)
			urlRate = (double) urlDigest.getQuantile(0.5) / 100_000;
		if (flag404)
			sc404Rate = (double) sc404Digest.getQuantile(0.5) / 100_000;
		if (flag500)
			sc500Rate = (double) sc500Digest.getQuantile(0.5) / 100_000;

		JSONObject json = new JSONObject();
		try {
			json.put("total", totalNumber);
			if (urlRate > 0.0)
				json.put("urlRate", urlRate);
			if (sc404Rate > 0.0)
				json.put("sc404", sc404Rate);
			if (sc500Rate > 0.0)
				json.put("sc500", sc500Rate);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		in.close();
		out.write(json.toString());
		out.flush();
		out.close();
	}

	private static void sum(String file) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		int sum = 0;
		String log;
		while ((log = in.readLine()) != null) {
			sum += Integer.parseInt(log.trim());
		}
		System.out.println("sum = " + sum);
	}

	public static void main(String[] args) throws IOException, ParserException,
			JSONException, ParseException {

		map(args[0], args[1]);
		reduce(args[1], args[2]);
		reduceModel(args[2], args[3]);
		// sum(args[2]);
	}
}
