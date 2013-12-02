package com.nebel_tv.utils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

public class DownloadManagerHelper {
	
	private static final String[] VIDEO_URLS = new String[] {
		"http://mirrorblender.top-ix.org/peach/bigbuckbunny_movies/big_buck_bunny_480p_h264.mov",
		"http://mirrorblender.top-ix.org/peach/bigbuckbunny_movies/big_buck_bunny_1080p_h264.mov"
	};
	
	public static void startVideoDownload(Context context) {
		DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		File configDirectory = new File(Environment.getExternalStorageDirectory(),ConfigHelper.CONFIG_FOLDER_NAME);
		
		for(int i=0; i<VIDEO_URLS.length; i++) {
			Uri uri = Uri.parse(VIDEO_URLS[i]);
			Request request = new Request(uri);
			request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
			request.setTitle(uri.getLastPathSegment());
			request.setDestinationUri(Uri.fromFile(new File(configDirectory, uri.getLastPathSegment())));
			dm.enqueue(request);
		}
	}
	
	public static String[] getVideoFiles(Context context) {
		DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		Cursor cursor = dm.query(new Query());
		int videoUrlsSize = VIDEO_URLS.length;
		ArrayList<String> videoFiles = new ArrayList<String>();
		if(cursor.moveToFirst()) {
			int uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
			int statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
			int localUriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
			do {
				String uri = cursor.getString(uriColumn);
				int status = cursor.getInt(statusColumn); 
				String localUri = cursor.getString(localUriColumn);
				for(int i=0; i<videoUrlsSize; i++) {
					if(VIDEO_URLS[i].equals(uri) && status==DownloadManager.STATUS_SUCCESSFUL) {
						videoFiles.add( new File(URI.create(localUri)).getAbsolutePath());
					}
				}
				if(videoFiles.size()>=videoUrlsSize) {
					break;
				}
			} while(cursor.moveToNext());
		}
		return videoFiles.size()>=videoUrlsSize?videoFiles.toArray(new String[videoFiles.size()]):null;
	}
	
}
