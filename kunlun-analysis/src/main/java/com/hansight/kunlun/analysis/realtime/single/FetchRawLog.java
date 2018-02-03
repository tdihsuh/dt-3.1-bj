package com.hansight.kunlun.analysis.realtime.single;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.google.common.collect.Lists;
import com.hansight.kunlun.analysis.realtime.conf.Constants;
import com.hansight.kunlun.analysis.realtime.model.EnhanceAccess;
import com.hansight.kunlun.analysis.realtime.model.RTConstants;
import com.hansight.kunlun.analysis.utils.DatetimeUtils;
import com.hansight.kunlun.utils.EsUtils;
import com.hansight.kunlun.utils.MetricsUtils;

public class FetchRawLog extends Thread {
	private final static Logger logger = LoggerFactory
			.getLogger(FetchRawLog.class);

	private long startTime;
	private BlockingQueue<EnhanceAccess> queue = null;
	private String type;// ES

	private SimpleDateFormat format;
	private TransportClient client = EsUtils.getEsClient();
	private int MAX_STEP_IN_MILLIS = 10 * 60 * 1000;
	private long DELAY_TIMES = 10 * 1000;
	private String status = "";
	
	public FetchRawLog(String type, BlockingQueue<EnhanceAccess> queue) {
		this(type, queue, System.currentTimeMillis() - 60 * 1000);
	}

	public FetchRawLog(String type, BlockingQueue<EnhanceAccess> queue,
			long startTime) {
		super();
		this.queue = queue;
		this.startTime = startTime;
		this.type = type;
		this.setName("fetch-raw-log-thread[" + new Date() + "]");
		this.format = new SimpleDateFormat(
				RTConstants.RAW_LOG_INDEX_NAME_SUFFIX_DATE_PATTERN);
		MAX_STEP_IN_MILLIS = RTHandler.getInt(Constants.RT_FETCH_RAW_LOG_STEP,
				MAX_STEP_IN_MILLIS);
		DELAY_TIMES = RTHandler.getLong(Constants.RT_FETCH_RAW_LOG_DELAY,
				DELAY_TIMES);
	}

	@Override
	public void run() {
		MetricsUtils.newGauge("raw.queue.size", queue);
		Counter counter = MetricsUtils.newCounter("raw.total");
		Counter queueCounter = MetricsUtils.newCounter("raw.queue.total");
		MetricsUtils.register("raw.fetch.status", new Gauge<String>() {
			@Override
			public String getValue() {
				return FetchRawLog.this.status;
			}
		});
		
		long sleep = RTHandler.getInt(Constants.RT_FETCH_RAW_LOG_SLEEP, 3);
		int size = RTHandler.getInt(Constants.RT_FETCH_RAW_LOG_SIZE, 500);
		int time = RTHandler.getInt(Constants.RT_FETCH_RAW_LOG_TIME, 10);
		long current;
		boolean past = false;
		long prevTime = this.startTime;
		long endTime;
		while (RTHandler.RUNNING) {
			try {
				endTime = prevTime + MAX_STEP_IN_MILLIS;
				current = System.currentTimeMillis();
				current -= DELAY_TIMES;
				if (endTime > current) {
					endTime = current;
					past = false;
				} else {
					past = true;
				}
				String prev = DatetimeUtils.getUTCDateTime(prevTime);
				String to = DatetimeUtils.getUTCDateTime(endTime);
				String[] indices = getIndices(prevTime, endTime);
				if (indices.length > 0) {
					SearchResponse response;
					long total = 0;
					response = search(size, prev, to, indices, time);
					while (true) {
						response = client
								.prepareSearchScroll(response.getScrollId())
								.setScroll(TimeValue.timeValueMinutes(time))
								.execute().actionGet();
						SearchHit[] hits = response.getHits().hits();
						status = "date:[" + prev +"," + to +"]" + hits.length;
						logger.debug(status);
						if (hits.length == 0) {
							// clean scroll id
							List<String> ids = new ArrayList<>(1);
							ids.add(response.getScrollId());
							client.prepareClearScroll().setScrollIds(ids)
									.execute().actionGet();
							break;
						}
						
						long vCounter = 0L;
						long nvCounter = 0L;
						long duration = System.currentTimeMillis();
						for (SearchHit hit : hits) {
							counter.inc();
							EnhanceAccess access = EnhanceAccess
									.getInstance(hit);
							if (access != null) {
								this.queue.put(access);
								queueCounter.inc();
								vCounter++;
							} else {
								nvCounter++;
							}
						}

						if (logger.isDebugEnabled()) {
							duration = System.currentTimeMillis() - duration;
							long took = response.getTookInMillis();
							double eps = Math.round((((response.getHits()
									.getTotalHits()) / (1.0 * response
									.getTookInMillis()))) * 1000);
							logger.debug(
									"From {} to {}, search result: {} need {} ms,validate record: {}, invalidate record: {} process need {}ms, EPS: {}eps, queue size: {}",
									prev, to, total, took, vCounter, nvCounter,
									duration, eps, this.queue.size());
						}
					}
				}
				prevTime = endTime;
			} catch (ElasticsearchException | InterruptedException e) {
				logger.error(this.getName(), e);
			}
			try {
				if (sleep > 0 && !past) {
					TimeUnit.SECONDS.sleep(sleep);
				}
			} catch (InterruptedException e) {
				logger.error(this.getName(), e);
			}
		}
		EsUtils.closeEsClient(client);
	}

	private SearchResponse search(int size, String from, String to,
			String[] indices, int time) {
		// index
		QueryBuilder qb = QueryBuilders.rangeQuery("@timestamp").from(from)
				.to(to).includeLower(true).includeUpper(false);
		return client.prepareSearch(indices).setTypes(type).setQuery(qb)
				.setSearchType(SearchType.SCAN)
				.setScroll(TimeValue.timeValueMinutes(time)).setSize(size)
				.execute().actionGet();
	}

	public String[] getIndices(long prev, long now) {
		List<String> result = Lists.newArrayList();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		calendar.setTime(new Date(prev));
		String preDate = format.format(calendar.getTime());
		calendar.setTime(new Date(now));
		String nowDate = format.format(calendar.getTime());
		//

		String index = RTConstants.RAW_LOG_INDEX_NAME_PREFIX + preDate;
		IndicesExistsRequest request = new IndicesExistsRequest(index);
		IndicesExistsResponse res = client.admin().indices().exists(request)
				.actionGet();
		if (res.isExists()) {
			result.add(index);
		} else {
			logger.warn("Index [{}] doesn't exist", index);
		}
		if (!preDate.equals(nowDate)) {
			index = RTConstants.RAW_LOG_INDEX_NAME_PREFIX + nowDate;
			request = new IndicesExistsRequest(index);
			res = client.admin().indices().exists(request).actionGet();
			if (res.isExists()) {
				result.add(index);
			} else {
				logger.warn("Index [{}] doesn't exist", index);
			}
		}
		return result.toArray(new String[result.size()]);
	}

}
