package org.xdty.smalltalk.wrapper;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * Created by ty on 15-1-17.
 */
public class XMPPWrapper implements Runnable {

    private static XMPPWrapper mInstance;

    ConnectionConfiguration configuration;

    XMPPTCPConnection connection;
    
    private XMPPWrapper() {
        
    }
    
    public static XMPPWrapper Instance() {
        if (mInstance==null) {
            mInstance = new XMPPWrapper();
        }
        return mInstance;
    }

    @Override
    public void run() {

    }
}
