package com.nebel_tv.ui.fragment;

import android.os.Bundle;

import com.nebel_tv.R;
import com.nebel_tv.activity.MediaPlaybackActivity;
import com.nebel_tv.ui.fragment.base.BaseWebViewFragment;
import com.nebel_tv.utils.DownloadManagerHelper;
import com.nebel_tv.utils.IVAHelper;
import com.nebel_tv.utils.UIUtils;

public class CategoryFragment extends BaseWebViewFragment {
	
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
		if(args!=null) {
			url = args.getString(EXTRA_CATEGORY_URL_KEY);
		}
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if(url!=null) {
			webView.loadUrl(url);
		}
	}

	@Override
	protected boolean shouldOverrideUrlLoading(String url, int depth) {
		if(url!=null && url.startsWith(IVAHelper.INTERTCEPT_URL)) {
			String jsCall = IVAHelper.getInstance().getMediaItem(url);
			webView.loadUrl("javascript:"+jsCall);
			return true;
		}
		if(depth==0) {
			return false;
		} else {
			String[] videoUrls = DownloadManagerHelper.getVideoFiles(getActivity());
			if(videoUrls!=null) {
				MediaPlaybackActivity.launch(getActivity(), videoUrls);
			} else {
				UIUtils.showMessage(R.string.videos_are_not_downloaded);
			}
			return true;
		}
	}

}
