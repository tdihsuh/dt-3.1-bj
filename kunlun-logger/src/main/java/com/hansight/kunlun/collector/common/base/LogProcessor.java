package com.hansight.kunlun.collector.common.base;


import com.hansight.kunlun.collector.common.exception.LogProcessorException;
import com.hansight.kunlun.collector.position.store.ReadPositionStore;
import com.hansight.kunlun.coordinator.config.AgentConfig;

import java.util.concurrent.Callable;

public interface LogProcessor<F, M, T> extends Callable<Void> {

    /**
     * 初始化
     *
     * @throws com.hansight.kunlun.collector.common.exception.LogProcessorException
     */
    void setup() throws LogProcessorException;

    /**
     * 需要实现的方法
     *
     * @throws com.hansight.kunlun.collector.common.exception.LogProcessorException
     */
    void run() throws LogProcessorException;

    /**
     * 清理资源
     *
     * @throws com.hansight.kunlun.collector.common.exception.LogProcessorException
     */
    void cleanup() throws LogProcessorException;

    /**
     * 线程调度入口
     *
     * @return
     * @throws Exception
     */
    Void call() throws Exception;

    /**
     * 线程调度入口
     *
     * @return
     * @throws Exception
     */
    void stop() throws Exception;

    /**
     * 设置一些配置信息
     *
     * @param conf
     */
    void config(AgentConfig conf);

    void setLexer(Lexer<M, T> lexer);

    public void process(LogReader<F> stream);

    void setWriter(LogWriter<T> writer);

    void setReadPositionStore( ReadPositionStore store);

}
