package com.nebel_tv.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtils {

	private static final int SECOND_IN_MILLIS = 1000;
	private static final int SECOND_IN_MICROS = SECOND_IN_MILLIS*1000;
	
	private static DateTimeFormatter defaultTimeFormatter; 
	
	public static synchronized DateTimeFormatter getDefaultTimeFormatter() {
		if(defaultTimeFormatter==null) {
			defaultTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss").withZoneUTC();
	    }
		return defaultTimeFormatter;
	}
	
	public static long getSecValueInMillis(long value) {
		return getSecValueInMillis(value, false);
	}
	
	public static long getSecValueInMillis(long value, boolean reverse) {
		return reverse?value/SECOND_IN_MILLIS:value*SECOND_IN_MILLIS;
	}
	
	public static long getSecValueInMicros(long value) {
		return getSecValueInMicros(value, false);
	}
	
	public static long getSecValueInMicros(long value, boolean reverse) {
		return reverse?value/SECOND_IN_MICROS:value*SECOND_IN_MICROS;
	}
}
