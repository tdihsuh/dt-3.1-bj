package com.hansight.kunlun.analysis.statistics.mr;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.hansight.kunlun.analysis.statistics.model.AnomalyConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.hadoop.mr.EsInputFormat;
import org.elasticsearch.hadoop.mr.EsOutputFormat;
import org.elasticsearch.hadoop.mr.LinkedMapWritable;

import com.hansight.kunlun.analysis.utils.quantile.QDigest;


public class AnomalyModeling extends Configured implements Tool {
    public static final String ANOMALY_MODEL_FORMAT = "com.hansight.kunlun.analysis.statistics.mr.AnomalyModel.format";

    /**
     * The first field of TimeIPPair is the start time, the second is the client
     * IP
     */
    public static class SessionMapper extends
            Mapper<Text, LinkedMapWritable, Text, LinkedMapWritable> {
        private DateFormat dateFormat;
       /* protected String[] fields;


        private HashMap<String, String> logDigest;


        @Override
        protected void setup(Context context
        ) throws IOException, InterruptedException {
            logDigest = new HashMap<String, String>();
        }*/

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            Configuration configuration = context.getConfiguration();
            String formatT = configuration.get(ANOMALY_MODEL_FORMAT);
            dateFormat = new SimpleDateFormat(formatT, Locale.US);
        }

        /**
         *
         */
        @Override
        protected void map(Text key, LinkedMapWritable value, Context context)
                throws IOException, InterruptedException {
            Writable vl = value.get(AnomalyConstants.time);
            Long logTime = 0L;
            if (vl != null) {
                logTime = Long.parseLong(vl.toString());
            }
            context.write(new Text(dateFormat.format(new Date(logTime))), value);
        }

    }

    public static class SessionReducer extends
            Reducer<Text, LinkedMapWritable, NullWritable, Text> {
        static final Logger s_logger = Logger.getLogger(SessionReducer.class);


        /**
         * This will definitely cause OOM, need to change to QDigest;
         * But firstly need to get the total count (in setup?).
         */
        @Override
        protected void reduce(Text key, Iterable<LinkedMapWritable> values,
                              Context context) throws IOException, InterruptedException {

            QDigest urlDigest = new QDigest(Long.MAX_VALUE);
            QDigest sc404Digest = new QDigest(Long.MAX_VALUE);
            QDigest sc500Digest = new QDigest(Long.MAX_VALUE);
            //We assume total url number and total sc-status count are the same as log line number
            Integer totalNumber = 0;
            boolean urlFlag = false;
            boolean flag404 = false;
            boolean flag500 = false;
            s_logger.info("Key info: " + key.toString());
            for (MapWritable log : values) {
                totalNumber++;
                s_logger.info("[" + totalNumber + "]: " + log.toString());
                Writable vl = log.get(AnomalyConstants.urlRate);
                if (vl != null) {
                    urlFlag = true;
                    urlDigest.offer((long) (Double.parseDouble(vl.toString()) * 100000));
                }
                vl = log.get(AnomalyConstants.sc404);
                if (vl != null) {
                    flag404 = true;
                    sc404Digest.offer((long) (Double.parseDouble(vl.toString()) * 100000));
                }
                vl = log.get(AnomalyConstants.sc500);
                if (vl != null) {
                    flag500 = true;
                    sc500Digest.offer((long) (Double.parseDouble(vl.toString()) * 100000));
                }
            }

            //Get the final model by day, (median)
            double urlRate = 0;
            double sc404Rate = 0;
            double sc500Rate = 0;
            if (urlFlag)
                urlRate = (double) urlDigest.getQuantile(0.5) / 100000;
            if (flag404)
                sc404Rate = (double) sc404Digest.getQuantile(0.5) / 100000;
            if (flag500)
                sc500Rate = (double) sc500Digest.getQuantile(0.5) / 100000;

            JSONObject json = new JSONObject();
            try {

                json.put("date", key);
                json.put("total", totalNumber);

                if (urlRate > 0.0) json.put("urlRate", urlRate);
                if (sc404Rate > 0.0) json.put("sc404", sc404Rate);
                if (sc500Rate > 0.0) json.put("sc500", sc500Rate);
                s_logger.info("Final result: " + json.toString());
                //Write the json to ES
                context.write(NullWritable.get(), new Text(json.toString()));
            } catch (JSONException e) {
                s_logger.error(e);
            }


        }
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setBoolean("mapreduce.map.speculative", false);
        conf.setBoolean("mapreduce.reduce.speculative", false);
        conf.set("es.nodes", String.format("%s:%d", args[0], 9200));
        conf.set("es.input.json", "yes");
        conf.set("es.resource.read", args[1] + "/anomaly/");
        conf.set("es.resource.write", args[2] + "/anomaly/");
        // conf.set("es.query", "?q=*");
        conf.set(ANOMALY_MODEL_FORMAT, "yyyyMMdd");
        Job job = Job.getInstance(conf);
        job.setJarByClass(AnomalyModeling.class);
        job.setInputFormatClass(EsInputFormat.class);
        job.setOutputFormatClass(EsOutputFormat.class);

        job.setMapperClass(SessionMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LinkedMapWritable.class);

       /* job.setPartitionerClass(FirstPartitioner.class);
        job.setSortComparatorClass(KeyComparator.class);
        job.setGroupingComparatorClass(GroupComparator.class);*/

        // job.setCombinerClass(SessionCombiner.class);
        job.setReducerClass(SessionReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new AnomalyModeling(), args);
        System.exit(exitCode);
    }
}