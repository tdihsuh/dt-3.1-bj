package com.hansight.kunlun.analysis.realtime.single;

import com.codahale.metrics.Counter;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.hansight.kunlun.analysis.realtime.conf.Constants;
import com.hansight.kunlun.analysis.realtime.model.Anomaly;
import com.hansight.kunlun.analysis.realtime.model.EnhanceAccess;
import com.hansight.kunlun.analysis.realtime.model.RTConstants;
import com.hansight.kunlun.analysis.utils.DatetimeUtils;
import com.hansight.kunlun.analysis.utils.EventTypeUtils;
import com.hansight.kunlun.utils.MetricsUtils;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionProcessor extends Thread {
	private final static Logger logger = LoggerFactory
			.getLogger(SessionProcessor.class);

	private static int DEFAULT_EXPIRE_QUEUE_CAPACITY = RTHandler.getInt(
			Constants.MESSAGE_QUEUE_CAPACITY,
			Constants.MESSAGE_QUEUE_CAPACITY_DEFAULT);
	//normal reading logs to session
	private BlockingQueue<EnhanceAccess> queue = null;
	private BlockingQueue<EnhanceAccess> singleQueue = new LinkedBlockingQueue<>(
			DEFAULT_EXPIRE_QUEUE_CAPACITY);
	private BlockingQueue<Anomaly> anomalies = new LinkedBlockingQueue<>(
			DEFAULT_EXPIRE_QUEUE_CAPACITY);
	/**
	 * final session
	 */
	private Map<Integer, Double> QSModel;
	private Map<Integer, Double> manualQueryStringModel = null;
	private Map<String, Boolean> model404;
	private Map<String, Boolean> manualModel404 = null;

	private Set<String> ipWhiteList = new HashSet<String>();
	private Set<String> urlPatternList = new HashSet<String>();
	private Pattern pattern;
	private Matcher matcher;
	private static final String IPV4_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	//private static final String URLEND_PATTERN = ".*\\.(jpg$|gif$|png$|bmp$|ico$|htc$|css$)";

	//private static final String URLBEGIN_PATTERN = "^/CmbBank_GenShell/UI/GenShellPC(_EN)?/Login/.*";

	private LookupService ipLookup;
	private ExecutorService service;

	private boolean notInModel404AsAnomaly = true;

	public boolean validate(final String ip) {
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	public void getIpWhiteList(String path) throws IOException {
		List<String> lines = Files.readLines(new File(path), Charsets.UTF_8);
		for (String line : lines) {
			if (validate(line))
				ipWhiteList.add(line);
		}
	}

	public void getUrlPatternList(String path) throws IOException {
		List<String> lines = Files.readLines(new File(path), Charsets.UTF_8);
		for (String line : lines) {
			if (line != null && line.length() > 0)
				urlPatternList.add(line);
		}
	}

	public SessionProcessor() {
	}

	public SessionProcessor(BlockingQueue<EnhanceAccess> queue,
			Map<Integer, Double> manualQueryStringModel,
			Map<Integer, Double> queryStringModel,
			Map<String, Boolean> manulModel404, Map<String, Boolean> model404) {
		super();
		this.queue = queue;
		this.manualQueryStringModel = manualQueryStringModel;
		this.QSModel = queryStringModel;
		this.manualModel404 = manulModel404;
		this.model404 = model404;
		this.setName("session-processor");
		pattern = Pattern.compile(IPV4_PATTERN);

		//real_time.properties should add ip.white.list path, keep default as empty
		String ipWhiteListPath = RTHandler.get("ip.white.list", "");
		if (ipWhiteListPath.length() > 0) {
			try {
				getIpWhiteList(ipWhiteListPath);
			} catch (IOException e) {
				logger.warn("Failed to get white ip list");
				ipWhiteList.clear();
			}
		}

		//构造函数中赋值urlPatternList
		String urlPatternListPath = RTHandler.get("url.pattern.list", "");
		if (urlPatternListPath.length() > 0) {
			try {
				getUrlPatternList(urlPatternListPath);
			} catch (IOException e) {
				logger.warn("Failed to get pattern url list");
				urlPatternList.clear();
			}
		}

		this.service = Executors.newFixedThreadPool(15);
		try {
			ipLookup = new LookupService(RTHandler.get(
					Constants.IP_GEO_DATABASE, "GeoIpCity.dat"),
					LookupService.GEOIP_MEMORY_CACHE);
		} catch (IOException e) {
			/*
			 * logger.error(e,""); e.printStackTrace();
			 */
		}
		this.notInModel404AsAnomaly = "true".equalsIgnoreCase(RTHandler.get(
				Constants.MODEL_404_NOT_EXISTS_AS_ANOMALY, "false"));

	}

	@Override
	public void run() {
		MetricsUtils.newGauge("processor.queue.single.size", singleQueue);
		Counter singleCounter = MetricsUtils
				.newCounter("processor.queue.single.total");
		service.execute(new SingleLogProcessor());
		service.execute(new SingleLogProcessor());
		service.execute(new SingleLogProcessor());
		service.execute(new SingleLogProcessor());
		//  service.execute(new SessionMonitor());
		//    service.execute(new SessionExpireMonitor());
		service.execute(new Storer(RTConstants.ANOMALY_ANOMALY_TYPE, anomalies));
		//splitter session
		while (RTHandler.RUNNING) {
			try {
				EnhanceAccess access = this.queue.take();
				if (access.cs_host == null || access.cs_host.length() == 0
						|| access.cs_uri_stem == null
						|| access.cs_uri_stem.length() == 0) {
					continue; // NO-OP invalid url
				}
				while (singleQueue.size() >= DEFAULT_EXPIRE_QUEUE_CAPACITY - 1) {
					TimeUnit.SECONDS.sleep(1);
				}
				singleQueue.add(access);
				singleCounter.inc();
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	private class SingleLogProcessor implements Runnable {

		private void storeAnomaly(EnhanceAccess access, double entropy,
				double upper, String type) {
			Anomaly anomaly = new Anomaly(access.c_ip, access.cookie_id,
					access.utcDatetime, DatetimeUtils.getUTCDateTime(new Date()
							.getTime()));
			Map<String, String> map = new HashMap<>();
			map.put(access.s_ip, access.s_computer_name);
			anomaly.setServers(map);
			anomaly.setIndices(new String[] { access.getIndex() });
			anomaly.setEventType(type);
			anomaly.setUrl(access.getCs_uri_stem());
			anomaly.setDegree(entropy);
			anomaly.setModelDegree(upper);
			anomaly.setCategory(EventTypeUtils.getCategory(type));
			if (type.equals(RTConstants.EVENT_TYPE_SQL_INJECTION)) {
				anomaly.setUriQuery(access.cs_uri_query);
			}
			if (ipLookup != null) {
				Location location = ipLookup.getLocation(access.c_ip);
				if (location != null) {
					if (location.countryName != null)
						anomaly.setCountry(location.countryName);
					if (location.city != null)
						anomaly.setCity(location.city);
					if (location.longitude > 0.0f)
						anomaly.setLongitude(location.longitude);
					if (location.latitude > 0.0f)
						anomaly.setLatitude(location.latitude);
				}
			}

			while (anomalies.size() > 9990)
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			try {
				anomalies.put(anomaly);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			Counter noExistsCounter = MetricsUtils
					.newCounter("anomaly.status.404.total.not.exits");
			Counter maliciousCounter = MetricsUtils
					.newCounter("anomaly.status.404.total.malicious");
			Counter qsCounter = MetricsUtils
					.newCounter("anomaly.query.string.total");
			Counter processorCounter = MetricsUtils
					.newCounter("processor.queue.single.take");
			while (RTHandler.RUNNING) {
				//model 404 detect start
				if (model404.size() == 0 || singleQueue.size() == 0) {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						logger.error("SingleLogProcessor Interrupted :{}",
								e.getCause());
						e.printStackTrace();
					}
					continue;
				}
				EnhanceAccess access = singleQueue.poll();
				if ((access == null) || (access.cs_uri_stem == null)
						|| "".equals(access.cs_uri_stem)) {
					continue;
				}
				processorCounter.inc();
				if (!ipWhiteList.contains(access.c_ip)) {
					if (model404 != null && access.getSc_status() == 404) {
						//logger.debug(" 404 key:{} - value:{} ", access.getCs_uri_stem());
						if (manualModel404.get(access.cs_uri_stem) == null) {
							Boolean stem = model404.get(access.cs_uri_stem);
							String url = access.getCs_uri_stem();
							/**
							 * 正则判断url.Pattern.List 中的正则,有一个不匹配都不能入库
							 * */
							Iterator<String> i = urlPatternList.iterator();
							boolean isMatch = true;
							while (i.hasNext()) {
								String url_pattern = i.next();
								//只要与url.pattern.list中文件有一个匹配上来就认为不能保存
								if (url_pattern != null
										&& !"".equals(url_pattern.trim())
										&& url.matches(url_pattern)) {
									isMatch = false;
									break;
								}
							}
							if (isMatch) {
								if (stem != null && stem) {
									maliciousCounter.inc();
									storeAnomaly(access, -1, -1,
											RTConstants.EVENT_TYPE_HTTP404);
								}
								if (notInModel404AsAnomaly) {
									if (stem == null) {
										noExistsCounter.inc();
										storeAnomaly(access, -1, -1,
												RTConstants.EVENT_TYPE_HTTP404);
									}
								}
							}

						}
					}
				} else {
					logger.info("The abnomal ip: " + access.c_ip
							+ " is in white list, ignored.");
					continue;
				}

				if ((access.cs_uri_query == null)
						|| "".equals(access.cs_uri_query)) {
					continue;
				}

				// QueryString anomaly
				String white = "abcdefghijklmnopqrstuvwxyz";
				String[] params = access.cs_uri_query.split("&");
				boolean isAnomal = false;
				double entropy = 0.0f;
				if (!ipWhiteList.contains(access.c_ip)) {
					for (String param : params) {
						String[] kvs = param.split("=");
						if (kvs.length == 2) {
							//ignore _=1411324567
							if (kvs[0].length() > 0
									&& (kvs[0].equalsIgnoreCase("_"))) {
								isAnomal = false;
								continue;
							}
							//has _ts key will be treated as normal
							if (kvs[0].length() > 0
									&& kvs[0].equalsIgnoreCase("_ts")) {
								isAnomal = false;
								break;
							}
							//not start with alphabet char or the value contains "script"
							if ((kvs[0].length() > 0 && !white.contains(kvs[0]
									.toLowerCase().substring(0, 1)))
									|| (kvs[1].length() > 0 && (kvs[1]
											.toLowerCase().contains("<script>") || kvs[1]
											.toLowerCase().contains(
													"javascript")))) {
								isAnomal = true;
								entropy += StringEntropyCalculator
										.calculate(kvs[1]);
								break;
							}
						}// value contains "javascript" or <scrip> will be anomal
						else if (kvs.length == 1
								&& kvs[0].length() > 0
								&& (kvs[0].toLowerCase().contains("<script>") || kvs[0]
										.toLowerCase().contains("javascript"))) {
							isAnomal = true;
							entropy += StringEntropyCalculator
									.calculate(kvs[0]);
							break;
						}
					}
				} else {
					logger.info("ip: " + access.c_ip
							+ " matches white list, ignored.");
					continue;
				}

				if (isAnomal) {
					// entropy will be ignored
					storeAnomaly(access, entropy, entropy,
							RTConstants.EVENT_TYPE_SQL_INJECTION);
					qsCounter.inc();
					logger.debug("Found anomal query string: {}",
							access.cs_uri_query);
				}
			}

		}
	}

	//for UT purpose, won't be used in app
	public static void main(String[] argv) throws IOException {

		String white = "abcdefghijklmnopqrstuvwxyz";
		double entropy = 0.0f;

		// File file = new File("/Volumes/MATERIALS/cmb.new/model/cs-uri-query.json");
		FileInputStream fis = new FileInputStream(
				"/Volumes/MATERIALS/cmb.new/model/cs-uri-query.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		FileWriter fw = new FileWriter(
				"/Volumes/MATERIALS/cmb.new/model/cs-uri-query.anormal", false);
		BufferedWriter out = new BufferedWriter(fw);
		boolean isAnomal = false;
		while ((line = br.readLine()) != null) {
			String pending = line.split(":")[1].trim().split("\"")[1];
			String[] params = pending.split("&");
			for (String param : params) {
				String[] kvs = param.split("=");
				if (kvs.length == 2) {
					//ignore _=1411324567
					if (kvs[0].length() > 0 && (kvs[0].equalsIgnoreCase("_"))) {
						isAnomal = false;
						continue;
					}
					//has _ts key will be treated as normal
					if (kvs[0].length() > 0 && kvs[0].equalsIgnoreCase("_ts")) {
						isAnomal = false;
						break;
					}
					//not start with alphabet char or the value contains "script"
					if ((kvs[0].length() > 0 && !white.contains(kvs[0]
							.toLowerCase().substring(0, 1)))
							|| (kvs[1].length() > 0 && (kvs[1].toLowerCase()
									.contains("<script>") || kvs[1]
									.toLowerCase().contains("javascript")))) {
						isAnomal = true;
						entropy += StringEntropyCalculator.calculate(kvs[1]);
						continue;
					}
				}// value contains "javascript" or <scrip> will be anomal
				else if (kvs.length == 1
						&& kvs[0].length() > 0
						&& (kvs[0].toLowerCase().contains("<script>") || kvs[0]
								.toLowerCase().contains("javascript"))) {
					isAnomal = true;
					entropy += StringEntropyCalculator.calculate(kvs[0]);
					continue;
				}
			}
			if (isAnomal) {
				out.write(line);
				out.newLine();
				isAnomal = false;
				System.out.println(entropy + " ---> " + line);
			}

		}

		br.close();
		out.close();
		fw = null;
		br = null;
		fis = null;

		//        String ip = "311.1.1.100";
		//        SessionProcessor sp = new SessionProcessor();
		//        sp.pattern = Pattern.compile(IPV4_PATTERN);
		//        sp.matcher = sp.pattern.matcher(ip);
		//        boolean matches = sp.matcher.matches();
		//        System.out.println(matches);

	}

}
