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
package com.nebel_tv.model;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;

public enum TopView {
	FEED(R.string.top_friends_feed), 
	WHATS_HOT(R.string.top_whats_hot), 
	WHATS_CLOSE(R.string.top_whats_close), 
	WATCH_LATER(R.string.top_watch_later), 
	RECENTLY_VIEWED(R.string.top_recently_viewed), 
	SPACE(R.string.top_space), 
	CONTENT(R.string.top_content), 
	WALLET(R.string.top_wallet), 
	SETTINGS(R.string.top_settings); 

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
		for (int i = 0; i < titles.length; i++) {
			titles[i] = NebelTVApp.getContext().getString(values[i].getStringRes());
		}

		return titles;
	}
}
