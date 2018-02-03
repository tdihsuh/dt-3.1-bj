package com.hansight.kunlun.collector.reader;

public abstract class DelimiterFileLogReader extends FileLogReader {
    public abstract String getApp();

    public abstract String getDelimiter();
}