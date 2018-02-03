package com.hansight.kunlun.collector.stream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.collector.common.model.Stream;

/**
 * Created by zhhui on 2014/11/5.
 */
public class SyslogStream extends Stream<ByteBuffer> {
	private Queue<ByteBuffer> queue;
	protected final static Logger logger = LoggerFactory
			.getLogger(SyslogStream.class);
	SyslogServerConfigIF config;

	public SyslogStream(SyslogServerConfigIF syslogServerConfig,
			final int cacheSize) {
		config=syslogServerConfig;
		queue = new ArrayBlockingQueue<>(cacheSize + 1);
		syslogServerConfig.addEventHandler(new SyslogServerEventHandlerIF() {
			private static final long serialVersionUID = -1334812019329301273L;

			@Override
			public void event(SyslogServerIF syslogServer,
					SyslogServerEventIF syslogEvent) {

				try {
					while (queue.size() == cacheSize) {
						TimeUnit.MILLISECONDS.sleep(100);
					}
					ByteBuffer buffer=ByteBuffer.wrap(getMessage(
							syslogEvent.getMessage(), syslogEvent.getCharSet()));
					queue.offer(buffer);
				} catch (Exception e) {
					logger.error("syslog message process error:{}", e);
				}
			}

			public byte[] getMessage(String msg, String encoding) {
				try {
					if (msg != null) {
						if (!"utf-8".equalsIgnoreCase(encoding)) {
							return new String(msg.getBytes(encoding), "utf-8")
									.getBytes("utf-8");
						} else {
							return msg.getBytes("utf-8");
						}
					}
				} catch (UnsupportedEncodingException e) {
					logger.error(" UnsupportedEncodingException error", e);
				}
				return "".getBytes();
			}
		});
	}

	@Override
	public void close() throws IOException {
		config.removeAllEventHandlers();
		config.setShutdownWait(0);
		config.getShutdownWait();

	}

	@Override
	public boolean hasNext() {

		while ((item = queue.poll()) == null) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				logger.error("InterruptedException :{}", e);
			}
		}
		return true;
	}
}
