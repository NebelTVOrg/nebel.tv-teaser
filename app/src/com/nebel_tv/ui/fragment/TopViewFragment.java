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

import java.util.HashMap;

import android.os.Bundle;

import com.nebel_tv.activity.CategoryActivity;
import com.nebel_tv.model.Mood;
import com.nebel_tv.model.TopView;
import com.nebel_tv.storage.LocalStorage;
import com.nebel_tv.ui.fragment.base.BaseWebViewFragment;
import com.nebel_tv.ui.fragment.base.WebViewUILoaderHelper.UIState;
import com.nebel_tv.utils.ConfigHelper;

public class TopViewFragment extends BaseWebViewFragment {

	private static final String EXTRA_TOP_VIEW_KEY = "EXTRA_TOP_VIEW_KEY";

	private TopView topView;
	private HashMap<TopView, String> configUrls;

	public static TopViewFragment newInstance(TopView topView) {
		TopViewFragment f = new TopViewFragment();

		Bundle args = new Bundle();
		args.putInt(EXTRA_TOP_VIEW_KEY, topView.ordinal());
		f.setArguments(args);

		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			topView = TopView.values()[args.getInt(EXTRA_TOP_VIEW_KEY, 0)];
		} else {
			topView = TopView.FRIENDS_FEED;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		webViewUILoaderHelper.switchUIState(UIState.LOADING);
		Mood lastMood = LocalStorage.from(getActivity()).getLastMood();
		configUrls = ConfigHelper.getInstance().getConfig().getConfigUrls().get(lastMood);
		loadTopView();
	}

	private void loadTopView() {
		if (configUrls != null) {
			String url = configUrls.get(topView);
			if (url != null) {
				// TODO maybe change it when remote front-end will be
				// implemented
				webView.clearCache(true);
				webView.loadUrl(url);
			}
		}
	}

	@Override
	protected boolean shouldOverrideUrlLoading(String url, int depth) {
		if (depth == 0) {
			return false;
		} else {
			CategoryActivity.launch(getActivity(), url);
			return true;
		}
	}

}
