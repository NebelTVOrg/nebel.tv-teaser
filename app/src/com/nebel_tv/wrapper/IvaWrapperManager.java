package com.nebel_tv.wrapper;

import android.net.Uri;

import com.nebel_tv.content.api.IContentWrapper;
import com.nebel_tv.content.api.ContentWrapper;
import com.nebel_tv.content.api.WrapperResponse;

public class IvaWrapperManager {
	
	public static final String IVAWRAPPER_HOST = "http://nebel.tv";
	public static final String CALLBACK_PARAM_NAME = "callback";

	
	private static IvaWrapperManager instance;
	
	public static synchronized IvaWrapperManager getInstance() {
		if(instance==null) {
			instance = new IvaWrapperManager();
		}
		return instance;
	}
	

	private IContentWrapper mediaWrapper;
	
	private IvaWrapperManager() {
		mediaWrapper = new ContentWrapper();
	}
	
	public WrapperResponse getData(String url) {
		return mediaWrapper.getMediaData(url);
	}
	
	public static String getCallbackFuncName(String url) {
		if(url==null) {
			return null;
		}
		
		Uri uri = Uri.parse(url);
		return uri.getQueryParameter(CALLBACK_PARAM_NAME);
	}
}
