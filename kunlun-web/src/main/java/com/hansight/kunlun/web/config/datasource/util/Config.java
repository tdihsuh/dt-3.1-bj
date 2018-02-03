package com.hansight.kunlun.web.config.datasource.util;

import com.hansight.kunlun.coordinator.config.*;
import com.hansight.kunlun.web.config.datasource.entity.ConfDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * @author tao_zhang
 * @date 2014年8月24日
 * 
 */

public class Config {
	private static final Logger LOG = LoggerFactory.getLogger(Config.class);
	/***agentAdd**/
	public static boolean addAgent(ConfDatasource datasource) {
		boolean flag = true;
		AgentConfigService agent = new AgentConfigService(datasource.getAgentId());
		try {
			AgentConfig config = new AgentConfig();
			config.put("id",datasource.getId());
			config.put("reader",datasource.getAgentParser());
			config.put("type",datasource.getType());
			if(null!=datasource.getUrl()){
				config.put("uri", datasource.getUrl());
			}
			if(null!=datasource.getPattern()){
				config.put("pattern",datasource.getPattern());
			}
			if(null!=datasource.getHost()){
				config.put("host", datasource.getHost());
			}
			if(null!=datasource.getPort()){
				config.put("port",datasource.getPort().toString());
			}
			config.put("encoding",datasource.getEncoding());
			
			config.put("lexer", datasource.getForwarderParser());
			config.put("protocol",datasource.getProtocol());
			config.put("category",datasource.getCategory());
			config.put("writer", "es");
			config.put("start_position", "end");
			
			agent.add(config);
			LOG.info("-------前端添加Agent对应的config的参数-----[datasourceId="+datasource.getId()+",reader="+datasource.getAgentParser()+",uri="+datasource.getUrl()+",start_position=end]-----agentId="+datasource.getAgentId());
			LOG.info("-------前端添加Agent对应的config的参数-----[type="+datasource.getType()+",pattern="+datasource.getPattern()+",host="+datasource.getHost()+",port="+datasource.getPort()+",encoding="+datasource.getEncoding()+",lexer="+datasource.getForwarderParser()+"]");
			LOG.info("-------前端添加Agent对应的config的参数-----[protocol="+datasource.getProtocol()+",category="+datasource.getCategory()+"]");
			flag = true;
		} catch (ConfigException e) {
			LOG.debug(e.getClass()+"类中的addAgent方法抛出"+e.getMessage());
			flag = false;
		} catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的addAgent方法抛出"+e.getMessage());
			flag = false;
		}finally{
			try {
				agent.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的addAgent方法抛出"+e.getMessage());
				flag = false;
			}
		}
		return flag;
	}
	/****agentUpdate***/
	public static boolean updateAgent(ConfDatasource datasource) {
		boolean flag = true;
		AgentConfigService agent = new AgentConfigService(datasource.getAgentId());
		try{
			AgentConfig config = new AgentConfig();
			config.put("id",datasource.getId());
			config.put("reader",datasource.getAgentParser());
			config.put("type",datasource.getType());
			
			if(null!=datasource.getUrl()){
				config.put("uri", datasource.getUrl());
			}
			if(null!=datasource.getPattern()){
				config.put("pattern",datasource.getPattern());
			}
			if(null!=datasource.getHost()){
				config.put("host", datasource.getHost());
			}
			if(null!=datasource.getPort()){
				config.put("port",datasource.getPort().toString());
			}
			
			config.put("encoding",datasource.getEncoding());
			config.put("lexer", datasource.getForwarderParser());
			config.put("protocol",datasource.getProtocol());
			config.put("category",datasource.getCategory());
			config.put("writer", "es");
			config.put("start_position", "end");
			agent.update(config);
			LOG.info("-------前端更新Agent对应的config的参数-----[datasourceId="+datasource.getId()+",reader="+datasource.getAgentParser()+",uri="+datasource.getUrl()+",start_position=end]-----agentId="+datasource.getAgentId());
			LOG.info("-------前端更新Agent对应的config的参数-----[type="+datasource.getType()+",pattern="+datasource.getPattern()+",host="+datasource.getHost()+",port="+datasource.getPort()+",encoding="+datasource.getEncoding()+",lexer="+datasource.getForwarderParser()+"]");
			LOG.info("-------前端更新Agent对应的config的参数-----[protocol="+datasource.getProtocol()+",category="+datasource.getCategory()+"]");
			flag = true;
		} catch (ConfigException e) {
			LOG.debug(e.getClass()+"类中的updateAgent方法抛出"+e.getMessage());
			flag = false;
		} catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的updateAgent方法抛出"+e.getMessage());
			flag = false;
		}finally{
			try {
				agent.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的updateAgent方法抛出"+e.getMessage());
				flag = false;
			}
		}
		return flag;
	}
	/****agentDelete***/
	public static boolean deleteAgent(ConfDatasource datasource) {
		AgentConfigService agent = new AgentConfigService(datasource.getAgentId());
		boolean flag = true;
		try{
			AgentConfig config = new AgentConfig();
			config.put("id",datasource.getId());
			config.put("reader",datasource.getAgentParser());
			config.put("type",datasource.getType());
			if(null!=datasource.getUrl()){
				config.put("uri", datasource.getUrl());
			}
			if(null!=datasource.getPattern()){
				config.put("pattern",datasource.getPattern());
			}
			if(null!=datasource.getHost()){
				config.put("host", datasource.getHost());
			}
			if(null!=datasource.getPort()){
				config.put("port",datasource.getPort().toString());
			}
			
			config.put("encoding",datasource.getEncoding());
			
			config.put("lexer", datasource.getForwarderParser());
			config.put("protocol",datasource.getProtocol());
			config.put("category",datasource.getCategory());
			config.put("writer", "es");
			config.put("start_position", "end");
			agent.delete(config);
			LOG.info("-------前端删除Agent对应的config的参数-----[datasourceId="+datasource.getId()+",reader="+datasource.getAgentParser()+",uri="+datasource.getUrl()+",start_position=end]-----agentId="+datasource.getAgentId());
			LOG.info("-------前端删除Agent对应的config的参数-----[type="+datasource.getType()+",pattern="+datasource.getPattern()+",host="+datasource.getHost()+",port="+datasource.getPort()+",encoding="+datasource.getEncoding()+",lexer="+datasource.getForwarderParser()+"]");
			LOG.info("-------前端删除Agent对应的config的参数-----[protocol="+datasource.getProtocol()+",category="+datasource.getCategory()+"]");
			flag = true;
		} catch (ConfigException e) {
			LOG.debug(e.getClass()+"类中的deleteAgent方法抛出"+e.getMessage());
			flag = false;
		}catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的deleteAgent方法抛出"+e.getMessage());	
			flag = false;
		}finally{
			try {
				agent.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的deleteAgent方法抛出"+e.getMessage());
				flag = false;
			}
		}
		return flag;
	}
	/***forwarderAdd****/
	public static boolean addForwarder(String id,String category,String parser,String type,String pattern,String forwarderId){
		ForwarderConfigService forwarder = new ForwarderConfigService(forwarderId);
		boolean flag = true;
		try{
			ForwarderConfig config = new ForwarderConfig();
			config.put("id", id);
			config.put("category", category);
			config.put("parser", parser);
			config.put("type", type);
			config.put("pattern", pattern);
			forwarder.add(config);
			LOG.info("-------前端添加forwarder对应config的参数-----[datasourceId="+id+",category="+category+",parser="+parser+",type="+type+",pattern="+pattern+"]----forwarderId="+forwarderId);
			flag = true;
		} catch (ConfigException e) {
			LOG.debug(e.getClass()+"类中的addForwarder方法抛出"+e.getMessage());
			flag = false;
		}catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的addForwarder方法抛出"+e.getMessage());
			flag = false;
		}finally{
			try {
				forwarder.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的addForwarder方法抛出"+e.getMessage());
				flag = false;
			}
		}
		return flag;
	}
	/****forwarderUpdate***/
	public static boolean updateForwarder(String id,String category,String parser,String type,String pattern,String forwarderId){
		ForwarderConfigService forwarder = new ForwarderConfigService(forwarderId);
		boolean flag = true;
		try{
			ForwarderConfig config = new ForwarderConfig();
			config.put("id", id);
			config.put("category", category);
			config.put("parser", parser);
			config.put("type", type);
			config.put("pattern", pattern);
			forwarder.update(config);
			LOG.info("-------前端更新forwarder对应config的参数-----[datasourceId="+id+",category="+category+",parser="+parser+",type="+type+",pattern="+pattern+"]----forwarderId="+forwarderId);
			flag = true;
		} catch (ConfigException e) {
			LOG.debug(e.getClass()+"类中的updateForwarder方法抛出"+e.getMessage());
			flag = false;
		}catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的updateForwarder方法抛出"+e.getMessage());
			flag = false;
		}finally{
			try {
				forwarder.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的updateForwarder方法抛出"+e.getMessage());
				flag = false;
			}
		}
		return flag;
	}
	/****forwarderDelete***/
	public static boolean deleteForwarder(String id,String category,String parser,String type,String pattern,String forwarderId){
		ForwarderConfigService forwarder = new ForwarderConfigService(forwarderId);
		boolean flag = true;
		try{
			ForwarderConfig config = new ForwarderConfig();
			config.put("id", id);
			config.put("category", category);
			config.put("parser", parser);
			config.put("type", type);
			config.put("pattern", pattern);
			forwarder.delete(config);
			LOG.info("-------前端删除forwarder对应config的参数-----[datasourceId="+id+",category="+category+",parser="+parser+",type="+type+",pattern="+pattern+"]----forwarderId="+forwarderId);
			flag = true;
		} catch (ConfigException e) {
			LOG.debug(e.getClass()+"类中的updateForwarder方法抛出"+e.getMessage());
			flag = false;
		}catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的updateForwarder方法抛出"+e.getMessage());
			flag = false;
		}finally{
			try {
				forwarder.close();
			} catch (IOException e) {
				LOG.debug(e.getClass()+"类中的deleteForwarder方法抛出"+e.getMessage());
				flag = false;
			}
		}
		return flag;
	}

	/****ForwarderDelete***/
	public static boolean deleteConfForwarder(String forwarderId){
	           List<String> list = new ArrayList<String>();
	           list.add(forwarderId);
	           boolean flag = true;  
	        try {
				ConfigUtils.deleteAllByIds(list, ConfigCategory.FORWARDER);
				LOG.info("删除forwarderId="+forwarderId+"的config信息");
				flag = true;
			} catch (ConfigException e) {
				LOG.debug(e.getClass()+"类中的deleteConfForwarder方法抛出"+e.getMessage());
				//flag = false;
			}catch (ConnectException e) {
				LOG.debug(e.getClass()+"类中的deleteConfForwarder方法抛出"+e.getMessage());
				flag = false;
			}
	        return flag;
	}
	
	/****ForwarderDelete***/
	public static boolean deleteConfAgent(String agentId){
		 List<String> list = new ArrayList<String>();
         list.add(agentId);
		boolean flag = true;  
		try{
	           ConfigUtils.deleteAllByIds(list, ConfigCategory.AGENT);
	           LOG.info("删除agentId="+agentId+"的config信息");
	           flag = true; 
		} catch (ConfigException e) {
			LOG.debug(e.getClass()+"类中的deleteConfAgent方法抛出"+e.getMessage());
			//flag = false;
		}catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的deleteConfForwarder方法抛出"+e.getMessage());
			flag = false;
		}
		  return flag;
	}
	 public static void forwarderQuery(String forwarderId) throws ConfigException {
		 LOG.info("forwarder测试开始");
		 ForwarderConfigService forwarder = new ForwarderConfigService(forwarderId);
	        List<ForwarderConfig> configs;
			try {
				configs = forwarder.queryAll();
		        for (ForwarderConfig config : configs) {
		            LOG.info("forwarderId = " + config.get("id"));
		            LOG.info("parser = " + config.get("parser"));
		            LOG.info("type = " + config.get("type"));
		            LOG.info("pattern = " + config.get("pattern"));
		        }
			} catch (ConnectException e) {
				LOG.debug(e.getClass()+"类中的forwarderQuery方法抛出"+e.getMessage());
			}
	     LOG.info("forwarder测试结束");
	 }
	 public static void agentQuery(String agentId) throws ConfigException {
		 LOG.info("agent测试开始");
		 try{
	    	AgentConfigService agent = new AgentConfigService(agentId);
	        List<AgentConfig> configs = agent.queryAll();
	        for (AgentConfig config : configs) {
	            LOG.info("agentId = " + config.get("id"));
	            LOG.info("category = " + config.get("category"));
	            LOG.info("uri = " + config.get("uri"));
	        }
	 	} catch (ConnectException e) {
			LOG.debug(e.getClass()+"类中的agentQuery方法抛出"+e.getMessage());
		}
	      LOG.info("agent测试结束");
	 }
	 public static void main(String[] args) {
		 UUID uuid = UUID.randomUUID();
		  System.out.println(uuid);
	 }
}
