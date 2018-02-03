package com.hansight.kunlun.analysis.statistics.exception;

import com.hansight.kunlun.exception.KunlunException;
import com.hansight.kunlun.exception.StatusCode;

public class ParserException extends KunlunException {
    private static final long serialVersionUID = -537733358196006247L;
    private final String log_;

    public ParserException(StatusCode code, String message) {
        this(code, message, null);
    }

    public ParserException(StatusCode code, String message, String log) {
        super(code, message);
        log_ = log;
    }

    public String getLog() {
        return log_;
    }
}