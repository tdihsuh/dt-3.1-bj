package com.hansight.kunlun.collector.forwarder.parser;

import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.lexer.XMLLogLexer;

import java.util.Map;

/**
 * @author tao_zhang
 */
public class XMLLogParser extends DefaultLogParser {

    @Override
    public Lexer<Event, Map<String, Object>> getLexer() {
        Lexer<Event, Map<String, Object>> lexer = new XMLLogLexer();
        lexer.setTemplet(this.conf.get("encoding"));
        return lexer;
    }
}