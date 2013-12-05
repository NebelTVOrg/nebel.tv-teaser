package com.nebel_tv.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;

public class VerticalSeekBar extends SeekBar {
	
	public static enum ThumbOrientation {
		LEFT,
		RIGHT
	}
	
	private static final int THUMB_TEXT_PADDING = 
		NebelTVApp.getContext().getResources().getDimensionPixelSize(R.dimen.vertical_seek_bar__thumb_text_padding);
	private static final int THUMB_TEXT_SIZE = 
		NebelTVApp.getContext().getResources().getDimensionPixelSize(R.dimen.seek_bal_labels_size);
	
    private OnSeekBarChangeListener onChangeListener;
    private Drawable thumb;
    private ThumbOrientation thumbOrientation = ThumbOrientation.LEFT;
    private Paint textPaint;
    private Matrix transform;
    private int lastProgress = 0; 
    
    public VerticalSeekBar(Context context) {
        super(context);
        init();
    }
 
    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
 
    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        textPaint = new Paint(); 
        textPaint.setColor(Color.WHITE); 
        textPaint.setTextSize(THUMB_TEXT_SIZE); 
        transform = new Matrix();
    }
 
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }
 
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }
    
    @Override
    public void setThumb(Drawable thumb) {
    	super.setThumb(thumb);
    	if(this.thumb==null) {
    		this.thumb = thumb;
    	}
    }
    
    public void setThumbOrientation(ThumbOrientation thumbOrientation) {
    	this.thumbOrientation = thumbOrientation;
    }
    
    public void setThumbLabel(String label) {
    	if(thumb==null) {
    		return;
    	}
    	setThumb(writeOnDrawable(thumb, label));
    }
    
    private Drawable writeOnDrawable(Drawable drawable, String text){

    	Bitmap bm = ((BitmapDrawable)drawable).getBitmap();
    	if(bm==null) {
    		return drawable;
    	}
    	bm = bm.copy(Bitmap.Config.ARGB_8888, true);
    	transform.setRotate(90, bm.getWidth()/2, bm.getHeight()/2);
    	
    	float[] pts = new float[2];
    	
        int textWidth = (int)textPaint.measureText(text);
    	
    	pts[0] = bm.getWidth()/2;
        pts[1] = thumbOrientation==
        	ThumbOrientation.LEFT?textWidth+THUMB_TEXT_PADDING:bm.getHeight()-THUMB_TEXT_PADDING;
        
        transform.mapPoints(pts);

        Canvas canvas = new Canvas(bm);
        canvas.save();
        canvas.setMatrix(transform);
        canvas.drawText(text, pts[0], pts[1], textPaint);
        canvas.restore();

        return new BitmapDrawable(NebelTVApp.getContext().getResources(), bm);
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
                setThumbLabel(getText());
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
        setThumbLabel(getText());
        onSizeChanged(getWidth(), getHeight() , 0, 0);
        if(progress != lastProgress) {
            // Only enact listener if the progress has actually changed
            lastProgress = progress;
            if(onChangeListener!=null) {
            	onChangeListener.onProgressChanged(this, progress, false);
            }
        }
    }

	protected String getText() {
		return getProgress()+"%";
	}
    
}