package com.iemr.common.data.grievance;


import javax.persistence.*;

import com.google.gson.annotations.Expose;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "t_grievancetransaction")
public class GrievanceTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gtid")
    private Long gtid;

    @Column(name = "gwid", nullable = false)
    @Expose
    private Long gwid;

    @Column(name = "FileName", nullable = false, length = 500)
    @Expose
    @NotBlank(message = "File name is required")
    @Size(max = 500, message = "File name cannot exceed 500 characters")
    private String fileName;

    @Column(name = "FileType", nullable = false, length = 300)
    @Expose
    @NotBlank(message = "File name is required")
    @Size(max = 300, message = "File type cannot exceed 300 characters")
    private String fileType;

    @Column(name = "Redressed", nullable = false)
    @Expose
    private String redressed;

    @Column(name = "createdAt", nullable = false)
    @Expose
    private Date createdAt;

    @Column(name = "updatedAt", nullable = false)
    @Expose
    private Date updatedAt;

    @Column(name = "Comments", length = 500)
    private String comments;

    @Column(name = "ProviderServiceMapID")
    @Expose
    private Integer providerServiceMapID;

    @Column(name = "Deleted", insertable = false, updatable = true)
    @Expose
    private Boolean deleted;

    @Column(name = "Processed", nullable = false)
    @Expose
    private String processed;


    @Column(name = "ActionTakenBy")
    @Expose
    private String actionTakenBy;
    
    @Column(name = "Status")
    @Expose
    private String status;
    
    @Column(name = "Comment")
    @Expose
    private String comment;
    
    @Column(name = "CreatedBy", nullable = false)
    @Expose
    private String createdBy;

    @Column(name = "CreatedDate", nullable = false)
    private Date createdDate;

    @Column(name = "ModifiedBy")
    @Expose
    private String modifiedBy;

    @Column(name = "LastModDate")
    @Expose
    private Date lastModDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gwid", insertable = false, updatable = false)
    private GrievanceDetails grievanceDetails;

    // Getters and Setters

    public Long getGtid() {
        return gtid;
    }

    public void setGtid(Long gtid) {
        this.gtid = gtid;
    }

    public Long getGwid() {
        return gwid;
    }

    public void setGwid(Long gwid) {
        this.gwid = gwid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getRedressed() {
        return redressed;
    }

    public void setRedressed(String redressed) {
        this.redressed = redressed;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getProviderServiceMapID() {
        return providerServiceMapID;
    }

    public void setProviderServiceMapID(Integer providerServiceMapID) {
        this.providerServiceMapID = providerServiceMapID;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getProcessed() {
        return processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getLastModDate() {
        return lastModDate;
    }

    public void setLastModDate(Date lastModDate) {
        this.lastModDate = lastModDate;
    }
    
    public String getActionTakenBy( ) {
    	return actionTakenBy;
    }
    
    public void setActionTakenBy(String actionTakenBy) {
    	this.actionTakenBy = actionTakenBy;
    }
    
    public String getStatus( ) {
    	return status;
    }
    	
    public void setStatus(String status) {
    	this.status = status;
    }
    
    public String getComment( ) {
    	return comment;
    }
    
    public void setComment(String comment ) {
    	this.comment = comment;
    }
}

