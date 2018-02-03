package com.hansight.kunlun.analysis.statistics.model;


import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author takeshi
 */
//To Do : binary comparator
public class TimeIPPair implements WritableComparable<TimeIPPair>,Serializable {

    private long ip;
    private long time;

    public TimeIPPair() {
        this.ip = 0l;
        this.time = 0l;
    }

    public TimeIPPair(long time, long ip) {
        set(time, ip);
    }

    public TimeIPPair(TimeIPPair p) {
        set(p.getTime(), p.getIp());
    }

    public void set(long time, long ip) {
        this.time = time;
        this.ip = ip;
    }

    public long getIp() {
        return ip;
    }

    public long getTime() {
        return time;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(time);
        out.writeLong(ip);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        time = in.readLong();
        ip = in.readLong();
    }

    @Override
    public int hashCode() {
        return ((int) time * 163 + (int) ip);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TimeIPPair) {
            TimeIPPair ip = (TimeIPPair) o;
            return this.ip == ip.ip && time == ip.time;
        }
        return false;
    }

    @Override
    public String toString() {
        return time + "_" + ip;
    }

      @Override
    public int compareTo(TimeIPPair p) {
        if (p == null) {
            return -1;
        }
        int cmp = compare(this.time, p.time);
        if (cmp != 0) {
            return cmp;
        }
        return compare(this.ip, p.ip);
    }

    /**
     * Convenience method for comparing two long /long + int.
     */

    public static int compare(long a, long b) {
        return (a < b ? -1 : (a == b ? 0 : 1));
    }

}
