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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iemr.common.data.beneficiary.BenDemographics;
import com.iemr.common.data.beneficiary.BenPhoneMap;
import com.iemr.common.data.beneficiary.Beneficiary;
import com.iemr.common.data.beneficiary.SexualOrientation;
import com.iemr.common.data.userbeneficiarydata.Gender;
import com.iemr.common.data.userbeneficiarydata.MaritalStatus;
import com.iemr.common.data.userbeneficiarydata.Status;
import com.iemr.common.data.userbeneficiarydata.Title;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.dto.identity.IdentitySearchDTO;
import com.iemr.common.mapper.BenCompleteDetailMapper;
import com.iemr.common.mapper.BenPhoneMapperDecorator;
import com.iemr.common.mapper.CommunityMapper;
import com.iemr.common.mapper.DistrictBlockMapper;
import com.iemr.common.mapper.DistrictBranchMapper;
import com.iemr.common.mapper.DistrictMapper;
import com.iemr.common.mapper.EducationMapper;
import com.iemr.common.mapper.GenderMapper;
import com.iemr.common.mapper.GovtIdentityTypeMapper;
import com.iemr.common.mapper.HealthCareWorkerMapper;
import com.iemr.common.mapper.IdentityBenEditMapper;
import com.iemr.common.mapper.MaritalStatusMapper;
import com.iemr.common.mapper.RelationshipMapper;
import com.iemr.common.mapper.SexualOrientationMapper;
import com.iemr.common.mapper.StateMapper;
import com.iemr.common.mapper.TitleMapper;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.repository.beneficiary.BeneficiaryRelationshipTypeRepository;
import com.iemr.common.repository.beneficiary.CommunityRepository;
import com.iemr.common.repository.beneficiary.EducationRepository;
import com.iemr.common.repository.beneficiary.GovtIdentityTypeRepository;
import com.iemr.common.repository.location.LocationDistrictBlockRepository;
import com.iemr.common.repository.location.LocationDistrictRepository;
import com.iemr.common.repository.location.LocationDistrilctBranchRepository;
import com.iemr.common.repository.location.LocationStateRepository;
import com.iemr.common.repository.userbeneficiarydata.GenderRepository;
import com.iemr.common.repository.userbeneficiarydata.MaritalStatusRepository;
import com.iemr.common.repository.userbeneficiarydata.SexualOrientationRepository;
import com.iemr.common.repository.userbeneficiarydata.TitleRepository;
import com.iemr.common.utils.mapper.OutputMapper;

/**
 * 
 */
@Service
public class IEMRSearchUserServiceImpl implements IEMRSearchUserService {

	// private InputMapper inputMapper = new InputMapper();
	// private ExecutorService executor = Executors.newFixedThreadPool(3);

	// private BeneficiaryRepository iemrSearchBeneficiaryRepo;
	@Autowired
	private IdentityBenEditMapper identityBenEditMapper;
	@Autowired
	private BenCompleteDetailMapper benCompleteMapper;
	@Autowired
	private IdentityBeneficiaryService identityBeneficiaryService;

	@Autowired
	LocationStateRepository locationStateRepo;
	@Autowired
	StateMapper stateMapper;

	@Autowired
	LocationDistrictRepository locationDistrictRepository;

	@Autowired
	DistrictMapper districtMapper;
	@Autowired
	LocationDistrictBlockRepository blockRepository;
	@Autowired
	DistrictBlockMapper blockMapper;
	@Autowired
	LocationDistrilctBranchRepository branchRepository;
	@Autowired
	DistrictBranchMapper branchMapper;
	@Autowired
	EducationRepository educationRepository;
	@Autowired
	EducationMapper educationMapper;
	@Autowired
	CommunityRepository communityRepository;
	@Autowired
	CommunityMapper communityMapper;
	@Autowired
	BeneficiaryRelationshipTypeRepository relationshipRepository;
	@Autowired
	RelationshipMapper relationshipMapper;

