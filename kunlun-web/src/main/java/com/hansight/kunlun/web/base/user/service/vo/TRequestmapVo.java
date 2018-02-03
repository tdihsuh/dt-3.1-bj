package com.hansight.kunlun.web.base.user.service.vo;

public class TRequestmapVo{
	private String id;
	private String description;
	private String url;
	private Long useFlag;
	
	public void setId(String id){
		this.id = id;
	}
	
	public Long getUseFlag(){
		return useFlag;
	}
	
	public void setUseFlag(Long useFlag){
		this.useFlag = useFlag;
	}
	
	public String getUrl(){
		return url;
	}
	
	public void setUrl(String url){
		this.url = url;
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
	
}