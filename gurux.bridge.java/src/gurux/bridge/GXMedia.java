
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

import gurux.common.IGXMedia;

public class GXMedia {
    /**
     * Media name.
     */
    private String name;
    /**
     * Media type.
     */
    private String type;

    /**
     * Media settings.
     */
    private String settings;

    /**
     * Wait time in seconds if optical head is used. Default is 5 seconds
     */
    private int waitTime;
    /**
     * Maximum baud rate. It's not used if value is Zero.
     */
    private int maximumBaudRate;

    /*
     * Media.
     */
    private IGXMedia target;
    /**
     * Last received message.
     */
    private GXMessage message;

    /**
     * @return Media name.
     */
    public final String getName() {
        return name;
    }

    /**
     * @param value
     *            Media name.
     */
    public final void setName(final String value) {
        name = value;
    }

    /**
     * @return Media type.
     */
    public final String getType() {
        return type;
    }

    /**
     * @param value
     *            Media type.
     */
    public final void setType(final String value) {
        type = value;
    }

    /**
     * @return Media settings.
     */
    public final String getSettings() {
        return settings;
    }

    /**
     * @param value
     *            Media settings.
     */
    public final void setSettings(final String value) {
        settings = value;
    }

    /**
     * Is optical head (probe) used to read the meter.
     */
    private boolean useOpticalHead;

    /**
     * @return Is optical head (probe) used to read the meter.
     */
    public final boolean getUseOpticalHead() {
        return useOpticalHead;
    }

    /**
     * @param value
     *            Is optical head (probe) used to read the meter.
     */
    public final void setUseOpticalHead(boolean value) {
        useOpticalHead = value;
    }

    /**
     * @return Wait time in seconds if optical head is used. Default is 5
     *         seconds
     */
    public final int getWaitTime() {
        return waitTime;
    }

    /**
     * @param value
     *            Wait time in seconds if optical head is used. Default is 5
     *            seconds
     */
    public final void setWaitTime(final int value) {
        waitTime = value;
    }

    /**
     * @return Maximum baud rate. It's not used if value is Zero.
     */
    public final int getMaximumBaudRate() {
        return maximumBaudRate;
    }

    /**
     * @param value
     *            Maximum baud rate. It's not used if value is Zero.
     */
    public final void setMaximumBaudRate(final int value) {
        maximumBaudRate = value;
    }

    /*
     * Media.
     */
    public final IGXMedia getTarget() {
        return target;
    }

    /*
     * Media.
     */
    public final void setTarget(IGXMedia value) {
        target = value;
    }

    /**
     * Last received message.
     */
    public final GXMessage getMessage() {
        return message;
    }

    /**
     * Last received message.
     */
    public final void setMessage(GXMessage value) {
        message = value;
    }
}