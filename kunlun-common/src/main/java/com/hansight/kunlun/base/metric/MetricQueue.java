package com.hansight.kunlun.base.metric;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Author:zhhui
 * DateTime:2014/8/14 15:38.
 */
public class MetricQueue<T> extends LinkedList<T> {
    private static Map<String, MetricRegistry> registries = new ConcurrentHashMap<>();
    private final Meter reader;
    private final Meter write;
    public static MetricRegistry get(Class clazz, String path) throws IOException {
        MetricRegistry metrics;
        if (registries.containsKey(clazz.getSimpleName())) {
            metrics = registries.get(clazz.getName());
        } else {
            File file=new File(path);
            if(!file.exists()){
                file.mkdirs();
            }
            metrics = new MetricRegistry();
            registries.put(clazz.getName(), metrics);
            CsvReporter.forRegistry(metrics)
                    .formatFor(Locale.US)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build(file).start(1, TimeUnit.MINUTES);
        }
        return metrics;
    }

    public MetricQueue(Class clazz, String path) throws IOException {
        super();
        MetricRegistry metrics = get(clazz, path);
        reader = metrics.meter(MetricRegistry.name(clazz, "reader"));
        write = metrics.meter(MetricRegistry.name(clazz, "writer"));
    }

    @Override
    public boolean add(T t) {
        boolean flag = super.add(t);
        if (flag)
            reader.mark();
        return flag;
    }

    @Override
    public boolean offer(T t) {
        boolean flag = super.offer(t);
        if (flag)
            reader.mark();
        return flag;
    }

    @Override
    public T poll() {
        T t = super.poll();
        if (t != null)
            write.mark();
        return t;
    }


    @Override
    public boolean remove(Object o) {
        boolean flag = super.remove(o);
        if (flag)
            write.mark();
        return flag;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean flag = super.addAll(c);
        if (flag)
            reader.mark(c.size());
        return flag;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean flag = super.removeAll(c);
        if (flag)
            write.mark(c.size());
        return flag;
    }
}
