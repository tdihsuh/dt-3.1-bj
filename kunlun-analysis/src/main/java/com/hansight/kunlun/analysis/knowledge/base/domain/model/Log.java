package com.hansight.kunlun.analysis.knowledge.base.domain.model;

import com.hansight.kunlun.analysis.knowledge.base.domain.model.cef.CEF;

import java.io.Serializable;

/**
 * @author zhhui
 * @version 1.0
 * @see com.hansight.kunlun.analysis.knowledge.base.domain.model.cef.CEFs
 * http://www.ietf.org/rfc/rfc3164.txt
 * http://jackiechen.blog.51cto.com/196075/149860
 * @since 2014.09.11
 */
public class Log implements Serializable {
    private CEF cef;


    /**
     * 用户
     */
    private String user;
    /**
     * 用户地址
     */

    private String userFrom;
    /**
     * 详细信息，备注等
     */

    private String destination;
    /**
     * 用户行为
     */

    private String action;
    /**
     * 行为目标
     */
    private String accessTo;
    /**
     * 行为结果
     */
    private String accessResult;

    public CEF getCef() {
        return cef;
    }

    public void setCef(CEF cef) {
        this.cef = cef;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(String userFrom) {
        this.userFrom = userFrom;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAccessTo() {
        return accessTo;
    }

    public void setAccessTo(String accessTo) {
        this.accessTo = accessTo;
    }

    public String getAccessResult() {
        return accessResult;
    }

    public void setAccessResult(String accessResult) {
        this.accessResult = accessResult;
    }
}