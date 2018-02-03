package com.hansight.kunlun.collector.processor.file;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * Created by zhhuiyan on 14/12/22.
 */
public class WatchServerTest {
    @Test
    public  void test() throws IOException {
        Path path
                = Paths.get("/Users/zhhuiyan/workspace/data/temp");
        System.out.println("Now watching the current directory ...");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            //给path路径加上文件观察服务
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
           // start an infinite loop
            while (true) {
                // retrieve and remove the next watch key
                final WatchKey key = watchService.take();
                // get list of pending events for the watch key
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    // get the kind of event (create, modify, delete)
                    final WatchEvent.Kind<?> kind = watchEvent.kind();
                    // handle OVERFLOW event
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    //创建事件
                    if(kind == StandardWatchEventKinds.ENTRY_CREATE){

                    }
                    //修改事件
                    if(kind == StandardWatchEventKinds.ENTRY_MODIFY){

                    }
                    //删除事件
                    if(kind == StandardWatchEventKinds.ENTRY_DELETE){

                    }
                    // get the filename for the event
                    final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                    final Path filename = watchEventPath.context();
                    // print it out
                    System.out.println(kind + " -> " + filename);

                }
                // reset the keyf
                boolean valid = key.reset();
                // exit loop if the key is not valid (if the directory was
                // deleted, for
                if (!valid) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
