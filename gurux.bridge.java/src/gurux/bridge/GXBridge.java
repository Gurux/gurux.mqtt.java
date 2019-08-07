
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
package gurux.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.Gson;

import gurux.common.GXCommon;
import gurux.common.IGXMedia2;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.ReceiveParameters;
import gurux.common.TraceEventArgs;
import gurux.common.enums.TraceLevel;
import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.net.GXNet;
import gurux.serial.GXSerial;

public class GXBridge
        implements MqttCallback, IGXMediaListener, IMqttActionListener {
    TraceLevel trace;
    Connection connection;
    MqttAsyncClient publisher;

    public static void showInformation(Connection connection) {
        System.out.println("Bridge topic: " + connection.getName());
        // Subscribe to a topic
        for (GXMedia it : connection.getConnections()) {
            System.out.println("Media topic: " + it.getName());
        }
    }

    public void start(TraceLevel traceLevel, String server, int port,
            Connection c) throws MqttException {
        trace = traceLevel;
        connection = c;
        // Create a new MQTT client.
        String publisherId = UUID.randomUUID().toString();
        publisher = new MqttAsyncClient("tcp://" + connection.getBrokerAddress()
                + ":" + connection.getBrokerPort(), publisherId);
        publisher.setCallback(this);
        int pos = 1;
        for (GXMedia it : connection.getConnections()) {
            if (it.getType().equals("Net")) {
                it.setTarget(new GXNet());
                it.getTarget().setSettings(it.getSettings());
            } else if (it.getType().equals("Serial")) {
                it.setTarget(new GXSerial());
                it.getTarget().setSettings(it.getSettings());
            } else {
                throw new RuntimeException(
                        "Unknown media type." + it.getType());
            }
            if (it.getName() == null || it.getName() == "") {
                it.setName(connection.getName() + "/" + String.valueOf(pos));
                ++pos;
            } else {
                it.setName(connection.getName() + "/" + it.getName());
            }
            it.getTarget().addListener(this);
            if (it.getTarget() instanceof IGXMedia2) {
                ((IGXMedia2) it.getTarget()).setReceiveDelay(100);
            }
        }
        showInformation(connection);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        publisher.connect(options, this);
    }

    private static void initializeIEC(TraceLevel trace, GXMedia media)
            throws Exception {
        GXSerial serial = (GXSerial) media.getTarget();
        serial.setBaudRate(BaudRate.BAUD_RATE_300);
        serial.setDataBits(7);
        serial.setParity(Parity.EVEN);
        serial.setStopBits(StopBits.ONE);
        byte Terminator = (byte) 0x0A;
        // Some meters need a little break.
        Thread.sleep(1000);
        // Query device information.
        String data = "/?!\r\n";
        WriteLog(trace, "IEC Sending:" + data);
        if (media.getWaitTime() == 0) {
            media.setWaitTime(5);
        }
        ReceiveParameters<String> p =
                new ReceiveParameters<String>(String.class);
        p.setAllData(false);
        p.setEop(Terminator);
        p.setWaitTime(media.getWaitTime() * 1000);
        synchronized (media.getTarget().getSynchronous()) {
            media.getTarget().send(data, null);
            if (!media.getTarget().receive(p)) {
                discIEC(media);
                String str =
                        "Failed to receive reply from the device in given time.";
                WriteLog(trace, str);
                media.getTarget().send(data, null);
                if (!media.getTarget().receive(p)) {
                    throw new RuntimeException(str);
                }
            }
            // If echo is used.
            if (data.equals(p.getReply())) {
                p.setReply(null);
                if (!media.getTarget().receive(p)) {
                    data = "Failed to receive reply from the device in given time.";
                    WriteLog(trace, data);
                    throw new RuntimeException(data);
                }
            }
        }
        WriteLog(trace, "IEC received: " + p.getReply());
        if (p.getReply().charAt(0) != '/') {
            p.setWaitTime(100);
            media.getTarget().receive(p);
            throw new RuntimeException("Invalid responce.");
        }
        String manufactureID = p.getReply().substring(1, 4);
        char baudrate = p.getReply().charAt(4);
        int BaudRate = 0;
        switch (baudrate) {
        case '0':
            BaudRate = 300;
            break;
        case '1':
            BaudRate = 600;
            break;
        case '2':
            BaudRate = 1200;
            break;
        case '3':
            BaudRate = 2400;
            break;
        case '4':
            BaudRate = 4800;
            break;
        case '5':
            BaudRate = 9600;
            break;
        case '6':
            BaudRate = 19200;
            break;
        default:
            throw new RuntimeException("Unknown baud rate.");
        }
        if (media.getMaximumBaudRate() != 0) {
            BaudRate = media.getMaximumBaudRate();
            baudrate = getIecBaudRate(BaudRate);
            WriteLog(trace,
                    "Maximum BaudRate is set to : " + String.valueOf(BaudRate));
        }
        WriteLog(trace, "BaudRate is : " + String.valueOf(BaudRate));
        // Send ACK
        // Send Protocol control character
        // "2" HDLC protocol procedure (Mode E)
        byte controlCharacter = (byte) '2';
        // Send Baud rate character
        // Mode control character
        byte ModeControlCharacter = (byte) '2';
        // "2" //(HDLC protocol procedure) (Binary mode)
        // Set mode E.
        byte[] arr = new byte[] { 0x06, controlCharacter, (byte) baudrate,
                ModeControlCharacter, 13, 10 };
        WriteLog(trace, "Moving to mode E. " + GXCommon.bytesToHex(arr));
        synchronized (media.getTarget().getSynchronous()) {
            p.setReply(null);
            media.getTarget().send(arr, null);
            p.setWaitTime(2000);
            // Note! All meters do not echo this.
            media.getTarget().receive(p);
            if (p.getReply() != null) {
                WriteLog(trace, "Received: " + p.getReply());
            }
            media.getTarget().close();
            serial.setBaudRate(gurux.io.BaudRate.forValue(BaudRate));
            serial.setDataBits(8);
            serial.setParity(Parity.NONE);
            serial.setStopBits(StopBits.ONE);
            serial.open();
            // Some meters need this sleep. Do not remove.
            Thread.sleep(1000);
        }
    }

    /**
     * Send IEC disconnect message.
     * 
     * @throws Exception
     */
    private static void discIEC(GXMedia media) throws Exception {
        ReceiveParameters<String> p =
                new ReceiveParameters<String>(String.class);
        p.setAllData(false);
        p.setEop((byte) 0x0A);
        p.setWaitTime(media.getWaitTime() * 1000);
        String data = (char) 0x01 + "B0" + (char) 0x03 + "\r\n";
        media.getTarget().send(data, null);
        p.setCount(1);
        media.getTarget().receive(p);
    }

    private static char getIecBaudRate(int baudrate) {
        char rate = '5';
        switch (baudrate) {
        case 300:
            rate = '0';
            break;
        case 600:
            rate = '1';
            break;
        case 1200:
            rate = '2';
            break;
        case 2400:
            rate = '3';
            break;
        case 4800:
            rate = '4';
            break;
        case 9600:
            rate = '5';
            break;
        case 19200:
            rate = '6';
            break;
        default:
            throw new RuntimeException("Unknown baud rate.");
        }
        return rate;
    }

    private static void WriteLog(TraceLevel trace, String message) {
        if (trace.ordinal() > TraceLevel.WARNING.ordinal()) {
            System.out.println(message);
        }
    }

    public void connectionLost(Throwable cause) {
        if (trace.ordinal() > TraceLevel.WARNING.ordinal()) {
            System.out.println(
                    "--- Disconnected from the server. " + cause.getMessage());
        }
    }

    private GXMessage getMessage(final String data) {
        Gson g = new Gson();
        GXMessage msg = g.fromJson(data, GXMessage.class);
        return msg;
    }

    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        String payload = new String(message.getPayload());
        GXMessage msg = getMessage(payload);
        if (trace == TraceLevel.VERBOSE) {
            System.out.println("---Received message");
            System.out.println("Topic = " + topic);
            System.out.println("Payload = " + payload);
            System.out.println("QoS = " + message.getQos());
            System.out.println("Retain = " + message.getQos());
        }
        if (msg.getSender() != null) {
            GXMessage msg2;
            for (GXMedia it : connection.getConnections()) {
                if (it.getName().equals(topic)) {
                    it.setMessage(msg);
                    msg2 = new GXMessage();
                    msg2.setId(msg.getId());
                    msg2.setSender(topic);
                    try {
                        switch (msg.getType()) {
                        case MessageType.OPEN:
                            it.getTarget().open();
                            try {
                                // Move to mode E if optical head is used.
                                if (it.getTarget() instanceof GXSerial
                                        && it.getUseOpticalHead()) {
                                    initializeIEC(trace, it);
                                }
                                // Mark EOP so reading is faster.
                                it.getTarget().setEop((byte) 0x7e);
                            } catch (RuntimeException ex) {
                                it.getTarget().close();
                                throw ex;
                            }
                            msg2.setType(MessageType.OPEN);
                            publish(msg.getSender(), msg2);
                            break;
                        case MessageType.SEND:
                            it.getTarget().send(
                                    GXCommon.hexToBytes(msg.getFrame()), null);
                            // There is no need to send ACK.
                            break;
                        case MessageType.CLOSE:
                            it.getTarget().close();
                            msg2.setType(MessageType.CLOSE);
                            publish(msg.getSender(), msg2);
                            break;
                        }
                    } catch (Exception ex) {
                        msg2.setType(MessageType.EXCEPTION);
                        msg2.setException(ex.getMessage());
                        publish(msg.getSender(), msg2);
                    }
                    break;
                }
            }
        }
    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void onError(final Object sender, final Exception ex) {
        if (trace != TraceLevel.OFF) {
            System.out.println(ex.getMessage());
        }
        for (GXMedia it : connection.getConnections()) {
            if (it.getTarget() == sender
                    && it.getMessage().getSender() != null) {
                GXMessage msg = new GXMessage();
                msg.setId((short) 0);
                msg.setSender(it.getName());
                msg.setType(MessageType.EXCEPTION);
                msg.setException(ex.getMessage());
                try {
                    publish(it.getMessage().getSender(), msg);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                break;
            }
        }
    }

    public void onReceived(final Object sender, final ReceiveEventArgs e) {
        String tmp = GXCommon.bytesToHex((byte[]) e.getData());
        if (trace == TraceLevel.VERBOSE) {
            System.out.println("Received: " + tmp);
        }
        for (GXMedia it : connection.getConnections()) {
            if (it.getTarget() == sender
                    && it.getMessage().getSender() != null) {
                GXMessage msg = new GXMessage();
                msg.setId(it.getMessage().getId());
                msg.setType(MessageType.RECEIVE);
                msg.setSender(it.getName());
                msg.setFrame(tmp);
                try {
                    publish(it.getMessage().getSender(), msg);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                break;
            }
        }
    }

    private void publish(final String topic, final GXMessage payload)
            throws MqttPersistenceException, MqttException {
        MqttMessage msg = new MqttMessage();
        msg.setQos(0);
        msg.setRetained(true);
        Gson g = new Gson();
        String tmp = g.toJson(payload);
        msg.setPayload(tmp.getBytes());
        if (trace.ordinal() > TraceLevel.WARNING.ordinal()) {
            System.out.println("---Sending message: " + tmp);
        }
        publisher.publish(topic, msg);
    }

    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {
    }

    public void onTrace(Object sender, TraceEventArgs e) {
    }

    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        if (trace.ordinal() > TraceLevel.WARNING.ordinal()) {
            System.out.println("--- Connected with the server.");
        }
        // Subscribe topics
        List<String> topics = new ArrayList<String>();
        int[] list = new int[connection.getConnections().size()];
        for (GXMedia it : connection.getConnections()) {
            topics.add(it.getName());
        }
        try {
            publisher.subscribe(topics.toArray(new String[0]), list);
        } catch (MqttException e) {
            System.out.println(
                    "--- Failed to Connect to the server. " + e.getMessage());
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
    }
}