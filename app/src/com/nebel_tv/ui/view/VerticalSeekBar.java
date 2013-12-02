package com.nebel_tv.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.nebel_tv.ui.view.base.SeekBarWithText;

public class VerticalSeekBar extends SeekBarWithText {
	
    private OnSeekBarChangeListener onChangeListener;
    private int lastProgress = 0; 
    
    public VerticalSeekBar(Context context) {
        super(context);
    }
 
    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }
 
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }
 
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
 
        super.onDraw(c);
    }
    
    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onChangeListener){
        this.onChangeListener = onChangeListener;
    }
 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
 
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	if(onChangeListener!=null) {
        		onChangeListener.onStartTrackingTouch(this);
        	}
            setPressed(true);
            setSelected(true);
            break;
        case MotionEvent.ACTION_MOVE:
            super.onTouchEvent(event);
            int heightWithoutPaddings = getHeight()-getPaddingLeft()-getPaddingRight()+getThumbOffset();
            int progress = getMax() - (int) (getMax() * event.getY() / heightWithoutPaddings);
             
            // Ensure progress stays within boundaries
            if(progress < 0) {progress = 0;}
            if(progress > getMax()) {progress = getMax();}
            setProgress(progress);  // Draw progress
            if(progress != lastProgress) {
                // Only enact listener if the progress has actually changed
                lastProgress = progress;
                if(onChangeListener!=null) {
                	onChangeListener.onProgressChanged(this, progress, true);
                }
            }
             
            onSizeChanged(getWidth(), getHeight() , 0, 0);
            setPressed(true);
            setSelected(true);
            break;
        case MotionEvent.ACTION_UP:
        	if(onChangeListener!=null) {
        		onChangeListener.onStopTrackingTouch(this);
        	}
            setPressed(false);
            setSelected(false);
            break;
        case MotionEvent.ACTION_CANCEL:
            super.onTouchEvent(event);
            setPressed(false);
            setSelected(false);
            break;
        }
        return true;
    }
 
    public synchronized void setProgressAndThumb(int progress) {
        setProgress(progress);
        onSizeChanged(getWidth(), getHeight() , 0, 0);
        if(progress != lastProgress) {
            // Only enact listener if the progress has actually changed
            lastProgress = progress;
            if(onChangeListener!=null) {
            	onChangeListener.onProgressChanged(this, progress, false);
            }
        }
    }

	@Override
	protected String getText() {
		return getProgress()+"%";
	}
    
}