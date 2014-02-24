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
package com.nebel_tv.utils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

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
			"http://mirrorblender.top-ix.org/peach/bigbuckbunny_movies/big_buck_bunny_1080p_h264.mov" };

	public static void startVideoDownload(Context context) {
		DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		Cursor cursor = dm.query(new Query());
		File configDirectory = new File(Environment.getExternalStorageDirectory(), ConfigHelper.CONFIG_FOLDER_NAME);

		int videoUrlsSize = VIDEO_URLS.length;
		ArrayList<String> bufferVideoUrls = new ArrayList<String>(Arrays.asList(VIDEO_URLS));

		if (cursor.moveToFirst()) {
			int uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
			int statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
			do {
				String uri = cursor.getString(uriColumn);
				int status = cursor.getInt(statusColumn);
				for (int i = 0; i < videoUrlsSize; i++) {
					if (VIDEO_URLS[i].equals(uri) && status != DownloadManager.STATUS_FAILED) {
						bufferVideoUrls.remove(VIDEO_URLS[i]);
					}
					if (bufferVideoUrls.size() == 0) {
						break;
					}
				}
			} while (cursor.moveToNext());
		}
		cursor.close();

		for (String url : bufferVideoUrls) {
			Uri uri = Uri.parse(url);
			Request request = new Request(uri);
			request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
			request.setTitle(uri.getLastPathSegment());
			request.setDestinationUri(Uri.fromFile(new File(configDirectory, uri.getLastPathSegment())));
			dm.enqueue(request);
		}
	}

	public static String[] getVideoFiles(Context context) {
		// return new String[] {""};
		DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		Cursor cursor = dm.query(new Query());
		int videoUrlsSize = VIDEO_URLS.length;
		ArrayList<String> videoFiles = new ArrayList<String>();
		if (cursor.moveToFirst()) {
			int uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
			int statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
			int localUriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
			do {
				String uri = cursor.getString(uriColumn);
				int status = cursor.getInt(statusColumn);
				String localUri = cursor.getString(localUriColumn);
				for (int i = 0; i < videoUrlsSize; i++) {
					if (VIDEO_URLS[i].equals(uri) && status == DownloadManager.STATUS_SUCCESSFUL) {
						videoFiles.add(new File(URI.create(localUri)).getAbsolutePath());
					}
				}
				if (videoFiles.size() >= videoUrlsSize) {
					break;
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return videoFiles.size() >= videoUrlsSize ? videoFiles.toArray(new String[videoFiles.size()]) : null;
	}

}
