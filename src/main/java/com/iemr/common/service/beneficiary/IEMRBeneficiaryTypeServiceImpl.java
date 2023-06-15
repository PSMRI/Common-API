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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.common.data.beneficiary.BeneficiaryType;
import com.iemr.common.repository.beneficiary.IEMRBeneficiaryTypeRepository;

@Service
public class IEMRBeneficiaryTypeServiceImpl implements IEMRBeneficiaryTypeService{

	@Autowired
	private IEMRBeneficiaryTypeRepository iemrBeneficiaryTypeRepository;
	
	@Override
	public BeneficiaryType addRelation(BeneficiaryType i_beneficiaryType) {
		return iemrBeneficiaryTypeRepository.save(i_beneficiaryType);
	}

	@Override
	public Iterable<BeneficiaryType> getRelations() {
		return iemrBeneficiaryTypeRepository.findAll();
	}
	
}
