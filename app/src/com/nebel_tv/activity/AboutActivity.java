package com.nebel_tv.activity;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.nebel_tv.R;

public class AboutActivity extends ActionBarActivity {
	
	private static final String VERSION_DELIMITER = "_";
	private static final String INPUT_DATE_PATTERN = "ddMMyyyy";
	
	public static void launch(Context c) {
		Intent intent = new Intent(c, AboutActivity.class);
		c.startActivity(intent);
	}
	
	private TextView contentText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
		
		contentText = (TextView) findViewById(R.id.txt_content);
		String versionName = getBuildVersionName();
		//if versionName is valid according to pattern <version>_<date>
		//then extract info and fill about content
		if(versionName!=null) {
			String[] values = versionName.split(VERSION_DELIMITER);
			if(values.length==2) {
				try {
					DateTime releaseDate = DateTimeFormat.forPattern(INPUT_DATE_PATTERN).parseDateTime(values[1]);
					String releaseDateText = DateTimeFormat.mediumDate().print(releaseDate);
					fillAboutContent(values[0], releaseDateText);
					return;
				} catch(IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			
		}
		//else fill with "unknown" version
		fillAboutContent(getString(R.string.unknown_version), "");
	}
	
	private void fillAboutContent(String version, String releaseDate) {
		contentText.setText(String.format(
				getString(R.string.about_content), version, releaseDate));
	}
	
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
	
	private String getBuildVersionName() {
    	String versionName;
    	try {
	    	PackageInfo info = getPackageManager().
	    									getPackageInfo(getPackageName(), 0);
	    	versionName = info.versionName;
    	} catch(NameNotFoundException e) {
    		e.printStackTrace();
    		versionName = null;
    	}
    	return versionName;
    }

}
