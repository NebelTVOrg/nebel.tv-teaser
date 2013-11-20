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

}
