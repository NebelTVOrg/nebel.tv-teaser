package com.nebel_tv.activity;

import java.util.HashMap;
import java.util.Iterator;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;
import com.nebel_tv.activity.base.BaseActivity;
import com.nebel_tv.adapter.NavigationDrawerAdapter;
import com.nebel_tv.adapter.NavigationDrawerAdapter.GroupType;
import com.nebel_tv.frontend.FrontendUpdateTask;
import com.nebel_tv.model.Mood;
import com.nebel_tv.storage.LocalStorage;
import com.nebel_tv.ui.dialog.PrivacyDialogFragment;
import com.nebel_tv.ui.fragment.TopViewPagerFragment;
import com.nebel_tv.utils.ConfigHelper;
import com.nebel_tv.utils.ConfigHelper.OnConfigUpdatedListener;

public class MainActivity extends BaseActivity 
					implements OnChildClickListener, OnGroupCollapseListener,
							   OnGroupExpandListener, OnPageChangeListener, OnConfigUpdatedListener  {
	
	private DrawerLayout drawerLayout;
    private ExpandableListView drawerList;
    private NavigationDrawerAdapter drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private TopViewPagerFragment topViewPagerFragment;
    
    private LocalStorage localStorage;
	private String[] menuTitles;
    private CharSequence currentTitle;
    private int currentPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		localStorage = LocalStorage.from(this);
		initNavigationDrawerUI();
        
        if (savedInstanceState != null) {
        	topViewPagerFragment = (TopViewPagerFragment) 
        		getSupportFragmentManager().findFragmentByTag(TopViewPagerFragment.TAG);
        } else {
        	topViewPagerFragment = new TopViewPagerFragment();
    		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.content_frame, topViewPagerFragment, TopViewPagerFragment.TAG)
				.commit();
        }
        
        //show app policy if it's not accepted yet
        if(!localStorage.isPolicyAccepted()) {
        	PrivacyDialogFragment.showPrivacyDialog(getSupportFragmentManager());
        }
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		ConfigHelper.getInstance().registerOnConfigUpdatedListener(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		ConfigHelper.getInstance().unregisterOnConfigUpdatedListener(this);
	}
	
	private void initNavigationDrawerUI() {
		HashMap<GroupType, String[]> drawerData = initNavigationDrawerData();
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ExpandableListView) findViewById(R.id.left_drawer);
		drawerAdapter = new NavigationDrawerAdapter(this, drawerData);
		drawerList.setAdapter(drawerAdapter);
		drawerList.setOnChildClickListener(this);
		drawerList.setOnGroupCollapseListener(this);
		drawerList.setOnGroupExpandListener(this);
		Iterator<GroupType> it = drawerData.keySet().iterator();
		int i=0;
		while(it.hasNext()) {
			if(localStorage.getNavigationGroupState(it.next())) {
				drawerList.expandGroup(i);
			}
			i++;
		}
		
		drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
                ) {

            public void onDrawerClosed(View view) {
            	updateCurrentTitle();
            }

            public void onDrawerOpened(View drawerView) {
            	getSupportActionBar().setTitle(R.string.app_name);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(currentTitle);
	}
	
	private HashMap<GroupType, String[]> initNavigationDrawerData() {
		menuTitles = getResources().getStringArray(R.array.menu_items);
		currentPosition = localStorage.getLastScreen().ordinal();
		currentTitle = menuTitles[currentPosition];
		HashMap<GroupType, String[]> drawerData = new HashMap<GroupType, String[]>();
		drawerData.put(GroupType.TOP_CATEGORIES, menuTitles);
		drawerData.put(GroupType.MOOD, getResources().getStringArray(R.array.mood_items));
		return drawerData;
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        switch (item.getItemId()) {
		case R.id.menu_about:
			AboutActivity.launch(this);
			return true;
		case R.id.menu_product_tour:
			launchHomeWebPage();
			return true;
		case R.id.menu_feedback:
			FeedbackActivity.launch(this);
			return true;
		case R.id.menu_update_frontend:
			new FrontendUpdateTask(this).execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if(!NebelTVApp.FRONTEND_DEBUG_MODE) {
			MenuItem item = menu.findItem(R.id.menu_update_frontend);
			if(item!=null) {
				item.setVisible(false);
			}
		}
		return true;
	}

	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		GroupType type = drawerAdapter.getGroupEnum(groupPosition);
		if(type==GroupType.MOOD) {
			Mood mood = Mood.values()[childPosition];
			if(localStorage.getLastMood()==mood) {
				drawerLayout.closeDrawer(drawerList);
				return true;
			}
			localStorage.setLastMood(mood);
			drawerLayout.closeDrawer(drawerList);
			drawerAdapter.notifyDataSetChanged();
			if(topViewPagerFragment!=null) {
				topViewPagerFragment.notifyDataChanged();
			}
		} else if(type==GroupType.TOP_CATEGORIES) {
			if(currentPosition==childPosition) {
				drawerLayout.closeDrawer(drawerList);
				return true;
			}
			currentPosition = childPosition;
			currentTitle = menuTitles[childPosition];
			drawerLayout.closeDrawer(drawerList);
			if(topViewPagerFragment!=null) {
				topViewPagerFragment.setCurrentTopView(childPosition);
			}
		}
		return true;
	}
	
	@Override
	public void onGroupExpand(int groupPosition) {
		localStorage.setNavigationGroupState(drawerAdapter.getGroupEnum(groupPosition), true);
	}

	@Override
	public void onGroupCollapse(int groupPosition) {
		localStorage.setNavigationGroupState(drawerAdapter.getGroupEnum(groupPosition), false);
	}
	
	@Override
	public void onPageSelected(int position) {
		currentPosition = position;
		currentTitle = menuTitles[position];
		getSupportActionBar().setTitle(currentTitle);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		//empty implementation
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		//empty implementation
	}	

	@Override
	public void onConfigUpdated() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(topViewPagerFragment!=null) {
					topViewPagerFragment.notifyDataChanged();
				}
			}
		});
	}
	
	private void updateCurrentTitle() {
		getSupportActionBar().setTitle(currentTitle);
	}
	
	private void launchHomeWebPage() {
		Intent externalBrowserIntent = new Intent(Intent.ACTION_VIEW);
		String homePageUrl = ConfigHelper.getInstance().getConfig().getNebelTVHomepage();
		if(homePageUrl!=null) {
			externalBrowserIntent.setData(Uri.parse(homePageUrl));
			startActivity(externalBrowserIntent);
		}
	}

}
