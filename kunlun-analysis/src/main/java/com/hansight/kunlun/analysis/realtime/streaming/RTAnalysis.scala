package com.hansight.kunlun.analysis.realtime.streaming

import java.io.IOException
import java.util.Properties

import org.apache.spark.SparkConf
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext

import com.hansight.kunlun.analysis.utils.DatetimeUtils
import com.hansight.kunlun.utils.Common

object RTAnalysis {
	val CONF = new Properties();
	
	def main(args: Array[String]) {
		if (args.length < 2) {
	      	System.out.println("usage: RTAnalysis <type> <startTime>");
	        System.out.println("type: elasticsearch time");
	        System.out.println("startTime: utc time");
	        System.out.println("\texample: log_iis 2013-12-09T16:00:00.000Z");
	        System.exit(1);
	    }
        try {
        	val global = getClass.getClassLoader()
        			.getResourceAsStream("real_time.properties");
            CONF.load(global);
        } catch  {
          case e: IOException =>
            println("can't get config");
            return
        }
		
		val esType = args(0);
		val startDate = DatetimeUtils.getTimestamp(args(1));
	    // Create the context with a 1 second batch size
	    val sparkConf = new SparkConf().setAppName("CustomReceiver")
	    val master = System.getenv("SPARK_MASTER")
	    if (master != null && master.length() > 0) {
	    	sparkConf.setMaster(master)
	    }
	    val ssc = new StreamingContext(sparkConf, Seconds(1))
	
	    // Create a input stream with the custom receiver on target ip:port and count the
	    // words in input stream of \n delimited text (eg. generated by 'nc')
	    val rdd = ssc.receiverStream(new ESReceiver(esType, startDate))
	    import Rule._
	    rdd.flatMap(dectect)
	    ssc.start()
	    ssc.awaitTermination()
	}
}