package com.hansight.kunlun.web.base.user.service.vo;

public class TAuthorityVo{
	private String id;
	private String name;
	private String description;
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
	
	public String getName(){
		return name;
	}
	
	public String getId(){
		return id;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
}