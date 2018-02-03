package com.hansight.kunlun.collector.lexer;

import com.hansight.kunlun.collector.common.exception.CollectorException;
import com.hansight.kunlun.collector.common.model.Event;
import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author:zhhui
 * DateTime:2014/7/23 15:27.
 * 默认按照String 解析一行日志到，json
 */
public class RegexLogLexer extends DefaultLexer<Event, Map<String, Object>> {

    protected final static Logger logger = LoggerFactory.getLogger(RegexLogLexer.class);
    private Grok grok;


    private String pattern;

    @Override
    public void setTemplet(String name) {
        this.pattern = name;
        setGrok(Grok.getInstance(pattern));
    }

    @Override
    public Map<String, Object> parse(Event event) throws CollectorException {
        Match match;
        byte[] log_src = event.getBody().array();
        String value;
        value = new String(log_src);
        match = grok.match(value);
        match.captures(true);
        Map<String, Object> log = match.toMap();
        if (log == null || log.isEmpty()) {
            log = new LinkedHashMap<>();
            log.put("data", value);
            log.put("error", "this data can`t parse by give regex:" + pattern);
            logger.debug("event parser error,data:{}", value);
        }
        return log;
    }

    public void setGrok(Grok grok) {
        this.grok = grok;
    }

    @Override
    public RegexLogLexer newClone() {
        RegexLogLexer lexer =new RegexLogLexer();
        lexer.grok=grok;
        lexer.pattern=pattern;
        lexer.header=header;
        return lexer;
    }
}
