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
package com.iemr.common.repository.sms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.iemr.common.data.sms.SMSTemplate;

@Repository
public abstract interface SMSTemplateRepository extends CrudRepository<SMSTemplate, Integer>
{
	@Query("select smsTemplate from SMSTemplate smsTemplate where "
			+ "smsTemplate.providerServiceMapID = :providerServiceMapID "
			+ "order by smsTemplate.deleted, smsTemplate.smsTypeID ")
	public List<SMSTemplate> getSMSTemplateByProviderServiceMapIDOrderByDeletedSmsTypeIDDesc(
			@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select smsTemplate from SMSTemplate smsTemplate where "
			+ "smsTemplate.providerServiceMapID = :providerServiceMapID and "
			+ "smsTemplate.smsTypeID = :smsTypeID and smsTemplate.deleted<> true "
			+ "order by smsTemplate.deleted, smsTemplate.smsTypeID ")
	public List<SMSTemplate> getSMSTemplateByProviderServiceMapIDAndSMSTypeID(
			@Param("providerServiceMapID") Integer providerServiceMapID, @Param("smsTypeID") Integer smsTypeID);

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Modifying
	@Query("update SMSTemplate set deleted = :deleted where smsTemplateID = :smsTemplateID")
	public int updateSMSTemplate(@Param("smsTemplateID") Integer smsTemplateID, @Param("deleted") Boolean deleted);
	
	@Query("select smsTemplate.dltTemplateId from SMSTemplate smsTemplate where smsTemplate.smsTemplateID = :smsTemplateID and smsTemplate.deleted <> true")
	public String findDLTTemplateID(@Param("smsTemplateID") Integer smsTemplateID );

	public Optional<SMSTemplate> findBySmsTemplateName(String smsTemplateName);
	
	SMSTemplate findBySmsTemplateID(Integer smsTemplateID);

}
