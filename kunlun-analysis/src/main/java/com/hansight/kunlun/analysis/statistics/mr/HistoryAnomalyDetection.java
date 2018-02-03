package com.hansight.kunlun.analysis.statistics.mr;

import com.hansight.kunlun.analysis.realtime.model.RTConstants;
import com.hansight.kunlun.analysis.realtime.single.StringEntropyCalculator;
import com.hansight.kunlun.analysis.statistics.model.AnomalyConstants;
import com.hansight.kunlun.analysis.statistics.model.TimeIPPair;
import com.hansight.kunlun.analysis.statistics.model.TimeTotalMaps;
import com.hansight.kunlun.analysis.utils.DatetimeUtils;
import com.hansight.kunlun.analysis.utils.EventTypeUtils;
import com.hansight.kunlun.analysis.utils.quantile.QDigest;
import com.hansight.kunlun.utils.EsUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.mr.EsInputFormat;
import org.elasticsearch.hadoop.mr.EsOutputFormat;
import org.elasticsearch.hadoop.mr.LinkedMapWritable;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * c_Ip：客户IP地址，cs_referer：访问到的地址 ，sc_status：返回状态码
 */
public class HistoryAnomalyDetection extends Configured implements Tool {
	private static final Logger LOG = Logger
			.getLogger(HistoryAnomalyDetection.class);
	public static String esMaster;
	public static String esReadIndex;
	public static String esReadType;
	public static String esWriteIndex;
	public static String esWriteType;

	/**
	 * The first field of TimeIPPair is the start time, the second is the
	 * client,请求到来的第一次时间，IP地址 IP
	 */
	public static class SessionMapper extends
			Mapper<Text, LinkedMapWritable, TimeIPPair, LinkedMapWritable> {
		static final Logger s_logger = Logger.getLogger(SessionMapper.class);
		protected String[] fields;

		/**
		 * Output Key: TimeIPPair, combination of [date + time + c_ip]<br/>
		 * Output Value: LinkedMapWritable of [input]<br/>
		 */
		@Override
		protected void map(Text key, LinkedMapWritable value, Context context)
				throws IOException, InterruptedException {
			try {
				String c_ip = value.get(AnomalyConstants.IP).toString();
				String datetime = value.get(AnomalyConstants.dtKey).toString();
				String query = value.get(AnomalyConstants.uriQuery).toString();
				MapWritable qvs = new LinkedMapWritable();
				// Split into param-value pairs
				double entropy = StringEntropyCalculator.calculate(query);

				int posNameStart = 0; // Move from the start pos of query string
										// to the first name
				do {
					int posValueEnd = query.indexOf('&', posNameStart);
					if (posValueEnd < 0)
						posValueEnd = query.length();

					String pair = query.substring(posNameStart, posValueEnd);
					int posEQ = pair.indexOf('=');

					String param = null, pvalue = null;
					if (posEQ >= 0) {
						param = pair.substring(0, posEQ);
						pvalue = pair.substring(posEQ + 1);
					} else {
						param = "";
						pvalue = pair;
					}
					qvs.put(new Text(param), new DoubleWritable(entropy));
					posNameStart = posValueEnd + 1;
				} while (posNameStart < query.length());
				value.put(AnomalyConstants.uriQuery, qvs);
				// ignore '.000Z' to avoid parse exception, time unit is 1 sec
				// at least
				long timestamp = DatetimeUtils.getTimestamp(datetime);
				InetAddress ipaddr = InetAddress.getByName(c_ip);
				long clientIP = 0;
				for (byte b : ipaddr.getAddress())
					clientIP = clientIP << 8 | (b & 0xFF);
				context.write(new TimeIPPair(timestamp, clientIP), value);
			} catch (Exception ex) {
				s_logger.error(ex.getMessage());
			} finally {
				// logDigest.clear();
			}
		}

	}

	public static class SessionReducer extends
			Reducer<TimeIPPair, LinkedMapWritable, NullWritable, Text> {
		private static final Integer ONE = 1;
		static final Logger s_logger = Logger.getLogger(SessionReducer.class);

		private double modelUrlRate;
		private double modelSc404Rate;
		private Double modelSc500Rate;
		private String readIndex;



		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			Configuration conf = context.getConfiguration();
			modelUrlRate = conf.getDouble("anomaly.urlRate", 0);
			modelUrlRate = conf.getDouble("anomaly.sc404", 0);
			modelUrlRate = conf.getDouble("anomaly.sc500", 0);
			readIndex = conf.get(ConfigurationOptions.ES_RESOURCE_READ);
		}

