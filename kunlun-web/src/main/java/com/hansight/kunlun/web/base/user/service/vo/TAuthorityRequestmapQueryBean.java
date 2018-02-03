package com.hansight.kunlun.web.base.user.service.vo;

public class TAuthorityRequestmapQueryBean{
	private String id;
	private String authorityId;
	private String requestmapId;
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getAuthorityId(){
		return authorityId;
	}
	
	public void setAuthorityId(String authorityId){
		this.authorityId = authorityId;
	}
	
	public String getRequestmapId(){
		return requestmapId;
	}
	
	public void setRequestmapId(String requestmapId){
		this.requestmapId = requestmapId;
	}
	
	public String getId(){
		return id;
	}
	
}