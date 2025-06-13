package com.iemr.common.data.grievance;

import java.sql.Timestamp;

public class GrievanceResponse {
    private Long grievanceId;
    private String complaintID;
    private String primaryNumber;
    private String complaintResolution;
    private String remarks;
    private Timestamp createdDate;
    private Timestamp lastModDate;

    // Getters and Setters
    public Long getGrievanceId() {
        return grievanceId;
    }

    public void setGrievanceId(Long grievanceId) {
        this.grievanceId = grievanceId;
    }

    public String getComplaintID() {
        return complaintID;
    }

    public void setComplaintID(String complaintID) {
        this.complaintID = complaintID;
    }

    public String getPrimaryNumber() {
        return primaryNumber;
    }

    public void setPrimaryNumber(String primaryNumber) {
        this.primaryNumber = primaryNumber;
    }

    public String getComplaintResolution() {
        return complaintResolution;
    }

    public void setComplaintResolution(String complaintResolution) {
        this.complaintResolution = complaintResolution;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getLastModDate() {
        return lastModDate;
    }

    public void setLastModDate(Timestamp lastModDate) {
        this.lastModDate = lastModDate;
    }
}
