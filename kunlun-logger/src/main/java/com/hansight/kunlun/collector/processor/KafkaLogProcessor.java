package com.hansight.kunlun.collector.processor;

import com.hansight.kunlun.collector.agent.Agent;
import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.collector.common.exception.LogProcessorException;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.reader.KafkaLogReader;
import com.hansight.kunlun.collector.transfer.ByteBufferToEvent;
import com.hansight.kunlun.collector.transfer.Transfer;
import com.hansight.kunlun.collector.writer.ElasticSearchLogWriter;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.WorkerStatus;
import com.hansight.kunlun.utils.Common;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author:zhhui
 * DateTime:2014/7/29 15:55.
 * iis,nigix
 */
public class KafkaLogProcessor extends DefaultLogProcessor<ByteBuffer> {
    final static Logger logger = LoggerFactory.getLogger(KafkaLogProcessor.class);
    public ExecutorService monitorPool = Executors.newCachedThreadPool();
    protected List<Executor> executors;

    private static final String GROUP_ID = "group.id";

    private Map<String, Integer> map = new HashMap<>();

    class Executor extends DefaultExecutor<ByteBuffer> {
        LogReader<ByteBuffer> reader;
        public Executor( LogReader<ByteBuffer> reader,long cacheSize,long PROCESSOR_THREAD_WAIT_TIMES) {
            super( reader, cacheSize,PROCESSOR_THREAD_WAIT_TIMES);
        }


        @Override
        public LogWriter<Map<String, Object>> makeWriter() {
            LogWriter<Map<String, Object>> writer=null;
            String clazzName = Agent.WRITER_CLASS_MAPPING.getProperty(conf.get("writer"), ElasticSearchLogWriter.class.getName());
            try {
                Class<LogWriter<Map<String, Object>>> writerClass = (Class<LogWriter<Map<String, Object>>>) Class
                        .forName(clazzName);
                writer = writerClass.newInstance();
                writer.config(conf);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.error("INIT LOG WRITER ERROR:{}", e);
            }

            return writer;
        }

        @Override
        public Lexer<Event, Map<String, Object>> makeLexer() {
            if(lexers.size()==0)
                return lexer;
             return lexer.newClone();
        }

        @Override
        public MetricService getMetric() {
            return metricService;
        }

        @Override
        public Transfer<ByteBuffer, Event> getTransfer() {
            return transfer;
        }

        public void setReader(LogReader<ByteBuffer> reader) {
            this.reader = reader;
        }

    }

    class Monitor implements Runnable {
        List<KafkaStream<byte[], byte[]>> streams;

        public Monitor(List<KafkaStream<byte[], byte[]>> streams) {
            this.streams = streams;
        }

        private void sleep() {
            while (readers.size() >= 2 * poolSize) {
                try {
                    TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
                    logger.debug("monitor : waiting {} ms for file  process ", PROCESSOR_THREAD_WAIT_TIMES);
                    if (!running) {
                        return;
                    }
                } catch (InterruptedException e) {
                    logger.error("INTERRUPTED ERROR:{}", e);
                }
            }
        }

        @Override
        public void run() {
            for (KafkaStream<byte[], byte[]> stream : streams) {
                sleep();
                readers.add(new KafkaLogReader(stream));
            }

        }
    }

    private synchronized Executor get(LogReader<ByteBuffer> processor) {
        // Executor executor=  new Executor(processor);
        Executor executor = null;
        label:
        while (running) {
            for (Executor temp : executors) {
                if (temp.isFinished()) {
                    executor = temp;
                    executor.setReader(processor);
                    break label;
                }
            }
            if (executors.size() < poolSize) {
                executor = new Executor(processor,cacheSize,PROCESSOR_THREAD_WAIT_TIMES);
                executors.add(executor);
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
                logger.debug("top :{}  waiting {} ms for  processor process ", processor, PROCESSOR_THREAD_WAIT_TIMES);
            } catch (InterruptedException e) {
                logger.error("INTERRUPTED ERROR:{}", e);
            }
        }
        return executor;
    }

    @Override
    public void process(LogReader<ByteBuffer> stream) {
        Executor executor = get(stream);
        if (executor != null)
            threadPool.execute(executor);
    }
    private boolean isFinished() {
        if(executors!=null){
            for (int i = executors.size()-1; i >=0; i--) {
                Executor temp=executors.get(i);
                if (!temp.isFinished()) {
                    return false;
                }
            }
            executors.clear();
        }

        return true;
    }
    @Override
    public void stop() throws Exception {
        super.stop();
        while (!isFinished()){
            TimeUnit.MILLISECONDS.sleep(100);
        }
        while (!threadPool.isTerminated()) {
            TimeUnit.SECONDS.sleep(1);
            threadPool.shutdownNow();

        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setup() throws LogProcessorException {
        threadPool = Executors.newFixedThreadPool(poolSize);
        executors = new CopyOnWriteArrayList<>();
        this.lexer.setWriter(writer);
        transfer = new ByteBufferToEvent();

        Properties props = new Properties();
        props.put(Common.ZOOKEEPER_CONNECTION_TIMEOUT, Agent.GLOBAL.getProperty(Common.ZOOKEEPER_CONNECTION_TIMEOUT, "1000000"));
        props.put(Common.ZOOKEEPER_SESSION_TIMEOUT_MS, Agent.GLOBAL.getProperty(Common.ZOOKEEPER_SESSION_TIMEOUT_MS, "30000"));
        props.put(Common.AUTO_OFFSET_RESET, Agent.GLOBAL.getProperty(Common.AUTO_OFFSET_RESET, "smallest"));
        props.put(" offsets.topic.segment.bytes", "100");
        String connect = conf.get(Common.ZOOKEEPER_CONNECT);
        if (connect == null) {
            connect = Agent.GLOBAL.getProperty(Common.ZOOKEEPER_CONNECT, "local:2181");
        }
        props.put(Common.ZOOKEEPER_CONNECT, connect);
        String groupId = conf.get(GROUP_ID);
        if (groupId == null) {
            groupId = "";
        }
        props.put(GROUP_ID, groupId);
        ConsumerConnector consumerConnector = Consumer
                .createJavaConsumerConnector(new ConsumerConfig(props));
        String topic = conf.get("topic");

        int partitionsNum;
        try {
            partitionsNum = Integer.valueOf(conf.get("partitionsNum"));
        } catch (NumberFormatException e) {
            logger.error("config error must be number error :{}", e);
            partitionsNum = 1;
        }
        map.put(topic, partitionsNum);
        List<KafkaStream<byte[], byte[]>> streams = consumerConnector
                .createMessageStreams(map).get(topic);

        monitorPool.execute(new Monitor(streams));
    }

    @Override
    public void cleanup() throws LogProcessorException {

    }
}
