package com.hansight.kunlun.analysis.realtime.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Anomaly {

    private String c_ip = null;
    // yzhhui add by cookie
    private String cookieId = null;

    // Add c_ip to Country/City mapping:
    private String country = null;
    private String city = null;
    private float longitude = 0.0f;
    private float latitude = 0.0f;

    private String category = null;
    private String url = null;

    private String startDatetime = null;

    private String endDatetime = null;

    private Double degree = null;

    private Double modelDegree = null;

    private String eventType = null;

    private Map<String, String> servers = null;

    private String[] indices = null;
    
    private String uriQuery;

    private long counter = 0L;

    private long total = 0L;

    public Anomaly(String c_ip, String cookieId, String startDatetime, String endDatetime) {
        super();
        this.c_ip = c_ip;
        this.cookieId = cookieId;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = Maps.newHashMap();
        json.put("c_ip", this.c_ip);
        json.put("cookie_id", this.cookieId);
        if (country != null)
            json.put("country", country);
        if (city != null)
            json.put("city", city);
        if (longitude != 0.0f && latitude != 0.0f) {
            json.put("geo", latitude + "," + longitude);
        }
        json.put("category", this.category);
        json.put("eventType", this.eventType);
        json.put(RTConstants.MODEL_FIELD_DATE_KEY, this.startDatetime);
        json.put("degree", this.degree);
        json.put("modelDegree", this.modelDegree);
        if (url != null)
            json.put("url", this.url);
        if (uriQuery != null) {
        	json.put("url_query", this.uriQuery);
        }
        json.put("endDatetime", this.endDatetime);
        List<Map<String, String>> list = Lists.newArrayList();
        Map<String, String> tmp;
        for (Map.Entry<String, String> entry : this.servers.entrySet()) {
            tmp = new HashMap<>(1);
            tmp.put("ip", entry.getKey());
            tmp.put("server", entry.getValue());
            list.add(tmp);
        }
        json.put("servers", list);
        json.put("indices", this.indices);
        return json;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(String startDatetime) {
        this.startDatetime = startDatetime;
    }

    public String getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(String endDatetime) {
        this.endDatetime = endDatetime;
    }

    public String getCookieId() {
        return cookieId;
    }

    public String getC_ip() {
        return c_ip;
    }

    public void setC_ip(String c_ip) {
        this.c_ip = c_ip;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Double getDegree() {
        return degree;
    }

    public void setDegree(Double degree) {
        this.degree = degree;
    }

    public Double getModelDegree() {
        return modelDegree;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setModelDegree(Double modelDegree) {
        this.modelDegree = modelDegree;
    }

    public Map<String, String> getServers() {
        return servers;
    }

    public void setServers(Map<String, String> servers) {
        this.servers = servers;
    }

    public String[] getIndices() {
        return indices;
    }

    public void setIndices(String[] indices) {
        this.indices = indices;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public enum MsgType {
        OVER, BELOW
    }

    public String getUriQuery() {
		return uriQuery;
	}

	public void setUriQuery(String uriQuery) {
		this.uriQuery = uriQuery;
	}

	@Override
    public String toString() {
        return String.format("anomaly [c_ip=%s, category=%s%s, startDatetime=%s, endDatetime=%s, degree=%s, modelDegree=%s, servers=%s, indices=%s]", c_ip, category, (url != null ? (", url=" + url) : ""), startDatetime, endDatetime, degree, modelDegree, servers, Arrays.toString(indices));
    }

    public static class Msg {
        private String name;

        private Double calDegree = 0.0D;

        private Double modelDegree = 0.0D;

        private MsgType type;

        public Msg(String name, Double calDegree, Double modelDegree,
                   MsgType type) {
            super();
            this.name = name;
            this.calDegree = calDegree * 100;
            this.modelDegree = modelDegree * 100;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Double getCalDegree() {
            return calDegree;
        }

        public Double getModelDegree() {
            return modelDegree;
        }

        public MsgType getType() {
            return type;
        }

        public static void main(String[] args) {
            Msg msg = new Msg("404", 0.1, 0.3, MsgType.BELOW);
            System.out.println(msg);
        }
    }
}
