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
package com.iemr.common.repository.kmfilemanager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.kmfilemanager.KMFileManager;

@Repository
public interface KMFileManagerRepository extends CrudRepository<KMFileManager, Long>
{
	@Query("update KMFileManager " + "set fileUID = :fileUID, fileName = :fileName, fileExtension = :fileExtension, "
			+ "versionNo = :versionNo, fileCheckSum = :fileCheckSum, "
			+ "providerServiceMapID = :providerServiceMapID, kmUploadStatus = :kmUploadStatus, "
			+ "validFrom = :validFrom, validUpto = :validUpto, deleted = :deleted, modifiedBy = :modifiedBy "
			+ "where kmFileManagerID=:kmFileManagerID")
	Integer updateKMFileManager(@Param("kmFileManagerID") Integer kmFileManagerID, @Param("fileUID") String fileUID,
			@Param("fileName") String fileName, @Param("fileExtension") String fileExtension,
			@Param("versionNo") String versionNo, @Param("fileCheckSum") String fileCheckSum,
			@Param("providerServiceMapID") Integer providerServiceMapID, @Param("kmUploadStatus") String kmUploadStatus,
			@Param("validFrom") Timestamp validFrom, @Param("validUpto") Timestamp validUpto,
			@Param("deleted") Boolean deleted, @Param("modifiedBy") String modifiedBy);

	@Query("select providerServiceMapID, kmFileManagerID, fileUID, fileName, fileExtension, versionNo, fileCheckSum, "
			+ "kmUploadStatus, validFrom, validUpto, deleted from KMFileManager "
			+ "where providerServiceMapID=:providerServiceMapID")
	Set<Object[]> getKMFileLists(@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select files from KMFileManager files "
			+ "where files.providerServiceMapID = :providerServiceMapID and files.fileName = :fileName "
			+ "order by kmFileManagerID asc")
	List<KMFileManager> getKMFileByFileName(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("fileName") String fileName);

//	@Query("select new KMFileManager(providerServiceMapID, kmFileManagerID, fileUID, fileName, "
//			+ "fileExtension, versionNo, fileCheckSum, kmUploadStatus, validFrom, validUpto, deleted, createdBy) "
//			+ "from KMFileManager where providerServiceMapID=:providerServiceMapID and fileUID = :fileUID")
//	ArrayList<KMFileManager> getKMFileLists(@Param("providerServiceMapID") Integer providerServiceMapID,
//			@Param("fileUID") String fileUID);
	
	@Query("select kmFileManager from KMFileManager kmFileManager "
			+ "where kmFileManager.providerServiceMapID=:providerServiceMapID and kmFileManager.fileUID = :fileUID")
	ArrayList<KMFileManager> getKMFileLists(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("fileUID") String fileUID);
	
	@Query("select kmFileManager.fileName, kmFileManager.fileExtension from KMFileManager kmFileManager "
			+ "where kmFileManager.fileUID = :fileUID")
	List<Object[]> getFileNameByUID(@Param("fileUID") String fileUID);
	
	//newChange
    @Query("SELECT km FROM KMFileManager km WHERE km.subCategoryID = :subCategoryID AND km.deleted = false")
    List<KMFileManager> getFilesBySubCategoryID(@Param("subCategoryID") Integer subCategoryID);

}
