package com.nebel_tv.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.Environment;

import com.flurry.android.FlurryAgent;
import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;
import com.nebel_tv.model.Mood;
import com.nebel_tv.model.TopView;

public class ConfigHelper {
	
	//config file version should be changed after every structural change in config file
	private static final String CONFIG_FILE_VERSION = "1.1";
	
	public static final String TAG = ConfigHelper.class.getName();
	public static final String CONFIG_FOLDER_NAME = "NebelTV";
	private static final String CONFIG_FILE_NAME = "config";
	private static final String CONFIG_FILE_EXTENSION = ".xml";
	
	private static final String MOOD_TAG = "mood";
	private static final String MOOD_NAME_ATTRIBUTE = "name";
	private static final String FAMILY_MOOD_TAG = "family";
	private static final String KIDS_MOOD_TAG = "kids";
	private static final String ROMANCE_MOOD_TAG = "romance";
	private static final String CONFIG_TAG = "config";
	private static final String VIDEO_OPTIONS_TAG = "video_options";	
	private static final String JUMP_AHEAD_TAG = "jump_ahead_sec";
	private static final String JUMP_BACK_TAG = "jump_back_sec";
	private static final String FRIENDS_FEED_TAG = "friends_feed";
	private static final String WHATS_CLOSE_TAG = "whats_close";
	private static final String RECENTLY_VIEWED_TAG = "recently_viewed";
	private static final String WHATS_HOT_TAG = "whats_hot";
	private static final String PICTURES_TAG = "pictures";
	private static final String RECOMMENDED_TAG = "recommended";
	private static final String NEBEL_TV_HOMEPAGE_TAG = "nebel_tv_homepage";
	private static final String FRONTEND_DOWNLOAD_LINK_TAG = "frontend_download_link";
	
	private static final String INVALID_CONFIG_MSG = "Invalid config";
	
    private static ConfigHelper instance;
	
	private HashMap<Mood, HashMap<TopView, String>> configUrls;
	private int jumpAheadSecValue; 
	private int jumpBackSecValue;
	private String nebelTVHomepage;
	private String frontendDownloadLink;
	private Context context;

    private ConfigHelper(Context context) {
    	this.context = context;
    	configUrls = new HashMap<Mood, HashMap<TopView, String>>();
    	jumpAheadSecValue = 0;
    	jumpBackSecValue = 0;
    }

    public static synchronized ConfigHelper getInstance() {
        if (instance == null) {
        	instance = new ConfigHelper(NebelTVApp.getContext());
        }
        //parse config every time we request config helper class
        //so the values will be surely up to date
        instance.parseConfig();
        return instance;
    }
	
	public HashMap<Mood, HashMap<TopView, String>> getConfigUrls() {
		return configUrls;
	}
	
	public int getJumpAheadSecValue() {
		return jumpAheadSecValue;
	}
	
	public int getJumpBackSecValue() {
		return jumpBackSecValue;
	}
	
	public String getNebelTVHomepage() {
		return nebelTVHomepage;
	}

	public String getFrontendDownloadLink() {
		return frontendDownloadLink;
	}

	private void parseConfig() {
		try {
			configUrls = parseConfig(getConfigFileStream());
		} catch (IOException e) {
			D.e(e,false);
			FlurryAgent.onError(TAG, e.getMessage(), e);
		}
	}
	
