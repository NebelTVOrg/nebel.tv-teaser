package com.nebel_tv.ui.fragment;

import android.os.Bundle;

import com.nebel_tv.ui.fragment.base.BaseWebViewFragment;

public class CategoryFragment extends BaseWebViewFragment {
	
    private static final String EXTRA_CATEGORY_URL_KEY = "EXTRA_CATEGORY_URL_KEY";
    
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
		if(args!=null) {
			url = args.getString(EXTRA_CATEGORY_URL_KEY);
		}
		
		if(url!=null) {
			webView.loadUrl(url);
		}
	}

	@Override
	protected boolean shouldOverrideUrlLoading(String url, int depth) {
		if(depth==0) {
			return false;
		} else {
    		getMainActivity().showFragment(MediaPlaybackFragment.newInstance());
			return true;
		}
	}

}
