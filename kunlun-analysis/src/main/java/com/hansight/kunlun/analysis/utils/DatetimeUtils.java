package com.hansight.kunlun.analysis.utils;

import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.DateTimeZone;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;

public class DatetimeUtils {

	public static Date addYear(Date date) {
		long current = System.currentTimeMillis();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(current);
		int year = c.get(Calendar.YEAR);
		c.setTime(date);
		c.set(Calendar.YEAR, year);
		Date d = c.getTime();
		if (d.getTime() <= current) {
			return d;
		}
		c.add(Calendar.YEAR, -1);
		return c.getTime();
	}

	/**
	 * Get UTC format date time
	 * 
	 * @param datePattern
	 * @param formattedDateTime
	 * @return utc format datetime
	 */
	public static String getUTCDateTime(String datePattern,
			String formattedDateTime) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(datePattern);
		DateTime dt = formatter.parseDateTime(formattedDateTime);
		return dt.toDateTime(DateTimeZone.UTC).toString();
	}

	/**
	 * Get UTC format date time
	 * 
	 * @param time
	 * @return
	 */
	public static String getUTCDateTime(long time) {
		DateTime dt = new DateTime(time);
		return dt.toDateTime(DateTimeZone.UTC).toString();
	}

	/**
	 * Get timestamp
	 * 
	 * @param utcDateTime
	 * @return
	 */
	public static long getTimestamp(String utcDateTime) {
		DateTime dt = new DateTime(utcDateTime);
		return dt.getMillis();
	}

	/**
	 * Get timestamp
	 * 
	 * @param utcDateTime
	 * @return
	 */
	public static Date getDate(String utcDateTime) {
		DateTime dt = new DateTime(utcDateTime);
		return dt.toDate();
	}

	public static long getDayBegin(long date) {
		return getDayBegin(date, DateTimeZone.UTC);
	}

	public static long getDayBegin(long date, long hoursOffsetFromZeroZone) {
		return getDayBegin(date, DateTimeZone.forOffsetHours(8));
	}

	public static long getDayBegin(long date, DateTimeZone zone) {
		DateTime dt = new DateTime(date, zone);
		return dt.minusHours(dt.getHourOfDay())
				.minusMinutes(dt.getMinuteOfHour())
				.minusSeconds(dt.getSecondOfMinute())
				.minusMillis(dt.getMillisOfSecond()).getMillis();
	}

	public static void main(String[] args) {
		long l1 = System.currentTimeMillis();
		long l2 = getDayBegin(l1);
		System.out.println(new DateTime(l1));
		System.out.println(new DateTime(l2));
		System.out.println(new DateTime(getDayBegin(l1, 8)));
		System.out.println(getUTCDateTime(System.currentTimeMillis()));
	}

}
