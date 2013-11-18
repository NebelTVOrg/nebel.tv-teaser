package com.nebel_tv.activity.base;

import com.nebel_tv.R;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

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

	public void showFragment(Fragment fragment) {
		getSupportFragmentManager()
			.beginTransaction()
			.addToBackStack(null)
			.replace(R.id.content_frame, fragment)
			.commit();
	}
}
