package com.vayavision.android.MediaPlayer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.vayavision.MediaCore.MediaCore;
import com.vayavision.MediaCore.PlayerCore2;
import com.vayavision.MediaCore.PlayerCore2.Configuration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MediaPlayer extends Activity implements PlayerCore2.OnEventListener {
	private static final String TAG = "Player";
	
	private static String getMethodName(final int depth) {
	  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	  return ste[ste.length - 1 - depth].getMethodName();
	}	
	
	private SurfaceView mSurfaceView = null;
	private PlayerCore2 mCore2 = null;
	
	private int mState;
	private long mPosition;
	private long mDuration;
	private long mL1Length;
	private long mL2Length;
	private int mAudioTrackCount;
	private int mVideoTrackCount;
	private int mSubtitleTrackCount;
	private boolean mSubtitleState;
	private int mActiveAudioTrack;
	private int mActiveVideoTrack;
	private int mActiveSubtitleTrack;
	
	private Button mBtnLoadUnload;
	private Button mBtnPlayPause;
	private Button mBtnStop;
	private Button mBtnSeekLeft;
	private Button mBtnSeekRight;
	private Button mBtnToggleAudio;
	private Button mBtnToggleVideo;
	private Button mBtnToggleSubtitle;
	private TextView mTxtFileName;
	private TextView mTxtPlaybackStats;
	private View mControlView;
	private View mStatusView;
	
	/**************************************************
	 * File browser resources
	 *************************************************/
	private static final int DIALOG_LOAD_FILE = 1000;	
	
	// Stores names of traversed directories
	private ArrayList<String> directories = new ArrayList<String>();

	// Check if the first level of the directory structure is the one showing
	private Boolean topLevel = true;

	private Item[] fileList;
	private File browsePath = new File("/mnt");
	
	private String browseFile = null;
	
	private ListAdapter listAdapter;
	
	private class SurfaceHolderCallback implements SurfaceHolder.Callback{
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			// TODO Auto-generated method stub
		}

		public void surfaceCreated(SurfaceHolder holder) {
			if(mCore2 != null){
				mCore2.setWindowState(true);
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if(mCore2 != null){
				mCore2.setWindowState(false);
			}
		}
	}
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mControlView = (View)findViewById(R.id.controlLayout);
		mStatusView = (View)findViewById(R.id.statusLayout);
		
		mSurfaceView = new SurfaceView(this);
		
		RelativeLayout frame = (RelativeLayout)mStatusView;
		
		if(frame != null){
			frame.addView(mSurfaceView, 0);
		}
		
		SurfaceHolder holder = mSurfaceView.getHolder();
		
		holder.addCallback(new SurfaceHolderCallback());
		
        mCore2 = MediaCore.createPlayerCore2(holder, getExternalCacheDir().getAbsolutePath());
		
        initPlayerControlListeners();

        mCore2.addListener(this);
	        
        mCore2.getState();
    }
	
	private void updateState(int state) {
		switch(state){
		case PlayerCore2.STATE_IDLE:
			mBtnLoadUnload.setText(getResources().getString(R.string.btn_load));
			
			mBtnLoadUnload.setEnabled(true);
			mTxtFileName.setText("Nothing loaded");
			
			mBtnPlayPause.setEnabled(false);
			mBtnStop.setEnabled(false);
			mBtnSeekLeft.setEnabled(false);
			mBtnSeekRight.setEnabled(false);
			mBtnToggleAudio.setEnabled(false);
			mBtnToggleVideo.setEnabled(false);
			mBtnToggleSubtitle.setEnabled(false);
			break;		
		case PlayerCore2.STATE_STOPPED:
			mBtnLoadUnload.setText(getResources().getString(R.string.btn_unload));
			mBtnPlayPause.setText(getResources().getString(R.string.btn_play));
	        
			mBtnLoadUnload.setEnabled(true);
			mBtnPlayPause.setEnabled(true);
			mBtnStop.setEnabled(false);
			mBtnSeekLeft.setEnabled(false);
			mBtnSeekRight.setEnabled(false);
			mBtnToggleAudio.setEnabled(false);
			mBtnToggleVideo.setEnabled(false);
			mBtnToggleSubtitle.setEnabled(false);
			break;		
		case PlayerCore2.STATE_PLAYING:
			mBtnPlayPause.setText(getResources().getString(R.string.btn_pause));
			
			mBtnLoadUnload.setEnabled(false);
			mBtnPlayPause.setEnabled(true);
			mBtnStop.setEnabled(true);
			mBtnSeekLeft.setEnabled(true);
			mBtnSeekRight.setEnabled(true);
			mBtnToggleAudio.setEnabled(mAudioTrackCount > 1);
			mBtnToggleVideo.setEnabled(mVideoTrackCount > 1);
			mBtnToggleSubtitle.setEnabled(mSubtitleTrackCount > 0);
			break;		
		case PlayerCore2.STATE_PAUSED:
			mBtnPlayPause.setText(getResources().getString(R.string.btn_play));
			mBtnStop.setText(getResources().getString(R.string.btn_stop));
			
			mBtnLoadUnload.setEnabled(false);
			mBtnPlayPause.setEnabled(true);
			mBtnStop.setEnabled(true);
			mBtnSeekLeft.setEnabled(true);
			mBtnSeekRight.setEnabled(true);
			mBtnToggleAudio.setEnabled(mAudioTrackCount > 1);
			mBtnToggleVideo.setEnabled(mVideoTrackCount > 1);
			mBtnToggleSubtitle.setEnabled(mSubtitleTrackCount > 0);
			break;		
		case PlayerCore2.STATE_BUFFERING:
			mBtnPlayPause.setText(getResources().getString(R.string.btn_pause));
			
			mBtnLoadUnload.setEnabled(false);
			mBtnPlayPause.setEnabled(false);
			mBtnStop.setEnabled(true);
			mBtnSeekLeft.setEnabled(true);
			mBtnSeekRight.setEnabled(true);
			mBtnToggleAudio.setEnabled(mAudioTrackCount > 1);
			mBtnToggleVideo.setEnabled(mVideoTrackCount > 1);
			mBtnToggleSubtitle.setEnabled(mSubtitleTrackCount > 0);
			break;
		default:
			Log.e(TAG, "Unknown state " + state);
			return;
		}
		
		mTxtPlaybackStats.setText(String.format("%.02f/%.02f %.02f/%.02f", (float )mL1Length / 1000000.0, (float )mL2Length / 1000000.0, (float )mPosition / 1000000.0, (float )mDuration / 1000000.0));
		
		mState = state;
	}	
    
    protected void initPlayerControlListeners(){
    	mBtnLoadUnload = (Button) findViewById(R.id.btnLoadUnload);
        mBtnLoadUnload.setOnClickListener(new OnClickListener() {
        	 public void onClick(View v) {
        		 if(mState == PlayerCore2.STATE_IDLE){
					 String url = new String("http://54.201.170.111/assets/001-270p-686kb.mp4");
    				 mCore2.load(new String[]{url});
    				 mTxtFileName.setText("URL: " + url);
/*    				 
        			 loadFileList();
        			 showDialog(DIALOG_LOAD_FILE);
*/
        		 }else{
       				 mCore2.unload();
        		 }        			 
        	 }
		});        
        
        mBtnPlayPause = (Button) findViewById(R.id.btnPlayPause);
        mBtnPlayPause.setOnClickListener(new OnClickListener() {
        	 public void onClick(View v) {
           		 if(mState == PlayerCore2.STATE_PLAYING){
        			 mCore2.pause();
        		 }else{
       				 mCore2.play();
        		 }
        	 }
		});	        
        
        mBtnStop = (Button) findViewById(R.id.btnStop);
        mBtnStop.setOnClickListener(new OnClickListener() {				
			public void onClick(View v) {
				mCore2.stop();
			}
		});
        
        mBtnSeekLeft = (Button) findViewById(R.id.btnSeekLeft);
        mBtnSeekLeft.setOnClickListener(new OnClickListener() {				
			public void onClick(View v) {
				mCore2.setPosition(mPosition - 10 * 1000 * 1000, 0, mPosition);
			}
		});	        
        
        mBtnSeekRight = (Button) findViewById(R.id.btnSeekRight);
        mBtnSeekRight.setOnClickListener(new OnClickListener() {				
			public void onClick(View v) {
				mCore2.setPosition(mPosition + 10 * 1000 * 1000, mPosition, mDuration); 
			}
		});  

        mBtnToggleAudio = (Button) findViewById(R.id.btnToggleAudio);
        mBtnToggleAudio.setOnClickListener(new OnClickListener() {				
			public void onClick(View v) {
				if(mAudioTrackCount < 2){
					return;
				}

				int nextTrack = mActiveAudioTrack + 1;

				if(nextTrack >= mAudioTrackCount){
					nextTrack = 0;
				}

				mCore2.activateTrack(PlayerCore2.TRACK_TYPE_AUDIO, nextTrack);				
			}
		});  

        mBtnToggleVideo = (Button) findViewById(R.id.btnToggleVideo);
        mBtnToggleVideo.setOnClickListener(new OnClickListener() {				
			public void onClick(View v) {
				if(mVideoTrackCount < 2){
					return;
				}

				int nextTrack = mActiveVideoTrack + 1;

				if(nextTrack >= mVideoTrackCount){
					nextTrack = 0;
				}

				mCore2.activateTrack(PlayerCore2.TRACK_TYPE_VIDEO, nextTrack);				
			}
		});  

        mBtnToggleSubtitle = (Button) findViewById(R.id.btnToggleSubtitle);
        mBtnToggleSubtitle.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				if(mSubtitleTrackCount < 1){
					return;
				}

				if(mSubtitleState == false){
					mCore2.setSubtitleState(true);
				}else{
					int nextTrack = mActiveSubtitleTrack + 1;

					if(nextTrack >= mSubtitleTrackCount){
						nextTrack = 0;
						mCore2.setSubtitleState(false);
					}

					mCore2.activateTrack(PlayerCore2.TRACK_TYPE_SUBTITLE, nextTrack);
				}
			}
		});  
        
        mTxtFileName = (TextView) findViewById(R.id.txtFilePath);
        mTxtPlaybackStats = (TextView) findViewById(R.id.txtFilePosition);
    }
    
    @Override
    protected void onResume() 
    {
        super.onResume();
        if(mCore2 != null && mState == PlayerCore2.STATE_PAUSED){
        	mCore2.play();
        }
    }

    @Override
    protected void onPause() 
    {
    	if(mCore2 != null && mState == PlayerCore2.STATE_PLAYING){
    		mCore2.pause();
       	}
        super.onPause();
	}
    
    public void onGetConfigurationComplete(int status, Configuration configuration) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}
	
	public void onSetConfigurationComplete(int status) {
		Log.d(TAG, getMethodName(1) + ": " + status);
	}
	
	public void onGetStateComplete(int state) {
		Log.d(TAG, getMethodName(1) + ": " + state);
		updateState(state);
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
		mDuration = duration;

		mStatusView.post(new Runnable() {
	        public void run() {
	        	mTxtPlaybackStats.setText(String.format("%.02f/%.02f %.02f/%.02f", (float )mL1Length / 1000000.0, (float )mL2Length / 1000000.0, (float )mPosition / 1000000.0, (float )mDuration / 1000000.0));
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
		mSubtitleState = state;
	}

	public void onSetSubtitleStateComplete(int status, boolean newState) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + newState);
		mSubtitleState = newState;
	}

	public void onGetTrackCountComplete(int status, int type, int trackCount) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + type + " " + trackCount);
		switch (type) {
		case PlayerCore2.TRACK_TYPE_AUDIO:
			mAudioTrackCount = trackCount;
			break;
		case PlayerCore2.TRACK_TYPE_VIDEO:
			mVideoTrackCount = trackCount;
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
			break;
		case PlayerCore2.TRACK_TYPE_VIDEO:
			mActiveVideoTrack = track;
			break;
		case PlayerCore2.TRACK_TYPE_SUBTITLE:
			mActiveSubtitleTrack = track;
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
			mActiveVideoTrack = newTrack;
			break;
		case PlayerCore2.TRACK_TYPE_SUBTITLE:
			mActiveSubtitleTrack = newTrack;
			break;
		}
	}

	public void onGetMediaCountComplete(int status, int mediaCount) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + mediaCount);
	}
	
	public void onGetActiveMediaComplete(int status, int media) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + media);
	}
	
	public void onActivateMediaComplete(int status, int newMedia) {
		Log.d(TAG, getMethodName(1) + ": " + status + " " + newMedia);
	}
	
	public void onStateChange(final int newState, int oldState) {
		Log.d(TAG, getMethodName(1) + ": " + oldState + " => " + newState);
		mControlView.post(new Runnable() {
	        public void run() {
	    		updateState(newState);
	        }
	    });
	}		

	public void onPositionChange(final long position) {
		mPosition = position;
		
		mStatusView.post(new Runnable() {
	        public void run() {
	        	mTxtPlaybackStats.setText(String.format("%.02f/%.02f %.02f/%.02f", (float )mL1Length / 1000000.0, (float )mL2Length / 1000000.0, (float )mPosition / 1000000.0, (float )mDuration / 1000000.0));
	        }
	    });
	}

	public void onBufferingProgress(long bufferL1, long bufferL2) {
		mL1Length = bufferL1;
		mL2Length = bufferL2;
		
		mStatusView.post(new Runnable() {
	        public void run() {
	        	mTxtPlaybackStats.setText(String.format("%.02f/%.02f %.02f/%.02f", (float )mL1Length / 1000000.0, (float )mL2Length / 1000000.0, (float )mPosition / 1000000.0, (float )mDuration / 1000000.0));
	        }
	    });
	}
	
	private void loadFileList() {
		try {
			browsePath.mkdirs();
		} catch (SecurityException e) {
			Log.e(TAG, "unable to write on the sd card ");
		}

		// Checks whether path exists
		if (browsePath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					// Filters based on whether the file is hidden or not
					return (sel.isFile() || sel.isDirectory())
							&& !sel.isHidden();

				}
			};

			String[] fList = browsePath.list(filter);
			fileList = new Item[fList.length];
			for (int i = 0; i < fList.length; i++) {
				fileList[i] = new Item(fList[i], R.drawable.file_icon);

				// Convert into file path
				File sel = new File(browsePath, fList[i]);

				// Set drawables
				if (sel.isDirectory()) {
					fileList[i].icon = R.drawable.directory_icon;
					Log.d("DIRECTORY", fileList[i].file);
				} else {
					Log.d("FILE", fileList[i].file);
				}
			}

			if (!topLevel) {
				Item temp[] = new Item[fileList.length + 1];
				for (int i = 0; i < fileList.length; i++) {
					temp[i + 1] = fileList[i];
				}
				temp[0] = new Item("Up", R.drawable.directory_up_icon);
				fileList = temp;
			}
		} else {
			Log.e(TAG, "path does not exist");
		}

		listAdapter = new ArrayAdapter<Item>(this,
				android.R.layout.select_dialog_item, android.R.id.text1, fileList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// creates view
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view
						.findViewById(android.R.id.text1);

				// put the image on the text view
				textView.setCompoundDrawablesWithIntrinsicBounds(
						fileList[position].icon, 0, 0, 0);

				// add margin between image and text (support various screen
				// densities)
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				textView.setCompoundDrawablePadding(dp5);

				return view;
			}
		};

	}

	private class Item {
		public String file;
		public int icon;

		public Item(String file, Integer icon) {
			this.file = file;
			this.icon = icon;
		}

		@Override
		public String toString() {
			return file;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);

		if (fileList == null) {
			Log.e(TAG, "No files loaded");
			dialog = builder.create();
			return dialog;
		}

		switch (id) {
		case DIALOG_LOAD_FILE:
			builder.setTitle("Choose media file");
			builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					browseFile = fileList[which].file;
					File sel = new File(browsePath + File.separator + browseFile);
					if (sel.isDirectory()) {
						topLevel = false;

						// Adds chosen directory to list
						directories.add(browseFile);
						fileList = null;
						browsePath = new File(sel + "");

						loadFileList();

						removeDialog(DIALOG_LOAD_FILE);
						showDialog(DIALOG_LOAD_FILE);
						Log.d(TAG, browsePath.getAbsolutePath());

					} else if (browseFile.equalsIgnoreCase("up") && !sel.exists()) {
						// Checks if 'up' was clicked

						// present directory removed from list
						String s = directories.remove(directories.size() - 1);

						// path modified to exclude present directory
						browsePath = new File(browsePath.toString().substring(0,
								browsePath.toString().lastIndexOf(s)));
						fileList = null;

						// if there are no more directories in the list, then
						// its the first level
						if (directories.isEmpty()) {
							topLevel = true;
						}
						loadFileList();

						removeDialog(DIALOG_LOAD_FILE);
						showDialog(DIALOG_LOAD_FILE);
						Log.d(TAG, browsePath.getAbsolutePath());

					}else{
        				 mCore2.load(new String[]{sel.getAbsolutePath()});
        				 mTxtFileName.setText("File: " + sel.getName());//sel.getAbsolutePath());
					}
				}
			});
			break;
		}
		dialog = builder.show();
		return dialog;
	}	
}
