package com.hansight.kunlun.collector.writer;

import com.alibaba.fastjson.JSONObject;
import com.hansight.kunlun.collector.agent.Agent;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.collector.common.exception.LogWriteException;
import com.hansight.kunlun.coordinator.config.Config;
import com.hansight.kunlun.utils.Common;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class KafkaLogWriter<T extends Map<String, Object>> implements LogWriter<T>, Serializable,Flushable {

    protected ExecutorService threadPool = Executors.newFixedThreadPool(5);
    private  List<Executor> executors = new LinkedList<>();
    private List<KeyedMessage<String, byte[]>> cache;
    private int cacheSize = 1;
    private long times = 0;
    protected final static Logger logger = LoggerFactory.getLogger(KafkaLogWriter.class);
    private String topic="kunlun-kafka-default-topic";
   private ProducerConfig config;

    public boolean cache(KeyedMessage<String, byte[]> message) {
        cache.add(message);
        if (cache.size() == cacheSize) {
            logger.debug("cache {} take time {} ms", cache.size(), System.currentTimeMillis() - times);
            this.flush();
            if (logger.isDebugEnabled()) {
                times = System.currentTimeMillis();
            }
        }
        return true;
    }

    public void flush() {
        if (cache.size() != 0) {
            try {
                long start = 0;
                if (logger.isDebugEnabled()) {
                    start = System.currentTimeMillis();
                }
                Executor executor = get();
                executor.setCache(cache);
                threadPool.execute(executor);
                logger.debug("send  {} to kafka take time {} ms", cache.size(), System.currentTimeMillis() - start);
                cache = new ArrayList<>(cacheSize);
            } catch (InterruptedException e) {
                logger.error("send to kafka Interrupted", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Executor get() throws InterruptedException {
        while (true) {
            for (Executor temp : executors) {
                if (!temp.isRunning()) {
                    temp.start();
                    return temp;
                }
            }
            if (executors.size() < 5) {
                Executor executor = new Executor();
                executor.start();
                executors.add(executor);
                return executor;
            }
            logger.warn("Data sent to the Kafka too slow, wait for  {} ms", 500);
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    public KafkaLogWriter() {
        cache = new ArrayList<>(cacheSize);
       }

     public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    public void write(T t) throws LogWriteException {
        JSONObject object=new JSONObject(t);
            cache(new KeyedMessage<String, byte[]>(
                    topic,  object.toJSONString().getBytes()));
       }

    @Override
    public void config(Config conf) {
        this.topic=conf.get("topic");

        Properties props = new Properties();
        props.put(Common.ZOOKEEPER_CONNECT, Agent.GLOBAL.getProperty(Common.ZOOKEEPER_CONNECT, "local:2181"));
        props.put(Common.ZOOKEEPER_CONNECTION_TIMEOUT, Agent.GLOBAL.getProperty(Common.ZOOKEEPER_CONNECTION_TIMEOUT, "1000000"));
        props.put(Common.AUTO_COMMIT_ENABLE, Agent.GLOBAL.getProperty(Common.AUTO_COMMIT_ENABLE, "true"));
        props.put(Common.AUTO_COMMIT_INTERVAL_MS, Agent.GLOBAL.getProperty(Common.AUTO_COMMIT_INTERVAL_MS, "6000"));
        props.put("producer.type","async");
        props.put("kafka.metadata.partitions",conf.get("partitions"));
        String hosts=conf.get(Common.METADATA_BROKER_LIST);
        if(hosts==null){
            hosts=Agent.GLOBAL.getProperty(Common.METADATA_BROKER_LIST, "local:9092");

        }
        props.put("metadata.broker.list",hosts);


        config=new ProducerConfig(props);

    }

    @Override
    public boolean isFinished() {
        if (executors != null) {
            for (int i = executors.size() - 1; i >= 0; i--) {
                Executor temp = executors.get(i);
                if (temp.isRunning()) {
                    return false;
                } else {
                    executors.remove(temp);
                }
            }
        }
        return false;
    }

    class Executor implements Runnable, Closeable {
        private List<KeyedMessage<String, byte[]>> cache;
        private Producer<String, byte[]> producer;
        private boolean running = true;

        Executor() {
            producer = new Producer<>(config);
        }

        @Override
        public void run() {
            long start = 0;
            running = true;
            if (logger.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            do {
                try {
                    producer.send(cache);
                    running=false;
                }catch (Exception e){
                    logger.error("kafka seed Exception {}",e);
                    running=true;
                }

            }while (!running);
               logger.debug("send  {} data to kafka take {} ms", cache.size(), System.currentTimeMillis() - start);
        }

        public void setCache(List<KeyedMessage<String, byte[]>> cache) {
            this.cache = cache;
        }

        @Override
        public void close() throws IOException {
            producer.close();
        }

        public boolean isRunning() {
            return running;
        }

        public void start() {
            this.running = true;
        }
    }

    @Override
    public void close() {
        if (cache.size() > 0)
            flush();
        while (executors.size() > 0) {
            for (int i = executors.size() - 1; i >= 0; i--) {
                Executor temp = executors.get(i);
                if (!temp.isRunning()) {
                    try {
                        temp.close();
                        executors.remove(i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                threadPool.shutdownNow();
                if (!threadPool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                    logger.warn("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted.
            threadPool.shutdownNow();
            // Preserve interrupt status.
            Thread.currentThread().interrupt();
        }
    }
}