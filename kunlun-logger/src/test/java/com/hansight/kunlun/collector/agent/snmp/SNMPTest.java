package com.hansight.kunlun.collector.agent.snmp;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class SNMPTest {
	public static void main(String[] args) {
		Map<String, String> map = new HashMap<>();
		map.put("aa", "bb");
		Gson gson = new Gson();
		String str = gson.toJson(map);
		System.out.println(str);
	}
}
