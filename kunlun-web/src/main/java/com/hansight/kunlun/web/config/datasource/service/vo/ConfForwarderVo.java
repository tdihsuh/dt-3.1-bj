package com.hansight.kunlun.web.config.datasource.service.vo;

import java.util.Date;

/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_FORWARDER表对应的ConfForwarderVo
 */
public class ConfForwarderVo{
	private String id;
	private String name;
	private String ip;
	private String description;
	private String state;
	private Date createDate;
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getId(){
		return id;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getIp(){
		return ip;
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
	
}