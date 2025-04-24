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
@Table(name = "t_nhmagentrealtimedata")
public class AgentRealTimeData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "id", insertable = false)
	private Long id;
	
	@Expose
	@Column(name="CampaignName")
	private String campaignName;
	
	@Expose
	@Column(name="Loggedin")
	private int loggedIn;
	
	@Expose
	@Column(name="Free")
	private int free;
	
	@Expose
	@Column(name="Incall")
	private int inCall;
	@Expose
	@Column(name="AWT")
	private int awt;
	
	@Expose
	@Column(name="Hold")
	private int hold;
	
	@Expose
	@Column(name="NotReady")
	private int notReady;
	
	@Expose
	@Column(name="Aux")
	private int aux;
	
	@Expose
	@Column(name="Deleted",insertable = false, updatable = true)
	private Boolean deleted;
	
	@Expose
	@Column(name = "Processed", insertable = false)
	private String processed;
	
	@Expose
	@Column(name="CreatedBy")
	private String createdBy;
	
	@Expose
	@Column(name="CreatedDate")
	private Timestamp createdDate;
	
	@Expose
	@Column(name="ModifiedBy")
	private String modifiedBy;
	
	@Expose
	@Column(name="LastModDate")
	private Timestamp modifiedDate;

}
