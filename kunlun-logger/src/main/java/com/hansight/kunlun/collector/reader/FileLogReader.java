package com.hansight.kunlun.collector.reader;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.common.model.ReadPosition;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public abstract class FileLogReader<T> implements LogReader<T> {
    protected ReadPosition position;
    protected int lineSeparatorLength;
    Lexer<Event, Map<String, Object>> lexer;
    /**
     * 要处理的文件
     */
    protected BufferedReader reader;
    protected String path;

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ReadPosition readPosition() {
        return position;
    }

    public void setPosition(ReadPosition position) {
        this.position = position;
    }
    @Override
    public void close() throws IOException {
        reader.close();
        reader=null;
    }
    @Override
    public String path() {
        return path;
    }

    public int getLineSeparatorLength() {
        return lineSeparatorLength;
    }

    public void setLineSeparatorLength(int lineSeparatorLength) {
        this.lineSeparatorLength = lineSeparatorLength;
    }
      public Lexer<Event, Map<String, Object>> lexer() {
        return lexer;
    }

    public void setLexer(Lexer<Event, Map<String, Object>> lexer) {
        this.lexer = lexer;
    }
}