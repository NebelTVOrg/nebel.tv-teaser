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
