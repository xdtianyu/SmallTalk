package org.xdty.smalltalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.xdty.smalltalk.R;
import org.xdty.smalltalk.model.InstantMessage;

import java.util.List;

/**
 * Created by ty on 15-2-16.
 */
public class MessageAdapter extends ArrayAdapter<InstantMessage> {
    
    private Context context;
    private int resource_left;
    private int resource_right;
    private List<InstantMessage> messageList;
    
    private LayoutInflater mInflater;
    
    public MessageAdapter(Context context, int resource_left, int resource_right, List<InstantMessage> messageList) {
        super(context, resource_left, resource_right, messageList);
        this.context = context;
        this.resource_left = resource_left;
        this.resource_right = resource_right;
        this.messageList = messageList;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public InstantMessage getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        InstantMessage message = messageList.get(position);

        TextView senderText;
        TextView dateText;
        TextView messageText;
        
        if (message.isSent) {
            convertView = mInflater.inflate(resource_right, null);
        } else {
            convertView = mInflater.inflate(resource_left, null);
        }
        
        senderText = (TextView) convertView.findViewById(R.id.text_sender);
        dateText = (TextView) convertView.findViewById(R.id.text_date);
        messageText = (TextView) convertView.findViewById(R.id.text_message);

        senderText.setText(message.from);
        messageText.setText(message.body);
        dateText.setText(Long.toString(message.timestamp));
        
        return convertView;
    }

}
