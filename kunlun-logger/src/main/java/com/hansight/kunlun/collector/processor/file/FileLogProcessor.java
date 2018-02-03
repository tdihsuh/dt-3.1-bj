package com.hansight.kunlun.collector.processor.file;

import com.hansight.kunlun.collector.agent.Agent;
import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.collector.common.model.ReadPosition;
import com.hansight.kunlun.collector.processor.Executor;
import com.hansight.kunlun.collector.writer.ElasticSearchLogWriter;
import com.hansight.kunlun.collector.processor.DefaultLogProcessor;
import com.hansight.kunlun.collector.reader.FileLogReader;
import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.exception.LogProcessorException;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.transfer.ByteBufferToEvent;
import com.hansight.kunlun.collector.transfer.StringToEvent;
import com.hansight.kunlun.collector.transfer.Transfer;
import com.hansight.kunlun.coordinator.metric.MetricException;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.WorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author:zhhui
 * DateTime:2014/7/29 15:55.
 * iis,nigix
 */
public abstract class FileLogProcessor<F> extends DefaultLogProcessor<F> {
    final static Logger logger = LoggerFactory.getLogger(FileLogProcessor.class);

    String readerName;
    protected List<FileExecutor> executors;

    class FileExecutor extends Executor<F> {
        public FileExecutor(LogReader<F> reader, long cacheSize, long PROCESSOR_THREAD_WAIT_TIMES) {
            super(reader,cacheSize,PROCESSOR_THREAD_WAIT_TIMES);
        }


        @Override
        public LogWriter<Map<String, Object>> makeWriter() {
            LogWriter<Map<String, Object>> writer = null;
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
            Lexer<Event, Map<String, Object>> lexer = ((FileLogReader<F>) reader).lexer();
            if (lexers.size() == 0) {
                return lexer;
            }
            return lexer.newClone();
        }

        @Override
        public MetricService getMetric() {
            return metricService;
        }

        @Override
        public Transfer<F, Event> getTransfer() {
            return transfer;
        }

        public void setReader(FileLogReader<F> reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            long timeTaken = 0;
            FileLogReader<F> fileLogReader = (FileLogReader<F>) reader;
            try {

                if (logger.isInfoEnabled()) {
                    timeTaken = System.currentTimeMillis();
                }
                newCache();
                int i = 0;
                for (F object : fileLogReader) {
                    if (!running) {
                        break;
                    }
                    try {
                        fileLogReader.readPosition().recordAdd();
                        if ("winevt".equals(readerName)) {
                            fileLogReader.readPosition().positionAdd(1);
                        } else {
                            fileLogReader.readPosition().positionAdd((long) object.toString().length() + fileLogReader.getLineSeparatorLength());
                        }
                        parse(transfer.transfer(object));
                        store.set(fileLogReader.readPosition());
                        metricService.mark();
                        if (i == 0) {
                            metricService.setProcessorStatus(WorkerStatus.ConfigStatus.SUCCESS);
                            i++;
                        }

                    } catch (Exception e) {
                        logger.error("PROCESSOR ERROR :{}", e);
                        try {
                            metricService.setProcessorStatus(WorkerStatus.ConfigStatus.FAIL);
                        } catch (MetricException e1) {
                            logger.error("METRIC ERROR :{}", e1);
                        }
                    }
                }
            } finally {
                if (logger.isInfoEnabled()) {
                    timeTaken = System.currentTimeMillis() - timeTaken;
                }
                try {
                    clear();
                } catch (Exception e) {
                    logger.error("file:{} clear  ERROR :{}", reader.path(), e);
                }

                try {
                    if (running)
                        fileLogReader.readPosition().setFinished(true);
                    synchronized (logger){
                        ReadPosition position;
                    do {
                        store.set(fileLogReader.readPosition());
                        store.flush();
                        position = store.get(fileLogReader.readPosition().getPath());
                       } while (position==null|| ! position.records().equals(fileLogReader.readPosition().records()));
                    }


                } catch (IOException e) {
                    logger.error("file:{} POSITION FLUSH ERROR :{}", reader.path(), e);
                }


                try {
                    inProcess.remove(reader.path());
                    reader.close();
                } catch (IOException e) {
                    logger.error("file:{} READER CLOSE ERROR :{}", reader.path(), e);
                } finally {
                    reader = null;
                }
                if (running) {
                    logger.info("file:{} process  finished  : records {} ,take {} ms", fileLogReader.path(), fileLogReader.readPosition().records(), timeTaken);
                } else {
                    logger.info("file:{} process  finished by  killed : records {},take {} ms ", fileLogReader.path(), fileLogReader.readPosition().records(), timeTaken);
                }
            }


        }
        }

    private boolean isFinished() {
        if (executors != null) {
            for (Executor<F> temp : executors) {
                if (!temp.isFinished()) {
                    return false;
                }
            }
        }

        return true;
    }

    private FileExecutor get(LogReader<F> reader) {
        FileExecutor executor = null;
        label:
        while (running) {
            for (FileExecutor temp : executors) {
                if (temp.isFinished()) {
                    executor = temp;
                    executor.setReader(reader);
                    break label;
                }
            }
            if (executors.size() < poolSize) {
                executor = new FileExecutor(reader, cacheSize, PROCESSOR_THREAD_WAIT_TIMES);
                executors.add(executor);
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
                logger.debug("file :{}  waiting {} ms for  processor process ", reader.path(), PROCESSOR_THREAD_WAIT_TIMES);
            } catch (InterruptedException e) {
                logger.error("INTERRUPTED ERROR:{}", e);
            }
        }
        return executor;
    }

    @Override
    public void process(LogReader<F> stream) {
        if (stream == null) {
            return;
        }
        if (writer != null) {
            try {
                writer.close();
                writer = null;
            } catch (IOException e) {
                logger.error("close unused writer error:{}", e);
            }
        }
        FileLogReader<F> processor = (FileLogReader<F>) stream;
        Executor<F> executor = get(processor);
        if (executor != null)
            threadPool.execute(executor);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        logger.info("file process stopping");
        while (!isFinished()) {
            logger.info("waiting  {} ms for file process stopping ", PROCESSOR_THREAD_WAIT_TIMES);
            TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
        }
        for (Executor<F> executor : executors) {
            executor.close();
        }
        executors.clear();
        while (!threadPool.isTerminated()) {
            TimeUnit.SECONDS.sleep(1);
            threadPool.shutdownNow();

        }
        logger.info("file process stopped");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setup() throws LogProcessorException {
        threadPool = Executors.newFixedThreadPool(poolSize);
        executors = new CopyOnWriteArrayList<>();

        this.lexer.setWriter(writer);
        readerName = conf.get("reader");
        if ("winevt".equals(readerName)) {
            transfer = (Transfer<F, Event>) new ByteBufferToEvent();
        } else {
            transfer = (Transfer<F, Event>) new StringToEvent();
        }
    }
}
