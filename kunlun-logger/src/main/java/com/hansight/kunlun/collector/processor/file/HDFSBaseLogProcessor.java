package com.hansight.kunlun.collector.processor.file;

import com.hansight.kunlun.collector.common.exception.LogProcessorException;
import com.hansight.kunlun.collector.reader.FileLogReader;
import com.hansight.kunlun.utils.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Author:zhhui
 * DateTime:2014/7/29 15:55.
 * reader from need  hadoop fs -chmod 777 /user/hadoop
 */
public class HDFSBaseLogProcessor<F> extends FileLogProcessor<F> {
    final static Logger logger = LoggerFactory.getLogger(HDFSBaseLogProcessor.class);
    private static FileSystem fs;
    private Path path;
    public Map<Path, Monitor> monitorPaths = new ConcurrentHashMap<>();
    public ExecutorService monitorPool = Executors.newCachedThreadPool();
    private boolean starting = true;

    @Override
    public void setup() throws LogProcessorException {
        super.setup();

        String pathName = conf.get("uri");
        logger.debug("HDFS PATH:{}", pathName);
        String[] shame = pathName.split("//");
        String[] url = shame[1].split("/");
        String name = shame[0] + "//" + url[0] + "/";
        Configuration config = new Configuration();
        URI uri = URI.create(name);

        path = new Path(pathName.substring(name.length() - 1));
        try {
            fs = FileSystem.get(uri, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        starting = true;
        monitorPool.execute(new Monitor(path, true));
    }
     /**
     * @throws com.hansight.kunlun.collector.common.exception.LogProcessorException
     */
    @Override
    public void cleanup() throws LogProcessorException {
        //nothing tod logger.debug("cache stop" + conf.getId());
    }


    class Monitor extends FileHandler<F> implements Runnable {
        private Path path;
        private boolean first = false;
        private Map<Path, Pair<Long, Long>> fileStatus = new HashMap<>();

        @SuppressWarnings("unchecked")
        private FileLogReader<F> handle(Path path, boolean first, boolean isCreate) {
            String key = conf.getId() + "_" + path.toString();
            FileLogReader<F> reader=null;
            try {
                if (!inProcess.contains(key)) {
                    inProcess.add(key);
                    FileStatus status= fs.getFileStatus(path);
                    reader= handle(conf, store, lexer, fs.open(path), path.toString(),status.getModificationTime()+status.getLen(),first, isCreate);
                    if(reader==null){
                        inProcess.remove(key);
                    }
                }
            } catch (IOException e) {
                logger.error("HANDLE FILE ERROR FileNotFound :{}", e);
            }
            return reader;
        }

        private Pair<Long, Long> mkStatus(Path path) {
            try {
                if (fs.isFile(path)) {
                    FileStatus status = fs.getFileStatus(path);
                    return new Pair<>(status.getModificationTime(), status.getLen());
                }
            } catch (IOException e) {
                logger.error("IOException ERROR:{}", e);
            }
            return null;
        }

        Monitor(Path path, boolean first) {
            this.path = path;
            this.first = first;
            monitorPaths.put(path, this);
        }

        /**
         * monitor an path that
         * when  it is file handle it
         * else if is directory check sub file
         * when sub is directory monitor it
         * else add sub status
         *
         * @param path
         */
        protected void monitor(Path path) {
            first = false;
            try {
                if (fs.isDirectory(path)) {
                    if (!monitorPaths.containsKey(path)) {
                        Monitor monitor = new Monitor(path, false);
                        monitorPool.execute(monitor);
                    }
                    RemoteIterator<LocatedFileStatus> iterator = fs.listFiles(path, true);
                    while (iterator.hasNext()) {
                        LocatedFileStatus status = iterator.next();
                        logger.debug("monitor new path:{}", status.getPath());
                        monitor(status.getPath());
                    }
                } else if(!path.toString().endsWith("_COPYING_")){
                    fileStatus.put(path, mkStatus(path));
                    sleep();
                    FileLogReader<F> reader;
                    if (starting) {
                        reader = handle(path, true, false);
                    } else {
                        reader = handle(path, false, true);
                    }
                    if (reader != null)
                        readers.add(reader);
                }
            } catch (IOException e) {
                logger.error("IOException ERROR:{}", e);
            }
        }

        public void check() throws IOException, InterruptedException {

            FileStatus status = fs.getFileStatus(path);
            if (status == null) {
                return;
            }
            if (fs.isFile(path)) {
                Pair<Long, Long> modifies = fileStatus.get(path);
                if (modifies.first() < status.getModificationTime() && modifies.second() < status.getLen()) {
                    FileLogReader<F> reader = handle(path, false, false);
                    if (reader != null) {
                        readers.add(reader);
                    }
                    fileStatus.put(path, mkStatus(path));
                }

            } else if (fs.isDirectory(path)) {
                //  monitor(path);
                RemoteIterator<LocatedFileStatus> iterator = fs.listFiles(path, true);
                while (iterator.hasNext()) {
                    status = iterator.next();
                    Path file = status.getPath();

                    Pair<Long, Long> modifies = fileStatus.get(file);
                    if (modifies == null) {
                        logger.info("monitor find a new  file:{}", status.getPath());
                        if (fs.isFile(file)) {
                            FileLogReader<F> reader = handle(file, true, true);
                            if (reader != null) {
                                readers.add(reader);
                            }
                            fileStatus.put(file, mkStatus(file));
                        } else {
                            monitor(file);
                        }

                    } else if (modifies.first() != status.getModificationTime() || modifies.second() < status.getLen()) {
                        logger.info("monitor find a  file :{} ,status:modification", status.getPath());
                        FileLogReader<F> reader = handle(file, false, false);
                        if (reader != null) {
                            readers.add(reader);
                        }
                        fileStatus.put(file, mkStatus(file));
                    }
                    //    monitor(status.getPath());
                }
            }
        }

        /*
         *cache to reader is double pool size，when find more then this file waiting for process
         */
        private void sleep() {
            while (running && readers.size() >= 2 * poolSize) {
                try {
                    TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
                    logger.debug("monitor : waiting {} ms for file  process ", PROCESSOR_THREAD_WAIT_TIMES);
                } catch (InterruptedException e) {
                    logger.error("INTERRUPTED ERROR:{}", e);
                }
            }
        }

        @Override
        public void run() {
            if (first) {
                monitor(path);
                starting = false;
            }

            while (running && monitorPaths.size() > 0) {
                try {
                    check();
                    TimeUnit.MICROSECONDS.sleep(100* PROCESSOR_THREAD_WAIT_TIMES);
                } catch (InterruptedException e) {
                    logger.error("INTERRUPTED ERROR:{}", e);
                } catch (IOException e) {
                    logger.error("IO EXCEPTION ERROR:{}", e);
                }

            }
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
         TimeUnit.SECONDS.sleep(5);
        monitorPaths.clear();
        if (path != null)
            path = null;
        try {
            if (fs != null) {
                fs.close();
            }
        } catch (IOException e) {
            throw new LogProcessorException("FS CLOSE ERROR:", e);
        }
    }
}
