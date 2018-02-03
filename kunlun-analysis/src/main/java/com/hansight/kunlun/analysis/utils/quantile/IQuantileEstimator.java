package com.hansight.kunlun.analysis.utils.quantile;

public interface IQuantileEstimator {

    void offer(long value);

    long getQuantile(double q);
}
