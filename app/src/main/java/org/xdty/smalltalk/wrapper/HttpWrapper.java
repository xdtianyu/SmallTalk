package org.xdty.smalltalk.wrapper;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xdty.smalltalk.model.Config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ty on 15-1-24.
 */

// TODO: make http request all single thread.

public class HttpWrapper implements Runnable {
    
    public final static String TAG = "HttpWrapper";
    
    private static HttpWrapper mInstance;
    
    private ArrayDeque<HttpUri> queue;
    
    Thread thread;
    
    private HttpWrapper() {
        queue = new ArrayDeque<>();
        thread = new Thread(this);
        thread.start();
    }
    
    public static HttpWrapper Instance() {
        if (mInstance == null) {
            mInstance = new HttpWrapper();
        }
        
        return mInstance;
    }
    
    public void reportCrash(String message) {
        
        HttpUri uri = new HttpUri();
        uri.url = Config.CRASH_REPORTER_URI;

        uri.params.add(new BasicNameValuePair("event", "crashed"));
        uri.params.add(new BasicNameValuePair("name", "SmallTalk"));
        uri.params.add(new BasicNameValuePair("email", Config.CRASH_RECEIVER));
        uri.params.add(new BasicNameValuePair("extra", message));

        queue.addLast(uri);
        thread.interrupt();
    }

    @Override
    public void run() {
        while (true) {
            if (queue.size()>0) {
                
                HttpUri uri = queue.pop();
                
                try {
                    URL url = new URL(uri.url);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept-Encoding", "gzip, deflate");

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getQuery(uri.params));
                    writer.flush();
                    writer.close();
                    os.close();

                    conn.connect();

                    int response = conn.getResponseCode();
                    
                    switch (response) {
                        case HttpURLConnection.HTTP_OK:
                            Log.d(TAG, "http response ok");
                            break;
                        default:
                            break;
                    }

                    conn.disconnect();
                    
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.d(TAG, "thread interrupted.");
                }
            }
        }
    }

    private class HttpUri {
        public String url;
        public List<NameValuePair> params = new ArrayList<>();
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    
}
