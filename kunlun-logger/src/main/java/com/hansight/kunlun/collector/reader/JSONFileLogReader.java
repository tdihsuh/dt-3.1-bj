package com.hansight.kunlun.collector.reader;

import com.hansight.kunlun.collector.stream.JSONStream;

import java.io.IOException;
import java.util.Iterator;

public class JSONFileLogReader extends FileLogReader<String> {
    @Override
    public Iterator<String> iterator() {
        return new JSONStream(reader);
    }

    @Override
    public long skip(long skip) {
        try {
            return reader.skip(skip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}