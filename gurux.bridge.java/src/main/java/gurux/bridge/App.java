
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import gurux.common.GXCmdParameter;
import gurux.common.GXCommon;
import gurux.common.enums.TraceLevel;

/**
 * Gurux bridge for Java.
 */
public class App {
    /**
     * @param args
     *            the command line arguments
     * @throws IOException
     * @throws MqttException
     */
    public static void main(String args[]) throws IOException, MqttException {
        Gson g = new GsonBuilder().disableHtmlEscaping().create();
        Connection settings = null;
        File file = new File("connections.json");
        if (file.exists()) {
            settings = g.fromJson(new FileReader(file), Connection.class);
        }
        if (settings == null) {
            settings = new Connection();
            // Add TCP/IP connection example.
            GXMedia m = new GXMedia();
            settings.setBrokerAddress("localhost");
            settings.setBrokerPort(1883);
            settings.setName(UUID.randomUUID().toString());
            m.setName("1");
            m.setType("Net");
            m.setSettings("<IP>localhost</IP><Port>4061</Port>");
            settings.getConnections().add(m);

            // Add serial port connection example
            m = new GXMedia();
            m.setName("2");
            m.setType("Serial");
            m.setSettings("<Port>COM1</Port>");
            settings.getConnections().add(m);
            FileWriter writer = new FileWriter(file);
            JsonWriter jWriter = new JsonWriter(writer);
            jWriter.setIndent("  ");
            try {
                g.toJson(settings, Connection.class, jWriter);
            } finally {
                writer.flush();
            }
            return;
        }
        String host = settings.getBrokerAddress();
        int port = settings.getBrokerPort();
        TraceLevel trace = TraceLevel.ERROR;
        List<GXCmdParameter> parameters = GXCommon.getParameters(args, "h:p:t:");
        for (GXCmdParameter it : parameters) {
            switch (it.getTag()) {
            case 'h':
                // Port.
                host = it.getValue();
                break;
            case 'p':
                // Port.
                port = Integer.parseInt(it.getValue());
                break;
            case 't':
                // Trace.
                try {
                    trace = TraceLevel.valueOf(it.getValue().toUpperCase());
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException(
                            "Invalid trace level option. (Error, Warning, Info, Verbose, Off)");
                }
                break;
            default:
                showHelp();
                return;
            }
        }
        if (host.equals("")) {
            throw new RuntimeException("Broker address is missing. Example -h localhost");
        }
        if (port == 0) {
            throw new RuntimeException("Broker port is missing. Example -p 1883");
        }
        System.out.println("Connecting to the Broker in address: " + host + ":" + port);
        GXBridge bridge = new GXBridge();
        bridge.start(trace, host, port, settings);
        System.out.println("Press Enter to close.");
        while (System.in.read() != 13) {
            System.out.println("Press Enter to close.");
        }
    }

    static void showHelp() {
        System.out.println("Gurux.Bridge distribute received connections to several servers.");
        System.out.println("Gurux.Bridge -h Broker Address -p Broker Port numer");
        System.out.println(" -h \tBroker IP address.");
        System.out.println(" -p \tBroker port number.");
        System.out.println(" -t [Error, Warning, Info, Verbose] Trace messages.");
        System.out.println("Example:");
        System.out.println("Gurux.Bridge -h localhost -p 1883");
    }
}