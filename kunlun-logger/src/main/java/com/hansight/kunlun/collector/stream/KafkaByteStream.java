package com.hansight.kunlun.collector.stream;

import com.hansight.kunlun.collector.common.model.Stream;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zhhui on 2014/11/5.
 */
public class KafkaByteStream extends Stream<ByteBuffer> {
    private final static Logger logger = LoggerFactory.getLogger(KafkaByteStream.class);
    ConsumerIterator<byte[],byte[]> iterator;

    public KafkaByteStream(KafkaStream<byte[],byte[]> stream) {
        iterator=stream.iterator();
    }

    @Override
    public boolean hasNext() {
        boolean flag=iterator.hasNext();
       if(flag) {
           item = ByteBuffer.wrap(iterator.next().message());
       }
        return flag;
    }

    @Override
    public void close() throws IOException {

    }
}
