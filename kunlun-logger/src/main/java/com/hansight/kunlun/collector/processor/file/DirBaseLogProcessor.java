package com.hansight.kunlun.collector.processor.file;

import com.hansight.kunlun.collector.common.exception.LogProcessorException;
import com.hansight.kunlun.collector.reader.FileLogReader;
import com.hansight.kunlun.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Author:zhhui
 * DateTime:2014/7/29 15:55.
 */
public class DirBaseLogProcessor<F> extends FileLogProcessor<F> {
    final static Logger logger = LoggerFactory.getLogger(DirBaseLogProcessor.class);
    public ExecutorService monitorPool = Executors.newFixedThreadPool(1);
    private Monitor monitor = null;

    @Override
    public void setup() throws LogProcessorException {
        super.setup();
        String pathName = conf.get("uri");
        logger.info("dir path :{}", pathName);
        monitor = new Monitor(Paths.get(pathName));
        monitorPool.submit(monitor);


    }

    /**
     * @throws com.hansight.kunlun.collector.common.exception.LogProcessorException
     */
    @Override
    public void cleanup() throws LogProcessorException {
        // store.close();
    }


    class Monitor extends FileHandler implements Runnable {
        private Path path;
        private Boolean flag = true;
        private WatchService watcher = null;
        private boolean starting = true;
        public Map<WatchKey, Pair<Path, Set<Path>>> monitorPaths = new ConcurrentHashMap<>();
        public Map<Path, WatchKey> pathWatchKeyMap = new ConcurrentHashMap<>();
        private volatile boolean done = false;

        @SuppressWarnings("unchecked")
        WatchEvent<Path> cast(WatchEvent<?> event) {
            return (WatchEvent<Path>) event;
        }

        {
            try {
                watcher = FileSystems.getDefault().newWatchService();
            } catch (IOException e) {
                logger.error("FILE MONITOR INIT ERROR:{}", e);
            }
        }

        public void stop() throws IOException {

            if (!done) {
                this.flag = false;
            }
            monitorPaths.clear();
            if (watcher != null)
                watcher.close();
        }

        Monitor(Path path) {
            this.path = path;
        }

        private void sleep() {
            while (readers.size() >= 2 * poolSize) {
                try {
                    TimeUnit.MILLISECONDS.sleep(PROCESSOR_THREAD_WAIT_TIMES);
                    logger.debug("monitor : waiting {} ms for file  process ", PROCESSOR_THREAD_WAIT_TIMES);
                    if (!running) {
                        return;
                    }
                } catch (InterruptedException e) {
                    logger.error("INTERRUPTED ERROR:{}", e);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private FileLogReader<F> handle(Path path, boolean first, boolean isCreate) {
            String key = conf.getId() + "_" + path.toString();
            FileLogReader<F> reader = null;
            try {
                if (!inProcess.contains(key)) {
                    logger.info(" handler  process  mate data  with file:{}",path.toString());
                    inProcess.add(key);

                    reader = handle(conf, store, lexer, new BufferedInputStream(Files.newInputStream(path)), path.toString(),Files.getLastModifiedTime(path).toMillis(), first, isCreate);
                    if (reader == null) {
                        inProcess.remove(key);
                    }
                }
            } catch (FileNotFoundException e) {
                logger.error("ERROR FileNotFoundException :{}", e);
            } catch (IOException e) {
                logger.error("ERROR IOException :{}", e);
            }
            return reader;
        }

        @SuppressWarnings("unchecked")
        protected void monitor(Path path) {
            register(path);
            if (Files.isDirectory(path)) {
                try ( DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                    for (Path sub : directoryStream) {
                        monitor(sub);
                    }
                } catch (IOException e) {
                    logger.error("ERROR IOException :{}", e);
                }
            } else {
                sleep();

                FileLogReader<F> reader = handle(path, starting, !starting);
                if (reader != null)
                    readers.add(reader);
            }
        }


        private void register(Path path) {
            Pair<Path, Set<Path>> monitor = null;
            WatchKey key;
            if (Files.isDirectory(path)) {
                key = pathWatchKeyMap.get(path);
                if (key == null) {
                    monitor = new Pair<>(path, null);
                }
            } else {
                Path parent = path.getParent();
                key = pathWatchKeyMap.get(parent);
                if (key == null) {
                    monitor = new Pair<Path, Set<Path>>(parent, new HashSet<Path>());
                } else {
                    monitor = monitorPaths.get(key);
                }
                if (monitor.second() != null) {
                    monitor.second().add(path);
                }
                path = parent;
            }
            if (key == null) {
                try {
                    key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    monitorPaths.put(key, monitor);
                    pathWatchKeyMap.put(path, key);

                } catch (IOException e) {
                    logger.error("Monitor register watcher error:{}", e);
                }
            }
        }

        @Override
        public void run() {
            monitor(path);
            starting = false;
            while (flag) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    logger.error("Monitor IOException:{}", e);
                    continue;
                }
                Pair<Path, Set<Path>> monitor = monitorPaths.get(key);
                List<WatchEvent<?>> events = key.pollEvents();
                if (!events.isEmpty()) {
                    for (WatchEvent<?> event : events) {
                        WatchEvent<Path> evt = cast(event);
                        WatchEvent.Kind<Path> kind = evt.kind();
                        Path name = evt.context();
                        Path child = monitor.first().resolve(name);
                        if (monitor.second() == null || monitor.second().contains(child)) {
                            if (kind.equals(ENTRY_DELETE)) {
                                logger.debug("delete path:{}",child);
                                WatchKey child_key = pathWatchKeyMap.remove(child);
                                if (child_key != null){
                                    monitorPaths.remove(child_key);
                                  }
                            }
                            boolean create = kind.equals(ENTRY_CREATE);
                            boolean directory= Files.isDirectory(child);
                            if (directory ) {
                                if(create){
                                    logger.debug("create path:{}", child);
                                    monitor(child);
}
                            }else{
                                if ((create || kind.equals(ENTRY_MODIFY))) {
                                    logger.debug(" {} file  on path:{}",kind.name(),child);
                                    sleep();
                                    FileLogReader<F> reader = handle(child, false, create);
                                    if (reader != null) {
                                        readers.add(reader);
                                    }
                                }
                            }

                        }
                    }
                    key.reset();
                }
            }
            done = true;
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        logger.info("monitor stopping");
        if (monitor != null) {
            monitor.stop();
        }
        logger.info("monitor stopped");
    }

}
