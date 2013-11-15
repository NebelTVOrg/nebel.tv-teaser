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
import com.nebel_tv.utils.ConfigHelper;

public class WebViewFragment extends Fragment {
	
    public static enum UIState {
        LOADING, SHOWING_DATA
    }
    
    private static final String EXTRA_TOP_VIEW_KEY = "EXTRA_TOP_VIEW_KEY";
	
	private WebView webView;
	private ProgressBar progressBar;
	private ViewGroup webViewContainer;
	
	private TopView topView;
	private HashMap<TopView, String> configUrls;
	private Bundle savedInstanceState;
	
	public static WebViewFragment newInstance(TopView topView) {
		WebViewFragment f = new WebViewFragment();
		
		Bundle args = new Bundle();
        args.putInt(EXTRA_TOP_VIEW_KEY, topView.ordinal());
        f.setArguments(args);
        
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_webview, container, false);
		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		webViewContainer = (ViewGroup) view.findViewById(R.id.container_webview);
	
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		
		Bundle args = getArguments();
		if(args!=null) {
			topView = TopView.values()[args.getInt(EXTRA_TOP_VIEW_KEY, 0)];
		} else {
			topView = TopView.FRIENDS_FEED;
		}

		if(webView==null) {
			webView = new WebView(getActivity());
			
			webView.getSettings().setJavaScriptEnabled(true);
			webView.setWebViewClient(new NebelTVWebChromeClient());
		}
		
		this.savedInstanceState = savedInstanceState;
		if(savedInstanceState!=null) {
			webView.restoreState(savedInstanceState);
		}
		
		webViewContainer.addView(webView);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		if(savedInstanceState==null) {
			switchUIState(UIState.LOADING);
			configUrls = ConfigHelper.getInstance().getConfigUrls();
			loadTopView();
		} else {
			savedInstanceState = null;
		}
	}
	
	@Override
	public void onDetach() {
		webViewContainer.removeView(webView);
		super.onDetach();
	}
	
	private void loadTopView() {
		String url = configUrls.get(topView);
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
