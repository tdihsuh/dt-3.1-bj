package com.hansight.kunlun.collector.forwarder.parser;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.lexer.CEFLogLexer;

import java.util.Map;

/**
 * @author yzhhui
 */
public class CEFLogParser extends DefaultLogParser {
    @Override
    public Lexer<Event, Map<String, Object>> getLexer() {
        return new CEFLogLexer();
    }
}