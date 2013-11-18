package com.nebel_tv.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.Environment;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;
import com.nebel_tv.model.Mood;
import com.nebel_tv.model.TopView;

public class ConfigHelper {
	
	private static final String CONFIG_FOLDER_NAME = "NebelTV";
	private static final String CONFIG_FILE_NAME = "config.xml"; 
	
    private static ConfigHelper instance;
	
	private HashMap<Mood, HashMap<TopView, String>> configUrls;
	private Context context;

    private ConfigHelper(Context context) {
    	this.context = context;
    	configUrls = new HashMap<Mood, HashMap<TopView, String>>();
    }

    public static synchronized ConfigHelper getInstance() {
        if (instance == null) {
        	instance = new ConfigHelper(NebelTVApp.getContext());
        }
        return instance;
    }
	
	public HashMap<Mood, HashMap<TopView, String>> getConfigUrls() {
	   try {
		   configUrls = parseConfig(getConfigFileStream());
	   } catch (IOException e) {
		   e.printStackTrace();
	   }
	   return configUrls;
	}
	
	private InputStream getConfigFileStream() throws IOException {
		final String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			File configDirectory = new File(Environment.getExternalStorageDirectory(),CONFIG_FOLDER_NAME);
			File configFile = new File(configDirectory,CONFIG_FILE_NAME);
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
			return context.getAssets().open(CONFIG_FILE_NAME);
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private void saveConfigToExternalStorage(File configFile) {
		InputStream in = null;
        OutputStream out = null;
        try {
            in = getDefaultConfigFileStream();
            out = new FileOutputStream(configFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
          } catch(IOException e) {
              e.printStackTrace();
          }       
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	private HashMap<Mood, HashMap<TopView, String>> parseConfig(InputStream in) throws IOException {
        try {
        	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(in, null);
            return readConfig(parser);
        } catch(Exception e) {
        	e.printStackTrace();
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
		TopView topView = null;
		int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
        	if(eventType == XmlPullParser.START_TAG) {
        		String name = parser.getName();
        		if("mood".equals(name)) {
        			String moodType = parser.getAttributeValue(null, "name");
    	        	if("family".equals(moodType)) {
    	        		currentMood = Mood.FAMILY;
    	        	} else if("kids".equals(moodType)) {
    	        		currentMood = Mood.KIDS;
    	        	} else if("romance".equals(moodType)) {
    	        		currentMood = Mood.ROMANCE;
    	        	} else {
    	        		//invalid config file
    	        		throw new XmlPullParserException("");
    	        	}
        		} else if("friends_feed".equals(name)) {
    	        	topView = TopView.FRIENDS_FEED;
        	    } else if("whats_close".equals(name)) {
        	        topView = TopView.WHATS_CLOSE;
        	    } else if("recently_viewed".equals(name)) {
        	        topView = TopView.RECENTLY_VIEWED;
        	    } else if("whats_hot".equals(name)) {
        	        topView = TopView.WHATS_HOT;
        	    } else if("pictures".equals(name)) {
        	        topView = TopView.PICTURES;
        	    } else if("recommended".equals(name)) {
        	        topView = TopView.RECOMMENDED;
        	    } else if("config".equals(name)) {
        	    	//do nothing
        	    } else {
        	        	//invalid config file
        	        	throw new XmlPullParserException("");
        	    }
        	} else if(eventType == XmlPullParser.END_TAG) {
        		if("mood".equals(parser.getName())) {
        			if(currentMood!=null) {
        				configMap.put(currentMood, topViewMap);
        				currentMood = null;
        				topViewMap = new HashMap<TopView, String>();
        			}
        		}
        	} else if(eventType == XmlPullParser.TEXT) {
        		if(topView!=null) {
        			topViewMap.put(topView, parser.getText());
        			topView = null;
        		}
        	}
        	eventType = parser.next();
        }
        return configMap;
	}

}
