package com.nebel_tv;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.nebel_tv.utils.D;

public class NebelTVApp extends Application {
	
	public static final String FLURRY_API_KEY = "MBPCG7WZGRPXP5DHGMCH";
	public final static boolean FRONTEND_DEBUG_MODE = true; 
	
	private static Context context;
	private static NebelTVUncaughtExceptionHandler exceptionHandler;
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		D.enableDebug();
		FlurryAgent.setCaptureUncaughtExceptions(false);
		exceptionHandler = new NebelTVUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
	}
	
	public static Context getContext() {
		return context;
	}
	
    public static void setCurrentHandler(UncaughtExceptionHandler handler) {
    	if(exceptionHandler!=null) {
    		exceptionHandler.setCurrentHandler(handler);
    	}
    }
    
    public void removeCurrentHandler() {
    	if(exceptionHandler!=null) {
    		exceptionHandler.removeCurrentHandler();
    	}
    }
	
	private class NebelTVUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	    private Thread.UncaughtExceptionHandler defaultHandler;
	    private Thread.UncaughtExceptionHandler currentHandler;

	    public NebelTVUncaughtExceptionHandler() {
	        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	    }

	    @Override
	    public void uncaughtException(Thread thread, Throwable throwable) {
	    	if(currentHandler!=null) {
	    		currentHandler.uncaughtException(thread, throwable);
	    	}
	        if(defaultHandler != null) {
	            defaultHandler.uncaughtException(thread, throwable);
	        }
	    }
	    
	    public void setCurrentHandler(UncaughtExceptionHandler handler) {
	    	this.currentHandler = handler;
	    }
	    
	    public void removeCurrentHandler() {
	    	this.currentHandler = null;
	    }
	}

}
