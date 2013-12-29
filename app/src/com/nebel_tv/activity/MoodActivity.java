package com.nebel_tv.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Gallery;

import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;
import com.nebel_tv.adapter.MoodAdapter;
import com.nebel_tv.model.Mood;
import com.nebel_tv.storage.LocalStorage;

public class MoodActivity extends BaseActivity implements OnClickListener {
	
	public static final int MOOD_ACTIVITY_REQUEST_CODE = 101;
	public static final String EXTRA_MOOD_ORDINAL = "EXTRA_MOOD_ORDINAL";
	
	public static void launch(Context c) {
		Intent intent = new Intent(c, MoodActivity.class);
		c.startActivity(intent);
	}
	
	public static void launchForResult(Activity activity) {
		Intent intent = new Intent(activity, MoodActivity.class);
		activity.startActivityForResult(intent, MOOD_ACTIVITY_REQUEST_CODE);
	}
	
	private Gallery moodGallery;
	private LocalStorage storage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mood);
		
		storage = LocalStorage.from(this);
		
		findViewById(R.id.btn_ok).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		
		moodGallery = (Gallery) findViewById(R.id.gallery_mood);
		moodGallery.setAdapter(new MoodAdapter(this));
		moodGallery.setSelection(storage.getLastMood().ordinal());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_ok:
			Intent intent = null;
			int currentMoodOridinal = (int) moodGallery.getSelectedItemId();
			if(currentMoodOridinal!=storage.getLastMood().ordinal()) {
				storage.setLastMood(Mood.values()[currentMoodOridinal]);
				intent = new Intent();
				intent.putExtra(EXTRA_MOOD_ORDINAL, currentMoodOridinal);
			}
			setResult(RESULT_OK, intent);
			finish();
			break;
		case R.id.btn_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		//do nothing
		//block default back functionality
	}

}
