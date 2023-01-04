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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.Gson;

import gurux.common.GXCommon;
import gurux.common.GXSync;
import gurux.common.GXSynchronousMediaBase;
import gurux.common.IGXMedia2;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.ReceiveParameters;
import gurux.common.TraceEventArgs;
import gurux.common.enums.MediaState;
import gurux.common.enums.TraceLevel;
import gurux.common.enums.TraceTypes;
import gurux.mqtt.enums.AvailableMediaSettings;

/**
 * The GXMqtt component determines methods that make the communication possible
 * using Internet.
 * 
 */
public class GXMqtt implements IGXMedia2, AutoCloseable {
    private MqttAsyncClient publisher;
    private int receiveDelay;

    private int asyncWaitTime;
    /**
     * Server address.
     */
    private String serverAddress;
    /**
     * Host port.
     */
    private int port = 1883;
    /**
     * Topic.
     */
    private String topic;

    /**
     * Used client ID.
     */
    private String clientId;

    /**
     * Client ID that user want's to use.
     */
    private String userClientId;

    /**
     * Synchronous data handler.
     */
    private GXSynchronousMediaBase syncBase;

    private final GXListener listener;

    /**
     * Amount of sent bytes.
     */
    private long bytesSent = 0;
    /**
     * Synchronous counter.
     */
    private int synchronous = 0;

    /**
     * Trace level.
     */
    private TraceLevel trace = TraceLevel.OFF;
    /**
     * Used end of packet.
     */
    private Object eop;
    /**
     * Configurable settings.
     */
    private int configurableSettings;
    /**
     * Media listeners.
     */
    private List<IGXMediaListener> listeners =
            new ArrayList<IGXMediaListener>();

    /**
     * Used locale.
     */
    private Locale locale;

    /**
     * Constructor.
     */
    public GXMqtt() {
        listener = new GXListener(this);
        syncBase = new GXSynchronousMediaBase(1024);
        setConfigurableSettings(AvailableMediaSettings.ALL.getValue());
        locale = Locale.getDefault();
    }

    /**
     * Client Constructor.
     * 
     * @param name
     *            Host name.
     * @param portNo
     *            Client port number.
     */
    public GXMqtt(final String name, final int portNo) {
        this();
        serverAddress = name;
        setPort(portNo);
    }

    /**
     * Destructor.
     */
    @Override
    protected final void finalize() throws Throwable {
        if (isOpen()) {
            close();
        }
    }

    /**
     * Returns synchronous class used to communicate synchronously.
     * 
     * @return Synchronous class.
     */
    final GXSynchronousMediaBase getSyncBase() {
        return syncBase;
    }

    @Override
    public final TraceLevel getTrace() {
        return trace;
    }

    @Override
    public final void setTrace(final TraceLevel value) {
        trace = value;
        syncBase.setTrace(value);
    }

    /**
     * Notify that property has changed.
     * 
     * @param info
     *            Name of changed property.
     */
    private void notifyPropertyChanged(final String info) {
        for (IGXMediaListener it : listeners) {
            it.onPropertyChanged(this, new PropertyChangedEventArgs(info));
        }
    }

    /**
     * Notify clients from error occurred.
     * 
     * @param ex
     *            Occurred error.
     */
    final void notifyError(final RuntimeException ex) {
        for (IGXMediaListener it : listeners) {
            it.onError(this, ex);
            if (trace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                it.onTrace(this, new TraceEventArgs(TraceTypes.ERROR, ex));
            }
        }
    }

    /**
     * Notify clients from new data received.
     * 
     * @param e
     *            Received event argument.
     */
    final void notifyReceived(final ReceiveEventArgs e) {
        for (IGXMediaListener it : listeners) {
            it.onReceived(this, e);
        }
    }

    /**
     * Notify clients from trace events.
     * 
     * @param e
     *            Trace event argument.
     */
    final void notifyTrace(final TraceEventArgs e) {
        for (IGXMediaListener it : listeners) {
            it.onTrace(this, e);
        }
    }

    @Override
    public final int getConfigurableSettings() {
        return configurableSettings;
    }

    @Override
    public final void setConfigurableSettings(final int value) {
        this.configurableSettings = value;
    }

