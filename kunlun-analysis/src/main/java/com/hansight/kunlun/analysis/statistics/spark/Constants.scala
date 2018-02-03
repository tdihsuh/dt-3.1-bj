package com.hansight.kunlun.analysis.statistics.spark

import scala.collection.mutable

/**
 * Created by zhhui on 2014/10/31.
 */
object Constants {
  final val C_IP = "c_ip"
  final val S_IP = "s_ip"
  final val TIMESTAMP = "@timestamp"
  final val COUNT = "count"
  final val S_PORT = "s_port"
  final val URL = "url"
  final val MALICIOUS = "malicious"
  final val CS_HOST = "cs_host"
  final val URI_STEM = "cs_uri_stem"
  final val URI_QUERY = "cs_uri_query"
  final val SC_STATUS = "sc_status"
  final val USER_AGENT = "cs_useragent"
  final val COOKIE_ID = "cookie_id"
  final val URL_RATE = "urlRate"
  final val SESSION = "session"
  final val SC404 = "sc404"
  final val SC500 = "sc500"
  final val TOTAL = "total"
  final val PARAM = "param"
  final val UPPER = "upper"
  final var UNIQUEURLS:mutable.Map[String, Int] = mutable.Map.empty
}
