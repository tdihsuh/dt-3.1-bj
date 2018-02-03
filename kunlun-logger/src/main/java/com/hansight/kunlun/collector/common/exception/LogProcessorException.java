package com.hansight.kunlun.collector.common.exception;

/**
 * Author:zhhui
 * DateTime:2014/7/30 9:50.
 */
public class LogProcessorException extends CollectorException {
    public LogProcessorException(String message) {
        super(message);
    }

    public LogProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
