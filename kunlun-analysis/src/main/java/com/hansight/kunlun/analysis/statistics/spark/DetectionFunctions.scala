package com.hansight.kunlun.analysis.statistics.spark

import java.math.BigDecimal
import java.net.InetAddress
import java.util
import java.util.UUID

import com.hansight.kunlun.analysis.statistics.model.{LogWritable, TimeTotalMaps}
import com.hansight.kunlun.analysis.statistics.spark.Constants._
import com.hansight.kunlun.analysis.utils.DatetimeUtils
import com.hansight.kunlun.analysis.utils.quantile.QDigest
import com.hansight.kunlun.analysis.utils.v2.EditDistanceUrlCluster
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Author: zhhui 
 * Date: 2014/9/17
 */
object DetectionFunctions extends Serializable {
  protected final val logger: Logger = LoggerFactory.getLogger(classOf[DetectionFunctions])

  def computerRate(total: Double)(quantile: Double): Double = {
    new BigDecimal(quantile / total).setScale(5, BigDecimal.ROUND_CEILING).doubleValue
  }

  /**
   * iis log with map to our LogWritable  Ip as key with long type
   * @param value    Map[String, Any]
   * @return
   */
  def mapToLogIpAsKey(value: mutable.LinkedHashMap[String,Any]): (Long, LogWritable) = {
    logger.info("mapToLogIpAsKey start")
    val wip = value.get(C_IP)
    val netAdd: InetAddress = InetAddress.getByName(wip.getOrElse("127.0.0.1").asInstanceOf[ mutable.Buffer[Any]](0).toString)
    var clientIP = 0l
    for (b <- netAdd.getAddress) {
      clientIP = clientIP << 8 | (b & 0xFF)
    }
    val log: LogWritable = new LogWritable
    log.setCip(clientIP)
    val time = value.get(TIMESTAMP)
    if (time != None) {
      log.setTime(DatetimeUtils.getTimestamp(time.get.asInstanceOf[mutable.Buffer[Any]](0).toString))
    }
    val statusValue = value.get(SC_STATUS)
    if (statusValue != None) {
      log.setStatus(statusValue.get.asInstanceOf[mutable.Buffer[Any]](0).toString.toInt)
    }
    val url = new StringBuilder
    val port = value.get(S_PORT)
    var p = 80
    if (port != None) {

      p = port.get.asInstanceOf[mutable.Buffer[Any]](0).toString.toInt
      if (p == 443) url.append("https://")
      else url.append("http://")
    }
    val host = value.get(CS_HOST)
    if (host != None) {
      url.append(host.get.asInstanceOf[mutable.Buffer[Any]](0).toString)
    }
    if (p != 80) {
      url.append(p)
    }
    var ref = value.get(URI_STEM)
    if (ref != None) {
      url.append(ref.get.asInstanceOf[mutable.Buffer[Any]](0).toString)
    }
    log.setUrl(url.toString())
    ref = value.get(USER_AGENT)
    if (ref != None) {
      log.setUserAgent(ref.get.asInstanceOf[mutable.Buffer[Any]](0).toString)
    }
    ref = value.get(COOKIE_ID)
    if (ref != None) {
      log.setCookieId(ref.get.asInstanceOf[mutable.Buffer[Any]](0).toString.hashCode)
    }
    logger.info("mapToLogIpAsKey end")
    (clientIP, log)
  }

  def urlStatusAsKey(value: mutable.LinkedHashMap[String, Any]): ((String, Int), Long) = {
    val ref: String = value.getOrElse(URI_STEM, "").asInstanceOf[mutable.Buffer[Any]](0).toString
    val status = value.getOrElse(SC_STATUS, "0").asInstanceOf[mutable.Buffer[Any]](0).toString.toInt
    ((ref, status), 1)
  }

  def keyTransfer(tuple: (String, Int)): (Int, (String, Int)) = {
    (0, tuple)
  }

  def compute404Model(tuple: (Int, Iterable[(String, Int)])): TraversableOnce[(Map[String, Any])] = {
    val cluster: EditDistanceUrlCluster = new EditDistanceUrlCluster
    val urls200: util.List[String] = new util.ArrayList[String]
    val urls404: util.List[String] = new util.ArrayList[String]
    tuple._2.foreach(line => {
      val (cs_uri_stem, sc_status) = line

      if (sc_status == 200) {
        if (!urls200.contains(cs_uri_stem)) urls200.add(cs_uri_stem)
      }
      else if (sc_status == 404) {
        if (!urls404.contains(cs_uri_stem)) urls404.add(cs_uri_stem)
      }
    }
    )
    cluster.cluster(urls200)
    import scala.collection.JavaConversions._
    var json: List[Map[String, Any]] = Nil
    for (url <- urls404) {
      json = Map(URL -> url, MALICIOUS -> cluster.isOutlier(url)) :: json
    }
    json
  }

