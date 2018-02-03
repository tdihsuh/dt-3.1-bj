package com.hansight.kunlun.analysis.realtime.single;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.google.gson.Gson;
import com.hansight.kunlun.utils.MetricsUtils;

public class MetricsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	@SuppressWarnings("rawtypes")
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Map<String, Object> map = new TreeMap<>();
		for (Map.Entry<String, Gauge> entry : MetricsUtils.metrics.getGauges().entrySet()) {
			map.put(entry.getKey(), entry.getValue().getValue());
		}
		for (Map.Entry<String, Counter> entry : MetricsUtils.metrics.getCounters().entrySet()) {
			map.put(entry.getKey(), entry.getValue().getCount());
		}
		PrintWriter out = resp.getWriter();
		Gson gson = new Gson();
		out.write(gson.toJson(map));
		out.flush();
	}

}
