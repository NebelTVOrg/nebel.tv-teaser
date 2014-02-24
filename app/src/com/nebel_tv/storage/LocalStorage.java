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
package com.nebel_tv.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nebel_tv.adapter.NavigationDrawerAdapter.GroupType;
import com.nebel_tv.model.Mood;
import com.nebel_tv.model.TopView;

public class LocalStorage {
	private static final String KEY_LAST_SCREEN = "KEY_LAST_SCREEN";
	private static final String KEY_LAST_MOOD = "KEY_LAST_MOOD";
	private static final String KEY_SHOW_TIME_REMAINING = "KEY_SHOW_TIME_REMAINING";
	private static final String KEY_POLICY_ACCEPTED = "KEY_POLICY_ACCEPTED";

	private final SharedPreferences mSharedPreferences;
	private static LocalStorage sInstance;

	private LocalStorage(final Context context) {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static synchronized LocalStorage from(final Context context) {
		if (LocalStorage.sInstance == null) {
			LocalStorage.sInstance = new LocalStorage(context);
		}
		return LocalStorage.sInstance;
	}

	public TopView getLastScreen() {
		return TopView.values()[mSharedPreferences.getInt(KEY_LAST_SCREEN, 0)];
	}

	public void setLastScreen(TopView lastScreen) {
		mSharedPreferences.edit().putInt(KEY_LAST_SCREEN, lastScreen.ordinal()).commit();
	}

	public Mood getLastMood() {
		return Mood.values()[mSharedPreferences.getInt(KEY_LAST_MOOD, 0)];
	}

	public void setLastMood(Mood lastMood) {
		mSharedPreferences.edit().putInt(KEY_LAST_MOOD, lastMood.ordinal()).commit();
	}

	public boolean getNavigationGroupState(GroupType groupType) {
		return mSharedPreferences.getBoolean(groupType.toString(), true);
	}

	public void setNavigationGroupState(GroupType groupType, boolean expanded) {
		mSharedPreferences.edit().putBoolean(groupType.toString(), expanded).commit();
	}

	public boolean isShowTimeRemaining() {
		return mSharedPreferences.getBoolean(KEY_SHOW_TIME_REMAINING, false);
	}

	public void setShowTimeRemaining(boolean value) {
		mSharedPreferences.edit().putBoolean(KEY_SHOW_TIME_REMAINING, value).commit();
	}

	public boolean isPolicyAccepted() {
		return mSharedPreferences.getBoolean(KEY_POLICY_ACCEPTED, false);
	}

	public void setPolicyAccepted() {
		mSharedPreferences.edit().putBoolean(KEY_POLICY_ACCEPTED, true).commit();
	}
}
