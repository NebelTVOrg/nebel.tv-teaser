package com.nebel_tv.activity;

import java.util.Timer;
import java.util.TimerTask;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.WheelView.CenterImageAlignType;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;
import com.nebel_tv.storage.LocalStorage;
import com.nebel_tv.ui.view.VerticalSeekBar;
import com.nebel_tv.ui.view.VideoSeekBar;
import com.nebel_tv.utils.ConfigHelper;
import com.nebel_tv.utils.DateTimeUtils;
import com.vayavision.MediaCore.MediaCore;
import com.vayavision.MediaCore.OpenGLES20Renderer;
import com.vayavision.MediaCore.PlayerCore2;
import com.vayavision.MediaCore.PlayerCore2.Configuration;

public class MediaPlaybackActivity extends Activity 
					implements PlayerCore2.OnEventListener, OnSeekBarChangeListener, OnWheelChangedListener {
	
	private static final String TAG = MediaPlaybackActivity.class.getName();
	private static final String KEY_VIDEO_URLS = "KEY_VIDEO_URLS";
	
	private static final int DEFAULT_CONTROLS_VISIBILITY_TIME = 3000; //3 sec
	private static final int AUDIO_WHEEL_VISIBLE_ITEMS_NUM = 5;
	private static final int SUBTITLE_WHEEL_VISIBLE_ITEMS_NUM = AUDIO_WHEEL_VISIBLE_ITEMS_NUM;
	private static final int QUALITY_WHEEL_VISIBLE_ITEMS_NUM = 3;
	private static final int DEFAULT_AUDIO_STREAM = AudioManager.STREAM_MUSIC;
	
	public static void launch(Context c, String[] videoUrls) {
		Intent intent = new Intent(c, MediaPlaybackActivity.class);
		intent.putExtra(KEY_VIDEO_URLS, videoUrls);
		c.startActivity(intent);
	}
	
	private SurfaceView mSurfaceView = null;
	private GLSurfaceView mGlView = null;
	private OpenGLES20Renderer mGlRenderer = null;
	private PlayerCore2 mCore2 = null;
	
	private AudioManager audioManager;
	
	private ImageButton playBtn;
	private ImageButton seekBackBtn;
	private ImageButton seekAheadBtn;
	
	private ViewGroup videoViewContainer;
	private ViewGroup controlContainer;
	private VideoSeekBar videoSeekBar;
	private VerticalSeekBar volumeSeekBar;
	private VerticalSeekBar brightnessSeekBar;
	private WheelView audiotrackWheel;
	private WheelView subtitleWheel;
	private WheelView videoQualityWheel;
	private TextView durationText;

	private String[] videoUrls;
	private int maxStreamVolume;
	private int seekBackValueInSec;
	private int seekAheadValueInSec;
	private Timer timer;
	private ControlVisibilityTimerTask controlVisibilityTimerTask;
	private LocalStorage localStorage;
	
	private int mState;
	private long mPositionInSeconds;
	private long mDurationInSeconds;
	private int mAudioTrackCount;
	private int mSubtitleTrackCount;
	private int mActiveAudioTrack;
	private int mActiveSubtitleTrack;
	private boolean showTimeRemaining;
	
	private Animation animFadeOut;
	private Animation animFadeIn;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mediaplayback);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setVolumeControlStream(DEFAULT_AUDIO_STREAM);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		maxStreamVolume = audioManager.getStreamMaxVolume(DEFAULT_AUDIO_STREAM);
		ConfigHelper configHelper = ConfigHelper.getInstance();
		seekBackValueInSec = configHelper.getJumpBackSecValue();
		seekAheadValueInSec = configHelper.getJumpAheadSecValue();
		localStorage = LocalStorage.from(this);
		showTimeRemaining = localStorage.isShowTimeRemaining();
		
		videoViewContainer = (ViewGroup) findViewById(R.id.container_video);
		controlContainer = (ViewGroup) findViewById(R.id.container_control);
		videoSeekBar = (VideoSeekBar) findViewById(R.id.seekbar_video);
		volumeSeekBar = (VerticalSeekBar) findViewById(R.id.seekbar_volume);
		brightnessSeekBar = (VerticalSeekBar) findViewById(R.id.seekbar_brightness);
		audiotrackWheel = (WheelView) findViewById(R.id.audiotrack_content);
		subtitleWheel = (WheelView) findViewById(R.id.subtitle_content);
		videoQualityWheel = (WheelView) findViewById(R.id.quality_content);
		durationText = (TextView) findViewById(R.id.txt_duration);
		
		videoUrls = getIntent().getStringArrayExtra(KEY_VIDEO_URLS);
		timer = new Timer();
		
		if(isOpenGL2ES20supported()){
			mGlRenderer = new OpenGLES20Renderer();
			mGlView = new GLSurfaceView(this);	
	        mGlView.setEGLContextClientVersion(2);			
			mGlView.setRenderer(mGlRenderer);
		        
			videoViewContainer.addView(mGlView);			        
			mCore2 = MediaCore.createPlayerCore2(mGlRenderer);
		}else{
			mSurfaceView = new SurfaceView(this);
			
			videoViewContainer.addView(mSurfaceView);			        
	        mCore2 = MediaCore.createPlayerCore2(mSurfaceView.getHolder());
		}
		
        initPlayerControlListeners();

        mCore2.addListener(this);
        videoSeekBar.setOnSeekBarChangeListener(this);
	        
        updateState(PlayerCore2.STATE_IDLE);
        mCore2.load(videoUrls);
        
        animFadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        animFadeOut.setAnimationListener(controlsFadeOutAnimationListener);
        animFadeIn.setAnimationListener(controlsFadeInAnimationListener);
        showControls();
        
    }
	
	private void updateState(int state) {
		switch(state){
		case PlayerCore2.STATE_IDLE:
			playBtn.setEnabled(false);
			seekBackBtn.setEnabled(false);
			seekAheadBtn.setEnabled(false);
			videoSeekBar.setEnabled(false);
			audiotrackWheel.setEnabled(false);
			subtitleWheel.setEnabled(false);
			videoQualityWheel.setEnabled(false);
			break;		
		case PlayerCore2.STATE_STOPPED:
			playBtn.setEnabled(true);
			seekBackBtn.setEnabled(false);
			seekAheadBtn.setEnabled(false);
			videoSeekBar.setEnabled(false);
			audiotrackWheel.setEnabled(true);
			subtitleWheel.setEnabled(true);
			videoQualityWheel.setEnabled(true);
			break;		
		case PlayerCore2.STATE_PLAYING:
			playBtn.setEnabled(true);
			seekBackBtn.setEnabled(true);
			seekAheadBtn.setEnabled(true);
			videoSeekBar.setEnabled(true);
			audiotrackWheel.setEnabled(true);
			subtitleWheel.setEnabled(true);
			videoQualityWheel.setEnabled(true);
			break;		
		case PlayerCore2.STATE_PAUSED:
			playBtn.setEnabled(true);
			seekBackBtn.setEnabled(true);
			seekAheadBtn.setEnabled(true);
			videoSeekBar.setEnabled(true);
			audiotrackWheel.setEnabled(true);
			subtitleWheel.setEnabled(true);
			videoQualityWheel.setEnabled(true);
			break;		
		case PlayerCore2.STATE_BUFFERING:
			playBtn.setEnabled(false);
			seekBackBtn.setEnabled(true);
			seekAheadBtn.setEnabled(true);
			videoSeekBar.setEnabled(true);
			audiotrackWheel.setEnabled(true);
			subtitleWheel.setEnabled(true);
			videoQualityWheel.setEnabled(true);
			break;
		default:
			Log.e(TAG, "Unknown state " + state);
			return;
		}
		
		mState = state;
	}	
    
    protected void initPlayerControlListeners(){
    	
    	volumeSeekBar.setOnSeekBarChangeListener(this);  	
    	brightnessSeekBar.setOnSeekBarChangeListener(this);
    	
    	audiotrackWheel.setCenterDrawableRes(R.drawable.panel_left_selected_item_bg, CenterImageAlignType.ALIGN_LEFT);
    	audiotrackWheel.setVisibleItems(AUDIO_WHEEL_VISIBLE_ITEMS_NUM);
    	audiotrackWheel.addChangingListener(this);
    	
    	subtitleWheel.setCenterDrawableRes(R.drawable.panel_right_selected_item_bg, CenterImageAlignType.ALIGN_RIGHT);
    	subtitleWheel.setVisibleItems(SUBTITLE_WHEEL_VISIBLE_ITEMS_NUM);
    	subtitleWheel.addChangingListener(this);
    	
    	videoQualityWheel.setCenterDrawableRes(R.drawable.panel_top_selected_item_bg, CenterImageAlignType.CENTER);
    	videoQualityWheel.setVisibleItems(QUALITY_WHEEL_VISIBLE_ITEMS_NUM);
    	updateVideoQualityValues();
        
        playBtn = (ImageButton) findViewById(R.id.btn_play);
        seekBackBtn = (ImageButton) findViewById(R.id.btn_back);     
        seekAheadBtn = (ImageButton) findViewById(R.id.btn_ahead);
 
        //TODO remove this
        //harcoded crash event to test Flurry crash reports
        playBtn.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				throw new RuntimeException("harcoded runtime crash to test Flurry Crash reporting");
			}
		});
    }
    
    public void onPlayClick(View v) {
    	if(mState == PlayerCore2.STATE_PLAYING){
			 mCore2.pause();
		 }else{
				 mCore2.play();
		 }
    }
    
    public void onSeekBackClick(View v) {
    	mCore2.setPosition(
				DateTimeUtils.getSecValueInMicros(mPositionInSeconds - seekBackValueInSec),
				0,
				DateTimeUtils.getSecValueInMicros(mPositionInSeconds));
		mCore2.play();
    }
    
    public void onSeekForwardClick(View v) {
    	mCore2.setPosition(
				DateTimeUtils.getSecValueInMicros(mPositionInSeconds + seekAheadValueInSec),
				0,
				DateTimeUtils.getSecValueInMicros(mDurationInSeconds));
		mCore2.play();
    }
    
    public void onDurationClick(View v) {
    	showTimeRemaining = !showTimeRemaining;
    	localStorage.setShowTimeRemaining(showTimeRemaining);
    	updateDurationText();
    }
    
    private void updateDurationText() {
    	long  value;
    	if(!showTimeRemaining) {
	    	value = mDurationInSeconds;
    	} else {
    		value = mDurationInSeconds-mPositionInSeconds;
    	}
    	durationText.setText(DateTimeUtils.getDefaultTimeFormatter().print(DateTimeUtils.getSecValueInMillis(value)));
    }
    
    private void updateAudioTrackCount() {
    	//TODO implement real audiotrack names as far as it will be implemented on video player
    	//hardcoded names used until then
    	String[] items = new String[mAudioTrackCount];
    	for(int i=0; i<mAudioTrackCount; i++) {
    		items[i]="track "+i;
    	}
    	if(mAudioTrackCount>0) {
	    	ArrayWheelAdapter<String> audiotrackWheelAdapter = new ArrayWheelAdapter<String>(this, items);
	    	audiotrackWheelAdapter.setItemResource(R.layout.wheel_text_item);
	    	audiotrackWheel.setViewAdapter(audiotrackWheelAdapter);
	    	audiotrackWheel.setCurrentItem(mActiveAudioTrack);
    	}
    }
    
    private void updateSubtitleCount() {
    	//TODO implement real subtitle names as far as it will be implemented on video player
    	//hardcoded names used until then
    	String[] items = new String[mSubtitleTrackCount];
    	for(int i=0; i<mSubtitleTrackCount; i++) {
    		items[i]="subtitle "+i;
    	}
    	if(mSubtitleTrackCount>0) {
        	ArrayWheelAdapter<String> subtitleWheelAdapter = new ArrayWheelAdapter<String>(this, items);
        	subtitleWheelAdapter.setItemResource(R.layout.wheel_text_item);
        	subtitleWheel.setViewAdapter(subtitleWheelAdapter);
        	subtitleWheel.setCurrentItem(mActiveSubtitleTrack);
    	}
    }
    
    private void updateVideoQualityValues() {
    	//TODO implement real quality as far as it will be implemented on video player
    	//hardcoded names used until then
    	String[] items = new String[] {"Auto", "360", "480", "720"};
    	ArrayWheelAdapter<String> videoQualityWheelAdapter = new ArrayWheelAdapter<String>(this, items);
    	videoQualityWheelAdapter.setItemResource(R.layout.wheel_text_item);
    	videoQualityWheel.setViewAdapter(videoQualityWheelAdapter);
    	videoQualityWheel.setCurrentItem(3);
    }
    
    private void setCurrentSeekbarValues() {
    	volumeSeekBar.setProgressAndThumb(audioManager.getStreamVolume(DEFAULT_AUDIO_STREAM));
    	int brightness = (int)(Math.abs(getWindow().getAttributes().screenBrightness)*100);
    	brightnessSeekBar.setProgressAndThumb(brightness);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setCurrentSeekbarValues();
        if(mGlView != null){
        	mGlView.onResume();
        }
        if(mCore2 != null && mState == PlayerCore2.STATE_PAUSED){
        	mCore2.play();
        }
    }

    @Override
    protected void onPause() {
    	if(mCore2 != null && mState == PlayerCore2.STATE_PLAYING){
    		mCore2.pause();
       	}
        if(mGlView != null){
            mGlView.onPause();        	
		}
        super.onPause();
	}
    
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, NebelTVApp.FLURRY_API_KEY);
	}
	
	@Override
	protected void onStop() {
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || 
        		(keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
        	int currentStreamVolume = audioManager.getStreamVolume(DEFAULT_AUDIO_STREAM);
        	if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN) {
        		currentStreamVolume--;
        	} else {
        		currentStreamVolume++;
        	}
        	if(currentStreamVolume<0) {
        		currentStreamVolume=0;
        	} else if(currentStreamVolume>maxStreamVolume) {
        		currentStreamVolume = maxStreamVolume;
        	}
            volumeSeekBar.setProgressAndThumb(currentStreamVolume*volumeSeekBar.getMax()/maxStreamVolume);
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if(ev.getAction()==MotionEvent.ACTION_UP) {
    		if(isControlsVisible()) {
    			timer.schedule(controlVisibilityTimerTask, DEFAULT_CONTROLS_VISIBILITY_TIME);
    		} else {
    			showControls();
    		}
    	} else {
    		resetTimer();
    	}
    	return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction()==MotionEvent.ACTION_UP) {
    		onPlayClick(playBtn);
    	}
    	return super.onTouchEvent(event);
    }
    
    public void onGetConfigurationComplete(int status, Configuration configuration) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}
	
	public void onSetConfigurationComplete(int status) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}
	
	public void onGetStateComplete(final int state) {
		Log.d(TAG, getMethodName(1) + ": " + state);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				updateState(state);
			}
		});
	}

	public void onLoadComplete(int status) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}

	public void onUnloadComplete(int status) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}

	public void onPlayComplete(int status) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}

	public void onPauseComplete(int status) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}

	public void onStopComplete(int status) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}

	public void onGetDurationComplete(int status, long duration) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + duration);
		mDurationInSeconds = DateTimeUtils.getSecValueInMicros(duration, true);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				videoSeekBar.setMax((int)mDurationInSeconds);
				updateDurationText();
			}
		});
	}

	public void onGetPositionComplete(int status, long position) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + position);
	}

	public void onSetPositionComplete(int status, long newPosition) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + newPosition);
	}

	public void onGetVolumeComplete(int status, float volume) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + volume);
	}

	public void onSetVolumeComplete(int status, float newVolume) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + newVolume);
	}

	public void onGetSubtitleStateComplete(int status, boolean state) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + state);
	}

	public void onSetSubtitleStateComplete(int status, boolean newState) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + newState);
	}

	public void onGetTrackCountComplete(int status, int type, int trackCount) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + type + " " + trackCount);
		switch (type) {
		case PlayerCore2.TRACK_TYPE_AUDIO:
			mAudioTrackCount = trackCount;
			break;
		case PlayerCore2.TRACK_TYPE_VIDEO:
			//empty implementation
			break;
		case PlayerCore2.TRACK_TYPE_SUBTITLE:
			mSubtitleTrackCount = trackCount;
			break;
		}
	}

	public void onGetActiveTrackComplete(int status, int type, int track) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + type + " " + track);
		switch (type) {
		case PlayerCore2.TRACK_TYPE_AUDIO:
			mActiveAudioTrack = track;
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					updateAudioTrackCount();
				}
			});
			break;
		case PlayerCore2.TRACK_TYPE_VIDEO:
			//empty implementation
			break;
		case PlayerCore2.TRACK_TYPE_SUBTITLE:
			mActiveSubtitleTrack = track;
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					updateSubtitleCount();
				}
			});
			break;
		}
	}

	public void onActivateTrackComplete(int status, int type, int newTrack) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + type + " " + newTrack);
		switch (type) {
		case PlayerCore2.TRACK_TYPE_AUDIO:
			mActiveAudioTrack = newTrack;
			break;
		case PlayerCore2.TRACK_TYPE_VIDEO:
			//empty implementation
			break;
		case PlayerCore2.TRACK_TYPE_SUBTITLE:
			mActiveSubtitleTrack = newTrack;
			break;
		}
	}

	public void onStateChange(final int newState, int oldState) {
		Log.d(TAG, getMethodName(1) + ": " + oldState + " => " + newState);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				updateState(newState);
			}
		});
	}		

	public void onPositionChange(final long position) {
		mPositionInSeconds = DateTimeUtils.getSecValueInMicros(position, true);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				videoSeekBar.setProgress((int)mPositionInSeconds);
				if(showTimeRemaining) {
					updateDurationText();
				}
			}
		});
	}

	public void onBufferingProgress(long bufferL1, long bufferL2) {
		//empty implementation
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(videoSeekBar==seekBar) {
			videoSeekBar.notifyTimeTextPositionChange();
		} else if(volumeSeekBar==seekBar) {
			if(!fromUser) {
				return;
			}
			int volume = (int) Math.ceil(progress*maxStreamVolume/(float)seekBar.getMax())-1;
			if(audioManager.getStreamVolume(DEFAULT_AUDIO_STREAM)!=volume) {
				audioManager.setStreamVolume(DEFAULT_AUDIO_STREAM, volume, 0);
			}
		} else if(brightnessSeekBar==seekBar) {
			if(!fromUser) {
				return;
			}
			float brightness = (float)(progress)/100f*255f;
			android.provider.Settings.System.putInt(getContentResolver(),
				     android.provider.Settings.System.SCREEN_BRIGHTNESS,
				     (int)brightness);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if(videoSeekBar==seekBar) {
			videoSeekBar.setAutochange(false);
		} 
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(videoSeekBar==seekBar) {
			videoSeekBar.setAutochange(true);
			mCore2.setPosition(
					DateTimeUtils.getSecValueInMicros(seekBar.getProgress()),
					0,
					DateTimeUtils.getSecValueInMicros(mDurationInSeconds));
		}
	}
	
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if(audiotrackWheel==wheel) {
			mCore2.activateTrack(PlayerCore2.TRACK_TYPE_AUDIO, newValue);
		} else if(subtitleWheel==wheel) {
			mCore2.activateTrack(PlayerCore2.TRACK_TYPE_SUBTITLE, newValue);
		}
	}
	
	private boolean isControlsVisible() {
		return controlContainer.getVisibility()==View.VISIBLE;
	}
	
	private void showControls() {
		controlContainer.startAnimation(animFadeIn);
		resetTimer();
		timer.schedule(controlVisibilityTimerTask, DEFAULT_CONTROLS_VISIBILITY_TIME);
	}
	
	private void resetTimer() {
		if(timer!=null) {
			timer.cancel();
		}
		if(controlVisibilityTimerTask!=null) {
			controlVisibilityTimerTask.cancel();
		}
		timer = new Timer();
		controlVisibilityTimerTask  = new ControlVisibilityTimerTask();
	}
	
	private boolean isOpenGL2ES20supported() {
		final ActivityManager activityManager = 
		    (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = 
		    activityManager.getDeviceConfigurationInfo();
		return configurationInfo.reqGlEsVersion >= 0x20000;
	}
	
	private String getMethodName(final int depth) {
	  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	  return ste[ste.length - 1 - depth].getMethodName();
	}
	
	private class ControlVisibilityTimerTask extends TimerTask {
		
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					controlContainer.startAnimation(animFadeOut);
				}
			});
		}
	}
	
	private AnimationListener controlsFadeOutAnimationListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			controlContainer.setVisibility(View.GONE);
		}
	};
	
	private AnimationListener controlsFadeInAnimationListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			controlContainer.setVisibility(View.VISIBLE);
		}
	};
	
}
