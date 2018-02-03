package com.hansight.kunlun.collector.agent.syslog;

import java.util.concurrent.TimeUnit;

import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogConfigIF;
import org.productivity.java.syslog4j.SyslogIF;

public class Syslog4jTest {
	public static void main(String[] args) throws InterruptedException {
		// send("udp", 514, "udp message");
		while (true) {
			send("udp",
					"172.16.219.121",
					514,
					"CEF");
			TimeUnit.MILLISECONDS.sleep(100);
		}
	}

	public static void send(String protocol, String host, int port,
			String message) {
		SyslogIF syslog = Syslog.getInstance(protocol);
		SyslogConfigIF config = syslog.getConfig();
		config.setHost(host);
		config.setPort(port);
		config.setFacility(1);
		syslog.log(1, message);
	}
}
