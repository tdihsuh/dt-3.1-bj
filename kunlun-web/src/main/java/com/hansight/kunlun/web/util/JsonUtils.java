package com.hansight.kunlun.web.util;

import com.google.gson.Gson;

public class JsonUtils {
	private static Gson gson = new Gson();

	public static String toJson(Object object) {
		return gson.toJson(object);
	}
	
	public static <T> T fromJson(String json,Class<T> className) {
		return gson.fromJson(json, className);
	}
	
	public static <T> T transform(Object object,Class<T> className){
		return gson.fromJson(gson.toJson(object), className);
	}
}
