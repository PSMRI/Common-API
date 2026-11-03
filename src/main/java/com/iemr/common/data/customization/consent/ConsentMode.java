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
package com.iemr.common.data.customization.consent;

/**
 * Ways consent can be taken at the facility (proposal §3.1 rule 6). Verbal-on-call is for helplines and is
 * not configurable here.
 */
public enum ConsentMode {
	SELF_READ("Self-read"),
	READ_ALOUD("Read aloud"),
	VOICE_CLIP("Voice clip"),
	WITNESSED("Witnessed"),
	SIGNATURE_PHOTO("Signature / thumb + photo"),
	OTP("OTP");

	private final String displayName;

	ConsentMode(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
