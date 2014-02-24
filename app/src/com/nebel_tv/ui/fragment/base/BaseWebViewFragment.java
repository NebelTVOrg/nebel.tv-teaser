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
import com.nebel_tv.activity.MediaPlaybackActivity;
import com.nebel_tv.activity.base.BaseActivity;
import com.nebel_tv.content.api.VideoAssetsWrapper;
import com.nebel_tv.content.api.WrapperResponse;
import com.nebel_tv.ui.fragment.base.WebViewUILoaderHelper.UIState;
import com.nebel_tv.utils.D;
import com.nebel_tv.wrapper.ContentWrapperManager;

public abstract class BaseWebViewFragment extends Fragment {

	protected WebViewUILoaderHelper webViewUILoaderHelper;
	protected WebView webView;
	protected ProgressBar progressBar;

	// private static final String[] VIDEO_URLS = new String[] {
	// "http://54.201.170.111/assets/001-180p-185kb.mp4",
	// "http://54.201.170.111/assets/001-270p-686kb.mp4",
	// "http://54.201.170.111/assets/001-720p-2500kb.mp4" };

	protected BaseActivity getParentActivity() {
		return (BaseActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
				Log.d(BaseWebViewFragment.class.getName(), cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
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
			if (url.startsWith(ContentWrapperManager.WRAPPER_HOST)) {
				wrapperRequestUrl = url;
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			boolean ret = BaseWebViewFragment.this.shouldOverrideUrlLoading(url, counter);
			if (ret = true) {
				// counter=0;
			}
			return ret;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			counter++;
			if (wrapperRequestUrl != null) {
				new WrapperRequestTask().execute(wrapperRequestUrl);
				wrapperRequestUrl = null;
			}
		}
	}

	private class WrapperRequestTask extends AsyncTask<String, Void, WrapperResponse> {

		private String url;

		@Override
		protected void onPreExecute() {
			webViewUILoaderHelper.switchUIState(UIState.LOADING);
		}

		@Override
		protected WrapperResponse doInBackground(String... params) {
			url = params[0];
			return ContentWrapperManager.getInstance().getData(url);
		}

		protected void onPostExecute(WrapperResponse result) {
			if (result.responseResult == WrapperResponse.ResponseResult.Ok) {

				switch (result.responseType) {
				case Content:
					webView.loadUrl("javascript:" + getFunctionCallString(result));
					webViewUILoaderHelper.switchUIState(UIState.SHOWING_DATA);
					break;
				case VideoAssets:
					VideoAssetsWrapper wrapper = new VideoAssetsWrapper(result.responseData);
					String[] urls = wrapper.getVideoURLs();
					if (urls != null && url.length() != 0) {
						MediaPlaybackActivity.launch(getActivity(), urls);
					}
					break;
				}
			} else {
				D.w("Wrapper: url: " + url + ", response: " + result.responseResult);
			}
		}

		private String getFunctionCallString(WrapperResponse response) {
			String callbackFuncName = ContentWrapperManager.getCallbackFuncName(url);
			return callbackFuncName + "(\"" + formatJsonForJS(response.responseData) + "\");";
		}

		private String formatJsonForJS(String value) {
			return escape(value);
		}

		/**
		 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
		 * (U+0000 through U+001F).
		 * 
		 * @warn Replace method with regexp
		 * @param s
		 *            The input JSON string
		 * 
		 * @return Well-formatted JSON string for JavaScript
		 */
		private String escape(String s) {
			if (s == null)
				return null;
			StringBuffer sb = new StringBuffer();
			escape(s, sb);
			return sb.toString();
		}

		/**
		 * @note: Reference: http://www.unicode.org/versions/Unicode5.1.0
		 * @param s
		 * @param sb
		 */
		private void escape(String s, StringBuffer sb) {
			if (s == null)
				return;

			final int len = s.length();
			for (int i = 0; i < len; i++) {
				char ch = s.charAt(i);
				switch (ch) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '/':
					sb.append("\\/");
					break;
				default:
					if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
						String ss = Integer.toHexString(ch);
						sb.append("\\u");
						for (int k = 0; k < 4 - ss.length(); k++) {
							sb.append('0');
						}
						sb.append(ss.toUpperCase());
					} else {
						sb.append(ch);
					}
				}
			}
		}
	}
}
