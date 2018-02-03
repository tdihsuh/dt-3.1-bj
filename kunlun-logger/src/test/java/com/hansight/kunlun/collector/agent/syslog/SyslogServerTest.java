package com.hansight.kunlun.collector.agent.syslog;

import org.productivity.java.syslog4j.server.SyslogServerMain;

public class SyslogServerTest {

	public static void main(String args[]) throws Exception {
		args = new String[] { "-h", "172.16.219.121", "-p", "5014", "udp" };
		SyslogServerMain.main(args);
	}

}