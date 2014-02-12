package com.nebel_tv.wrapper;

import java.lang.reflect.Method;

import android.net.Uri;

import com.nebel_tv.content.api.IMediaWrapper;
import com.nebel_tv.content.api.MediaWrapper;
import com.nebel_tv.content.api.MediaWrapperResponse;

public class IvaWrapperManager {
	
	public static final String IVAWRAPPER_HOST = "http://nebel.tv";
	public static final String CALLBACK_PARAM_NAME = "callback";
	private static final String INTERNAL_DEX_DIR = "out_dex";
	private static final String WRAPPER_LIB_FILENAME = "nebel-tv-mediawrapper.jar";
	private static final String CLASS_NAME = "com.nebel_tv.content.api.MediaWrapper";
	private static final String METHOD_NAME = "getMediaData";
	private static final String RESPONSE_DATA_FIELD = "responseData";
	private static final String FEED_CLASS = " com.nebel_tv.content.xmlparser.nodes.Feed";
	
	private static IvaWrapperManager instance;
	
	public static synchronized IvaWrapperManager getInstance() {
		if(instance==null) {
			instance = new IvaWrapperManager();
		}
		return instance;
	}
	
	private Object wrapperInstance;
	private Method wrapperMediaDataMethod;
	private IMediaWrapper mediaWrapper;
	
	private IvaWrapperManager() {
		mediaWrapper = new MediaWrapper();
		//TODO fix dynamic linking
//		File configFolder = ConfigHelper.getInstance().getConfigDirectory();
//		File wrapperLibFile = new File(configFolder, WRAPPER_LIB_FILENAME);
//		
//		if(!wrapperLibFile.exists()) {
//			return;
//		}
//		
//		final File optimizedDexOutputPath = NebelTVApp.getContext().getDir(INTERNAL_DEX_DIR, 0);
//		ClassLoader classLoader = new DexClassLoader(wrapperLibFile.getAbsolutePath(),
//		optimizedDexOutputPath.getAbsolutePath(),null, NebelTVApp.getContext().getClassLoader());
//		
//		try {
//			classLoader.loadClass(FEED_CLASS);
//			Class<?> wrapperClass = classLoader.loadClass(CLASS_NAME);
//			wrapperInstance = (Object) wrapperClass.newInstance();
//			wrapperMediaDataMethod = wrapperClass.getMethod(METHOD_NAME, String.class);
//		} catch (Exception e) {
//			D.e(e);
//		}
	}
	
	public MediaWrapperResponse getData(String url) {
		return mediaWrapper.getMediaData(url);
		//TODO fix dynamic linking
//		try {
//		
//			Object rawResponse = wrapperMediaDataMethod.invoke(wrapperInstance, url);
//			Field[] responseFields = rawResponse.getClass().getFields();
//			
//			String responseData = null;
//			for(Field field : responseFields) {
//				if(RESPONSE_DATA_FIELD.equals(field.getName())) {
//					responseData = (String) field.get(rawResponse);
//					break;
//				}
//			}
//			return responseData;
//		} catch (Exception e) {
//			D.e(e);
//			return null;
//		}
		
	}
	
	public static String getCallbackFuncName(String url) {
		if(url==null) {
			return null;
		}
		
		Uri uri = Uri.parse(url);
		return uri.getQueryParameter(CALLBACK_PARAM_NAME);
	}
}
