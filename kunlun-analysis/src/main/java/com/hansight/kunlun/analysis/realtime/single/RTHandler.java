package com.hansight.kunlun.analysis.realtime.single;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hansight.kunlun.analysis.realtime.conf.Constants;
import com.hansight.kunlun.analysis.realtime.model.EnhanceAccess;
import com.hansight.kunlun.analysis.utils.DatetimeUtils;
import com.hansight.kunlun.utils.MetricsUtils;

public class RTHandler {
    public final static Properties CONF = new Properties();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private final static Logger logger = LoggerFactory.getLogger(RTHandler.class);
    private static FetchRawLog fetchRawLog;
    private static FetchReferenceModel fetchReferenceModel;
    private static SessionProcessor processor;
    public static AtomicLong lastFetch = new AtomicLong();
    public static boolean RUNNING = true;

    static {
        InputStream global = RTHandler.class.getClassLoader()
                .getResourceAsStream("real_time.properties");
        try {
            CONF.load(global);
        } catch (IOException e) {
            logger.error("error: real_time.properties is exists ?", e);
        }
    }
    
    public static long getLong(String key, long value) {
    	String obj = CONF.getProperty(key);
    	if (obj == null) {
    		return value;
    	}
    	try {
			return Long.parseLong(obj);
		} catch (NumberFormatException e) {
			return value;
		}
    }
    
    public static int getInt(String key, int value) {
    	String obj = CONF.getProperty(key);
    	if (obj == null) {
    		return value;
    	}
    	try {
			return Integer.parseInt(obj);
		} catch (NumberFormatException e) {
			return value;
		}
    }
    public static String get(String key, String value) {
		return CONF.getProperty(key, value);
    }

    /**
     * @param args String []
     */
    public static void main(String[] args) {
        String startDate;//
        if (args.length != 2) {
            System.out.println("usage: type startTime");
            System.out.println("startTime: utc time, example: log_iis 2013-12-09T15:59:59.000Z");
            System.exit(1);
        }
        startDate = args[1];
        MetricsUtils.newGauge("args", Arrays.toString(args));

        BlockingQueue<EnhanceAccess> queue = new LinkedBlockingQueue<>(getInt(Constants.MESSAGE_QUEUE_CAPACITY, Constants.MESSAGE_QUEUE_CAPACITY_DEFAULT));
        Map<Integer, Double> QSModel = Maps.newConcurrentMap();
        Map<String, Boolean> model404 = Maps.newConcurrentMap();
        Map<Integer, Double> manualQSModel = Maps.newConcurrentMap();
        Map<String, Boolean> manualModel404 = Maps.newConcurrentMap();
        fetchRawLog = new FetchRawLog(args[0], queue,
                DatetimeUtils.getTimestamp(startDate));
        fetchReferenceModel = new FetchReferenceModel(manualQSModel, QSModel, manualModel404, model404);
        processor = new SessionProcessor(queue, manualQSModel, QSModel, manualModel404, model404);
        threadPool.execute(fetchReferenceModel);
        threadPool.execute(fetchRawLog);
        threadPool.execute(processor);
        threadPool.shutdown();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                logger.info("AGENT SHUTDOWN BY USER,PLEASE WAIT FOR THE SYSTEM TO HANDLE CACHE....");
                RTHandler.stop();
            }
        });
        metrics();
    }
    
    private static void metrics(){
    	if ("true".equalsIgnoreCase(get("metrics.enable", "false"))) {
        	int port = getInt("metrics.port", 8030);
        	String prefix = get("metrics.uri", "/metrics");
        	Server server = new Server(port);
    		ServletContextHandler context = new ServletContextHandler();
    		context.setContextPath("");
    		context.addServlet(new ServletHolder(new MetricsServlet()), prefix);
    		HandlerCollection handlers = new HandlerCollection();
    		handlers.setHandlers(new Handler[] { context, new DefaultHandler() });
    		server.setHandler(handlers);
    		try {
				server.start();
				server.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

    public static void stop() {
        try {
            RUNNING = false;
            threadPool.shutdown();
            while (!threadPool.isTerminated()) {
                Thread.sleep(10_000);
            }
            threadPool.shutdownNow();
        } catch (Exception e) {
            logger.error("error:{}", e);
        }
    }

}
