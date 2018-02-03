package com.hansight.kunlun.analysis.statistics.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.log4j.Logger;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.mr.EsInputFormat;
import org.elasticsearch.hadoop.mr.EsOutputFormat;
import org.elasticsearch.hadoop.mr.LinkedMapWritable;

import com.hansight.kunlun.analysis.statistics.model.LogWritable;
import com.hansight.kunlun.utils.Pair;
import com.hansight.kunlun.analysis.utils.PropertiesUtils;


/**
 * Created by zhhuiyan on 5/15/2014.
 */
public class AnomalyWorkflow {
    static final Logger logger = Logger.getLogger(AnomalyDetection.class);
    private HashMap<Job, ControlledJob> jobs_;
    private ArrayList<Pair<Job, Job>> depends_;
    private String master;
    private String port;
    private String readIndex;
    private String readType;
    private String modelWriteIndex;
    private String modelWriteType = "anomaly";
    private String qsWriteIndex;
    private String qsWriteType = "anomaly";
    private String format;
    private String esTempIndex;

    JobControl jc;

    public AnomalyWorkflow(String chainName) {
        jc = new JobControl(chainName);
        jobs_ = new HashMap<>();
        depends_ = new ArrayList<>();
    }

    public void add(Job job) {
        jobs_.put(job, null);
    }

    public void mustAfter(Job job2, Job job1) {
        add(job1);
        add(job2);
        depends_.add(new Pair<Job, Job>(job1, job2));
    }

    public int runJobs() throws IOException {
        for (Map.Entry<Job, ControlledJob> entry : jobs_.entrySet()) {
            Job job = entry.getKey();
            ControlledJob ctrJob = new ControlledJob(job.getConfiguration());
            ctrJob.setJob(job);
            jc.addJob(ctrJob);
            entry.setValue(ctrJob);
        }
        for (Pair<Job, Job> entry : depends_) {
            Job job1 = entry.first();
            Job job2 = entry.second();
            jobs_.get(job2).addDependingJob(jobs_.get(job1));
        }
        Thread jcThread = new Thread(jc);
        jcThread.start();
        while (true) {
            if (jc.allFinished()) {
                logger.info(jc.getSuccessfulJobList());
                jc.stop();
                return 0;
            }
            if (jc.getFailedJobList().size() > 0) {
                logger.info(jc.getSuccessfulJobList());
                jc.stop();
                return 1;
            }
        }
    }

    /**
     * make ES Configuration
     *
     * @param args
     * @return
     */
    private Configuration getConf(String... args) {
        if (args.length < 5)
            throw new IllegalArgumentException("args is less must be five");
        Configuration conf = new Configuration();
        conf.setBoolean("mapreduce.map.speculative", false);
        conf.setBoolean("mapreduce.reduce.speculative", false);
        conf.set(ConfigurationOptions.ES_NODES, String.format("%s:%s", args[0], args[1]));
        conf.set(ConfigurationOptions.ES_INPUT_JSON, "yes");
        conf.set(ConfigurationOptions.ES_SCROLL_KEEPALIVE, "10m");
        conf.set(ConfigurationOptions.ES_RESOURCE_READ, String.format("%s/%s/", args[2], args[3]));
        conf.set(ConfigurationOptions.ES_RESOURCE_WRITE, String.format("%s/%s/", args[4], args[5]));
        //   conf.set(ConfigurationOptions.ES_QUERY, "?q=*");
        return conf;
    }

