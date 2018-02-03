package com.hansight.kunlun.collector.reader;

import com.hansight.kunlun.collector.stream.SyslogStream;
import com.hansight.kunlun.collector.common.base.LogReader;

import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class SyslogLogReader implements LogReader<ByteBuffer> {
    private String path;
    private SyslogStream stream;
    Thread thread;

    public SyslogLogReader(String protocol, String host, int port, String encoding) {
        SyslogServerIF syslogServer = SyslogServer.getInstance(protocol);
        SyslogServerConfigIF    syslogServerConfig = syslogServer.getConfig();
        syslogServerConfig.setHost(host);
        syslogServerConfig.setPort(port);
        syslogServerConfig.setCharSet(encoding);
        path = protocol + "://" + host + ":" + port;
         thread = new Thread(syslogServer);

        thread.setName("SyslogServer: " + protocol);
        stream=  new SyslogStream(syslogServerConfig, 5000);
		syslogServer.setThread(thread);
		thread.start();

    }

    @Override
    public long skip(long skip) {
        return 0;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        return stream;
    }

    @Override
    public void close() throws IOException {
            stream.close();
        thread.interrupt();

    }
}