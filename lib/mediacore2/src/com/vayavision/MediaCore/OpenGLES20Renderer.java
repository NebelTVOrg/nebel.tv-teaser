package com.vayavision.MediaCore;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.os.Build;

public class OpenGLES20Renderer implements GLSurfaceView.Renderer {
	static{
		System.loadLibrary("ass");
		System.loadLibrary("ffmpeg");
		System.loadLibrary("SystemCore");
		try{
			System.loadLibrary("SystemExt" + Build.VERSION.SDK_INT);
		}catch(UnsatisfiedLinkError e){
		}
		System.loadLibrary("PlayerCore");
	}

	private long handle = 0;
	private native void onDrawFrame();
	private native void onSurfaceChanged(int width, int height);
	
    public void onDrawFrame(GL10 gl) {
        onDrawFrame();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
    	onSurfaceChanged(width, height);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }
}
