package com.hansight.kunlun.collector.transfer;

import com.hansight.kunlun.collector.common.model.Event;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhhui on 2014/11/6.
 */
public class NoTransfer implements Transfer<Event, Event> {
    @Override
    public Event transfer(Event v) {
        return v;
    }
}
