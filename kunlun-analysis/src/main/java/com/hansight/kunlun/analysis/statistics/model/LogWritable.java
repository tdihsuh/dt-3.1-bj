package com.hansight.kunlun.analysis.statistics.model;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Author: zhhui
 * Date: 2014/7/2
 */
public class LogWritable implements Writable, Serializable {
    private long cookieId;
    private long cip;
    private long time;
    private int status;
    private String url;
    private String userAgent;

    public LogWritable() {
        super();
    }

    public LogWritable(LogWritable log) {
        super();
        this.cip = log.getCip();
        this.cookieId = log.getCookieId();
        this.time = log.getTime();
        this.status = log.getStatus();
        this.url = log.getUrl();
        this.userAgent = log.getUserAgent();
    }

    /**
     * @param w1
     * @param w2
     * @return
     */
    public static boolean equal(Object w1, Object w2) {
        if (w1 == null || w2 == null)
            return w1 == null && w2 == null;
        return w1.equals(w2);

    }

    public LogWritable(long cookieId, long cip, long time, int status, String url, String userAgent) {
        this.cookieId = cookieId;
        this.cip = cip;
        this.time = time;
        this.status = status;
        this.url = url;
        this.userAgent = userAgent;
    }

    public long getCookieId() {
        return cookieId;
    }

    public void setCookieId(long cookieId) {
        this.cookieId = cookieId;
    }

    public long getCip() {
        return cip;
    }

    public void setCip(long cip) {
        this.cip = cip;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(cookieId);
        out.writeLong(cip);
        out.writeLong(time);
        out.writeInt(status);
        //  out.writeBytes(url);
        if (url == null)
            out.writeUTF("");
        else
            out.writeUTF(url);
        if (userAgent == null)
            out.writeUTF("");
        else
            out.writeUTF(userAgent);

    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.cookieId = in.readLong();
        cip = in.readLong();
        time = in.readLong();
        status = in.readInt();
        url = in.readUTF();
        if ("".equals(url))
            url = null;
        userAgent = in.readUTF();
        if ("".equals(userAgent))
            userAgent = null;

    }

    @Override
    public String toString() {
        return "LogWritable{" +
                "cookieId=" + cookieId +
                ", cip=" + cip +
                ", time=" + time +
                ", status=" + status +
                ", url='" + url + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
