package com.hansight.kunlun.analysis.statistics.spark

import java.math.BigDecimal
import java.net.InetAddress
import java.util
import com.hansight.kunlun.analysis.realtime.model.RTConstants
import com.hansight.kunlun.analysis.realtime.single.StringEntropyCalculator
import com.hansight.kunlun.analysis.statistics.model.{TimeIPPair, TimeTotalMaps}
import com.hansight.kunlun.analysis.utils.quantile.QDigest
import com.hansight.kunlun.analysis.utils.{DatetimeUtils, EventTypeUtils}
import org.apache.hadoop.io.Writable
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.hadoop.cfg.ConfigurationOptions
import org.elasticsearch.hadoop.mr.LinkedMapWritable
import org.elasticsearch.spark._

import scala.collection.mutable
import scala.collection.JavaConverters._

/**
 * Author: zhhui 
 * Date: 2014/9/16
 */
object HistoryAnomalyDetection extends Serializable {
  var modelUrlRate: Double = .0
  var modelSc404Rate: Double = .0
  var modelSc500Rate: Double = .0

  def main(args: Array[String]) {
    if (args.length < 2) throw new IllegalArgumentException("args is less ,must be 2 args")
    val input: Array[String] = args(1).split("/")
    if (input.length < 2) {
      throw new IllegalArgumentException("args is less,must be ../..")
    }
    val master = args(0)
    val readIndex = input(0)
    val readType = input(1)
    val writeIndex = "anomaly"
    val writeType = "anomaly"
    compute(master, readIndex, readType, writeIndex, writeType)
  }


  def compute(master: String,
              readIndex: String,
              readType: String,
              writeIndex: String,
              writeType: String): Unit = {
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local[12]")
    conf.set(ConfigurationOptions.ES_NODES, master + ":9200").set("es.scroll.size", "5000")
    val sc = new SparkContext(conf)
    val modelData = sc.esRDD("model_final/anomaly")
    val model = modelData.first()._2

    val urlRate = model.get("urlRate")
    val sc404 = model.get("sc404")
    val sc500 = model.get("sc500")
    if (urlRate != None)
      HistoryAnomalyDetection.modelUrlRate = urlRate.get.asInstanceOf[Double]
    if (sc404 != None)
      HistoryAnomalyDetection.modelSc404Rate = sc404.get.asInstanceOf[Double]
    if (sc500 != None)
      HistoryAnomalyDetection.modelSc500Rate = sc500.get.asInstanceOf[Double]
    val logData = sc.esRDD(readIndex + "/" + readType).asInstanceOf[RDD[Map[String, Any]]]
    val logs = logData.filter(filterNone).map(mapToTuple).groupByKey().map(reduce)
    logs.saveToEs(writeIndex + "/" + writeType)
  }

  /**
   * Output Key: TimeIPPair, combination of [date + time + c_ip]<br/>
   * Output Value: LinkedMapWritable of [input]<br/>
   */
  def filterNone(value: Map[String, Any]): Boolean = {
    val ip = value.get("c_ip")
    val dt = value.get("datetime")
    val uri_query = value.get("cs_uri_query")
    if (ip == None || dt == None || uri_query == None)
      true
    false
  }

  def mapToTuple(kv: Map[String, Any]): (TimeIPPair, mutable.Map[String, Writable]) = {
    val value = kv.values.head.asInstanceOf[mutable.Map[String, Writable]]
    val ip = value.get("c_ip")
    val c_ip: String = if (ip == None) "" else ip.get.toString
    val dt = value.get("datetime")
    val datetime: String = if (dt == None) "0" else dt.get.toString
    val uri_query = value.get("cs_uri_query")
    val query: String = if (uri_query == None) "" else uri_query.get.toString
    val qvs = new LinkedMapWritable()
    // val entropy: Double = StringEntropyCalculator.calculate(query)
    query.split('&').foreach(pair => {
      val posEQ: Int = pair.indexOf('=')
      val (param, entropy) = if (posEQ >= 0) (pair.substring(0, posEQ), StringEntropyCalculator.calculate(pair.substring(posEQ + 1))) else ("", StringEntropyCalculator.calculate(pair))
      qvs.put(param, entropy)
    })
    value += (("cs_uri_query", qvs))
    val timestamp: Long = DatetimeUtils.getTimestamp(datetime)
    val ipaddr: InetAddress = InetAddress.getByName(c_ip)
    var clientIP: Long = 0
    for (b <- ipaddr.getAddress) clientIP = clientIP << 8 | (b & 0xFF)
    (new TimeIPPair(timestamp, clientIP), value)
  }

