package com.hansight.kunlun.web.util;

public class TimeUtils {
	/**
	 * 将java.util.Date格式化成“yyyy-MM-dd HH:mm:ss”的字符串(24小时制)
	 * @param date java.util.Date
	 * @return string
	 */
	public static String format(java.util.Date date) {
		java.text.DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format1.format(date);
	}
}
