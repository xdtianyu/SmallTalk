package org.xdty.smalltalk.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.jivesoftware.smack.SmackAndroid;
import org.xdty.smalltalk.R;
import org.xdty.smalltalk.service.SmallTalkService;
import org.xdty.smalltalk.service.SmallTalkService.SmallTalkServiceBinder;
import org.xdty.smalltalk.wrapper.ConfigWrapper;


public class SmallTalkActivity extends ActionBarActivity {
    
    private SmallTalkService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmackAndroid.init(this.getApplicationContext());

        ConfigWrapper.init(this);
        Intent intent = new Intent(SmallTalkActivity.this, SmallTalkService.class);
        startService(intent);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(SmallTalkActivity.this, SmallTalkService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override 
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SmallTalkServiceBinder binder = (SmallTalkServiceBinder)service;
            mService = binder.getService();
            mService.connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
