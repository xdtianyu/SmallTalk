package org.xdty.smalltalk.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.xdty.smalltalk.R;
import org.xdty.smalltalk.activity.SmallTalkActivity;
import org.xdty.smalltalk.adapter.MessageAdapter;
import org.xdty.smalltalk.model.Error;
import org.xdty.smalltalk.model.InstantMessage;
import org.xdty.smalltalk.service.SmallTalkService;
import org.xdty.smalltalk.wrapper.XMPPWrapper;

import java.util.ArrayList;

/**
 * Created by ty on 15-2-16.
 */
public class MessageFragment extends Fragment implements 
        SwipeRefreshLayout.OnRefreshListener,
        SmallTalkService.MessageCallback{

    private SwipeRefreshLayout swipeRefreshLayout;
    
    private ListView listView;
    
    private MessageAdapter messageAdapter;
    
    private ArrayList<InstantMessage> messageList;
    
    private SmallTalkService smallTalkService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_layout, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);

        messageList = new ArrayList<>();

        messageAdapter = new MessageAdapter(getActivity(), 
                R.layout.message_left, R.layout.message_right, messageList);
        listView = (ListView) view.findViewById(R.id.message_list);
        listView.setAdapter(messageAdapter);
        
        (new MessageTask()).execute(MessageTask.REGISTER_CALLBACK);
        
        return view;
    }

    @Override
    public void onRefresh() {
        (new RefreshTask()).execute();
    }

    @Override
    public void onMessage(InstantMessage message) {
        messageList.add(message);
        messageAdapter.notifyDataSetChanged();
    }

    private class RefreshTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    
    private class MessageTask extends AsyncTask<String, Void, String> {
        
        public final static String REGISTER_CALLBACK = "register_callback";

        private final static int MAX_COUNT = 30;

        @Override
        protected String doInBackground(String... params) {

            int count = 0;
            while (smallTalkService==null && count <= MAX_COUNT) {
                smallTalkService = ((SmallTalkActivity)getActivity()).getService();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (count>=MAX_COUNT) {
                throw new RuntimeException(Error.BIND_SERVICE_TIMEOUT);
            }
            
            if (params[0].equalsIgnoreCase(REGISTER_CALLBACK)) {
                smallTalkService.setMessageCallback(MessageFragment.this);
            }
            
            return null;
        }
    }
}
