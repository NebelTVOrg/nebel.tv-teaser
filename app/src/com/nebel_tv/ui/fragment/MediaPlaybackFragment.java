package com.nebel_tv.ui.fragment;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;

public class MediaPlaybackFragment extends Fragment {
	
	public static MediaPlaybackFragment newInstance() {
		MediaPlaybackFragment f = new MediaPlaybackFragment();
        
		return f;
	}
	
	private BaseActivity getParentActivity() {
		return (BaseActivity) getActivity();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		updateFullscreenStatus(true);
		getParentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.mediaplayback, container, false);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		updateFullscreenStatus(false);
		getParentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	private void updateFullscreenStatus(boolean useFullscreen) {  
		Window window = getParentActivity().getWindow();
		if(useFullscreen) {
			getParentActivity().getSupportActionBar().hide();
			window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	    }
		else {
			window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getParentActivity().getSupportActionBar().show();
	    }
	}

}
