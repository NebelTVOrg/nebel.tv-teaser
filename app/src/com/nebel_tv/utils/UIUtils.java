package com.nebel_tv.utils;

import android.widget.Toast;

import com.nebel_tv.NebelTVApp;

public class UIUtils {

	public static void showMessage(int resId) {
		showMessage(NebelTVApp.getContext().getString(resId));
	}
	
	public static void showMessage(String message) {
		Toast.makeText(NebelTVApp.getContext(), message, Toast.LENGTH_LONG).show();
	}
}