	private InputStream getConfigFileStream() throws IOException {
		final String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			File configDirectory = new File(Environment.getExternalStorageDirectory(),CONFIG_FOLDER_NAME);
			File configFile = new File(configDirectory, getConfigFilename(true));
			if(configFile.exists()) {
				return new FileInputStream(configFile);
			} else {
				if(!state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
					configDirectory.mkdirs();
					saveConfigToExternalStorage(configFile);
				} else {
					UIUtils.showMessage(R.string.external_storage_read_only);
				}
				return getDefaultConfigFileStream();
			}
		} else {
        	UIUtils.showMessage(R.string.external_storage_unmounted);
			return getDefaultConfigFileStream();
		}
	}
	
	private InputStream getDefaultConfigFileStream() {
		try {
			return context.getAssets().open(getConfigFilename(false));
		}catch (IOException e) {
			D.e(e,false);
			FlurryAgent.onError(TAG, e.getMessage(), e);
			return null;
		}
		
	}
	
	private void saveConfigToExternalStorage(File configFile) {
		FileUtils.saveFileFromInputStream(getDefaultConfigFileStream(), configFile);     
	}
	
	/**
	 * 
	 * @param external -  true if config filename with config version requires; false otherwise
	 * @return config file name
	 */
	private String getConfigFilename(boolean withVersion) {
		return CONFIG_FILE_NAME + (withVersion?("_" + CONFIG_FILE_VERSION):"") + CONFIG_FILE_EXTENSION;
	}
	
	private HashMap<Mood, HashMap<TopView, String>> parseConfig(InputStream in) throws IOException {
        try {
        	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(in, null);
            return readConfig(parser);
        } catch(Exception e) {
        	D.e(e,false);
        	FlurryAgent.onError(TAG, FileUtils.convertStreamToString(in), e);
        	UIUtils.showMessage(R.string.invalid_extenal_config);
        	return parseConfig(getDefaultConfigFileStream());
        } finally {
            in.close();
        }
    }
	
	private HashMap<Mood, HashMap<TopView, String>> readConfig(XmlPullParser parser) 
													throws XmlPullParserException, IOException {
		
		HashMap<Mood, HashMap<TopView, String>> configMap = new HashMap<Mood, HashMap<TopView,String>>();
	    HashMap<TopView, String> topViewMap = new HashMap<TopView, String>();
	    Mood currentMood = null;
		TopView currentTopView = null;
		String name = null; 
		
		int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
        	if(eventType == XmlPullParser.START_TAG) {		
        		name = parser.getName();
        		if(MOOD_TAG.equals(name)) {        			
        			String moodType = parser.getAttributeValue(null, MOOD_NAME_ATTRIBUTE);
    	        	if(FAMILY_MOOD_TAG.equals(moodType)) {
    	        		currentMood = Mood.FAMILY;
    	        		
    	        	} else if(KIDS_MOOD_TAG.equals(moodType)) {
    	        		currentMood = Mood.KIDS;
    	        		
    	        	} else if(ROMANCE_MOOD_TAG.equals(moodType)) {
    	        		currentMood = Mood.ROMANCE;
    	        		
    	        	} else {
    	        		//invalid config file
    	        		throw new XmlPullParserException(INVALID_CONFIG_MSG);
    	        	}
        		} else if(FRIENDS_FEED_TAG.equals(name)) {
    	        	currentTopView = TopView.FRIENDS_FEED;
    	        	
        	    } else if(WHATS_CLOSE_TAG.equals(name)) {
        	        currentTopView = TopView.WHATS_CLOSE;
        	        
        	    } else if(RECENTLY_VIEWED_TAG.equals(name)) {
        	        currentTopView = TopView.RECENTLY_VIEWED;
        	        
        	    } else if(WHATS_HOT_TAG.equals(name)) {
        	        currentTopView = TopView.WHATS_HOT;
        	        
        	    } else if(PICTURES_TAG.equals(name)) {
        	        currentTopView = TopView.PICTURES;
        	        
        	    } else if(RECOMMENDED_TAG.equals(name)) {
        	        currentTopView = TopView.RECOMMENDED;
        	        
        	    }else if(VIDEO_OPTIONS_TAG.equals(name)) {
        	    	jumpAheadSecValue = Integer.valueOf(parser.getAttributeValue(null, JUMP_AHEAD_TAG));
        	    	jumpBackSecValue = Integer.valueOf(parser.getAttributeValue(null, JUMP_BACK_TAG));   
        	    	
        	    } else if(CONFIG_TAG.equals(name) || NEBEL_TV_HOMEPAGE_TAG.equals(name)
        	    		|| FRONTEND_DOWNLOAD_LINK_TAG.equals(name)) {
        	    	//do nothing
        	    	
        	    } else {
        	        	//invalid config file
        	        	throw new XmlPullParserException(INVALID_CONFIG_MSG);
        	    }
        	} else if(eventType == XmlPullParser.END_TAG) {
        		if(MOOD_TAG.equals(parser.getName())) {
        			if(currentMood!=null) {
        				configMap.put(currentMood, topViewMap);
        				currentMood = null;
        				topViewMap = new HashMap<TopView, String>();
        			}
        		}
        		name = null;
        		
        	} else if(eventType == XmlPullParser.TEXT) {
        		if(currentTopView!=null) {
        			topViewMap.put(currentTopView, parser.getText());
        			currentTopView = null;
        		} else if(NEBEL_TV_HOMEPAGE_TAG.equals(name)) {
        			nebelTVHomepage = parser.getText();
        		} else if(FRONTEND_DOWNLOAD_LINK_TAG.equals(name)) {
        			frontendDownloadLink = parser.getText();
        		}
        		
        	}
        	eventType = parser.next();
        }
        return configMap;
	}

}
