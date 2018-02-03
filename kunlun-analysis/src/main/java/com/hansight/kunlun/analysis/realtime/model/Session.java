package com.hansight.kunlun.analysis.realtime.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hansight.kunlun.analysis.realtime.single.RTHandler;
import com.hansight.kunlun.analysis.utils.quantile.QDigest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class Session {

    private String id = null; // session id uuid, automate generate

    private final String c_ip;
    private String cookieId;
    private String userAgent;

    private final long startTimestamp;

    private long stopTimestamp;

    private final static long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // default
    // 30min

    private long timeout = -1L;

    private Map<Integer, Long> statusCounterMap = Maps
            .newConcurrentMap();

    private Map<String, Long> urlCounterMap = Maps
            .newConcurrentMap();

    // use for avoid check ANOMALY again while not access in
    private boolean hasDataEnter = false; // has data in , need set true
    // otherwise false
    private long total;

    private Set<String> indices = null;

    private Map<String, String> servers = null;

    private Map<String, AnomalyStatus> ANOMALYOccur = null;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public Session(String c_ip) {
        this(c_ip, System.currentTimeMillis());
    }

    public Session(String c_ip, long startTimestamp) {
        this(c_ip, startTimestamp, DEFAULT_TIMEOUT);
    }

    public Session(String c_ip, long startTimestamp, long timeout) {
        this(c_ip, startTimestamp, timeout, null);
    }

    public Session(String c_ip, long startTimestamp, String cookieId) {
        this(c_ip, startTimestamp, DEFAULT_TIMEOUT, cookieId);
    }

    public Session(String c_ip, long startTimestamp, long timeout, String cookieId, String userAgent) {

        super();

        this.c_ip = c_ip;
        this.id = UUID.randomUUID().toString();
        this.startTimestamp = startTimestamp;
        this.cookieId = cookieId;
        this.userAgent = userAgent;
        this.timeout = timeout;
        this.indices = Sets.newHashSet();
        this.servers = Maps.newHashMap();
        this.ANOMALYOccur = Maps.newConcurrentMap();

        statusCounterMap.put(RTConstants.COUNTER_KEY_STATUS_404,
                0l);
        statusCounterMap.put(RTConstants.COUNTER_KEY_STATUS_500,
                0l);
    }

    public Session(String c_ip, long startTimestamp, long timeout, String cookieId) {
        this(c_ip, startTimestamp, timeout, cookieId, null);
    }


    public boolean isTimeExpire() {
        return (Calendar.getInstance().getTimeInMillis() - stopTimestamp) >= timeout;
    }

    public boolean needNewSession(String cookieId, String userAgent) {
        if (this.cookieId == null || "".equals(this.cookieId)) {
            if (this.userAgent != null && this.userAgent.equals(userAgent)) {
                this.cookieId = cookieId;
                return false;
            } else {
                return true;
            }
        }

        return !this.cookieId.equals(cookieId);
    }

    public void addAccess(EnhanceAccess access) {
        long lastTime = Calendar.getInstance().getTimeInMillis();
        RTHandler.lastFetch.set(lastTime);
        setHasDataEnter(true);
        getIndices().add(access.getIndex());
        getServers().put(access.s_ip, access.s_computer_name);
        String url = access.toURL();
        Long urlCount = urlCounterMap.get(url);
        urlCounterMap.put(url, urlCount == null ? 1 : urlCount + 1);
        Long statusCount = statusCounterMap.get(access.sc_status);
        statusCounterMap.put(access.sc_status, statusCount == null ? 1 : statusCount + 1);
        this.stopTimestamp = lastTime;
        this.total++;
    }

    public long getTotal() {
        return total;
    }

    public Map<Integer, Long> getStatusCounterMap() {
        return statusCounterMap;
    }

    public Map<String, Long> getUrlCounterMap() {
        return urlCounterMap;
    }

    public String getId() {
        return id;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getStopTimestamp() {
        return stopTimestamp;
    }

    public long getTimeout() {
        return timeout;
    }

    public String getC_ip() {
        return c_ip;
    }

    public boolean isHasDataEnter() {
        return hasDataEnter;
    }

    public void setHasDataEnter(boolean hasDataEnter) {
        this.hasDataEnter = hasDataEnter;
    }

    public Set<String> getIndices() {
        return indices;
    }

    public synchronized void incAnomaly(String key) {
        if (!this.ANOMALYOccur.containsKey(key)) {
            this.ANOMALYOccur.put(key, new AnomalyStatus());
        }
        AnomalyStatus as = this.ANOMALYOccur.get(key);
        as.incOccur();
    }

    public Long getANOMALYCount(String eventType) {
        long result = 0L;
        if (this.ANOMALYOccur.containsKey(eventType)) {
            result = this.ANOMALYOccur.get(eventType).getOccur();
        }
        return result;
    }

    public Map<String, String> getServers() {
        return servers;
    }

    public String getCookieId() {
        return cookieId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Double get404Rate() {
        double sc404Rate = 0.0;

        if (statusCounterMap.get(404) != null)
            sc404Rate = new BigDecimal((double) statusCounterMap.get(404) / this.total).setScale(5, BigDecimal.ROUND_CEILING).doubleValue();
        return sc404Rate;
    }

    public Double get500Rate() {
        double sc500Rate = 0.0;
        if (statusCounterMap.get(500) != null)
            sc500Rate = new BigDecimal((double) statusCounterMap.get(500) / this.total).setScale(5, BigDecimal.ROUND_CEILING).doubleValue();
        return sc500Rate;
    }

    public Double getUrlRate() {
        double urlRate = 0.0;
        if (urlCounterMap.size() > 0) {
            QDigest digest = new QDigest(urlCounterMap.size() + 1);

            for (Map.Entry<String, Long> num : urlCounterMap.entrySet()) {
                digest.offer(num.getValue().longValue());
            }
            long l = digest.getQuantile(0.1);
            urlRate = new BigDecimal((double) l / this.total).setScale(5, BigDecimal.ROUND_CEILING).doubleValue();

        }
        return urlRate;
    }

    @Override
    public String toString() {
        String startTS = this.dateFormat.format(new Date(startTimestamp));
        String endTS = this.dateFormat.format(new Date(stopTimestamp));
        return "\nSession [c_ip=" + c_ip + ", startTS=" + startTS
                + ", stopTimestamp=" + endTS + ", total=" + total
                + ", statusCounterMap=" + statusCounterMap
                + ", urlCounterMap=" + urlCounterMap + ", hasDataEnter="
                + hasDataEnter + ", indices=" + indices + ", servers="
                + servers + ", ANOMALYOccur=" + ANOMALYOccur + "]";
    }

}