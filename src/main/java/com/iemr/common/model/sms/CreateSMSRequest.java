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
package com.iemr.common.model.sms;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateSMSRequest
{
	Integer smsTemplateID;
	@NotBlank(message = "SMS template name is required")
	@Size(min = 3, max = 100, message = "Template name must be between 3 and 100 characters")
	@Pattern(regexp = "^[a-zA-Z0-9_\\s-]+$", 
	         message = "Template name can only contain alphanumeric characters, spaces, hyphens and underscores")
	String smsTemplateName;
	@NotBlank(message = "SMS template content is required")
	@Size(min = 10, max = 500, message = "Template content must be between 10 and 500 characters")
	@Pattern(regexp = "^[^<>]*$", 
	         message = "Template cannot contain < or > characters")
	String smsTemplate;
	Integer smsTypeID;
	SMSTypeModel smsType;
	@NotNull(message = "Provider service map ID is required")
	@Positive(message = "Provider service map ID must be positive")
	Integer providerServiceMapID;
	String createdBy;
	@Valid
	@NotEmpty(message = "At least one SMS parameter is required")
	@Size(max = 20, message = "Maximum 20 parameters allowed")
	List<SMSParameterMapModel> smsParameterMaps;
}
