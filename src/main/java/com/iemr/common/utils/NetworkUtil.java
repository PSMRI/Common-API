/*
* AMRIT – Accessible Medical Records via Integrated Technology
* Integrated EHR (Electronic Health Records) Solution
*
* Copyright (C) "Piramal Swasthya Management and Research Institute"
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.utils;

import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the single LAN-facing IPv4 address the OS would use for outbound
 * traffic, used to build a URL that a device on the same wifi network (e.g.
 * a mobile phone) can connect to. A machine can have several network
 * interfaces (wifi, ethernet, VPN, virtual adapters); connecting a UDP
 * socket to an external address - without sending any packet - makes the OS
 * routing table pick the one real outbound interface, avoiding ambiguity
 * from iterating all interfaces.
 */
public final class NetworkUtil {

	private static final Logger logger = LoggerFactory.getLogger(NetworkUtil.class);
	private static final String FALLBACK_IP = "127.0.0.1";

	private NetworkUtil() {
	}

	public static String getLanIPAddress() {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			return socket.getLocalAddress().getHostAddress();
		} catch (Exception e) {
			logger.error("Failed to resolve LAN IP address", e);
			return FALLBACK_IP;
		}
	}
}