  /**
   * computer one log count ,total 404 500 ed
   * @param tuple  (Long, Iterable[LogWritable])
   * @return
   */
  def modelingStatic(tuple: (Long, Iterable[LogWritable])): TraversableOnce[(String, TimeTotalMaps)] = {

    var logs: List[(String, TimeTotalMaps)] = Nil
    var roughs: ArrayBuffer[LogWritable] = new ArrayBuffer[LogWritable]()
    logger.info("logs.size:{} ", tuple._2.size)
    logger.info("modelingStatic start")
    for (l <- tuple._2) {
      val log = new LogWritable(l)
      if (log.getCookieId != 0) {
        var noRoughLogs = true
        var i = 0
        while (i < roughs.size) {
          val p: LogWritable = roughs(i)
          if (p != null) {
            if (LogWritable.equal(p.getUserAgent, log.getUserAgent)) {
              p.setCookieId(log.getCookieId)
              roughs.remove(i)
            } else {
              noRoughLogs = false
            }
          }
          i = i + 1
        }
        if (noRoughLogs) {
          roughs = roughs.dropWhile(_ => true)
        }

      } else {
        roughs += log
      }
      val timeTotalMaps: TimeTotalMaps = new TimeTotalMaps
      timeTotalMaps.setTime(log.getTime)
      timeTotalMaps.setIP(log.getCip + "")
      if (log.getUrl != null) {
        timeTotalMaps.getUniqueUrls.put(log.getUrl, 1)
      }
      if (log.getStatus != 0) {
        timeTotalMaps.getResponseCodes.put(log.getStatus, 1)
      }
      timeTotalMaps.inc()
      logs = ("%s:%d".format(tuple._1, log.getCookieId), timeTotalMaps) :: logs
    }
    roughs = roughs.dropWhile(_ => true)
    logger.info("modelingStatic end")
    logs
  }

  /**
   * one cookies one result this.is computer session count
   * @param tuple (String,TimeTotalMaps)
   * @return
   */
  def modelingMiddle(tuple: (String, TimeTotalMaps)): (Long, mutable.Map[String, Any]) = {
    logger.debug("modelingMiddle start")
    val maps = tuple._2
    val computer = computerRate(maps.getTotal.toDouble)(_)
    val urlRate: Double = if (maps.getUniqueUrls.size > 0) {
      val digest: QDigest = new QDigest(maps.getUniqueUrls.size + 1)
      import scala.collection.JavaConversions._
      maps.getUniqueUrls.values().foreach(
        num => {
          digest.offer(num.longValue)
        })
      computer(digest.getQuantile(0.1).toDouble)
    } else 0.0
    import scala.collection.JavaConverters._
    val smaps = maps.getResponseCodes.asScala
    val sc404 = smaps.get(404)
    val sc404Rate: Double = if (sc404 != None) computer(sc404.get.toDouble) else 0.0
    val sc500 = smaps.get(500)
    val sc500Rate: Double = if (sc500 != None) computer(sc500.get.toDouble) else 0.0
    val json = mutable.Map[String, Any]((SESSION, UUID.randomUUID.toString.replace("-", ""))
      , (C_IP, maps.getIP)
      , (TIMESTAMP, maps.getTime)
      , (TOTAL, maps.getTotal))
    if (urlRate > 0.0) json += ((URL_RATE, urlRate))
    if (sc404Rate > 0.0) json += ((SC404, sc404Rate))
    if (sc500Rate > 0.0) json += ((SC500, sc500Rate))
    logger.debug("modelingMiddle end")
    (0, json)
  }

  /**
   * This will definitely cause OOM, need to change to QDigest;
   * But firstly need to get the total count (in setup?).
   */
  def modelingFinal(kv: (Long, Iterable[mutable.Map[String, Any]])): mutable.Map[String, Any] = {
    logger.info("modelingFinal start")
    val urlDigest: QDigest = new QDigest(Long.MaxValue)
    val sc404Digest: QDigest = new QDigest(Long.MaxValue)
    val sc500Digest: QDigest = new QDigest(Long.MaxValue)
    var totalNumber: Integer = 0
    var urlFlag: Boolean = false
    var flag404: Boolean = false
    var flag500: Boolean = false
    val maps = kv._2
    maps.foreach(log => {
      totalNumber += 1

      var vl = log.get(URL_RATE)
      if (vl != None) {
        urlFlag = true
        urlDigest.offer((vl.get.asInstanceOf[Double] * 100000).asInstanceOf[Long])
      }
      vl = log.get(SC404)
      if (vl != None) {
        flag404 = true
        sc404Digest.offer((vl.get.asInstanceOf[Double] * 100000).asInstanceOf[Long])
      }
      vl = log.get(SC500)
      if (vl != None) {
        flag500 = true
        sc500Digest.offer((vl.get.asInstanceOf[Double] * 100000).asInstanceOf[Long])
      }
    })
    val urlRate: Double = if (urlFlag) urlDigest.getQuantile(0.5).toDouble / 100000 else .0
    val sc404Rate = if (flag404) sc404Digest.getQuantile(0.5).toDouble / 100000 else .0
    val sc500Rate = if (flag500) sc500Digest.getQuantile(0.5).toDouble / 100000 else .0
    val json = new mutable.HashMap[String, Any]()
    json.put(TIMESTAMP, kv._1)
    json.put(TOTAL, totalNumber)
    if (urlRate > 0.0) json.put(URL_RATE, urlRate)
    if (sc404Rate > 0.0) json.put(SC404, sc404Rate)
    if (sc500Rate > 0.0) json.put(SC500, sc500Rate)
    logger.info("modelingFinal end")
    json
  }


}

class DetectionFunctions {

}