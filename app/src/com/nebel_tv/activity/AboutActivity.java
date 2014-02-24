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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;
import com.nebel_tv.utils.D;

public class AboutActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = AboutActivity.class.getName();

	public static void launch(Context c) {
		Intent intent = new Intent(c, AboutActivity.class);
		c.startActivity(intent);
	}

	private TextView versionText;
	private TextView releaseDateText;
	private Button finishBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		versionText = (TextView) findViewById(R.id.txt_version);
		releaseDateText = (TextView) findViewById(R.id.txt_release_date);
		finishBtn = (Button) findViewById(R.id.btn_finish);
		finishBtn.setOnClickListener(this);

		String versionName = getBuildVersionName();
		String buildDate = getBuildDate();
		if (versionName == null) {
			versionName = getString(R.string.unknown_version);
		}
		if (buildDate == null) {
			buildDate = "";
		}
		fillAboutContent(versionName, buildDate);
	}

	private void fillAboutContent(String version, String releaseDate) {
		versionText.setText(String.format(getString(R.string.version), version));
		releaseDateText.setText(String.format(getString(R.string.release_date), releaseDate));
	}

	private String getBuildVersionName() {
		String versionName;
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = info.versionName;
		} catch (NameNotFoundException e) {
			D.e(e);
			FlurryAgent.onError(TAG, e.getMessage(), e);
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

		} catch (Exception e) {
			D.e(e);
			FlurryAgent.onError(TAG, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public void onClick(View v) {
		onBackPressed();
	}
}
