package com.hansight.kunlun.collector.transfer;

/**
 * Created by zhhui on 2014/11/6.
 */
public interface Transfer<F, T> {
    T transfer(F v);

}
