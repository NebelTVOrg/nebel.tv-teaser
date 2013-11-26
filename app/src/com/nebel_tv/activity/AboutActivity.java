package com.nebel_tv.activity;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;

public class AboutActivity extends BaseActivity {
	
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
		String buildDate = getBuildDate();
		if(versionName==null) {
			versionName = getString(R.string.unknown_version);
		}
		if(buildDate==null) {
			buildDate = "";
		}
		fillAboutContent(versionName, buildDate);
	}
	
	private void fillAboutContent(String version, String releaseDate) {
		contentText.setText(String.format(
				getString(R.string.about_content), version, releaseDate));
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
	
	private String getBuildDate() {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
		    ZipFile zf = new ZipFile(ai.sourceDir);
		    ZipEntry ze = zf.getEntry("classes.dex");
		    long time = ze.getTime();
		    zf.close();
		    return DateTimeFormat.mediumDate().print(new DateTime(time));

		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

}
