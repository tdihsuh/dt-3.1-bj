package com.hansight.kunlun.collector.common.model;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Created by zhhui on 2014/11/5.
 */
public abstract class Stream<T> implements Iterator<T>, Closeable {
    protected T item;


    @Override
    public T next() {
        return item;
    }

    @Override
    public void remove() {
        this.hasNext();
    }
}
