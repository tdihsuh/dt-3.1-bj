package com.hansight.kunlun.collector.lexer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.hansight.kunlun.collector.common.exception.CollectorException;
import com.hansight.kunlun.collector.common.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DelimiterLogLexer extends DefaultLexer<Event, Map<String, Object>> {
    protected final Pattern pattern = Pattern.compile("\\S*?(?<cookieId>([iI][dD]=([0-9.]*-[0-9]*|\\w*)))\\S*?");
    protected final String cookieId_p = "cookieId";
    protected final String cookie_id = "cookie_id";
    protected final static Logger logger = LoggerFactory.getLogger(DelimiterLogLexer.class);
    private String separate;
    private String separateWithSpace;
    private String[] fields;

    public String getSeparate() {
        return separate;
    }

    public String[] getFields() {
        return fields;
    }

    @Override
    public void setTemplet(String name) {
        Map<String, Object> log = JSON.parseObject(name);
        separate = log.get("separate").toString();
        if(" ".equals(separate)){
            separateWithSpace = "\t" + separate + "\t";
        }else{
            separateWithSpace = " " + separate + " ";
        }

        Object[] array = ((JSONArray) log.get("fields")).toArray();
        fields = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            fields[i] = array[i].toString();
        }
    }

    @Override
    public Map<String, Object> parse(Event event) throws CollectorException {
        Map<String, Object> log = new LinkedHashMap<>();
        String line;
        line = new String(event.getBody().array()).replaceAll(separate, separateWithSpace);
         String[] values = line.split(separate);
        if (fields.length == values.length) {
            for (int i = 0; i < fields.length; i++) {
                if (values[i] != null) {
                    String value = values[i].trim();
                    if ("".equals(value) ||"-".equals(value)) {
                        continue;
                    }
                    int len = value.length();
                    if (len >= 1) {
                        char c = value.charAt(0);
                        if (c == '"' || c == '\'') {
                            value = value.substring(1);
                        }
                    }
                    len = value.length();
                    if (len >= 1) {
                        char c = value.charAt(len - 1);
                        if (c == '"' || c == '\'') {
                            value = value.substring(0, len - 1);
                        }
                    }
                    if ("cs_cookie".equals(fields[i])) {
                        //TODO only for IIS maybe removed later
                        Matcher matcher = pattern.matcher(values[i]);
                        if (matcher.find()) {
                            log.put(cookie_id, matcher.group(cookieId_p));
                        }
                    }

                    log.put(fields[i], value);
                }

            }
        } else {
            log.put("message", line);
            log.put("error", "your set fields.length:" +
                    fields.length + ",not match the values.length:" + values.length + "that you want parser");
            //   throw new CollectorException("your set fields,not match the values that you want parser");
        }

         /*   try {

            } catch (UnsupportedEncodingException e) {
                log.put("error", "your set UnsupportedEncoding you set for");
                throw new CollectorException("your set UnsupportedEncoding you set for", e);
            }*/

        return log;
    }

    @Override
    public DelimiterLogLexer newClone() {
        DelimiterLogLexer lexer=new DelimiterLogLexer();
        lexer.separate=separate;
        lexer.separateWithSpace=separateWithSpace;
        lexer.fields=fields;
        lexer.header=header;
        return lexer;
    }
}