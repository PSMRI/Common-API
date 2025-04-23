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
import lombok.Data;

@Data
@Entity
@Table(name = "t_agentsummaryreport")
public class AgentSummaryReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "id", insertable = false)
	private Long id;

	@Expose
	@Column(name = "AgentName")
	private String agent_name;
	@Expose
	@Column(name = "AgentID")
	private Integer agent_id;
	@Expose
	@Column(name = "Agent_Identity")
	private String agent_identity;
	@Expose
	@Column(name = "CampaignName")
	private String campaign_name;
	@Expose
	@Column(name = "Totalcall")
	private Integer total_call;
	@Expose
	@Column(name = "Answd_Prev")
	private Integer answd_prev;
	@Expose
	@Column(name = "Answd_pro")
	private Integer answd_pro;
	@Expose
	@Column(name = "Answd_Pred")
	private Integer answd_pred;
	@Expose
	@Column(name = "Disp_Set")
	private Integer disp_set;
	@Expose
	@Column(name = "Disp_Notset")
	private Integer disp_not_set;
	@Expose
	@Column(name = "Calls_Held")
	private Integer calls_held;
	@Expose
	@Column(name = "TalkTime")
	private String talk_time;
	@Expose
	@Column(name = "Avg_TalkTime")
	private String avg_talk_time;
	@Expose
	@Column(name = "Preview_TalkTime")
	private String preview_talk_time;
	@Expose
	@Column(name = "Actual_TalkTime")
	private String actual_talk_time;
	@Expose
	@Column(name = "RingTime")
	private String ring_time;
	@Expose
	@Column(name = "Avg_RingTime")
	private String avg_ring_time;
	@Expose
	@Column(name = "HoldTime")
	private String hold_time;
	@Expose
	@Column(name = "Avg_HoldTime")
	private String avg_hold_time;
	@Expose
	@Column(name = "Call_HandledTime")
	private String call_handled_time;
	@Expose
	@Column(name = "Avg_Call_HandledTime")
	private String avg_call_handled_duration;
	@Expose
	@Column(name = "Wrap_Count")
	private Integer wrap_count;
	@Expose
	@Column(name = "Wrapuptime")
	private String wrapup_time;
	@Expose
	@Column(name = "Avg_Wrapuptime")
	private String avg_wrapup_time;
	@Expose
	@Column(name = "Total_Logintime")
	private String total_login_time;
	@Expose
	@Column(name = "Idletime")
	private String idle_time;
	@Expose
	@Column(name = "PauseDuration")
	private String pauseDuration;
	@Expose
	@Column(name = "Preview_Pauseduration")
	private String preview_pause_duration;
	@Expose
	@Column(name = "Readytime")
	private String ready_time;
	@Expose
	@Column(name = "Stafftime")
	private String staff_time;
	@Expose
	@Column(name = "Confduration")
	private String conf_duration;
	@Expose
	@Column(name = "Total_Previewtime")
	private String total_preview_time;
	@Expose
	@Column(name = "Pre_Idletime")
	private String pre_idle_time;
	@Expose
	@Column(name = "Starttime")
	private String start_time;
	@Expose
	@Column(name = "Endtime")
	private String end_time;
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
