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

import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;
import com.nebel_tv.storage.LocalStorage;

public class NavigationDrawerAdapter extends BaseExpandableListAdapter {

	public enum GroupType {
		TOP_CATEGORIES(R.string.categories_title), MOOD(R.string.mood_title);

		private int resId;

		private GroupType(int resId) {
			this.resId = resId;
		}

		public int getResId() {
			return resId;
		}
	}

	private static final int CHILD_ID_PARAMETER = 100;

	private HashMap<GroupType, String[]> data;
	private LayoutInflater inflater;
	private LocalStorage localStorage;

	public NavigationDrawerAdapter(Context context, HashMap<GroupType, String[]> data) {
		this.data = data;
		inflater = LayoutInflater.from(context);
		localStorage = LocalStorage.from(context);
	}

	@Override
	public String getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition)[childPosition];
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition * CHILD_ID_PARAMETER + childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
		}

		((TextView) convertView).setText(getChild(groupPosition, childPosition));
		if (getGroupEnum(groupPosition) == GroupType.MOOD && localStorage.getLastMood().ordinal() == childPosition) {
			convertView.setBackgroundResource(R.color.drawer_item_selected_color);
		} else {
			convertView.setBackgroundResource(android.R.color.transparent);
		}

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return getGroup(groupPosition).length;
	}

	@Override
	public String[] getGroup(int groupPosition) {
		return data.get(getGroupEnum(groupPosition));
	}

	public GroupType getGroupEnum(int groupPosition) {
		return GroupType.values()[groupPosition];
	}

	@Override
	public int getGroupCount() {
		return data.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_header_item, parent, false);
		}

		GroupType type = getGroupEnum(groupPosition);
		String value = NebelTVApp.getContext().getString(type.getResId());
		((TextView) convertView).setText(value.toUpperCase());

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
