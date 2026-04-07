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
package com.iemr.common.service.beneficiary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.common.data.beneficiary.BeneficiaryOccupation;
import com.iemr.common.repository.beneficiary.BeneficiaryOccupationRepository;
import org.springframework.context.annotation.Profile;

@Service
@Profile("!swagger")
public class BeneficiaryOccupationServiceImpl implements BeneficiaryOccupationService {

	private BeneficiaryOccupationRepository beneficiaryOccupationRepository;

	@Autowired
	public void setBeneficiaryOccupationRepository(BeneficiaryOccupationRepository beneficiaryOccupationRepository) {
		this.beneficiaryOccupationRepository = beneficiaryOccupationRepository;
	}

	@Override
	public List<BeneficiaryOccupation> getActiveOccupations() {
		List<BeneficiaryOccupation> benRelationshipTypes = new ArrayList<BeneficiaryOccupation>();
		Set<Object[]> resultSet = beneficiaryOccupationRepository.getActiveOccupations();
		for (Object[] object : resultSet) {
			if (object != null && object.length >= 2) {
				benRelationshipTypes.add(new BeneficiaryOccupation().getOccupation((Long) object[0], (String) object[1]));
			}
		}
		return benRelationshipTypes;
	}

}
