package com.hansight.kunlun.analysis.realtime.model;

public class RTConstants {
    public static final String Manual = "manual";
	public static Integer COUNTER_KEY_STATUS_404 = 404;
    public static Integer COUNTER_KEY_STATUS_500 = 500;

    public static String MODEL= "model";
    public static String TYPE_FINAL= "final";
    public static String TYPE_QS= "query_string";
    public static String TYPE_404 = "status_404";
    public static String MODEL_FIELD_HTTP404_KEY = "sc404";
    public static String MODEL_QS_FIELD_UPPER = "upper";
    public static String MODEL_404_URL = "url";
    public static String MODEL_404_MALICIOUS = "malicious";


    public static String MODEL_FIELD_HTTP500_KEY = "sc500";
    public static String MODEL_FIELD_HTTP_UNIQUE_URL_KEY = "urlRate";
    public static String MODEL_FIELD_DATE_KEY = "@timestamp";

    public static String EVENT_TYPE_HTTP404 = "sc404";
    public static String EVENT_TYPE_HTTP500 = "sc500";
    public static String EVENT_TYPE_HTTP_UNIQUE_URL = "urlRate";
    public static String EVENT_TYPE_SQL_INJECTION = "sqlInject";

    public static String EVENT_CATEGORY_MISCONFIG = "MisConfiguration";
    public static String EVENT_CATEGORY_DDOS = "DDoS";
    public static String EVENT_CATEGORY_SQL_INJECTION = "SQL Injection";
    public static String EVENT_CATEGORY_UNKNOWN = "UNKNOWN";

    public static String ANOMALY_STAGE_INDEX = "anomaly_stage";
    public static String ANOMALY_INDEX = "anomaly";
    public static String ANOMALY_ANOMALY_TYPE = "anomaly";
    public static String ANOMALY_SUMMARY_TYPE = "summary";
    public static String ANOMALY_FIELD_SESSION_ID = "_id";
    public static String ANOMALY_FIELD_MSG = "msg";

    public static String RAW_LOG_INDEX_NAME_PREFIX = "logs_";
    public static String RAW_LOG_INDEX_NAME_SUFFIX_DATE_PATTERN = "yyyyMMdd";
}
