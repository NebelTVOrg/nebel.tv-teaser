/**
 * Copyright (C) 2014 Nebel TV (http://nebel.tv)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.nebel_tv.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtils {

	private static final int SECOND_IN_MILLIS = 1000;
	private static final int SECOND_IN_MICROS = SECOND_IN_MILLIS * 1000;

	private static DateTimeFormatter defaultTimeFormatter;

	public static synchronized DateTimeFormatter getDefaultTimeFormatter() {
		if (defaultTimeFormatter == null) {
			defaultTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss").withZoneUTC();
		}
		return defaultTimeFormatter;
	}

	public static long getSecValueInMillis(long value) {
		return getSecValueInMillis(value, false);
	}

	public static long getSecValueInMillis(long value, boolean reverse) {
		return reverse ? value / SECOND_IN_MILLIS : value * SECOND_IN_MILLIS;
	}

	public static long getSecValueInMicros(long value) {
		return getSecValueInMicros(value, false);
	}

	public static long getSecValueInMicros(long value, boolean reverse) {
		return reverse ? value / SECOND_IN_MICROS : value * SECOND_IN_MICROS;
	}
}
