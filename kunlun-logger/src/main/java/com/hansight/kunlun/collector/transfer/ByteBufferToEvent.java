package com.hansight.kunlun.collector.transfer;

import com.hansight.kunlun.collector.common.model.Event;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhhui on 2014/11/6.
 */
public class ByteBufferToEvent implements Transfer<ByteBuffer, Event> {
    @Override
    public Event transfer(ByteBuffer v) {
        Event event = new Event();
        Map<CharSequence, CharSequence> map = new LinkedHashMap<>();
        // map.put("path", path);
        event.setHeader(map);
        event.setBody(v);
        return event;
    }
}
