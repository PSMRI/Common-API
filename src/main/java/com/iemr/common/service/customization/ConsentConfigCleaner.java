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
package com.iemr.common.service.customization;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.iemr.common.data.customization.consent.ConsentMode;
import com.iemr.common.data.customization.consent.ContextConfig;
import com.iemr.common.data.customization.consent.ModeEntry;

/**
 * Cleans a stored consent configuration before it is returned (feature 005, FR-005, research R5). Configurations
 * saved before the reviewed mode list may still allow VOICE_CLIP or carry the retired photo and voice settings;
 * they are removed in the response only. The stored row and its version are never changed here: the next save by
 * the administrator stores the cleaned form.
 */
@Component
public class ConsentConfigCleaner {

	/**
	 * Cleans the given context in place and returns it.
	 *
	 * @param stored a context parsed from the stored configuration, or {@code null}
	 * @return the same object, without retired modes or settings
	 */
	public ContextConfig clean(ContextConfig stored) {
		if (stored == null || stored.getModes() == null) {
			return stored;
		}
		List<ModeEntry> modes = stored.getModes();
		modes.removeIf(entry -> entry == null || entry.getMode() == null || !entry.getMode().isOffered());
		for (ModeEntry entry : modes) {
			if (entry.getMode() != ConsentMode.OTP && entry.getMode() != ConsentMode.WITNESSED) {
				entry.setSettings(null);
			}
		}
		if (modes.isEmpty() && Boolean.TRUE.equals(stored.getEnabled())) {
			ModeEntry readAloud = new ModeEntry();
			readAloud.setMode(ConsentMode.READ_ALOUD);
			readAloud.setRank(1);
			modes.add(readAloud);
		}
		if (stored.getDefaultMode() != null && !allows(modes, stored.getDefaultMode())) {
			Comparator<ModeEntry> byRank = Comparator.comparing(ModeEntry::getRank,
					Comparator.nullsLast(Comparator.naturalOrder()));
			stored.setDefaultMode(modes.stream().min(byRank).map(ModeEntry::getMode).orElse(null));
		}
		return stored;
	}

	private static boolean allows(List<ModeEntry> modes, ConsentMode mode) {
		return modes.stream().map(ModeEntry::getMode).anyMatch(m -> Objects.equals(m, mode));
	}
}
