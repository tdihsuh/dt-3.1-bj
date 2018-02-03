package com.hansight.kunlun.analysis.realtime.streaming

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.slf4j.LoggerFactory
import com.hansight.kunlun.analysis.realtime.conf.Constants
import com.hansight.kunlun.analysis.realtime.model.EnhanceAccess
import com.hansight.kunlun.analysis.realtime.model.RTConstants
import com.hansight.kunlun.analysis.realtime.single.RTHandler
import com.hansight.kunlun.analysis.utils.DatetimeUtils
import com.hansight.kunlun.utils.EsUtils
import java.util.Calendar
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import java.util.TimeZone
import java.util.Date
import org.elasticsearch.client.Client

class ESReceiver(esType: String, startTime:Long)
  extends Receiver[EnhanceAccess](StorageLevel.MEMORY_AND_DISK_2) {
  val LOG = LoggerFactory.getLogger(classOf[ESReceiver]);
  var running = true
  val format = new SimpleDateFormat(
		  RTConstants.RAW_LOG_INDEX_NAME_SUFFIX_DATE_PATTERN);
  def onStart() {
    // Start the thread that receives data over a connection
    new Thread("ES Receiver") {
      override def run() { receive() }
    }.start()
  }

  def onStop() {
   // There is nothing much to do as the thread calling receive()
   // is designed to stop by itself isStopped() returns false
    running = false
  }

  	/** Create a socket connection and receive data until receiver is stopped */
	private def receive() {
	   val client = EsUtils.getEsClient()
	   	// init
		val MAX_STEP_IN_MILLIS = RTHandler.getInt(Constants.RT_FETCH_RAW_LOG_STEP,
			10 * 60 * 1000);
		val DELAY_TIMES = RTHandler.getLong(Constants.RT_FETCH_RAW_LOG_DELAY,
			10 * 1000);
 
		val sleep: Long = RTHandler.getInt(Constants.RT_FETCH_RAW_LOG_SLEEP, 3);
    val size: Int = RTHandler.getInt(Constants.RT_FETCH_RAW_LOG_SIZE, 500);
    val time: Int = RTHandler.getInt(Constants.RT_FETCH_RAW_LOG_TIME, 10);
		var current: Long = 0;
		var past = false;
		var prevTime = startTime;
		var endTime: Long = 0;
		while (RTHandler.RUNNING) {
			try {
				endTime = prevTime + MAX_STEP_IN_MILLIS;
				current = System.currentTimeMillis();
				current -= DELAY_TIMES;
				if (endTime > current) {
					endTime = current;
					past = false;
				} else {
					past = true;
				}
        val prev = DatetimeUtils.getUTCDateTime(prevTime);
        val to = DatetimeUtils.getUTCDateTime(endTime);
        val indices = getIndices(client, prevTime, endTime);
				if (indices.length > 0) {
					var total = 0;
					var response = search(client, size, prev, to, indices, time);
					var flag = true;
					while (flag) {
						response = client
								.prepareSearchScroll(response.getScrollId())
								.setScroll(TimeValue.timeValueMinutes(time))
								.execute().actionGet();
						var hits = response.getHits().hits();
						if (hits.length == 0) {
              // clean scroll id
              val ids = new ArrayList[String](1);
							ids.add(response.getScrollId());
							client.prepareClearScroll().setScrollIds(ids)
									.execute().actionGet();
							flag = false;
						}
						for (hit <- hits) {
              val access = EnhanceAccess.getInstance(hit);
							if (access != null) {
								store(access)
							}
						}
					}
				}
				prevTime = endTime;
			} catch {
			  case e: Exception => LOG.error("fetch raw error", e)
			}
			try {
				if (sleep > 0 && past) {
					TimeUnit.SECONDS.sleep(sleep);
				}
			} catch  {
			  case e: InterruptedException =>
			}
		}
		System.out.println("Stopped receiving")
	 	restart("Trying to connect again")
	}
	def search(client:Client, size: Int, from: String, to: String,
			 indices: Array[String], time:Int):SearchResponse = {
    // index
    val qb: QueryBuilder = QueryBuilders.rangeQuery("@timestamp").from(from).to(to).includeLower(true).includeUpper(false);
		import scala.collection.convert.WrapAsJava
		return client.prepareSearch(indices:_*).setTypes(esType).setQuery(qb)
				.setSearchType(SearchType.SCAN)
				.setScroll(TimeValue.timeValueMinutes(time)).setSize(size)
				.execute().actionGet();
	}
	
	def getIndices(client:Client, prev: Long, now: Long): Array[String] = {

    val calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		calendar.setTime(new Date(prev));
    val preDate = format.format(calendar.getTime());
		calendar.setTime(new Date(now));
    val nowDate = format.format(calendar.getTime());
		//
		var index = RTConstants.RAW_LOG_INDEX_NAME_PREFIX + preDate;
		var request = new IndicesExistsRequest(index);
		var res = client.admin().indices().exists(request)
				.actionGet();
    val result: List[String] = List[String]();
		if (res.isExists()) {
			result + (index);
		} else {
			LOG.warn("Index [{}] doesn't exist", index);
		}
		if (!preDate.equals(nowDate)) {
			index = RTConstants.RAW_LOG_INDEX_NAME_PREFIX + nowDate;
			request = new IndicesExistsRequest(index);
			res = client.admin().indices().exists(request).actionGet();
			if (res.isExists()) {
				result + (index);
			} else {
				LOG.warn("Index [{}] doesn't exist", index);
			}
		}
		return result.toArray;
	}

}
