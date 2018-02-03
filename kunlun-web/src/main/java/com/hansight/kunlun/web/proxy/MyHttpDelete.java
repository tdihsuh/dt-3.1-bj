package com.hansight.kunlun.web.proxy;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class MyHttpDelete extends HttpEntityEnclosingRequestBase {
	
	public static final String METHOD_NAME = "DELETE";
	
	@Override public String getMethod(){
		return METHOD_NAME;
	}
	
	public MyHttpDelete(final String uri){
		super();
		setURI(URI.create(uri));
	}
	
	public MyHttpDelete(final URI uri){
		super();
		setURI(uri);
	}
	
	public MyHttpDelete(){
		super();
	}
}
