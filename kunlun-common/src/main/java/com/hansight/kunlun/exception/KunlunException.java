package com.hansight.kunlun.exception;

/**
 * Author:zhhui
 * DateTime:2014/9/1 14:21.
 */
public class KunlunException extends Exception {
    private StatusCode code;

    public KunlunException(StatusCode code) {
        super();
        this.code = code;
    }

    public KunlunException(StatusCode code, String message) {
        super(message);
        this.code = code;
    }

    public KunlunException(StatusCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public StatusCode getStatusCode() {
        return code;
    }
}
