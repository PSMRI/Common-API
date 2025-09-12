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
package com.iemr.common.service.kmfilemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.repository.category.SubCategoryRepository;
import com.iemr.common.repository.kmfilemanager.KMFileManagerRepository;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.km.KMService;
import com.iemr.common.utils.mapper.InputMapper;

@Service
public class KMFileManagerServiceImpl implements KMFileManagerService {
	private static final String KM_UPLOADSTATUS_PENDING = "Pending";
	private static final String KM_UPLOADSTATUS_STARTED = "Started";
	private static final String KM_UPLOADSTATUS_COMPLETED = "Completed";

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private KMService kmService;

	@Autowired
	public void setKmService(KMService kmService) {
		this.kmService = kmService;
	}

	private KMFileManagerRepository kmFileManagerRepository;

	@Autowired
	public void setKmFileManagerRepository(KMFileManagerRepository kmFileManagerRepository) {
		this.kmFileManagerRepository = kmFileManagerRepository;
	}

	private SubCategoryRepository subCategoryRepository;

	@Autowired
	public void setSubCategoryRepository(SubCategoryRepository subCategoryRepository) {
		this.subCategoryRepository = subCategoryRepository;
	}

	private InputMapper inputMapper = new InputMapper();

	@Override
	public String getKMFileLists(String request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		KMFileManager manager = objectMapper.readValue(request, KMFileManager.class);
		List<KMFileManager> kmFileManagers = new ArrayList<KMFileManager>();
		Set<Object[]> resultSet = kmFileManagerRepository.getKMFileLists(manager.getProviderServiceMapID());
		for (Object[] object : resultSet) {
			if (object != null && object.length > 12) {
				kmFileManagers.add(new KMFileManager((Integer) object[0], (Integer) object[1], (String) object[2],
						(String) object[3], (String) object[5], (String) object[6], (String) object[7],
						(String) object[8], (Timestamp) object[9], (Timestamp) object[10], (Boolean) object[11]));
			}
		}
		return kmFileManagers.toString();
	}

	@Override
	public Integer updateKMFileManager(String request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		KMFileManager manager = objectMapper.readValue(request, KMFileManager.class);
		Integer updateCount = kmFileManagerRepository.updateKMFileManager(manager.getKmFileManagerID(),
				manager.getFileUID(), manager.getFileName(), manager.getFileExtension(), manager.getVersionNo(),
				manager.getFileCheckSum(), manager.getProviderServiceMapID(), manager.getKmUploadStatus(),
				manager.getValidFrom(), manager.getValidUpto(), manager.getDeleted(), manager.getModifiedBy());
		return updateCount;
	}

	@Override
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	public String addKMFile(String request) throws IOException, NoSuchAlgorithmException, IEMRException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		KMFileManager[] kmFileManagerArray = objectMapper.readValue(request, KMFileManager[].class);
		ArrayList<KMFileManager> kmFileManagers = new ArrayList<KMFileManager>();
		kmFileManagers = addKMFile(new ArrayList(Arrays.asList(kmFileManagerArray)));
		if (kmFileManagers.size() <= 0) {
			throw new IOException("File upload to KM server failed. Please try after some time");
		}
		return kmFileManagers.toString();
	}


	private ArrayList<KMFileManager> addKMFile(Iterable<KMFileManager> kmFileManagers)
			throws IOException, NoSuchAlgorithmException {
		ArrayList<KMFileManager> savedFileManagers = new ArrayList<KMFileManager>();
		FileOutputStream newFile = null;
		FileInputStream fis = null;
		try
		{
		for (KMFileManager kmFileManager : kmFileManagers) {
			if (kmFileManager.getFileName() != null && kmFileManager.getProviderServiceMapID() != null
					&& kmFileManager.getFileContent() != null) {
				kmFileManager.setFileName(kmFileManager.getFileName().replace("`", "").replace("'", "").replace("$", "")
						.replace("\\", "").replace("/", "").replace("~", "").replace("`", "").replace("!", "")
						.replace("@", "").replace("#", "").replace("$", "").replace("%", "").replace("^", "")
						.replace("&", "").replace("*", "").replace("(", "").replace(")", "").replace("{", "")
						.replace("}", "").replace("[", "").replace("]", "").replace("|", "").replace("\\", "")
						.replace(":", "").replace(";", "").replace("-", "").replace("_", "").replace("+", "")
						.replace("=", "").replace("\"", "").replace("'", ""));
				String tempFilePath = ConfigProperties.getPropertyByName("tempFilePath");
				newFile = new FileOutputStream(tempFilePath + "/" + kmFileManager.getFileName());
				newFile.write(Base64.getDecoder().decode(kmFileManager.getFileContent()));
				newFile.flush();
				newFile.close();
				fis = new FileInputStream(tempFilePath + "/" + kmFileManager.getFileName());
				String checksum = DigestUtils.md5DigestAsHex(fis);
				fis.close();
				logger.info("File is " + kmFileManager.getFileName());
				logger.info("File size is " + new File(tempFilePath + "/" + kmFileManager.getFileName()).length());
				logger.info("File checksum is " + checksum);
				logger.info("File checksum length is " + checksum.length());
				kmFileManager.setFileCheckSum(checksum);
				kmFileManager.setKmUploadStatus(KM_UPLOADSTATUS_PENDING);
				String version = getFileVersion(kmFileManager);
				kmFileManager.setVersionNo(version);
				String documentPath = kmFileManager.getProviderServiceMapID() + "/";
				if (kmFileManager.getCategoryID() != null) {
					documentPath += kmFileManager.getCategoryID() + "/";
				}
				if (kmFileManager.getSubCategoryID() != null) {
					documentPath += kmFileManager.getSubCategoryID() + "/";
				}
				if (kmFileManager.getVanID() != null)
					documentPath += kmFileManager.getVanID() + "/";

				documentPath += version + "/";
				documentPath += kmFileManager.getFileName();
				kmFileManager.setKmUploadStatus(KM_UPLOADSTATUS_STARTED);
				String uuid = kmService.createDocument(documentPath, tempFilePath + "/" + kmFileManager.getFileName());
				if (uuid != null) {
					kmFileManager.setKmUploadStatus(KM_UPLOADSTATUS_COMPLETED);
					kmFileManager.setFileUID(uuid);

					kmFileManager.setSubCategoryID(kmFileManager.getSubCategoryID());

					savedFileManagers.add(kmFileManagerRepository.save(kmFileManager));
					if (kmFileManager.getSubCategoryID() != null) {
						updateSubcategoryFilePath(kmFileManager);
					}
				}
			}
		}
		}
		catch(Exception e)
		{
			logger.error("error " + e.getMessage());
		}
		finally
		{
			if(newFile !=null)
			newFile.close();
			if(fis !=null)
			fis.close();
		}
		return savedFileManagers;
	}


	private void updateSubcategoryFilePath(KMFileManager kmFileManager) {
		subCategoryRepository.updateFilePath(kmFileManager.getSubCategoryID(), kmFileManager.getFileUID());
	}

	private String getFileVersion(KMFileManager kmFileManager) {
		String version = "V1";
		List<KMFileManager> files = kmFileManagerRepository.getKMFileByFileName(kmFileManager.getProviderServiceMapID(),
				kmFileManager.getFileName());
		version = "V" + (files.size() + 1);

		return version;
	}
}
