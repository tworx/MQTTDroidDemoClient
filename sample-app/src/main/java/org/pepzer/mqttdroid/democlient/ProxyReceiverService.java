/* Copyright © 2017 Giuseppe Zerbo. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.pepzer.mqttdroid.democlient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import org.pepzer.mqttdroid.client.MqttMsgContainer;
import org.pepzer.mqttdroid.client.MqttProxyReceiverService;

public class ProxyReceiverService extends MqttProxyReceiverService {

    private static final String TAG = "ProxyReceiverService";

    @Override
    protected void onMessageArrived(MqttMsgContainer msg) {
        Log.v(TAG, "onMessageArrived, Topic: " + msg.getTopic() + ", msg:" + new String(msg.getPayload()));

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification n = new Notification.Builder(this)
                .setContentTitle("MQTTDroidDemoClient")
                .setContentText("Topic: " + msg.getTopic() + ", msg:" + new String(msg.getPayload()))
                .setSmallIcon(R.mipmap.logo_nobg)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(15537, n);
    }

}