    @Override
    public final boolean properties(final javax.swing.JFrame parent) {
        GXSettings dlg = new GXSettings(parent, true, this, locale);
        dlg.pack();
        dlg.setVisible(true);
        return dlg.isAccepted();
    }

    /**
     * Displays the copyright of the control, user license, and version
     * information, in a dialog box.
     */
    public final void aboutBox() {
        throw new UnsupportedOperationException();
    }

    private void publishMessageAsync(final GXMessage payload)
            throws MqttPersistenceException, MqttException {
        MqttMessage msg = new MqttMessage();
        msg.setQos(1);
        Gson g = new Gson();
        String tmp = g.toJson(payload);
        msg.setPayload(tmp.getBytes());
        if (trace.ordinal() > TraceLevel.WARNING.ordinal()) {
            System.out.println("---Sending message: " + tmp);
        }
        publisher.publish(topic, msg);
    }

    /**
     * @param target
     *            IP address of the receiver (optional). Reply data is received
     *            through OnReceived event.
     */
    @Override
    public final void send(final Object data, final String target)
            throws Exception {
        if (trace == TraceLevel.VERBOSE) {
            notifyTrace(new TraceEventArgs(TraceTypes.SENT, data));
        }
        // Reset last position if end of packet is used.
        synchronized (syncBase.getSync()) {
            syncBase.resetLastPosition();
        }
        byte[] buff = GXSynchronousMediaBase.getAsByteArray(data);
        if (buff == null) {
            throw new IllegalArgumentException(
                    "Data send failed. Invalid data.");
        }
        bytesSent += buff.length;
        GXMessage msg = new GXMessage();
        msg.setId(listener.getMessageId());
        msg.setType(MessageType.SEND);
        msg.setSender(clientId);
        msg.setFrame(GXCommon.bytesToHex(buff));
        publishMessageAsync(msg);
    }

    /**
     * Notify client from media state change.
     * 
     * @param state
     *            New media state.
     */
    void notifyMediaStateChange(final MediaState state) {
        for (IGXMediaListener it : listeners) {
            if (trace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                it.onTrace(this, new TraceEventArgs(TraceTypes.INFO, state));
            }
            it.onMediaStateChange(this, new MediaStateEventArgs(state));
        }
    }

