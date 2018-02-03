package com.hansight.kunlun.collector.common.base;

import com.hansight.kunlun.collector.common.exception.CollectorException;
import com.hansight.kunlun.coordinator.metric.MetricService;

import java.io.Closeable;
import java.util.Map;
import java.util.Queue;

/**
 * Author:zhhui
 * DateTime:2014/7/23 15:23.
 */
public interface Lexer<F, T> extends Runnable{
    String name();

    void set(String name);

    void setTemplet(String name);

    /**
     * parse take time is long??
     *
     * @throws com.hansight.kunlun.collector.common.exception.CollectorException
     */
    T parse(F from) throws CollectorException;

    /**
     * 设置要解析的值
     *
     * @param values @see Queue
     */
    void setValueToParse(Queue<F> values);
    void setHeader(Map<String,Object> values);

    /**
     * 解析结果存储器
     *
     * @param writer @see LogWriter
     */
    void setWriter(LogWriter<T> writer);

    LogWriter<T> getWriter();

    /**
     * 监控服务
     *
     * @param metricService @see   MetricService
     */
    void setMetric(MetricService metricService);

    boolean isFinished();

    void start();

    Lexer<F, T> newClone();
}
