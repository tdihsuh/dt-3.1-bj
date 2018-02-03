package com.hansight.kunlun.web.base.user.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "T_AUTHORITY_REQUESTMAP")
public class TAuthorityRequestmap implements Serializable{

	private String id;

	private String authorityId;

	private String requestmapId;

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

	@Column(name = "AUTHORITY_ID")
	public String getAuthorityId() {
		return authorityId;
	}

	public void setAuthorityId(String authorityId) {
		this.authorityId = authorityId;
	}

	@Column(name = "REQUESTMAP_ID")
	public String getRequestmapId() {
		return requestmapId;
	}

	public void setRequestmapId(String requestmapId) {
		this.requestmapId = requestmapId;
	}
}