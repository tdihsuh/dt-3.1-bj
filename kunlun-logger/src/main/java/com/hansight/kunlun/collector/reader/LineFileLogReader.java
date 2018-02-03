package com.hansight.kunlun.collector.reader;

import com.hansight.kunlun.collector.stream.LineStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

public class LineFileLogReader extends FileLogReader<String> {
    final static Logger logger = LoggerFactory.getLogger(LineFileLogReader.class);
    @Override
    public Iterator<String> iterator() {
        return new LineStream(reader);
    }

    @Override
    public long skip(long skip) {
        try {
            return reader.skip(skip);
        } catch (IOException e) {
            logger.error("lineFileLogReader skip error:{}",e);
        }
        return -1;
    }


}