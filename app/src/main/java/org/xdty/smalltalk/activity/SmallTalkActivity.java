package org.xdty.smalltalk.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.xdty.smalltalk.R;
import org.xdty.smalltalk.fragment.MessageFragment;
import org.xdty.smalltalk.service.SmallTalkService;
import org.xdty.smalltalk.service.SmallTalkService.SmallTalkServiceBinder;


public class SmallTalkActivity extends FragmentActivity {
    
    public final static String TAG = "SmallTalkActivity";
    
    private SmallTalkService smallTalkService;
    
    private ViewPager viewPager;
    
    private SmallTalkPageAdapter smallTalkPageAdapter;

    public static final String FINISH_FILTER = "org.xdty.smalltalk.FINISH_FILTER";

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start main service
        Intent intent = new Intent(SmallTalkActivity.this, SmallTalkService.class);
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_FILTER);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context con, Intent intent) {
                if (intent.getAction().equals(FINISH_FILTER)) {
                    stopService(new Intent(SmallTalkActivity.this, SmallTalkService.class));
                    finish();
                    (new ExitTask()).execute();
                }
            }
        };

        registerReceiver(receiver, filter);
        
        // bind fragment
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        smallTalkPageAdapter = new SmallTalkPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(smallTalkPageAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(SmallTalkActivity.this, SmallTalkService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    
    @Override 
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        
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
    
    public SmallTalkService getService() {
        
        return smallTalkService;
        
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SmallTalkServiceBinder binder = (SmallTalkServiceBinder)service;
            smallTalkService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    
    private static class SmallTalkPageAdapter extends FragmentPagerAdapter {
        
        private final static int PAGE_COUNT = 1;


        public SmallTalkPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            
            switch (position) {
                case 0:
                    fragment = new MessageFragment();
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
    
    private class ExitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            System.exit(0);
        }
    }
}
