package com.hansight.kunlun.web.config.datasource.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_DS_FORWARDER表对应的实体类
 */
@Entity
@Table(name = "CONF_DS_FORWARDER")
public class ConfDsForwarder implements Serializable{

	private String id;
	
	private String datasourceId;
	
	private String forwarderId;
	
	@Id
	@Column(nullable=false,length = 40)
	@GeneratedValue(generator="hibernate-uuid")
	@GenericGenerator(name = "hibernate-uuid", strategy = "uuid")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name = "DATASOURCE_ID")
	public String getDatasourceId() {
		return datasourceId;
	}
	public void setDatasourceId(String datasourceId) {
		this.datasourceId = datasourceId;
	}
	@Column(name = "FORWARDER_ID")
	public String getForwarderId() {
		return forwarderId;
	}
	public void setForwarderId(String forwarderId) {
		this.forwarderId = forwarderId;
	}
	
	
	
	

}
