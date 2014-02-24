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
package com.nebel_tv.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import com.nebel_tv.ui.view.base.SeekBarWithText;
import com.nebel_tv.utils.DateTimeUtils;

public class VideoSeekBar extends SeekBarWithText {

	public VideoSeekBar(Context context) {
		super(context);
	}

	public VideoSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VideoSeekBar(Context context, AttributeSet attrs) {
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
