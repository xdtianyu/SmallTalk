package org.xdty.smalltalk.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.xdty.smalltalk.R;
import org.xdty.smalltalk.activity.SmallTalkActivity;
import org.xdty.smalltalk.adapter.MessageAdapter;
import org.xdty.smalltalk.model.Error;
import org.xdty.smalltalk.model.InstantMessage;
import org.xdty.smalltalk.service.SmallTalkService;

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
    
    private EditText messageText;
    
    private Button sendButton;
    

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
        
        messageText = (EditText) view.findViewById(R.id.message_body);
        sendButton = (Button) view.findViewById(R.id.send_button);
        
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new MessageTask()).execute(MessageTask.SEND_MESSAGE);
                throw new RuntimeException("test");
            }
        });
        
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
    
    private class MessageTask extends AsyncTask<Integer, Void, Integer> {
        
        public final static int REGISTER_CALLBACK = 0x01;
        
        public final static int SEND_MESSAGE = 0x02;
        
        private final static int EMPTY_MESSAGE = 0x03;

        private final static int MAX_COUNT = 30;

        @Override
        protected Integer doInBackground(Integer... params) {

            int result = 0;
            
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

            result = params[0];
            
            switch (params[0]) {
                case REGISTER_CALLBACK:
                    if (smallTalkService!=null)
                        smallTalkService.setMessageCallback(MessageFragment.this);
                    break;
                case SEND_MESSAGE:
                    // TODO: build and send xmpp message.
                    String message = messageText.getText().toString();
                    if (message.isEmpty()) {
                        result = EMPTY_MESSAGE;
                    } else {
                        if (smallTalkService!=null) {
                            smallTalkService.sendMessage(message);
                            InstantMessage instantMessage = new InstantMessage();
                            instantMessage.body = message;
                            instantMessage.from = smallTalkService.getUser();
                            instantMessage.timestamp = System.currentTimeMillis();
                            instantMessage.isSent = true;
                            messageList.add(instantMessage);
                        }
                    }
                    break;
                default:
                    break;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case EMPTY_MESSAGE:
                    Toast.makeText(getActivity(), R.string.empty_message,Toast.LENGTH_SHORT).show();
                    break;
                case SEND_MESSAGE:
                    messageText.setText("");
                    messageAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }
}
