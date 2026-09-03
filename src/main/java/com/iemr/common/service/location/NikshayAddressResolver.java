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
package com.iemr.common.service.location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iemr.common.data.location.NikshayDistrict;
import com.iemr.common.data.location.NikshayTU;
import com.iemr.common.data.location.NikshayVillage;
import com.iemr.common.data.users.ProviderServiceMapping;
import com.iemr.common.repository.location.NikshayDistrictRepository;
import com.iemr.common.repository.location.NikshayTURepository;
import com.iemr.common.repository.location.NikshayVillageRepository;
import com.iemr.common.repository.users.ProviderServiceMapRepository;

/**
 * Stop TB workers are mapped against Nikshay's own, isolated location
 * hierarchy (m_nikshay_district/tu/village), which uses a separate ID space
 * from AMRIT's standard masters (m_district/m_districtblock/m_DistrictBranchMapping).
 * The same numeric district/block/village IDs mean different places in each
 * hierarchy, so a Stop TB registration must never be resolved against the
 * standard masters (see StopTB district/village mismatch investigation, 2026-07-28).
 *
 * Every other service line (FLW/HWC/MMU) keeps using the standard masters
 * untouched — this resolver only changes behavior for Stop TB beneficiaries.
 */
@Component
public class NikshayAddressResolver {

	@Autowired
	private ProviderServiceMapRepository providerServiceMapRepository;
	@Autowired
	private NikshayDistrictRepository nikshayDistrictRepository;
	@Autowired
	private NikshayTURepository nikshayTURepository;
	@Autowired
	private NikshayVillageRepository nikshayVillageRepository;

	private static final String STOP_TB_SERVICE_NAME = "Stop TB";

	public boolean isStopTB(Integer providerServiceMapID) {
		if (providerServiceMapID == null) {
			return false;
		}
		ProviderServiceMapping psm = providerServiceMapRepository.findByID(providerServiceMapID);
		return psm != null && psm.getM_ServiceMaster() != null
				&& STOP_TB_SERVICE_NAME.equalsIgnoreCase(psm.getM_ServiceMaster().getServiceName());
	}

	public String resolveDistrictName(Integer nikshayDistrictID) {
		return nikshayDistrictRepository.findById(nikshayDistrictID).map(NikshayDistrict::getDistrictName)
				.orElse(null);
	}

	public String resolveTUName(Integer nikshayTUID) {
		return nikshayTURepository.findById(nikshayTUID).map(NikshayTU::getTuName).orElse(null);
	}

	public String resolveVillageName(Integer nikshayVillageID) {
		return nikshayVillageRepository.findById(nikshayVillageID).map(NikshayVillage::getVillageName).orElse(null);
	}
}
