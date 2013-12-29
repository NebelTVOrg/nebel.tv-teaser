package com.nebel_tv.model;

import com.nebel_tv.R;

public enum Mood {
	FAMILY(R.string.mood_family, R.drawable.ic_mood_family),
	KIDS(R.string.mood_kids, R.drawable.ic_mood_kids),
	ROMANCE(R.string.mood_romance, R.drawable.ic_mood_romance);
	
	private int stringRes;
	private int iconRes;
	
	private Mood(int stringRes, int iconRes) {
		this.stringRes = stringRes;
		this.iconRes = iconRes;
	}

	public int getStringRes() {
		return stringRes;
	}

	public int getIconRes() {
		return iconRes;
	}
	
}
