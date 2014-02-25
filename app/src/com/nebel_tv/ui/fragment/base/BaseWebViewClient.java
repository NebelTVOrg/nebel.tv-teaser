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

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nebel_tv.ui.fragment.base.WebViewUILoaderHelper.UIState;
import com.nebel_tv.utils.UIUtils;

public class BaseWebViewClient extends WebViewClient {

	private WebViewUILoaderHelper webViewUILoaderHelper;

	public BaseWebViewClient() {
		this(null);
	}

	public BaseWebViewClient(WebViewUILoaderHelper webViewUILoaderHelper) {
		this.webViewUILoaderHelper = webViewUILoaderHelper;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		
		if (webViewUILoaderHelper != null) {
			webViewUILoaderHelper.switchUIState(UIState.LOADING);
		}
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		if (webViewUILoaderHelper != null) {
			webViewUILoaderHelper.switchUIState(UIState.SHOWING_DATA);
		}
	}

	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		super.onReceivedError(view, errorCode, description, failingUrl);
		if (webViewUILoaderHelper != null) {
			webViewUILoaderHelper.switchUIState(UIState.SHOWING_DATA);
		}
		UIUtils.showMessage(description);
	}

}
