package com.iemr.common.dto.sms;

public class SMSTemplateDTO {
    private Integer smsTemplateID;
    private String smsTemplateName;
    private String smsTemplate;
    private String dltTemplateId;
    private String smsSenderID;
    private Integer smsTypeID;
    private Integer providerServiceMapID;
    private Boolean deleted;
    private String createdBy;
    private String modifiedBy;
    private String createdDate;
    private String lastModDate;

    // Getters and Setters for all fields

    public Integer getSmsTemplateID() {
        return smsTemplateID;
    }

    public void setSmsTemplateID(Integer smsTemplateID) {
        this.smsTemplateID = smsTemplateID;
    }

    public String getSmsTemplateName() {
        return smsTemplateName;
    }

    public void setSmsTemplateName(String smsTemplateName) {
        this.smsTemplateName = smsTemplateName;
    }

    public String getSmsTemplate() {
        return smsTemplate;
    }

    public void setSmsTemplate(String smsTemplate) {
        this.smsTemplate = smsTemplate;
    }

    public String getDltTemplateId() {
        return dltTemplateId;
    }

    public void setDltTemplateId(String dltTemplateId) {
        this.dltTemplateId = dltTemplateId;
    }

    public String getSmsSenderID() {
        return smsSenderID;
    }

    public void setSmsSenderID(String smsSenderID) {
        this.smsSenderID = smsSenderID;
    }

    public Integer getSmsTypeID() {
        return smsTypeID;
    }

    public void setSmsTypeID(Integer smsTypeID) {
        this.smsTypeID = smsTypeID;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModDate() {
        return lastModDate;
    }

    public void setLastModDate(String lastModDate) {
        this.lastModDate = lastModDate;
    }
}
