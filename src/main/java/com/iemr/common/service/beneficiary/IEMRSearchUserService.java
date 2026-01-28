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

import java.util.List;

import com.iemr.common.data.beneficiary.BenPhoneMap;
import com.iemr.common.data.beneficiary.Beneficiary;
import com.iemr.common.model.beneficiary.BeneficiaryModel;

public interface IEMRSearchUserService {

	List<BeneficiaryModel> userExitsCheckWithId(Long beneficiaryId, String auth, Boolean is1097) throws Exception;

	List<Beneficiary> findByBeneficiaryRegID(Beneficiary beneficiary);

	String findByBeneficiaryPhoneNo(BenPhoneMap benPhoneMap, Integer pageNo, Integer rows, String auth)
			throws Exception;

	String findBeneficiary(BeneficiaryModel request, String auth) throws Exception;

	List<BeneficiaryModel> userExitsCheckWithId(String beneficiaryID, String auth, Boolean is1097) throws Exception;

	public List<BeneficiaryModel> userExitsCheckWithHealthId_ABHAId(String healthID, String auth, Boolean is1097)
			throws Exception;

	public List<BeneficiaryModel> userExitsCheckWithHealthIdNo_ABHAIdNo(String healthIDNo, String auth, Boolean is1097)
			throws Exception;

	public List<BeneficiaryModel> userExitsCheckWithFamilyId(String familyId, String auth, Boolean is1097)
			throws Exception;

	public List<BeneficiaryModel> userExitsCheckWithGovIdentity(String identity, String auth, Boolean is1097)
			throws Exception;

}
