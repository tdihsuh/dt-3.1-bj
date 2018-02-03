package com.hansight.kunlun.collector.stream;

import com.hansight.kunlun.collector.common.model.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by zhhui on 2014/11/5.
 */
public class LineStream extends Stream<String>{
private final static Logger logger = LoggerFactory.getLogger(LineStream.class);
    private BufferedReader reader;

    public LineStream(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public boolean hasNext() {
        try {
            item = reader.readLine();
        } catch (Exception e) {
            logger.error("MK A LINE ERROR:{}",e);
            item = null;
        }
        return item != null ;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
