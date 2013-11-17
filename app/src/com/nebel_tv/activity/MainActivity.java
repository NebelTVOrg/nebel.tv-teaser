package com.nebel_tv.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nebel_tv.R;
import com.nebel_tv.storage.LocalStorage;
import com.nebel_tv.ui.fragment.CategoryFragment;
import com.nebel_tv.ui.fragment.TopViewPagerFragment;

public class MainActivity extends ActionBarActivity 
					implements ListView.OnItemClickListener, OnPageChangeListener  {
	
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private TopViewPagerFragment topViewPagerFragment;
    
	private String[] menuTitles;
    private CharSequence currentTitle;
    private int currentPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		menuTitles = getResources().getStringArray(R.array.menu_items);
		currentPosition = LocalStorage.from(this).getLastScreen().ordinal();
		currentTitle = menuTitles[currentPosition];
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		
		drawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, menuTitles));
		drawerList.setOnItemClickListener(this);
		
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
        
        
        if (savedInstanceState != null) {
        	topViewPagerFragment = (TopViewPagerFragment) getSupportFragmentManager().findFragmentByTag("customtag");
        } else {
        	topViewPagerFragment = new TopViewPagerFragment();
    		getSupportFragmentManager()
			.beginTransaction()
			.add(R.id.content_frame, topViewPagerFragment, "customtag")
			.commit();
        }
	}
	
	private void updateCurrentTitle() {
		getSupportActionBar().setTitle(currentTitle);
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
		default:
			return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void showFragment(Fragment fragment) {
		getSupportFragmentManager()
			.beginTransaction()
			.addToBackStack(null)
			.replace(R.id.content_frame, fragment)
			.commit();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(currentPosition==position) {
			drawerLayout.closeDrawer(drawerList);
			return;
		}
		currentPosition = position;
		currentTitle = menuTitles[position];
		drawerLayout.closeDrawer(drawerList);
		if(topViewPagerFragment!=null) {
			topViewPagerFragment.setCurrentTopView(position);
		}
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

}
