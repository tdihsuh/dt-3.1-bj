package com.hansight.kunlun.web.config.datasource.service.vo;

/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_DS_FORWARDER表对应的ConfDsForwarderQueryBean
 */
public class ConfDsForwarderQueryBean{
	private Long id;
	private String datasourceId;
	private String forwarderId;
	
	public Long getId(){
		return id;
	}
	
	public String getDatasourceId(){
		return datasourceId;
	}
	
	public void setDatasourceId(String datasourceId){
		this.datasourceId = datasourceId;
	}
	
	public String getForwarderId(){
		return forwarderId;
	}
	
	public void setForwarderId(String forwarderId){
		this.forwarderId = forwarderId;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	
}