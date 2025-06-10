package com.iemr.common.data.videocall;

import java.sql.Timestamp;

import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "t_videocallparameter") 
@Data
public class VideoCallParameters {
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "MeetingID")
	    Integer meetingID;

	    @Column(name = "DateOfCall")
	    Timestamp dateOfCall;

	    @Column(name = "CallerPhoneNumber")
	    String callerPhoneNumber;

	    @Column(name = "AgentID")
	    String agentID;

	    @Column(name = "AgentName")
	    String agentName;

	    @Column(name = "MeetingLink")
	    String meetingLink;

	    @Column(name = "CallStatus")
	    String callStatus;

	    @Column(name = "CallDuration")
	    String callDuration;

	    @Column(name = "ProviderServiceMapID")
	    Integer providerServiceMapID;

	    @Column(name = "BeneficiaryRegID")
    	private Long beneficiaryRegID;

	    @Column(name = "ClosureRemark")
	    String closureRemark;
	    
		@Column(name = "LinkGeneratedAt")
		private Timestamp linkGeneratedAt;

		@Column(name = "IsLinkUsed")
		private boolean linkUsed;

	    @Column(name = "Deleted", insertable = false, updatable = true)
		Boolean deleted;

		@Column(name = "CreatedBy", insertable = true, updatable = false)
		String createdBy;

		@Column(name = "CreatedDate", insertable = false, updatable = false)
		Timestamp createdDate;

		@Column(name = "ModifiedBy", insertable = false, updatable = true)
		String modifiedBy;

		@Column(name = "LastModDate", insertable = false, updatable = false)
		Timestamp lastModDate;
		
		@Override
		public String toString()
		{
			return OutputMapper.gsonWithoutExposeRestriction().toJson(this);
		}
}
