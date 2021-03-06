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
package com.nebel_tv.wrapper;

import android.net.Uri;
import android.support.v4.util.LruCache;

import com.nebel_tv.content.api.IContentWrapper;
import com.nebel_tv.content.api.ContentWrapper;
import com.nebel_tv.content.api.WrapperResponse;

public class ContentWrapperManager {

	public static final String WRAPPER_HOST = "http://nebeltv.org";
	public static final String CALLBACK_PARAM_NAME = "callback";
	
	/**
	 * Default cache size: 4 MiB
	 */
	public static final int CACHE_SIZE = 4 * 1024 * 1024;

	private static ContentWrapperManager instance;

	private IContentWrapper contentWrapper;

	private LruCache<String, WrapperResponse> responseCache = null;

	public static synchronized ContentWrapperManager getInstance() {
		if (instance == null) {
			instance = new ContentWrapperManager();
		}
		return instance;
	}

	private ContentWrapperManager() {
		contentWrapper = new ContentWrapper();
		responseCache = new LruCache<String, WrapperResponse>(CACHE_SIZE);
	}

	public WrapperResponse getData(String url) {
		WrapperResponse response = null;
		
		synchronized (responseCache) {
			response = responseCache.get(url);
			if (response == null) {
				response = contentWrapper.getMediaData(url);
				
				if(response != null){
					responseCache.put(url, response);
				}
			}
		}
		return response;
	}

	public static String getCallbackFuncName(String url) {
		if (url == null) {
			return null;
		}

		Uri uri = Uri.parse(url);
		return uri.getQueryParameter(CALLBACK_PARAM_NAME);
	}
}
