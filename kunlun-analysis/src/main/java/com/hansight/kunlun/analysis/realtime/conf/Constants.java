package com.hansight.kunlun.analysis.realtime.conf;

public class Constants {
	public static final String SESSION_INTERVAL_MS = "session.interval.ms";
    public static final long SESSION_INTERVAL_MS_DEFAULT = 1800000L;
	public static final String MESSAGE_QUEUE_CAPACITY = "message.queue.capacity";
    public static final int MESSAGE_QUEUE_CAPACITY_DEFAULT = 100000;
	public static final String RT_FETCH_RAW_LOG_STEP = "rt.fetch.raw.log.step";
	public static final String RT_FETCH_RAW_LOG_SIZE = "rt.fetch.raw.log.size";
	public static final String RT_FETCH_RAW_LOG_TIME = "rt.fetch.raw.log.time";
	public static final String RT_FETCH_RAW_LOG_SLEEP = "rt.fetch.raw.log.sleep";
	public static final String RT_SESSION_PROCESSOR_EXPIRE_QUEUE_CAPACITY = "rt.session.processor.expire.queue.capacity";
	public static final String RT_SESSION_RUN_SLEEP_TIME = "rt.session.processor.run.sleep.time";
	public static final String RT_FETCH_RAW_LOG_DELAY = "rt.fetch.raw.log.delay";
	//Add ip->geo database config:
	public static final String IP_GEO_DATABASE = "ip.geo.database";
	
	public static final String ES_MODEL_INDEX_NAME = "es.model.index.name";
	public static final String ES_MODEL_FIELD_HTTP500_NAME = "es.model.field.http500.name";
	public static final String ES_MODEL_FIELD_HTTP404_NAME = "es.model.field.http404.name";
	public static final String ES_MODEL_FIELD_HTTP_UNIQUE_URL_NAME = "es.model.field.http.unique.url.name";
	public static final String ES_MODEL_FIELD_DATE_NAME = "es.model.field.date.name";
	
	public static final String EVENT_TYPE_MISCONFIG_NAME = "event.type.misconfig";
	public static final String EVENT_TYPE_DDOS_NAME = "event.type.ddos.name";
	public static final String EVENT_TYPE_SQL_INJECTION_NAME = "event.type.sql.injection.name";
	
	public static final String ES_ANOMALY_STAGE_INDEX_NAME = "es.anomaly.stage.index.name";
	public static final String ES_ANOMALY_INDEX_NAME = "es.anomaly.index.name";
	public static final String ES_ANOMALY_ANOMALY_TYPE = "es.anomaly.anomaly.type.name";
	public static final String ES_ANOMALY_SUMMARY_TYPE_NAME = "es.anomaly.summary.type.name";
	public static final String ES_ANOMALY_FIELD_SESSION_ID_NAME = "es.anomaly.field.session.id.name";
	public static final String ES_ANOMALY_FIELD_MSG_NAME = "es.anomaly.field.msg.name";
	
	public static final String ES_RAW_LOG_INDEX_NAME_PREFIX = "es.raw.log.index.name.prefix";
	public static final String ES_RAW_LOG_INDEX_NAME_SUFFIX_DATE_PATTERN = "es.raw.log.index.name.suffix.date.pattern";
	public static final String MODEL_404_NOT_EXISTS_AS_ANOMALY = "model.404.not.exists.as.anomaly";
	public static final String RT_STORE_BATCH_MAX_SIZE = "rt.store.batch.max.size";
	public static final String RT_STORE_SLEEP_TIME = "rt.store.sleep.time";
	public static final String RT_STORE_BATCH_MAX_TIME = "rt.store.batch.max.time";
	public static final String RT_FETCH_REFERENCE_REGEX = "fetch.reference.regex";
}
