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
package com.nebel_tv;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.nebel_tv.utils.D;

public class NebelTVApp extends Application {

	public static final String FLURRY_API_KEY = "MBPCG7WZGRPXP5DHGMCH";
	public final static boolean FRONTEND_DEBUG_MODE = true;

	private static Context context;
	private static NebelTVUncaughtExceptionHandler exceptionHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		D.enableDebug();
		FlurryAgent.setCaptureUncaughtExceptions(false);
		exceptionHandler = new NebelTVUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
	}

	public static Context getContext() {
		return context;
	}

	public static void setCurrentHandler(UncaughtExceptionHandler handler) {
		if (exceptionHandler != null) {
			exceptionHandler.setCurrentHandler(handler);
		}
	}

	public void removeCurrentHandler() {
		if (exceptionHandler != null) {
			exceptionHandler.removeCurrentHandler();
		}
	}

	private class NebelTVUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

		private Thread.UncaughtExceptionHandler defaultHandler;
		private Thread.UncaughtExceptionHandler currentHandler;

		public NebelTVUncaughtExceptionHandler() {
			defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		}

		@Override
		public void uncaughtException(Thread thread, Throwable throwable) {
			if (currentHandler != null) {
				currentHandler.uncaughtException(thread, throwable);
			}
			if (defaultHandler != null) {
				defaultHandler.uncaughtException(thread, throwable);
			}
		}

		public void setCurrentHandler(UncaughtExceptionHandler handler) {
			this.currentHandler = handler;
		}

		public void removeCurrentHandler() {
			this.currentHandler = null;
		}
	}
}
