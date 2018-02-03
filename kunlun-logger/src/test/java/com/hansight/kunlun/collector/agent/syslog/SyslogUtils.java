package com.hansight.kunlun.collector.agent.syslog;

import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogUtils {
    private static final Logger LOG = LoggerFactory
            .getLogger(SyslogUtils.class);
    private SyslogServerIF syslogServer;
    private SyslogServerConfigIF syslogServerConfig;
    
    public synchronized void launch(String protocol) {
        String host =  "0.0.0.0";
        int port =514;
        String encoding = "utf-8";
        syslogServer = SyslogServer.getInstance(protocol);
        syslogServerConfig = syslogServer.getConfig();
        syslogServerConfig.setHost(host);
        syslogServerConfig.setPort(port);
        syslogServerConfig.setCharSet(encoding);
        LOG.debug("syslog protocol:{}, host:{}, port:{}, encoding:{}",
                protocol, host, port, encoding);

        syslogServerConfig.addEventHandler(new SyslogServerEventHandlerIF() {
            private static final long serialVersionUID = -1334812019329301273L;

            @Override
            public void event(SyslogServerIF syslogServer,
                              SyslogServerEventIF syslogEvent) {
            	System.out.println(syslogEvent.getMessage());
            }
        });
        Thread thread = new Thread(syslogServer);
		thread.setName("SyslogServer: " + protocol);
		
		syslogServer.setThread(thread);
		thread.start();
    }

    public static void main(String[] args) throws InterruptedException {
        SyslogUtils syslog = new SyslogUtils();
        syslog.launch("udp");
        while(true) {
        	
        }
    }
}
