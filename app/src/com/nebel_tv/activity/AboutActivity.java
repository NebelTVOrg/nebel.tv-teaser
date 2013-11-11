package com.nebel_tv.activity;

import com.nebel_tv.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends ActionBarActivity {
	
	public static void launch(Context c) {
		Intent intent = new Intent(c, AboutActivity.class);
		c.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
		
		TextView contentText = (TextView) findViewById(R.id.txt_content);
		contentText.setText(String.format(getString(R.string.about_content), getBuildVersionName()));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	onBackPressed();
	        return true;
	    default:
		    return super.onOptionsItemSelected(item);
	    }
	}
	
	private String getBuildVersionName() {
    	String versionName;
    	try {
	    	PackageInfo info = getPackageManager().
	    									getPackageInfo(getPackageName(), 0);
	    	versionName = info.versionName;
    	} catch(NameNotFoundException e) {
    		e.printStackTrace();
    		versionName = getString(R.string.unknown_version);
    	}
    	return versionName;
    }

}
