package com.hansight.kunlun.analysis.statistics.spark

import com.hansight.kunlun.analysis.statistics.spark.Constants._
import com.hansight.kunlun.analysis.utils.v2.EditDistanceUrlCluster
import com.hansight.kunlun.utils.EsUtils
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.{QueryBuilders, QueryBuilder}
import org.elasticsearch.search.aggregations.{InternalAggregations, AggregationBuilders}
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket
import org.elasticsearch.search.aggregations.bucket.terms.{LongTerms, StringTerms}

import scala.collection.JavaConverters._

/**
 * Created by zhhuiyan on 14/11/26.
 */
object AggrTest {
  def main(args: Array[String]) {

    val qb: QueryBuilder = QueryBuilders.queryString( """"sc_status:200 OR sc_status:404"""")
    val client = EsUtils.getNewClient("es_local", "local:9300")
 val rq=  client.prepareSearch("logs_*")//.setQuery(qb)
    val response: SearchResponse =
        rq.addAggregation(AggregationBuilders.terms("status").field("sc_status").subAggregation(
          AggregationBuilders.terms("url").field("cs_uri_stem")
        )
      ).get()
    val Aggregation = response.getAggregations
    var  urls200:List[String]=Nil
    var  urls404:List[(String,Long)]=Nil
     var aggs=Aggregation.get("status").asInstanceOf[StringTerms]
    aggs.getBucketByKey("200").getAggregations.asInstanceOf[InternalAggregations].asList().toArray.foreach(agg=>{
      val term=agg.asInstanceOf[StringTerms]
        term.getBuckets.toArray.foreach(
          b=>{
          val bucket=  b.asInstanceOf[Bucket]
            urls200= bucket.getKey ::urls200
          } )
    })
    aggs=Aggregation.get("status").asInstanceOf[StringTerms]
    aggs.getBucketByKey("404").getAggregations.asInstanceOf[InternalAggregations].asList().toArray.foreach(agg=>{
      val term=agg.asInstanceOf[StringTerms]
      term.getBuckets.toArray.foreach(
        b=>{
          val bucket=  b.asInstanceOf[Bucket]
          urls404=(bucket.getKey,bucket.getDocCount)::urls404
        } )
    })
    var json: List[Map[String, Any]] = Nil
    val cluster: EditDistanceUrlCluster = new EditDistanceUrlCluster
    cluster.cluster(urls200.asJava)
  //  println(aggs)
    for (url <- urls404) {
      json = Map(URL -> url, MALICIOUS -> cluster.isOutlier(url._1)) :: json
      println(url._1+":"+url._2+":"+cluster.isOutlier(url._1))
    }
    json

  }
}
