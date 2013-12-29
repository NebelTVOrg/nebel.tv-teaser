package com.nebel_tv.model;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;

public enum TopView {
	FRIENDS_FEED(R.string.top_friends_feed),
	WHATS_CLOSE(R.string.top_whats_close),
	RECENTLY_VIEWED(R.string.top_recently_viewed),
	WHATS_HOT(R.string.top_whats_hot),
	PICTURES(R.string.top_pictures),
	RECOMMENDED(R.string.top_recommended);
	
	private int stringRes;
	
	private TopView(int stringRes) {
		this.stringRes = stringRes;
	}
	
	public int getStringRes() {
		return stringRes;
	}
	
	public static String[] getTitles() {
		TopView[] values = TopView.values();
		String[] titles = new String[values.length];
		for(int i=0; i<titles.length; i++) {
			titles[i] = NebelTVApp.getContext().getString(values[i].getStringRes());
		}
		
		return titles;
	}
}
