/* Copyright © 2017 Giuseppe Zerbo. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.pepzer.mqttdroid.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.pepzer.mqttdroid.IMQTTDroidAuth;
import org.pepzer.mqttdroid.IMQTTDroidNet;
import org.pepzer.mqttdroid.IMQTTDroidNetCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MqttDroidClientActivity extends AppCompatActivity {

    private static final String TAG = "DroidClientActivity";
    IMQTTDroidAuth authService = null;
    private boolean authIsBound = false;
    IMQTTDroidNet proxyService = null;
    private boolean proxyIsBound = false;

    private String[] publishTopics = new String[0];
    private String[] subscribeTopics = new String[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: Override
        super.onCreate(savedInstanceState);
        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                "org.pepzer.mqttdroid.BIND_AUTH")
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add("org.pepzer.mqttdroid.BIND_AUTH");
        }

        if (ContextCompat.checkSelfPermission(this,
                "org.pepzer.mqttdroid.BIND_PROXY")
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add("org.pepzer.mqttdroid.BIND_PROXY");
        }

        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    0);
        }
    }

    /**
     * Unbind all connected services.
     */
    @Override
    protected void onPause() {
        super.onPause();
        doUnbindServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        doBindProxyService();
        Log.v(TAG, "onResume ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO: Override
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO: Override
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection authConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.
            authService = IMQTTDroidAuth.Stub.asInterface(service);

            Log.v(TAG, "onServiceConnected AUTH");
            mHandler.sendMessage(mHandler.obtainMessage(MSG_AUTH_CONNECTED));
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            authService = null;
            authIsBound = false;
            mHandler.sendMessage(mHandler.obtainMessage(MSG_AUTH_DISCONNECTED));
        }
    };

       private ServiceConnection proxyConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            proxyService = IMQTTDroidNet.Stub.asInterface(service);

            Log.v(TAG, "onServiceConnected NET");

            try {
                proxyService.registerNetCallback(proxyCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mHandler.sendMessage(mHandler.obtainMessage(MSG_PROXY_CONNECTED));
        }

        public void onServiceDisconnected(ComponentName className) {
            proxyService = null;
            proxyIsBound = false;
            mHandler.sendMessage(mHandler.obtainMessage(MSG_PROXY_DISCONNECTED));
        }
    };

    final private static int MSG_AUTH_CONNECTED = 1;
    final private static int MSG_AUTH_DISCONNECTED = 2;
    final private static int MSG_PROXY_CONNECTED = 3;
    final private static int MSG_PROXY_DISCONNECTED = 4;
    final private static int MSG_SUBSCRIBE_CB = 5;
    final private static int MSG_UNSUBSCRIBE_CB = 6;
    final private static int MSG_PUBLISH_CB = 7;
    final private static int MSG_PROXY_CHANGE = 8;
    final private static int MSG_ARRIVED = 9;

    private final IMQTTDroidNetCallback proxyCallback = new IMQTTDroidNetCallback.Stub() {
        public void subscribeCallback(String topic, boolean success) {
            MsgDelivery delivery = new MsgDelivery(topic, -1, success);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SUBSCRIBE_CB, delivery));
        }

        public void unsubscribeCallback(String topic, boolean success) {
            MsgDelivery delivery = new MsgDelivery(topic, -1, success);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_UNSUBSCRIBE_CB, delivery));
        }

        public void publishCallback(String topic, int msgId, boolean success) {
            MsgDelivery delivery = new MsgDelivery(topic, msgId, success);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_PUBLISH_CB, delivery));
        }

        public void proxyStateChanged(int proxyState) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_PROXY_CHANGE, proxyState));
        }

        public void msgArrived(String topic, int id, byte[] payload, boolean duplicated, boolean retained) {
            MqttMsgContainer msgContainer = new MqttMsgContainer(topic, id, payload, duplicated, retained);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_ARRIVED, msgContainer));
        }
    };

    private class MsgDelivery {
        private String topic;
        private int id;
        private boolean success;

        public MsgDelivery(String topic, int id, boolean success) {
            this.topic = topic;
            this.id = id;
            this.success = success;
        }

        public String getTopic() {
            return topic;
        }

        public int getId() {
            return id;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    protected void doBindAuthService() {
        if (ContextCompat.checkSelfPermission(this,
                "org.pepzer.mqttdroid.BIND_AUTH")
                == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent();
            i.setClassName("org.pepzer.mqttdroid", "org.pepzer.mqttdroid.AuthService");
            authIsBound = bindService(i, authConnection, Context.BIND_AUTO_CREATE);
        }
    }

    protected void doBindProxyService() {
        if (ContextCompat.checkSelfPermission(this,
                "org.pepzer.mqttdroid.BIND_PROXY")
                == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent();
            i.setClassName("org.pepzer.mqttdroid", "org.pepzer.mqttdroid.ProxyService");
            proxyIsBound = bindService(i, proxyConnection, Context.BIND_ABOVE_CLIENT);
        }
    }

    protected void doUnbindAuthService() {
        if (authIsBound) {
            // Detach our existing connection.
            unbindService(authConnection);
            authIsBound = false;
        }
    }

    protected void doUnbindProxyService() {
        if (proxyIsBound) {
            // Detach our existing connection.
            unbindService(proxyConnection);
            proxyIsBound = false;
        }
    }

    protected void doUnbindServices() {
        doUnbindAuthService();
        doUnbindProxyService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindServices();
    }

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTH_CONNECTED:
                    onAuthServiceConnected();
                    break;
                case MSG_AUTH_DISCONNECTED:
                    onAuthServiceDisconnected();
                    break;
                case MSG_PROXY_CONNECTED:
                    onProxyServiceConnected();
                    break;
                case MSG_PROXY_DISCONNECTED:
                    onProxyServiceDisconnected();
                    break;
                case MSG_SUBSCRIBE_CB:
                    MsgDelivery subDelivery = (MsgDelivery) msg.obj;
                    onSubscribeCallback(subDelivery.getTopic(), subDelivery.isSuccess());
                    break;
                case MSG_UNSUBSCRIBE_CB:
                    MsgDelivery unsubDelivery = (MsgDelivery) msg.obj;
                    onUnsubscribeCallback(unsubDelivery.getTopic(), unsubDelivery.isSuccess());
                    break;
                case MSG_PUBLISH_CB:
                    MsgDelivery msgDelivery = (MsgDelivery) msg.obj;
                    Log.v(TAG, "Delivery of msgId: " + msgDelivery.getId() +
                    ", Topic: " + msgDelivery.getTopic() + ", success: " +
                    msgDelivery.isSuccess());
                    onPublishCallback(msgDelivery.getTopic(), msgDelivery.getId(), msgDelivery.isSuccess());
                    break;
                case MSG_PROXY_CHANGE:
                    int proxyState = (int) msg.obj;
                    onProxyStateChange(proxyStateToEnum(proxyState));
                    break;
                case MSG_ARRIVED:
                    MqttMsgContainer msgContainer = (MqttMsgContainer) msg.obj;
                    Log.v(TAG, "New msg arrived, id: " + msgContainer.getId());
                    onMessageArrived(msgContainer);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };

    protected void setPublishTopics(String[] publishTopics) {
        this.publishTopics = publishTopics;
    }

    protected void setSubscribeTopics(String[] subscribeTopics) {
        this.subscribeTopics = subscribeTopics;
    }

    protected void sendAuthRequest(boolean update) {
        if (authIsBound && authService != null) {
            ArrayList<String> pubList = new ArrayList<>();
            for (int i = 0; i < publishTopics.length; ++i) {
                pubList.add(publishTopics[i]);
            }
            ArrayList<String> subList = new ArrayList<>();
            for (int i = 0; i < subscribeTopics.length; ++i) {
                subList.add(subscribeTopics[i]);
            }
            HashMap<String, ArrayList<String>> topics = new HashMap<>();
            topics.put(MqttDroidUtils.REQ_PUB, pubList);
            topics.put(MqttDroidUtils.REQ_SUB, subList);

            try {
                authService.authRequest(topics, update);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            doUnbindAuthService();
        }
    }

    protected void onAuthServiceConnected() {
        Log.v(TAG, "onAuthServiceConnected");
        sendAuthRequest(true);
        //TODO: Override
    }

    protected void onAuthServiceDisconnected() {
        Log.v(TAG, "onAuthServiceDisconnected");
        //TODO: Override
    }

    protected void onProxyServiceConnected() {
        Log.v(TAG, "onNetServiceConnected");
        //TODO: Override
    }

    protected void onProxyServiceDisconnected() {
        Log.v(TAG, "onNetServiceDisconnected");
        //TODO: Override
    }

    protected void onSubscribeCallback(String topic, boolean success) {
        //TODO: Override
    }

    protected void onUnsubscribeCallback(String topic, boolean success) {
        //TODO: Override
    }

    protected void onPublishCallback(String topic, int id, boolean success) {
        //TODO: Override
    }

    protected void onProxyStateChange(MqttDroidUtils.ProxyState proxyState) {
        //TODO: Override
    }

    protected void onMessageArrived(MqttMsgContainer msg) {
        //TODO: Override
    }

    public MqttDroidUtils.AuthState getAuthState() {
        int authState = -1;
        if (proxyIsBound && proxyService != null) {
            try {
                authState = proxyService.getAuthState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        MqttDroidUtils.AuthState authEnum;
        switch (authState) {
            case MqttDroidUtils.APP_ALLOWED:
                authEnum = MqttDroidUtils.AuthState.APP_ALLOWED;
                break;
            case MqttDroidUtils.APP_REFUSED:
                authEnum = MqttDroidUtils.AuthState.APP_REFUSED;
                break;
            case MqttDroidUtils.APP_UNKNOWN:
                authEnum = MqttDroidUtils.AuthState.APP_UNKNOWN;
                break;
            default:
                authEnum = MqttDroidUtils.AuthState.UNKNOWN;
                break;
        }
        return authEnum;
    }

    private MqttDroidUtils.ProxyState proxyStateToEnum(int proxyState) {
        MqttDroidUtils.ProxyState proxyEnum;
        switch (proxyState) {
            case MqttDroidUtils.PROXY_CONNECTED:
                proxyEnum = MqttDroidUtils.ProxyState.PROXY_CONNECTED;
                break;
            case MqttDroidUtils.PROXY_DISCONNECTED:
                proxyEnum = MqttDroidUtils.ProxyState.PROXY_DISCONNECTED;
                break;
            case MqttDroidUtils.PROXY_STARTING:
                proxyEnum = MqttDroidUtils.ProxyState.PROXY_STARTING;
                break;
            case MqttDroidUtils.PROXY_STOPPING:
                proxyEnum = MqttDroidUtils.ProxyState.PROXY_STOPPING;
                break;
            case MqttDroidUtils.PROXY_STOPPED:
                proxyEnum = MqttDroidUtils.ProxyState.PROXY_STOPPED;
                break;
            default:
                proxyEnum = MqttDroidUtils.ProxyState.UNKNOWN;
                break;
        }
        return proxyEnum;
    }

    public MqttDroidUtils.ProxyState getProxyState() {
        int proxyState = -1;
        if (proxyIsBound && proxyService != null) {
            try {
                proxyState = proxyService.getProxyState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return proxyStateToEnum(proxyState);
    }

    /**
     * Obtain a Map from topic to QoS for subscriptions that are active.
     * @return
     *   could be null if the proxy is unbound or the app is not allowed on the proxy.
     */
    public Map<String, Integer> getActiveSubscriptions() {
        Map<String, Integer> activeSubs = null;
        if (proxyIsBound && proxyService != null) {
            try {
                activeSubs = proxyService.getActiveSubscriptions();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return activeSubs;
    }

    public boolean subscribe(String topic, int qos) {
        boolean success = false;
        if (proxyIsBound && proxyService != null) {
            try {
                success = proxyService.subscribe(topic, qos);
                Log.i(TAG, "subscribe result: " + success);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return  success;
    }

    public boolean unsubscribe(String topic) {
        boolean success = false;
        if (proxyIsBound && proxyService != null) {
            try {
                success = proxyService.unsubscribe(topic);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return  success;
    }

    public boolean publish(int id, String topic, byte[] payload, int qos, boolean retained) {
        boolean success = false;
        if (proxyIsBound && proxyService != null) {
            try {
                success = proxyService.publish(id, topic, payload, qos, retained);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
}
