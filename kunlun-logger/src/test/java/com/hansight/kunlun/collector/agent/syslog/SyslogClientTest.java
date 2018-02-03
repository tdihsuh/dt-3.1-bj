package com.hansight.kunlun.collector.agent.syslog;

import org.productivity.java.syslog4j.SyslogMain;

public class SyslogClientTest {

	public static void main(String args[]) throws Exception {
		args = new String[] { "-h", "172.16.219.121", "-p", "514", "-l", "1",
				"-f", "1", "udp", "CEF" };
		SyslogMain.main(args);
	}

}