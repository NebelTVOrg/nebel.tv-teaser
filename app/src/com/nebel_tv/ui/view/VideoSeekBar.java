package com.nebel_tv.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import com.nebel_tv.ui.view.base.SeekBarWithText;
import com.nebel_tv.utils.DateTimeUtils;

public class VideoSeekBar extends SeekBarWithText {

	public VideoSeekBar (Context context) {
		super(context);
	}

	public VideoSeekBar (Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VideoSeekBar (Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public synchronized void setMax(int max) {
		super.setMax(max);
	}

	@Override
	protected String getText() {
		return DateTimeUtils.getDefaultTimeFormatter().print(getProgressInMillis());
	}
	
	private long getProgressInMillis() {
		return DateTimeUtils.getSecValueInMillis(getProgress());
	}

}
