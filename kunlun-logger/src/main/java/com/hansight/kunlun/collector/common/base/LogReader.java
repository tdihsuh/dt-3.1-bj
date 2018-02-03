package com.hansight.kunlun.collector.common.base;

import java.io.Closeable;

public interface LogReader<T> extends Iterable<T>,Closeable {
    long skip(long skip);
    String path();
}
