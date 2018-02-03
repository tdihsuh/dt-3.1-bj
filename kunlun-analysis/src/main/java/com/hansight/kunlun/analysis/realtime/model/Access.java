package com.hansight.kunlun.analysis.realtime.model;

import java.util.Date;

/**
 * Entity class to hold every single row in the access log in a generic way. No
 * support will be provided at the moment, this software is opensource for
 * reuse/modification/distribution.
 *
 * @author Senthil Balakrishnan
 */
public class Access {
    final static String REQUEST_FIELDS[] = new String[]{"c_ip", "sc_status", "@timestamp", "cs_uri_stem"};
    // Fields present in the original raw record
    public String id;
    public String date;
    public String time;
    public String c_ip;
    public String cookie_id;
    public String cs_username;
    public String s_site_name;
    public String s_computer_name;
    public String s_ip;
    public Integer s_port;
    public String cs_method;
    public String cs_uri_stem;
    public String cs_uri_query;
    public Integer sc_status;
    public Integer sc_substatus;
    public Integer sc_winstatus;
    public Integer sc_bytes;
    public Integer cs_bytes;
    public String cs_host;
    public String cs_useragent;
    public String cs_referer;
    public String cs_version;
    public String cs_cookie;
    public Integer time_taken;

    // Fields created from original raw record

    public Date datetime;

    private String requestedUrlPath;

    public void setRequestedUrlPath(String path) {
        requestedUrlPath = path;
    }

    public String getRequestedUrlPath() {
        if (requestedUrlPath == null && cs_uri_stem != null)
            requestedUrlPath = cs_uri_stem
                    + (cs_uri_query != null ? "?" + cs_uri_query : "");

        return requestedUrlPath;
    }

    private String pathOnly = null;

    public final String getPathOnly() {
        if (pathOnly != null)
            return pathOnly;

        if (requestedUrlPath == null)
            return null;

        pathOnly = requestedUrlPath;

        int posNameStart = pathOnly.indexOf('?');
        if (posNameStart >= 0)
            pathOnly = pathOnly.substring(0, posNameStart);

        int fragmentStart = pathOnly.indexOf('#');
        if (fragmentStart > 0)
            pathOnly = pathOnly.substring(0, fragmentStart);

        return pathOnly;
    }


    public String toURL() {
        return this.cs_host + "/" + this.cs_uri_stem;
    }

    @Override
    public String toString() {
        return "Access{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", c_ip='" + c_ip + '\'' +
                ", cookie_id='" + cookie_id + '\'' +
                ", cs_username='" + cs_username + '\'' +
                ", s_site_name='" + s_site_name + '\'' +
                ", s_computer_name='" + s_computer_name + '\'' +
                ", s_ip='" + s_ip + '\'' +
                ", s_port=" + s_port +
                ", cs_method='" + cs_method + '\'' +
                ", cs_uri_stem='" + cs_uri_stem + '\'' +
                ", cs_uri_query='" + cs_uri_query + '\'' +
                ", sc_status=" + sc_status +
                ", sc_substatus=" + sc_substatus +
                ", sc_winstatus=" + sc_winstatus +
                ", sc_bytes=" + sc_bytes +
                ", cs_bytes=" + cs_bytes +
                ", cs_host='" + cs_host + '\'' +
                ", cs_useragent='" + cs_useragent + '\'' +
                ", cs_referer='" + cs_referer + '\'' +
                ", cs_version='" + cs_version + '\'' +
                ", cs_cookie='" + cs_cookie + '\'' +
                ", time_taken=" + time_taken +
                ", datetime=" + datetime +
                ", requestedUrlPath='" + requestedUrlPath + '\'' +
                ", pathOnly='" + pathOnly + '\'' +
                '}';
    }

    public String getC_ip() {
        return c_ip;
    }

    public void setC_ip(String c_ip) {
        this.c_ip = c_ip;
    }

    public String getCookie_id() {
        return cookie_id;
    }

    public void setCookie_id(String cookie_id) {
        this.cookie_id = cookie_id;
    }

    public String getCs_username() {
        return cs_username;
    }

    public void setCs_username(String cs_username) {
        this.cs_username = cs_username;
    }

    public String getS_site_name() {
        return s_site_name;
    }

    public void setS_site_name(String s_site_name) {
        this.s_site_name = s_site_name;
    }

    public String getS_computer_name() {
        return s_computer_name;
    }

    public void setS_computer_name(String s_computer_name) {
        this.s_computer_name = s_computer_name;
    }

    public String getS_ip() {
        return s_ip;
    }

    public void setS_ip(String s_ip) {
        this.s_ip = s_ip;
    }

    public Integer getS_port() {
        return s_port;
    }

    public void setS_port(Integer s_port) {
        this.s_port = s_port;
    }

    public String getCs_method() {
        return cs_method;
    }

    public void setCs_method(String cs_method) {
        this.cs_method = cs_method;
    }

    public String getCs_uri_stem() {
        return cs_uri_stem;
    }

    public void setCs_uri_stem(String cs_uri_stem) {
        this.cs_uri_stem = cs_uri_stem;
    }

    public String getCs_uri_query() {
        return cs_uri_query;
    }

    public void setCs_uri_query(String cs_uri_query) {
        this.cs_uri_query = cs_uri_query;
    }

    public Integer getSc_status() {
        return sc_status;
    }

    public void setSc_status(Integer sc_status) {
        this.sc_status = sc_status;
    }

    public Integer getSc_substatus() {
        return sc_substatus;
    }

    public void setSc_substatus(Integer sc_substatus) {
        this.sc_substatus = sc_substatus;
    }

    public Integer getSc_winstatus() {
        return sc_winstatus;
    }

    public void setSc_winstatus(Integer sc_winstatus) {
        this.sc_winstatus = sc_winstatus;
    }

    public Integer getSc_bytes() {
        return sc_bytes;
    }

    public void setSc_bytes(Integer sc_bytes) {
        this.sc_bytes = sc_bytes;
    }

    public Integer getCs_bytes() {
        return cs_bytes;
    }

    public void setCs_bytes(Integer cs_bytes) {
        this.cs_bytes = cs_bytes;
    }

    public String getCs_host() {
        return cs_host;
    }

    public void setCs_host(String cs_host) {
        this.cs_host = cs_host;
    }

    public String getCs_useragent() {
        return cs_useragent;
    }

    public void setCs_useragent(String cs_useragent) {
        this.cs_useragent = cs_useragent;
    }

    public String getCs_referer() {
        return cs_referer;
    }

    public void setCs_referer(String cs_referer) {
        this.cs_referer = cs_referer;
    }

    public String getCs_version() {
        return cs_version;
    }

    public void setCs_version(String cs_version) {
        this.cs_version = cs_version;
    }

    public String getCs_cookie() {
        return cs_cookie;
    }

    public void setCs_cookie(String cs_cookie) {
        this.cs_cookie = cs_cookie;
    }

    public Integer getTime_taken() {
        return time_taken;
    }

    public void setTime_taken(Integer time_taken) {
        this.time_taken = time_taken;
    }
}