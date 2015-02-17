package org.xdty.smalltalk.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * Created by ty on 15-2-17.
 */
public class BaseActivity extends Activity {
    public static final String FINISH_FILTER = "org.xdty.smalltalk.FINISH_FILTER";

    private BroadcastReceiver receiver;

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
}
