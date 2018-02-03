package com.hansight.kunlun.analysis.utils;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

public class PropertiesUtils {
    private static Properties datas = new Properties();
    static final Logger logger = Logger.getLogger(PropertiesUtils.class);


    public static String getClassPath() {
        String path = PropertiesUtils.class.getResource("").getPath();
        try {
            path = URLDecoder.decode(path, "utf-8");
        } catch (Exception ex) {
            logger.info("getClassPath", ex);
        }
        return path;
    }

    public static void loadFile(String name) {
        try {
            InputStream in = PropertiesUtils.class.getClassLoader().getResourceAsStream(
                    name);
            datas.load(in);
            in.close();
        } catch (Exception e) {
            logger.info("loadFile", e);
        }
    }

    public static Object getValue(String name) {
        return datas.get(name);
    }

    public static Properties getDatas() {
        return datas;
    }

}