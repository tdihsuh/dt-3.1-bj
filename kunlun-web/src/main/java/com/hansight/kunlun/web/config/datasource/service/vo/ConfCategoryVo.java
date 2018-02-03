package com.hansight.kunlun.web.config.datasource.service.vo;

import java.util.Date;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_CATEGORY表对应的ConfCategoryVo
 */
public class ConfCategoryVo{
	private String id;
	private String name;
	private String type;
	private String pattern;
	private String agentParser;
	private String example;
	private Date createdate;
	private String protocol;
	private String forwarderParser;
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getId(){
		return id;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public Date getCreatedate(){
		return createdate;
	}
	
	public void setCreatedate(Date createdate){
		this.createdate = createdate;
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

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getForwarderParser() {
		return forwarderParser;
	}

	public void setForwarderParser(String forwarderParser) {
		this.forwarderParser = forwarderParser;
	}
	
}