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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.iemr.common.controller.version;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class VersionController {

	private final Logger logger =
			LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Operation(summary = "Get version")
	@RequestMapping(value = "/version", method = { RequestMethod.GET })
	public VersionInfo versionInformation() {

		Properties properties = new Properties();

		try (InputStream is = getClass()
				.getClassLoader()
				.getResourceAsStream("git.properties")) {

			if (is != null) {
				properties.load(is);
			}

		} catch (Exception e) {
			logger.error("Error reading git.properties", e);
		}

		return new VersionInfo(
				properties.getProperty("git.commit.id.abbrev", "unknown"),
				properties.getProperty("git.build.time", "unknown")
		);
	}
}
