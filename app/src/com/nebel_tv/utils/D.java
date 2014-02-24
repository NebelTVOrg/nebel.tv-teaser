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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import android.util.Log;

public class D {

	private static boolean debug = false; // set false to disable debugging
	private static boolean localLog = false; // set false to disable adding logs
												// to local memory buffer

	private static int CALLER_POINT_STACK_TRACE_ELEMENT_NUMBER = 4;
	private static StringBuffer logBuffer = new StringBuffer();

	public static void enableDebug() {
		D.debug = true;
	}

	public static void disableDebug() {
		D.debug = false;
	}

	public static void enableLocalLog() {
		D.localLog = true;
	}

	public static void disableLocalLog() {
		D.localLog = false;
	}

	public static void e(final Throwable throwable) {
		e(throwable, true);
	}

	public static void e(final String message) {
		e(message, true);
	}

	public static void i(final String message) {
		i(message, true);
	}

	public static void v(final String message) {
		v(message, true);
	}

	public static void d(final String message) {
		d(message, true);
	}

	public static void w(final String message) {
		w(message, true);
	}

	public static void e(final Throwable throwable, final boolean logToBuffer) {
		if (throwable != null) {
			log(Log.ERROR, stackTraceStringOfTheThrowable(throwable), logToBuffer);
		}
	}

	public static void e(final String message, final boolean logToBuffer) {
		log(Log.ERROR, message, logToBuffer);
	}

	public static void i(final String message, final boolean logToBuffer) {
		log(Log.INFO, message, logToBuffer);
	}

	public static void v(final String message, final boolean logToBuffer) {
		log(Log.VERBOSE, message, logToBuffer);
	}

	public static void d(final String message, final boolean logToBuffer) {
		log(Log.DEBUG, message, logToBuffer);
	}

	public static void w(final String message, final boolean logToBuffer) {
		log(Log.WARN, message, logToBuffer);
	}

	public static String stackTraceStringOfTheThrowable(Throwable e) {
		if (e == null) {
			return "";
		}

		String ret = "";
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		e.printStackTrace(ps);
		ret = os.toString();
		return ret;
	}

	public static void logCurrentStackTrace(String customMessage) {
		String logString = customMessage + "\n" + getStackTrace();
		log(Log.DEBUG, logString, false);
	}

	public static void logCurrentStackTrace() {
		String logString = "\n" + getStackTrace(); // if you'll call
													// logCurrentStackTrace(String
													// customMessage)
													// instead this code, extra
													// stack trace element will
													// be displayed
													// see getStackTrace(). So
													// small duplicating left
													// for a while
		log(Log.DEBUG, logString, false);
	}

	private static String getStackTrace() {
		String ret = "";
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		for (int i = 4; i < ste.length - 1; i++) {
			ret += "\n" + ste[i].toString();
		}
		return ret;
	}

	private static void log(final int type, String message, boolean logToBuffer) {
		if (!debug) {
			return;
		}

		String caller = "D";

		String callPoint = "";
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();

		if (ste != null && ste.length > CALLER_POINT_STACK_TRACE_ELEMENT_NUMBER) {
			callPoint += ste[CALLER_POINT_STACK_TRACE_ELEMENT_NUMBER].getFileName() + ":"
					+ ste[CALLER_POINT_STACK_TRACE_ELEMENT_NUMBER].getLineNumber();
		}

		message = callPoint + " " + message;

		switch (type) {
		case Log.ERROR:
			Log.e(caller, message);
			break;
		case Log.INFO:
			Log.i(caller, message);
			break;
		case Log.VERBOSE:
			Log.v(caller, message);
			break;
		case Log.DEBUG:
			Log.d(caller, message);
			break;
		case Log.WARN:
			Log.w(caller, message);
			break;
		default:
			Log.i(caller, message);
			break;
		}

		if (localLog && logToBuffer) {
			logToLocalLogBuffer(message);
		}
	}

	public static synchronized void clearLocalLogBuffer() {
		if (logBuffer != null) {
			logBuffer.setLength(0);
		}
	}

	public static synchronized void logToLocalLogBuffer(String message) {
		if (logBuffer != null) {
			logBuffer.append(message);
			logBuffer.append("\n");
		}
	}

	public static synchronized String getMessageLocalBuffer() {
		return getMessageLocalBuffer(false);
	}

	public static synchronized String getMessageLocalBuffer(boolean clearBuffer) {
		if (logBuffer != null) {
			String bufferValue = logBuffer.toString();
			if (clearBuffer) {
				clearLocalLogBuffer();
			}
			return bufferValue;
		}
		return null;
	}
}
