package com.hansight.kunlun.analysis.realtime.single;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.hansight.kunlun.analysis.realtime.conf.Constants;
import com.hansight.kunlun.analysis.realtime.model.Anomaly;
import com.hansight.kunlun.analysis.realtime.model.RTConstants;
import com.hansight.kunlun.analysis.utils.DatetimeUtils;
import com.hansight.kunlun.utils.EsUtils;
import com.hansight.kunlun.utils.MetricsUtils;


public class Storer extends Thread {
    private final static Logger logger = LoggerFactory
            .getLogger(Storer.class);
    protected BlockingQueue<Anomaly> queue = null;
    final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    TransportClient client = EsUtils.getEsClient();
    String type;
    private Map<String, Info> cache = new HashMap<>();
    public Storer(String type, BlockingQueue<Anomaly> queue) {
        super();
        this.type = type;
        this.queue = queue;
        this.setName("storer-thread[" + new Date() + "]");
    }
    private class Info {
    	private AtomicInteger count;
    	private Set<String> indices;
    	private long date;
		public Info(int i, String index) {
			count = new  AtomicInteger(i);
			indices = new HashSet<>();
			indices.add(index);
		}
		public Info(int i, @SuppressWarnings("rawtypes") List arr) {
			count = new  AtomicInteger(i);
			this.indices = new HashSet<>();
			for (Object index : arr) {
				this.indices.add((String)index);
			}
		}
    }

    long lastTime = System.currentTimeMillis();
    long MAX_TIME = RTHandler.getLong(Constants.RT_STORE_BATCH_MAX_TIME, 5000L);
    long sleepTime =RTHandler.getLong(Constants.RT_STORE_SLEEP_TIME, 500L);
    int BATCH_SIZE = RTHandler.getInt(Constants.RT_STORE_BATCH_MAX_SIZE, 500);;
    ArrayList<IndexRequestBuilder> batchList = new ArrayList<>(BATCH_SIZE);
    private Counter totalCounter = MetricsUtils.newCounter("anomaly.store.total");
    private Counter saveCounter = MetricsUtils.newCounter("anomaly.store.total.save");
    @Override
    public void run() {

        Anomaly anomaly;
        while (RTHandler.RUNNING) {
            anomaly = this.queue.poll();
            try {
	            if (anomaly == null) {
	            	if (batchList.size() > 0) {
	            		long time = System.currentTimeMillis() - lastTime;
	            		if (time > MAX_TIME) {
	            			logger.debug("flush for size:{} real time {} > {}", batchList.size(), time, MAX_TIME);
                            flush();
	            			lastTime = System.currentTimeMillis();
	            		}
	            	}
	            	TimeUnit.MILLISECONDS.sleep(100);
	                continue;
	            }
                totalCounter.inc();
	            // logger.debug("anomaly:{}", anomaly.toJson());
				process(anomaly, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

    boolean check(String index, boolean isChecked) {
    	// client.admin().indices().prepareExists(index).execute().actionGet().isExists()
        return isChecked || client.admin().indices()
                .exists(new IndicesExistsRequest(index))
                .actionGet().isExists();
    }

    @SuppressWarnings("rawtypes")
	void process(Anomaly anomaly, boolean isChecked) throws Exception {
        String date = format.format(DatetimeUtils.getDate(anomaly.getStartDatetime()));
        String index = "logs_" + date;
        // id
    	String id;
        if (!anomaly.getCategory().equals(RTConstants.EVENT_TYPE_HTTP404)) {
            id = DigestUtils.md5Hex(anomaly.getUrl() + "_" + anomaly.getCategory());
        } else {
        	id = DigestUtils.md5Hex(anomaly.getUrl() + "_" + anomaly.getUriQuery() +"_" + anomaly.getCategory());
        }
        // count && indices
    	Info info = cache.get(id);
    	if (info == null) {
    		GetResponse get = this.client.prepareGet(index, type, id).execute().actionGet();
    		if (get.isExists()) {
    			int count = Integer.parseInt(get.getSource().get("counter").toString());
    			count ++;
    			info = new Info(count, (List)get.getSource().get("indices"));
    			cache.put(id, info);
    		} else {
    			info = new Info(1, index);
    		}
    	} else {
    		info.count.addAndGet(1);
    		info.indices.add(index);
    	}
    	Map<String, Object> map = anomaly.toJson();
    	map.put("counter", info.count.get());
    	map.put("indices", info.indices);
    	IndexRequestBuilder builder = client.prepareIndex(
				(String) index, (String) type).setSource(map);
		if (id != null) {
			builder.setId((String) id);
		}
		batchList.add(builder);
		long time = System.currentTimeMillis() - lastTime;
		if (time > MAX_TIME) {
			logger.debug("flush for real time {} > {}", time, MAX_TIME);
			flush();
			lastTime = System.currentTimeMillis();
		} else if (batchList.size() >= BATCH_SIZE) {
			logger.debug("flush for batch size");
			flush();
			lastTime = System.currentTimeMillis();
		}
	}
    
    public int flush() throws Exception {
		if (batchList.size() > 0) {
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			for (IndexRequestBuilder storage : batchList) {
				try {
					bulkRequest.add(storage);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			int totalFailures = 0;
			BulkResponse bulkResponse = bulkRequest.execute().actionGet();
			if (bulkResponse.hasFailures()) {
				BulkItemResponse[] items = bulkResponse.getItems();
				for (BulkItemResponse item : items) {
					if (item.isFailed()) {
						logger.debug("store2ES failure, status:{}, message:{}",
								item.getFailure().getStatus(),
								item.getFailureMessage());
						totalFailures++;
					}
				}
			}
			if (totalFailures > 0) {
				logger.error("save2ES failure count:{}, pathName:{}",
						totalFailures);
			}
			int result = batchList.size() - totalFailures;
			batchList.clear();
            saveCounter.inc(result);
			return result;
		}
		return 0;
	}

}

