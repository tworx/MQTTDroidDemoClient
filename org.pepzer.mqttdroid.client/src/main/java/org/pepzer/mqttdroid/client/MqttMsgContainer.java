/* Copyright © 2017 Giuseppe Zerbo. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.pepzer.mqttdroid.client;

public class MqttMsgContainer {
    private String topic;
    private int id;
    private byte[] payload;
    private boolean duplicated;
    private boolean retained;

    public MqttMsgContainer(String topic, int id, byte[] payload, boolean duplicated, boolean retained) {
        this.topic = topic;
        this.id = id;
        this.payload = payload;
        this.duplicated = duplicated;
        this.retained = retained;
    }

    public String getTopic() {
        return topic;
    }

    public int getId() {
        return id;
    }

    public byte[] getPayload() {
        return payload;
    }

    public boolean isDuplicated() {
        return duplicated;
    }

    public boolean isRetained() {
        return retained;
    }
}
