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
 * Ways consent can be taken at the facility (proposal §3.1 rule 6, as reviewed on 3 Nov 2026). Verbal-on-call is
 * for helplines and is not configurable here.
 *
 * <p>VOICE_CLIP is no longer offered (feature 005): the recording is part of Patient read it and Read aloud. It is
 * kept only so configurations saved before the change still load, and so a request that still sends it gets a clear
 * refusal instead of "not a valid mode".
 */
public enum ConsentMode {
	SELF_READ("Patient read it", true),
	READ_ALOUD("Read aloud + recorded agreement", true),
	VOICE_CLIP("Voice clip", false),
	WITNESSED("Guardian / Representative", true),
	SIGNATURE_PHOTO("Signed consent form (signature / thumb)", true),
	OTP("OTP to patient's phone", true);

	private final String displayName;

	private final boolean offered;

	ConsentMode(String displayName, boolean offered) {
		this.displayName = displayName;
		this.offered = offered;
	}

	public String getDisplayName() {
		return displayName;
	}

	/** Whether the mode may be configured and is returned to the apps. */
	public boolean isOffered() {
		return offered;
	}
}
