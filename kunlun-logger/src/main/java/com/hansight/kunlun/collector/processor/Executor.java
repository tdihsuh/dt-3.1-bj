package com.hansight.kunlun.collector.processor;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.transfer.Transfer;
import com.hansight.kunlun.coordinator.metric.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhhuiyan on 14/12/23.
 */
public abstract class Executor<F> implements Runnable, Closeable {
    final static Logger logger = LoggerFactory.getLogger(Executor.class);

    protected List<Lexer<Event, Map<String, Object>>> lexers = new LinkedList<>();
    protected Map<LogWriter<Map<String, Object>>, Boolean> writes = new HashMap<>();
    public LogReader<F> reader;
    Queue<Event> cache = null;
    long heartbeat;
    long cachingFromTime;
    boolean running = true;
    protected ExecutorService lexerPool = Executors.newFixedThreadPool(3);
    private long cacheSize;
    public long PROCESSOR_THREAD_WAIT_TIMES;

    public abstract LogWriter<Map<String, Object>> makeWriter();

    public LogWriter<Map<String, Object>> getWriter() {
        for (Map.Entry<LogWriter<Map<String, Object>>, Boolean> en : writes.entrySet()) {
            if (!en.getValue()) {
                return en.getKey();
            }

        }
        LogWriter<Map<String, Object>> writer = makeWriter();
        writes.put(writer, true);
        return writer;
    }

    public abstract Lexer<Event, Map<String, Object>> makeLexer();

    public abstract MetricService getMetric();

    public abstract Transfer<F, Event> getTransfer();

    public void newCache() {
        cache = new LinkedList<>();
    }

    private void heartbeat() {
        heartbeat = System.currentTimeMillis();

    }

    @SuppressWarnings("unchecked")
    private Lexer<Event, Map<String, Object>> get() throws InterruptedException {
        Lexer<Event, Map<String, Object>> lx;
        while (running) {
            for (Lexer<Event, Map<String, Object>> temp : lexers) {
                if (temp.isFinished()) {
                    temp.start();
                    return temp;
                }
            }
            if (lexers.size() < 3) {
                lx = makeLexer();
                lx.setWriter(getWriter());
                lx.setMetric(getMetric());
                lx.start();
                lx.set("lexer " + (lexers.size() + 1));
                lexers.add(lx);
                return lx;

            }
            logger.debug("parse is too slow, waiting for {} ms.", PROCESSOR_THREAD_WAIT_TIMES);
            TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
        }
        return null;
    }

    public boolean isDone() {
        if (lexers != null) {
            for (Lexer<Event, Map<String, Object>> temp : lexers) {
                if (!temp.isFinished()) {
                    return false;
                }
            }
        }

        return true;
    }
    public boolean isFinished(){
        return  isDone()&& reader==null;
    }
    public void flush() throws InterruptedException {
        if (cache.size() > 0) {
            Lexer<Event, Map<String, Object>> lexer = get();
            while (lexer == null) {
                lexer = get();
            }
            lexer.start();
            lexer.setValueToParse(cache);
            lexerPool.submit(lexer);
            newCache();
        }

    }

    public void parse(Event event) throws InterruptedException {
       // logger.debug("find a event:{}",event);
        cache.add(event);
        if (cache.size() >= cacheSize) {
            logger.debug(" cache {} events take {} ms", cache.size(), System.currentTimeMillis() - cachingFromTime);
            flush();
            if (logger.isDebugEnabled()) {
                cachingFromTime = System.currentTimeMillis();
            }
        }
    }

    public Executor(LogReader<F> reader, long cacheSize, long PROCESSOR_THREAD_WAIT_TIMES) {
        this.PROCESSOR_THREAD_WAIT_TIMES = PROCESSOR_THREAD_WAIT_TIMES;
        this.cacheSize = cacheSize;
        this.reader = reader;
    }

    public void setReader(LogReader<F> reader) {
        this.reader = reader;
    }

    public void clear() throws InterruptedException {
        flush();
        while (!isDone()) {
            TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
        }

        for (Lexer<Event, Map<String, Object>> temp : lexers) {
            writes.put(temp.getWriter(), false);
        }
        lexers.clear();
    }

    @Override
    public void close() throws IOException {
        running=false;
        try {
            clear();
        } catch (InterruptedException e) {
            logger.error("ERROR:{}", e);
        }
        for (Map.Entry<LogWriter<Map<String, Object>>, Boolean> en : writes.entrySet()) {
            en.getKey().close();
        }
        writes.clear();
        lexerPool.shutdownNow();
    }

}
