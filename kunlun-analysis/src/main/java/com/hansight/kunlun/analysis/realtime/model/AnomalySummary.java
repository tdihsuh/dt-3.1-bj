package com.hansight.kunlun.analysis.realtime.model;

import java.util.Map;

import com.google.common.collect.Maps;

public class AnomalySummary {

	private String category = null;

	private String server = null; // not use for 6.30 version

	private String latestModify = null;

	private long count = 1L;

	public AnomalySummary() {
		super();
	}

	public Map<String, Object> toJson() {
		Map<String, Object> json = Maps.newHashMap();
		json.put("category", getCategory());
		// json.put("server",getServer()); // session only
		json.put("latestModify", getLatestModify());
		json.put("count", getCount());
		return json;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getLatestModify() {
		return latestModify;
	}

	public void setLatestModify(String latestModify) {
		this.latestModify = latestModify;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result
				+ ((latestModify == null) ? 0 : latestModify.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnomalySummary other = (AnomalySummary) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (count != other.count)
			return false;
		if (latestModify == null) {
			if (other.latestModify != null)
				return false;
		} else if (!latestModify.equals(other.latestModify))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnomalySummary [category=" + category + ", server=" + server
				+ ", latestModify=" + latestModify + ", count=" + count + "]";
	}

}
