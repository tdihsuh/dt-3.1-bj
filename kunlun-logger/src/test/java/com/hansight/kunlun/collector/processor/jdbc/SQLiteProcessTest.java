package com.hansight.kunlun.collector.processor.jdbc;

import com.hansight.kunlun.collector.writer.ElasticSearchLogWriter;
import com.hansight.kunlun.coordinator.config.AgentConfig;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhhuiyan on 14/12/29.
 */
public class SQLiteProcessTest {
    @Test
    public void testSQLiteReader() throws Exception {
        ExecutorService service=Executors.newFixedThreadPool(1);
        JDBCLogProcessor processor=new JDBCLogProcessor();
        AgentConfig config= new AgentConfig();
        config.put("id","5a725d685a1d4de4af537105adab7320");
        config.put("agent","agent");
        config.put("driver","sqlite");
        config.put("table","READ_POSITION");
        config.put("database","/Users/zhhuiyan/workspace/kunlun/db/read_position.db");
        config.put("type", "test");
        config.put("category", "sqlite");
        processor.setWriter(new ElasticSearchLogWriter<Map<String, Object>>());

        processor.config(config);
        service.submit(processor);

        if(!service.isShutdown()){
            TimeUnit.SECONDS.sleep(10);
            processor.stop();
        }
    }
}
