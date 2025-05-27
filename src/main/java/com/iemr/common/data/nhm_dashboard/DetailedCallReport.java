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

import java.sql.Timestamp;

import com.google.gson.annotations.Expose;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "t_DetailedCallReport")
public class DetailedCallReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "id", insertable = false)
	private Long id;
	@Expose
	@Column(name = "AgentName")
	private String Agent_Name;
	@Expose
	@Column(name = "AgentID")
	private Integer Agent_ID;
	@Expose
	@Column(name = "Uniqueid")
	private String Unique_id;
	@Expose
	@Column(name = "Campaign_Name")
	private String Campaign_Name;
	@Expose
	@Column(name = "List_Name")
	private String List_Name;
	@Expose
	@Column(name = "Customer_Name")
	private String Customer_Name;
	@Expose
	@Column(name = "PHONE")
	private String PHONE;
	@Expose
	@Column(name = "Zone")
	private String Zone;
	@Expose
	@Transient
	private String Call_Start_Time;
	@Expose
	@Transient
	private String Call_End_Time;

	@Expose
	@Column(name = "Call_Start_Time")
	private Timestamp callStartTime;
	@Expose
	@Column(name = "Call_End_Time")
	private Timestamp callEndTime;

	@Expose
	@Column(name = "Call_Status")
	private String Call_Status;
	@Expose
	@Column(name = "Remarks")
	private String Remarks;
	@Expose
	@Column(name = "SessionID")
	private String Session_ID;
	@Expose
	@Column(name = "Monitor_File_Path")
	private String Monitor_File_Path;
	@Expose
	@Column(name = "Monitor_File_Name")
	private String Monitor_File_Name;
	@Expose
	@Column(name = "Agent_Disposition")
	private String Agent_Disposition;
	@Expose
	@Column(name = "Queue_time")
	private Integer queue_time;
	@Expose
	@Column(name = "Wrapup_time")
	private Integer Wrapup_time;
	@Expose
	@Column(name = "Agent_Disposition_Category")
	private String Agent_Disposition_Category;
	@Expose
	@Column(name = "Call_Skill")
	private String Call_Skill;
	@Expose
	@Column(name = "DID_Number")
	private String DID_Number;
	@Expose
	@Column(name = "Hold_time")
	private Integer Hold_time;
	@Expose
	@Column(name = "Orientation_Type")
	private String Orientation_Type;
	@Expose
	@Column(name = "Transferred_To")
	private String Transferred_To;
	@Expose
	@Column(name = "Transferred_from")
	private String Transferred_from;
	@Expose
	@Column(name = "Ring_Start_Time")
	private String Ring_Start_Time;
	@Expose
	@Column(name = "Ring_End_Time")
	private String Ring_End_Time;

	@Expose
	@Transient
	private String Agent_Ring_Start_Time;
	@Expose
	@Transient
	private String Agent_Ring_End_Time;

	@Expose
	@Column(name = "Agent_Ring_Start_Time")
	private Timestamp Agent_Ring_Start_Time_T;
	@Expose
	@Column(name = "Agent_Ring_End_Time")
	private Timestamp Agent_Ring_End_Time_T;

	@Expose
	@Column(name = "Hold_Num_Times")
	private Integer Hold_Num_Times;
	@Expose
	@Column(name = "CallerId")
	private String Caller_Id;
	@Expose
	@Column(name = "Channel")
	private String Channel;
	@Expose
	@Column(name = "Call_Duration")
	private Integer Call_Duration;
	@Expose
	@Column(name = "Wait_Time")
	private Integer Wait_Time;
	@Expose
	@Column(name = "Call_Type")
	private String Call_Type;
	@Expose
	@Column(name = "Redial_Flag")
	private String Redial_Flag;
	@Expose
	@Column(name = "Custom_Monitor_File_Name")
	private String Custom_Monitor_File_Name;
	@Expose
	@Column(name = "Next_Call_Time")
	private Integer Next_Call_Time;
	@Expose
	@Column(name = "phone_type")
	private String phone_type;
	@Expose
	@Column(name = "ivrs_path")
	private String ivrs_path;
	@Expose
	@Column(name = "Disconnected_By")
	private String Disconnected_By;
	@Expose
	@Column(name = "Ringing")
	private String Ringing;
	@Expose
	@Column(name = "Total_Attempt")
	private Integer Total_Attempt;
	@Expose
	@Column(name = "Total_Phone_Attempt")
	private Integer Total_Phone_Attempt;
	@Expose
	@Column(name = "Dialer_Action")
	private String Dialer_Action;
	@Expose
	@Column(name = "Ring_Duration")
	private Integer Ring_Duration;
	@Expose
	@Column(name = "Actual_Talk_Time")
	private Integer Actual_Talk_Time;
	@Expose
	@Column(name = "Final_Call_Status")
	private String Final_Call_Status;
	@Expose
	@Column(name = "Failure_Reason")
	private Integer Failure_Reason;
	@Expose
	@Column(name = "Agent_Ring_Duration")
	private Integer Agent_Ring_Duration;
	@Expose
	@Column(name = "Wrapped_By")
	private String Wrapped_By;
	@Expose
	@Column(name = "Deleted", insertable = false)
	private Boolean deleted;
	@Expose
	@Column(name = "Processed", insertable = false)
	private String processed;
	@Expose
	@Column(name = "CreatedBy")
	private String createdBy = "default";
	@Expose
	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private Timestamp createdDate;
	@Expose
	@Column(name = "ModifiedBy")
	private String modifiedBy = "default";
	@Expose
	@Column(name = "LastModDate", insertable = false, updatable = false)
	private Timestamp lastModDate;
}
