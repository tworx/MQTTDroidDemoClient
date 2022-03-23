/* Copyright © 2017 Giuseppe Zerbo. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.pepzer.mqttdroid.democlient;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.tworx.eud.mqttdroid.AuthState;
import com.tworx.eud.mqttdroid.ProxyState;

import org.pepzer.mqttdroid.client.MqttMsgContainer;
import org.pepzer.mqttdroid.client.MqttDroidClientActivity;
import org.pepzer.mqttdroid.client.MqttDroidUtils;

public class MainActivity extends MqttDroidClientActivity {

    private static final String TAG = "DemoMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        setPublishTopics(new String[] {"/demo/pub/#", "/demo/sub/+"});
        setSubscribeTopics(new String[] {"/demo/sub/#"});

        Button bind_auth = (Button) findViewById(R.id.bind_auth);
        bind_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBindAuthService();
            }
        });

        Button unbind_auth = (Button) findViewById(R.id.unbind_auth);
        unbind_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUnbindAuthService();
            }
        });

        Button bind_proxy = (Button) findViewById(R.id.bind_proxy);
        bind_proxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBindProxyService();
            }
        });


        Button unbind_proxy = (Button) findViewById(R.id.unbind_proxy);
        unbind_proxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUnbindProxyService();
            }
        });

        Button pub1 = (Button) findViewById(R.id.pub_1);
        pub1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "pub_1");
                publish(121, "/demo/pub/pub1", "hello".getBytes(), 0, false);
            }
        });

        Button pub2 = (Button) findViewById(R.id.pub_2);
        pub2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "pub_2");
                publish(122, "/demo/pub/pub2", "hello2".getBytes(), 0, false);
            }
        });

        Button sub1 = (Button) findViewById(R.id.sub_1);
        sub1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "sub_1");
                subscribe("/demo/sub/sub1", 2);
            }
        });

        Button sub2 = (Button) findViewById(R.id.sub_2);
        sub2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "sub_2");
                subscribe("/demo/sub/sub2/#", 0);
            }
        });

        Button pub3 = (Button) findViewById(R.id.pub_3);
        pub3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "pub_3");
                publish(123, "/demo/wrong/pub3", "hello3".getBytes(), 0, false);
            }
        });

        Button sub3 = (Button) findViewById(R.id.sub_3);
        sub3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "sub_3");
                subscribe("/demo/wrong/sub3", 0);
            }
        });

        Button pubSub1 = (Button) findViewById(R.id.pub_sub_1);
        pubSub1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "pub_sub_1");
                publish(124, "/demo/sub/sub1", "hello sub1".getBytes(), 0, false);
            }
        });

        Button pubSub2 = (Button) findViewById(R.id.pub_sub_2);
        pubSub2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "pub_sub_2");
                publish(125, "/demo/sub/sub2", "hello sub2".getBytes(), 0, false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.v(TAG, "action_settings");
            Toast.makeText(this, "Settings",
                    Toast.LENGTH_SHORT).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onMessageArrived(MqttMsgContainer msg) {
        Toast.makeText(this, "Id: " + msg.getId() +
                ", Msg: " + new String(msg.getPayload()) +
                ", Topic: " + msg.getTopic(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onProxyServiceConnected() {
        AuthState authState = getAuthState();
        String text;

        switch (authState) {
            case APP_ALLOWED:
                text = "App allowed!";
                break;
            case APP_REFUSED:
                text = "App not allowed!";
                break;
            case APP_UNKNOWN:
                text = "App unknown!";
                break;
            default:
                text = "Unknown error!";
                break;
        }
        Toast.makeText(this, text,
                Toast.LENGTH_SHORT).show();

        onProxyStateChange(getProxyState());
    }

    @Override
    protected void onProxyStateChange(ProxyState proxyState) {
        TextView proxyStateText = (TextView) findViewById(R.id.proxy_status);
        switch (proxyState) {
            case PROXY_CONNECTED:
                proxyStateText.setText(R.string.status_proxy_connected);
                proxyStateText.setBackgroundColor(getResources().getColor(R.color.colorConnected));
                break;
            case PROXY_DISCONNECTED:
                proxyStateText.setText(R.string.status_proxy_disconnected);
                proxyStateText.setBackgroundColor(getResources().getColor(R.color.colorDisconnected));
                break;
            case PROXY_STARTING:
                proxyStateText.setText(R.string.status_proxy_starting);
                proxyStateText.setBackgroundColor(getResources().getColor(R.color.colorBusy));
                break;
            case PROXY_STOPPING:
                proxyStateText.setText(R.string.status_proxy_stopping);
                proxyStateText.setBackgroundColor(getResources().getColor(R.color.colorBusy));
                break;
            case PROXY_STOPPED:
                proxyStateText.setText(R.string.status_proxy_stopped);
                proxyStateText.setBackgroundColor(getResources().getColor(R.color.colorStopped));
                break;
            default:
                break;
        }
    }
}
