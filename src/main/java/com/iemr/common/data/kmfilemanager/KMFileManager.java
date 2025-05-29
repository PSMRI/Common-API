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
package com.iemr.common.data.kmfilemanager;

import java.sql.Timestamp;

import com.google.gson.annotations.Expose;
import com.iemr.common.data.notification.Notification;
import com.iemr.common.utils.mapper.OutputMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;


@Entity
@Table(name = "t_kmfilemanager")
@Data
	public class KMFileManager {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "KmFileManagerID")
		@Expose
		private Integer kmFileManagerID;
		@Transient
		@OneToOne(mappedBy = "kmFileManager")
		private transient Notification notification;

		@Column(name = "FileUID")
		@Expose
		private String fileUID;

		@Column(name = "FileName")
		@Expose
		private String fileName;

		@Column(name = "FileExtension")
		@Expose
		private String fileExtension;

		@Column(name = "VersionNo")
		@Expose
		private String versionNo;

		@Column(name = "FileCheckSum")
		@Expose
		private String fileCheckSum;

		@Column(name = "ProviderServiceMapID")
		@Expose
		private Integer providerServiceMapID;

		@Column(name = "UserID")
		@Expose
		private Integer userID;

		@Column(name = "KmUploadStatus")
		@Expose
		private String kmUploadStatus;

		@Column(name = "ValidFrom")
		@Expose
		private Timestamp validFrom;

		@Column(name = "ValidUpto")
		@Expose
		private Timestamp validUpto;

		@Column(name = "Deleted", insertable = false, updatable = true)
		@Expose
		private Boolean deleted;
		@Column(name = "CreatedBy")
		@Expose
		private String createdBy;
		@Column(name = "CreatedDate", insertable = false, updatable = false)
		@Expose
		private Timestamp createdDate;
		@Column(name = "ModifiedBy")
		private String modifiedBy;
		@Column(name = "LastModDate", insertable = false, updatable = true)
		private Timestamp lastModDate;

		@Transient
		@Expose
		private Integer categoryID;
		@Transient
		@Expose
		private Integer subCategoryID;
		@Transient
		@Expose
		private String fileContent;
		@Transient
		@Expose
		private boolean isUploaded;

		@Transient
		private transient OutputMapper outputMapper = new OutputMapper();

		// column added for MMU
		@Transient
		@Expose
		private Integer vanID;

		public Integer getProviderServiceMapID() {
			return providerServiceMapID;
		}

		public Integer getKmFileManagerID() {
			return kmFileManagerID;
		}

		public String getFileUID() {
			return fileUID;
		}

		public String getFileName() {
			return fileName;
		}

		public String getFileExtension() {
			return fileExtension;
		}

		public String getVersionNo() {
			return versionNo;
		}

		public String getFileCheckSum() {
			return fileCheckSum;
		}

		public Integer getUserID() {
			return userID;
		}

		public String getKmUploadStatus() {
			return kmUploadStatus;
		}

		public Timestamp getValidFrom() {
			return validFrom;
		}

		public Timestamp getValidUpto() {
			return validUpto;
		}

		public Boolean getDeleted() {
			return deleted;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public Timestamp getCreatedDate() {
			return createdDate;
		}

		public String getModifiedBy() {
			return modifiedBy;
		}

		public Timestamp getLastModDate() {
			return lastModDate;
		}

		public OutputMapper getOutputMapper() {
			return outputMapper;
		}

		public Integer getCategoryID() {
			return categoryID;
		}

		public Integer getSubCategoryID() {
			return subCategoryID;
		}

		public String getFileContent() {
			return fileContent;
		}

		public void setFileUID(String fileUID) {
			this.fileUID = fileUID;
		}

		public void setFileCheckSum(String fileCheckSum) {
			this.fileCheckSum = fileCheckSum;
		}

		public void setKmUploadStatus(String kmUploadStatus) {
			this.kmUploadStatus = kmUploadStatus;
		}

		public void setVersionNo(String versionNo) {
			this.versionNo = versionNo;
		}

		public KMFileManager(Integer providerServiceMapID, Integer kmFileManagerID, String fileUID, String fileName,
				String fileExtension, String versionNo, String fileCheckSum, String kmUploadStatus, Timestamp validFrom,
				Timestamp validUpto, Boolean deleted) {
			this.providerServiceMapID = providerServiceMapID;
			this.kmFileManagerID = kmFileManagerID;
			this.fileUID = fileUID;
			this.fileName = fileName;
			this.fileExtension = fileExtension;
			this.versionNo = versionNo;
			this.fileCheckSum = fileCheckSum;
			this.versionNo = versionNo;
			this.fileCheckSum = fileCheckSum;
			this.kmUploadStatus = kmUploadStatus;
			this.validFrom = validFrom;
			this.validUpto = validUpto;
			this.deleted = deleted;
		}

		public KMFileManager(Integer providerServiceMapID, Integer kmFileManagerID, String fileUID, String fileName,
				String fileExtension, String versionNo, String fileCheckSum, String kmUploadStatus, Timestamp validFrom,
				Timestamp validUpto, Boolean deleted, String createdBy) {
			this.providerServiceMapID = providerServiceMapID;
			this.kmFileManagerID = kmFileManagerID;
			this.fileUID = fileUID;
			this.fileName = fileName;
			this.fileExtension = fileExtension;
			this.versionNo = versionNo;
			this.fileCheckSum = fileCheckSum;
			this.versionNo = versionNo;
			this.fileCheckSum = fileCheckSum;
			this.kmUploadStatus = kmUploadStatus;
			this.validFrom = validFrom;
			this.validUpto = validUpto;
			this.deleted = deleted;
			this.createdBy = createdBy;
		}

		public KMFileManager() {

		}

		@Override
		public String toString() {
			Gson gson = new GsonBuilder()
		.excludeFieldsWithoutExposeAnnotation()
		.setLongSerializationPolicy(LongSerializationPolicy.STRING)
		.serializeNulls()
		.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // format dates without custom adapter
		.create();

		return gson.toJson(this);

		
		}
	}
