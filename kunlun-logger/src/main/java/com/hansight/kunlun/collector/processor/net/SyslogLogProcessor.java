package com.hansight.kunlun.collector.processor.net;

import com.hansight.kunlun.collector.reader.SyslogLogReader;
import com.hansight.kunlun.collector.common.exception.LogProcessorException;

public class SyslogLogProcessor extends NetLogProcessor {
    @Override
    public void setup() throws LogProcessorException {
        super.setup();
         readers.add(new SyslogLogReader(protocol, host, port, encoding));
    }
}
