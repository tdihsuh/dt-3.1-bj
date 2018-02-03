package com.hansight.kunlun.web.config.warning.service;

import com.hansight.kunlun.utils.EsUtils;
import com.hansight.kunlun.web.config.warning.entity.ConfWarning;
import com.hansight.kunlun.web.config.warning.util.WarningUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;


@Service("esMonitorJob")
public class EsMonitorJob {
	private  static final Logger LOG = LoggerFactory.getLogger(EsMonitorJob.class);
	private  ConfWarningService warningService;
	private  static TransportClient esClient;
	private  static WarningUtils wu; 
	private  BigDecimal total = null;
	private  BigDecimal free = null;
	private  BigDecimal available = null;
	private  double firstValue;
	private  double secondValue;
	private  String email;
	private  ConfWarning cw;
	private  static final String name = "alert.es.rate";
 	static{
		esClient =   EsUtils.getEsClient();
		wu = new WarningUtils();
	}
	
	protected void executeInternal(){
		if(esClient!=null){
			ClusterAdminClient cluster = esClient.admin().cluster();
			ClusterStatsResponse actionGet = cluster
					.clusterStats(new ClusterStatsRequest()).actionGet();
			Gson gson = new Gson();
			JsonObject jsonObject = gson.toJsonTree(actionGet).getAsJsonObject();
			Iterator<Entry<String, JsonElement>> iterator1 = jsonObject.entrySet().iterator();
			while(iterator1.hasNext()){	
				    Entry<String, JsonElement> next = iterator1.next();
				    if(next.getKey().trim().equals("nodesStats")){
				    	JsonElement jsonTree = gson.toJsonTree(next.getValue());
				    	if(jsonTree.getAsJsonObject().isJsonObject() && jsonTree.isJsonObject()){
				    		Set<Entry<String, JsonElement>> entrySet = jsonTree
				    				.getAsJsonObject().entrySet();
				    		for(Entry e:entrySet){
				    			if(e.getKey() == "fs"){
				    				JsonElement json = gson.toJsonTree(e.getValue());
				    				if(json.getAsJsonObject().isJsonObject()){
				    					JsonObject jsonObject1 = json.getAsJsonObject();
				    					total = BigDecimal.valueOf(Long.parseLong(jsonObject1.get("total").toString()));
				    					free = BigDecimal.valueOf(Long.parseLong(jsonObject1.get("free").toString()));
				    					available = BigDecimal.valueOf(Long.parseLong(jsonObject1.get("available").toString()));
				    					try {
				    						cw = warningService.getConfWarningByName(name);
				    					} catch (Exception ep) {
				    						// TODO Auto-generated catch block
				    						LOG.debug("");
				    					}	
				    					LOG.debug("TOTAL : "+total + "FREE : " +free+ " AVAILABLE : "+available);
				    					BigDecimal subtract = total.subtract(available);
				    					float floatValue = subtract.divide(total, 3, BigDecimal.ROUND_HALF_EVEN)
											 		.floatValue();
				    					if(cw.getValue().trim().equals("0.00")){
				    						secondValue = Double.parseDouble(cw.getDefaultValue());
				    					}else {secondValue = Double.parseDouble(cw.getValue());}
				    					LOG.error("用户设定的值: "+secondValue+"es集群空间占用值: "+floatValue);
				    					if(secondValue <= floatValue){
												try {
													if(secondValue != firstValue || !cw.getEmail().trim().equals(email)){
														wu.warning(cw.getEmail(),"占用空间已超用户预定义的值: "+secondValue+" 达到: "+floatValue+"。  请及时处理。 ");
														LOG.debug("上次用户设定的值: "+firstValue+"这次用户设定的值: "+secondValue);
														update(secondValue,cw.getEmail());
													}
													warningService.setInforMation("占用空间已超用户预定义的值: "+secondValue+" 达到: "+floatValue+"。 请及时处理。 ");
												} catch (Exception e1) {
													// TODO Auto-generated catch block
													LOG.debug("");
												}
												LOG.error("邮箱地址: "+cw.getEmail(),"占用的空间值: "+floatValue);
										}else{
												update(secondValue,cw.getEmail());
												warningService.setInforMation(null);
										}
									/*if(0.9 <= floatValue && d < floatValue){
										try {
											wu.warning(cw.getEmail(),"危险：占用空间已超90%达到: "+floatValue+" 需要扩大空间");
											warningService.setInforMation("占用空间已超用户预定义的值达到: "+floatValue);
										} catch (Exception e1) {
											// TODO Auto-generated catch block
											LOG.debug("差春来的对象可能为空");
										}
										LOG.debug("邮箱地址: "+cw.getEmail(),"占用的空间值: "+floatValue);
									}*/
									/*Statement stm = null;
									try {
										stm = WarningUtils.connection.createStatement();
										boolean execute = stm.execute("update ES_STATUS set TOTAL = "+total+", FREE = "+free+", AVAILABLE = "+available+" where ID = '123'");
										return execute;
									
									} catch (SQLException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}*/
								}
							}
						}
					}
				}
			}
		}else LOG.debug("No connection to the Elasticsearch cluster");
	}

	public double getFirstValue() {
		return firstValue;
	}

	public void setFirstValue(double firstValue) {
		this.firstValue = firstValue;
	}

	public double getSecondValue() {
		return secondValue;
	}

	public void setSecondValue(double secondValue) {
		this.secondValue = secondValue;
	}

	public ConfWarningService getWarningService() {
		return warningService;
	}

	public void setWarningService(ConfWarningService warningService) {
		this.warningService = warningService;
	}

	public static TransportClient getEsClient() {
		return esClient;
	}

	public static void setEsClient(TransportClient esClient) {
		EsMonitorJob.esClient = esClient;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void update(double d,String email){
		setFirstValue(d);
		setEmail(email);
	}

}
