package com.iemr.common.service.abdmfacility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.common.data.users.ProviderServiceAddressMapping;
import com.iemr.common.repository.abdmfacility.AbdmFacilityRepository;

@Service
public class AbdmFacilityServiceImpl implements AbdmFacilityService{
	
	@Autowired
	private AbdmFacilityRepository abdmFacilityRepo;
	
	@Override
	public String getMappedAbdmFacility(int psmId) {
		ProviderServiceAddressMapping res = abdmFacilityRepo.getAbdamFacility(psmId);
		return res.toString();
		
	}

}
