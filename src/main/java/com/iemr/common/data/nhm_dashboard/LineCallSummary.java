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
package com.iemr.common.data.nhm_dashboard;

import java.sql.Time;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_linecallsummary")
public class LineCallSummary {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false)
    private Long id;     
    @Column(name = "LineNumber")
    private String lineNumber;     
    @Column(name = "CampaignName")
    private String campaignName;    
    @Column(name = "TotalCalls")
    private Integer totalCalls;
    @Column(name = "TotalTalkTime")
    private Time totalTalkTime;
    @Column(name = "AverageTalkTime")
    private Time averageTalkTime;    
    @Column(name = "Deleted", insertable = false, updatable = true)
    private Boolean deleted;
    @Column(name = "Processed")
	private String processed;
    @Column(name = "CreatedBy", updatable = false)
    private String createdBy;     
    @Column(name = "CreatedDate", insertable = false, updatable = false)
    private Timestamp createdDate;     
    @Column(name = "ModifiedBy", insertable = false, updatable = true)
    private String modifiedBy;     
    @Column(name = "LastModDate", insertable = false, updatable = false)
    private Timestamp lastModDate;


}