    public Job createSessionSplitterJob() throws Exception {
        Configuration conf = getConf(master, port, readIndex, readType, esTempIndex, modelWriteType);
        Job job = Job.getInstance(conf, "sessionSplitter");
        job.setJarByClass(AnomalyDetection.class);
        job.setInputFormatClass(EsInputFormat.class);
        job.setOutputFormatClass(EsOutputFormat.class);
        job.setMapperClass(AnomalyDetection.SessionMapper.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(LogWritable.class);
        job.setPartitionerClass(AnomalyDetection.FirstPartitioner.class);
        job.setSortComparatorClass(AnomalyDetection.KeyComparator.class);
        // job.setGroupingComparatorClass(AnomalyDetection.GroupComparator.class);
        // job.setCombinerClass(AnomalyDetection.SessionCombiner.class);
        job.setReducerClass(AnomalyDetection.SessionReducer.class);
        job.setNumReduceTasks(8);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        return job;
    }

    public Job createCounterJob() throws Exception {
        Configuration conf = getConf(master, port, esTempIndex, modelWriteType, modelWriteIndex, modelWriteType);
        conf.set(AnomalyModeling.ANOMALY_MODEL_FORMAT, format);
        Job job = Job.getInstance(conf, "Counter");
        job.setJarByClass(AnomalyModeling.class);
        job.setInputFormatClass(EsInputFormat.class);
        job.setOutputFormatClass(EsOutputFormat.class);
        job.setMapperClass(AnomalyModeling.SessionMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LinkedMapWritable.class);
       /* job.setPartitionerClass(FirstPartitioner.class);
        job.setSortComparatorClass(KeyComparator.class);
        job.setGroupingComparatorClass(GroupComparator.class);*/

        //  job.setCombinerClass(AnomalyModel.SessionCombiner.class);
        job.setNumReduceTasks(8);
        job.setReducerClass(AnomalyModeling.SessionReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        return job;
    }

    public Job createQueryStringAnomalyDetectionJob() throws Exception {
        Configuration conf = getConf(master, port, readIndex, readType, qsWriteIndex, qsWriteType);
        conf.set(ConfigurationOptions.ES_QUERY, "?q=cs_uri_query:*");
        Job job = Job.getInstance(conf, "QueryStringAnomalyDetector");
        job.setJarByClass(QueryStringAnomalyDetection.class);
        job.setInputFormatClass(EsInputFormat.class);
        job.setOutputFormatClass(EsOutputFormat.class);
        job.setMapperClass(QueryStringAnomalyDetection.QSMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        //  job.setPartitionerClass(QueryStringAnomalyDetector.KeyPartitioner.class);
        job.setReducerClass(QueryStringAnomalyDetection.QSReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
     /*   FileInputFormat.addInputPath(job, new Path("output/sessions"));
        FileOutputFormat.setOutputPath(job, new Path("output/queries"));*/

        return job;
    }

    private static AnomalyWorkflow init(String[] args) {
        AnomalyWorkflow workflow = new AnomalyWorkflow("anomalyWorkFlow");
        PropertiesUtils.loadFile("workflow.properties");
        String qs[] = PropertiesUtils.getValue("qs_output_index_type").toString().split("/");
        workflow.qsWriteIndex = qs[0];
        workflow.qsWriteType = qs[1];
        if (args.length < 4) {

            workflow.format = PropertiesUtils.getValue("count_time_format").toString();
        } else {
            workflow.format = args[3];
        }
        if (args.length < 3) {
            String[] oit = PropertiesUtils.getValue("output_index_type").toString().split("/");
            workflow.modelWriteIndex = oit[0];
            workflow.modelWriteType = oit[1];
        } else {
            String[] input = args[2].split("/");
            if (input.length > 1) {
                workflow.modelWriteType = input[1];
            }
            workflow.modelWriteIndex = input[0];
        }
        if (args.length < 2) {
            String[] iit = PropertiesUtils.getValue("input_index_types").toString().split("/");
            workflow.readIndex = iit[0];
            workflow.readType = iit[1];
        } else {
            String[] input = args[1].split("/");
            if (input.length < 2) {
                throw new IllegalArgumentException("args is less,read source  must be inputIndex1,inputIndex2/type ");
            }
            workflow.readIndex = input[0];
            workflow.readType = input[1];
        }

        if (args.length < 1) {
            String[] oit = PropertiesUtils.getValue("host_port").toString().split("/");
            workflow.master = oit[0];
            workflow.port = oit[1];
        } else {
            String[] oit = args[0].split("/");
            if (oit.length < 2) {
                throw new IllegalArgumentException("args is less,read source  must be inputIndex1,inputIndex2/type ");
            }
            workflow.master = oit[0];
            workflow.port = oit[1];
        }
        workflow.esTempIndex = "model_middle";
        return workflow;
    }

    /**
     * master:ip readIndex/type outIndex datetime
     *
     * @param args ip readIndex/type outIndex countTimeFormat
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        AnomalyWorkflow workflow = init(args);
        Job job10 = workflow.createSessionSplitterJob();
        Job job21 = workflow.createCounterJob();
        Job job11 = workflow.createQueryStringAnomalyDetectionJob();
        workflow.mustAfter(job21, job10);
        workflow.add(job11);
        //   workflow.mustAfter(job22, job10);
        workflow.runJobs();
    }

}
