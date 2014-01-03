package com.nebel_tv.frontend;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.nebel_tv.R;
import com.nebel_tv.utils.ConfigHelper;
import com.nebel_tv.utils.UIUtils;

public class FrontendUpdateTask extends AsyncTask<Void, Integer, String> {

    private Context context;
    private	ProgressDialog progressDialog;
    private File configDirectory;
    private String frontendDownloadLink;

    public FrontendUpdateTask(Context context) {
    	this.context = context;

	    progressDialog = new ProgressDialog(context);
	    progressDialog.setMessage(context.getString(R.string.frontend_update_msg));
	    progressDialog.setIndeterminate(true);
	    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    progressDialog.setCancelable(true);

	    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
	        @Override
	        public void onCancel(DialogInterface dialog) {
	            FrontendUpdateTask.this.cancel(true);
	        }
	    });
	    
	    ConfigHelper configHelper = ConfigHelper.getInstance();
	    configDirectory = configHelper.getConfigDirectory();
	    frontendDownloadLink = configHelper.getConfig().getFrontendDownloadLink();
    }

    @Override
    protected String doInBackground(Void... params) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
             getClass().getName());
        wl.acquire();

        String result = downloadFrontendPackage();
        if(result==null) {
        	result = unpackFrontendZip();
        	if(result==null) {
        		 try {
					ConfigHelper.getInstance().updateConfigFile();
				} catch (Exception e) {
					result = e.getMessage();
				}
        	}
        }
        
        wl.release();
        return result;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        progressDialog.dismiss();
        if (result != null) {
            UIUtils.showMessage(String.format(context.getString(R.string.frontend_update_error_msg), result));
        } else {
        	UIUtils.showMessage(R.string.frontend_update_success_msg);
        }
    }
    
    private String downloadFrontendPackage() {
    	InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
        	URL url = new URL(frontendDownloadLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                 return "Server returned HTTP " + connection.getResponseCode() 
                     + " " + connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(getFrontendPackageFile());

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                	if(input!=null) {
                		input.close();
                	}
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) {
                	publishProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }
            output.flush();
        } catch (Exception e) {
        	return e.getMessage();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) { }

            if (connection != null) {
            	connection.disconnect();
            }
        }
        return null;
    }
    
    private String unpackFrontendZip() {       
         InputStream is = null;
         ZipInputStream zis = null;
         File frontendPackageFile = getFrontendPackageFile();
         String topDirectoryName = "NebelTV-TeaserFrontend-master/";
         try {
        	 String filename;
             is = new FileInputStream(frontendPackageFile);
             zis = new ZipInputStream(new BufferedInputStream(is));          
             ZipEntry ze;
             byte[] buffer = new byte[1024];
             int count;
             
             while ((ze = zis.getNextEntry()) != null) {
                 filename = ze.getName();
                 //TODO this is temp solution to skip root folder
                 //should be implemented universal solution
                 if(filename.contains(topDirectoryName)) {
                	 filename = filename.substring(topDirectoryName.length());
                 }

                 if (ze.isDirectory()) {
                	 File fmd = new File(configDirectory, filename);
                     fmd.mkdirs();
                     continue;
                 }

                 FileOutputStream fout = new FileOutputStream(new File(configDirectory, filename));

                 while ((count = zis.read(buffer)) != -1) {
                     fout.write(buffer, 0, count);             
                 }

                 fout.close();               
                 zis.closeEntry();
             }
             frontendPackageFile.delete();

         } catch(IOException e) {
             return e.getMessage();
         } finally {
             try {
                 if (zis!=null) {
                	 zis.close();
                 }
             } catch (IOException ignored) { }
         }

        return null;
    }
    
    private File getFrontendPackageFile() {
    	String filename = Uri.parse(frontendDownloadLink).getLastPathSegment();
    	return new File(configDirectory, filename);
    }
}
