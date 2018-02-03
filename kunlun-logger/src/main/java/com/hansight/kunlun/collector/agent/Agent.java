package com.hansight.kunlun.collector.agent;


import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogProcessor;
import com.hansight.kunlun.collector.common.base.LogWriter;
import com.hansight.kunlun.collector.writer.ElasticSearchLogWriter;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.common.utils.AgentConstants;
import com.hansight.kunlun.collector.lexer.RegexLogLexer;
import com.hansight.kunlun.collector.position.store.ReadPositionStore;
import com.hansight.kunlun.coordinator.config.*;
import com.hansight.kunlun.utils.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * guan_yi @config zhhui_yan
 */
public class Agent {
    private final static Logger logger = LoggerFactory.getLogger(Agent.class);
    private static String agent;
    private static ExecutorService threadPool;
    private static Map<String, LogProcessor> processors = new ConcurrentHashMap<>();
    public final static Properties READER_CLASS_MAPPING = new Properties();
    public final static Properties WRITER_CLASS_MAPPING = new Properties();
    public final static Properties PROCESSOR_CLASS_MAPPING = new Properties();
    public final static Properties LEXER_CLASS_MAPPING = new Properties();
    public final static Properties NAME_MAPPING = new Properties();
    public final static Properties GLOBAL = new Properties();
    final static ClassLoader loader = Agent.class.getClassLoader();
    protected static Integer POSITION_STORE_OFFSET;
    protected static ReadPositionStore store;
    static {
        InputStream global = loader.getResourceAsStream(AgentConstants.AGENT_CONF);
        try {
            GLOBAL.load(global);
            GLOBAL.putAll(Common.getAll());
            READER_CLASS_MAPPING.load(loader.getResourceAsStream("reader-class-mapping.properties"));
            WRITER_CLASS_MAPPING.load(loader.getResourceAsStream("writer-class-mapping.properties"));
            PROCESSOR_CLASS_MAPPING.load(loader.getResourceAsStream("processor-class-mapping.properties"));
            LEXER_CLASS_MAPPING.load(loader.getResourceAsStream("lexer-class-mapping.properties"));
            NAME_MAPPING.load(loader.getResourceAsStream("names.properties"));
            POSITION_STORE_OFFSET = Integer.valueOf(Agent.GLOBAL.getProperty(AgentConstants.AGENT_READ_POSITION_STORE_OFFSET, AgentConstants.AGENT_READ_POSITION_STORE_OFFSET_DEFAULT));
        } catch (IOException e) {
            logger.trace("error:{}", e);
        }
    }

    public Agent() throws ConfigException {
        super();

    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, ConfigException, InterruptedException {
        threadPool = Executors.newCachedThreadPool();
        agent = GLOBAL.getProperty(AgentConstants.AGENT_ID, AgentConstants.AGENT_ID_DEFAULT);
        MonitorService<AgentConfig> service = new AgentMonitorService(agent);
        try {
            Class<ReadPositionStore> clazz = (Class<ReadPositionStore>) Class.forName(Agent.GLOBAL.getProperty(AgentConstants.AGENT_READ_POSITION_STORE_CLASS, AgentConstants.AGENT_READ_POSITION_STORE_CLASS_DEFAULT));
            store = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IOException("cannot init read position storer  ");
        }
        if (!store.init())
            throw new IOException("cannot init read position storer  ");
        store.setCacheSize(POSITION_STORE_OFFSET);
        try {
            service.registerConfigChangedProcessor(new ConfigChangedAction());
            threadPool.submit(service);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    super.run();
                    logger.info("Shutdown callback is invoked.");
                    Agent.stop();
                }
            });

            //Thread.sleep(10000);
        } catch (MonitorException e) {
            logger.error("monitor error:{}", e);
        }

    }

    /**
     * 提供静态接口给
     */
    public static void stop() {
        try {
            for (LogProcessor processor : processors.values()) {
                processor.stop();
            }

            threadPool.shutdown();
            if(!threadPool.isTerminated()) {
                Thread.sleep(10_000);
            }
            threadPool.shutdownNow();
            if(store!=null)
            store.close();
        } catch (Exception e) {
            logger.error("error:{}", e);
        }
    }

    private static class ConfigChangedAction implements
            MonitorService.ConfigChangedProcessor<AgentConfig> {

        @Override
        @SuppressWarnings("unchecked")
        public void process(List<AgentConfig> configList) throws MonitorException {
            for (AgentConfig config : configList) {
                logger.debug(" new config changed:{}", config);
                String key = config.getId();
                LogProcessor processor = processors.remove(key);
                try {
                    if (config.getState() == Config.State.DELETE) {
                        if (processor != null)//changed

                            processor.stop();

                        continue;
                    } else if (config.getState() == Config.State.UPDATE) {
                        if (processor != null)//changed
                            processor.stop();
                    }
                    String protocol = config.get("protocol");
                    String lexerName = config.get("lexer");
                    String writer = config.get("writer");

                    String clazzName = PROCESSOR_CLASS_MAPPING.getProperty(protocol);
                    if (clazzName == null) {
                        protocol = config.get("reader");
                        clazzName = PROCESSOR_CLASS_MAPPING.getProperty(protocol);
                    }
                    Class<LogProcessor> clazz = (Class<LogProcessor>) Class.forName(clazzName);
                    processor = clazz.newInstance();
                    processor.setReadPositionStore(store);

                    clazzName = WRITER_CLASS_MAPPING.getProperty(writer, ElasticSearchLogWriter.class.getName());
                    Class<LogWriter<Map<String, Object>>> writerClass = (Class<LogWriter<Map<String, Object>>>) Class
                            .forName(clazzName);
                    LogWriter<Map<String, Object>> logWriter = writerClass.newInstance();
                    processor.setWriter(logWriter);
                    clazzName = LEXER_CLASS_MAPPING
                            .getProperty(lexerName, RegexLogLexer.class.getName());
                    Class<Lexer<Event, Map<String, Object>>> lexerClass = (Class<Lexer<Event, Map<String, Object>>>) Class
                            .forName(clazzName);
                    Lexer<Event, Map<String, Object>> lexer = lexerClass.newInstance();
                    lexer.setTemplet(config.get("pattern"));
                    processor.setLexer(lexer);
                    lexer.setWriter(logWriter);
                    config.put("agent", agent);
                    processor.config(config);
                    processors.put(key, processor);
                    threadPool.submit(processor);
                } catch (Exception e) {
                    logger.error("error:{}", e);
                }
                //add

            }
        }
    }
}
