package com.hansight.kunlun.collector.reader;

import com.hansight.kunlun.collector.stream.SnmpStream;
import com.hansight.kunlun.collector.common.base.LogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class SnmpLogReader implements LogReader<ByteBuffer> {
    final static Logger logger = LoggerFactory.getLogger(SnmpLogReader.class);
    private String path;
    SnmpStream stream;


    public SnmpLogReader(String protocol, String host, int port, String version, String... auths) {
        TransportMapping<?> transport=null;
        try {

            switch (protocol) {
                case "tcp":
                    transport = new DefaultTcpTransportMapping(new TcpAddress(host
                            + "/" + port));
                    break;
                case "udp":
                    transport = new DefaultUdpTransportMapping(new UdpAddress(host
                            + "/" + port));
                    break;
                default:
                    throw new IOException("unexpected protocol:" + protocol);
            }
        } catch (IOException e) {
            logger.error("I/O:{}",e);
        }
        ThreadPool threadPool = ThreadPool.create("snmptrap-pool", 2);
        MessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(
                threadPool, new MessageDispatcherImpl());


        Snmp snmp = new Snmp(dispatcher, transport);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
        if ("v3".equals(version)) {
            USM usm = new USM(
                    SecurityProtocols.getInstance().addDefaultProtocols(),
                    new OctetString(MPv3.createLocalEngineID()), 0);
            usm.setEngineDiscoveryEnabled(true);
            snmp.getMessageDispatcher()
                    .addMessageProcessingModel(new MPv3(usm));
            SecurityModels.getInstance().addSecurityModel(usm);
            String user = auths[0];
            String authKey = auths[1];
            String privateKey = auths[2];
            String authProtocol = auths[3];
            String privateProtocol = auths[4];
            OID auth = get(authProtocol);
            OID oid = get(privateProtocol);
            snmp.getUSM().addUser(
                    new OctetString(user),
                    new UsmUser(new OctetString(user), auth, new OctetString(
                            authKey), oid, new OctetString(privateKey)));
        }
        path = protocol + "://" + host + ":" + port;
        stream = new SnmpStream(transport, snmp, 5000);
    }

    private OID get(String key) {
        OID oid = null;
        switch (key) {
            case "sha":
                oid = AuthSHA.ID;
                break;
            case "md5":
                oid = AuthMD5.ID;
                break;
            case "des":
                oid = PrivDES.ID;
                break;
            case "aes128":
                oid = PrivAES128.ID;
                break;
            case "aes192":
                oid = PrivAES192.ID;
                break;
            case "aes256":
                oid = PrivAES256.ID;
                break;
        }
        return oid;
    }

    @Override
    public long skip(long skip) {
        return 0;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Iterator<ByteBuffer> iterator() {
        return stream;
    }

    @Override
    public void close() throws IOException {

        stream.close();
       }
}