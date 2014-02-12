package com.nebel_tv.ui.fragment.base;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;
import com.nebel_tv.content.api.MediaWrapperResponse;
import com.nebel_tv.ui.fragment.base.WebViewUILoaderHelper.UIState;
import com.nebel_tv.wrapper.IvaWrapperManager;

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
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new NebelTVWebViewClient(webViewUILoaderHelper));
		webView.setWebChromeClient(new WebChromeClient() {
			 public boolean onConsoleMessage(ConsoleMessage cm) {
				    Log.d(BaseWebViewFragment.class.getName(), cm.message() + " -- From line "
				                         + cm.lineNumber() + " of "
				                         + cm.sourceId() );
				    return true;
			 }
		});
	}
    
    protected abstract boolean shouldOverrideUrlLoading(String url, int depth);
    
    private class NebelTVWebViewClient extends BaseWebViewClient {
    	
    	int counter = 0;
    	private String wrapperRequestUrl = null;
    	
    	public NebelTVWebViewClient(WebViewUILoaderHelper webViewUILoaderHelper) {
    		super(webViewUILoaderHelper);
    	}
    	
    	@Override
    	public void onLoadResource(WebView view, String url) {
    		super.onLoadResource(view, url);
    		if(url.startsWith(IvaWrapperManager.IVAWRAPPER_HOST)) {
    			wrapperRequestUrl = url;
    		}
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
    		if(wrapperRequestUrl!=null) {
    			new WrapperRequestTask().execute(wrapperRequestUrl);
    			wrapperRequestUrl = null;
    		}
    	}
    }
    
	private class WrapperRequestTask extends AsyncTask<String, Void, MediaWrapperResponse> {
		
		private String url;
		
		@Override
		protected void onPreExecute() {
			webViewUILoaderHelper.switchUIState(UIState.LOADING);
		}
		
		@Override
		protected MediaWrapperResponse doInBackground(String... params) {
			url = params[0];
			return IvaWrapperManager.getInstance().getData(url);
		}
		
		
		protected void onPostExecute(MediaWrapperResponse result) {
			webView.loadUrl("javascript:"+getFunctionCallString(result));
			webViewUILoaderHelper.switchUIState(UIState.SHOWING_DATA);
		}
		
		private String getFunctionCallString(MediaWrapperResponse response) {
			String callbackFuncName = IvaWrapperManager.getCallbackFuncName(url);
			return callbackFuncName+"(\""+formatJsonForJS(response.responseData)+"\");";
		}
		
		private String formatJsonForJS(String value) {
			return value.replace("\"", "\\\"").replace("\n", "");
		}

	}
 
}
