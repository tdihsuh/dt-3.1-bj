package com.hansight.kunlun.collector.reader;

import com.hansight.kunlun.collector.stream.WinEvtStream;

import java.nio.ByteBuffer;
import java.util.Iterator;

public class WinEvtFileLogReader extends FileLogReader<ByteBuffer> {
    WinEvtStream stream = new WinEvtStream(path);
    @Override
    public Iterator<ByteBuffer> iterator() {
        return stream;
    }

    @Override
    public long skip(long skip) {
        int i = 0;
        while (stream.hasNext()) {
            i++;
            if (i == skip) {
                return skip;
            }
        }
        return i;
    }
}