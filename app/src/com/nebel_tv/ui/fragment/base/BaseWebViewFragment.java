package com.nebel_tv.ui.fragment.base;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.nebel_tv.R;
import com.nebel_tv.activity.MainActivity;
import com.nebel_tv.utils.UIUtils;

public abstract class BaseWebViewFragment extends Fragment {

	public static enum UIState {
        LOADING, SHOWING_DATA
    }
	
	protected WebView webView;
	protected ProgressBar progressBar;
	
	protected MainActivity getMainActivity() {
		return (MainActivity)getActivity();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_webview, container, false);
		webView = (WebView) view.findViewById(R.id.webview);
		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
	
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new NebelTVWebChromeClient());
	}
	
    protected void switchUIState(UIState state) {
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
    
    protected abstract boolean shouldOverrideUrlLoading(String url, int depth);
    
    private class NebelTVWebChromeClient extends WebViewClient {
    	
    	int counter = 0;
    	
    	@Override
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		return BaseWebViewFragment.this.shouldOverrideUrlLoading(url, counter);
    	}
    	
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
    		super.onPageStarted(view, url, favicon);
    		switchUIState(UIState.LOADING);
    	}
    	
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		super.onPageFinished(view, url);
    		switchUIState(UIState.SHOWING_DATA);
    		counter++;
    	}
    	
    	@Override
    	public void onReceivedError(WebView view, int errorCode,
    			String description, String failingUrl) {
    		super.onReceivedError(view, errorCode, description, failingUrl);
    		switchUIState(UIState.SHOWING_DATA);
    		UIUtils.showMessage(description);
    	}
    }
 
}
