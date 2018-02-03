package com.hansight.kunlun.collector.position.store;

import com.hansight.kunlun.collector.common.model.ReadPosition;
import com.hansight.kunlun.coordinator.config.ConfigException;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhhuiyan on 14/11/29.
 */
public class SQLiteStoreTest {
    protected static ExecutorService threadPool= Executors.newCachedThreadPool();
    private static boolean running =true;
    
    static class Executor extends  Thread{
        private String  name;
        SQLiteReadPositionStore store;

        private boolean finished=false;
        public Executor(SQLiteReadPositionStore store, String name) {
          this.store=store;
            this.name=name;
        }

        @Override
        public void run() {
            ReadPosition position=store.get(name);
            if(position==null){
                position=new ReadPosition(name,0l,0l);
            }
            System.out.println(name+".records:" + position.records());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 1000_000_000; i++) {

                position.recordAdd();
                position.positionAdd(1);
                store.set(position);
                System.out.println(getName() +"."+name+":"+ position.records());
                //  store.flush();
                if(!running){
                    break;
                }

            }
            try {
                synchronized (store){
                    store.set(position);
                    store.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            finished=true;
            System.out.println(name+" finished records:"+position.records());
        }

        public boolean isFinished() {
            return finished;
        }
    }
    public static void main(String[] args) throws InterruptedException {

        final List<Executor> executors=new ArrayList<>();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            boolean  isFinished(){
                for (Executor executor :executors) {
                        if(!executor.isFinished())
                        return false;
                }
                return true;
            }
            @Override
            public void run() {
                super.run();
                System.out.print("Shutdown callback is invoked.");
                running=false;
                while (!isFinished()){
                    try {
                        System.out.println("waiting  for finished ");
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
     final   SQLiteReadPositionStore store=new SQLiteReadPositionStore();
        store.init();
        store.setCacheSize(20);
      final   String [] names={"test1","test2","test3","test4","test5"};
        for (int i = 0; i <5 ; i++) {
            Executor executor=new Executor(store,names[i]);
            executors.add(executor);
       threadPool.execute(executor);
        }
        while(true) TimeUnit.SECONDS.sleep(3);

    }

}
