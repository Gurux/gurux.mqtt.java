
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

import java.util.List;

/**
 * Connection settings.
 */
public class Connection {
    /**
     * Broker address.
     */
    private String brokerAddress;
    /**
     * Broker port.
     */
    private int brokerPort;

    /**
     * (Unique) Bridge name.
     */
    private String name;

    private java.util.ArrayList<GXMedia> connections;

    /**
     * Constructor.
     */
    public Connection() {
        connections = new java.util.ArrayList<GXMedia>();
    }

    /**
     * @return Broker address.
     */
    public final String getBrokerAddress() {
        return brokerAddress;
    }

    /**
     * @param value
     *            Broker address.
     */
    public final void setBrokerAddress(final String value) {
        brokerAddress = value;
    }

    /**
     * @return Broker port.
     */
    public final int getBrokerPort() {
        return brokerPort;
    }

    /**
     * @param value
     *            Broker port.
     */
    public final void setBrokerPort(final int value) {
        brokerPort = value;
    }

    /**
     * @return (Unique) Bridge name.
     */
    public final String getName() {
        return name;
    }

    /**
     * @param value
     *            (Unique) Bridge name.
     */
    public final void setName(final String value) {
        name = value;
    }

    public final List<GXMedia> getConnections() {
        return connections;
    }
}
