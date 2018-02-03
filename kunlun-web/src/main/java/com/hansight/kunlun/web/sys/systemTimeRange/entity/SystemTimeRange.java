package com.hansight.kunlun.web.sys.systemTimeRange.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "SYSTEM_TIME_RANGE")
public class SystemTimeRange implements Serializable{

	private String id;

	private String category;

	private Long timeValue;

	private String timeUnit;

	private Long timeRefresh;

	private String timeRefreshUnit;

	private String userId;

	private Date dateUpdate;

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

	@Column(name = "CATEGORY")
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Column(name = "TIME_VALUE")
	public Long getTimeValue() {
		return timeValue;
	}

	public void setTimeValue(Long timeValue) {
		this.timeValue = timeValue;
	}

	@Column(name = "TIME_UNIT")
	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

	@Column(name = "TIME_REFRESH")
	public Long getTimeRefresh() {
		return timeRefresh;
	}

	public void setTimeRefresh(Long timeRefresh) {
		this.timeRefresh = timeRefresh;
	}

	@Column(name = "TIME_REFRESH_UNIT")
	public String getTimeRefreshUnit() {
		return timeRefreshUnit;
	}

	public void setTimeRefreshUnit(String timeRefreshUnit) {
		this.timeRefreshUnit = timeRefreshUnit;
	}

	@Column(name = "USER_ID")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name = "DATE_UPDATE")
	public Date getDateUpdate() {
		return dateUpdate;
	}

	public void setDateUpdate(Date dateUpdate) {
		this.dateUpdate = dateUpdate;
	}
}