/* Copyright © 2017 Giuseppe Zerbo. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.pepzer.mqttdroid.client;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.tworx.eud.mqttdroid.IMQTTReceiver;

public class MqttProxyReceiverService extends Service {

    private static final String TAG = "ProxyReceiverService";
    private static final String proxyNamespace = "org.pepzer.mqttdroid";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the interface
        Log.v(TAG, "onBind");

        return mBinder;
    }

    private boolean callerAllowed() {
        PackageManager pm = getApplicationContext().getPackageManager();
        String caller = MqttDroidUtils.getCallerPackage(pm);
        if (proxyNamespace.equals(caller)) {
            return true;
        }
        Log.w(TAG, "Package " + caller + " is not allowed!");
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Log.v(TAG, "onStartCommand()");
        return Service.START_NOT_STICKY;
    }

    private final IMQTTReceiver.Stub mBinder = new IMQTTReceiver.Stub() {
        public void deliveryResult(String topic, int id, boolean success) {
            if (callerAllowed()) {
                Log.v(TAG, "deliveryResult, id: "+ id);
                msgHandler.sendMessage(msgHandler.obtainMessage(MSG_DELIVERY_RESULT, new Receipt(topic, id, success)));
            }
        }

        public void msgArrived(String topic, int id, byte[] payload, boolean duplicated, boolean retained) {
            if (callerAllowed()) {
                Log.v(TAG, "msgArrived, topic: " + topic);
                MqttMsgContainer msgContainer = new MqttMsgContainer(topic, id, payload, duplicated, retained);
                msgHandler.sendMessage(msgHandler.obtainMessage(MSG_ARRIVED, msgContainer));
            }
        }

    };

    private static final int MSG_ARRIVED = 1;
    private static final int MSG_DELIVERY_RESULT = 2;

    protected void onMessageArrived(MqttMsgContainer msg) {
        // Empty
    }

    protected void onDeliveryResult(String topic, int msgId, boolean success) {
        Log.v(TAG, "onDeliveryResult, id: "+ msgId);
        // Empty
    }

    private class Receipt {
        private String topic;
        private int msgId;
        private boolean success;

        public Receipt(String topic, int msgId, boolean success) {
            this.topic = topic;
            this.msgId = msgId;
            this.success = success;
        }

        public String getTopic() {
            return topic;
        }

        public int getMsgId() {
            return msgId;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    private Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ARRIVED:
                    MqttMsgContainer msgContainer = (MqttMsgContainer) msg.obj;
                    onMessageArrived(msgContainer);
                    break;
                case MSG_DELIVERY_RESULT:
                    Receipt receipt = (Receipt) msg.obj;
                    onDeliveryResult(receipt.getTopic(), receipt.getMsgId(), receipt.isSuccess());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };
}
