package com.hansight.kunlun.collector.processor.net;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.processor.DefaultExecutor;
import com.hansight.kunlun.collector.processor.DefaultLogProcessor;
import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.exception.LogProcessorException;
import com.hansight.kunlun.collector.processor.Executor;
import com.hansight.kunlun.collector.transfer.ByteBufferToEvent;
import com.hansight.kunlun.collector.transfer.Transfer;
import com.hansight.kunlun.coordinator.metric.MetricException;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.WorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NetLogProcessor extends DefaultLogProcessor<ByteBuffer> {
    final static Logger logger = LoggerFactory.getLogger(NetLogProcessor.class);
    protected String host;
    protected String protocol;
    protected int port;
    protected String encoding;
    Map<String, Object> header = new LinkedHashMap<>();
    protected Executor<ByteBuffer> executor;
    @Override
    public void setup() throws LogProcessorException {
        host = get("host", "0.0.0.0");
        protocol = get("protocol", "tcp");
        port = Integer.parseInt(get("port", "514"));
        encoding = get("encoding", "utf-8");
        transfer = new ByteBufferToEvent();
        String path = protocol + "://" + host + ":" + port;
        header.put("source", path);
        lexer.setHeader(header);
        logger.info("NetLogProcessor start listen:{}", path);
        threadPool= Executors.newFixedThreadPool(2);
    }
    @Override
    public void cleanup() throws LogProcessorException {
        try {
            executor.close();
            threadPool.shutdown();
        } catch (IOException e) {
            throw new LogProcessorException("",e);
        }
    }

    @Override
    public void process(LogReader<ByteBuffer> stream) {

        executor =new DefaultExecutor<ByteBuffer>(stream,cacheSize,PROCESSOR_THREAD_WAIT_TIMES) {
            @Override
            public LogWriter<Map<String, Object>> makeWriter() {
                return writer;
            }

            @Override
            public Lexer<Event, Map<String, Object>> makeLexer() {
                return lexer;
            }

            @Override
            public MetricService getMetric() {
                return metricService;
            }

            @Override
            public Transfer<ByteBuffer, Event> getTransfer() {
                return transfer;
            }
        };
        threadPool.execute(executor);
    }


}
