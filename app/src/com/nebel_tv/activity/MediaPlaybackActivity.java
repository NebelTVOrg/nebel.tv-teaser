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
package com.nebel_tv.activity;

import it.sephiroth.slider.widget.MultiDirectionSlidingDrawer;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.WheelView.CenterImageAlignType;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
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
import com.nebel_tv.ui.view.VerticalSeekBar.ThumbOrientation;
import com.nebel_tv.ui.view.VideoSeekBar;
import com.nebel_tv.utils.ConfigHelper;
import com.nebel_tv.utils.ConfigHelper.ConfigModel;
import com.nebel_tv.utils.D;
import com.nebel_tv.utils.DateTimeUtils;
import com.vayavision.MediaCore.MediaCore;
import com.vayavision.MediaCore.PlayerCore2;
import com.vayavision.MediaCore.PlayerCore2.Configuration;

public class MediaPlaybackActivity extends Activity implements PlayerCore2.OnEventListener, OnSeekBarChangeListener,
		OnWheelChangedListener, UncaughtExceptionHandler {

	private static final String TAG = MediaPlaybackActivity.class.getName();
	private static final String KEY_VIDEO_URLS = "KEY_VIDEO_URLS";

	private static final int ICS_VERSION_NUM = 14;
	private static final int MAX_SETTINGS_BRIGHTNESS_VALUE = 255;
	private static final int DEFAULT_CONTROLS_VISIBILITY_TIME = 3000; // 3 sec
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
	private MultiDirectionSlidingDrawer audioTrackSliding;
	private MultiDirectionSlidingDrawer subtitleSliding;
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
	private int mMediaTrackCount;
	private int mActiveAudioTrack;
	private int mActiveSubtitleTrack;
	private int mActiveMediaTrack;
	private boolean showTimeRemaining;

	private Animation animFadeOut;
	private Animation animFadeIn;

	private class SurfaceHolderCallback implements SurfaceHolder.Callback {
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			// do nothing
		}

		public void surfaceCreated(SurfaceHolder holder) {
			if (mCore2 != null) {
				mCore2.setWindowState(true);
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCore2 != null) {
				mCore2.setWindowState(false);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mediaplayback);

		D.enableLocalLog();
		D.clearLocalLogBuffer();
		NebelTVApp.setCurrentHandler(this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setVolumeControlStream(DEFAULT_AUDIO_STREAM);
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		maxStreamVolume = audioManager.getStreamMaxVolume(DEFAULT_AUDIO_STREAM);
		ConfigModel config = ConfigHelper.getInstance().getConfig();
		seekBackValueInSec = config.getJumpBackSecValue();
		seekAheadValueInSec = config.getJumpAheadSecValue();
		localStorage = LocalStorage.from(this);
		showTimeRemaining = localStorage.isShowTimeRemaining();

		videoViewContainer = (ViewGroup) findViewById(R.id.container_video);
		controlContainer = (ViewGroup) findViewById(R.id.container_control);
		videoSeekBar = (VideoSeekBar) findViewById(R.id.seekbar_video);
		volumeSeekBar = (VerticalSeekBar) findViewById(R.id.seekbar_volume);
		brightnessSeekBar = (VerticalSeekBar) findViewById(R.id.seekbar_brightness);
		audiotrackWheel = (WheelView) findViewById(R.id.audiotrack_content);
		subtitleWheel = (WheelView) findViewById(R.id.subtitle_content);
		audioTrackSliding = (MultiDirectionSlidingDrawer) findViewById(R.id.drawer_audiotrack);
		subtitleSliding = (MultiDirectionSlidingDrawer) findViewById(R.id.drawer_subtitle);
		videoQualityWheel = (WheelView) findViewById(R.id.quality_content);
		durationText = (TextView) findViewById(R.id.txt_duration);

		videoUrls = getIntent().getStringArrayExtra(KEY_VIDEO_URLS);
		if (videoUrls == null) {
			String data = getIntent().getDataString();
			videoUrls = new String[] { new File(URI.create(data)).getAbsolutePath() };
		}
		timer = new Timer();

		mSurfaceView = new SurfaceView(this);

		videoViewContainer.addView(mSurfaceView);

		SurfaceHolder holder = mSurfaceView.getHolder();
		holder.addCallback(new SurfaceHolderCallback());

		mCore2 = MediaCore.createPlayerCore2(holder, getExternalCacheDir().getAbsolutePath());

		initPlayerControlListeners();

		mCore2.addListener(this);
		videoSeekBar.setOnSeekBarChangeListener(this);

		updateState(PlayerCore2.STATE_IDLE);
		D.d("Try to load video urls: " + Arrays.toString(videoUrls));
		mCore2.load(videoUrls);

		animFadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
		animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
		animFadeOut.setAnimationListener(controlsFadeOutAnimationListener);
		animFadeIn.setAnimationListener(controlsFadeInAnimationListener);
		setOnSystemUIVisibilityChangeListener();
		showControls();

	}

	private void updateState(int state) {
		switch (state) {
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
			D.e("Unknown state " + state);
			return;
		}

		mState = state;
	}

	protected void initPlayerControlListeners() {

		volumeSeekBar.setThumbOrientation(ThumbOrientation.LEFT);
		brightnessSeekBar.setThumbOrientation(ThumbOrientation.RIGHT);
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
		subtitleWheel.addChangingListener(this);

		playBtn = (ImageButton) findViewById(R.id.btn_play_pause);
		seekBackBtn = (ImageButton) findViewById(R.id.btn_back);
		seekAheadBtn = (ImageButton) findViewById(R.id.btn_ahead);

		// TODO remove this
		// harcoded crash event to test Flurry crash reports
		playBtn.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				throw new RuntimeException("harcoded runtime crash to test Flurry Crash reporting");
			}
		});
	}

	public void onPlayClick(View v) {
		if (mState == PlayerCore2.STATE_PLAYING) {
			D.d(getMethodName(1) + " : " + "pause request");
			mCore2.pause();
			playBtn.setBackgroundResource(R.drawable.playback_btn_play);
		} else {
			D.d(getMethodName(1) + " : " + "play request");
			mCore2.play();
			playBtn.setBackgroundResource(R.drawable.playback_btn_pause);
		}
	}

	public void onSeekBackClick(View v) {
		D.d(getMethodName(1) + " : " + "seek back request to pos:  " + (mPositionInSeconds - seekBackValueInSec) + " s");
		mCore2.setPosition(DateTimeUtils.getSecValueInMicros(mPositionInSeconds - seekBackValueInSec), 0,
				DateTimeUtils.getSecValueInMicros(mPositionInSeconds));
		mCore2.play();
	}

	public void onSeekForwardClick(View v) {
		D.d(getMethodName(1) + " : " + "seek forward request to pos: " + (mPositionInSeconds + seekAheadValueInSec)
				+ " s");
		mCore2.setPosition(DateTimeUtils.getSecValueInMicros(mPositionInSeconds + seekAheadValueInSec), 0,
				DateTimeUtils.getSecValueInMicros(mDurationInSeconds));
		mCore2.play();
	}

	public void onDurationClick(View v) {
		showTimeRemaining = !showTimeRemaining;
		localStorage.setShowTimeRemaining(showTimeRemaining);
		updateDurationText();
	}

	private void updateDurationText() {
		long value;
		if (!showTimeRemaining) {
			value = mDurationInSeconds;
		} else {
			value = mDurationInSeconds - mPositionInSeconds;
		}
		durationText.setText(DateTimeUtils.getDefaultTimeFormatter().print(DateTimeUtils.getSecValueInMillis(value)));
	}

	private void updateAudioTrackCount() {
		// TODO implement real audiotrack names as far as it will be implemented
		// on video player
		// hardcoded names used until then
		if (mAudioTrackCount > 1) {
			String[] items = new String[mAudioTrackCount];
			for (int i = 0; i < mAudioTrackCount; i++) {
				items[i] = "track " + i;
			}
			ArrayWheelAdapter<String> audiotrackWheelAdapter = new ArrayWheelAdapter<String>(this, items);
			audiotrackWheelAdapter.setItemResource(R.layout.wheel_text_item);
			audiotrackWheel.setViewAdapter(audiotrackWheelAdapter);
			audiotrackWheel.setCurrentItem(mActiveAudioTrack);
		}
		audioTrackSliding.setVisibility(mAudioTrackCount > 1 ? View.VISIBLE : View.GONE);
	}

	private void updateSubtitleCount() {
		// TODO implement real subtitle names as far as it will be implemented
		// on video player
		// hardcoded names used until then
		if (mSubtitleTrackCount > 1) {
			String[] items = new String[mSubtitleTrackCount];
			for (int i = 0; i < mSubtitleTrackCount; i++) {
				items[i] = "subtitle " + i;
			}
			ArrayWheelAdapter<String> subtitleWheelAdapter = new ArrayWheelAdapter<String>(this, items);
			subtitleWheelAdapter.setItemResource(R.layout.wheel_text_item);
			subtitleWheel.setViewAdapter(subtitleWheelAdapter);
			subtitleWheel.setCurrentItem(mActiveSubtitleTrack);
		}
		subtitleSliding.setVisibility(mSubtitleTrackCount > 1 ? View.VISIBLE : View.GONE);
	}

	private void updateVideoQualityValues() {
		// TODO implement real quality as far as it will be implemented on video
		// player
		// hardcoded names used until then
		if (mMediaTrackCount > 0) {
			String[] items = new String[mMediaTrackCount];
			for (int i = 0; i < mMediaTrackCount; i++) {
				items[i] = "quality " + i;
			}
			ArrayWheelAdapter<String> videoQualityWheelAdapter = new ArrayWheelAdapter<String>(this, items);
			videoQualityWheelAdapter.setItemResource(R.layout.wheel_text_item);
			videoQualityWheel.setViewAdapter(videoQualityWheelAdapter);
			videoQualityWheel.setCurrentItem(mActiveMediaTrack);
		}
	}

	private void setCurrentSeekbarValues() {
		volumeSeekBar.setProgressAndThumb(getProgressFromStreamVolume(audioManager
				.getStreamVolume(DEFAULT_AUDIO_STREAM)));
		brightnessSeekBar.setProgressAndThumb(getBrigtnessPercentage());
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setSoftNavigationBarVisibility(boolean visible) {
		if (android.os.Build.VERSION.SDK_INT >= ICS_VERSION_NUM) {
			int visibilityParam = visible ? View.SYSTEM_UI_FLAG_VISIBLE : View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			getWindow().getDecorView().setSystemUiVisibility(visibilityParam);

		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setOnSystemUIVisibilityChangeListener() {
		if (android.os.Build.VERSION.SDK_INT >= ICS_VERSION_NUM) {
			videoViewContainer.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {

				@Override
				public void onSystemUiVisibilityChange(int visibility) {
					if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
						showControls();
					}
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setCurrentSeekbarValues();
		if (mCore2 != null && mState == PlayerCore2.STATE_PAUSED) {
			mCore2.play();
		}
	}

	@Override
	protected void onPause() {
		D.disableLocalLog();
		D.clearLocalLogBuffer();
		NebelTVApp.setCurrentHandler(null);
		if (mCore2 != null && mState == PlayerCore2.STATE_PLAYING) {
			mCore2.pause();
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
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
			int currentStreamVolume = audioManager.getStreamVolume(DEFAULT_AUDIO_STREAM);
			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				currentStreamVolume--;
			} else {
				currentStreamVolume++;
			}
			if (currentStreamVolume < 0) {
				currentStreamVolume = 0;
			} else if (currentStreamVolume > maxStreamVolume) {
				currentStreamVolume = maxStreamVolume;
			}
			volumeSeekBar.setProgressAndThumb(getProgressFromStreamVolume(currentStreamVolume));
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			if (isControlsVisible()) {
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
		if (event.getAction() == MotionEvent.ACTION_UP) {
			onPlayClick(playBtn);
		}
		return super.onTouchEvent(event);
	}

	public void onGetConfigurationComplete(int status, Configuration configuration) {
		D.d(getMethodName(1) + ": " + status);
	}

	public void onSetConfigurationComplete(int status) {
		D.d(getMethodName(1) + ": " + status);
	}

	public void onGetStateComplete(final int state) {
		D.d(getMethodName(1) + ": " + state);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updateState(state);
			}
		});
	}

	public void onLoadComplete(int status) {
		D.d(getMethodName(1) + ": " + status);
		if (status == PlayerCore2.STATUS_OK) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					onPlayClick(playBtn);
				}
			});
		}
	}

	public void onUnloadComplete(int status) {
		D.d(getMethodName(1) + ": " + status);
	}

	public void onPlayComplete(int status) {
		D.d(getMethodName(1) + ": " + status);
	}

	public void onPauseComplete(int status) {
		D.d(getMethodName(1) + ": " + status);
	}

	public void onStopComplete(int status) {
		D.d(getMethodName(1) + ": " + status);
	}

	public void onGetDurationComplete(int status, long duration) {
		D.d(getMethodName(1) + ": " + status + " " + duration);
		mDurationInSeconds = DateTimeUtils.getSecValueInMicros(duration, true);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				videoSeekBar.setMax((int) mDurationInSeconds);
				updateDurationText();
			}
		});
	}

	public void onGetPositionComplete(int status, long position) {
		D.d(getMethodName(1) + ": " + status + " " + position, false);
	}

	public void onSetPositionComplete(int status, long newPosition) {
		D.d(getMethodName(1) + ": " + status + " " + newPosition, false);
	}

	public void onGetVolumeComplete(int status, float volume) {
		D.d(getMethodName(1) + ": " + status + " " + volume);
	}

	public void onSetVolumeComplete(int status, float newVolume) {
		D.d(getMethodName(1) + ": " + status + " " + newVolume);
	}

	public void onGetSubtitleStateComplete(int status, boolean state) {
		D.d(getMethodName(1) + ": " + status + " " + state);
	}

	public void onSetSubtitleStateComplete(int status, boolean newState) {
		D.d(getMethodName(1) + ": " + status + " " + newState);
	}

	public void onGetTrackCountComplete(int status, int type, int trackCount) {
		D.d(getMethodName(1) + ": " + status + " " + type + " " + trackCount);
		switch (type) {
		case PlayerCore2.TRACK_TYPE_AUDIO:
			mAudioTrackCount = trackCount;
			break;
		case PlayerCore2.TRACK_TYPE_VIDEO:
			// empty implementation
			break;
		case PlayerCore2.TRACK_TYPE_SUBTITLE:
			mSubtitleTrackCount = trackCount;
			break;
		}
	}

	public void onGetActiveTrackComplete(int status, int type, int track) {
		D.d(getMethodName(1) + ": " + status + " " + type + " " + track);
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
			// empty implementation
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
		D.d(getMethodName(1) + ": " + status + " " + type + " " + newTrack);
		switch (type) {
		case PlayerCore2.TRACK_TYPE_AUDIO:
			mActiveAudioTrack = newTrack;
			break;
		case PlayerCore2.TRACK_TYPE_VIDEO:
			// empty implementation
			break;
		case PlayerCore2.TRACK_TYPE_SUBTITLE:
			mActiveSubtitleTrack = newTrack;
			break;
		}
	}

	public void onStateChange(final int newState, int oldState) {
		D.d(getMethodName(1) + ": " + oldState + " => " + newState);
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
				videoSeekBar.setProgress((int) mPositionInSeconds);
				if (showTimeRemaining) {
					updateDurationText();
				}
			}
		});
	}

	public void onBufferingProgress(long bufferL1, long bufferL2) {
		// empty implementation
	}

	@Override
	public void onGetMediaCountComplete(int status, int mediaCount) {
		D.d(getMethodName(1) + ": " + status + " " + mediaCount);
		mMediaTrackCount = mediaCount;
	}

	@Override
	public void onGetActiveMediaComplete(int status, int media) {
		D.d(getMethodName(1) + ": " + status + " " + media);
		mActiveMediaTrack = media;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updateVideoQualityValues();
			}
		});
	}

	@Override
	public void onActivateMediaComplete(int status, int newMedia) {
		D.d(getMethodName(1) + ": " + status + " " + newMedia);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (videoSeekBar == seekBar) {
			videoSeekBar.notifyTimeTextPositionChange();
		} else if (volumeSeekBar == seekBar) {
			if (!fromUser) {
				return;
			}
			int volume = getStreamVolumeFromProgress(progress);
			if (audioManager.getStreamVolume(DEFAULT_AUDIO_STREAM) != volume) {
				audioManager.setStreamVolume(DEFAULT_AUDIO_STREAM, volume, 0);
			}
		} else if (brightnessSeekBar == seekBar) {
			if (!fromUser) {
				return;
			}
			setBrightnessPecentage(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if (videoSeekBar == seekBar) {
			videoSeekBar.setAutochange(false);
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (videoSeekBar == seekBar) {
			videoSeekBar.setAutochange(true);
			mCore2.setPosition(DateTimeUtils.getSecValueInMicros(seekBar.getProgress()), 0,
					DateTimeUtils.getSecValueInMicros(mDurationInSeconds));
		}
	}

	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if (audiotrackWheel == wheel) {
			mCore2.activateTrack(PlayerCore2.TRACK_TYPE_AUDIO, newValue);
		} else if (subtitleWheel == wheel) {
			mCore2.activateTrack(PlayerCore2.TRACK_TYPE_SUBTITLE, newValue);
		} else if (videoQualityWheel == wheel) {
			mCore2.activateMedia(newValue);
		}
	}

	private boolean isControlsVisible() {
		return controlContainer.getVisibility() == View.VISIBLE;
	}

	private void showControls() {
		controlContainer.startAnimation(animFadeIn);
		resetTimer();
		timer.schedule(controlVisibilityTimerTask, DEFAULT_CONTROLS_VISIBILITY_TIME);
	}

	private void resetTimer() {
		if (timer != null) {
			timer.cancel();
		}
		if (controlVisibilityTimerTask != null) {
			controlVisibilityTimerTask.cancel();
		}
		timer = new Timer();
		controlVisibilityTimerTask = new ControlVisibilityTimerTask();
	}

	private int getStreamVolumeFromProgress(int progress) {
		return (int) Math.ceil(progress * maxStreamVolume / (float) volumeSeekBar.getMax()) - 1;
	}

	private int getProgressFromStreamVolume(int volume) {
		return volume * volumeSeekBar.getMax() / maxStreamVolume;
	}

	private int getBrigtnessPercentage() {
		float floatPercentValue = (float) android.provider.Settings.System.getInt(getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS, MAX_SETTINGS_BRIGHTNESS_VALUE)
				/ (float) MAX_SETTINGS_BRIGHTNESS_VALUE;
		return Math.round(floatPercentValue * 100);
	}

	private void setBrightnessPecentage(int percentage) {
		float floatPercentValue = (float) percentage / 100f;
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS,
				Math.round(floatPercentValue * MAX_SETTINGS_BRIGHTNESS_VALUE));
	}

	private String getMethodName(final int depth) {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[ste.length - 1 - depth].getMethodName();
	}

	// used to pass video info data to flurry when uncaught exception occured
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		String logMessageStack = D.getMessageLocalBuffer(true);
		FlurryAgent.onError(TAG, logMessageStack != null ? logMessageStack : ex.getMessage(), ex);
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
			setSoftNavigationBarVisibility(false);
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
