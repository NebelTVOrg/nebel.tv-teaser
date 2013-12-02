package com.nebel_tv.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class VerticalSeekBar extends SeekBar {
	
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
    	
//	    int paddingLeft = this.getPaddingLeft();
//	    int paddingRight = this.getPaddingRight();
//	    int progress = this.getProgress();
//	    int maxProgress = this.getMax();
//    	int height = this.getHeight()-paddingLeft-paddingRight;
//	    double percentProgress = (double) progress / (double) maxProgress;
//	    int middleOfThumbControl = (int) ((double) height * (1-percentProgress)); 
//	    int textHeight = (int) (Math.abs(textPaint.ascent()) + textPaint.descent() + 1);
//	    float textWidth = textPaint.measureText(getText());
//    	
//    	c.drawText(getText(), getWidth()-textWidth, middleOfThumbControl+paddingRight-getThumbOffset(), textPaint);
    	
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
            
            int availableHeight = getHeight()-getPaddingLeft()-getPaddingRight();
            float scale;
            float progress = 0;
            int y = (int) event.getY();
            if (y >= availableHeight+getPaddingRight()) {
            	scale = 1.0f;
            } else if (y <= getPaddingRight()) {
            	scale = 0.0f;
            } else {
            	scale = (float)(y - getPaddingRight()) / (float)availableHeight;
            }
     
            final int max = getMax();
            progress = max -  scale * max;

            setProgress((int)progress);  // Draw progress
            if(progress != lastProgress) {
                // Only enact listener if the progress has actually changed
                lastProgress = (int)progress;
                if(onChangeListener!=null) {
                	onChangeListener.onProgressChanged(this, lastProgress, true);
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

//	@Override
	protected String getText() {
		return getProgress()+"%";
	}
    
}