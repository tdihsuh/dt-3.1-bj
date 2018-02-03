package com.hansight.kunlun.collector.stream;

import com.hansight.kunlun.collector.common.model.Stream;
import com.hansight.kunlun.collector.common.model.WinEvt;
import com.sun.jna.platform.win32.WinDef;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.hansight.kunlun.collector.common.model.WinEvt.*;

/**
 * Created by zhhui on 2014/11/5.
 */
public class WinEvtStream extends Stream<ByteBuffer> {
    static final WinEvt evt = INSTANCE;
    private WinEvt.EVT_HANDLE handle;
    private WinEvt.EVT_HANDLE[] events = null;
    private WinDef.DWORD[] returned = null;
    private int index = 0;

    public WinEvtStream(String path) {
        handle = evt.EvtQuery(null, path, null, EvtQueryFilePath);
    }

    byte[] eventContent(WinEvt.EVT_HANDLE events) {
        WinDef.DWORD bufferSize = new WinDef.DWORD();
        WinDef.DWORDByReference usedBuffer = new WinDef.DWORDByReference();
        WinDef.DWORDByReference propertyCount = new WinDef.DWORDByReference();
        boolean flag = evt.EvtRender(null, events, EvtRenderEventXml, bufferSize, null, usedBuffer, propertyCount);
        if (!flag) {
            bufferSize = usedBuffer.getValue();
            byte[] content = new byte[bufferSize.intValue()];
            evt.EvtRender(null, events, EvtRenderEventXml, bufferSize, content, usedBuffer, propertyCount);
            byte[] values = new byte[content.length / 2];
            for (int i = 0; i < values.length; i++) {
                values[i] = content[2 * i];
            }
            return values;
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        int size = 1;
        if (events == null) {
            events = new WinEvt.EVT_HANDLE[size];
            returned = new WinDef.DWORD[size];
            evt.EvtNext(handle, size, events, Integer.MAX_VALUE, 0, returned);
        }
        if (index < returned.length - 1) {
            if (evt.EvtNext(handle, size, events, Integer.MAX_VALUE, 0, returned)) {
                index = 0;
                byte[] value = eventContent(events[index]);
                evt.EvtClose(events[index]);
                if (value == null)
                    return false;
                item = ByteBuffer.wrap(value);
                index++;
                return true;
            } else {
                return false;
            }
        } else {
            byte[] value = eventContent(events[index]);
            evt.EvtClose(events[index]);
            if (value == null)
                return false;
            item = ByteBuffer.wrap(value);
            index++;
            return true;
        }
    }

    @Override
    public void close() throws IOException {
        evt.EvtClose(handle);
    }
}
