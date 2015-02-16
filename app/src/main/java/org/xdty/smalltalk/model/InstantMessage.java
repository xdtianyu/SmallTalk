package org.xdty.smalltalk.model;

/**
 * Created by ty on 15-1-24.
 */
public class InstantMessage {
    public String from;
    public String to;
    public String body;
    public long timestamp;
    public long received_timestamp;
    public boolean isSent = false;
}
