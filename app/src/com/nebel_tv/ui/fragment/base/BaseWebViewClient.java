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
		if(webViewUILoaderHelper!=null) {
			webViewUILoaderHelper.switchUIState(UIState.LOADING);
		}
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		if(webViewUILoaderHelper!=null) {
		webViewUILoaderHelper.switchUIState(UIState.SHOWING_DATA);
		}
	}
	
	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		super.onReceivedError(view, errorCode, description, failingUrl);
		if(webViewUILoaderHelper!=null) {
			webViewUILoaderHelper.switchUIState(UIState.SHOWING_DATA);
		}
		UIUtils.showMessage(description);
	}

}
