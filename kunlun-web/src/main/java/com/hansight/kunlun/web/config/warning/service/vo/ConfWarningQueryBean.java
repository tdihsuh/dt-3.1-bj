package com.hansight.kunlun.web.config.warning.service.vo;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

public class ConfWarningQueryBean {
	private String id;
	
	private String name;
	
	private String state;
	
	private String description;
	
	private String space;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	 
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	
	public String getSpace() {
		return space;
	}
	public void setSpace(String space) {
		this.state = space;
	
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
}

