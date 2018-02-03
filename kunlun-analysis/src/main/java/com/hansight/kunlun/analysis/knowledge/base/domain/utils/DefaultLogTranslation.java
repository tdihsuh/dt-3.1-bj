package com.hansight.kunlun.analysis.knowledge.base.domain.utils;

import com.hansight.kunlun.analysis.knowledge.base.domain.model.Log;
import com.hansight.kunlun.analysis.knowledge.base.domain.model.cef.CEF;
import com.hansight.kunlun.analysis.knowledge.base.domain.model.cef.Extension;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Author: zhhui
 * Date: 2014/9/9
 */
public class DefaultLogTranslation implements LogTranslation {
    public final static Logger logger = LoggerFactory.getLogger(DefaultLogTranslation.class);
    private Map<String, String> transMap;

    public DefaultLogTranslation(Map<String, String> transMap) {
        this.transMap = transMap;
    }

    @Override
    public Log translation(CEF cef) {

        Extension extension = cef.getExtension();
        Log log = new Log();
        log.setCef(cef);
        for (Map.Entry<String, String> entry : transMap.entrySet()) {
            try {
                BeanUtils.copyProperty(log, entry.getKey(), extension.getFields().get(entry.getValue()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("translation error when check cef in :{}", e);
                e.printStackTrace();

            }
        }
        return log;
    }
}
