package com.nebel_tv.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import com.flurry.android.FlurryAgent;
import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;
import com.nebel_tv.model.Mood;
import com.nebel_tv.model.TopView;

public class ConfigHelper {
	
	public interface OnConfigUpdatedListener {
		void onConfigUpdated();
	}
	
	//config file version should be changed after every structural change in config file
	private static final String CONFIG_FILE_VERSION = "1.1";
	
	public static final String TAG = ConfigHelper.class.getName();
	public static final String CONFIG_FOLDER_NAME = "NebelTV";
	private static final String CONFIG_FILE_NAME = "config";
	private static final String FRONTEND_CONFIG_FILE_NAME="frontend_config";
	private static final String CONFIG_FILE_EXTENSION = ".xml";
	private static final String CONFIG_FILE_ENCODING = "UTF-8";
	private static final String LOCAL_FILE_SCHEME = "file://";
	private static final String FRONT_END_FOLDER = "/html";
	
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
    
	private Context context;
	private ConfigModel configModel;
	private ArrayList<OnConfigUpdatedListener> listeners;

    private ConfigHelper(Context context) {
    	this.context = context;
    	configModel = new ConfigModel();
    	listeners = new ArrayList<OnConfigUpdatedListener>();
    }

    public static synchronized ConfigHelper getInstance() {
        if (instance == null) {
        	instance = new ConfigHelper(NebelTVApp.getContext());
        }
        return instance;
    }
    
    public synchronized ConfigModel getConfig() {
        //parse config every time we request config model
        //so the values will be surely up to date
        parseConfig();      
    	return configModel;
    }
    
    public synchronized void registerOnConfigUpdatedListener(OnConfigUpdatedListener listener) {
    	if(!listeners.contains(listener)) {
    		listeners.add(listener);
    	}
    }
    
    public synchronized void unregisterOnConfigUpdatedListener(OnConfigUpdatedListener listener) {
    	listeners.remove(listener);
    }
    
    private synchronized void notifyConfigUpdated() {
    	for(OnConfigUpdatedListener listener : listeners) {
    		if(listener!=null) {
    			listener.onConfigUpdated();
    		}
    	}
    }
    
    public synchronized void updateConfigFile() throws IOException, URISyntaxException {
    	
    	//parse frontend config and delete on completion
    	File frontEndConfigFile = getFrontEndConfigFile();
    	parseConfig(new FileInputStream(frontEndConfigFile));
    	frontEndConfigFile.delete();
    	
    	
    	//build merge of local config file and frontend config file
    	HashMap<Mood, HashMap<TopView, String>> configUrls = configModel.configUrls;
		File configDirectory = getConfigDirectory();
		URI configDirectoryUri = new URI(LOCAL_FILE_SCHEME+configDirectory.getAbsolutePath());
		
    	XmlSerializer xmlSerializer = Xml.newSerializer();
    	OutputStream os = getConfigFileOutputStream();
    	 
        xmlSerializer.setOutput(os, CONFIG_FILE_ENCODING);
        xmlSerializer.startDocument(CONFIG_FILE_ENCODING, true);
        xmlSerializer.startTag("", CONFIG_TAG);
        	
        for (Map.Entry<Mood, HashMap<TopView, String>> entry : configUrls.entrySet()) {        	
        	xmlSerializer.startTag("", MOOD_TAG);
        	xmlSerializer.attribute("", MOOD_NAME_ATTRIBUTE, getMoodTag(entry.getKey()));
        	
        	HashMap<TopView, String> moodUrls = entry.getValue();
        	for(Map.Entry<TopView, String> moodUrlEntry : moodUrls.entrySet()) {
        		
            	//update uri with full path to external storage
            	URI uri = new URI(moodUrlEntry.getValue());
    			
            	String path = configDirectoryUri.getPath()+FRONT_END_FOLDER+uri.getPath();
    			URI updatedUri = new URI(
    					configDirectoryUri.getScheme(), uri.getUserInfo(), configDirectoryUri.getHost(),
    					uri.getPort(), path, uri.getQuery(), uri.getFragment());
    			moodUrlEntry.setValue(updatedUri.toString());
        		
    			//write to xml
        		String topViewTag =  getTopViewTag(moodUrlEntry.getKey());
        		xmlSerializer.startTag("", topViewTag);
        		xmlSerializer.text(moodUrlEntry.getValue());
        		xmlSerializer.endTag("", topViewTag);
        	}
        	
        	xmlSerializer.endTag("", MOOD_TAG);
        }
        
        xmlSerializer.startTag("", VIDEO_OPTIONS_TAG);
        xmlSerializer.attribute("", JUMP_AHEAD_TAG, String.valueOf(configModel.jumpAheadSecValue));
        xmlSerializer.attribute("", JUMP_BACK_TAG, String.valueOf(configModel.jumpBackSecValue));
        xmlSerializer.endTag("", VIDEO_OPTIONS_TAG);
        
        xmlSerializer.startTag("", NEBEL_TV_HOMEPAGE_TAG);
        xmlSerializer.text(configModel.nebelTVHomepage);
        xmlSerializer.endTag("", NEBEL_TV_HOMEPAGE_TAG);
        
        xmlSerializer.startTag("", FRONTEND_DOWNLOAD_LINK_TAG);
        xmlSerializer.text(configModel.frontendDownloadLink);
        xmlSerializer.endTag("", FRONTEND_DOWNLOAD_LINK_TAG);

        xmlSerializer.endTag("", CONFIG_TAG);
        xmlSerializer.endDocument();
        
        xmlSerializer.flush();
        os.close();
        
        notifyConfigUpdated();
    }
    
