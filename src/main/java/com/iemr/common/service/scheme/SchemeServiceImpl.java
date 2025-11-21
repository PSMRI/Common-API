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
package com.iemr.common.service.scheme;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.data.scheme.Scheme;
import com.iemr.common.repository.scheme.SchemeRepository;
import com.iemr.common.service.kmfilemanager.KMFileManagerService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

@Service
public class SchemeServiceImpl implements SchemeService {

	private Logger logger = LoggerFactory.getLogger(SchemeServiceImpl.class);

	private InputMapper inputMapper = new InputMapper();

	// private ConfigProperties configProperties;
	//
	// @Autowired
	// public void setConfigProperties(ConfigProperties configProperties)
	// {
	// this.configProperties = configProperties;
	// }

	private KMFileManagerService kmFileManagerService;

	@Autowired
	public void setKmFileManagerService(KMFileManagerService kmFileManagerService) {
		this.kmFileManagerService = kmFileManagerService;
	}

	@Autowired
	private SchemeRepository schemeRepository;

	@Override
	public List<Scheme> getSchemeList(Integer providerServiceMapID) throws Exception {
		List<Scheme> schemeList = new ArrayList<Scheme>();
		List<Object[]> schemes = schemeRepository.getschemeList(providerServiceMapID);

		for (Object[] objects : schemes) {
			if (objects != null && objects.length > 0) {
				Scheme scheme = new Scheme((Integer) objects[0], (String) objects[1], (String) objects[2],
						(Integer) objects[3], (Integer) objects[4], (Boolean) objects[5], (String) objects[6],
						(KMFileManager) objects[7]);
				scheme.setKmFilePath(getFilePath((KMFileManager) objects[7]));
				schemeList.add(scheme);

			}
		}

		return schemeList;
	}

	@Override
	public Scheme getSchemeByID(Integer schemeID) throws Exception {

		return schemeRepository.getSchemeByID(schemeID);
	}

	@Override
	public String deletedata(Scheme deleteData) {
		// Scheme data=schemeRepository.save(deleteData);
		schemeRepository.save(deleteData);
		return "success";

	}

	public String getFilePath(KMFileManager kmFileManager) {
		String fileUIDAsURI = null;
		if (kmFileManager != null && kmFileManager.getFileUID() != null) {
			String fileUID = kmFileManager.getFileUID();
			String dmsPath = ConfigProperties.getPropertyByName("km-base-path");
			String dmsProtocol = ConfigProperties.getPropertyByName("km-base-protocol");
			String userName = ConfigProperties.getPropertyByName("km-guest-user");
			String userPassword = ConfigProperties.getPassword("km-guest-user");
			fileUIDAsURI = dmsProtocol + "://" + userName + ":" + userPassword + "@" + dmsPath + "/Download?uuid="
					+ fileUID;
		}
		// return fileUIDAsURI;
		String message = kmFileManager.getFileUID() ;
		return message;
	}

	@Override
	public Scheme save(Scheme schemeRequest) throws Exception {
		// KMFileManager kmFileManager = scheme.getKmFileManager();
		// scheme.setKmFileManager(null);
		// scheme.setKmFileManager(kmFileManager);
		if (schemeRequest.getKmFileManager() != null && schemeRequest.getKmFileManagerID() == null) {
			Integer KmFileManagerID = updateSchemeFile(schemeRequest);
			schemeRequest.setKmFileManagerID(KmFileManagerID);
		}
		// updateKMFileIDInScheme(scheme);
		Scheme schemeResponse = schemeRepository.save(schemeRequest);
		// scheme = schemeRepository.save(scheme);
		return schemeResponse;
	}

	private Integer updateSchemeFile(Scheme schemeRequest) throws NoSuchAlgorithmException, IOException, IEMRException {

		logger.info("KmFileManager: " + schemeRequest.getKmFileManager());
		logger.info("FileContent: " + schemeRequest.getKmFileManager().getFileContent());
		logger.info("FileExtension: " + schemeRequest.getKmFileManager().getFileExtension());
		logger.info("FileName: " + schemeRequest.getKmFileManager().getFileName());

		if (schemeRequest.getKmFileManager() != null && schemeRequest.getKmFileManager().getFileContent() != null
				&& schemeRequest.getKmFileManager().getFileExtension() != null
				&& schemeRequest.getKmFileManager().getFileName() != null) {

			List<KMFileManager> list = new ArrayList<KMFileManager>();
			list.add(schemeRequest.getKmFileManager());
			logger.info("addKMFile request: " + list.toString());
			String kmFileManagerResp = kmFileManagerService.addKMFile(list.toString());
			logger.info("addKMFile response " + kmFileManagerResp);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			KMFileManager[] kmFileManagerArray = objectMapper.readValue(kmFileManagerResp, KMFileManager[].class);
			for (KMFileManager kmFileManager : kmFileManagerArray) {
				schemeRequest.setKmFileManagerID(kmFileManager.getKmFileManagerID());
			}

		}
		return schemeRequest.getKmFileManagerID();
	}

	/*
	 * private Integer uploadSchemeFileInKM(Scheme m_scheme) throws
	 * NoSuchAlgorithmException, IOException, IEMRException { m_scheme =
	 * updateSchemeFile(m_scheme); return m_scheme.getKmFileManagerID(); }
	 * 
	 * @Transactional(propagation = Propagation.REQUIRES_NEW) private Scheme
	 * updateSchemeFile(Scheme schemeRequest) throws NoSuchAlgorithmException,
	 * IOException, IEMRException { if (schemeRequest.getKmFileManager() != null &&
	 * schemeRequest.getKmFileManager().getFileContent() != null &&
	 * schemeRequest.getKmFileManager().getFileExtension() != null &&
	 * schemeRequest.getKmFileManager().getFileName() != null) { List<KMFileManager>
	 * list = new ArrayList<KMFileManager>();
	 * list.add(schemeRequest.getKmFileManager()); String kmFileManagerResp =
	 * kmFileManagerService.addKMFile(list.toString()); KMFileManager[]
	 * kmFileManagerArray = inputMapper.gson().fromJson(kmFileManagerResp,
	 * KMFileManager[].class); for (KMFileManager kmFileManager :
	 * kmFileManagerArray) {
	 * schemeRequest.setKmFileManagerID(kmFileManager.getKmFileManagerID()); } }
	 * return schemeRequest; }
	 */

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	private void updateKMFileIDInScheme(Scheme scheme) {
		schemeRepository.updateScheme(scheme.getSchemeID(), scheme.getKmFileManagerID());
	}

}
