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

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

import com.nebel_tv.NebelTVApp;
import com.nebel_tv.R;
import com.nebel_tv.model.TopView;

public class ConfigHelper {
	
	private static final String CONFIG_FOLDER_NAME = "NebelTV";
	private static final String CONFIG_FILE_NAME = "config.xml"; 
	
    private static ConfigHelper instance;
	
	private HashMap<TopView, String> configUrls;
	private Context context;

    private ConfigHelper(Context context) {
    	this.context = context;
    	configUrls = new HashMap<TopView, String>();
    }

    public static synchronized ConfigHelper getInstance() {
        if (instance == null) {
        	instance = new ConfigHelper(NebelTVApp.getContext());
        }
        return instance;
    }
	
	public HashMap<TopView, String> getConfigUrls() {
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
	
	private HashMap<TopView, String> parseConfig(InputStream in) throws IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            parser.nextTag();
            return readConfig(parser);
        } catch(Exception e) {
        	e.printStackTrace();
        	UIUtils.showMessage(R.string.invalid_extenal_config);
        	return parseConfig(getDefaultConfigFileStream());
        } finally {
            in.close();
        }
    }
	
	private HashMap<TopView, String> readConfig(XmlPullParser parser) 
										throws XmlPullParserException, IOException {
	    HashMap<TopView, String> configMap = new HashMap<TopView, String>();

	    parser.require(XmlPullParser.START_TAG, null, "config");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        TopView topView = null;
	        if("friends_feed".equals(name)) {
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
	        } else {
	        	//invalid config file
	        	throw new XmlPullParserException("");
	        }
	        if(topView!=null) {
	        	configMap.put(topView, readText(parser));
	        }
	    }  
	    return configMap;
	}
	
	private String readText(XmlPullParser parser) 
										throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}

}
