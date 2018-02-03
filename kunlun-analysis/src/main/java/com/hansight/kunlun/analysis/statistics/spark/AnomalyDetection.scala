package com.hansight.kunlun.analysis.statistics.spark

import java.io.Serializable
import java.util.{HashMap, HashSet}

import com.hansight.kunlun.analysis.statistics.spark.Constants.{COOKIE_ID, CS_HOST, C_IP, MALICIOUS, SC_STATUS, S_IP, S_PORT, TIMESTAMP, UNIQUEURLS, URI_QUERY, URI_STEM, URL, USER_AGENT}
import com.hansight.kunlun.analysis.statistics.spark.DetectionFunctions.{compute404Model, keyTransfer, mapToLogIpAsKey, modelingFinal, modelingMiddle, modelingStatic, urlStatusAsKey}
import com.hansight.kunlun.analysis.statistics.spark.KMeanDataMaker.toIPURLTuple
import com.hansight.kunlun.analysis.statistics.spark.QueryStringFunctions.{calculateEntropy, modeling}
import com.hansight.kunlun.analysis.utils.SearchRequestBuilderMaker
import com.hansight.kunlun.analysis.utils.v2.EditDistanceUrlCluster
import com.hansight.kunlun.utils.{Common, EsUtils}
import org.apache.commons.codec.digest.DigestUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext.rddToPairRDDFunctions
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.hadoop.cfg.ConfigurationOptions
import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders}
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.{InternalTerms, StringTerms}
import org.elasticsearch.spark.rdd.EsSpark
import org.elasticsearch.spark.{sparkContextFunctions, sparkRDDFunctions}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

/**
 * Author: zhhui 
 * Date: 2014/9/11
 */
object AnomalyDetection extends Serializable {
  protected val logger: Logger = LoggerFactory.getLogger(classOf[AnomalyDetection])

  def main(args: Array[String]) {
    if (args.length < 2) throw new IllegalArgumentException("args is less ,must have [index/type method]\n method:\n\t1. kmeans\n\t2. 404 \n\t\t can add a new params [threshold]:404 count big than it as FalseAlarm, \n\t3. rate\n\t4. querystring\n")
    logger.debug("classpath:" + System.getProperty("java.class.path"));//系统的classpaht路径
    val from = args(0)
    val method = args(1)
    val middle = "model/middle"
    val finale = "model/final"
    val queryStringModel = "model/query_string"
    val model404 = "model/status_404"
    val modelKMean = "model/k_mean"
    val host_port = Common.get(Common.ES_CLUSTER_HOST)
    val conf = new SparkConf().setAppName("application: detection anomaly %s ".format(method))
    val host_port9200 = host_port.split(",").map(_.split(":")(0) + ":" + ConfigurationOptions.ES_PORT_DEFAULT).reduce((first, second) => {
      first + "," + second
    })
    ;
    conf.set(ConfigurationOptions.ES_NODES, host_port9200)
      .set(ConfigurationOptions.ES_SCROLL_KEEPALIVE, Common.get(ConfigurationOptions.ES_SCROLL_KEEPALIVE, "5m"))
      .set(ConfigurationOptions.ES_BATCH_SIZE_BYTES, Common.get(ConfigurationOptions.ES_BATCH_SIZE_BYTES, "50m"))
      .set(ConfigurationOptions.ES_INDEX_READ_MISSING_AS_EMPTY, Common.get(ConfigurationOptions.ES_INDEX_READ_MISSING_AS_EMPTY, "true"))
      .set(ConfigurationOptions.ES_SCROLL_SIZE, Common.get(ConfigurationOptions.ES_SCROLL_SIZE, "5000"))
      .set(ConfigurationOptions.ES_HEART_BEAT_LEAD, Common.get(ConfigurationOptions.ES_HEART_BEAT_LEAD, "5s"))
    val master = System.getenv("SPARK_MASTER")
    if (master != null) {
      conf.setMaster(master)
    }
    val sc = new SparkContext(conf)
    if (method.equals("kmeans")) {
      kmeans(sc, from, modelKMean)
    }
    if (method.equals("404")) {
      var threshold = Long.MaxValue
      if (args.length >= 3) {
        try { {
          threshold = args(2).toLong
        }
        } catch {
          case e: Exception =>
            throw new IllegalArgumentException(" threshold must be a long type number value", e)
        }
      }
      //modeling404(sc, from, model404)
      model404new(sc, from, model404, threshold)
    }
    if (method.toLowerCase.equals("rate"))
      modelingRate(sc, from, finale)
    if (method.toLowerCase.equals("querystring"))
      modelingQueryString(sc, from, queryStringModel)

  }

