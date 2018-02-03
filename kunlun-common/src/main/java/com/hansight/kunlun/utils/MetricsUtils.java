package com.hansight.kunlun.utils;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import java.util.Queue;

/**
 * Created by taoistwar on 2014/12/10.
 */
public class MetricsUtils {
    public static final MetricRegistry metrics = new MetricRegistry();

    public static <T> void register(String name, Gauge<T> t) {
        metrics.register(name, t);
    }

    public static void newGauge(String name, final Queue<?> queue) {
        metrics.register(name,
                new Gauge<Integer>() {
                    public Integer getValue() {
                        return queue.size();
                    }
                });
    }
    
    public static void newGauge(String name, final String tips) {
        metrics.register(name,
                new Gauge<String>() {
                    public String getValue() {
                        return tips;
                    }
                });
    }

    public static Counter newCounter(String name) {
        return metrics.counter(name);
    }

}
