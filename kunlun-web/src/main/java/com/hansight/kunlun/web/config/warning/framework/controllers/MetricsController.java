package com.hansight.kunlun.web.config.warning.framework.controllers;

import java.io.BufferedOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hansight.kunlun.utils.Common;
import com.hansight.kunlun.web.proxy.ProxyServlet;

@Controller
@RequestMapping(value = "/monitor/spark")
public class MetricsController {
	private static final Map<String, String> masterMap = new ConcurrentHashMap<>();
	private static final Map<String, String> workerMap = new ConcurrentHashMap<>();
	private static String workerMetricsUrl = "/metrics/json/";
	private static String masterMetricsUrl = "/metrics/master/json/";
	
	static {
		init(masterMap, "spark.master.metrics");
		init(workerMap, "spark.worker.metrics");
		workerMetricsUrl = Common.get("spark.worker.metrics.url", workerMetricsUrl);
		masterMetricsUrl = Common.get("spark.worker.metrics.url", masterMetricsUrl);
	}
	
	public static void init (Map<String, String> workerMap, String name) {
		String master = Common.get(name);
		if (master != null) {
			String[] arr = master.split(",");
			for (String line : arr) {
				if (line != null && line.contains(":")) {
					String[] tmp = line.split(":");
					workerMap.put(tmp[0], line);
				}
			}
		}
	}
	

	@RequestMapping(value = "/workers.hs")
	public void workers(HttpServletRequest request, HttpServletResponse response) throws Exception {
		BufferedOutputStream out = new BufferedOutputStream(
				response.getOutputStream());
		StringBuffer sb = new StringBuffer();
		sb.append("{\"hosts\":[");
		for (String h : workerMap.keySet()) {
			sb.append("\"").append(h).append("\",");
		}
		if (workerMap.size() > 0) {
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("]}");
		out.write(sb.toString().getBytes());
		out.flush();
	}
	
	@RequestMapping(value = "/worker")
	public void worker(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			String host = request.getParameter("host");
			if (host == null) {
				response.setStatus(400);
				return;
			}
			String base = workerMap.get(host);
			if (base == null) {
				response.setStatus(400);
				return;
			}
			String url = "http://" + base + workerMetricsUrl;
			HttpGet httpGet = new HttpGet(url);
			ProxyServlet.process(httpGet, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
