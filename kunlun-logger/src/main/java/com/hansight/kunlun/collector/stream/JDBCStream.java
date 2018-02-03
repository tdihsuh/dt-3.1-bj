package com.hansight.kunlun.collector.stream;

import com.hansight.kunlun.collector.common.model.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zhhui on 2014/11/5.
 */
public class JDBCStream extends Stream<Map<String, Object>> {
    private final static Logger logger = LoggerFactory.getLogger(JDBCStream.class);
    private Connection connection;

    private Statement statement;
    private ResultSet set;
    private ResultSetMetaData metaData;

    public JDBCStream(String uri,Properties info, String table) {
        try {
            connection = DriverManager.getConnection(uri,info);
            statement = connection.createStatement();
            set = statement.executeQuery("SELECT * FROM " + table);
            metaData=set.getMetaData();

        } catch (SQLException e) {
            logger.error("JDBC connect error:{}", e);
        }

    }
    @Override
    public boolean hasNext() {

        try {
            item=null;
            int count=    metaData.getColumnCount();

            if (set.next()) {
                item = new HashMap<>();
                for(int i=1;i<=count;i++){
                    item.put(metaData.getColumnName(i),set.getObject(i));
                }
            }


        } catch (Exception e) {
            logger.error("SQL get row date error:{}", e);
            item = null;
        }
        return item != null;
    }

    @Override
    public void close() throws IOException {
        try {
            if (set != null ) {
                set.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("JDBC close error:{}", e);
            throw new IOException(e);
        }
    }
}
