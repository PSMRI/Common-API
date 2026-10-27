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
package com.iemr.common.controller.version;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Covers the /version endpoint that reports the build's git metadata. */
class VersionControllerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new VersionController()).build();
	}

	@Test
	@DisplayName("the git metadata on the classpath is reported as JSON")
	void gitMetadataIsReported() throws Exception {
		mockMvc.perform(get("/version")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.branch").value("main")).andExpect(jsonPath("$.commitHash").exists())
				.andExpect(jsonPath("$.version").exists()).andExpect(jsonPath("$.buildTimestamp").exists());
	}

	@Test
	@DisplayName("metadata the build did not record is reported as unknown rather than omitted")
	void missingEntriesAreReportedAsUnknown() throws Exception {
		// The test classpath's git.properties carries no build time or version.
		mockMvc.perform(get("/version")).andExpect(status().isOk())
				.andExpect(jsonPath("$.buildTimestamp").value("unknown"))
				.andExpect(jsonPath("$.version").value("unknown"));
	}
}
