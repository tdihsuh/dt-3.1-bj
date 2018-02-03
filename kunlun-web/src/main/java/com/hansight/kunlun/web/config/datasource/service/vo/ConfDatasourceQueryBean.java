package com.hansight.kunlun.web.config.datasource.service.vo;

import java.io.Serializable;
import java.util.Date;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_DATASOURCE表对应的ConfDatasourceQueryBean
 */
public class ConfDatasourceQueryBean implements Serializable{
	private String id;
	private String agentId;
	private String categoryId;
	private String type;
	private String pattern;
	private String agentParser;
	private String host;
	private Long port;
	private String protocol;
	private String url;
	private String encoding;
	private String forwarderParser;
	private String category;
	private String state;
	private Date   createDate;
	private String config;
	private String datasourceName;
	private String forwarderName;
	private String configInfo;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Long getPort() {
		return port;
	}
	public void setPort(Long port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getAgentParser() {
		return agentParser;
	}
	public void setAgentParser(String agentParser) {
		this.agentParser = agentParser;
	}
	public String getForwarderParser() {
		return forwarderParser;
	}
	public void setForwarderParser(String forwarderParser) {
		this.forwarderParser = forwarderParser;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}
	public String getDatasourceName() {
		return datasourceName;
	}
	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}
	public String getForwarderName() {
		return forwarderName;
	}
	public void setForwarderName(String forwarderName) {
		this.forwarderName = forwarderName;
	}
	public String getConfigInfo() {
		return configInfo;
	}
	public void setConfigInfo(String configInfo) {
		this.configInfo = configInfo;
	}
	
	
}
