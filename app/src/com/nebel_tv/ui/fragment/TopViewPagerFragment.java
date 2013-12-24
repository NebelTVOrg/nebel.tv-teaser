package com.nebel_tv.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nebel_tv.R;
import com.nebel_tv.model.TopView;
import com.nebel_tv.storage.LocalStorage;

public class TopViewPagerFragment extends Fragment {
	
	public static final String TAG = TopViewPagerFragment.class.getName();
	
	private LocalStorage localStorage;
	
	private ViewPager topViewPager;
	private PagerAdapter topViewPagerAdapter;
	private OnPageChangeListenerWrapper onTopViewChangeListener;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	onTopViewChangeListener = new OnPageChangeListenerWrapper((OnPageChangeListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnPageChangeListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		topViewPager = (ViewPager) inflater.inflate(R.layout.fragment_top_view_pager, container, false);
		topViewPagerAdapter = new TopViewPagerAdapter(getChildFragmentManager());
		topViewPager.setAdapter(topViewPagerAdapter);
		
		return topViewPager;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		localStorage = LocalStorage.from(getActivity());
		topViewPager.setCurrentItem(localStorage.getLastScreen().ordinal());
		topViewPager.setOnPageChangeListener(onTopViewChangeListener);
	}
	
	public void setCurrentTopView(int position) {
		topViewPager.setCurrentItem(position);
	}
	
	public void notifyDataChanged() {
		topViewPagerAdapter = new TopViewPagerAdapter(getChildFragmentManager());
		topViewPager.setAdapter(topViewPagerAdapter);
		topViewPager.setCurrentItem(localStorage.getLastScreen().ordinal());
	}
	
	private class TopViewPagerAdapter extends FragmentStatePagerAdapter {
		
		private TopView[] topViewValues;
		
        public TopViewPagerAdapter(FragmentManager fm) {
            super(fm);
            topViewValues = TopView.values();
        }

        @Override
        public Fragment getItem(int position) {
        	return TopViewFragment.newInstance(topViewValues[position]);
        }

        @Override
        public int getCount() {
            return topViewValues.length;
        }
    }
	
	private class OnPageChangeListenerWrapper implements OnPageChangeListener {
		
		private OnPageChangeListener listener;
		
		public OnPageChangeListenerWrapper(OnPageChangeListener listener) {
			this.listener = listener;
		}
		
		@Override
		public void onPageSelected(int position) {
			localStorage.setLastScreen(TopView.values()[position]);
			if(listener!=null) {
				listener.onPageSelected(position);
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			//empty implementation
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			//empty implementation
		}
	}

}
