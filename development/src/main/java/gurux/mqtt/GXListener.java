//
// --------------------------------------------------------------------------
//  Gurux Ltd
// 
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License 
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU General Public License for more details.
//
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------
package gurux.mqtt;

import java.lang.reflect.Array;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

import gurux.common.AutoResetEvent;
import gurux.common.GXCommon;
import gurux.common.GXSynchronousMediaBase;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.common.enums.MediaState;
import gurux.common.enums.TraceLevel;
import gurux.common.enums.TraceTypes;

/**
 * Listener listens MQTT events.
 */
class GXListener implements MqttCallback, IMqttActionListener {

    final GXMqtt parentMedia;

    /**
     * Amount of received bytes.
     */
    long bytesReceived = 0;

    private int messageId = 0;

    int getMessageId() {
        return ++messageId;
    }

    /**
     * Received event.
     */
    final AutoResetEvent replyReceivedEvent = new AutoResetEvent(false);

    public GXListener(GXMqtt parent) {
        parentMedia = parent;
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    /**
     * Last exception.
     */
    String lastException;

    /**
     * Handle received data.
     * 
     * @param length
     *            Length of received data.
     * @param buffer
     *            Received data.
     * @param sender
     *            Sender information.
     */
    private void handleReceivedData(final int length, final byte[] buffer,
            final String sender) {
        if (length == 0) {
            return;
        }
        Object eop = parentMedia.getEop();
        bytesReceived += length;
        int totalCount = 0;
        if (parentMedia.getIsSynchronous()) {
            TraceEventArgs arg = null;
            synchronized (parentMedia.getSyncBase().getSync()) {
                parentMedia.getSyncBase().appendData(buffer, 0, length);
                // Search end of packet if it is given.
                if (eop != null) {
                    if (eop instanceof Array) {
                        for (Object it : (Object[]) eop) {
                            totalCount = GXSynchronousMediaBase.indexOf(buffer,
                                    GXSynchronousMediaBase.getAsByteArray(it),
                                    0, length);
                            if (totalCount != -1) {
                                break;
                            }
                        }
                    } else {
                        totalCount = GXSynchronousMediaBase.indexOf(buffer,
                                GXSynchronousMediaBase.getAsByteArray(eop), 0,
                                length);
                    }
                }
                if (totalCount != -1) {
                    if (parentMedia.getTrace() == TraceLevel.VERBOSE) {
                        arg = new gurux.common.TraceEventArgs(
                                TraceTypes.RECEIVED, buffer, 0, totalCount + 1);
                    }
                    parentMedia.getSyncBase().setReceived();
                }
            }
            if (arg != null) {
                parentMedia.notifyTrace(arg);
            }
        } else {
            parentMedia.getSyncBase().resetReceivedSize();
            byte[] data = new byte[length];
            System.arraycopy(buffer, 0, data, 0, length);
            if (parentMedia.getTrace() == TraceLevel.VERBOSE) {
                parentMedia.notifyTrace(new gurux.common.TraceEventArgs(
                        TraceTypes.RECEIVED, data));
            }
            ReceiveEventArgs e = new ReceiveEventArgs(data, sender);
            parentMedia.notifyReceived(e);
        }
    }

    private GXMessage getMessage(final String data) {
        Gson g = new Gson();
        GXMessage msg = g.fromJson(data, GXMessage.class);
        return msg;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        String payload = new String(message.getPayload());
        GXMessage msg = getMessage(payload);
        if (parentMedia.getTrace() == TraceLevel.VERBOSE) {
            System.out.println("---Received message");
            System.out.println("Topic = " + topic);
            System.out.println("Payload = " + payload);
            System.out.println("QoS = " + message.getQos());
            System.out.println("Retain = " + message.getQos());
        }
        if (msg.getId() == messageId || msg.getType() == MessageType.CLOSE
                || msg.getType() == MessageType.EXCEPTION) {
            switch (msg.getType()) {
            case MessageType.OPEN:
                parentMedia.notifyMediaStateChange(MediaState.OPEN);
                replyReceivedEvent.set();
                break;
            case MessageType.SEND:
                break;
            case MessageType.RECEIVE:
                byte[] bytes = GXCommon.hexToBytes(msg.getFrame());
                replyReceivedEvent.set();
                if (bytes.length != 0) {
                    handleReceivedData(bytes.length, bytes, msg.getSender());
                }
                break;
            case MessageType.CLOSE:
                parentMedia.notifyMediaStateChange(MediaState.CLOSED);
                replyReceivedEvent.set();
                break;
            case MessageType.EXCEPTION:
                lastException = msg.getException();
                replyReceivedEvent.set();
                break;
            }
        } else {
            if (parentMedia.getTrace().ordinal() >= TraceLevel.INFO.ordinal()) {
                parentMedia.notifyTrace(new TraceEventArgs(TraceTypes.INFO,
                        "Unknown reply: " + msg));
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        replyReceivedEvent.set();
    }

    /**
     * The client has established a connection with the broker.
     */
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        replyReceivedEvent.set();
    }

    /**
     * The client has failed to established a connection with the broker.
     */
    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        lastException = exception.getMessage();
        replyReceivedEvent.set();
    }
}
