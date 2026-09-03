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
package com.iemr.common.controller.connect;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.utils.NetworkUtil;

import io.swagger.v3.oas.annotations.Operation;

/**
 * Exposes the server's LAN address so a mobile device on the same wifi
 * network can connect to this API.
 */
@RestController
@RequestMapping(value = "/public/connect")
public class ConnectController {

	@Value("${server.port:8080}")
	private int serverPort;

	@Operation(summary = "Get the server's LAN IP address and port")
	@GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getConnectInfo() {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("ip", NetworkUtil.getLanIPAddress());
		response.put("port", serverPort);
		return ResponseEntity.ok(response);
	}
}
