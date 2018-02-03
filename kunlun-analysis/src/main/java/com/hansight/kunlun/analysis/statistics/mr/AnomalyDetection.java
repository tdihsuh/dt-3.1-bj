package com.hansight.kunlun.analysis.statistics.mr;

import com.hansight.kunlun.analysis.statistics.model.AnomalyConstants;
import com.hansight.kunlun.analysis.statistics.model.LogWritable;
import com.hansight.kunlun.analysis.statistics.model.TimeTotalMaps;
import com.hansight.kunlun.analysis.utils.DatetimeUtils;
import com.hansight.kunlun.analysis.utils.quantile.QDigest;
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
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.mr.EsInputFormat;
import org.elasticsearch.hadoop.mr.EsOutputFormat;
import org.elasticsearch.hadoop.mr.LinkedMapWritable;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * c_Ip：客户IP地址，cs_referer：访问到的地址 ，sc_status：返回状态码
 */
public class AnomalyDetection extends Configured implements Tool {
    public static String esMaster;
    public static String esReadIndex;
    public static String esReadType;
    public static String esWriteIndex;
    public static String esWriteType;

    /**
     * The first field of TimeIPPair is the start time, the second is the
     * client,请求到来的第一次时间，IP地址 IP
     */
    // public static class SessionMapper extends Mapper<Text, LinkedMapWritable,
    // TimeIPPair, LinkedMapWritable> {
    public static class SessionMapper extends
            Mapper<Text, LinkedMapWritable, LongWritable, LogWritable> {
        static final Logger logger = Logger.getLogger(SessionMapper.class);

        /**
         * 简单按照IP分割到不同的reduce
         */
        @Override
        protected void map(Text key, LinkedMapWritable value, Context context)
                throws IOException, InterruptedException {
            Writable wip = value.get(AnomalyConstants.IP);
            InetAddress ipaddr = InetAddress.getByName(wip.toString());
            long clientIP = 0;
            for (byte b : ipaddr.getAddress())
                clientIP = clientIP << 8 | (b & 0xFF);
            LogWritable log = new LogWritable();
            log.setCip(clientIP);
            Writable time = value.get(AnomalyConstants.dtKey);
            if (time != null) {
                log.setTime(DatetimeUtils.getTimestamp(time.toString()));
            }
            StringBuilder url = new StringBuilder();
            LongWritable port = (LongWritable) value.get(AnomalyConstants.port);
            if (port != null) {
                if (port.get() == 443)
                    url.append("https://");
                else
                    url.append("http://");
            }
            Writable host = value.get(AnomalyConstants.host);
            if (host != null) {
                url.append(host.toString());
            }
            Writable refererValue = value.get(AnomalyConstants.uriStem);
            if (refererValue != null) {
                url.append(refererValue.toString());

            }
            log.setUrl(url.toString());
            LongWritable statusValue = (LongWritable) value
                    .get(AnomalyConstants.status);
            if (statusValue != null) {
                log.setStatus((int) statusValue.get());
            }
            refererValue = value.get(AnomalyConstants.userAgent);
            if (refererValue != null) {
                log.setUserAgent(refererValue.toString());
            }
            refererValue = value.get(AnomalyConstants.cookieId);
            if (refererValue != null) {
                log.setCookieId(refererValue.toString().hashCode());
            }
            context.write(new LongWritable(clientIP), log);
        }

    }

