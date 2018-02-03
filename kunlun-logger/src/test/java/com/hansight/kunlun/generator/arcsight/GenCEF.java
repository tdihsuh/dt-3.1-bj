package com.hansight.kunlun.generator.arcsight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.time.DateUtils;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogConfigIF;
import org.productivity.java.syslog4j.SyslogIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.util.IOUtils;
import com.hansight.kunlun.generator.utils.GenUtils;

public class GenCEF {
	private static final Logger LOG = LoggerFactory.getLogger(GenCEF.class);
	private static BlockingQueue<String> queue = new LinkedBlockingQueue<>(
			100000);

	public static void main(String[] args) throws Exception {
		if (args.length != 6) {
			System.out
					.println("Usage:<log dir> <dist host> <dist port> <date str> <protocol> <interval time>");

			System.out.println("<interval time> unit:h, m, s");
			System.out
					.println("example: /root/cmb.new/sniffer-iis 172.16.219.121 514 2014-11-27 udp 24h");
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		File logFile = new File(args[0]);
		String distHost = args[1];
		int distPort = Integer.parseInt(args[2]);
		Date startDate = sdf.parse(args[3]);
		String protocol = args[4];
		long interval = GenUtils.parseDateWithUnit(args[5]);

		int days = 0;

		Date distDate = DateUtils.addDays(startDate, days);
		Thread gen = new Thread(new Gen(distDate, logFile, "UTF-8", interval));
		gen.setName("gen");
		gen.start();
		new Thread(new Processor(protocol, distHost, distPort)).start();
	}

	private static class Processor implements Runnable {
		private String protocol;
		String distHost;
		int distPort;

		public Processor(String protocol, String distHost, int distPort) {
			this.protocol = protocol;
			this.distHost = distHost;
			this.distPort = distPort;
		}

		@Override
		public void run() {
			SyslogIF syslog = Syslog.getInstance(protocol);
			SyslogConfigIF config = syslog.getConfig();
			config.setHost(distHost);
			config.setPort(distPort);
			config.setFacility(1);
			int level = -1;
			while (true) {
				String msg = GenCEF.queue.poll();
				syslog.log(level, msg);
			}
		}

	}

	private static class Gen implements Runnable {
		Date distDate;
		File logFile;
		String encoding;
		long interval;

		public Gen(Date distDate, File logFile, String encoding, long interval) {
			this.distDate = distDate;
			this.logFile = logFile;
			this.encoding = encoding;
			this.interval = interval;
		}

		@Override
		public void run() {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(logFile));
				String line = null;
				long lineNumber = 0;
				String msg = null;
				while ((line = in.readLine()) != null) {
					lineNumber++;
					if (lineNumber <= 3) {
						continue;
					}
					if (line.length() < 32) {
						continue;
					}
					if (line.contains("CEF:")) {
						if (msg != null) {
							try {
								GenCEF.queue.put(msg);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						msg = line.substring(line.indexOf("CEF:"),
								line.length() - 24);
					} else {
						msg += line.substring(3, line.length() - 24);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				IOUtils.close(in);
			}
		}

	}
}
