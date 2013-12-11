package com.nebel_tv.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;
import com.nebel_tv.ui.fragment.CategoryFragment;

public class CategoryActivity extends BaseActivity {

	public static void launch(Context c, String url) {
		Intent intent = new Intent(c, CategoryActivity.class);
		intent.putExtra(CategoryFragment.EXTRA_CATEGORY_URL_KEY, url);
		c.startActivity(intent);
	}
	
	private CategoryFragment categoryFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        if (savedInstanceState != null) {
        	categoryFragment = (CategoryFragment) 
        		getSupportFragmentManager().findFragmentByTag(CategoryFragment.TAG);
        } else {
        	categoryFragment = CategoryFragment.newInstance(
        			getIntent().getStringExtra(CategoryFragment.EXTRA_CATEGORY_URL_KEY));
    		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.content_frame, categoryFragment, CategoryFragment.TAG)
				.commit();
        }
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
