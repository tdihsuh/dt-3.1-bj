package com.hansight.kunlun.analysis.statistics.model;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: zhhui
 * Date: 2014/5/19
 */
public class TimeTotalMaps implements Serializable {
    private Long time;
    private String IP;
    private Integer total;
    private TreeMap<String, Integer> uniqueUrls;
    private TreeMap<Integer, Integer> responseCodes;

    public TimeTotalMaps() {
        uniqueUrls = new TreeMap<>();
        responseCodes = new TreeMap<>();
        total = 0;
        this.time = null;
    }

    public TimeTotalMaps(Long time) {
        uniqueUrls = new TreeMap<>();
        responseCodes = new TreeMap<>();
        total = 0;
        this.time = time;
    }

    public void inc() {
        total++;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }
    /*
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }*/

    public TreeMap<String, Integer> getUniqueUrls() {
        return uniqueUrls;
    }

    public TreeMap<Integer, Integer> getResponseCodes() {
        return responseCodes;
    }

    public TimeTotalMaps add(TimeTotalMaps other) {
        TreeMap<Integer, Integer> codes = other.responseCodes;
        for (Map.Entry<Integer, Integer> entry : codes.entrySet()) {
            Integer value = responseCodes.get(entry.getKey());
            responseCodes.put(entry.getKey(), (value == null ? 0 : value) + entry.getValue());

        }
        TreeMap<String, Integer> urls = other.uniqueUrls;
        for (Map.Entry<String, Integer> entry : urls.entrySet()) {
            Integer value = uniqueUrls.get(entry.getKey());
            uniqueUrls.put(entry.getKey(), (value == null ? 0 : value) + entry.getValue());

        }
        this.total += other.total;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeTotalMaps)) return false;

        TimeTotalMaps that = (TimeTotalMaps) o;

        if (responseCodes != null ? !responseCodes.equals(that.responseCodes) : that.responseCodes != null)
            return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (total != null ? !total.equals(that.total) : that.total != null) return false;
        if (uniqueUrls != null ? !uniqueUrls.equals(that.uniqueUrls) : that.uniqueUrls != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (total != null ? total.hashCode() : 0);
        result = 31 * result + (uniqueUrls != null ? uniqueUrls.hashCode() : 0);
        result = 31 * result + (responseCodes != null ? responseCodes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimeTotalMaps{" +
                "time=" + time +
                ", IP='" + IP + '\'' +
                ", total=" + total +
                ", uniqueUrls=" + uniqueUrls +
                ", responseCodes=" + responseCodes +
                '}';
    }
}
