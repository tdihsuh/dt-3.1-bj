package com.hansight.kunlun.analysis.realtime.streaming

import java.util.Date
import java.util.regex.Pattern
import org.joda.time.DateTime
import com.hansight.kunlun.analysis.realtime.model.Anomaly
import com.hansight.kunlun.analysis.realtime.model.EnhanceAccess
import com.hansight.kunlun.analysis.realtime.model.RTConstants
import com.hansight.kunlun.analysis.utils.DatetimeUtils
import com.hansight.kunlun.analysis.utils.EventTypeUtils
import scala.collection.mutable.ListBuffer

object Rule extends Serializable {
	var p1 = Pattern.compile(""); 
	
	def dectect(access: EnhanceAccess) : Traversable[Anomaly] = {
		if (access.cs_uri_stem != null && access.cs_uri_stem.length() >0) {
	        if (access.cs_uri_stem.contains('\'')) {
	        	val list = ListBuffer[Anomaly]()
	        	val anomaly = storeAnomaly(access,  RTConstants.EVENT_TYPE_SQL_INJECTION) 
	            list += anomaly
	            return list
	        }
    	}
		return null;
	}
	
	
    def storeAnomaly(access: EnhanceAccess, eventType: String):Anomaly = {
        var anomaly = new Anomaly(access.c_ip, access.cookie_id, access.utcDatetime, DatetimeUtils.getUTCDateTime(new Date().getTime()));
        var map = scala.collection.mutable.Map[String, String]();
        map(access.s_ip) = access.s_computer_name;
        import scala.collection.JavaConversions
        anomaly.setServers(map.asInstanceOf);
        anomaly.setIndices(Array(access.getIndex()));
        anomaly.setEventType(eventType);
        anomaly.setUrl(access.getCs_uri_stem());
        anomaly.setCategory(EventTypeUtils.getCategory(eventType));
        if (eventType.equals(RTConstants.EVENT_TYPE_SQL_INJECTION)) {
        	anomaly.setUriQuery(access.cs_uri_query);
        }
        anomaly
    }
}