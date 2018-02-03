package com.hansight.kunlun.collector.processor.jdbc;

import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.exception.LogProcessorException;
import com.hansight.kunlun.collector.common.exception.LogWriteException;
import com.hansight.kunlun.collector.processor.DefaultExecutor;
import com.hansight.kunlun.collector.processor.DefaultLogProcessor;
import com.hansight.kunlun.collector.reader.JDBCLogReader;
import com.hansight.kunlun.coordinator.metric.MetricException;
import com.hansight.kunlun.coordinator.metric.WorkerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhhuiyan on 14/12/27.
 */
public class JDBCLogProcessor extends DefaultLogProcessor<Map<String, Object>> {
    final static Logger logger = LoggerFactory.getLogger(JDBCLogProcessor.class);

    @Override
    public void setup() throws LogProcessorException {
        threadPool = Executors.newFixedThreadPool(1);
        String host = get("host", "127.0.0.1");
        String port = get("port", "3306");
        String database = get("database", "default");
        String table = get("table", "default");
        String driver = get("driver", "sqlite");
        String user = get("user", null);
        String password = get("password", null);
        JDBCLogReader reader;
        try {
            switch (driver) {
                case "sqlite": {
                    reader = new JDBCLogReader("jdbc:sqlite:" + database, "org.sqlite.JDBC", table);
                    readers.add(reader);
                    break;
                }
                case "mysql": {
                    reader = new JDBCLogReader("jdbc:mysql://" + host+":"+port + "/" + database, "com.mysql.jdbc.Driver", table, user, password);
                    readers.add(reader);
                    break;
                }
                case "oracle": {
                    reader = new JDBCLogReader("jdbc:oracle:thin:@" + host +":"+port +  ":" + database, "oracle.jdbc.driver.OracleDriver", table, user, password);
                    readers.add(reader);
                    break;
                }
                case "DB2": {
                    reader = new JDBCLogReader("jdbc:db2://" + host +":"+port +  "/" + database, "com.ibm.db2.jcc.DB2Driver", table, user, password);
                    readers.add(reader);
                    break;
                }
                case "sybase": {
                    reader = new JDBCLogReader("jdbc:jtds:sybase://" + host +":"+port + "/" + database, "net.sourceforge.jtds.jdbc.Driver", table, user, password);
                    readers.add(reader);
                    break;
                }
                case "postgresql": {
                    reader = new JDBCLogReader("jdbc:postgresql://" + host +":"+port +  "/" + database, "org.postgresql.Driver", table, user, password);
                    readers.add(reader);
                    break;
                }
                case "sqlserver": {
                    reader = new JDBCLogReader("jdbc:jtds:sqlserver://" + host +":"+port +  "/" + database, "net.sourceforge.jtds.jdbc.Driver", table, user, password);
                    readers.add(reader);
                    break;
                }

            }
        } catch (ClassNotFoundException e) {
            logger.error("create reader error:{}", e);
            throw new LogProcessorException("connect init error");
        }
    }

    @Override
    public void cleanup() throws LogProcessorException {

    }

    @Override
    public void process(final LogReader<Map<String, Object>> stream) {
        Thread thread = new Thread() {

            @Override
            public void run() {
                int i = 0;

                for (Map<String, Object> map : stream) {
                    if (!running) {
                        return;
                    }

                    try {
                        writer.write(map);
                        if (i == 0) {
                            metricService.setProcessorStatus(WorkerStatus.ConfigStatus.SUCCESS);
                            i++;
                        }
                    } catch (LogWriteException e) {
                        try {
                            metricService.setProcessorStatus(WorkerStatus.ConfigStatus.FAIL);
                        } catch (MetricException e1) {
                            logger.error("METRIC ERROR:{}", e1);
                        }
                        logger.error("writer data to es error:{}", e);
                    } catch (MetricException e) {
                        logger.error("metric  error:{}", e);
                    }

                }
                try {
                    writer.close();
                } catch (Exception e) {
                    logger.error("file:{} WRITER FLUSH ERROR :{}", stream.path(), e);
                }

                try {
                    stream.close();
                } catch (IOException e) {
                    logger.error("file:{} READER CLOSE ERROR :{}", stream.path(), e);
                }
            }
        };
        thread.setName("jdbc-process-" + stream.path());
        threadPool.execute(thread);
    }
}
