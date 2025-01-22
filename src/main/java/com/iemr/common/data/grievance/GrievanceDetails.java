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
import lombok.Data;

@Entity
@Table(name = "t_grievanceworklist")
@Data
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
	private String level;	
	@Expose
	@Column(name = "State")
	private String state;
	
	@Expose
	@Column(name = "Agentid")
	private String agentid;	
	
	@Expose
	@Column(name = "userid")
	private String userid;	
	
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
	private Integer vanSerialNo;
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
			String primaryNumber, String severety, String state, String agentID, String userid, Boolean isAllocated,
			Boolean retryNeeded, Boolean isRegistered, Integer callCounter, Boolean deleted, Character processed,
			String createdBy, Timestamp createdDate, String modifiedBy, Timestamp lastModDate, Integer vanSerialNo,
			Integer vanID, String vehicalNo, Integer parkingPlaceID, String syncedBy, Timestamp syncedDate,
			Boolean isCompleted) {
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
		this.state = state;
		this.agentid = agentID;
		this.userid = userid;
		this.isAllocated = isAllocated;
		this.retryNeeded = retryNeeded;
		this.isRegistered = isRegistered;
		this.callCounter = callCounter;
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
	
	  // Getters and Setters
    public Long getGwid() {
        return gwid;
    }

    public void setGwid(Long gwid) {
        this.gwid = gwid;
    }

    public Long getGrievanceId() {
        return grievanceId;
    }

    public void setGrievanceId(Long grievanceId) {
        this.grievanceId = grievanceId;
    }

    public Long getBeneficiaryRegId() {
        return beneficiaryRegID;
    }

    public void setBeneficiaryRegId(Long beneficiaryRegId) {
        this.beneficiaryRegID = beneficiaryRegId;
    }

    public Long getBencallId() {
        return benCallID;
    }

    public void setBencallId(Long bencallId) {
        this.benCallID = bencallId;
    }

    public Integer getProviderServiceMapId() {
        return providerServiceMapID;
    }

    public void setProviderServiceMapId(Integer providerServiceMapId) {
        this.providerServiceMapID = providerServiceMapId;
    }

    public String getComplaintId() {
        return complaintID;
    }

    public void setComplaintId(String complaintId) {
        this.complaintID = complaintId;
    }

    public String getSubjectOfComplaint() {
        return subjectOfComplaint;
    }

    public void setSubjectOfComplaint(String subjectOfComplaint) {
        this.subjectOfComplaint = subjectOfComplaint;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public String getPrimaryNumber() {
        return primaryNumber;
    }

    public void setPrimaryNumber(String primaryNumber) {
        this.primaryNumber = primaryNumber;
    }

    public String getSeverety() {
        return severety;
    }

    public void setSeverity(String severety) {
        this.severety = severety;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAgentId() {
        return agentid;
    }

    public void setAgentId(String agentId) {
        this.agentid = agentId;
    }

    public Boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(Boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public String getUserId() {
        return userid;
    }

    public void setUserId(String userId) {
        this.userid = userId;
    }

    public Boolean getIsAllocated() {
        return isAllocated;
    }

    public void setIsAllocated(Boolean isAllocated) {
        this.isAllocated = isAllocated;
    }

    public Boolean getRetryNeeded() {
        return retryNeeded;
    }

    public void setRetryNeeded(Boolean retryNeeded) {
        this.retryNeeded = retryNeeded;
    }

    public Integer getCallCounter() {
        return callCounter;
    }

    public void setCallCounter(Integer callCounter) {
        this.callCounter = callCounter;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Character getProcessed() {
        return processed;
    }

    public void setProcessed(Character processed) {
        this.processed = processed;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Timestamp getLastModDate() {
        return lastModDate;
    }

    public void setLastModDate(Timestamp lastModDate) {
        this.lastModDate = lastModDate;
    }

    public Integer getVanSerialNo() {
        return vanSerialNo;
    }

    public void setVanSerialNo(Integer vanSerialNo) {
        this.vanSerialNo = vanSerialNo;
    }

    public Integer getVanId() {
        return vanID;
    }

    public void setVanId(Integer vanId) {
        this.vanID = vanId;
    }

    public String getVehicleNo() {
        return vehicalNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicalNo = vehicleNo;
    }

    public Integer getParkingPlaceId() {
        return parkingPlaceID;
    }

    public void setParkingPlaceId(Integer parkingPlaceId) {
        this.parkingPlaceID = parkingPlaceId;
    }

    public String getSyncedBy() {
        return syncedBy;
    }

    public void setSyncedBy(String syncedBy) {
        this.syncedBy = syncedBy;
    }

    public Timestamp getSyncedDate() {
        return syncedDate;
    }

    public void setSyncedDate(Timestamp syncedDate) {
        this.syncedDate = syncedDate;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }



    // Getter for grievanceTransactionDetails
    public List<GrievanceTransaction> getGrievanceTransactionDetails() {
        return grievanceTransactionDetails;
    }

    // Setter for grievanceTransactionDetails
    public void setGrievanceTransactionDetails(List<GrievanceTransaction> grievanceTransactionDetails) {
        this.grievanceTransactionDetails = grievanceTransactionDetails;
    }

	
	
	
}
	
	