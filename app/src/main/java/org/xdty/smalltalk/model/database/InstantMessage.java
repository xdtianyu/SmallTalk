package org.xdty.smalltalk.model.database;

import com.orm.SugarRecord;

import org.xdty.smalltalk.model.MessageType;

/**
 * Created by ty on 15-1-24.
 */
public class InstantMessage extends SugarRecord<InstantMessage> {
    public String mFrom;
    public String mTo;
    public String mBody;
    public long mTimestamp;
    public boolean mIsSent = false;
    public MessageType mType = MessageType.CHAT;
}
