package com.nebel_tv.ui.fragment;

import java.util.HashMap;

import android.os.Bundle;

import com.nebel_tv.model.TopView;
import com.nebel_tv.ui.fragment.base.BaseWebViewFragment;
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
		if(args!=null) {
			topView = TopView.values()[args.getInt(EXTRA_TOP_VIEW_KEY, 0)];
		} else {
			topView = TopView.FRIENDS_FEED;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		switchUIState(UIState.LOADING);
		configUrls = ConfigHelper.getInstance().getConfigUrls();
		loadTopView();
	}
	
	private void loadTopView() {
		String url = configUrls.get(topView);
		if(url!=null) {
			webView.loadUrl(url);
		}
	}

	@Override
	protected boolean shouldOverrideUrlLoading(String url, int depth) {
		if(depth==0) {
			return false;
		} else {
    		getMainActivity().showFragment(CategoryFragment.newInstance(url));
			return true;
		}
	}

}