  /**
   * <pre>
   * Input &lt;K,Iterator&lt;V&gt;&gt; pair as following:<br/>
   * &lt;TimeIPPair, Iterator&lt;"cs-uri-stem:x,cs-uri-query:x,cs_referer:x,cs-host:x,sc-status:x"&gt;&gt;
   */
  def reduce(kvs: (TimeIPPair, Iterable[mutable.Map[String, Writable]])): TraversableOnce[Map[String, Any]] = {
    val key2: TimeIPPair = new TimeIPPair(kvs._1)
    val timeTotalMaps: TimeTotalMaps = new TimeTotalMaps(key2.getTime)
    val servers: util.Set[String] = new util.HashSet[String]
    kvs._2.foreach(log => {
      val serverValue = log.get("s_computer_name")
      if (serverValue != None)
        servers.add(serverValue.get.toString)
      val uriStemValue = log.get("cs_uri_stem")
      val hostValue = log.get("cs_host")
      if (uriStemValue != None) {
        val k: String = hostValue.get.toString + "/" + uriStemValue.get.toString
        val value:Integer = timeTotalMaps.getUniqueUrls.asScala.get(k).getOrElse(0)
        timeTotalMaps.getUniqueUrls.put(k, value+ 1)
      }
      val statusValue = log.get("sc_status")
      if (statusValue != None) {
        val k: Integer = statusValue.get.asInstanceOf
        val value:Integer = timeTotalMaps.getResponseCodes.asScala.get(k).getOrElse(0)
        timeTotalMaps.getResponseCodes.put(k, value+ 1)
      }
      val qvs = log.get("cs_uri_query")
      if (qvs != None) {
      }
      timeTotalMaps.inc()

    })
    val endDate = kvs._2.last.get("time")

    val urlRate = if (timeTotalMaps.getUniqueUrls.size > 0) {
      val digest: QDigest = new QDigest(timeTotalMaps.getUniqueUrls.size + 1)
      import scala.collection.JavaConversions._
      timeTotalMaps.getUniqueUrls.values.foreach(num => digest.offer(num.longValue))
      new BigDecimal(digest.getQuantile(0.1).toDouble / timeTotalMaps.getTotal.toDouble).setScale(5, BigDecimal.ROUND_CEILING).doubleValue
    } else {
      0.0
    }
    val smaps = timeTotalMaps.getResponseCodes.asScala
    val sc404=smaps.get(404)
    val sc404Rate: Double = if (sc404 != None) new BigDecimal(sc404.get.toDouble / timeTotalMaps.getTotal).setScale(5, BigDecimal.ROUND_CEILING).doubleValue else 0.0
    val sc500=smaps.get(500)
    val sc500Rate: Double = if (sc500!= None) new BigDecimal(sc500.get.toDouble / timeTotalMaps.getTotal).setScale(5, BigDecimal.ROUND_CEILING).doubleValue else 0.0
    var logs: List[Map[String, Any]] = Nil
    val map = Map("c_ip" -> key2.getIp,
      "endDatetime" -> endDate.toString,
      "startDatetime" -> key2.getTime,
      "servers" -> servers.toArray(new Array[String](servers.size)))
    if (urlRate > HistoryAnomalyDetection.modelUrlRate) {

      logs = map +(("category", EventTypeUtils.getCategory(RTConstants.EVENT_TYPE_HTTP_UNIQUE_URL)),
        ("eventType", RTConstants.EVENT_TYPE_HTTP_UNIQUE_URL),
        ("degree", urlRate),
        ("modelDegree", HistoryAnomalyDetection.modelUrlRate)) :: logs
    }
    if (sc404Rate > HistoryAnomalyDetection.modelSc404Rate) {
      logs = map +(("category", EventTypeUtils.getCategory(RTConstants.EVENT_TYPE_HTTP404)),
        ("eventType", RTConstants.EVENT_TYPE_HTTP404),
        ("degree", sc404Rate),
        ("modelDegree", HistoryAnomalyDetection.modelUrlRate)) :: logs
    }
    if (sc500Rate > HistoryAnomalyDetection.modelSc500Rate) {
      logs = map +(("category", EventTypeUtils.getCategory(RTConstants.EVENT_TYPE_HTTP500)),
        ("eventType", RTConstants.EVENT_TYPE_HTTP500),
        ("degree", sc500Rate),
        ("modelDegree", HistoryAnomalyDetection.modelUrlRate)) :: logs
    }
    logs
  }
}
