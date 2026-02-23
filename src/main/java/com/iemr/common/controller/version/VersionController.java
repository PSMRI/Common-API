/*
<<<<<<< HEAD
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
=======
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
/**
 * REST controller exposing application version and build metadata.
 * <p>
 * Provides the <code>/version</code> endpoint which returns Git metadata
 * in a standardized JSON format consistent across all AMRIT APIs.
 * </p>
 *
 * @author Vaishnav Bhosale
 */
>>>>>>> 5e9de591 (feat(health,version): update version and health endpoints and add advance check for database)
package com.iemr.common.controller.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
<<<<<<< HEAD
import java.io.InputStreamReader;
=======
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
>>>>>>> 5e9de591 (feat(health,version): update version and health endpoints and add advance check for database)

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@RestController
public class VersionController {

<<<<<<< HEAD
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Operation(summary = "Get version")
	@RequestMapping(value = "/version", method = { RequestMethod.GET })
	public String versionInformation() {
		OutputResponse output = new OutputResponse();
		try {
			logger.info("version Controller Start");
			output.setResponse(readGitProperties());
		} catch (Exception e) {
			output.setError(e);
=======
	private final Logger logger =
			LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	private static final String UNKNOWN_VALUE = "unknown";

	@Operation(summary = "Get version information")
	@GetMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> versionInformation() {
		Map<String, String> response = new LinkedHashMap<>();
		try {
			logger.info("version Controller Start");
			Properties gitProperties = loadGitProperties();
			response.put("buildTimestamp", gitProperties.getProperty("git.build.time", UNKNOWN_VALUE));
			response.put("version", gitProperties.getProperty("git.build.version", UNKNOWN_VALUE));
			response.put("branch", gitProperties.getProperty("git.branch", UNKNOWN_VALUE));
			response.put("commitHash", gitProperties.getProperty("git.commit.id.abbrev", UNKNOWN_VALUE));
		} catch (Exception e) {
			logger.error("Failed to load version information", e);
			response.put("buildTimestamp", UNKNOWN_VALUE);
			response.put("version", UNKNOWN_VALUE);
			response.put("branch", UNKNOWN_VALUE);
			response.put("commitHash", UNKNOWN_VALUE);
>>>>>>> 5e9de591 (feat(health,version): update version and health endpoints and add advance check for database)
		}
		logger.info("version Controller End");
		return ResponseEntity.ok(response);
	}

<<<<<<< HEAD
		logger.info("version Controller End");
		return output.toString();
	}

	private String readGitProperties() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("git.properties");

		return readFromInputStream(inputStream);
	}

	private String readFromInputStream(InputStream inputStream) throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}
		return resultStringBuilder.toString();
=======
	private Properties loadGitProperties() throws IOException {
		Properties properties = new Properties();
		try (InputStream input = getClass().getClassLoader()
				.getResourceAsStream("git.properties")) {
			if (input != null) {
				properties.load(input);
			}
		}
		return properties;
>>>>>>> 5e9de591 (feat(health,version): update version and health endpoints and add advance check for database)
	}
}
