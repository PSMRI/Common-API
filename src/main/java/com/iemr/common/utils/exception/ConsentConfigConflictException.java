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
package com.iemr.common.utils.exception;

/**
 * Someone else saved the consent configuration first. Returned to the client as HTTP 409 with
 * data.reason = STALE_VERSION.
 */
public class ConsentConfigConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static final String STALE_VERSION = "STALE_VERSION";

	public ConsentConfigConflictException() {
		super("Changed by someone else, reload");
	}

	public String getReason() {
		return STALE_VERSION;
	}
}
