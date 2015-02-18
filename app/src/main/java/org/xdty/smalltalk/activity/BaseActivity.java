package org.xdty.smalltalk.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import org.xdty.smalltalk.service.SmallTalkService;

/**
 * Created by ty on 15-2-17.
 */
public class BaseActivity extends Activity {
    public static final String FINISH_FILTER = "org.xdty.smalltalk.FINISH_FILTER";

    private BroadcastReceiver receiver;

    protected SmallTalkService smallTalkService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_FILTER);
        
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context con, Intent intent) {
                if (intent.getAction().equals(FINISH_FILTER)) {
                    finish();
                }
            }
        };

        registerReceiver(receiver, filter);
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(BaseActivity.this, SmallTalkService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SmallTalkService.SmallTalkServiceBinder binder = (SmallTalkService.SmallTalkServiceBinder)service;
            smallTalkService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
