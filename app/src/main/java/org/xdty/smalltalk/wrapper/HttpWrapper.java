package org.xdty.smalltalk.wrapper;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.xdty.smalltalk.model.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ty on 15-1-24.
 */

// TODO: make http request all single thread.

public class HttpWrapper implements Runnable {
    
    public final static String TAG = "HttpWrapper";

    private final static int REPORT_CRASH_MSG = 0x01;
    
    private final static int REPORT_CRASH_SUCCEED_MSG = 0x02;
    
    private final static int LOGIN_MSG = 0x03;
    
    private final static int LOGIN_SUCCEED_MSG = 0x04;
    
    private final static int LOGIN_FAILED_MSG = 0x05;
    
    private static HttpWrapper mInstance;
    
    private ArrayDeque<HttpUri> queue;

    private ReportCrashCallback reportCrashCallback;
    private LoginCallback loginCallback;

    private HttpHandler httpHandler;
    
    Thread thread;
    
    private HttpWrapper() {
        queue = new ArrayDeque<>();
        thread = new Thread(this);
        thread.start();
        httpHandler = new HttpHandler(this);
    }
    
    public static HttpWrapper Instance() {
        if (mInstance == null) {
            mInstance = new HttpWrapper();
        }
        
        return mInstance;
    }
    
    public void reportCrash(String message) {
        
        HttpUri uri = new HttpUri();
        uri.type = REPORT_CRASH_MSG;
        uri.url = Config.CRASH_REPORTER_URI;

        uri.params.add(new AbstractMap.SimpleEntry<>("event", "crashed"));
        uri.params.add(new AbstractMap.SimpleEntry<>("name", "SmallTalk"));
        uri.params.add(new AbstractMap.SimpleEntry<>("email", Config.CRASH_RECEIVER));
        uri.params.add(new AbstractMap.SimpleEntry<>("extra", message));

        queue.addLast(uri);
        thread.interrupt();
    }
    
    public void login(String username, String password) {

        HttpUri uri = new HttpUri();
        uri.type = LOGIN_MSG;
        uri.url = Config.LOGIN_URI;

        uri.params.add(new AbstractMap.SimpleEntry<>("username", username));
        uri.params.add(new AbstractMap.SimpleEntry<>("password", password));

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

                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                stringBuilder.append(line+"\n");
                            }
                            br.close();
                            
                            String json = stringBuilder.toString();
                            
                            Log.d(TAG, json);
                            
                            switch (uri.type) {
                                case REPORT_CRASH_MSG:
                                    Message.obtain(httpHandler, REPORT_CRASH_SUCCEED_MSG).sendToTarget();
                                    break;
                                case LOGIN_MSG:
                                    // TODO: parse result.

                                    JSONObject jsonObject = new JSONObject(json);
                                    String result = jsonObject.getString("result");
                                    String message = jsonObject.getString("message");
                                    
                                    if (result.equalsIgnoreCase("succeed")) {
                                        Message.obtain(httpHandler, LOGIN_SUCCEED_MSG, message).sendToTarget();
                                    } else if (result.equalsIgnoreCase("failed")){
                                        Message.obtain(httpHandler, LOGIN_FAILED_MSG, message).sendToTarget();
                                    } else {
                                        Log.e(TAG, "error result");
                                    }

                                    break;
                                default:
                                    break;
                            }
                            
                            break;
                        default:
                            break;
                    }

                    conn.disconnect();
                    
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
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
        public int type;
        public String url;
        public List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
    }

    private String getQuery(List<AbstractMap.SimpleEntry<String, String>> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    
    public interface ReportCrashCallback {
        public void onReportSucceed();
    }
    
    public void setReportCrashCallback(ReportCrashCallback callback) {
        reportCrashCallback = callback;
    }
    
    public interface LoginCallback {
        
        public void onLoginSucceed(String message);
        public void onLoginFailed(String message);
        
    }
    
    public void setLoginCallback(LoginCallback callback) {
        loginCallback = callback;
    }
    
    private static class HttpHandler extends Handler {

        private WeakReference<HttpWrapper> mOuter;

        public HttpHandler(HttpWrapper httpWrapper) {
            mOuter = new WeakReference<>(httpWrapper);
        }

        @Override
        public void handleMessage(Message msg) {

            HttpWrapper outer = mOuter.get();

            if (outer!=null) {
                switch (msg.what) {
                    case REPORT_CRASH_SUCCEED_MSG:
                        if (outer.reportCrashCallback!=null) {
                            outer.reportCrashCallback.onReportSucceed();
                        }
                        break;
                    case LOGIN_SUCCEED_MSG:
                        if (outer.loginCallback!=null) {
                            outer.loginCallback.onLoginSucceed((String)msg.obj);
                        }
                        break;
                    case LOGIN_FAILED_MSG:
                        if (outer.loginCallback!=null) {
                            outer.loginCallback.onLoginFailed((String)msg.obj);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
}
