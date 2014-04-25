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
package com.nebel_tv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;
import com.nebel_tv.model.Mood;

public class MoodAdapter extends BaseAdapter {

	private Mood[] moods;
	private LayoutInflater inflater;

	public MoodAdapter(Context context) {
		moods = Mood.values();
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return moods.length;
	}

	@Override
	public Mood getItem(int pos) {
		return moods[pos];
	}

	@Override
	public long getItemId(int pos) {
		return moods[pos].ordinal();
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.mood_item, parent, false);
			convertView.setTag(new ViewHolder(convertView));
		}
		((ViewHolder) convertView.getTag()).populateView(getItem(pos));
		return convertView;
	}

	private static class ViewHolder {

		private ImageView moodIcon;
		private TextView moodTitle;

		public ViewHolder(View v) {
			moodIcon = (ImageView) v.findViewById(R.id.img_mood_icon);
			moodTitle = (TextView) v.findViewById(R.id.txt_mood_title);
		}

		public void populateView(Mood mood) {
			moodIcon.setImageResource(mood.getIconRes());
			moodTitle.setText(NebelTVApp.getContext().getString(mood.getStringRes()));
		}
	}
}
