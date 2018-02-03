package com.hansight.kunlun.analysis.statistics.single

import com.hansight.kunlun.analysis.statistics.spark.Constants._
import com.hansight.kunlun.analysis.utils.SearchRequestBuilderMaker
import com.hansight.kunlun.analysis.utils.v2.EditDistanceUrlCluster
import com.hansight.kunlun.utils.EsUtils
import org.elasticsearch.action.bulk.{BulkRequestBuilder, BulkResponse}
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.{InternalTerms, LongTerms, StringTerms}
import org.slf4j.{Logger, LoggerFactory}

/**
 * Created by zhhuiyan on 14/11/27.
 */
object Modeling404 {
  protected val logger: Logger = LoggerFactory.getLogger(classOf[Modeling404])

  def main(args: Array[String]) {
    if (args.length < 2) throw new IllegalArgumentException("args is less ,must have {indices types [threshold]}")
    val indices = args(0)
    val types = args(1)
    var threshold =Long.MaxValue
    if(args.length>=3){
    try {
      threshold = args(2).toLong

     } catch {
      case e: Exception =>
        throw new IllegalArgumentException(" threshold must be a long type number value", e)
    }
    }
    modeling(indices, types, threshold)

  }

  def modeling(indices: String, types: String,
               threshold: Long): Unit = {
    logger.info("model 404 start")
    val client = EsUtils.getEsClient
    val rq = SearchRequestBuilderMaker.make(client,indices.split(","),types.split(",")).setSize(0) //.setQuery(qb)
    val response: SearchResponse =
      rq.addAggregation(AggregationBuilders.terms("status").field("sc_status").subAggregation(
        AggregationBuilders.terms("url").field("cs_uri_stem.raw").size(0)
      ).size(0)
      ).get()
    val aggregations = response.getAggregations.get[InternalTerms]("status")
    import scala.collection.JavaConverters._
    logger.debug("model 404 aggregations finished")
    val bucket200=aggregations.getBucketByKey("200")
    logger.info(" bucket 200 count:{}",bucket200.getDocCount)
    val urls200 =bucket200.getAggregations.get[StringTerms]("url").getBuckets.asScala.map(_.getKey).toList
    val cluster: EditDistanceUrlCluster = new EditDistanceUrlCluster
    cluster.cluster(urls200.asJava)
    var builder = client.prepareBulk
    var i = 0
    val bucket404=aggregations.getBucketByKey("404")
    logger.info(" bucket 404 count:{}",bucket404.getDocCount)
    bucket404.getAggregations.get[StringTerms]("url").getBuckets.asScala.foreach {
      bucket => {
        builder.add(client.prepareIndex("model", "status_404")
          .setSource(URL, bucket.getKey, MALICIOUS, threshold > bucket.getDocCount && cluster.isOutlier(bucket.getKey), "count", bucket.getDocCount))
          i += 1
        if (i == 1000) {
          execute(builder)
          i = 0
          builder = client.prepareBulk
    }
      }
    }

    if (i != 0) {
      execute(builder)
      i = 0
    }

    logger.info("model 404 end")
  }

  def execute(builder: BulkRequestBuilder): Unit = {
    val response: BulkResponse = builder.execute.actionGet
    if (response.hasFailures) {
      logger.error("es save has error info :{}", response.buildFailureMessage)
    }
  }
}

class Modeling404 {

}
