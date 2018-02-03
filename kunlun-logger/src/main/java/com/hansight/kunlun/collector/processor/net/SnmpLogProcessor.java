package com.hansight.kunlun.collector.processor.net;

import com.hansight.kunlun.collector.reader.SnmpLogReader;
import com.hansight.kunlun.collector.common.exception.LogProcessorException;

public class SnmpLogProcessor extends NetLogProcessor {
    @Override
    public void setup() throws LogProcessorException {
        super.setup();
        String version = get("version", "");
        if ("v3".equals(version)) {
            readers.add(new SnmpLogReader(protocol, host, port, get("version", ""), get("user", "")
                    , get("auth_key", ""), get("priv_key", "")
                    , get("auth_protocol", ""), get("priv_protocol", "")));
        } else {
            readers.add(new SnmpLogReader(protocol, host, port, get("version", "")));
        }

    }


}