	@Autowired
	GenderRepository genderRepository;
	@Autowired
	GenderMapper genderMapper;
	@Autowired
	TitleRepository titleRepository;
	@Autowired
	TitleMapper titleMapper;
	@Autowired
	MaritalStatusRepository maritalStatusRepository;
	@Autowired
	MaritalStatusMapper maritalStatusMapper;
	@Autowired
	SexualOrientationRepository sexualOrientationRepository;

	@Autowired
	SexualOrientationMapper sexualOrientationMapper;
	@Autowired
	GovtIdentityTypeRepository govtIdentityTypeRepository;
	@Autowired
	GovtIdentityTypeMapper govtIdentityTypeMapper;
	@Autowired
	HealthCareWorkerMapper healthCareWorkerMapper;
	@Autowired
	BeneficiaryRelationshipTypeRepository beneficiaryRelationshipTypeRepository;
	@Autowired
	BenPhoneMapperDecorator benPhoneMapper;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	// search patient by ben reg id
	@Override
	public List<BeneficiaryModel> userExitsCheckWithId(Long beneficiaryRegID, String auth, Boolean is1097)
			throws Exception {
		// result list
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		// search patient by ben reg id, call Identity API
		List<BeneficiariesDTO> listBen = identityBeneficiaryService.getBeneficiaryListByBenRegID(beneficiaryRegID, auth,
				is1097);

		beneficiaryList = getBeneficiaryListFromMapper(listBen);
		for (BeneficiaryModel beneficiaryModel : beneficiaryList) {
			addCreatedDateToOtherFields(beneficiaryModel);
		}
		return beneficiaryList;
	}

	// search patient by ben id
	@Override
	public List<BeneficiaryModel> userExitsCheckWithId(String beneficiaryID, String auth, Boolean is1097)
			throws Exception {
		// result list
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		// search patient by ben id, call Identity API
		List<BeneficiariesDTO> listBen = identityBeneficiaryService.getBeneficiaryListByBenID(beneficiaryID, auth,
				is1097);

		beneficiaryList = getBeneficiaryListFromMapper(listBen);
		for (BeneficiaryModel beneficiaryModel : beneficiaryList) {
			addCreatedDateToOtherFields(beneficiaryModel);
		}
		return beneficiaryList;
	}