  def modeling404(sc: SparkContext,
                  from: String,
                  model404: String): Unit = {

    val qb: QueryBuilder = QueryBuilders.queryString( """"sc_status:200 OR sc_status:404"""")
    /* val client=  EsUtils.getNewClient("es130","yzh:9300")

      val response:SearchResponse =
         client.prepareSearch("logs_*").setTypes("log_iis").setQuery(qb).addAggregation(
           AggregationBuilders.terms("status").field("sc_status").subAggregation(
             AggregationBuilders.terms("url").field("cs_uri_stem")
           )
         ).setSize(0).get()
        val aggs=response.getAggregations.asMap().values().toArray.foreach(println)
   */

    sc.esRDD(from,
      """{"query": {"query_string": {"query": "sc_status:200 OR sc_status:404"}},"fields":["%s","%s"]}""".format(URI_STEM, SC_STATUS)).asInstanceOf[RDD[(String, mutable.LinkedHashMap[String, Any])]]
      .values
      .map(urlStatusAsKey)
      .reduceByKey(_ + _)
      .keys
      .map(keyTransfer)
      .groupByKey()
      .flatMap(compute404Model)
      .saveToEs(model404)


  }

  def model404new(sc: SparkContext, from: String,
                  model404: String, threshold: Long): Unit = {
    logger.info("model 404 start")
    val client = EsUtils.getEsClient
    import scala.collection.JavaConverters._
    var agg = aggregation(from, client)
    
     val cluster: EditDistanceUrlCluster = new EditDistanceUrlCluster
    cluster.cluster(agg._2)
    
    var json: List[Map[String, Any]] = Nil
    var i = 0
    logger.info(" bucket 404 count:{}", agg._1.size())
    agg._1.asScala.foreach {
      bucket => json = Map("urlhash" -> DigestUtils.md5Hex(bucket._1),URL -> bucket._1, MALICIOUS -> (threshold > bucket._2 && cluster.isOutlier(bucket._1)), "count" -> bucket._2) :: json
        i += 1
        if (i == 1000) {
          //sc.makeRDD(json).saveToEs(model404)
          EsSpark.saveToEs(sc.makeRDD(json),model404,Map("es.mapping.id" -> "urlhash"))
          json = Nil
          i = 0
        }
    }
    if (i != 0) {
      //sc.makeRDD(json).saveToEs(model404)
      EsSpark.saveToEs(sc.makeRDD(json),model404,Map("es.mapping.id" -> "urlhash"))
    }

    logger.debug("model 404 end")
  }
  
  def aggregation(from: String, client: TransportClient) :(HashMap[String, Long], HashSet[String]) = {
    import scala.collection.JavaConverters._
    var res200 = new HashSet[String]()
    var res404 = new HashMap[String, Long]()
    println("indices")
    var arr = from.split(",")
    for (tmp <- arr) {
      var indexType = tmp.split("/")
      
      var toArr = new Array[String](1)
      toArr(0) = indexType(0)
      var typeArr = new Array[String](1)
      typeArr(0) = indexType(1)
      logger.debug("toArr" + toArr.length + ", value:" + toArr(0))
      logger.debug("typeArr" + typeArr.length + ", value:" + typeArr(0))
      val startTime = System.currentTimeMillis()
      val rq = SearchRequestBuilderMaker.make(client, toArr, typeArr).setSize(0) //.setQuery(qb)
      val response: SearchResponse =
        rq.addAggregation(AggregationBuilders.terms("status").field("sc_status").subAggregation(
          AggregationBuilders.terms("url").field("cs_uri_stem.raw").size(0)
        ).size(0)
        ).get()
  
      val aggregations = response.getAggregations.get("status").asInstanceOf[InternalTerms]
      val stopTime = System.currentTimeMillis()
      logger.info(tmp + " aggregation use time:" + (stopTime - startTime))
		  logger.debug("model 404 aggregations  finished")
      // status 200
      if (aggregations.getBucketByKey("200") != null &&
          aggregations.getBucketByKey("200").getAggregations != null &&
          aggregations.getBucketByKey("200").getAggregations.get[StringTerms]("url") != null) {
    	  val bucket200 = aggregations.getBucketByKey("200").getAggregations.get[StringTerms]("url").getBuckets;
    	  logger.info(" bucket 200 count:{}", bucket200.size())
    	  bucket200.asScala.foreach { x => res200.add(x.getKey) }
      }
      // status 404
      if (aggregations.getBucketByKey("404") != null &&
          aggregations.getBucketByKey("404").getAggregations != null &&
          aggregations.getBucketByKey("404").getAggregations.get[StringTerms]("url") != null) {
          val bucket404 = aggregations.getBucketByKey("404").getAggregations.get[StringTerms]("url").getBuckets
        logger.info(" bucket 404 count:{}", bucket404.size())
        bucket404.asScala.foreach(x => {
          var count = res404.get(x.getKey)
          if (count == null) {
            count = x.getDocCount
          } else {
            count += x.getDocCount
          }
          res404.put(x.getKey, count)
        })
      }
    }
    (res404, res200)
  }
  

  def modelingQueryString(sc: SparkContext,
                          from: String,
                          queryStringModel: String): Unit = {
    val logData = sc.esRDD(from,
      """{"query": {"query_string": {"query": "cs_uri_query:* AND cs_uri_stem:* AND sc_status:*" } },"fields":["%s","%s","%s","%s","%s"]}""".format(C_IP, CS_HOST, URI_STEM, URI_QUERY,SC_STATUS))
      .asInstanceOf[RDD[(String, mutable.LinkedHashMap[String, Any])]]
      .values.flatMap(calculateEntropy)
      .groupByKey()
      .map(modeling)
      .saveToEs(queryStringModel)

  }

  def modelingRate(sc: SparkContext,
                   from: String,
                   finale: String
                    ): Unit = {
    val logData = sc.esRDD(from,
      """{"query":{"match_all":{}},"fields":["%s","%s","%s","%s","%s","%s","%s","%s"]}""".format(C_IP, S_PORT, CS_HOST, URI_STEM, SC_STATUS, USER_AGENT, COOKIE_ID, TIMESTAMP))
      .asInstanceOf[RDD[(String, mutable.LinkedHashMap[String, Any])]]
      .values
      .cache()
    val logs = logData.map(mapToLogIpAsKey)
      .groupByKey()
      .flatMap(modelingStatic)
      .reduceByKey(_ add _)
      .map(modelingMiddle)
      .cache()
    //model from middle
    logs.groupByKey()
      .map(modelingFinal)
      .saveToEs(finale)
    //model from final
    //logs.map(_._2).saveToEs(middle)
  }

  def kmeans(sc: SparkContext,
             from: String, to: String) {
    val samples = sc.esRDD(from,
      """{"query": {"query_string": {"query": "cs_uri_stem:* AND sc_status:*" } },"fields":["%s","%s","%s","%s"]}""".format(C_IP, S_PORT, CS_HOST, URI_STEM))
      .asInstanceOf[RDD[(String, mutable.LinkedHashMap[String, Any])]]
      .values.map(toIPURLTuple)
      .groupByKey()
      .map(tuple => {
        val data = tuple._2
        val map: mutable.Map[Int, Double] = mutable.Map.empty
        data.foreach { key => {
          val value = map.getOrElse(key, .0)
          map.put(key, value + 1.0)
      }
      }

      //url,value
      //   val (indices, values) = map.unzip
      val seq = map.toSeq
      //  val (url, core) = seq.sortBy(_._1).last

      val features = Vectors.sparse(UNIQUEURLS.size + 1, seq)
      (tuple._1, features)
    })
    /* samples.map(
       record => {
         val json = new mutable.HashMap[String, Any]()
         json.put(S_IP, record._1)
         json.put("vector", record._2)
         json
       }).saveToEs(to)*/
    /* val sampleFeatures =sc.esRDD(to).asInstanceOf[RDD[(String, mutable.LinkedHashMap[String, Any])]]
       .values.map(
         record => {
           record.getOrElse("vector",None).asInstanceOf[Vector]
         }
       )*/
    val sampleFeatures = samples
      .map(
        record => {
          record._2
        }
      )



    val numClusters = 100
    val numIterations = 10
    val runs = 1
    val model = KMeans.train(sampleFeatures, numClusters, numIterations, runs, KMeans.K_MEANS_PARALLEL, KMeans.COSINE_DISTANCE)

    val sample2clusters = samples.map(record => {
      var (c, d) = model.predict2(record._2, KMeans.COSINE_DISTANCE)
      c = if (d > 0.3) -1 else c
      (c, record._1)
    })
    /* val URLMap = UNIQUEURLS.map(tuple => {
       (tuple._2, tuple._1)
     })*/
    sample2clusters.map(tree => {
      val json = new mutable.HashMap[String, Any]()
      json.put("score", tree._1)
      json.put(S_IP, tree._2)
      json
    }).saveToEs(to)
  }
}

class AnomalyDetection