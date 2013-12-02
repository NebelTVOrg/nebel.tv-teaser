package com.nebel_tv;

import android.app.Application;
import android.content.Context;

import com.nebel_tv.storage.LocalStorage;
import com.nebel_tv.utils.DownloadManagerHelper;

public class NebelTVApp extends Application {
	
	public static final String FLURRY_API_KEY = "MBPCG7WZGRPXP5DHGMCH";
	
	private static Context context;
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		final LocalStorage localStorage = LocalStorage.from(context);
		if(localStorage.isFirstRun()) {
			localStorage.setFirstRun();
			DownloadManagerHelper.startVideoDownload(context);
		}
	}
	
	public static Context getContext() {
		return context;
	}

}
