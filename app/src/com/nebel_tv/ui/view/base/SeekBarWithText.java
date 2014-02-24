/**
 * Copyright (C) 2014 Nebel TV (http://nebel.tv)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.nebel_tv.ui.view.base;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.SeekBar;

public abstract class SeekBarWithText extends SeekBar {

	private static final int THUMB_TEXT_SIZE = NebelTVApp.getContext().getResources().getDimensionPixelSize(R.dimen.seek_bal_labels_size);

	protected String currentText;
	protected boolean autoChangeTimeText;
	protected Paint textPaint;

	public SeekBarWithText(Context context) {
		super(context);
		init();
	}

	public SeekBarWithText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SeekBarWithText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	protected void init() {
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(THUMB_TEXT_SIZE);
		textPaint.setTextAlign(Align.CENTER);
		autoChangeTimeText = true;
		currentText = getText();
	}

	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		if (autoChangeTimeText) {
			notifyTimeTextPositionChange();
		}
	}

	public void setAutochange(boolean value) {
		autoChangeTimeText = value;
	}

	public void notifyTimeTextPositionChange() {
		currentText = getText();
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();

		int paddingLeft = this.getPaddingLeft();
		int paddingRight = this.getPaddingRight();
		int thumbOffset = this.getThumbOffset();
		int progress = this.getProgress();
		int maxProgress = this.getMax();

		int width = this.getWidth() - paddingLeft - paddingRight;
		double percentProgress = (double) progress / (double) maxProgress;
		int textHeight = (int) (Math.abs(textPaint.ascent()) + textPaint.descent() + 1);
		int middleOfThumbControl = (int) ((double) width * percentProgress);

		float x = middleOfThumbControl + thumbOffset + this.getPaddingLeft();
		float y = textHeight;

		canvas.drawText(currentText, x, y, textPaint);

		canvas.restore();
	}

	protected abstract String getText();
}