    /**
     * Opens the connection. Protocol, Port and HostName must be set, before
     * calling the Open method.
     * 
     * @see #port
     * @see #serverAddress
     * @see #close
     */
    @Override
    public final void open() throws Exception {
        close();
        listener.lastException = null;
        try {
            synchronized (syncBase.getSync()) {
                syncBase.resetLastPosition();
            }
            notifyMediaStateChange(MediaState.OPENING);
            if (userClientId == null || userClientId == "") {
                clientId = UUID.randomUUID().toString();
            } else {
                clientId = userClientId;
            }

            publisher = new MqttAsyncClient(
                    "tcp://" + serverAddress + ":" + port, clientId);
            publisher.setCallback(listener);
            if (trace.ordinal() >= TraceLevel.INFO.ordinal()) {
                notifyTrace(new TraceEventArgs(TraceTypes.INFO,
                        "MQTT settings: Host: " + serverAddress + " Port: "
                                + String.valueOf(getPort())));
            }
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            publisher.connect(options, listener);
            if (getAsyncWaitTime() < 1) {
                listener.replyReceivedEvent.waitOne();
            } else {
                listener.replyReceivedEvent.waitOne(getAsyncWaitTime() * 1000);
            }
            if (listener.lastException != null) {
                throw new RuntimeException(listener.lastException);
            }
            publisher.subscribe(clientId, 2);
            listener.replyReceivedEvent.reset();
            GXMessage msg = new GXMessage();
            msg.setId(listener.getMessageId());
            msg.setType(MessageType.OPEN);
            msg.setSender(clientId);
            publishMessageAsync(msg);
            if (getAsyncWaitTime() < 1) {
                listener.replyReceivedEvent.waitOne();
            } else {
                listener.replyReceivedEvent.waitOne(getAsyncWaitTime() * 1000);
            }
            if (listener.lastException != null) {
                throw new RuntimeException(listener.lastException);
            }
            notifyMediaStateChange(MediaState.OPEN);
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    @Override
    public final void close() {
        listener.lastException = null;
        if (isOpen()) {
            GXMessage msg = new GXMessage();
            msg.setId(listener.getMessageId());
            msg.setType(MessageType.CLOSE);
            msg.setSender(clientId);
            try {
                publishMessageAsync(msg);
            } catch (Exception ex) {
                listener.replyReceivedEvent.set();
            }
            if (getAsyncWaitTime() < 1) {
                listener.replyReceivedEvent.waitOne();
            } else {
                listener.replyReceivedEvent.waitOne(getAsyncWaitTime() * 1000);
            }
            if (publisher != null) {
                try {
                    IMqttToken disconnectToken = publisher.disconnect();
                    disconnectToken.waitForCompletion();
                    publisher.close(true);
                    publisher = null;
                } catch (MqttException e) {
                    publisher = null;
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            if (listener.lastException != null) {
                throw new RuntimeException(listener.lastException);
            }
        }
    }

    @Override
    public final boolean isOpen() {
        return publisher != null && publisher.isConnected();
    }

    /**
     * @return Used topic.
     * @see #open
     * @see #port
     * @see #serverAddress
     */
    public final String getTopic() {
        return topic;
    }

    /**
     * 
     * @param value
     *            Used topic.
     * @see #open
     * @see #port
     * @see #serverAddress
     */
    public final void setTopic(final String value) {
        if (topic == null || !topic.equals(value)) {
            topic = value;
            notifyPropertyChanged("Topic");
        }
    }

    /**
     * Retrieves the name or IP address of the host.
     * 
     * @return The name of the host.
     * 
     * @see #open
     * @see #port
     * @see #topic
     */
    public final String getServerAddress() {
        return serverAddress;
    }

    /**
     * Sets the name or IP address of the host.
     * 
     * @param value
     *            The name of the host.
     */
    public final void setServerAddress(final String value) {
        if (serverAddress == null || !serverAddress.equals(value)) {
            serverAddress = value;
            notifyPropertyChanged("ServerAddress");
        }
    }

    /**
     * Retrieves or sets the host or server port number.
     * 
     * @return Host or server port number.
     * 
     * @see #open
     * @see #serverAddress
     * @see #topic
     */
    public final int getPort() {
        return port;
    }

    /**
     * Retrieves or sets the host or server port number.
     * 
     * @param value
     *            Host or server port number
     * @see #open
     * @see #serverAddress
     * @see #topic
     */
    public final void setPort(final int value) {
        if (port != value) {
            port = value;
            notifyPropertyChanged("Port");
        }
    }

    @Override
    public final <T> boolean receive(final ReceiveParameters<T> args) {
        return syncBase.receive(args);
    }

    /**
     * Sent byte count.
     * 
     * @see #getBytesReceived
     * @see #resetByteCounters
     */
    @Override
    public final long getBytesSent() {
        return bytesSent;
    }

    /**
     * Received byte count.
     * 
     * @see #bytesSent
     * @see #resetByteCounters
     */
    @Override
    public final long getBytesReceived() {
        return listener.bytesReceived;
    }

    /**
     * Resets BytesReceived and BytesSent counters.
     * 
     * @see #bytesSent
     * @see #getBytesReceived
     */
    @Override
    public final void resetByteCounters() {
        listener.bytesReceived = bytesSent = 0;
    }

    /**
     * Media settings as a XML string.
     */
    @Override
    public final String getSettings() {
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");
        if (serverAddress != null && !serverAddress.isEmpty()) {
            sb.append("<IP>");
            sb.append(serverAddress);
            sb.append("</IP>");
            sb.append(nl);
        }
        if (port != 0) {
            sb.append("<Port>");
            sb.append(String.valueOf(port));
            sb.append("</Port>");
            sb.append(nl);
        }
        sb.append("<Topic>");
        sb.append(topic);
        sb.append("</Topic>");
        sb.append(nl);
        if (userClientId != null && !userClientId.isEmpty()) {
            sb.append("<ClientId>");
            sb.append(clientId);
            sb.append("</ClientId>");
            sb.append(nl);
        }
        return sb.toString();
    }

    @Override
    public final void setSettings(final String value) {
        // Reset to default values.
        topic = "";
        clientId = "";
        serverAddress = "";
        port = 0;
        userClientId = "";
        if (value != null && !value.isEmpty()) {
            try {
                DocumentBuilderFactory factory =
                        DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                StringBuilder sb = new StringBuilder();
                if (value.startsWith("<?xml version=\"1.0\"?>")) {
                    sb.append(value);
                } else {
                    String nl = System.getProperty("line.separator");
                    sb.append("<?xml version=\"1.0\"?>\r\n");
                    sb.append(nl);
                    sb.append("<Mqtt>");
                    sb.append(value);
                    sb.append(nl);
                    sb.append("</Mqtt>");
                }
                InputSource is =
                        new InputSource(new StringReader(sb.toString()));
                Document doc = builder.parse(is);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getChildNodes();
                if (nList.getLength() != 1) {
                    throw new IllegalArgumentException(
                            "Invalid XML root node.");
                }
                nList = nList.item(0).getChildNodes();
                for (int pos = 0; pos < nList.getLength(); ++pos) {
                    Node it = nList.item(pos);
                    if (it.getNodeType() == Node.ELEMENT_NODE) {
                        if ("topic".equalsIgnoreCase(it.getNodeName())) {
                            setTopic(it.getFirstChild().getNodeValue());
                        } else if ("IP".equalsIgnoreCase(it.getNodeName())) {
                            setServerAddress(it.getFirstChild().getNodeValue());
                        } else if ("Port".equalsIgnoreCase(it.getNodeName())) {
                            setPort(Integer.parseInt(
                                    it.getFirstChild().getNodeValue()));
                        } else if ("ClientId"
                                .equalsIgnoreCase(it.getNodeName())) {
                            userClientId = it.getFirstChild().getNodeValue();
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Override
    public final void copy(final Object target) {
        GXMqtt tmp = (GXMqtt) target;
        setPort(tmp.getPort());
        setServerAddress(tmp.getServerAddress());
        setTopic(tmp.getTopic());
    }

    @Override
    public final String getName() {
        return getServerAddress() + " " + getPort() + " MQTT";
    }

    @Override
    public final String getMediaType() {
        return "MQTT";
    }

    @Override
    public final Object getSynchronous() {
        synchronized (this) {
            int[] tmp = new int[] { synchronous };
            GXSync obj = new GXSync(tmp);
            synchronous = tmp[0];
            return obj;
        }
    }

    @Override
    public final boolean getIsSynchronous() {
        synchronized (this) {
            return synchronous != 0;
        }
    }

    @Override
    public final void resetSynchronousBuffer() {
        synchronized (syncBase.getSync()) {
            syncBase.resetReceivedSize();
        }
    }

    @Override
    public final void validate() {
        if (getPort() == 0) {
            // Localize strings.
            ResourceBundle bundle =
                    ResourceBundle.getBundle("resources", locale);
            throw new RuntimeException(bundle.getString("InvalidPortName"));
        }
        if (serverAddress == null || "".equals(serverAddress)) {
            ResourceBundle bundle =
                    ResourceBundle.getBundle("resources", locale);
            throw new RuntimeException(bundle.getString("InvalidHostName"));
        }
        if (topic == null || "".equals(topic)) {
            ResourceBundle bundle =
                    ResourceBundle.getBundle("resources", locale);
            throw new RuntimeException(bundle.getString("InvalidTopic"));
        }
    }

    @Override
    public final Object getEop() {
        return eop;
    }

    @Override
    public final void setEop(final Object value) {
        eop = value;
    }

    @Override
    public final void addListener(final IGXMediaListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public final void removeListener(final IGXMediaListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public int getReceiveDelay() {
        return receiveDelay;
    }

    @Override
    public void setReceiveDelay(final int value) {
        receiveDelay = value;
    }

    @Override
    public int getAsyncWaitTime() {
        return asyncWaitTime;
    }

    @Override
    public void setAsyncWaitTime(final int value) {
        asyncWaitTime = value;
    }

    @Override
    public Object getAsyncWaitHandle() {
        return null;
    }

    /**
     * 
     * @return Used locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * 
     * @param value
     *            Used locale.
     */
    public void setLocale(final Locale value) {
        locale = value;
    }

    /**
     * 
     * @return Optional client ID.
     */
    public String getUserClientId() {
        return userClientId;
    }

    /**
     * 
     * @param value
     *            Optional client ID.
     */
    public void setUserClientId(final String value) {
        userClientId = value;
    }

}