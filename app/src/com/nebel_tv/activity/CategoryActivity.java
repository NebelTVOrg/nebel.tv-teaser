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
			categoryFragment = (CategoryFragment) getSupportFragmentManager().findFragmentByTag(CategoryFragment.TAG);
		} else {
			categoryFragment = CategoryFragment.newInstance(getIntent().getStringExtra(CategoryFragment.EXTRA_CATEGORY_URL_KEY));
			getSupportFragmentManager().beginTransaction().add(R.id.content_frame, categoryFragment, CategoryFragment.TAG).commit();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
