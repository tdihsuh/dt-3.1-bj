package com.hansight.kunlun.collector.forwarder.parser;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogParser;
import com.hansight.kunlun.collector.common.exception.LogParserException;
import com.hansight.kunlun.collector.writer.ElasticSearchLogWriter;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.common.serde.AvroSerde;
import com.hansight.kunlun.collector.common.utils.ForwarderConstants;
import com.hansight.kunlun.collector.forwarder.Forwarder;
import com.hansight.kunlun.collector.reader.KafkaLogReader;
import com.hansight.kunlun.coordinator.config.ForwarderConfig;
import com.hansight.kunlun.coordinator.metric.MetricService;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author:zhhui DateTime:2014/7/29 14:36.
 */
public abstract class DefaultLogParser implements LogParser<Event,Map<String, Object>>{
    protected final static Logger logger = LoggerFactory.getLogger(DefaultLogParser.class);
    protected final Integer cacheSize = Integer.parseInt(Forwarder.GLOBAL.getProperty(ForwarderConstants.LOG_PARSER_CACHE_SIZE, ForwarderConstants.LOG_PARSER_CACHE_SIZE_DEFAULT));
    protected final Integer cacheThreads = Integer.parseInt(Forwarder.GLOBAL.getProperty(ForwarderConstants.LOG_PARSER_CACHE_THREADS, ForwarderConstants.LOG_PARSER_CACHE_THREADS_DEFAULT));
    protected final Integer threadWaiting = Integer.parseInt(Forwarder.GLOBAL.getProperty(ForwarderConstants.LOG_PARSER_CACHE_THREAD_WAITING, ForwarderConstants.LOG_PARSER_CACHE_THREAD_WAITING_DEFAULT));
    protected ExecutorService threadPool;
    protected KafkaLogReader reader;
    public MetricService metricService;

    protected ForwarderConfig conf;
    protected Boolean running = true;

    @Override
    public void setConf(ForwarderConfig conf) {
        this.conf = conf;
        String topic = conf.getId();
        /*try {
        TODO maybe to delete
            int partitions = Integer.parseInt(Forwarder.GLOBAL.getProperty(ForwarderConstants.KAFKA_METADATA_PARTITIONS, ForwarderConstants.KAFKA_METADATA_PARTITIONS_DEFAULT));
            reader = new KafkaLogReader(topic, partitions);
            threadPool = Executors.newFixedThreadPool(partitions + cacheThreads + 1);
            logger.debug("topic:{},type:{},category:{}", conf.get("id"), conf.get("type"), conf.get("category"));
            metricService = new MetricService(conf.getId(), conf.get("forwarder"), ProcessorType.FORWARDER);

        } catch (IOException e) {
            logger.error(" kafka connect error, please check your conf for this ,or see the kafka you conf is running !:{}", e);
        }*/
    }

    @Override
    public void parse() throws LogParserException {

      /* TODO  List<KafkaStream<byte[], byte[]>> streams = reader.getStreams();
        int i = 0;
        for (KafkaStream<byte[], byte[]> stream : streams) {
            threadPool.execute(new Processor(stream, i++));
        }*/
    }

    @Override
    public void run() {
        try {
            parse();
        } catch (Exception e) {
            logger.error("error:{}", e);
        }
    }

    class Processor extends Thread {
        protected List<Lexer> lexers;
        Queue<Event> cache = null;
        Lexer<Event, Map<String, Object>> lexer;
        long cachingFromTime;
        long heartbeat;
        private final KafkaStream<byte[], byte[]> stream;
        protected final AvroSerde serde = new AvroSerde();
        private final int part;

        public Processor(KafkaStream<byte[], byte[]> stream, int part) {
            this.part = part;
            this.stream = stream;
            lexers = new LinkedList<>();
            newCache();
        }

        private void heartbeat() {
            heartbeat = System.currentTimeMillis();

        }

        @SuppressWarnings("unchecked")
        public void process(KafkaStream<byte[], byte[]> stream) {
            for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
                try {
                    if (!running) {
                        break;
                    }
                    parse(serde.deserialize(msgAndMetadata.message()));
                } catch (IOException e) {
                    logger.error("kafka  read event error :{}", e);
                } catch (InterruptedException e) {
                    logger.error("kafka  InterruptedException error :{}", e);
                }
            }
        }

        private void newCache() {
            cache = new LinkedList<>();
           /* if (metric) {
                try {
                    cache = new MetricQueue<>(Forwarder.class, path);
                } catch (IOException e) {
                    logger.error("metric error:{}", e);
                }
            } else {

            }*/
        }

        @SuppressWarnings("unchecked")
        private Lexer<Event, Map<String, Object>> get() throws InterruptedException {
            Lexer<Event, Map<String, Object>> lx;
            while (true) {
                for (Lexer<Event, Map<String, Object>> temp : lexers) {
                    if (temp.isFinished()) {
                        temp.start();
                        return temp;
                    }
                }
                if (lexers.size() < cacheThreads) {
                    lx = getLexer();
                    lx.setMetric(metricService);
                    lx.setWriter(new ElasticSearchLogWriter<Map<String, Object>>());
                    lx.start();
                    lx.set("lexer " + (lexers.size() + 1));
                    lexers.add(lx);
                    return lx;
                }
                logger.warn("parse is too slow, waiting for {} ms.", threadWaiting);
                TimeUnit.MILLISECONDS.sleep(threadWaiting);
            }
        }

        private void flush() throws InterruptedException {
            lexer = get();
            lexer.setValueToParse(cache);
            threadPool.submit(lexer);
            newCache();
        }

        private void parse(Event event) throws InterruptedException {
            cache.add(event);
            heartbeat();
            if (cache.size() >= cacheSize) {
                logger.debug(" cache {} events take {} ms", cache.size(), System.currentTimeMillis() - cachingFromTime);
                flush();
                if (logger.isDebugEnabled()) {
                    cachingFromTime = System.currentTimeMillis();
                }
            }
        }

        @Override
        public void run() {
            threadPool.execute(new Runnable() {
                /**
                 * an thread checking heartbeat when too long time no ,then flush data to parse 
                 */
                @Override
                public void run() {
                    while (running) {
                        try {
                            if ((System.currentTimeMillis() - heartbeat) > 100 * threadWaiting) {
                                if (cache.size() > 0) {
                                    logger.info("Long time no data entry, save the buffer(size:{}) to the database ", cache.size());
                                    flush();
                                }
                            }
                            TimeUnit.MILLISECONDS.sleep(100 * threadWaiting);
                        } catch (InterruptedException e) {
                            logger.error(" InterruptedException error :{}", e);
                        }
                    }
                }
            });
            process(this.stream);
        }
    }



    @Override
    public void stop() throws InterruptedException {
        this.running = false;
        while (!threadPool.isTerminated()) {
            TimeUnit.SECONDS.sleep(1);
            threadPool.shutdown();
        }
        logger.info("topic:{},stopped by user", conf.getId());
    }
}
