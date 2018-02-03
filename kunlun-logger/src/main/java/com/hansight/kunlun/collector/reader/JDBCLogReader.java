package com.hansight.kunlun.collector.reader;

import com.hansight.kunlun.collector.common.base.LogReader;
import com.hansight.kunlun.collector.stream.JDBCStream;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by zhhuiyan on 14/12/26.
 */
public class JDBCLogReader implements LogReader<Map<String,Object>> {


    String table;
    String clazz;
    String uri;
    JDBCStream stream;

    public JDBCLogReader(String uri, String clazz, String table,String ... info) throws ClassNotFoundException {
        this.uri = uri;
        this.clazz = clazz;
        this.table = table;
            Class.forName(clazz);
        Properties properties=new Properties();
        if (info.length>=1&& info[0] != null) {
            properties.put("user", info[0]);
        }
        if (info.length>=2&&info[1] != null) {
            properties.put("password", info[1]);
        }

        stream=new JDBCStream(uri,properties,table);
    }

    @Override
    public long skip(long skip) {
        return 0;
    }

    @Override
    public String path() {
        return uri;
    }

    @Override
    public void close() throws IOException {
        if(stream!=null){
            stream.close();
        }
    }

    @Override
    public Iterator<Map<String,Object>> iterator() {
        return stream;
    }
}
