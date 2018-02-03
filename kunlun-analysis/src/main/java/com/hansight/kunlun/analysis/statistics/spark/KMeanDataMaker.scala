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
import scala.util.hashing.MurmurHash3

/**
 * Author: zhhui 
 * Date: 2014/9/17
 */
object KMeanDataMaker extends Serializable {
  protected final val logger: Logger = LoggerFactory.getLogger(classOf[KMeanDataMaker])

  def toIPURLTuple(value: mutable.LinkedHashMap[String, Any]): (String, Int) = {
    val wip = value.get(C_IP)
    val IPAddress= wip.getOrElse("127.0.0.1").asInstanceOf[mutable.Buffer[Any]](0).toString

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
    val ref = value.get(URI_STEM)
    if (ref != None) {
      url.append(ref.get.asInstanceOf[mutable.Buffer[Any]](0).toString)
    }
    val url_f = url.toString()
    var url_id = UNIQUEURLS.getOrElse(url_f, 0)
    if (url_id == 0) {
      url_id = UNIQUEURLS.size + 1
      UNIQUEURLS.put(url_f, url_id)
    }

    (IPAddress, url_id)
  }


  class KMeanDataMaker {

  }

}

