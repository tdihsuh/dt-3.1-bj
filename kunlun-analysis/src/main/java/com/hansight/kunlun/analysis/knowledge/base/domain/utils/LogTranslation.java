package com.hansight.kunlun.analysis.knowledge.base.domain.utils;

import com.hansight.kunlun.analysis.knowledge.base.domain.model.Log;
import com.hansight.kunlun.analysis.knowledge.base.domain.model.cef.CEF;

/**
 * Author: zhhui
 * Date: 2014/9/9
 */
public interface LogTranslation {
    Log translation(CEF cef);
}
