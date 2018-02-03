package com.hansight.kunlun.collector.processor;

import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.transfer.Transfer;
import com.hansight.kunlun.coordinator.metric.MetricException;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.WorkerStatus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhhuiyan on 14/12/23.
 */
public abstract class DefaultExecutor<F> extends Executor<F> {
    public DefaultExecutor( LogReader<F> reader, long cacheSize,long PROCESSOR_THREAD_WAIT_TIMES) {
        super(reader,cacheSize,PROCESSOR_THREAD_WAIT_TIMES);
    }

    @Override
    public void run() {
         MetricService metricService=getMetric();
         Transfer<F, Event> transfer=getTransfer();
        int i=0;
        newCache();
        for (F object : reader) {
           try {
                parse(transfer.transfer(object));
                metricService.mark();
                if (!running) {
                    return;
                }
                if(i==0){
                    metricService.setProcessorStatus(WorkerStatus.ConfigStatus.SUCCESS);
                    i++;
                }
            } catch (Exception e) {
                try {
                    metricService.setProcessorStatus(WorkerStatus.ConfigStatus.FAIL);
                } catch (MetricException e1) {
                    logger.error("METRIC ERROR:{}", e1);
                }
                logger.error("PROCESS ERROR:{}", e);
            }
        }
        try {
            clear();
        } catch (Exception e) {
            logger.error("file:{} WRITER FLUSH ERROR :{}",reader.path(), e);
        }

        try {
            reader.close();
        } catch (IOException e) {
            logger.error("file:{} READER CLOSE ERROR :{}",reader.path(), e);
        } finally {
            reader = null;
        }

    }
}
