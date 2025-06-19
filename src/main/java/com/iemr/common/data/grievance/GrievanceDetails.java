package com.iemr.common.data.grievance;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.gson.annotations.Expose;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_grievanceworklist")
@Data
@NoArgsConstructor
public class GrievanceDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "GWID")
	private Long gwid;

	@Expose
	@Column(name = "Grievanceid")
	private Long grievanceId;

	@Expose
	@Column(name = "BeneficiaryRegID")
	private Long beneficiaryRegID;

	@Column(name = "BenCallid")
	@Expose
	private Long benCallID;

	@Column(name = "ProviderServiceMapID")
	@Expose
	private Integer providerServiceMapID;

	@Expose
	@Column(name = "ComplaintID")
	private String complaintID;

	@Expose
	@Column(name = "SubjectOfComplaint")
	private String subjectOfComplaint;

	@Expose
	@Column(name = "Complaint")
	private String complaint;

	@Expose
	@Column(name = "primaryNumber")
	private String primaryNumber;

	@Expose
	@Column(name = "Severety")
	@NotBlank(message = "Severety is required")
	private String severety;

	@Expose
	@Column(name = "Level")
	private Integer level;
	@Expose
	@Column(name = "State")
	private String state;

	@Expose
	@Column(name = "userid")
	private Integer userID;

	@Expose
	@Column(name = "isAllocated")
	private Boolean isAllocated = false;

	@Expose
	@Column(name = "retryNeeded")
	private Boolean retryNeeded;

	@Expose
	@Column(name = "isRegistered")
	private Boolean isRegistered = false;

	@Expose
	@Column(name = "callCounter")
	private Integer callCounter;

	@Expose
	@Column(name = "PreferredLanguageId")
	private Integer preferredLanguageId;

	@Expose
	@Column(name = "PreferredLanguage")
	private String preferredLanguage;
	
	@Expose
	@Column(name = "ComplaintResolution")
	private String complaintResolution;
	
	@Expose
	@Column(name = "Remarks")
	@Size(max = 5000, message = "Remarks cannot exceed 5000 characters")
	private String remarks;

	@Column(name = "Deleted", insertable = false, updatable = true)
	private Boolean deleted = false;

	@Expose
	@Column(name = "Processed")
	private Character processed = 'N';

	@Column(name = "CreatedBy")
	@Expose
	private String createdBy;

	@Expose
	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private Timestamp createdDate;

	@Column(name = "ModifiedBy")
	private String modifiedBy;

	@Column(name = "LastModDate", insertable = false, updatable = false)
	private Timestamp lastModDate;

	@Expose
	@Column(name = "VanSerialNo")
	private Long vanSerialNo;
	@Expose
	@Column(name = "VanID")
	private Integer vanID;

	@Expose
	@Column(name = "VehicalNo")
	private String vehicalNo;
	@Expose
	@Column(name = "ParkingPlaceID")
	private Integer parkingPlaceID;
	@Expose
	@Column(name = "SyncedBy")
	private String syncedBy;

	@Expose
	@Column(name = "SyncedDate")
	private Timestamp syncedDate;

	@Expose
	@Column(name = "isCompleted")
	private Boolean isCompleted = false;

	@OneToMany(mappedBy = "grievanceDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<GrievanceTransaction> grievanceTransactionDetails;

	public GrievanceDetails(Long gwid, Long grievanceId, Long beneficiaryRegID, Long benCallID,
			Integer providerServiceMapID, String complaintID, String subjectOfComplaint, String complaint,
			String primaryNumber, String severety, Integer level, String state, Integer userid, Boolean isAllocated,
			Boolean retryNeeded, Boolean isRegistered, Integer callCounter, Integer preferredLanguageId,
			String preferredLanguage, String complaintResolution, String remarks, Boolean deleted, Character processed, String createdBy, Timestamp createdDate,
			String modifiedBy, Timestamp lastModDate, Long vanSerialNo, Integer vanID, String vehicalNo,
			Integer parkingPlaceID, String syncedBy, Timestamp syncedDate, Boolean isCompleted) {
		super();
		this.gwid = gwid;
		this.grievanceId = grievanceId;
		this.beneficiaryRegID = beneficiaryRegID;
		this.benCallID = benCallID;
		this.providerServiceMapID = providerServiceMapID;
		this.complaintID = complaintID;
		this.subjectOfComplaint = subjectOfComplaint;
		this.complaint = complaint;
		this.primaryNumber = primaryNumber;
		this.severety = severety;
		this.level = level;
		this.state = state;
		this.userID = userid;
		this.isAllocated = isAllocated;
		this.retryNeeded = retryNeeded;
		this.isRegistered = isRegistered;
		this.callCounter = callCounter;
		this.preferredLanguageId = preferredLanguageId;
		this.preferredLanguage = preferredLanguage;
		this.complaintResolution = complaintResolution;
		this.remarks = remarks;
		this.deleted = deleted;
		this.processed = processed;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.modifiedBy = modifiedBy;
		this.lastModDate = lastModDate;
		this.vanSerialNo = vanSerialNo;
		this.vanID = vanID;
		this.vehicalNo = vehicalNo;
		this.parkingPlaceID = parkingPlaceID;
		this.syncedBy = syncedBy;
		this.syncedDate = syncedDate;
		this.isCompleted = isCompleted;
	}

}