    public File getConfigDirectory() {
    	return new File(Environment.getExternalStorageDirectory(),CONFIG_FOLDER_NAME);
    }

	private void parseConfig() {
		try {
			parseConfig(getConfigFileStream());
		} catch (IOException e) {
			D.e(e,false);
			FlurryAgent.onError(TAG, e.getMessage(), e);
		}
	}
	
	private InputStream getConfigFileStream() throws IOException {
		File configFile = getConfigFile();
		if(configFile!=null) {
			return new FileInputStream(configFile);
		} else {
			return getDefaultConfigFileStream();
		}
	}
	
	private File getFrontEndConfigFile() {
		File configDirectory = getConfigDirectory();
		return new File(configDirectory, FRONTEND_CONFIG_FILE_NAME+CONFIG_FILE_EXTENSION);
	}
	
	private OutputStream getConfigFileOutputStream() throws IOException {
		File configFile = getConfigFile();
		if(configFile!=null) {
			return new FileOutputStream(configFile);
		} else {
			return null;
		}
	}
	
	private File getConfigFile() {
		final String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			File configDirectory = getConfigDirectory();
			File configFile = new File(configDirectory, getConfigFilename(true));
			if(configFile.exists()) {
				return configFile;
			} else {
				if(!state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
					configDirectory.mkdirs();
					saveConfigToExternalStorage(configFile);
				} else {
					UIUtils.showMessage(R.string.external_storage_read_only);
				}
				return null;
			}
		} else {
        	UIUtils.showMessage(R.string.external_storage_unmounted);
        	return null;
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
	
	private void parseConfig(InputStream in){
        try {
        	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(in, null);
            readConfig(parser);
        } catch(Exception e) {
        	D.e(e,false);
        	FlurryAgent.onError(TAG, FileUtils.convertStreamToString(in), e);
        	UIUtils.showMessage(R.string.invalid_extenal_config);
        	parseConfig(getDefaultConfigFileStream());
        } finally {
        	try {
	        	if(in!=null) {
	        		in.close();
	        	}
        	} catch(IOException ignored) {}
        }
    }
	
	private void readConfig(XmlPullParser parser) throws XmlPullParserException, IOException {
		
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
        	    	configModel.jumpAheadSecValue = Integer.valueOf(parser.getAttributeValue(null, JUMP_AHEAD_TAG));
        	    	configModel.jumpBackSecValue = Integer.valueOf(parser.getAttributeValue(null, JUMP_BACK_TAG));   
        	    	
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
        			configModel.nebelTVHomepage = parser.getText();
        		} else if(FRONTEND_DOWNLOAD_LINK_TAG.equals(name)) {
        			configModel.frontendDownloadLink = parser.getText();
        		}
        		
        	}
        	eventType = parser.next();
        }
        configModel.configUrls = configMap;
	}
    
    private String getMoodTag(Mood mood) {
    	if(mood==null) {
    		return null;
    	}
    	switch (mood) {
		case FAMILY:
			return FAMILY_MOOD_TAG;
		case KIDS:
			return KIDS_MOOD_TAG;
		case ROMANCE:
			return ROMANCE_MOOD_TAG;
		default:
			return null;
		}
    }
    
    private String getTopViewTag(TopView topView) {
    	if(topView==null) {
    		return null;
    	}
    	switch (topView) {
		case FRIENDS_FEED:
			return FRIENDS_FEED_TAG;
		case WHATS_CLOSE:
			return WHATS_CLOSE_TAG;
		case RECENTLY_VIEWED:
			return RECENTLY_VIEWED_TAG;
		case WHATS_HOT:
			return WHATS_HOT_TAG;
		case PICTURES:
			return PICTURES_TAG;
		case RECOMMENDED:
			return RECOMMENDED_TAG;
		default:
			return null;
		}
    }
	
	public class ConfigModel {
		
		private HashMap<Mood, HashMap<TopView, String>> configUrls;
		private int jumpAheadSecValue; 
		private int jumpBackSecValue;
		private String nebelTVHomepage;
		private String frontendDownloadLink;
		
		public ConfigModel() {
	    	configUrls = new HashMap<Mood, HashMap<TopView, String>>();
	    	jumpAheadSecValue = 0;
	    	jumpBackSecValue = 0;
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
	}

}
