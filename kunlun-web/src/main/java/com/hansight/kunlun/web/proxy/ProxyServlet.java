package com.hansight.kunlun.web.proxy;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hansight.kunlun.utils.Common;
import com.hansight.kunlun.utils.FileUtils;

public class ProxyServlet extends HttpServlet {
	private static final long serialVersionUID = -5701008112628264371L;
	private static final Logger LOG = LoggerFactory
			.getLogger(ProxyServlet.class);
	private static List<String> hostList = new ArrayList<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		String hosts = Common.get("es.cluster.hosts");
		int port = Integer.parseInt(Common.get("es.cluster.http.port", "9200"));

		if (hosts == null) {
			return;
		}
		String[] arr = hosts.split(",");
		if (arr == null || arr.length == 0) {
			return;
		}
		for (String tmp : arr) {
			String[] tmpArr = tmp.split(":");
			String host = tmpArr[0];
			hostList.add("http://" + host + ":" + port);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		for (String host : hostList) {
			boolean error = false;
			try {
				HttpGet httpGet = new HttpGet(host + getURI(req));

				process(httpGet, resp);
			} catch (Exception e) {
				e.printStackTrace();
				error = true;
			}
			if (!error) {
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private String getURI(HttpServletRequest req) {
		String url = req.getRequestURL().toString();
		String real = url.substring(url.indexOf("/proxy/") + 6);
		Map<String, String[]> maps = req.getParameterMap();
		if (maps.size() > 0) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("?");
			for (Map.Entry<String, String[]> entry : maps.entrySet()) {
				sb.append(entry.getKey()).append("=")
						.append(connect(entry.getValue())).append("&");
			}
			if (maps.containsKey("ignore_unavailable")) {
				sb.deleteCharAt(sb.length() - 1);
			} else {
				sb.append("ignore_unavailable=true");
			}
			real += sb.toString();
		} else {
			real += "?ignore_unavailable=true";
		}
		return real;
	}

	private String connect(String[] arr) {
		if (arr != null) {
			StringBuffer sb = new StringBuffer();
			for (String s : arr) {
				sb.append(URLEncoder.encode(s)).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		}
		return "";
	}

	public static void process(HttpRequestBase hrb, HttpServletResponse resp)
			throws IOException {
//		LOG.debug("process {} {}", hrb.getMethod(), hrb.getURI());
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response1 = httpclient.execute(hrb);
		try {
			StatusLine statusLine = response1.getStatusLine();
			if (200 != statusLine.getStatusCode()) {
				resp.sendError(statusLine.getStatusCode(),
						statusLine.getReasonPhrase());
			} else {
				HttpEntity entity1 = response1.getEntity();
				BufferedOutputStream out = new BufferedOutputStream(
						resp.getOutputStream());
				String con = FileUtils.read(entity1.getContent());
				out.write(con.getBytes());
				EntityUtils.consume(entity1);
				out.flush();
			}
		} finally {
			response1.close();
			httpclient.close();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		for (String host : hostList) {
			boolean error = false;
			try {
				HttpPost post = new HttpPost(host + getURI(req));
				post.setEntity(new InputStreamEntity(req.getInputStream()));
				process(post, resp);
			} catch (Exception e) {
				e.printStackTrace();
				error = true;
			}
			if (!error) {
				break;
			}
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		for (String host : hostList) {
			boolean error = false;
			try {
				HttpPut put = new HttpPut(host + getURI(req));
				put.setEntity(new InputStreamEntity(req.getInputStream()));
				process(put, resp);
			} catch (Exception e) {
				e.printStackTrace();
				error = true;
			}
			if (!error) {
				break;
			}
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		for (String host : hostList) {
			boolean error = false;
			try {
				MyHttpDelete put = new MyHttpDelete(host+getURI(req));
				String content = FileUtils.read(req.getInputStream());
				put.setEntity(new StringEntity(content));
				process(put,resp);
			} catch (Exception e) {
				e.printStackTrace();
				error = true;
			}
			if (!error) {
				break;
			}
		}
	}
}
