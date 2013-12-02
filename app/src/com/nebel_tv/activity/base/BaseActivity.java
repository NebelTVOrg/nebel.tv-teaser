package com.nebel_tv.activity.base;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.flurry.android.FlurryAgent;
import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;

public abstract class BaseActivity extends ActionBarActivity {
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	onBackPressed();
	        return true;
	    default:
		    return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, NebelTVApp.FLURRY_API_KEY);
	}
	
	@Override
	protected void onStop() {
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}

	public void showFragment(Fragment fragment) {
		getSupportFragmentManager()
			.beginTransaction()
			.addToBackStack(null)
			.replace(R.id.content_frame, fragment)
			.commit();
	}
}
