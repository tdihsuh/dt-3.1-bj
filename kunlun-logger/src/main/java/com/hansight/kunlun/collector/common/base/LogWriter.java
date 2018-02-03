package com.hansight.kunlun.collector.common.base;

import com.hansight.kunlun.collector.common.exception.LogWriteException;
import com.hansight.kunlun.coordinator.config.Config;

import java.io.Closeable;
import java.io.Flushable;

public interface LogWriter<T> extends Closeable,Flushable {
    void write(T t) throws LogWriteException;
    public void config(Config conf);
    public boolean isFinished();

}
