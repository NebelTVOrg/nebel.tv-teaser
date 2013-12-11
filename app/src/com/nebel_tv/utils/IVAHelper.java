package com.nebel_tv.utils;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

public class IVAHelper {

	public static final String INTERTCEPT_URL = "nebeltv://wrapper/getMedias";
	
	private static final String CALLBACK_PARAM = "callback";
	
	private static final String MEDIA_ID_PARAM = "media_id";
	private static final String IMAGE_PARAM = "image";
	private static final String TITLE_PARAM = "title";
	private static final String AUTHOR_PARAM = "author ";
	private static final String DATE_PARAM = "date ";
	private static final String DESCRIPTION_PARAM = "descr";
	
	private static IVAHelper instance;
	
	private DateTimeFormatter dateFormatter;
	
	public static synchronized IVAHelper getInstance() {
		if(instance==null) {
			instance = new IVAHelper();
		}
		return instance;
	}
	
	private IVAHelper() {
		dateFormatter = DateTimeFormat.shortDate();
	}
	
	
	public  String getMedias(String url) {
		Uri uri = Uri.parse(url);
		String callbackFuncName = getCallbackFuncName(uri);
		//TODO parse rest URL parameters
		
		//return hardcoded json array
		JSONArray jsonArray = new JSONArray();
		List<MediaItemModel> mediaItems = createHardcodedMediaItems();
		
		for(MediaItemModel mediaItem : mediaItems) {
			jsonArray.put(buildMediaItemJSON(mediaItem));
		}
		return callbackFuncName+"(\""+formatJsonForJS(jsonArray.toString())+"\");";
	}
	
	public String getMediaItem(String url) {
		Uri uri = Uri.parse(url);
		String callbackFuncName = getCallbackFuncName(uri);
		//TODO parse rest URL parameters
		
		//return hardcoded json object
		List<MediaItemModel> mediaItems = createHardcodedMediaItems();
		JSONObject jsonObj = buildMediaItemJSON(mediaItems.get(0));
		return callbackFuncName+"(\""+formatJsonForJS(jsonObj.toString())+"\");";
	}
	
	//replace " with \"
	private String formatJsonForJS(String value) {
		return value.replace("\"", "\\\"");
	}
	
	private  String getCallbackFuncName(Uri uri) {
		return uri.getQueryParameter(CALLBACK_PARAM);
	}
	
	private JSONObject buildMediaItemJSON(MediaItemModel mediaItem) {
		JSONObject jsonMediaItem = new JSONObject();
		try {
			jsonMediaItem.put(MEDIA_ID_PARAM, mediaItem.getMediaId());
			jsonMediaItem.put(IMAGE_PARAM, mediaItem.getImageUrl());
			jsonMediaItem.put(TITLE_PARAM, mediaItem.getTitle());
			jsonMediaItem.put(AUTHOR_PARAM, mediaItem.getAuthor());
			jsonMediaItem.put(DATE_PARAM, dateFormatter.print(mediaItem.getDate()));
			jsonMediaItem.put(DESCRIPTION_PARAM, mediaItem.getDescription());
		} catch(JSONException e) {
			D.e(e);
		}
		return jsonMediaItem;
	}
	
	private List<MediaItemModel> createHardcodedMediaItems() {
		ArrayList<MediaItemModel> items = new ArrayList<IVAHelper.MediaItemModel>();
		
		MediaItemModel item = new MediaItemModel();
		item.setMediaId("1");
		item.setImageUrl("http://content.internetvideoarchive.com/content/photos/119/1_042.jpg");
		item.setTitle("BLUE CHIPS");
		item.setAuthor("William Friedkin");
		item.setDate(new DateTime(1382956560000L));
		item.setDescription("On court action combined with off court drama make this an exciting look at big time college hoops. Story focuses on the recruitment of 'blue chip' prospects that can make or break a season and a coach's career. Look for cameo by Bobby Knight.");
		items.add(item);
		
		item = new MediaItemModel();
		item.setMediaId("2");
		item.setImageUrl("http://content.internetvideoarchive.com/content/photos/8562/2_042.jpg");
		item.setTitle("ON THE TOWN");
		item.setAuthor("Stanley Donen");
		item.setDate(new DateTime(1384787160000L));
		item.setDescription("Three sailors team up to find the beautiful poster girl whose picture they saw in a New York City subway. Oscar-winning score by Bernstein, Comden and Green!");
		items.add(item);
		
		item = new MediaItemModel();
		item.setMediaId("3");
		item.setImageUrl("http://content.internetvideoarchive.com/content/photos/000/000000_36.jpg");
		item.setTitle("HOMECOMING: A CHRISTMAS STORY, THE");
		item.setAuthor("Fielder Cook");
		item.setDate(new DateTime(1383339840000L));
		item.setDescription("A Virginia mountain family celebrates Christmas as they anxiously await their father's return through a blizzard.");
		items.add(item);
		
		item = new MediaItemModel();
		item.setMediaId("6");
		item.setImageUrl("http://content.internetvideoarchive.com/content/photos/011/6_012.jpg");
		item.setTitle("CODE OF SILENCE");
		item.setAuthor("Andrew Davis");
		item.setDate(new DateTime(1382709300000L));
		item.setDescription("A Chicago vice cop must battle the mob as well as his own department's corruption. One of Norris' best!");
		items.add(item);
		
		item = new MediaItemModel();
		item.setMediaId("7");
		item.setImageUrl("http://content.internetvideoarchive.com/content/photos/006/000292_11.jpg");
		item.setTitle("P.O.W.: THE ESCAPE");
		item.setAuthor("Gideon Amir");
		item.setDate(new DateTime(1382720760000L));
		item.setDescription("Story of an American prisoner-of-war at the tail end of U.S. involvement in Vietnam. Trapped in a lame plot, clumsy direction, and muddled action, the inconsistant talents of Carradine go AWOL.");
		items.add(item);
		
		return items;
	}
	
	private static class MediaItemModel {
		private String mediaId;
		private String imageUrl;
		private String title;
		private String author;
		private DateTime date;
		private String description;
		
		public String getMediaId() {
			return mediaId;
		}
		public void setMediaId(String mediaId) {
			this.mediaId = mediaId;
		}
		public String getImageUrl() {
			return imageUrl;
		}
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getAuthor() {
			return author;
		}
		public void setAuthor(String author) {
			this.author = author;
		}
		public DateTime getDate() {
			return date;
		}
		public void setDate(DateTime date) {
			this.date = date;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
	}
	
}
