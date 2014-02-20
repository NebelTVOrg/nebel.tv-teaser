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
package com.nebel_tv.ui.fragment;

import android.os.Bundle;

import com.nebel_tv.R;
import com.nebel_tv.activity.MediaPlaybackActivity;
import com.nebel_tv.ui.fragment.base.BaseWebViewFragment;
import com.nebel_tv.utils.DownloadManagerHelper;
import com.nebel_tv.utils.UIUtils;

public class CategoryFragment extends BaseWebViewFragment {

	public static final String TAG = CategoryFragment.class.getName();
	public static final String EXTRA_CATEGORY_URL_KEY = "EXTRA_CATEGORY_URL_KEY";

	private String url;

	public static CategoryFragment newInstance(String url) {
		CategoryFragment f = new CategoryFragment();

		Bundle args = new Bundle();
		args.putString(EXTRA_CATEGORY_URL_KEY, url);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			url = args.getString(EXTRA_CATEGORY_URL_KEY);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		if (url != null) {
			webView.loadUrl(url);
		}
	}

	@Override
	protected boolean shouldOverrideUrlLoading(String url, int depth) {
		if (depth == 0) {
			return false;
		} else {
			String[] videoUrls = DownloadManagerHelper.getVideoFiles(getActivity());
			if (videoUrls != null) {
				MediaPlaybackActivity.launch(getActivity(), videoUrls);
			} else {
				UIUtils.showMessage(R.string.videos_are_not_downloaded);
			}
			return true;
		}
	}

}
