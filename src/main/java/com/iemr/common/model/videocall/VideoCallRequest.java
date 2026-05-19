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

package com.iemr.common.model.videocall;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;
import lombok.Data;
import com.google.gson.Gson;

@Data
public class VideoCallRequest {
	private Timestamp dateOfCall;
	private String callerPhoneNumber;
	private String agentID;
	private String agentName;
	private String meetingLink;
	private String callStatus;
	private String callDuration;
	private Integer providerServiceMapID;
	private Long beneficiaryRegID;
	private String closureRemark;
    private boolean isLinkUsed;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Timestamp linkGeneratedAt;

	public String toJson() {
        return new Gson().toJson(this); 
    }
}
