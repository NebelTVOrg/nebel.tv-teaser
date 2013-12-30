/**
 * 
 */
package com.vayavision.MediaCore;

import android.os.Build;
import android.view.SurfaceHolder;

/**
 * @author Andrew Voznytsa <andrew.voznytsa@gmail.com>
 *
 */
public class MediaCore {
	
	static{
		System.loadLibrary("ffmpeg");
		System.loadLibrary("SystemCore");
		try{
			System.loadLibrary("SystemExt" + Build.VERSION.SDK_INT);
		}catch(UnsatisfiedLinkError e){
		}
		System.loadLibrary("PlayerCore");
	}
	
	public native static PlayerCore2 createPlayerCore2(SurfaceHolder holder);
}
