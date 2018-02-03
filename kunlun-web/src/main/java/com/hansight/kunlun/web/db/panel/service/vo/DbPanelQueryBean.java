package com.hansight.kunlun.web.db.panel.service.vo;

import java.util.Date;

public class DbPanelQueryBean{
	private String id;

	private String name;

	private String type;

	private String content;

	private Long position;

	private String userId;

	private Long useFlag;

	private Date dateCreated;
	
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
	
	public Long getPosition(){
		return position;
	}
	
	public void setPosition(Long position){
		this.position = position;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getUserId(){
		return userId;
	}
	
	public void setUserId(String userId){
		this.userId = userId;
	}
	
	public Long getUseFlag(){
		return useFlag;
	}
	
	public void setUseFlag(Long useFlag){
		this.useFlag = useFlag;
	}
	
	public Date getDateCreated(){
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated){
		this.dateCreated = dateCreated;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}