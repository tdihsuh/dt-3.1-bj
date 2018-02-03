package com.hansight.kunlun.web.config.datasource.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tao_zhang
 * @date 2014年8月18日 CONF_DATASOURCE表对应的实体类
 */
@Entity
@Table(name = "CONF_DATASOURCE")
public class ConfDatasource implements Serializable {

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

	private String forwarderName;
	
	private Date createDate;
	
	private String datasourceName;
	
	private String config;
	
	private String configInfo;
	
	
	
	@Id
	@Column(nullable = false, length = 40)
	// @GeneratedValue(generator = "hibernate-uuid")
	// @GenericGenerator(name = "hibernate-uuid", strategy = "uuid")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "AGENT_ID")
	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	@Column(name = "CATEGORY_ID")
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	@Column(name = "TYPE")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "PATTERN")
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Column(name = "AGENT_PARSER")
	public String getAgentParser() {
		return agentParser;
	}

	public void setAgentParser(String agentParser) {
		this.agentParser = agentParser;
	}

	@Column(name = "HOST")
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Column(name = "PORT")
	public Long getPort() {
		return port;
	}

	public void setPort(Long port) {
		this.port = port;
	}

	@Column(name = "PROTOCOL")
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Column(name = "URL")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "ENCODING")
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Column(name = "FORWARDER_PARSER")
	public String getForwarderParser() {
		return forwarderParser;
	}

	public void setForwarderParser(String forwarderParser) {
		this.forwarderParser = forwarderParser;
	}

	@Column(name = "CATEGORY")
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Column(name = "STATE")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Column(name = "FORWARDER_NAME")
	public String getForwarderName() {
		return forwarderName;
	}

	public void setForwarderName(String forwarderName) {
		this.forwarderName = forwarderName;
	}
	@Column(name = "CREATE_DATE")
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	@Column(name = "DATASOURCE_NAME")
	public String getDatasourceName() {
		return datasourceName;
	}

	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}

	public String getConfig() {
		return config;
	}
	@Column(name = "CONFIG")
	public void setConfig(String config) {
		this.config = config;
	}
	@Column(name = "CONFIGINFO")
	public String getConfigInfo() {
		return configInfo;
	}

	public void setConfigInfo(String configInfo) {
		this.configInfo = configInfo;
	}
	
	
}