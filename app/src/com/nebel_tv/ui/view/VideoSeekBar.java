package com.nebel_tv.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.nebel_tv.ui.view.base.SeekBarWithText;
import com.nebel_tv.utils.DateTimeUtils;

public class VideoSeekBar extends SeekBarWithText {

	private String durationTimeText;

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
		durationTimeText = DateTimeUtils.getDefaultTimeFormatter().print(getMaxInMillis());
	}
	
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	    canvas.save();
	    
	    int paddingLeft = this.getPaddingLeft();
	    int paddingRight = this.getPaddingRight();
	    
	    int width = this.getWidth()-paddingLeft-paddingRight;
	    float textWidth = textPaint.measureText(durationTimeText);
	    int textHeight = (int) (Math.abs(textPaint.ascent()) + textPaint.descent() + 1);

	    float y = textHeight;

	    canvas.drawText(durationTimeText, paddingLeft+width-textWidth, y, textPaint);

	    canvas.restore();
	}

	@Override
	protected String getText() {
		return DateTimeUtils.getDefaultTimeFormatter().print(getProgressInMillis());
	}
	
	private long getProgressInMillis() {
		return DateTimeUtils.getSecValueInMillis(getProgress());
	}
	
	private long getMaxInMillis() {
		return DateTimeUtils.getSecValueInMillis(getMax());
	}

}
