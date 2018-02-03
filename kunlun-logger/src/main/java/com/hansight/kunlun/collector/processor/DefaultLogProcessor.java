package com.hansight.kunlun.collector.processor;

import com.hansight.kunlun.collector.agent.Agent;
import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogProcessor;
import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.collector.writer.ElasticSearchLogWriter;
import com.hansight.kunlun.collector.common.exception.LogProcessorException;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.common.model.Stream;
import com.hansight.kunlun.collector.common.utils.AgentConstants;
import com.hansight.kunlun.collector.position.store.ReadPositionStore;
import com.hansight.kunlun.collector.transfer.Transfer;
import com.hansight.kunlun.coordinator.config.AgentConfig;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.ProcessorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public abstract class DefaultLogProcessor<F> implements LogProcessor<F, Event, Map<String, Object>> {
    final static Logger logger = LoggerFactory.getLogger(DefaultLogProcessor.class);
    protected Transfer<F, Event> transfer;
    protected Lexer<Event, Map<String, Object>> lexer;
    protected LogWriter<Map<String, Object>> writer;
    protected boolean running = true;
    protected AgentConfig conf;
    public MetricService metricService;
    protected ExecutorService threadPool;

    protected Queue<LogReader<F>> readers = new ConcurrentLinkedDeque<>();
    protected  Set<String> inProcess=new CopyOnWriteArraySet<>();
     protected ReadPositionStore store;
    protected final int poolSize = Integer.valueOf(Agent.GLOBAL.getProperty(AgentConstants.AGENT_POOL_SIZE, AgentConstants.AGENT_POOL_SIZE_DEFAULT));
    protected final int cacheSize = Integer.valueOf(Agent.GLOBAL.getProperty(AgentConstants.AGENT_WRITE_CACHE_SIZE, AgentConstants.AGENT_WRITE_CACHE_SIZE_DEFAULT));
    protected static Long PROCESSOR_THREAD_WAIT_TIMES = Long.valueOf(Agent.GLOBAL.getProperty(AgentConstants.PROCESSOR_THREAD_WAIT_TIMES, AgentConstants.PROCESSOR_THREAD_WAIT_TIMES_DEFAULT));



    @Override
    public void config(AgentConfig conf) {
        this.conf = conf;
       writer.config(conf);
        metricService = new MetricService(conf.getId(), conf.get("agent"), ProcessorType.AGENT);
    }

    @Override
    public Void call() throws Exception {
        try {
            setup();
            run();
            cleanup();
        } catch (Exception e) {
            logger.error("DEFAULT :{}", e);
        }
        return null;
    }

    @Override
    public void run() throws LogProcessorException {
        Iterator<LogReader<F>> iterator = readers();
        while (running && iterator.hasNext()) {
            LogReader<F> next = iterator.next();
            process(next);
        }

    }

    public String get(String key,
                      String defaultValue) {
        Object object = conf.get(key);
        if (object == null) {
            return defaultValue;
        }
        return object.toString();
    }

    @Override
    public void stop() throws Exception {
        running = false;
        metricService.close();
    }

    @Override
    public void setLexer(Lexer<Event, Map<String, Object>> lexer) {
        this.lexer = lexer;
    }

    @Override
    public void setWriter(LogWriter<Map<String, Object>> writer) {
        this.writer = writer;

    }

    class LogReaderStream<T> extends Stream<LogReader<T>> {
        private Queue<LogReader<T>> readers;

        LogReaderStream(Queue<LogReader<T>> readers) {
            this.readers = readers;
        }

        @Override
        public void close() throws IOException {
            try {
                stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean hasNext() {
            item = readers.poll();
            while (running && item==null){
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    logger.error(" INTERRUPTED EXCEPTION ERROR:{}",e);
                }
                item = readers.poll();
            }
            return item != null;
        }
    }

    private Iterator<LogReader<F>> readers() {
        return new LogReaderStream<F>(readers);
    }

    @Override
    public void setReadPositionStore(ReadPositionStore store) {
        this.store=store;
    }
}
