package com.hansight.kunlun.analysis.statistics.model;

import org.apache.hadoop.io.Text;

/**
 * Created by zhhui on 2014/5/21.
 */
public interface AnomalyConstants {
    public static final Text IP = new Text("c_ip");
    public static final Text referer = new Text("cs_referer");
    public static final Text dtKey = new Text("datetime");
    public static final Text status = new Text("sc_status");
    public static final Text uriQuery = new Text("cs_uri_query");
    public static final Text url = new Text("url");
    public static final Text uriStem = new Text("cs_uri_stem");
    public static final Text time = new Text("time");
    public static final Text urlRate = new Text("urlRate");
    public static final Text sc404 = new Text("sc404");
    public static final Text sc500 = new Text("sc500");
    public static final Text host = new Text("cs_host");
    public static final Text port = new Text("s_port");
    public static final Text computername = new Text("s_computer_name");
    public static final Text cookie = new Text("cs_cookie");
    public static final Text cookieId = new Text("cookie_id");
    public static final Text userAgent = new Text("cs_useragent");
}
