package com.hansight.kunlun.collector.processor.file;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hansight.kunlun.collector.agent.Agent;
import com.hansight.kunlun.collector.common.base.Lexer;
import com.hansight.kunlun.collector.common.model.Event;
import com.hansight.kunlun.collector.common.model.ReadPosition;
import com.hansight.kunlun.collector.common.utils.AgentConstants;
import com.hansight.kunlun.collector.lexer.DelimiterLogLexer;
import com.hansight.kunlun.collector.position.store.ReadPositionStore;
import com.hansight.kunlun.collector.reader.FileLogReader;
import com.hansight.kunlun.collector.reader.LineFileLogReader;
import com.hansight.kunlun.collector.utils.ReaderUtils;
import com.hansight.kunlun.coordinator.config.AgentConfig;
import com.sun.istack.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2014/11/15.
 */
public class FileHandler<F> {
    private static Logger logger = LoggerFactory.getLogger(FileHandler.class);
    protected static Integer AGENT_FILE_READER_THREAD_BUFFER = Integer.valueOf(Agent.GLOBAL.getProperty(AgentConstants.AGENT_FILE_READER_THREAD_BUFFER, AgentConstants.AGENT_FILE_READER_THREAD_BUFFER_DEFAULT));
    ReadPosition position;

    @SuppressWarnings("unchecked")
    public FileLogReader<F> handle(@NotNull AgentConfig conf, ReadPositionStore store, Lexer<Event, Map<String, Object>> lexer, InputStream file, String path, long modified, Boolean first, Boolean create) {
        logger.debug(" handle  processor file:{} start", path);
        FileLogReader<F> logReader = null;
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("source", path);
        Lexer<Event, Map<String, Object>> fileLexer = null;
        try {
            String readerName = conf.get("reader");
            if (readerName != null) {
                String clazzName = Agent.READER_CLASS_MAPPING.getProperty(readerName, LineFileLogReader.class.getName());
                Class<FileLogReader<F>> readerClass = (Class<FileLogReader<F>>) Class.forName(clazzName);
                logReader = readerClass.newInstance();
            } else {
                logger.error("config error reader,can't find reader conf");
                return null;
            }
            String key = conf.getId() + "_" + path;
            ReadPosition position = store.get(key);
            if (position == null || create) {
                position = new ReadPosition(key, 0l, 0l);
            } else if (modified == position.getModified() && position.getFinished()) {
                logger.info(key + " has process finished,won`t need to re read. records:" + position.records());
                return null;
            }
            position.setFinished(false);
            position.setModified(modified);

            logReader.setPosition(position);
            Object enc = conf.get("encoding");
            String encoding;
            if (enc == null) {
                encoding = ReaderUtils.charset(file);
            } else {
                encoding = (enc.toString());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(file, encoding), AGENT_FILE_READER_THREAD_BUFFER);
            String lineSeparator = ReaderUtils.lineSeparator(reader);
            logReader.setLineSeparatorLength(lineSeparator.length());
            String line=null;
            if ("iis".equals(conf.get("category"))) {
                for (int i = 0; i < 4; i++) {
                    reader.mark(1024 * 1024 * 10);
                    line = reader.readLine();
                    if (line != null && line.startsWith("#")) {
                        line = line.substring(1);
                        String[] fields = line.split(":");
                        if (fields.length == 2) {
                            if (fields[0] != null && fields[0].equals("Fields")) {
                                fileLexer = new DelimiterLogLexer();
                                JSONObject object = new JSONObject();
                                object.put("separate", " ");
                                JSONArray array = new JSONArray();
                                String[] names = fields[1].trim().split(" ");
                                for (String name : names) {
                                    array.add(Agent.NAME_MAPPING.getProperty(name, name));
                                }
                                // Collections.addAll(array, fields[1].trim().split(" "));
                                object.put("fields", array);
                                fileLexer.setTemplet(object.toJSONString());
                            } else {
                                header.put(fields[0], fields[1]);
                            }
                        }

                    } else {
                        reader.reset();
                        break;
                    }
                }


            } else if ("delimit".equals(conf.get("lexer"))) {
                reader.mark(1024 * 1024 * 10);
                line = reader.readLine();
                if (line != null) {
                    DelimiterLogLexer delimiterLogLexer = (DelimiterLogLexer) lexer;
                    if (delimiterLogLexer.getSeparate() == null) {
                        throw new IOException();
                    }

                    String[] fields = line.split(delimiterLogLexer.getSeparate());
                    if (delimiterLogLexer.getFields() == null || fields.length != delimiterLogLexer.getFields().length) {
                        fileLexer = new DelimiterLogLexer();
                        JSONObject object = new JSONObject();
                        object.put("separate", " ");
                        JSONArray array = new JSONArray();
                        for (String name : fields) {
                            array.add(Agent.NAME_MAPPING.getProperty(name, name));
                        }
                        //  Collections.addAll(array, fields);
                        object.put("fields", array);
                        fileLexer.setTemplet(object.toJSONString());
                    }
                } else {
                    reader.reset();

                }


            }else{
                line="";
            }
            if(line==null){
                return null;
            }
            logReader.setPath(key);
            logReader.setReader(reader);
            logger.debug(" starting  processor file:{} ... ", key);
            if (!(first && "beginning".equals(conf.get("start_position")))) {
                long skipped = logReader.skip(position.position());
                logger.debug("{} need skip:{} skipped:{}", key, position.position(), skipped);
                if (skipped < position.position()) {
                    store.set(position);
                    store.flush();
                    return null;
                }
            } else {
                position.setPosition(0l);
                position.setRecords(0l);
            }
            logReader.setLexer(fileLexer == null ? lexer : fileLexer);
            logReader.lexer().setHeader(header);
        } catch (Exception e) {

            logger.error("FILE HANDLER ERROR:{}", e);
            logReader=null;
        } finally {
            logger.debug(" handle  processor file:{} end", path);
        }

        return logReader;
    }
}
