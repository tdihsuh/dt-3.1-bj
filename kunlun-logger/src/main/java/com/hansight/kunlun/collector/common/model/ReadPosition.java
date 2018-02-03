package com.hansight.kunlun.collector.common.model;

import java.io.Serializable;
import java.sql.Date;

/**
 * Author:zhhui
 * DateTime:2014/7/31 17:16.
 */
public class ReadPosition implements Serializable {
    private Long records;
    private Long position;
    private String path;
    private Long modified;
    private Boolean finished=false;

    public ReadPosition(String path, Long records, Long position) {
        this.records = records;
        this.position = position;
        this.path = path;
    }

    public Long getModified() {
        return modified;
    }

    public void setModified(Long modified) {
        this.modified = modified;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Long records() {
        return records;
    }

    public void recordAdd() {
        records++;
    }

    public void positionAdd(long pos) {
        this.position += pos;
    }

    public void setRecords(Long records) {
        this.records = records;
    }

    public Long position() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "FilePos{" +
                "records=" + records +
                ", position=" + position +
                ", path='" + path + '\'' +
                '}';
    }
}
