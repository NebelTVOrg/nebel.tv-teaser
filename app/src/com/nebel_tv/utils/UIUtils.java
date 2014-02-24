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
			// do nothing
		}
		dialog.setCancelable(false);
		dialog.setContentView(new ProgressBar(context));

		return dialog;
	}
}
