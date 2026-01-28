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

import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.dto.identity.BeneficiariesPartialDTO;
import com.iemr.common.dto.identity.IdentityEditDTO;
import com.iemr.common.model.beneficiary.BeneficiaryGenModel;
import com.iemr.common.utils.exception.IEMRException;

public interface IdentityBeneficiaryService {
	public List<BeneficiariesDTO> getBeneficiaryListByIDs(HashSet benIdList, String auth, Boolean is1097)
			throws IEMRException;

	String getIdentityResponse(String request, String auth, Boolean is1097) throws IEMRException;

	List<BeneficiariesPartialDTO> getPartialBeneficiaryListByIDs(HashSet benIdList, String auth, Boolean is1097)
			throws IEMRException;

	List<BeneficiariesDTO> getBeneficiaryListByPhone(String phoneNo, String auth, Boolean is1097) throws IEMRException,JsonProcessingException;

	List<BeneficiariesDTO> getBeneficiaryListByBenID(String benId, String auth, Boolean is1097) throws IEMRException;

	List<BeneficiariesDTO> getBeneficiaryListByBenRegID(Long benRegId, String auth, Boolean is1097)
			throws IEMRException;

	Integer editIdentityEditDTO(IdentityEditDTO identityEditDTO, String auth, Boolean is1097) throws IEMRException;

	List<BeneficiariesDTO> searchBeneficiaryList(String identitySearchDTO, String auth, Boolean is1097)
			throws IEMRException;

	Integer editIdentityEditDTOCommunityorEducation(IdentityEditDTO identityEditDTO, String auth, Boolean is1097)
			throws IEMRException;

	List<BeneficiaryGenModel> generateBeneficiaryIDs(String request, String auth) throws IEMRException;

	public List<BeneficiariesDTO> getBeneficiaryListByHealthID_ABHAAddress(String healthID, String auth, Boolean is1097)
			throws IEMRException;

	public List<BeneficiariesDTO> getBeneficiaryListByHealthIDNo_ABHAIDNo(String healthIDNo, String auth,
			Boolean is1097) throws IEMRException;

	public List<BeneficiariesDTO> getBeneficiaryListByFamilyId(String familyId, String auth, Boolean is1097)
			throws IEMRException;

	public List<BeneficiariesDTO> getBeneficiaryListByGovId(String identity, String auth, Boolean is1097)
			throws IEMRException;
}
