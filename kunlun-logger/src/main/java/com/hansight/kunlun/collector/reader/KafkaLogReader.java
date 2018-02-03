package com.hansight.kunlun.collector.reader;

import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.common.serde.AvroSerde;
import com.hansight.kunlun.collector.stream.KafkaByteStream;
import com.hansight.kunlun.utils.Common;
import com.hansight.kunlun.collector.forwarder.Forwarder;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaLogReader implements LogReader<ByteBuffer> {
    KafkaStream<byte[], byte[]> stream;

    public KafkaLogReader(KafkaStream<byte[], byte[]> stream){
        this.stream = stream;
    }


    @Override
    public long skip(long skip) {
        return 0;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        return new KafkaByteStream(stream);
    }


}
