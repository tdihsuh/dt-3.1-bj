package com.hansight.kunlun.collector.reader;

/**
 * Author:zhhui
 * DateTime:2014/8/1 10:01.
 */
public abstract class ExcelFileLogReader extends FileLogReader {
    public abstract boolean getHasHeader();

    public abstract String getSeparator();
}
