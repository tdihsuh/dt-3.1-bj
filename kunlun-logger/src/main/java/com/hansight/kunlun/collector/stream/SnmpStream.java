package com.hansight.kunlun.collector.stream;

import com.google.gson.Gson;
import com.hansight.kunlun.collector.common.model.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhhui on 2014/11/5.
 */
public class SnmpStream extends Stream<ByteBuffer> {
    protected final static Logger logger = LoggerFactory.getLogger(SnmpStream.class);
    private TransportMapping<?> transport;
    private Snmp snmp;
    private Queue<ByteBuffer> queue;


    public SnmpStream(TransportMapping<?> transport, Snmp snmp, final int cacheSize) {
        this.transport = transport;
        this.snmp = snmp;
        queue = new ArrayBlockingQueue<>(cacheSize+1);
        this.snmp.addCommandResponder(new CommandResponder() {
            private Gson gson = new Gson();
            @Override
            public void processPdu(CommandResponderEvent snmpEvent) {
                PDU command = snmpEvent.getPDU();
                if (command != null) {
                    Vector<? extends VariableBinding> recVBs = command
                            .getVariableBindings();
                    if(recVBs!=null){
                        try {
                            while (!queue.offer(trans(recVBs))){
                                TimeUnit.MILLISECONDS.sleep(100);
                            }
                        } catch (Exception e) {
                            logger.error(" message process error:{}", e);
                        }
                    }
                }
            }

            // translate to JSON
            private ByteBuffer trans(Vector<? extends VariableBinding> recVBs) {
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < recVBs.size(); i++) {
                    VariableBinding recVB = recVBs.elementAt(i);
                    if(recVB!=null && recVB.getOid()!=null)
                    map.put(recVB.getOid().toString(), recVB.toValueString());
                }
                String dist = gson.toJson(map);
                return ByteBuffer.wrap(dist.getBytes());
            }
        });

        try {
            this.transport.listen();
        } catch (IOException e) {
            logger.error(" transport listen error:{}", e);
        }
    }

    @Override
    public void close() throws IOException {
        snmp.close();
        transport.close();
    }

    @Override
    public boolean hasNext() {
        while ((item=queue.poll())==null){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            return true;
    }
}
