package com.hansight.kunlun.analysis.utils;

import com.hansight.kunlun.analysis.realtime.model.RTConstants;

public class EventTypeUtils {
    public static String getCategory(String eventType) {
        if (eventType.equals(RTConstants.EVENT_TYPE_HTTP404)) {
            return RTConstants.EVENT_CATEGORY_MISCONFIG;
        } else if (eventType.equals(RTConstants.EVENT_TYPE_HTTP500)) {
            return RTConstants.EVENT_CATEGORY_MISCONFIG;
        } else if (eventType.equals(RTConstants.EVENT_TYPE_HTTP_UNIQUE_URL)) {
            return RTConstants.EVENT_CATEGORY_DDOS;
        } else if (eventType.equals(RTConstants.EVENT_TYPE_SQL_INJECTION)) {
            return RTConstants.EVENT_CATEGORY_SQL_INJECTION;
        }

        return RTConstants.EVENT_CATEGORY_UNKNOWN;
    }
}
