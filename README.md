## What is MQTTDroidDemoClient?
 
This Android® application requires [MQTTDroid](https://github.com/pepzer/MQTTDroid) to work and is usefult as an example of a client for MQTTDroid and to test that it works.  

## Building MQTTDroidDemoClient

To test MQTTDroidDemoClient you need to clone this repository and compile it from sources.  

## Using the sample app

MQTTDroid has to be installed in order to use this application, and should be properly configured to connect to an MQTT broker.  
To verify that the connection with the server is working it might be of help to run the broker in foreground with verbose output enabled.  

The sample app consists of a single activity with several buttons, the first thing to do is to click on 'BIND AUTH', which triggers the transmission of an authorization request to MQTTDroid.  
At this point the client should be listed inside MQTTDroid and could be allowed through the switch on the right of the app name.  
Coming back to MQTTDroidDemoClient it should automatically bind to the proxy service, otherwise the button 'BIND PROXY' could be clicked, a toast should
appear notifying that the app is now allowed to transmit.

By clicking on one of the two 'PUB ..\*' buttons of the third row a publish is sent to MQTTDroid and, if the client is allowed, the message should be forwarded to the broker.  
The two 'SUB ..\*' buttons send a subscribe to MQTTDroid, the subscribed topics are different and the corresponding 'PUB ..*' buttons right below publish to those topics.  
After clicking on a subscribe button, the publish button below in the same column will send a message that should go to the broker and back to the client and finally printed on screed through a toast.

The two red buttons are respectively an attempt to publish and subscribe to topics that are not present among those authorized, MQTTDroid should ignore the requests.

## Structure

This repository contains two main modules:

- a client library for MQTTDroid under 'org.pepzer.mqttdroid.client',
- a demo client that uses and extends the library under 'sample-app'.

A client for MQTTDroid should contain at least three elements:

- an Activity/Service that could bind to the Authorization Service of MQTTDroid to require permission to publish/subscribe,
- an Activity/Service that could bind to the Proxy Service of MQTTDroid to invoke its API (publish, subscribe, unsubscribe, etc),
- a ProxyReceiverService that implements the interface IMQTTReceiver and allows the client to receive messages from MQTTDroid even when stopped.

## Creating a Client

To make a client for MQTTDroid create an Activity that extends MqttDroidClientActivity from the client package.  
The base Activity implements the necessary interfaces, asks the necessary permissions to the user and offers handlers for all events that could be overridden.  
The first thing to do is to invoke 'setPublishTopics()' and 'setSubscribeTopics()' to specify the fields of the authorization request.  
A call to 'doBindAuthService()' will bind to MQTTDroid Auth service and trigger the transmission of the request, unless 'onAuthServiceConnected()' is overridden to avoid it.  
All MQTTDroid API endpoints are exposed through methods inside MqttDroidClientActivity, like publish, subscribe, unsubscribe, etc.  
All events and callback invocations are exposed through methods that could be overridden, like 'onProxyServiceConnected()', 'onSubscribeCallback()', 'onMessageArrived()'.  
For a complete list refer to [MqttDroidClientActivity.java](./org.pepzer.mqttdroid.client/src/main/java/org/pepzer/mqttdroid/client/MqttDroidClientActivity.java)

Extending the MQTTProxyReceiverService the application will receive messages even when the activity is not bound to the proxy.  
Because for security reasons MQTTDroid binds to the service explicitly, the name of the extending service must be ProxyReceiverService.  
In the manifest the service must be protected by the permission org.pepzer.mqttdroid.BIND_RCV, for other details refer to [AndroidManifest.xml](./sample-app/src/main/AndroidManifest.xml).

## Contacts

[Giuseppe Zerbo](https://github.com/pepzer), [giuseppe (dot) zerbo (at) gmail (dot) com](mailto:giuseppe.zerbo@gmail.com).

## License

Copyright © 2017 Giuseppe Zerbo.  
Distributed under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/).