		/**
		 * <pre>
		 * Input &lt;K,Iterator&lt;V&gt;&gt; pair as following:<br/>
		 * &lt;TimeIPPair, Iterator&lt;"cs-uri-stem:x,cs-uri-query:x,cs_referer:x,cs-host:x,sc-status:x"&gt;&gt;
		 */
		@Override
		protected void reduce(TimeIPPair key,
				Iterable<LinkedMapWritable> values, Context context)
				throws IOException, InterruptedException {
			TimeIPPair key2 = new TimeIPPair(key);
			s_logger.info("Key info: " + key2.toString());
			TimeTotalMaps timeTotalMaps = new TimeTotalMaps(key.getTime());
			LinkedMapWritable lastLog = null;
			Set<String> servers = new HashSet<>();
			for (LinkedMapWritable log : values) {// log 相同IP
				lastLog = log;
				Writable serverValue = log.get(AnomalyConstants.computername);
				servers.add(serverValue.toString());
				
				Writable uriStemValue = log.get(AnomalyConstants.uriStem);
				Writable hostValue = log.get(AnomalyConstants.host);
				if (uriStemValue != null) {
					String k = hostValue.toString() + "/"
							+ uriStemValue.toString();// c_ip一段时间内请求一个地址的次数
					Integer value = timeTotalMaps.getUniqueUrls().get(k);
					timeTotalMaps.getUniqueUrls().put(k,
							value == null ? ONE : value + 1);
				}
				Writable statusValue = log.get(AnomalyConstants.status);
				if (statusValue != null) {
					Integer k = Integer.parseInt(statusValue.toString());
					Integer value = timeTotalMaps.getResponseCodes().get(k);
					timeTotalMaps.getResponseCodes().put(k,
							value == null ? ONE : value + 1);
				}
				Writable qvs = log.get(AnomalyConstants.uriQuery);
				if (qvs != null) {
					// TODO: the query parameter mode here

				}
				timeTotalMaps.inc();
			}
			Writable endDate = lastLog.get(AnomalyConstants.time);
			double urlRate = 0.0, sc404Rate = 0.0, sc500Rate = 0.0;
			if (timeTotalMaps.getUniqueUrls().size() > 0) {
				QDigest digest = new QDigest(timeTotalMaps.getUniqueUrls()
						.size() + 1);
				for (Integer num : timeTotalMaps.getUniqueUrls().values()) {
					s_logger.info("UniqueUrls: " + num);
					digest.offer(num.longValue());
				}
				long l = digest.getQuantile(0.1);
				urlRate = new BigDecimal((double) l / timeTotalMaps.getTotal())
						.setScale(5, BigDecimal.ROUND_CEILING).doubleValue();

			}
			if (timeTotalMaps.getResponseCodes().get(404) != null)
				sc404Rate = new BigDecimal((double) timeTotalMaps
						.getResponseCodes().get(404) / timeTotalMaps.getTotal())
						.setScale(5, BigDecimal.ROUND_CEILING).doubleValue();
			if (timeTotalMaps.getResponseCodes().get(500) != null)
				sc500Rate = new BigDecimal((double) timeTotalMaps
						.getResponseCodes().get(500) / timeTotalMaps.getTotal())
						.setScale(5, BigDecimal.ROUND_CEILING).doubleValue();
			try {
                JSONObject json = new JSONObject();
				json.put("c_ip", key.getIp());
				json.put("endDatetime", endDate.toString());
				json.put("startDatetime", key.getTime());
				json.put("indices", readIndex);
				json.put("servers", servers.toArray(new String[servers.size()]));
				if (urlRate > modelUrlRate) {
					json.put("category", EventTypeUtils.getCategory(RTConstants.EVENT_TYPE_HTTP_UNIQUE_URL));
					json.put("eventType", RTConstants.EVENT_TYPE_HTTP_UNIQUE_URL);
					json.put("degree", urlRate);
					json.put("modelDegree", modelUrlRate);
					context.write(NullWritable.get(), new Text(json.toString()));
				}
				if (sc404Rate > modelSc404Rate) {
					json.put("category", EventTypeUtils.getCategory(RTConstants.EVENT_TYPE_HTTP404));
					json.put("eventType", RTConstants.EVENT_TYPE_HTTP404);
					json.put("degree", sc404Rate);
					json.put("modelDegree", modelSc404Rate);
					context.write(NullWritable.get(), new Text(json.toString()));
				}
				if (sc500Rate > modelSc500Rate) {
					json.put("category", EventTypeUtils.getCategory(RTConstants.EVENT_TYPE_HTTP500));
					json.put("eventType", RTConstants.EVENT_TYPE_HTTP500);
					json.put("degree", sc500Rate);
					json.put("modelDegree", modelSc500Rate);
					context.write(NullWritable.get(), new Text(json.toString()));
				}
			} catch (JSONException e) {
				s_logger.error(e.getMessage());
			}

			// Get the session unique URL quantile (10 percentile), 404/500 rate
			// for (Map.Entry<Long, TimeTotalMaps> entry : map.entrySet()) {}

		}
	}
	
	public static class FirstPartitioner extends Partitioner<TimeIPPair, Text> {
		// static final Logger s_logger =
		// Logger.getLogger(FirstPartitioner.class);
		@Override
		public int getPartition(TimeIPPair key, Text value, int numPartitions) {
			// s_logger.info("key: " + key + ", value: " + value.toString() +
			// ", partitions: "+ numPartitions);
			// divide by IP
			return (int) (Math.abs(key.getIp() * 127) % numPartitions);
		}
	}

	public static class KeyComparator extends WritableComparator {
		// static final Logger s_logger = Logger.getLogger(KeyComparator.class);
		protected KeyComparator() {
			super(TimeIPPair.class, true);
		}

		/**
		 * Sort will follow TimeIPPair comparator sequence
		 */
		@Override
		public int compare(WritableComparable w1, WritableComparable w2) {
			TimeIPPair p1 = (TimeIPPair) w1;
			TimeIPPair p2 = (TimeIPPair) w2;
			// s_logger.info("compare: " + p1 + " and " + p2);
			int cmp = TimeIPPair.compare(p1.getIp(), p2.getIp());
			if (cmp != 0) {
				return cmp;
			}
			return TimeIPPair.compare(p1.getTime(), p2.getTime());
		}
	}

	public static class GroupComparator extends WritableComparator {
		// static final Logger s_logger =
		// Logger.getLogger(GroupComparator.class);
		protected GroupComparator() {
			super(TimeIPPair.class, true);
		}

		/**
		 * Logs with from the same c-ip within 30 minutes will go to the same
		 * reducer<br/>
		 */
		@Override
		public int compare(WritableComparable w1, WritableComparable w2) {
			TimeIPPair p1 = (TimeIPPair) w1;
			TimeIPPair p2 = (TimeIPPair) w2;
			// s_logger.info("compare: " + p1 + " and " + p2);

			int cmp = TimeIPPair.compare(p1.getIp(), p2.getIp());
			if (cmp != 0) {
				return cmp;
			}
			// return cmp;
			/* 取消分组 */
			long timeDiff = p1.getTime() - p2.getTime();
			if (timeDiff > 300000) // 1800000L
				return 1; // as one session if there are within 30 mins
			else if (timeDiff < -300000)
				return -1;
			else
				return 0;

		}
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 1) {
			usage();
			return 1;
		}
		String date = args[0];
		esReadIndex = "logs_" + date;
		esReadType = "aadp";
		esWriteIndex = "anomaly_" + date;
		esWriteType = "anomaly";
		Configuration conf = new Configuration();
		conf.addResource("es-site.xml");
		conf.set(ConfigurationOptions.ES_RESOURCE_READ,
				String.format("%s/%s/", esReadIndex, esReadType));
		conf.set(ConfigurationOptions.ES_RESOURCE_WRITE,
				String.format("%s/%s/", esWriteIndex, esWriteType));
		if (!setModelRate(conf, date)) {
			throw new IllegalArgumentException(
					"unable get the model from model_final/anomaly");
		}

		// ES model_final/anomaly
		Job job = Job.getInstance(conf);
		job.setJarByClass(HistoryAnomalyDetection.class);

		job.setInputFormatClass(EsInputFormat.class);
		job.setOutputFormatClass(EsOutputFormat.class);

		job.setMapperClass(SessionMapper.class);
		job.setMapOutputKeyClass(TimeIPPair.class);
		job.setMapOutputValueClass(LinkedMapWritable.class);

		job.setPartitionerClass(FirstPartitioner.class);
		job.setSortComparatorClass(KeyComparator.class);
		job.setGroupingComparatorClass(GroupComparator.class);

		job.setReducerClass(SessionReducer.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	private boolean setModelRate(Configuration conf, String date) {
		String name = conf.get("es.cluster.name");
		String address = conf.get("es.nodes");
		String index = "model_final";
		String type = "anomaly";
		String field = "date";
		TransportClient client = EsUtils.getNewClient(name, address);
		SearchResponse response = client.prepareSearch(index).setTypes(type)
				.setQuery(QueryBuilders.termQuery(field, date)).setFrom(0)
				.setSize(1).execute().actionGet();
		if (response != null) {
			for (SearchHit hit : response.getHits()) {
				Double urlRate = (Double) hit.getSource().get("urlRate");
				Double sc404 = (Double) hit.getSource().get("sc404");
				Double sc500 = (Double) hit.getSource().get("urlRate");
				conf.set("anomaly.urlRate", urlRate.toString());
				conf.set("anomaly.sc404", sc404.toString());
				conf.set("anomaly.sc500", sc500.toString());
				return true;
			}
		}
		return false;
	}

	private void usage() {
		System.out.println("params: date");
		System.out.println("date: yyyyMMdd, example: 20131215");
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new AnomalyDetection(), args);
		System.exit(exitCode);
	}
}