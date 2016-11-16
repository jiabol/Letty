package com.jiabo.letty.mvc.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTimeUtil {
	private static final Logger log = LoggerFactory
			.getLogger(DateTimeUtil.class);

	private static String pattern = "yyyy-MM-dd HH:mm:ss";

	public static void initFormat(String format) {
		pattern = format;
	}

	public static Date parseDate(String date, String format) {
		if (format == null)
			format = pattern;
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(date);
		} catch (Exception e) {
			log.error("can not parse date :" + date);
		}
		return null;
	}

	public static String formatDate(Date date, String format) {
		if (format == null)
			format = pattern;
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
}
