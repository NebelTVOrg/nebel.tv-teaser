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

import com.nebel_tv.content.api.IContentWrapper;
import com.nebel_tv.content.api.ContentWrapper;
import com.nebel_tv.content.api.WrapperResponse;

public class ContentWrapperManager {
	
	public static final String IVAWRAPPER_HOST = "http://nebeltv.org";
	public static final String CALLBACK_PARAM_NAME = "callback";
	
	
	private static ContentWrapperManager instance;
	
	private IContentWrapper contentWrapper;
	
	public static synchronized ContentWrapperManager getInstance() {
		if(instance==null) {
			instance = new ContentWrapperManager();
		}
		return instance;
	}
	
	private ContentWrapperManager() {
		contentWrapper = new ContentWrapper();
	}
	
	public WrapperResponse getData(String url) {
		return contentWrapper.getMediaData(url);
	}
	
	public static String getCallbackFuncName(String url) {
		if(url==null) {
			return null;
		}
		
		Uri uri = Uri.parse(url);
		return uri.getQueryParameter(CALLBACK_PARAM_NAME);
	}
}
