package com.nebel_tv.ui.fragment.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;

public abstract class BaseWebViewFragment extends Fragment {
	
	protected WebViewUILoaderHelper webViewUILoaderHelper;
	protected WebView webView;
	protected ProgressBar progressBar;
	
	protected BaseActivity getParentActivity() {
		return (BaseActivity)getActivity();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_webview, container, false);
		webView = (WebView) view.findViewById(R.id.webview);
		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		webViewUILoaderHelper = new WebViewUILoaderHelper(webView, progressBar);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new NebelTVWebViewClient(webViewUILoaderHelper));
		webView.setWebChromeClient(new WebChromeClient());
	}
    
    protected abstract boolean shouldOverrideUrlLoading(String url, int depth);
    
    private class NebelTVWebViewClient extends BaseWebViewClient {
    	
    	int counter = 0;
    	
    	public NebelTVWebViewClient(WebViewUILoaderHelper webViewUILoaderHelper) {
    		super(webViewUILoaderHelper);
    	}
    	
    	@Override
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		boolean ret =  BaseWebViewFragment.this.shouldOverrideUrlLoading(url, counter);
    		if(ret=true) {
//    			counter=0;
    		}
    		return ret;
    	}
    	
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		super.onPageFinished(view, url);
    		counter++;
    	}
    }
 
}
