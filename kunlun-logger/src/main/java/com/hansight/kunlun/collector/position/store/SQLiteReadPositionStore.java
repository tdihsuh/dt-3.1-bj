package com.hansight.kunlun.collector.position.store;

import com.hansight.kunlun.collector.common.model.ReadPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yzh on 4/14/14.
 */
public class SQLiteReadPositionStore implements ReadPositionStore {
    final static Logger logger = LoggerFactory.getLogger(FileReadPositionStore.class);
    private final static String SELECT_TABLE_NAME = "SELECT name FROM sqlite_master WHERE type='table' AND name='READ_POSITION'";
    //8 byte (long long) integer type used to store a signed integer between -9223372036854775808 and 9223372036854775807 or an unsigned integer between 0 and 18446744073709551615. See below for a discussion of INTEGER PRIMARY KEY.
    private final static String CREATE_TABLE = "CREATE TABLE READ_POSITION (PATH TEXT PRIMARY KEY NOT NULL, POSITION INT8 NOT NULL,RECORDS INT8 NOT NULL,FINISHED BOOLEAN NOT NULL ,MODIFIED INT8 NOT NULL )";
    private final static String SELECT_STATEMENT = "SELECT POSITION,RECORDS FROM READ_POSITION WHERE PATH=?";
    private final static String SELECT_FULL = "SELECT POSITION,RECORDS,FINISHED,MODIFIED FROM READ_POSITION WHERE PATH=?";
    private final static String UPDATE_STATEMENT = "UPDATE READ_POSITION SET POSITION = ?, RECORDS = ?  WHERE PATH = ?";
    private final static String UPDATE_FULL = "UPDATE READ_POSITION SET POSITION = ?, RECORDS = ?, FINISHED= ? ,MODIFIED= ?  WHERE PATH =?";
    private final static String INSERT_STATEMENT = "INSERT INTO READ_POSITION(PATH, POSITION, RECORDS,FINISHED,MODIFIED) VALUES(?,?,?,?,?)";

    private Connection conn;

    private String db = path + (path.endsWith("/") ? "" : "/") + "read_position.db";
    private int cacheSize = 1;
    private volatile ReadPosition position;

    public synchronized boolean init() {
        Statement stmt = null;
        ResultSet result = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + db);
            conn.setAutoCommit(true);

            stmt = conn.createStatement();
            result = stmt.executeQuery(SELECT_TABLE_NAME);
            if (!result.next()) {
                stmt.executeUpdate(CREATE_TABLE);
                stmt.close();
            }
            return true;
        } catch (Exception e) {
            logger.error("init read_position store db error:{}", e);
            return false;
        } finally {
            if (result != null)
                try {
                    result.close();
                } catch (SQLException e) {
                    logger.error("close result error:{}", e);
                }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error("close stmt error:{}", e);
                }
            }

        }
    }

    public void close() {
        try {
            flush();
            conn.close();
        } catch (Exception e) {
            logger.error("close read_position store error,when flush to db:{}", e);
        }
    }

    public synchronized boolean set(ReadPosition position) {
        this.position = position;
        if (position.records() % cacheSize == 0) {
            try {
                flush();
            } catch (IOException e) {
                logger.error("read_position store error,when flush to file:{}", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public void setCacheSize(int size) {
        this.cacheSize = size;
    }

    public synchronized ReadPosition get(String path) {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = conn.prepareStatement(SELECT_FULL);
            stmt.setString(1, path);
            result = stmt.executeQuery();
            if (result.next()) {
                long pos = result.getLong("POSITION");
                long records = result.getLong("RECORDS");
                boolean finished = result.getBoolean("FINISHED");
                Long modified = result.getLong("MODIFIED");
                ReadPosition position = new ReadPosition(path, records, pos);
                position.setFinished(finished);
                position.setModified(modified);
                return position;
            } else
                return null;
        } catch (SQLException e) {
            logger.error("get read_position store db error:{}", e);
            return null;
        } finally {
            if (result != null)
                try {
                    result.close();
                } catch (SQLException e) {
                    logger.error("close result error:{}", e);
                }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error("close stmt error:{}", e);
                }
            }

        }
    }


    @Override
    public synchronized void flush() throws IOException {
        if (position != null) {
            PreparedStatement stmt = null;
            if (get(position.getPath()) != null) {
                try {
                    stmt = conn.prepareStatement(UPDATE_FULL);
                    stmt.setLong(1, position.position());
                    stmt.setLong(2, position.records());
                    stmt.setBoolean(3, position.getFinished());
                    stmt.setLong(4, position.getModified());
                    stmt.setString(5, position.getPath());
                    int result = stmt.executeUpdate();
                } catch (SQLException e) {
                    logger.error(" sqlite update error:{}", e);
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            logger.error("close stmt error:{}", e);
                        }
                    }

                }
            } else {
                try {
                    stmt = conn.prepareStatement(INSERT_STATEMENT);
                    stmt.setString(1, position.getPath());
                    stmt.setLong(2, position.position());
                    stmt.setLong(3, position.records());
                    stmt.setBoolean(4, position.getFinished());
                    stmt.setLong(5, position.getModified());
                    stmt.execute();
                } catch (SQLException e) {
                    logger.error("sqlite  insert error:{}", e);
                    flush();
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            logger.error("close stmt error:{}", e);
                        }
                    }

                }

            }

        }
    }
}
