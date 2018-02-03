package com.hansight.kunlun.collector.agent.syslog;

import java.util.concurrent.CountDownLatch;

import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Syslog4jServerTest {
	private static final Logger LOG = LoggerFactory
			.getLogger(Syslog4jServerTest.class);

	private static CountDownLatch cdl = new CountDownLatch(1);

	public static void main(String[] args) throws InterruptedException {
		SyslogServerIF syslogServer;
		SyslogServerConfigIF syslogServerConfig;

		syslogServer = SyslogServer.getThreadedInstance("udp");
		syslogServerConfig = syslogServer.getConfig();
		syslogServerConfig.setHost("0.0.0.0");
		syslogServerConfig.setPort(5014);
		syslogServerConfig.setCharSet("utf-8");

		syslogServerConfig.addEventHandler(new SyslogServerEventHandlerIF() {
			private static final long serialVersionUID = -1334812019329301273L;

			@Override
			public void event(SyslogServerIF syslogServer,
					SyslogServerEventIF syslogEvent) {
				if (LOG.isInfoEnabled()) {
					LOG.info("syslog:{}, raw:{}", syslogEvent.getMessage(),
							new String(syslogEvent.getRaw()));
				}
			}
		});
		cdl.await();
	}
}
