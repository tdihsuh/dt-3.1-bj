package com.hansight.kunlun.collector.forwarder.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.lexer.DelimiterLogLexer;

import java.util.Map;

/**
 * @author tao_zhang
 */
public class DelimiterLogParser extends DefaultLogParser {


    @Override
    public Lexer<Event, Map<String, Object>> getLexer() {
        Lexer<Event, Map<String, Object>> lexer;
        String pattern = conf.get("pattern");
        logger.debug("pattern:{}", pattern);
        JSONObject obj = JSON.parseObject(pattern);
        JSONArray array = obj.getJSONArray("fields");
        String[] fields = new String[array.size()];
        array.toArray(fields);
        lexer = new DelimiterLogLexer();
        lexer.setTemplet(pattern);
        return lexer;
    }
}