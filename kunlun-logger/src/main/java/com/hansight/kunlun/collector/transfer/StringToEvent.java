package com.hansight.kunlun.collector.transfer;

import com.hansight.kunlun.collector.common.model.Event;

import java.nio.ByteBuffer;

/**
 * Created by zhhui on 2014/11/6.
 */
public class StringToEvent implements Transfer<String, Event> {
    ByteBufferToEvent bufferToEvent = new ByteBufferToEvent();

    @Override
    public Event transfer(String v) {
        return bufferToEvent.transfer(ByteBuffer.wrap(v.getBytes()));
    }
}
