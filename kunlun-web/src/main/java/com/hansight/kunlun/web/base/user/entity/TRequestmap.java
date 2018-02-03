package com.hansight.kunlun.web.base.user.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "T_REQUESTMAP")
public class TRequestmap implements Serializable{

	private String id;

	private String description;

	private String url;

	private Long useFlag = 1L;

	@Id
	@Column(nullable=false,length = 32)
	@GeneratedValue(generator="hibernate-uuid")
	@GenericGenerator(name = "hibernate-uuid", strategy = "uuid")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "DESCRIPTION")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "URL")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "USE_FLAG")
	public Long getUseFlag() {
		return useFlag;
	}

	public void setUseFlag(Long useFlag) {
		this.useFlag = useFlag;
	}
}