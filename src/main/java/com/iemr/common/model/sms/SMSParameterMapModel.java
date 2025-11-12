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

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SMSParameterMapModel
{
	Integer smsParameterID;
	Integer smsTemplateID;
	String createdBy;
	String modifiedBy;
	@Size(max = 200, message = "Parameter value must not exceed 200 characters")
	@Pattern(regexp = "^[^<>\"';&|`$(){}\\[\\]]*$", 
	         message = "Parameter value contains invalid characters")
	String smsParameterValue;
	@NotBlank(message = "Parameter name is required")
	@Size(min = 2, max = 50, message = "Parameter name must be between 2 and 50 characters")
	@Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", 
	         message = "Parameter name must start with a letter and contain only alphanumeric and underscore")
	String smsParameterName;
	String smsParameterType;
}
