package com.hansight.kunlun.web.config.datasource.util;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.coordinator.metric.ConfigInfo;
import com.hansight.kunlun.coordinator.metric.LiveInfo;
import com.hansight.kunlun.coordinator.metric.MetricException;
import com.hansight.kunlun.coordinator.metric.MetricMsg;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.ProcessorType;
import com.hansight.kunlun.coordinator.metric.WorkerStatus.ConfigStatus;

public class MonitorConfig {
	private static final Logger LOG = LoggerFactory.getLogger(MonitorConfig.class);
	 
	/****监控数据源状态******/
	public static List<MetricMsg> dataSourceMonitor(){
		MetricService ms = new MetricService();
		List<MetricMsg> list = null;
		try {
			list = ms.getDatasourceStatus();
		} catch (MetricException e) {
			LOG.debug(e.getClass()+"类中的dataSourceMonitor方法抛出"+e.getMessage());
		} catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的dataSourceMonitor方法抛出"+e.getMessage());
		}finally{
			try {
				ms.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的dataSourceMonitor方法抛出"+e.getMessage());
			}
		}
		return list;
	}
	/****监控数据源配置成功与否**********/
	public static List<ConfigInfo> dataSourceConfigInfo(){
		MetricService ms = new MetricService();
		List<ConfigInfo> configStatus = null;
		try {
			configStatus = ms.getConfigStatus();
			LOG.info("从后台获取的datasource对应ConfigInfo配置信息:"+configStatus.size());
		} catch (MetricException e) {
			LOG.debug(e.getClass()+"类中的dataSourceConfigInfo方法抛出"+e.getMessage());
		} catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的dataSourceConfigInfo方法抛出"+e.getMessage());
		}finally{
			try {
				ms.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的dataSourceConfigInfo方法抛出"+e.getMessage());
			}
		}
		return configStatus;
	}
	/****监控采集器状态********/
	public static List<LiveInfo> getAgentLive(){
		MetricService ms = new MetricService();
		List<LiveInfo> agentLives = null;
		try {
			agentLives= ms.getStatus(ProcessorType.AGENT);
			LOG.info("从后台获取的agent状态信息:"+agentLives.size());
		} catch (MetricException e) {
			LOG.debug(e.getClass()+"类中的getAgentLive方法抛出"+e.getMessage());
		} catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的getAgentLive方法抛出"+e.getMessage());
		}finally{
			try {
				ms.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的getAgentLive方法抛出"+e.getMessage());
			}
		}
		return agentLives;
	}
	
	/*****监控转发器状态*******/
	public static List<LiveInfo> getForwarderLive(){
		MetricService ms = new MetricService();
		List<LiveInfo> forwarderLives = null;
		try {
			forwarderLives= ms.getStatus(ProcessorType.FORWARDER);
			LOG.info("从后台获取的forwarder状态信息:"+forwarderLives.size());
		} catch (MetricException e) {
			LOG.debug(e.getClass()+"类中的getForwarderLive方法抛出"+e.getMessage());
		} catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的getForwarderLive方法抛出"+e.getMessage());
		}finally{
			try {
				ms.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的getForwarderLive方法抛出"+e.getMessage());
			}
		}
		return forwarderLives;
	}
	public static Map<String, ConfigStatus> getDatasourceConfig(){
		MetricService ms = new MetricService();
		Map<String, ConfigStatus> map= null;
		try {
			map = ms.getDatasourceConfig();
			LOG.info("从后台获取的datasource Config状态信息:"+map.size());
		} catch (MetricException e) {
			LOG.debug(e.getClass()+"类中的getDatasourceConfig方法抛出"+e.getMessage());
		} catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的getDatasourceConfig方法抛出"+e.getMessage());
		}finally{
			try {
				ms.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的getDatasourceConfig方法抛出"+e.getMessage());
			}
		}
		return map;
	}
	
	//@org.junit.Test
	public void test() throws Exception{
		String datasourceId = "297e123148a719020148a71ad7ce0001";
		String processorId = "297e123148a63d7f0148a63f6aff0001";
		
		MetricService ms = new MetricService(datasourceId, processorId, ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.SUCCESS);
	}

	//@org.junit.Test
	public void test2() throws Exception{
		MetricService ms = new MetricService();
		List<ConfigInfo> configStatus = ms.getConfigStatus();
		LOG.info("从后台获取到的数据信息：：：：：", configStatus.size());
	}
}
