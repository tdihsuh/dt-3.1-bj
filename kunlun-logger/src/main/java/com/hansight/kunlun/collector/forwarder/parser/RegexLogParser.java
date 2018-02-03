package com.hansight.kunlun.collector.forwarder.parser;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.lexer.RegexLogLexer;

import java.util.Map;

/**
 * @author tao_zhang
 */
public class RegexLogParser extends DefaultLogParser {
    @Override
    public Lexer<Event, Map<String, Object>> getLexer() {
        Lexer<Event, Map<String, Object>> lexer = new RegexLogLexer();
        lexer.setTemplet(this.conf.get("encoding"));
        return lexer;
    }
}