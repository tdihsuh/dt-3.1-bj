package com.hansight.kunlun.web.sys.systemTimeRange.service.vo;

import java.util.Date;

public class SystemTimeRangeQueryBean{
	private String id;
	private String category;
	private Long timeValue;
	private String timeUnit;
	private String userId;
	private Date dateUpdate;
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getCategory(){
		return category;
	}
	
	public void setCategory(String category){
		this.category = category;
	}
	
	public Long getTimeValue(){
		return timeValue;
	}
	
	public void setTimeValue(Long timeValue){
		this.timeValue = timeValue;
	}
	
	public String getTimeUnit(){
		return timeUnit;
	}
	
	public void setTimeUnit(String timeUnit){
		this.timeUnit = timeUnit;
	}
	
	public String getUserId(){
		return userId;
	}
	
	public void setUserId(String userId){
		this.userId = userId;
	}
	
	public Date getDateUpdate(){
		return dateUpdate;
	}
	
	public void setDateUpdate(Date dateUpdate){
		this.dateUpdate = dateUpdate;
	}
	
}