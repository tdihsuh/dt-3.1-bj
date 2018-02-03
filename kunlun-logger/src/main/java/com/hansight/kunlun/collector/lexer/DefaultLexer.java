package com.hansight.kunlun.collector.lexer;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.coordinator.metric.MetricException;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.WorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;

/**
 * Author:zhhui
 * DateTime:2014/7/28 13:04.
 */
public abstract class DefaultLexer<F, T extends Map<String, Object>> implements Lexer<F, T> {
    private String name;
    protected final static Logger logger = LoggerFactory.getLogger(DefaultLexer.class);
    protected LogWriter<T> writer;
    protected Map<String, Object> header;
    Queue<F> events;
    MetricService metricService;
    private volatile boolean isDone = true;
    private static boolean markStatus = false;

    public void setMetric(MetricService metricService) {
        this.metricService = metricService;
    }
    @Override
    public void setHeader(Map<String, Object> values) {
        this.header=values;
    }
    @Override
    public void run() {
        isDone = false;
        long times = 0;
        if (logger.isInfoEnabled())
            times = System.currentTimeMillis();
        int caches = events.size();
        F f;
        while ((f = events.poll()) != null) {
            //    metricService.mark();
            try {
                T log = parse(f);
                log.putAll(header);
                writer.write(log);
            } catch (Exception e) {
                logger.error("lexer error :{}", e);
                try {
                    metricService.setProcessorStatus(WorkerStatus.ConfigStatus.FAIL);
                } catch (MetricException e1) {
                    logger.error("Metric error :{}", e1);
                }
            }

        }
        logger.debug("parse {}, take {} ms", caches, System.currentTimeMillis() - times);
        try {
            events.clear();
            writer.flush();
            if (!markStatus) {
                markStatus = true;
                metricService.setProcessorStatus(WorkerStatus.ConfigStatus.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("lexer error :{}", e);
            try {
                metricService.setProcessorStatus(WorkerStatus.ConfigStatus.FAIL);
            } catch (MetricException e1) {
                logger.error("Metric error :{}", e1);
            }
        }
        isDone = true;
    }

    @Override
    public void setValueToParse(Queue<F> events) {
        this.events = events;
    }

    @Override
    public void setWriter(LogWriter<T> writer) {
        this.writer = writer;
    }
    @Override
    public LogWriter<T> getWriter() {
        return writer;
    }

    @Override
    public boolean isFinished() {
        if(isDone&& writer.isFinished()){
            logger.debug("lexer is finished");
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        isDone = false;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void set(String name) {
        this.name = name;
    }

}
