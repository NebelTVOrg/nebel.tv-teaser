package com.nebel_tv.ui.fragment;

import java.util.HashMap;

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
import com.nebel_tv.model.TopView;
import com.nebel_tv.storage.LocalStorage;
import com.nebel_tv.utils.ConfigHelper;

public class WebViewFragment extends Fragment {
	
    public static enum UIState {
        LOADING, SHOWING_DATA
    }
	
	private WebView webView;
	private ProgressBar progressBar;
	
	private LocalStorage localStorage;
	private TopView currentTopView;
	
	private HashMap<TopView, String> configUrls;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_webview, container, false);
		
		webView = (WebView) view.findViewById(R.id.webview);
		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new NebelTVWebChromeClient());
		
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		localStorage = LocalStorage.from(getActivity());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		switchUIState(UIState.LOADING);
		configUrls = ConfigHelper.getInstance().getConfigUrls();
		loadCurrentTopView(localStorage.getLastScreen());
	}
	
	public void setCurrentTopView(int position) {
		TopView topView = TopView.values()[position];
		localStorage.setLastScreen(topView);
		loadCurrentTopView(topView);
	}
	
	private void loadCurrentTopView(TopView topView) {
		currentTopView = topView;
		String url = configUrls.get(currentTopView);
		if(url!=null) {
			webView.loadUrl(url);
		}
	}
	
    private void switchUIState(UIState state) {
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
    
    private class NebelTVWebChromeClient extends WebViewClient {
    	
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
    		super.onPageStarted(view, url, favicon);
    		switchUIState(UIState.LOADING);
    	}
    	
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		super.onPageFinished(view, url);
    		switchUIState(UIState.SHOWING_DATA);
    	}
    	
    	@Override
    	public void onReceivedError(WebView view, int errorCode,
    			String description, String failingUrl) {
    		super.onReceivedError(view, errorCode, description, failingUrl);
    		switchUIState(UIState.SHOWING_DATA);
    	}
    }

}
