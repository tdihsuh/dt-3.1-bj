package com.hansight.kunlun.web.config.datasource.util;

public class Log {
	private int id;
	private String logName;
	private String logValue;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public String getLogValue() {
		return logValue;
	}

	public void setLogValue(String logValue) {
		this.logValue = logValue;
	}
	
public static void main(String[] args) {
	String str = "G://Java;file;tomcat;regex";
	String sts = "127.1.1.1:80;snmp;snmp_tcp;regex";
	String strs[] = sts.split(";");
	String se [] = str.split(";");
	String ds[] = strs[0].split(":");
	String ds2[] = se[0].split(":");
	System.out.println(ds2.length);
	System.out.println(ds.length);
	for (int i = 0; i < strs.length; i++) {
		System.out.println(strs[i]);
	}
	
}
}
