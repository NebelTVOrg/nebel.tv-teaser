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
package com.nebel_tv.ui.fragment.base;

import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class WebViewUILoaderHelper {

	public static enum UIState {
		LOADING, SHOWING_DATA
	}

	private WebView webView;
	private ProgressBar progressBar;

	public WebViewUILoaderHelper(WebView webView, ProgressBar progressBar) {
		this.webView = webView;
		this.progressBar = progressBar;
	}

	public void switchUIState(UIState state) {
		webView.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);

		switch (state) {
		case LOADING:
			progressBar.setVisibility(View.VISIBLE);
			break;
		case SHOWING_DATA:
			webView.setVisibility(View.VISIBLE);
			break;
		}
	}
}
