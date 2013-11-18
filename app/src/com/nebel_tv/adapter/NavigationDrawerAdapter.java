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

public class NavigationDrawerAdapter extends BaseExpandableListAdapter {
	
	public enum GroupType {
		MOOD(R.string.mood_title), 
		TOP_CATEGORIES(R.string.categories_title);
		
		private int resId;
		
		private GroupType(int resId) {
			this.resId = resId;
		}
		
		public int getResId() {
			return resId;
		}
	}
	
	private HashMap<GroupType, String[]> data;
	private LayoutInflater inflater;
	
	public NavigationDrawerAdapter(Context context, HashMap<GroupType, String[]> data) {
		this.data = data;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public String getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition)[childPosition];
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition*100 + childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if(convertView==null) {
			convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
		}
		
		((TextView)convertView).setText(getChild(groupPosition, childPosition));
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return getGroup(groupPosition).length;
	}

	@Override
	public String[] getGroup(int groupPosition) {
		return data.get(GroupType.values()[groupPosition]);
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
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if(convertView==null) {
			convertView = inflater.inflate(R.layout.drawer_header_item, parent, false);
		}
		
		GroupType type = GroupType.values()[groupPosition];
		String value = NebelTVApp.getContext().getString(type.getResId());
		((TextView)convertView).setText(value.toUpperCase());
		
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