	private void addCreatedDateToOtherFields(BeneficiaryModel beneficiaryModel) {
		if (beneficiaryModel.getCreatedDate() != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				// Parse the existing otherFields JSON string into a JsonNode
				JsonNode otherFieldsNode = objectMapper.readTree(beneficiaryModel.getOtherFields());

				// Convert createdDate to a string
				String createdDateString = beneficiaryModel.getCreatedDate().toString(); 

				// Add createdDate to the JSON node
				((ObjectNode) otherFieldsNode).put("createdDate", createdDateString);

				// Convert the JsonNode back to a string and set it in the model
				beneficiaryModel.setOtherFields(objectMapper.writeValueAsString(otherFieldsNode));
			} catch (Exception e) {
				logger.error("Error processing otherFields JSON: " + e.getMessage(), e);
			}
		}
	}

	// search patient by healthid / ABHA ID
	@Override
	public List<BeneficiaryModel> userExitsCheckWithHealthId_ABHAId(String healthID, String auth, Boolean is1097)
			throws Exception {
		// result list
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		// search patient by ben id, call Identity API
		List<BeneficiariesDTO> listBen = null;
		if(healthID.contains("@")) {
			listBen = identityBeneficiaryService.getBeneficiaryListByHealthID_ABHAAddress(healthID,
					auth, is1097);
		}else {
			String healthIdNumber = getHealthId(healthID);
			listBen = identityBeneficiaryService.getBeneficiaryListByHealthIDNo_ABHAIDNo(healthIdNumber, auth, is1097);
		}
		beneficiaryList = getBeneficiaryListFromMapper(listBen);
		for (BeneficiaryModel beneficiaryModel : beneficiaryList) {
			addCreatedDateToOtherFields(beneficiaryModel);
		}
		return beneficiaryList;
	}
	private String getHealthId(String healthID) {
		String healthIdNumber = null;
		if (null != healthID) {
			if (healthID.length() == 17) {
				healthIdNumber = healthID;
			} else {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(healthID.substring(0, 2)).append("-");
				stringBuilder.append(healthID.substring(2, 6)).append("-");
				stringBuilder.append(healthID.substring(6, 10)).append("-");
				stringBuilder.append(healthID.substring(10));
				String formattedHealthID = stringBuilder.toString();
				healthIdNumber = formattedHealthID;
			}
		}
		return healthIdNumber;
	}
	// search patient by healthidNo / ABHA Id No
	@Override
	public List<BeneficiaryModel> userExitsCheckWithHealthIdNo_ABHAIdNo(String healthIDNo, String auth, Boolean is1097)
			throws Exception {
		// result list
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		String healthId = getHealthId(healthIDNo);
		List<BeneficiariesDTO> listBen = identityBeneficiaryService.getBeneficiaryListByHealthIDNo_ABHAIDNo(healthId,
				auth, is1097);

		beneficiaryList = getBeneficiaryListFromMapper(listBen);
		for (BeneficiaryModel beneficiaryModel : beneficiaryList) {
			addCreatedDateToOtherFields(beneficiaryModel);
		}
		return beneficiaryList;
	}

	// search patient by family id
	@Override
	public List<BeneficiaryModel> userExitsCheckWithFamilyId(String familyId, String auth, Boolean is1097)
			throws Exception {
		// result list
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		// search patient by ben id, call Identity API
		List<BeneficiariesDTO> listBen = identityBeneficiaryService.getBeneficiaryListByFamilyId(familyId, auth,
				is1097);

		beneficiaryList = getBeneficiaryListFromMapper(listBen);
		return beneficiaryList;
	}

	// search patient by gov identity
	@Override
	public List<BeneficiaryModel> userExitsCheckWithGovIdentity(String identity, String auth, Boolean is1097)
			throws Exception {
		// result list
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		// search patient by ben id, call Identity API
		List<BeneficiariesDTO> listBen = identityBeneficiaryService.getBeneficiaryListByGovId(identity, auth, is1097);

		beneficiaryList = getBeneficiaryListFromMapper(listBen);
		return beneficiaryList;
	}

	// search patient by phone no
	@Override
	public String findByBeneficiaryPhoneNo(BenPhoneMap benPhoneMap, Integer pageNo, Integer rows, String auth)
			throws Exception {
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		// search patient by phone no
		List<BeneficiariesDTO> listBen = identityBeneficiaryService.getBeneficiaryListByPhone(benPhoneMap.getPhoneNo(),
				auth, benPhoneMap.getIs1097());

		beneficiaryList = getBeneficiaryListFromMapper(listBen);
		for (BeneficiaryModel beneficiaryModel : beneficiaryList) {
			addCreatedDateToOtherFields(beneficiaryModel);
		}
		setBeneficiaryGender(beneficiaryList);
		logger.info("Serach user by phone no response size "
				+ (beneficiaryList != null ? beneficiaryList.size() : "No Beneficiary Found"));
		ObjectMapper mapper = new ObjectMapper();
		String result = mapper.writeValueAsString(beneficiaryList);
		return result;
	}

	private void setBeneficiaryGender(List<BeneficiaryModel> iBeneficiary) {
		for (BeneficiaryModel beneficiaryModel : iBeneficiary) {
			if (null != beneficiaryModel.getM_gender() && beneficiaryModel.getM_gender().getGenderName() != null)
				beneficiaryModel.setGenderName(beneficiaryModel.getM_gender().getGenderName());
		}

	}

	// Advance search
	@Override
	public String findBeneficiary(BeneficiaryModel i_beneficiary, String auth) throws Exception {

		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();

		IdentitySearchDTO identitySearchDTO = identityBenEditMapper.getidentitysearchModel(i_beneficiary);

		if (i_beneficiary.getDOB() != null)
			identitySearchDTO.setDob(i_beneficiary.getDOB());

		if (i_beneficiary.getHouseHoldID() != null)
			identitySearchDTO.setHouseHoldID(i_beneficiary.getHouseHoldID());

		if (i_beneficiary.getIsD2D() != null)
			identitySearchDTO.setIsD2D(i_beneficiary.getIsD2D());

		if (i_beneficiary.getBenPhoneMaps() != null && i_beneficiary.getBenPhoneMaps().size() > 0) {
			identitySearchDTO.setContactNumber(i_beneficiary.getBenPhoneMaps().get(0).getPhoneNo());
		}
		logger.info("searchBeneficiary request common going to identity" + identitySearchDTO);
		if (i_beneficiary.getBeneficiaryID() != null && i_beneficiary.getBeneficiaryID() != "") {
			BigInteger numBig = new BigInteger(i_beneficiary.getBeneficiaryID());
			identitySearchDTO.setBeneficiaryId(numBig);
		}

		if (i_beneficiary.getIs1097() != null && i_beneficiary.getIs1097() == true) {
			i_beneficiary.setIs1097(true);
		} else {
			i_beneficiary.setIs1097(false);
		}

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		List<BeneficiariesDTO> listBen = identityBeneficiaryService
				.searchBeneficiaryList(gson.toJson(identitySearchDTO), auth, i_beneficiary.getIs1097());

		beneficiaryList = getBeneficiaryListFromMapper(listBen);

		logger.info("searchUserByParameters response size: "
				+ (beneficiaryList != null ? beneficiaryList.size() : "No Beneficiary Found"));
		return OutputMapper.gson().toJson(beneficiaryList);
	}

	// get response mapper
	public List<BeneficiaryModel> getBeneficiaryListFromMapper(List<BeneficiariesDTO> benDetailForOutboundDTOList) {
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();

		benDetailForOutboundDTOList.forEach(beneficiaryModel -> {

			BeneficiaryModel beneficiary = benCompleteMapper.benDetailForOutboundDTOToIBeneficiary(beneficiaryModel);
			if(null != beneficiaryModel && null != beneficiaryModel.getBeneficiaryDetails()) {
				beneficiary.setCommunityName(beneficiaryModel.getBeneficiaryDetails().getCommunity());
				beneficiary.setReligion(beneficiaryModel.getBeneficiaryDetails().getReligion());
				beneficiary.setReligionName(beneficiaryModel.getBeneficiaryDetails().getReligion());
				beneficiary.setOccupationName(beneficiaryModel.getBeneficiaryDetails().getOccupation());
				beneficiary.setOccupation(beneficiaryModel.getBeneficiaryDetails().getOccupation());
				beneficiary.setEducation(beneficiaryModel.getBeneficiaryDetails().getEducation());
				beneficiary.setIncomeStatus(beneficiaryModel.getBeneficiaryDetails().getIncomeStatus());
			}
			if (beneficiary.getAge() == null) {
				beneficiary.setAge(beneficiary.getActualAge());
			}
			beneficiary.setBenPhoneMaps(benPhoneMapper.benPhoneMapToResponseByID(beneficiaryModel));
			beneficiary.setSexualOrientation(
					sexualOrientationMapper.sexualOrientationByIDToModel(beneficiary.getSexualOrientationID()));
			beneficiary.setGovtIdentityType(
					govtIdentityTypeMapper.govtIdentityTypeModelByIDToModel(beneficiary.getGovtIdentityTypeID()));

			beneficiary.setI_bendemographics(benCompleteMapper.createBenDemographicsModel(beneficiaryModel));
			beneficiary.getI_bendemographics().setHealthCareWorkerType(healthCareWorkerMapper
					.getModelByWorkerID(beneficiary.getI_bendemographics().getHealthCareWorkerID()));
			beneficiary.setM_gender(
					genderMapper.genderByIDToLoginResponse(beneficiaryModel.getBeneficiaryDetails().getGenderId()));
			beneficiary.setMaritalStatus(maritalStatusMapper
					.maritalStatusByIDToResponse(beneficiaryModel.getBeneficiaryDetails().getMaritalStatusId()));
			beneficiary
					.setM_title(titleMapper.titleByIDToResponse(beneficiaryModel.getBeneficiaryDetails().getTitleId()));
			beneficiary.setBeneficiaryIdentities(govtIdentityTypeMapper
					.govtIdentityTypeModelsByIDToModel(beneficiaryModel.getBeneficiaryIdentites()));

			beneficiary.setAbhaDetails(beneficiaryModel.getAbhaDetails());

			beneficiaryList.add(beneficiary);
		});

		beneficiaryList.removeIf(Objects::isNull);
		// Collections.sort(beneficiaryList);
		return beneficiaryList;
	}

	// ABHA address & Abha id no based search
	// private String searchByAbhaAddress

	// Deprecated, 07-03-2022
	@Deprecated
	private Beneficiary prepareBeneficiaryData(Object[] i_Beneficiary) {
		Beneficiary beneficiary = new Beneficiary((Long) i_Beneficiary[0], (String) i_Beneficiary[1],
				(String) i_Beneficiary[2], (String) i_Beneficiary[3], (String) i_Beneficiary[4],
				(Gender) i_Beneficiary[5], (Timestamp) i_Beneficiary[6], (Title) i_Beneficiary[7],
				(Status) i_Beneficiary[8], (MaritalStatus) i_Beneficiary[9], (SexualOrientation) i_Beneficiary[10],
				(String) i_Beneficiary[11], (String) i_Beneficiary[12], null, (String) i_Beneficiary[13],
				(Integer) i_Beneficiary[14], (Integer) i_Beneficiary[15], (Boolean) i_Beneficiary[16],
				(Integer) i_Beneficiary[17], (Integer) i_Beneficiary[18], (Integer) i_Beneficiary[19],
				(Integer) i_Beneficiary[20], (Integer) i_Beneficiary[21], (String) i_Beneficiary[22],
				(String) i_Beneficiary[23], (String) i_Beneficiary[24], (String) i_Beneficiary[25]);
		Long beneficiaryId = (Long) i_Beneficiary[0];
		if (beneficiary.getBenPhoneMaps() == null) {
			beneficiary.setBenPhoneMaps(getBenPhoneMapByID(beneficiaryId));
		}
		if (beneficiary.getI_bendemographics() == null) {
			beneficiary.setI_bendemographics(getBenDemographicsByID(beneficiaryId));
		}
		return beneficiary;
	}

	@Deprecated
	private BenDemographics getBenDemographicsByID(Long beneficiaryRegID) {
		BenDemographics benDemographics = new BenDemographics();
		return benDemographics;
	}

	@Deprecated
	private List<BenPhoneMap> getBenPhoneMapByID(Long beneficiaryRegID) {
		List<BenPhoneMap> benDemographics = new ArrayList<BenPhoneMap>();
		return benDemographics;
	}

	@Deprecated
	private List<BenPhoneMap> getBenPhoneMap(Long beneficiaryRegID, String phoneNo) {

		List<BenPhoneMap> benDemographics = new ArrayList<BenPhoneMap>();

		return benDemographics;
	}

	@Deprecated
	@Override
	public List<Beneficiary> findByBeneficiaryRegID(Beneficiary beneficiary) {
		List<Beneficiary> beneficiaryList = new ArrayList<Beneficiary>();

		return beneficiaryList;
	}

}
