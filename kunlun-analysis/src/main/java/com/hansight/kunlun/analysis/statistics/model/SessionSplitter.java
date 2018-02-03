package com.hansight.kunlun.analysis.statistics.model;

import com.hansight.kunlun.analysis.utils.PropertiesUtils;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

public class SessionSplitter {
    public SessionSplitter() {
        idFields_ = new ArrayList<>(4);
        PropertiesUtils.loadFile("session_splitter.properties");
        Properties properties = PropertiesUtils.getDatas();
        for (Object entry : properties.values()) {
            String[] vale = entry.toString().split("//");
            setIDFields(Boolean.parseBoolean(vale[1]), vale[0]);
        }
    }

    protected final ArrayList<FieldPattern> idFields_;

    /**
     * accepted fields:
     * <p/>
     * c-ip
     * or
     * cs(Cookie)/id=([0-9.]*-[0-9]*)
     */
    public void setIDFields(boolean rough, String... fieldPatterns) {
        idFields_.ensureCapacity(idFields_.size() + fieldPatterns.length);

        for (String fieldPattern : fieldPatterns)
            idFields_.add(FieldPattern.Parse(rough, fieldPattern));
    }

    public String getCookieId(String cookies) {
        String id = null;
        try {
            id = getIds(null, cookies)[1];
        } catch (UnknownHostException e) {
            id = null;
        }

        return id;
    }

    public String[] getIds(String ip, String cookies) throws UnknownHostException {
        String[] ids = new String[2];
        StringBuilder cookie = new StringBuilder();
        for (FieldPattern idField : idFields_) {
            String id = (idField.pattern_ == null) ? ip : (cookies == null ? null : idField.match(cookies));

            if (id != null) {
                if (idField.rough_) {
                    ids[0] = id;
                } else {
                    if (cookie.length() > 0)
                        cookie.append('_');
                    cookie.append(id);
                }
            }
        }
        if (cookie.length() > 0)
            ids[1] = cookie.toString();

        return ids;
    }

    public SessionId getSessionId(String ip, String cookies) throws UnknownHostException {
        SessionId sessionId = new SessionId();
        StringBuilder cookie = new StringBuilder();
        for (FieldPattern idField : idFields_) {
            String id = (idField.pattern_ == null) ? ip : (cookies == null ? null : idField.match(cookies));

            if (id != null) {
                if (idField.rough_) {
                    InetAddress ipaddr = InetAddress.getByName(id);
                    long clientIP = 0;
                    for (byte b : ipaddr.getAddress())
                        clientIP = clientIP << 8 | (b & 0xFF);

                    sessionId.setIp(clientIP);
                } else {
                    if (cookie.length() > 0)
                        cookie.append('_');
                    cookie.append(id);
                }
            }
        }
        if (cookie.length() > 0)
            sessionId.setCookeId(cookie.toString().hashCode());
        return sessionId;
    }

    public SessionId getSessionId(MapWritable log) throws UnknownHostException {
        SessionId sessionId = new SessionId();
        if (log == null)
            return null;
        StringBuilder cookie = new StringBuilder();
        for (FieldPattern idField : idFields_) {
            Writable value = log.get(new Text(idField.field_));
            if (value == null) continue;

            String id = (idField.pattern_ == null) ? value.toString() : idField.match(value.toString());

            if (id != null) {
                if (idField.rough_) {
                    InetAddress ipaddr = InetAddress.getByName(id);
                    long clientIP = 0;
                    for (byte b : ipaddr.getAddress())
                        clientIP = clientIP << 8 | (b & 0xFF);

                    sessionId.setIp(clientIP);
                } else {
                    if (cookie.length() > 0)
                        cookie.append('_');
                    cookie.append(id);
                }
            }
        }
        if (cookie.length() > 0)
            sessionId.setCookeId(cookie.toString().hashCode());
        return sessionId;
    }

    public Log parseLine(String line) throws IOException {
        String[] fields_ = new String[0];
        String[] values_ = new String[0];
        if (line.startsWith("#")) {
            if (line.startsWith("#Fields: ")) {
                fields_ = line.substring("#Fields: ".length()).trim().split(" ");
            }

            return null;
        }

        values_ = line.split(" ");

        if (fields_ == null || fields_.length != values_.length)
            throw new IOException("Wrong format");

        for (int i = 0; i < values_.length; i++) {
            if (values_[i].equals("-")) values_[i] = null;
        }

        String[] ids = getCurrentIDs(fields_, values_);

        return new Log(ids[0], ids[1], fields_, values_);
    }

    public String[] getCurrentIDs(String[] fields_, String[] values_) {
        StringBuffer roughIDs_ = new StringBuffer();
        StringBuffer preciseIDs_ = new StringBuffer();
        roughIDs_.setLength(0);
        preciseIDs_.setLength(0);

        for (FieldPattern idField : idFields_) {
            String value = Log.GetFieldValue(idField.field_, fields_, values_);
            if (value == null) continue;

            String id = (idField.pattern_ == null) ? value : idField.match(value);

            if (id != null) {
                if (idField.rough_) {
                    if (roughIDs_.length() == 0) //roughIDs_.append('_');
                        roughIDs_.append(id);
                } else {
                    if (preciseIDs_.length() == 0)// preciseIDs_.append('_');
                        preciseIDs_.append(id);
                }
            }
        }

        return new String[]{roughIDs_.toString(), preciseIDs_.toString()};
    }
}