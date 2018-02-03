package com.hansight.kunlun.analysis.statistics.mr;

import com.hansight.kunlun.analysis.realtime.single.StringEntropyCalculator;
import com.hansight.kunlun.analysis.statistics.model.AnomalyConstants;
import com.hansight.kunlun.analysis.utils.IPFilter;
import com.hansight.kunlun.analysis.utils.QuartileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
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
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;


public class QueryStringAnomalyDetection extends Configured implements Tool {
    public static String esMaster;
    public static String esReadIndex;
    public static String esReadType;
    public static String esWriteIndex;
    public static String esWriteType;

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 3)
            throw new IllegalArgumentException("args is less ,must be 3 args i.e. master readIndex1,readIndex2,.../type writeIndex ");
        String[] input = args[1].split("/");
        if (input.length < 2) {
            throw new IllegalArgumentException("args[1] is less,must be ../..");
        }
        //should read this from conf or via command line args
        esMaster = args[0];

        esReadIndex = input[0];
        esReadType = input[1];
        esWriteIndex = args[2];
        esWriteType = "anomaly";

        Configuration conf = new Configuration();
        conf.setBoolean("mapreduce.map.speculative", false);
        conf.setBoolean("mapreduce.reduce.speculative", false);

        conf.set(ConfigurationOptions.ES_NODES, String.format("%s:%d", esMaster, 9200));
        conf.set(ConfigurationOptions.ES_INPUT_JSON, "yes");
        conf.set(ConfigurationOptions.ES_RESOURCE_READ, String.format("%s/%s/", esReadIndex, esReadType));
        conf.set(ConfigurationOptions.ES_RESOURCE_WRITE, String.format("%s/%s/", esWriteIndex, esWriteType));
        conf.set(ConfigurationOptions.ES_QUERY, "?q=cs_uri_query:*");

        /*
        try{
            initESMapping();
        }catch(Exception e){
            System.exit(-1);
        }*/

        Job job = Job.getInstance(conf);
        job.setJarByClass(QueryStringAnomalyDetection.class);

        job.setInputFormatClass(EsInputFormat.class);
        job.setOutputFormatClass(EsOutputFormat.class);

        job.setMapperClass(QSMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);

        //  job.setPartitionerClass(KeyPartitioner.class);
        job.setReducerClass(QSReducer.class);
        job.setNumReduceTasks(8);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new QueryStringAnomalyDetection(), args);
        System.exit(exitCode);
    }

    public static class QSMapper extends Mapper<Text, LinkedMapWritable, Text, DoubleWritable> {
        @Override
        protected void map(Text key, LinkedMapWritable value,
                           Context context) throws IOException, InterruptedException {
            //JSONObject json = new JSONObject(value.toString());
            String c_ip = value.get(AnomalyConstants.IP).toString();
            //过滤内网IP
            if (IPFilter.isInnerIP(c_ip)) {
                return;
            }
            Writable q = value.get(AnomalyConstants.uriQuery);
            if (q == null || value.get(AnomalyConstants.status) == null) {
                return;
            }
            Writable cs_host = value.get(AnomalyConstants.host);
            String csHost = "";
            if (cs_host != null) {
                csHost = cs_host.toString();
            }
            Writable uri_stem = value.get(AnomalyConstants.uriStem);
            String stem = "";
            if (uri_stem != null) {
                stem = uri_stem.toString();
            }

            String query = q.toString();

            // Split into param-value pairs
            //  double entropy = StringEntropyCalculator.calculate(query);

            String[] params = query.split("&");
            String pvalue = "";

            for (String param : params) {
                String[] nv = param.split("=");
                if (nv.length == 2) {
                    pvalue = nv[1];
                } else {
                    //RESTFULL 风格时，TODO 参数处理是否还有意义？
                    pvalue = nv[0];
                    nv[0] = "";
                }
                double entropy = StringEntropyCalculator.calculate(pvalue);
                //对于请求每个query 计算 值，分配给相应参数
                context.write(new Text(stem + "?" + nv[0]), new DoubleWritable(entropy));
              /*  if (nv.length > 1 && nv[1] != null && !nv[1].equals("")) {

                }*/


            }
          /*  int posNameStart = 0; // Move from the start pos of query string to the first name
                do {
                    int posValueEnd = query.indexOf('&', posNameStart);
                    if (posValueEnd < 0) posValueEnd = query.length();

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

                    context.write(new Text(json.getString("cs_uri_stem") + "?" + param), new DoubleWritable(entropy));

                    posNameStart = posValueEnd + 1;
                } while (posNameStart < query.length());*/

        }
    }

    public static class KeyPartitioner extends Partitioner<Text, Text> {
        static final Logger s_logger = Logger.getLogger(QSReducer.class);

        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            return key.toString().charAt(0) % numPartitions;
        }
    }

    public static class QSReducer extends Reducer<Text, DoubleWritable, NullWritable, Text> {
        static final Logger s_logger = Logger.getLogger(QSReducer.class);

        @Override
        protected void reduce(Text key, Iterable<DoubleWritable> values,
                              Context context) throws IOException, InterruptedException {
            String url = key.toString();
            int pos = url.lastIndexOf('?');
            if (pos < 0) return;/**/
            //使用 TreeMap 避免内存溢出
            Map<Double, Integer> data = new TreeMap<>(new Comparator<Double>() {
                @Override
                public int compare(Double zhis, Double that) {
                    return zhis.compareTo(that);
                }
            });
            int len = 0;
            for (DoubleWritable value : values) {
                len++;
                Double k = value.get();
                Integer v = data.get(k);
                v = (v == null ? 0 : v);
                data.put(k, ++v);
            }

            double upperFence = QuartileUtils.upperFence(data, len);
            try {
                JSONObject json = new JSONObject();
                json.put("cs_uri_stem", url.substring(0, pos));
                json.put("param", url.substring(pos + 1));
                json.put("upper", upperFence);
                s_logger.info(json.toString());
                context.write(NullWritable.get(), new Text(json.toString()));
            } catch (JSONException ex) {
                s_logger.error(ex);
            }
        }
    }
}
