package com.hansight.kunlun.collector.writer;

import com.hansight.kunlun.collector.agent.Agent;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.collector.common.exception.LogWriteException;
import com.hansight.kunlun.collector.common.utils.AgentConstants;
import com.hansight.kunlun.collector.common.utils.ESIndexMaker;
import com.hansight.kunlun.coordinator.config.Config;
import com.hansight.kunlun.utils.Common;
import com.hansight.kunlun.utils.EsUtils;
import com.hansight.kunlun.utils.Pair;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Flushable;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author:zhhui_yan
 * DateTime:2014/7/29 16:43.
 */
public class ElasticSearchLogWriter<T extends Map<String, Object>> implements LogWriter<T>, Flushable {
    protected final static Logger logger = LoggerFactory.getLogger(ElasticSearchLogWriter.class);
    protected ExecutorService threadPool;
    private List<Executor> executors;
    private ESIndexMaker maker;
    private final String mark;
    private final String indexPre;
    private int cacheSize = 10;
    private int caches = 0;
    private BulkRequestBuilder builder;
    protected TransportClient client;
    String type;
    private boolean running;
    protected final Long PROCESSOR_THREAD_WAIT_TIMES = Long.valueOf(Agent.GLOBAL.getProperty(AgentConstants.PROCESSOR_THREAD_WAIT_TIMES, AgentConstants.PROCESSOR_THREAD_WAIT_TIMES_DEFAULT));

    public ElasticSearchLogWriter() {
        cacheSize = Integer.valueOf(Agent.GLOBAL.getProperty(AgentConstants.AGENT_WRITE_CACHE_SIZE, AgentConstants.AGENT_WRITE_CACHE_SIZE_DEFAULT));
        //client = EsUtils.getEsClient();
        mark = "@timestamp";
        indexPre = "logs_";
        maker = new ESIndexMaker();
        executors = new LinkedList<>();
        threadPool = Executors.newFixedThreadPool(3);
        running = true;
    }


    public synchronized boolean save(T t, String index) {

        if (t == null || index == null)
            return true;
        if (builder == null) {
            builder = client.prepareBulk();
        }
        builder.add(client.prepareIndex(index, type)
                .setSource(t));
        caches++;
        if (caches >= cacheSize) {
            flush();
        }
        return true;
    }

    private class Executor extends Thread {

        private volatile BulkRequestBuilder builder;
        private int caches = 0;

        public void setBuilder(final BulkRequestBuilder builder) {
            this.builder = builder;
        }

        public void setCaches(int caches) {
            this.caches = caches;
        }

        @Override
        public void run() {
            long timeTaken = 0;
            if (logger.isInfoEnabled()) {
                timeTaken = System.currentTimeMillis();
            }
            boolean ok;
            int i = 0;
            do {

                try {
                    if (builder != null && builder.numberOfActions() > 0) {
                        BulkResponse response = builder.execute().actionGet();
                        if (response.hasFailures()) {
                            logger.error("es save has error info :{}", response.buildFailureMessage());
                        }
                    }
                    ok = true;
                } catch (Exception e) {
                    logger.error("ES ERROR: try submit builder:{},size:{} error info:{}", builder.toString(), builder.numberOfActions(), e);
                    if (i >= 10)
                        logger.error("ES ERROR:after {} times try submit builder:{},size:{} error info:{}", i, builder.toString(), builder.numberOfActions(), e);
                    else
                        logger.error("ES ERROR: try submit builder:{},size:{} error info:{}", builder.toString(), builder.numberOfActions(), e);

                    ok = builder == null;
                }
            } while (!ok && running);
            timeTaken = System.currentTimeMillis() - timeTaken;
            if (timeTaken > 10000)
                logger.warn("save  {} data take {} ms", caches, timeTaken);
            else
                logger.debug("save  {} data take {} ms", caches, timeTaken);
            builder = null;
            caches = 0;


        }

        public boolean isFinished() {
            return builder == null;
        }
    }

    private synchronized Executor get() {
        Executor executor = null;
        label:
        while (running) {
            for (Executor executor1 : executors) {
                executor = executor1;
                if (executor != null && executor.isFinished()) {
                    break label;
                }
            }
            if (executors.size() < 5) {
                executor = new Executor();
                executors.add(executor);
                break;
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
                    logger.debug("caches:{}, waiting   {} ms for es save data", caches, PROCESSOR_THREAD_WAIT_TIMES);
                } catch (InterruptedException e) {
                    logger.error("es save has error info :{}", e);
                }
            }
        }

        return executor;
    }

    public boolean isFinished() {
        if (executors != null) {
            for (int i = executors.size() - 1; i >= 0; i--) {
                Executor temp = executors.get(i);
                if (!temp.isFinished()) {
                    return false;
                }
            }
        }
        logger.debug("writer is finished");
        return true;
    }


    @Override
    public synchronized void flush() {
        if (builder != null) {
            Executor executor = get();
            if (executor != null) {
                executor.setBuilder(builder);
                executor.setCaches(caches);
                threadPool.execute(executor);
                builder = null;
                caches = 0;
            }

        }
    }

    @Override
    public void close() {
        flush();
        try {
            running = false;
            while (!isFinished()) {
                TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
            }
            executors.clear();
            client.close();
            threadPool.shutdownNow();

        } catch (Exception e) {
            logger.error(" logWriter close error:{}", e);
        }
    }

    @Override
    public void write(T t) throws LogWriteException {
        String index;
        if (t.get("error") == null) {
            Pair<String, Date> pair;
            try {
                pair = maker.indexSuffix(t);
            } catch (ParseException e) {
                throw new LogWriteException("index maker error", e);
            }
            t.put(mark, pair.second());
            index = indexPre + pair.first().split(" ")[0];
        } else {
            index = "parse_error";
            t.put(mark, new Date());
        }
        save(t, index);
    }

    @Override
    public void config(Config conf) {
        this.type = conf.get("type") + "_" + conf.get("category");
        String name;
        String address;
        name = conf.get(Common.ES_CLUSTER_NAME);
        if (name == null)
            name = Common.get(Common.ES_CLUSTER_NAME);
        address = conf.get(Common.ES_CLUSTER_HOST);
        if (address == null) {
            address = Common.get(Common.ES_CLUSTER_HOST);
        }
        client = EsUtils.getNewClient(name, address);
    }
}