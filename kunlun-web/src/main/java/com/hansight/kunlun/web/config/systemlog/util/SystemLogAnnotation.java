package com.hansight.kunlun.web.config.systemlog.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 系统日志类型注释类
 * @author zhangshuyu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SystemLogAnnotation {
	// 系统日志类别
	SystemLogModule logType();
}
