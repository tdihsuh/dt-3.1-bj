package com.hansight.kunlun.web.config.datasource.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.coordinator.metric.ConfigInfo;
import com.hansight.kunlun.coordinator.metric.LiveInfo;
import com.hansight.kunlun.coordinator.metric.MetricMsg;
import com.hansight.kunlun.coordinator.metric.WorkerStatus.ConfigStatus;
import com.hansight.kunlun.web.config.datasource.entity.ConfAgent;
import com.hansight.kunlun.web.config.datasource.entity.ConfDatasource;
import com.hansight.kunlun.web.config.datasource.entity.ConfForwarder;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfAgentQueryBean;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfDatasourceQueryBean;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfForwarderQueryBean;
import com.hansight.kunlun.web.config.datasource.util.MonitorConfig;

@Service("monitorJob")
public class MonitorJob {
	private static final Logger LOG = LoggerFactory.getLogger(MonitorJob.class);
	private ConfDatasourceService datasourceService;
	private ConfAgentService confAgentService;
	private ConfForwarderService forwarderService;

	protected void executeInternal(){
		try {
			ConfDatasourceQueryBean datasourceBean = new ConfDatasourceQueryBean();
			List<ConfDatasource> dataSourceList = datasourceService.list(datasourceBean);
			if (dataSourceList == null) {
				LOG.warn("no datasource");
				return;
			}
			List<MetricMsg> dataMonitor = MonitorConfig.dataSourceMonitor();
			List<ConfigInfo> configInfo = MonitorConfig.dataSourceConfigInfo();
			Map<String, ConfigStatus>map = MonitorConfig.getDatasourceConfig();
			if (dataMonitor.size() != 0 && configInfo.size() != 0
					&& dataSourceList.size() != 0 && map.size() != 0) {
				for (ConfDatasource datasource : dataSourceList) {
					for (MetricMsg metric : dataMonitor) {
						if (datasource.getId().equals(metric.getDataSourceId())) {
							//LOG.info("从后台获取的datasource状态：datasourceId="+metric.getDataSourceId()+",status="+metric.getCommonStatus());
							       datasource.setState(metric.getCommonStatus().toString());
									for (String key :map.keySet()) {
										//LOG.info("datasourceId:"+key+",配置的状态:"+map.get(key)+"DataSourceId:"+datasource.getId());
										if(datasource.getId().equals(key)){
											datasource.setConfig(map.get(key).toString());
											StringBuffer config = new StringBuffer();
											for (ConfigInfo configInfo2 : configInfo) {
												LOG.info("配置状态信息：datasourceId="+configInfo2.getDatasourceId()+",id="+configInfo2.getProcessorId()+",type="+configInfo2.getProcessorType()+",status="+configInfo2.getConfigStatus());
												if (datasource.getId().equals(configInfo2.getDatasourceId())) {
													config.append(configInfo2.getProcessorType()+ ",id="+ configInfo2.getProcessorId()+ ",state="+ configInfo2.getConfigStatus()+",");
													datasource.setConfigInfo(config.toString().substring(0, config.length()-1));
													datasourceService.updateJob(datasource);
												}
										
											}
									
										}
								}
						}
					}
				}
			} else {
				LOG.debug("后台获取数据源状态数据大小为："+dataMonitor.size()+",后台获取数据源配置信息数据大小为："+configInfo.size()+",后台获取数据源配置状态数据大小："+map.size());
			}

			ConfAgentQueryBean agent = new ConfAgentQueryBean();
			List<ConfAgent> listAgent = confAgentService.list(agent);
			List<LiveInfo> liveList = MonitorConfig.getAgentLive();
			LOG.info("后台获取Agent大小："+liveList.size());
			if (listAgent.size() != 0 && liveList.size() != 0) {
				for (LiveInfo liveInfo : liveList) {
					for (ConfAgent confAgent : listAgent) {
						if (liveInfo.getProcessorId().equals(confAgent.getId())) {
							confAgent.setState(liveInfo.getCommonStatus().toString());
							LOG.info("agentId="+liveInfo.getProcessorId()+",state="+liveInfo.getCommonStatus().toString());
							confAgentService.updateJob(confAgent);
						}
					}
				}
			} else {
				LOG.debug("后台获取agent的数据大小为："+liveList.size());
			}

			ConfForwarderQueryBean confForwarder = new ConfForwarderQueryBean();
			List<ConfForwarder> forwarderList = forwarderService
					.list(confForwarder);
			List<LiveInfo> liveInfoS = MonitorConfig.getForwarderLive();
			LOG.info("后台获取forwarder大小："+liveInfoS.size());
			if (liveInfoS.size() != 0 && forwarderList.size() != 0) {
				for (LiveInfo live : liveInfoS) {
					for (ConfForwarder confForwarder1 : forwarderList) {
						if (live.getProcessorId().equals(confForwarder1.getId())) {
							confForwarder1.setState(live.getCommonStatus().toString());
							forwarderService.updateJob(confForwarder1);
							LOG.info("forwarderId="+live.getProcessorId()+",state="+live.getCommonStatus().toString());
						}
					}
				}
			} else {
				LOG.debug("后台获取forwarder的数据大小为："+liveInfoS.size());
			}
			
			
		} catch (Exception e) {
			LOG.debug(e.getClass()+"类中的executeInternal方法抛出"+e.getMessage());
		}
	}

	public ConfDatasourceService getDatasourceService() {
		return datasourceService;
	}

	public void setDatasourceService(ConfDatasourceService datasourceService) {
		this.datasourceService = datasourceService;
	}

	public ConfAgentService getConfAgentService() {
		return confAgentService;
	}

	public void setConfAgentService(ConfAgentService confAgentService) {
		this.confAgentService = confAgentService;
	}

	public ConfForwarderService getForwarderService() {
		return forwarderService;
	}

	public void setForwarderService(ConfForwarderService forwarderService) {
		this.forwarderService = forwarderService;
	}
	public static void main(String[] args) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
	}
}