    public static class SessionReducer extends
            Reducer<LongWritable, LogWritable, NullWritable, Text> {

        private static final IntWritable ONE = new IntWritable(1);
        static final Logger s_logger = Logger.getLogger(SessionReducer.class);

        /**
         * <pre>
         * Input &lt;K,Iterator&lt;V&gt;&gt; pair as following:<br/>
         * &lt;TimeIPPair, Iterator&lt;"cs-uri-stem:x,cs-uri-query:x,cs_referer:x,cs-host:x,sc-status:x"&gt;&gt;
         */
        @Override
        protected void reduce(LongWritable key, Iterable<LogWritable> values,
                              Context context) throws IOException, InterruptedException {

            ArrayList<LogWritable> logs = new ArrayList<>();
            ArrayList<LogWritable> roughs = new ArrayList<>();
            for (LogWritable l : values) {
                // copy 不然日志不准确
                LogWritable log = new LogWritable(l);
                logs.add(new LogWritable(log));
                if (log.getCookieId() != 0) {
                    boolean noRoughLogs = true;
                    for (int i = 0; i < roughs.size(); i++) {
                        LogWritable p = roughs.get(i);
                        if (p == null)
                            continue;
                        if (LogWritable.equal(p.getUserAgent(),
                                log.getUserAgent())) {
                            p.setCookieId(log.getCookieId());
                            roughs.set(i, null);
                        } else {
                            noRoughLogs = false;
                        }
                    }
                    if (noRoughLogs)
                        roughs.clear();
                } else {
                    roughs.add(log);
                }

            }
            roughs.clear();
            for (int i = 0; i < logs.size(); i++) {
                LogWritable first = logs.get(i);
                if (first == null) {
                    continue;
                }
                TimeTotalMaps timeTotalMaps = new TimeTotalMaps();
                timeTotalMaps.setTime(first.getTime());

                if (first.getUrl() != null) {
                    Integer value = timeTotalMaps.getUniqueUrls().get(
                            first.getUrl());
                    timeTotalMaps.getUniqueUrls().put(first.getUrl(),
                            value == null ? ONE.get() : value + ONE.get());
                }
                if (first.getStatus() != 0) {
                    Integer value = timeTotalMaps.getResponseCodes().get(
                            first.getStatus());
                    timeTotalMaps.getResponseCodes().put(first.getStatus(),
                            value == null ? ONE.get() : value + ONE.get());
                }
                timeTotalMaps.inc();
                for (int j = i + 1; j < logs.size(); j++) {
                    LogWritable next = logs.get(j);
                    if (next == null)
                        continue;
                    if (LogWritable.equal(next.getCookieId(),
                            first.getCookieId())) {
                        if (next.getUrl() != null) {
                            Integer value = timeTotalMaps.getUniqueUrls().get(
                                    next.getUrl());
                            timeTotalMaps.getUniqueUrls().put(
                                    next.getUrl(),
                                    value == null ? ONE.get() : value
                                            + ONE.get());
                        }
                        if (next.getStatus() != 0) {
                            Integer value = timeTotalMaps.getResponseCodes()
                                    .get(next.getStatus());
                            timeTotalMaps.getResponseCodes().put(
                                    next.getStatus(),
                                    value == null ? ONE.get() : value
                                            + ONE.get());
                        }
                        timeTotalMaps.inc();
                        logs.set(j, null);
                    }
                }
                double urlRate = 0.0, sc404Rate = 0.0, sc500Rate = 0.0;
                if (timeTotalMaps.getUniqueUrls().size() > 0) {
                    QDigest digest = new QDigest(timeTotalMaps.getUniqueUrls()
                            .size() + 1);

                    for (Map.Entry<String, Integer> num : timeTotalMaps
                            .getUniqueUrls().entrySet()) {
                        digest.offer(num.getValue().longValue());
                    }
                    long l = digest.getQuantile(0.1);
                    urlRate = new BigDecimal((double) l
                            / timeTotalMaps.getTotal()).setScale(5,
                            BigDecimal.ROUND_CEILING).doubleValue();

                }
                if (timeTotalMaps.getResponseCodes().get(404) != null)
                    sc404Rate = new BigDecimal((double) timeTotalMaps
                            .getResponseCodes().get(404)
                            / timeTotalMaps.getTotal()).setScale(5,
                            BigDecimal.ROUND_CEILING).doubleValue();
                if (timeTotalMaps.getResponseCodes().get(500) != null)
                    sc500Rate = new BigDecimal((double) timeTotalMaps
                            .getResponseCodes().get(500)
                            / timeTotalMaps.getTotal()).setScale(5,
                            BigDecimal.ROUND_CEILING).doubleValue();
                JSONObject json = new JSONObject();
                try {
                    json.put("session",
                            UUID.randomUUID().toString().replace("-", ""));
                    json.put("c_ip", key.get());
                    json.put("time", timeTotalMaps.getTime());
                    json.put("total", timeTotalMaps.getTotal());
                    if (urlRate > 0.0)
                        json.put("urlRate", urlRate);
                    if (sc404Rate > 0.0)
                        json.put("sc404", sc404Rate);
                    if (sc500Rate > 0.0)
                        json.put("sc500", sc500Rate);
                     s_logger.info("Final result: " + json.toString());
                    context.write(NullWritable.get(), new Text(json.toString()));
                } catch (JSONException e) {
                    s_logger.error(e.getMessage());
                }
            }
        }
    }

    public static class FirstPartitioner extends
            Partitioner<LongWritable, LogWritable> {
        @Override
        public int getPartition(LongWritable key, LogWritable value,
                                int numPartitions) {
            return (int) (Math.abs(key.get() * 127) % numPartitions);
        }
    }

    public static class KeyComparator extends WritableComparator {
        protected KeyComparator() {
            super(LongWritable.class, true);
        }

        /**
         * Sort will follow TimeIPPair comparator sequence
         */
        @Override
        public int compare(WritableComparable w1, WritableComparable w2) {
            return w1.compareTo(w2);
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 2)
            throw new IllegalArgumentException("args is less ,must be 2 args");
        String[] input = args[1].split("/");
        if (input.length < 2) {
            throw new IllegalArgumentException("args is less,must be ../..");
        }
        // should read this from conf or via command line args
        esMaster = args[0];

        esReadIndex = input[0];
        esReadType = input[1];
        esWriteIndex = "model_mdl";
        esWriteType = "anomaly";

        Configuration conf = new Configuration();
        conf.setBoolean("mapreduce.map.speculative", false);
        conf.setBoolean("mapreduce.reduce.speculative", false);

        conf.set(ConfigurationOptions.ES_NODES,
                String.format("%s:%d", esMaster, 9200));
        conf.set(ConfigurationOptions.ES_INPUT_JSON, "yes");
        conf.set(ConfigurationOptions.ES_RESOURCE_READ,
                String.format("%s/%s/", esReadIndex, esReadType));
        conf.set(ConfigurationOptions.ES_RESOURCE_WRITE,
                String.format("%s/%s/", esWriteIndex, esWriteType));
        // conf.set(ConfigurationOptions.ES_QUERY, "?q=*");

        Job job = Job.getInstance(conf);
        job.setJarByClass(AnomalyDetection.class);

        job.setInputFormatClass(EsInputFormat.class);
        job.setOutputFormatClass(EsOutputFormat.class);

        job.setMapperClass(SessionMapper.class);
        // job.setMapOutputKeyClass(TimeIPPair.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(LogWritable.class);

        job.setPartitionerClass(FirstPartitioner.class);
        job.setSortComparatorClass(KeyComparator.class);
        // job.setGroupingComparatorClass(GroupComparator.class);

        job.setReducerClass(SessionReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new AnomalyDetection(), args);
        System.exit(exitCode);
    }
}