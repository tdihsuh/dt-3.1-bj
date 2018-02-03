package com.hansight.kunlun.collector.common.base;

import com.hansight.kunlun.collector.common.exception.LogParserException;
import com.hansight.kunlun.coordinator.config.ForwarderConfig;

public interface LogParser<F,T> extends Runnable {
    /**
     * process data from reader ,then parse,final writer to Writer
     *
     * @throws com.hansight.kunlun.collector.common.exception.CollectorException
     */
    void parse() throws LogParserException;

    void setConf(ForwarderConfig conf);

    /**
     * 停止线程
     */
    void stop() throws InterruptedException;

    Lexer<F,T> getLexer();
}
