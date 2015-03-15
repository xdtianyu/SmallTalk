package org.xdty.smalltalk.wrapper;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.xdty.smalltalk.model.Config;
import org.xdty.smalltalk.model.database.InstantMessage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Created by ty on 15-1-17.
 */
public class XMPPWrapper implements Runnable{
    
    public final static String TAG = "XMPPWrapper";

    private static XMPPWrapper mInstance;
    
    private static String mServer;
    
    private static String mUser;
    
    private static String mPassword;

    private ConnectionConfiguration configuration;

    private XMPPTCPConnection connection;

    private final static int MAX_COUNT = 30;
    
    private final static int MESSAGE_CONNECT = 1;
    
    private Callback mCallback;

    private ArrayDeque<Integer> queue;
    
    private ArrayDeque<InstantMessage> messageQueue;
    
    private Thread thread;
    
    private HashMap<String, String> chatMap;
    
    private XMPPWrapper() {
        mServer = ConfigWrapper.Instance().getString(Config.SERVER_URI);
        mUser = ConfigWrapper.Instance().getString(Config.USERNAME);
        mPassword = ConfigWrapper.Instance().getString(Config.PASSWORD);

        configuration = new ConnectionConfiguration(mServer);
        configuration.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);

        connection = new XMPPTCPConnection(configuration);
        
        queue = new ArrayDeque<>();
        messageQueue = new ArrayDeque<>();

        chatMap = new HashMap<>();

        thread = new Thread(this);
        thread.start();
        
    }
    
    public static XMPPWrapper Instance() {
        if (mInstance==null) {
            mInstance = new XMPPWrapper();
        }
        return mInstance;
    }
    
    public void connect() {
        queue.addLast(QueueMessage.CONNECT);
    }
    
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            
            switch (msg.what) {
                case MESSAGE_CONNECT:
                    Log.d(TAG, "connected");
                    break;
            }
            
            super.handleMessage(msg);
        }
    };
    
    public void sendMessage(InstantMessage message) {
        messageQueue.add(message);
        queue.add(QueueMessage.SEND_MESSAGE);
        thread.interrupt();
    }

    public void run() {
        
        boolean exitFlag = false;
        
        int message;
        
        while (!exitFlag) {
            if (!queue.isEmpty()) {
                message = queue.pop();
                
                switch (message) {
                    case QueueMessage.CONNECT:
                        Log.d(TAG, "connecting...");

                        try {

                            connection.connect();

                            int sleepCount = 0;

                            while (!connection.isConnected() && sleepCount<MAX_COUNT) {
                                Thread.sleep(200);
                                sleepCount++;
                            }

                            connection.addConnectionListener(connectionListener);
                            connection.addPacketListener(packetListener, packetFilter);
                            ChatManager chatManager = ChatManager.getInstanceFor(connection);
                            chatManager.addChatListener(chatManagerListener);

                            connection.login(mUser, mPassword);

                            // TODO: chatManager need to check.
                        } catch (SmackException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (SaslException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }
                        break;
                    case QueueMessage.SEND_MESSAGE:
                        InstantMessage instantMessage = messageQueue.pop();
                        String uid = instantMessage.mTo;
                        Chat chat;
                        if (chatMap.containsKey(uid)) {
                            chat = ChatManager.getInstanceFor(connection).getThreadChat(chatMap.get(uid));
                        } else {
                            chat = ChatManager.getInstanceFor(connection).createChat(uid, messageListener);
                            chatMap.put(uid, chat.getThreadID());
                        }

                        try {
                            chat.sendMessage(instantMessage.mBody);
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case QueueMessage.EXIT_THREAD:
                        exitFlag = true;
                        break;
                        
                    default:
                        break;
                }
            } else {
                try {
                    //Log.d(TAG, "message queue is empty");
                    // may add interrupt later.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "thread interrupted");
                }
            }
        }
    }
    
    private ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            Log.d(TAG, "connected");
        }

        @Override
        public void authenticated(XMPPConnection connection) {
            Log.d(TAG, "authenticated");
        }

        @Override
        public void connectionClosed() {
            Log.d(TAG, "connectionClosed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.d(TAG, "connectionClosedOnError");
        }

        @Override
        public void reconnectingIn(int seconds) {
            Log.d(TAG, "reconnectingIn");
        }

        @Override
        public void reconnectionSuccessful() {
            Log.d(TAG, "reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.d(TAG, "reconnectionFailed");
        }
    };
    
    private PacketListener packetListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) throws SmackException.NotConnectedException {
            Log.d(TAG, "processPacket:" +packet.toString());
        }
    };
    
    private PacketFilter packetFilter = new PacketFilter() {
        @Override
        public boolean accept(Packet packet) {
            Log.d(TAG, "accept:" +packet.toString());
            return false;
        }
    };
    
    private ChatManagerListener chatManagerListener = new ChatManagerListener() {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
            Log.d(TAG, "chatCreated: " + chat.toString());
            chat.addMessageListener(messageListener);
        }
    };
    
    private MessageListener messageListener = new MessageListener() {
        @Override
        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
            Log.d(TAG, "processMessage: " + message.toString());
            
            InstantMessage msg = new InstantMessage();
            msg.mFrom = message.getFrom().substring(0, message.getFrom().lastIndexOf('/'));
            msg.mTo = message.getTo();
            msg.mBody = message.getBody();
            // may add send_timestamp later.
            msg.mTimestamp = System.currentTimeMillis();

            msg.save();
            
            if (mCallback!=null) {
                mCallback.OnMessage(msg);
            }
        }
    };
    
    
    private class QueueMessage {
        public final static int CONNECT = 0x01;
        public final static int SEND_MESSAGE = 0x02;
        public final static int EXIT_THREAD = 0x100;
        
        
    }
    
    public interface Callback {
        
        public void OnMessage(InstantMessage message);
        
    }
    
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

}
