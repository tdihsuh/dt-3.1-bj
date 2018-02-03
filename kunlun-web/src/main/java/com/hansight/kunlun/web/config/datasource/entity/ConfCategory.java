package com.hansight.kunlun.web.config.datasource.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_CATEGORY表对应的实体类
 */
@Entity
@Table(name = "CONF_CATEGORY")
public class ConfCategory implements Serializable{
	
	private String id;
	
	private String name;
	
	private String type;
	
	private String pattern;
	
	private String agentParser;
	
	private byte[] example;
	
	private Date createDate;
	
	private String forwarderParser;
	
	private String protocol;
	
	
	
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

	@Column(name="NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name="PATTERN")
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Column(name="AGENT_PARSER")
	public String getAgentParser() {
		return agentParser;
	}

	public void setAgentParser(String agentParser) {
		this.agentParser = agentParser;
	}

	@Column(name="CREATEDATE")
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	@Column(name="EXAMPLE")
	public byte[] getExample() {
		return example;
	}

	public void setExample(byte[] example) {
		this.example = example;
	}
	@Column(name="TYPE")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	@Column(name="FORWARDER_PARSER")
	public String getForwarderParser() {
		return forwarderParser;
	}

	public void setForwarderParser(String forwarderParser) {
		this.forwarderParser = forwarderParser;
	}
	@Column(name="PROTOCOL")
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}