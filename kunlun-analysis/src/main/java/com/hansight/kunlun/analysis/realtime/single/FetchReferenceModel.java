package com.hansight.kunlun.analysis.realtime.single;

import com.codahale.metrics.Gauge;
import com.hansight.kunlun.analysis.realtime.conf.Constants;
import com.hansight.kunlun.analysis.realtime.model.RTConstants;
import com.hansight.kunlun.utils.EsUtils;
import com.hansight.kunlun.utils.MetricsUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetchReferenceModel extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(FetchReferenceModel.class);
    private final static long ONE_HOUR_INTERVAL = 1 * 60 * 60 * 1000;
    private Map<Integer, Double> queryStringModel;
    private Map<Integer, Double> manualQueryStringModel;
    private Map<String, Boolean> model404;
    private Map<String, Boolean> manualModel404;
    private Pattern pattern;

    public FetchReferenceModel(Map<Integer, Double> manualQueryStringModel, 
    		Map<Integer, Double> queryStringModel,
    		Map<String, Boolean> manulModel404,
            Map<String, Boolean> model404) {
        super();
        this.manualQueryStringModel = manualQueryStringModel;
        this.queryStringModel = queryStringModel;
        this.manualModel404 = manulModel404;
        this.model404 = model404;
        this.setName("fetch-model-result-thread[" + new Date() + "]");
        String regex = "(/[cC][mM][bB][bB][aA][nN][kK]_[\\d\\D]*)|([^\\.%'\"<>]*((\\.htc)|(\\.gif)|(\\.jpg)|(\\.js)|(\\.ico)|(\\.png)))";
        regex = RTHandler.get(Constants.RT_FETCH_REFERENCE_REGEX, regex);
        if (regex != null && regex.length() > 0) {
        	pattern = Pattern.compile(regex);
        }
    }

    @Override
    public void run() {
        MetricsUtils.register("model.info", new Gauge<Map<String, Object>>() {
            @Override
            public Map<String, Object> getValue() {
            	Map<String, Object> map = new HashMap<>();
            	map.put("status_404", model404.size());
            	map.put("query_string", queryStringModel.size());
                return map;
            }
        });
    	long sleep = RTHandler.getLong("fetch.reference.sleep", ONE_HOUR_INTERVAL);
        TransportClient client = null;
        try {
            client = EsUtils.getEsClient();
            while (RTHandler.RUNNING) {
            	int size = 5000;
//            	fetchManualQueryString(client, size);
//				fetchQueryString(client, size);
				fetchStatus404(client, size);
				fetchManualStatus404(client, size);
                try {
					TimeUnit.MILLISECONDS.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        } finally {
            EsUtils.closeEsClient(client);
        }
    }
    
    private void fetchQueryString(TransportClient client, int size) {
    	//qs model start
		try {
			SearchResponse response = client
			        .prepareSearch(RTConstants.MODEL).setTypes(RTConstants.TYPE_QS)
			        .setQuery(QueryBuilders.matchAllQuery()).setSize(size)
			        .execute().actionGet();
			long total = response.getHits().getTotalHits();
			SearchHit[] hits = response.getHits().getHits();
			if (response.getHits().getTotalHits() > 0) {
			    for (SearchHit hit : hits) {
			        Map<String, Object> source = hit.getSource();
			        this.queryStringModel.put((Integer) source.get("uri"), (Double) source.get("upper"));
			    }


			}
			total = total - hits.length;
            int from = hits.length;
			while (RTHandler.RUNNING && total > 0) {
			    response = client
			            .prepareSearch(RTConstants.MODEL).setTypes(RTConstants.TYPE_QS)
			            .setQuery(QueryBuilders.matchAllQuery()).setFrom((int)from).setSize(size)
			            .execute().actionGet();
			    if (response.getHits().getHits().length > 0) {
                    total -= response.getHits().getHits().length;
                    from += response.getHits().getHits().length;
			        for (SearchHit hit : response.getHits().getHits()) {
			            Map<String, Object> source = hit.getSource();
			            this.queryStringModel.put((Integer) source.get("uri"), (Double) source.get("upper"));
			        }
			    }else{
                    logger.debug("Get 0 response hits, set total to 0");
                    total = 0;
                }
			}
		} catch (Exception e) {
			logger.warn("fetch model for query_string error", e);
		}
    }
    
    private void fetchManualQueryString(TransportClient client, int size) {
    	//qs model start
		try {
			SearchResponse response = client
			        .prepareSearch(RTConstants.MODEL).setTypes(RTConstants.TYPE_QS)
			        .setQuery(QueryBuilders.matchAllQuery()).setSize(size)
			        .execute().actionGet();
			long total = response.getHits().getTotalHits();
			SearchHit[] hits = response.getHits().getHits();
			if (response.getHits().getTotalHits() > 0) {
			    for (SearchHit hit : hits) {
			        Map<String, Object> source = hit.getSource();
			        this.manualQueryStringModel.put((Integer) source.get("uri"), (Double) source.get("upper"));
			    }


			}
			total = total - hits.length;
            int from = hits.length;
			while (RTHandler.RUNNING && total > 0) {
			    response = client
			            .prepareSearch(RTConstants.MODEL).setTypes(RTConstants.TYPE_QS)
			            .setQuery(QueryBuilders.matchAllQuery()).setFrom((int)from).setSize(size)
			            .execute().actionGet();
			    if (response.getHits().getHits().length > 0) {
                    total -= response.getHits().getHits().length;
                    from += response.getHits().getHits().length;
			        for (SearchHit hit : response.getHits().getHits()) {
			            Map<String, Object> source = hit.getSource();
			            this.manualQueryStringModel.put((Integer) source.get("uri"), (Double) source.get("upper"));
			        }
			    }else{
                    logger.debug("Get 0 response hits, set total to 0");
                    total = 0;
                }
			}
		} catch (Exception e) {
			logger.warn("fetch model for manual query_string error", e);
		}
    }
    
    private void fetchStatus404(TransportClient client, int size) {
    	//status_404 model to cache:
        try {
        	TermQueryBuilder tqb = QueryBuilders.termQuery("malicious", true);
        	SearchResponse response = client
			        .prepareSearch(RTConstants.MODEL).setTypes(RTConstants.TYPE_404)
			        .setQuery(tqb).setSize(size)
			        .execute().actionGet();
			long total = response.getHits().getTotalHits();
			SearchHit[] hits = response.getHits().getHits();
			if (response.getHits().getTotalHits() > 0) {
			    for (SearchHit hit : hits) {
			    	filterStatus404(hit);
			    }
			}
			total = total - hits.length;
            int from = hits.length;
			while (RTHandler.RUNNING && total > 0) {
			    response = client
			            .prepareSearch(RTConstants.MODEL).setTypes(RTConstants.TYPE_404)
			            .setQuery(tqb).setFrom((int)from).setSize(size)
			            .execute().actionGet();
			    if (response.getHits().getHits().length > 0) {
                    total -= response.getHits().getHits().length;
                    from += response.getHits().getHits().length;
			        for (SearchHit hit : response.getHits().getHits()) {
			        	filterStatus404(hit);
			        }
			    } else{
                    logger.debug("Get 0 response hits, set total to 0");
                    total = 0;
                }
			}
            logger.info("status_404 modle total size: {}", this.model404.size());
		} catch (Exception e) {
			logger.warn("fetch model status_404 error: {}", e);
		}
    }
    
    private void filterStatus404(SearchHit hit) {
    	Map<String, Object> source = hit.getSource();
        String url = source.get(RTConstants.MODEL_404_URL).toString();
    	if (pattern != null) {
        	Matcher m = pattern.matcher(url);
        	if (!m.matches()) {
        		Boolean mailicious = (Boolean) source.get(RTConstants.MODEL_404_MALICIOUS);
        		this.model404.put(url, mailicious);
        		logger.debug("[url: {}, malicious: {}]", url, mailicious);
        	}
        } else {
        	Boolean mailicious = (Boolean) source.get(RTConstants.MODEL_404_MALICIOUS);
    		this.model404.put(url, mailicious);
    		logger.debug("[url: {}, malicious: {}]", url, mailicious);
        }
    }
    
    private void fetchManualStatus404(TransportClient client, int size) {
    	//status_404 model to cache:
        try {
        	SearchResponse response = client
			        .prepareSearch(RTConstants.Manual).setTypes(RTConstants.TYPE_404)
			        .setQuery(QueryBuilders.matchAllQuery()).setSize(size)
			        .execute().actionGet();
			long total = response.getHits().getTotalHits();
			SearchHit[] hits = response.getHits().getHits();
			if (response.getHits().getTotalHits() > 0) {
			    for (SearchHit hit : hits) {
			        Map<String, Object> source = hit.getSource();
                    if(source == null || source.get(RTConstants.MODEL_404_MALICIOUS) == null) continue;
			        this.manualModel404.put(source.get(RTConstants.MODEL_404_URL).toString(), (Boolean) source.get(RTConstants.MODEL_404_MALICIOUS));
                    logger.debug("manual [url: {}, malicious: {}]",source.get(RTConstants.MODEL_404_URL).toString(),source.get(RTConstants.MODEL_404_MALICIOUS));
			    }
			}
			total = total - hits.length;
            int from = hits.length;
			while (RTHandler.RUNNING && total > 0) {
			    response = client
			            .prepareSearch(RTConstants.MODEL).setTypes(RTConstants.TYPE_404)
			            .setQuery(QueryBuilders.matchAllQuery()).setFrom((int)from).setSize(size)
			            .execute().actionGet();
			    if (response.getHits().getHits().length > 0) {
                    total -= response.getHits().getHits().length;
                    from += response.getHits().getHits().length;
			        for (SearchHit hit : response.getHits().getHits()) {
			            Map<String, Object> source = hit.getSource();
                        if(source == null || source.get(RTConstants.MODEL_404_MALICIOUS) == null) continue;
			            this.manualModel404.put(source.get(RTConstants.MODEL_404_URL).toString(), (Boolean) source.get(RTConstants.MODEL_404_MALICIOUS));
                        logger.debug("[url: {}, malicious: {}]",source.get(RTConstants.MODEL_404_URL).toString(),source.get(RTConstants.MODEL_404_MALICIOUS));
			        }
			    }
                else{
                    logger.debug("Get 0 response hits, set total to 0");
                    total = 0;
                }
			}
            logger.info("manual status_404 modle total size: {}", this.manualModel404.size());
		} catch (Exception e) {
			logger.warn("fetch model manual status_404 error: {}", e);
		}
    }
}
