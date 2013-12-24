package com.nebel_tv.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.WindowManager.BadTokenException;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nebel_tv.NebelTVApp;

public class UIUtils {

	public static void showMessage(int resId) {
		showMessage(NebelTVApp.getContext().getString(resId));
	}
	
	public static void showMessage(String message) {
		Toast.makeText(NebelTVApp.getContext(), message, Toast.LENGTH_LONG).show();
	}
	
	public static ProgressDialog createBorderlessProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        try {
            dialog.show();
        } catch (BadTokenException e) {
        	//do nothing
        }
        dialog.setCancelable(false);
        dialog.setContentView(new ProgressBar(context));
        
        return dialog;
	}
}
