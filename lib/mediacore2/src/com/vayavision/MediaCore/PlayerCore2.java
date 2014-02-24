package com.vayavision.MediaCore;

public class PlayerCore2 {
	public interface OnEventListener {
		void onGetConfigurationComplete(int status, Configuration configuration);

		void onSetConfigurationComplete(int status);

		void onGetStateComplete(int state);

		void onLoadComplete(int status);

		void onUnloadComplete(int status);

		void onPlayComplete(int status);

		void onPauseComplete(int status);

		void onStopComplete(int status);

		void onGetDurationComplete(int status, long duration);

		void onGetPositionComplete(int status, long position);

		void onSetPositionComplete(int status, long newPosition);

		void onGetVolumeComplete(int status, float volume);

		void onSetVolumeComplete(int status, float newVolume);

		void onGetSubtitleStateComplete(int status, boolean state);

		void onSetSubtitleStateComplete(int status, boolean newState);

		void onGetTrackCountComplete(int status, int type, int trackCount);

		void onGetActiveTrackComplete(int status, int type, int track);

		void onActivateTrackComplete(int status, int type, int newTrack);

		void onGetMediaCountComplete(int status, int mediaCount);

		void onGetActiveMediaComplete(int status, int media);

		void onActivateMediaComplete(int status, int newMedia);

		void onStateChange(int newState, int oldState);

		void onPositionChange(long position);

		void onBufferingProgress(long L1Length, long L2Length);
	}

	public final static int STATUS_INVALID_STATE = -5;
	public final static int STATUS_UNSUPPORTED_MEDIA = -4;
	public final static int STATUS_RUN_TIME_ERROR = -3;
	public final static int STATUS_NOT_ENOUGH_MEMORY = -2;
	public final static int STATUS_INVALID_ARG = -1;
	public final static int STATUS_OK = 0;
	public final static int STATUS_BUSY = 1;
	public final static int STATUS_FEATURE_NOT_AVAILABLE = 2;
	public final static int STATUS_NO_MORE_DATA = 3;
	public final static int STATUS_OK_BUT_DATA_TRUNCATED = 4;
	public final static int STATUS_END_OF_STREAM = 5;

	public final static int STATE_IDLE = 0;
	public final static int STATE_STOPPED = 1;
	public final static int STATE_PLAYING = 2;
	public final static int STATE_PAUSED = 3;
	public final static int STATE_BUFFERING = 4;

	public final static int TRACK_TYPE_AUDIO = 0;
	public final static int TRACK_TYPE_VIDEO = 1;
	public final static int TRACK_TYPE_SUBTITLE = 2;

	public static class Configuration {
		public int tickResolution;
		public int maxQueueSize;
		public boolean resetMediaOnStop;
	}

	private long handle = 0;

	public native void release();

	public native void emptyCommandQueue();

	public native int addListener(OnEventListener listener);

	public native void removeListener(OnEventListener listener);

	public native int getConfiguration();

	public native int setConfiguration(Configuration configuration);

	public native int getState();

	public native int load(String[] mediaPath);

	public native int unload();

	public native int play();

	public native int pause();

	public native int stop();

	public native int getDuration();

	public native int getPosition();

	public native int setPosition(long newPosition, long minPosition, long maxPosition);

	public native int getVolume();

	public native int setVolume(float newVolume);

	public native int getSubtitleState();

	public native int setSubtitleState(boolean newState);

	public native int getTrackCount(int type);

	public native int getActiveTrack(int type);

	public native int activateTrack(int type, int newTrack);

	public native int getMediaCount();

	public native int getActiveMedia();

	public native int activateMedia(int newMedia);

	public native void setWindowState(boolean renderable);
}
