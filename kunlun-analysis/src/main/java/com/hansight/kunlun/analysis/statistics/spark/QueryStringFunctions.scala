package com.hansight.kunlun.analysis.statistics.spark

import java.lang
import java.util.Comparator

import com.hansight.kunlun.analysis.realtime.single.StringEntropyCalculator
import com.hansight.kunlun.analysis.statistics.spark.Constants._
import com.hansight.kunlun.analysis.utils.{IPFilter, QuartileUtils}
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.hadoop.cfg.ConfigurationOptions
import org.elasticsearch.spark._

import scala.collection.immutable.TreeMap
import scala.collection.mutable
import scala.util.hashing.MurmurHash3

/**
 * Author: zhhui 
 * Date: 2014/9/16
 */
object QueryStringFunctions extends Serializable {

  def main(args: Array[String]) {
    if (args.length < 2) throw new IllegalArgumentException("args is less ,must be 2 args")
    val input: Array[String] = args(1).split("/")
    if (input.length < 2) {
      throw new IllegalArgumentException("args is less,must be ../..")
    }
    val master = args(0)
    val readIndex = input(0)
    val readType = input(1)
    val writeIndex = "model_qs"
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
    val logData = sc.esRDD(readIndex + "/" + readType).asInstanceOf[RDD[(String, mutable.LinkedHashMap[String,Any])]].values
    val logs = logData.flatMap(calculateEntropy).groupByKey().map(modeling)
    logs.saveToEs(writeIndex + "/" + writeType)
  }

  def calculateEntropy(value: mutable.LinkedHashMap[String,Any]): TraversableOnce[(Int, Double)] = {
    //val value = tuple._2.asInstanceOf[mutable.Map[String, Writable]]
    val wip = value.get(C_IP)

    val c_ip= wip.getOrElse("172.0.0.1").asInstanceOf[mutable.Buffer[Any]](0).toString

    val q = value.get(URI_QUERY)
    //过滤内网IP
    var logs: List[(Int, Double)] = Nil
    if (!(IPFilter.isInnerIP(c_ip) || q == None) && !(value.get(SC_STATUS) == None)) {
      val cs_host = value.get(CS_HOST)
      var csHost: String = ""
      if (cs_host != None) {
        csHost = cs_host.get.asInstanceOf[mutable.Buffer[Any]](0).toString
      }
      val uri_stem = value.get(URI_STEM)
      var stem: String = ""
      if (uri_stem != None) {
        stem = uri_stem.get.asInstanceOf[mutable.Buffer[Any]](0).toString
      }
      val query = q.get.asInstanceOf[mutable.Buffer[Any]](0).toString
      // Split into param-value pairs
      //  double entropy = StringEntropyCalculator.calculate(query);
      val params: Array[String] = query.split("&")
      var paramValue: String = ""
      for (param <- params) {
        val nv: Array[String] = param.split("=")
        if (nv.length == 2) {
          paramValue = nv(1)
        }
        else {
          paramValue = nv(0)
          nv(0) = ""
        }
        val entropy = StringEntropyCalculator.calculate(paramValue)
        logs = (MurmurHash3.stringHash(stem + "?" + nv(0)), entropy) :: logs
      }
    }
    logs
  }

  def modeling(kv: (Int, Iterable[Double])): mutable.Map[String, Any] = {
    val json = new mutable.HashMap[String, Any]()
    val data: Map[lang.Double, Integer] = new TreeMap[lang.Double, Integer] {
      new Comparator[lang.Double] {
        def compare(obj: lang.Double, that: lang.Double): Int = {
          obj.compareTo(that)
        }
      }
    }

    var len: Int = 0
    for (value <- kv._2) {
      len += 1
      val k = value.get
      val v: Integer = data.getOrElse(k, 1)
      data + ((k, v))
    }
    import scala.collection.JavaConverters._
    val upperFence = QuartileUtils upperFence(data.asJava, len)
    json.put(URL, kv._1)
    json.put(UPPER, upperFence)
    json
  }
}
