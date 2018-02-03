package com.hansight.kunlun.analysis.realtime.model;


import org.elasticsearch.search.SearchHit;

import java.util.Map;

public class EnhanceAccess extends Access {

    public String index = null;

    public String utcDatetime = null;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getUtcDatetime() {
        return utcDatetime;
    }

    public void setUtcDatetime(String utcDatetime) {
        this.utcDatetime = utcDatetime;
    }

    private static boolean validate(Map<String, Object> fields) {
        boolean result = true;
        for (String field : REQUEST_FIELDS) {
            if (fields.containsKey(field)) {
                continue;
            }
            result = false;
            break;
        }

        return result;
    }

    public static EnhanceAccess getInstance(SearchHit hit) {
        Map<String, Object> source = hit.getSource();
        if (!validate(source)) {
            return null;
        }
        EnhanceAccess access = new EnhanceAccess();
        // BeanUtils.copyProperties(access, source);
        Object obj = source
                .get("@timestamp");
        access.utcDatetime = obj == null ? "" : obj.toString();
        obj = source.get("cs_useragent");
        access.cs_useragent = obj == null ? "" : obj.toString();
        access.id = hit.getId();
        access.index = hit.getIndex();
        obj = hit
                .getSource().get("s_computer_name");
        access.s_computer_name = obj != null ? obj.toString() : "";
        obj = source.get(
                "c_ip");
        access.c_ip = obj == null ? "" : obj.toString();
        obj = source.get("sc_status");
        access.sc_status = Integer.parseInt(obj == null ? "" : obj.toString());
        obj = source.get("@timestamp");
        access.utcDatetime = obj == null ? "" : obj.toString();
        obj = source.get("cs_uri_stem");
        access.cs_uri_stem = obj == null ? "" : obj.toString();
        obj = source.get("cs_uri_query");
        access.cs_uri_query = obj == null ? "" : obj.toString();
        obj = source.get("cs_host");
        access.cs_host = obj == null ? "" : obj.toString();
        obj = source.get("s_ip");
        access.s_ip = obj == null ? "" : obj.toString();
        obj = source.get("cs_cookie");
        access.cs_cookie = obj == null ? "" : obj.toString();
        obj = source.get("cookie_id");
        access.cookie_id = obj == null ? "" : obj.toString();
        obj = source.get("cs_useragent");
        access.cs_useragent = obj == null ? "" : obj.toString();
        access.id = hit.getId();
        access.index = hit.getIndex();
        return access;
    }
}
