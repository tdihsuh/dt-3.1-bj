package com.hansight.kunlun.web.config.systemlog.util;

public enum SystemLogModule {
	
	AGENT_ADD("采集器配置","新增采集器"),
	AGENT_DELETE("采集器配置","删除采集器"),
	AGENT_UPDATE("采集器配置","修改采集器"),
	DATASOURCE_ADD("数据源配置","新增数据源"),
	DATASOURCE_DELETE("数据源配置","删除数据源"),
	DATASOURCE_UPDATE("数据源配置","修改数据源"),
	FORWARDER_ADD("转发器配置","新增转发器"),
	FORWARDER_DELETE("转发器配置","删除转发器"),
	FORWARDER_UPDATE("转发器配置","修改转发器");
	
	private SystemLogModule(String _moduleName,String _option){
		this.moduleName = _moduleName;
		this.option = _option;
	}
	
	private String moduleName;
	private String option;
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	
}
