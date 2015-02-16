package org.xdty.smalltalk.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.jivesoftware.smack.SmackAndroid;
import org.xdty.smalltalk.model.InstantMessage;
import org.xdty.smalltalk.wrapper.ConfigWrapper;
import org.xdty.smalltalk.wrapper.XMPPWrapper;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;

public class SmallTalkService extends Service implements 
        XMPPWrapper.Callback {
    
    public final static String TAG = "SmallTalkService";
    
    private final static int RECEIVED_MESSAGE_MSG = 0x01;
    
    private final SmallTalkServiceBinder mBinder = new SmallTalkServiceBinder();
    
    private MessageCallback messageCallback;
    
    private MessageHandler messageHandler = new MessageHandler(this);

    public SmallTalkService() {

        
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        SmackAndroid.init(this.getApplicationContext());
        ConfigWrapper.init(this);
        XMPPWrapper.Instance().setCallback(this);
        XMPPWrapper.Instance().connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory");
        super.onTrimMemory(level);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public void OnMessage(InstantMessage message) {
        Log.d(TAG, "OnMessage: " + message.body);
        Message.obtain(messageHandler, RECEIVED_MESSAGE_MSG, message).sendToTarget();
    }

    public class SmallTalkServiceBinder extends Binder {
        public SmallTalkService getService() {
            return SmallTalkService.this;
        }
    }
    
    public void isConnected() {

        
    }
    
    private Handler mServiceHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage");
            super.handleMessage(msg);
        }
    };
    
    
    private static class MessageHandler extends Handler {
        
        private WeakReference<SmallTalkService> mOuter;
        
        public MessageHandler(SmallTalkService service) {
            mOuter = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            
            SmallTalkService outer = mOuter.get();
            
            if (outer!=null) {
                switch (msg.what) {
                    case RECEIVED_MESSAGE_MSG:

                        if (outer.messageCallback!=null) {
                            outer.messageCallback.onMessage((InstantMessage)msg.obj);
                        }

                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    public interface MessageCallback {
        
        public void onMessage(InstantMessage message);
        
    }
    
    public void setMessageCallback(MessageCallback callback) {
        
        messageCallback = callback;
        
    }
     
}
