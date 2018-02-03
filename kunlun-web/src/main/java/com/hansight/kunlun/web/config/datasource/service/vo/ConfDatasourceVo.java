package com.hansight.kunlun.web.config.datasource.service.vo;

/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_DATASOURCE表对应的ConfDatasourceVo
 */
public class ConfDatasourceVo{
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
	private String agentName;
	private String datasourceName;
	private String config;
	private String configInfo;
	public String getId(){
		return id;
	}
	
	public String getType(){
		return type;
	}
	
	public String getEncoding(){
		return encoding;
	}
	
	public String getProtocol(){
		return protocol;
	}
	
	public String getHost(){
		return host;
	}
	
	public Long getPort(){
		return port;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public void setEncoding(String encoding){
		this.encoding = encoding;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getAgentId(){
		return agentId;
	}
	
	public void setAgentId(String agentId){
		this.agentId = agentId;
	}
	
	public String getCategoryId(){
		return categoryId;
	}
	
	public void setCategoryId(String categoryId){
		this.categoryId = categoryId;
	}
	
	public void setHost(String host){
		this.host = host;
	}
	
	public void setPort(Long port){
		this.port = port;
	}
	
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}
	
	public String getUrl(){
		return url;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public String getPattern(){
		return pattern;
	}
	
	public void setPattern(String pattern){
		this.pattern = pattern;
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

	public String getForwarderName() {
		return forwarderName;
	}

	public void setForwarderName(String forwarderName) {
		this.forwarderName = forwarderName;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getDatasourceName() {
		return datasourceName;
	}

	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getConfigInfo() {
		return configInfo;
	}

	public void setConfigInfo(String configInfo) {
		this.configInfo = configInfo;
	}
	
}