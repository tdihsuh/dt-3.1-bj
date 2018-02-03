package com.hansight.kunlun.collector.stream;

import com.hansight.kunlun.collector.common.model.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by zhhui on 2014/11/5.
 */
public class JSONStream extends Stream<String> {
    private final static Logger logger = LoggerFactory.getLogger(JSONStream.class);
    private BufferedReader reader;

    public JSONStream(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public boolean hasNext() {
        StringBuilder builder = new StringBuilder();
        int openPointer = 0;
        int closePointer = 0;
        boolean quotes[] = {false, false};
        int c;
        item = null;
        try {
            while ((c = reader.read()) != -1) {
                builder.append((char) c);
                if (c == '\\') {
                    c = reader.read();
                    if (c != -1) {
                        builder.append((char) c);
                    } else {
                        break;
                    }

                }
                switch ((char) c) {
                    case '\"': {
                        if (!quotes[1])
                            quotes[0] = !quotes[0];
                        break;
                    }
                    case '\'': {
                        if (!quotes[0])
                            quotes[1] = !quotes[1];
                        break;
                    }
                    case '}': {
                        if (quotes[0] || quotes[1]) {
                            break;
                        }

                        closePointer++;
                        if (openPointer == closePointer) {
                            item = builder.toString();
                            return true;
                        }
                        break;
                    }
                    case '{': {
                        if (quotes[0] || quotes[1])
                            break;
                        openPointer++;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("MK A JSON LINE ERROR:{}",e);
        }
        return item != null;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
