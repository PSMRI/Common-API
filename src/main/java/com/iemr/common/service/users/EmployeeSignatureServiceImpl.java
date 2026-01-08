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
package com.iemr.common.service.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.common.data.users.EmployeeSignature;
import com.iemr.common.repository.users.EmployeeSignatureRepo;

@Service
public class EmployeeSignatureServiceImpl implements EmployeeSignatureService {

	@Autowired
	EmployeeSignatureRepo employeeSignatureRepo;

	

	@Override
	public EmployeeSignature fetchSignature(Long userSignID) {
		// TODO Auto-generated method stub
		return employeeSignatureRepo.findOneByUserID(userSignID);
	}

	@Override
	public EmployeeSignature fetchActiveSignature(Long userSignID) {
		// New method - fetches only non-deleted records
		return employeeSignatureRepo.findOneByUserID(userSignID);
	}

	public Boolean existSignature(Long userID) {
		// TODO Auto-generated method stub
		return employeeSignatureRepo.countByUserIDAndSignatureNotNull(userID)>0;
	}

}
