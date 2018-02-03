package com.hansight.kunlun.analysis.statistics.model;


/**
 * @author takeshi
 */
//To Do : binary comparator
public class SessionId {

    private long ip;
    private long cookeId;

    public SessionId() {
    }

    public SessionId(long ip, long cookeId) {
        set(ip, cookeId);
    }

    public SessionId(SessionId p) {
        set(p.getIp(), p.getCookeId());
    }

    public void set(long ip, long cookeId) {
        this.ip = ip;
        this.cookeId = cookeId;
    }

    public long getIp() {
        return ip;
    }

    public long getCookeId() {
        return cookeId;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }

    public void setCookeId(long cookeId) {
        this.cookeId = cookeId;
    }


    @Override
    public int hashCode() {
        return (int) cookeId * 163 + (int) ip;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SessionId) {
            SessionId ip = (SessionId) o;
            return this.ip == (ip.ip) && cookeId == (ip.cookeId);
        }
        return false;
    }

    @Override
    public String toString() {
        return cookeId + "_" + ip;
    }


    /**
     * Convenience method for comparing two long /long + int.
     */

    public static int compare(long a, long b) {
        return (a < b ? -1 : (a == b ? 0 : 1));
    }
}
