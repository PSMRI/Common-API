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
package com.iemr.common.service.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.iemr.common.data.category.CategoryDetails;
import com.iemr.common.data.category.SubCategoryDetails;
import com.iemr.common.data.service.SubService;
import com.iemr.common.repository.category.CategoryRepository;
import com.iemr.common.repository.category.SubCategoryRepository;
import com.iemr.common.repository.kmfilemanager.KMFileManagerRepository;
import com.iemr.common.repository.services.ServiceTypeRepository;
import com.iemr.common.utils.aesencryption.AESEncryptionDecryption;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.data.common.DocFileManager;
import com.iemr.common.data.kmfilemanager.KMFileManager;

@Service
@PropertySource("classpath:/application.properties")
public class CommonServiceImpl implements CommonService {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	private static final String FILE_PATH = "filePath";  

	/**
	 * Designation repository
	 */
	private ServiceTypeRepository serviceTypeRepository;
	private CategoryRepository categoryRepository;
	private SubCategoryRepository subCategoryRepository;
	private KMFileManagerRepository kmFileManagerRepository;
	private AESEncryptionDecryption aESEncryptionDecryption;

	private InputMapper inputMapper = new InputMapper();

	/*
	 * private ConfigProperties configProperties;
	 * 
	 * @Autowired public void setConfigProperties(ConfigProperties
	 * configProperties) { this.configProperties = configProperties; }
	 */

	@Autowired
	public void setCategoryRepository(CategoryRepository categoryRepository) {

		this.categoryRepository = categoryRepository;
	}

	@Autowired
	public void setSubCategoryRepository(SubCategoryRepository subCategoryRepository) {

		this.subCategoryRepository = subCategoryRepository;
	}

	@Autowired
	public void setServiceTypeRepository(ServiceTypeRepository serviceTypeRepository) {

		this.serviceTypeRepository = serviceTypeRepository;
	}

	@Autowired
	public void setKmFileManagerRepository(KMFileManagerRepository kmFileManagerRepository) {
		this.kmFileManagerRepository = kmFileManagerRepository;
	}
	
	@Autowired
	public CommonServiceImpl(AESEncryptionDecryption aESEncryptionDecryption) {
		this.aESEncryptionDecryption = aESEncryptionDecryption;
	}
	

	@Value("${fileBasePath}")
	private String fileBasePath;
	
	@Override
	public Iterable<CategoryDetails> getCategories() {
		List<CategoryDetails> categoriesList = new ArrayList<CategoryDetails>();
		// ArrayList<Object[]> lists = categoryRepository.findBy();
		// for (Object[] objects : lists)
		// {
		// if (objects != null && objects.length > 1)
		// {
		// System.out.println(objects);
		// categoriesList.add(new CategoryDetails((Integer) objects[0], (String)
		// objects[1]));
		// }
		// }
		categoriesList = categoryRepository.findBy();
		return categoriesList;
	}

	
	//newChange
	@Override
	public Iterable<SubCategoryDetails> getSubCategories(String request) throws IEMRException, JsonMappingException, JsonProcessingException {
	    ObjectMapper objectMapper = new ObjectMapper();
	    SubCategoryDetails subCategoryDetails = objectMapper.readValue(request, SubCategoryDetails.class);
	    List<SubCategoryDetails> subCategoriesList = new ArrayList<>();
	    ArrayList<Object[]> lists = subCategoryRepository.findByCategoryID(subCategoryDetails.getCategoryID());

	    for (Object[] objects : lists) {
	        if (objects != null && objects.length > 1) {
	            Integer subCatId = (Integer) objects[0];
	            String subCatName = (String) objects[1];

	            // Fetch all files under this subcategory from KMFileManager
	            List<KMFileManager> files = kmFileManagerRepository.getFilesBySubCategoryID(subCatId);
	            ArrayList<KMFileManager> fileList = new ArrayList<>(files);

	            String fileURL = null;
	            String fileNameWithExtension = null;

	            if (!fileList.isEmpty()) {
	                KMFileManager firstFile = fileList.get(0); // Just for representative file URL and name
	                fileURL = getFilePath(firstFile.getFileUID());
	                fileNameWithExtension = firstFile.getFileName() + firstFile.getFileExtension();
	            }

	            SubCategoryDetails subCategory = new SubCategoryDetails(subCatId, subCatName);
	            subCategory.setFileManger(fileList);  // Attach all files here
	            subCategory.setFileURL(fileURL);      // Representative file URL
	            subCategory.setFileNameWithExtension(fileNameWithExtension); // Representative file name+ext

	            subCategoriesList.add(subCategory);
	        }
	    }
	    return subCategoriesList;
	}
	
	
	private String getFilePath(String fileUID)
	{
		String fileUIDAsURI = null;

			String dmsPath = ConfigProperties.getPropertyByName("km-base-path");
			String dmsProtocol = ConfigProperties.getPropertyByName("km-base-protocol");
			String userName = ConfigProperties.getPropertyByName("km-guest-user");
			String userPassword = ConfigProperties.getPassword("km-guest-user");
			fileUIDAsURI =
					dmsProtocol + "://" + userName + ":" + userPassword + "@" + dmsPath + "/Download?uuid=" + fileUID;
		
		return fileUIDAsURI;
	}

	@Override
	public List<SubCategoryDetails> getSubCategoryFiles(String request) throws IEMRException, JsonMappingException, JsonProcessingException {
		SubCategoryDetails subCategoryDetails;
		ObjectMapper objectMapper = new ObjectMapper();
		subCategoryDetails = objectMapper.readValue(request, SubCategoryDetails.class);
		List<SubCategoryDetails> subCategoriesList = new ArrayList<SubCategoryDetails>();
		if (subCategoryDetails.getSubCategoryID() != null) {
			subCategoriesList = subCategoryRepository.findBySubCategoryID(subCategoryDetails.getSubCategoryID());
		} else if ((subCategoryDetails.getCategoryID() != null)
				&& (subCategoryDetails.getProviderServiceMapID() != null)) {
			subCategoriesList = subCategoryRepository.findByProviderServiceMapCategoryID(
					subCategoryDetails.getProviderServiceMapID(), subCategoryDetails.getCategoryID());
		} else if (subCategoryDetails.getProviderServiceMapID() != null) {
			subCategoriesList = subCategoryRepository
					.findByProviderServiceMapID(subCategoryDetails.getProviderServiceMapID());
		}
		for (int index = 0; index < subCategoriesList.size(); index++) {
			SubCategoryDetails subCategory = subCategoriesList.get(index);
			if (subCategory.getSubCatFilePath() != null && subCategory.getSubCatFilePath().length() > 0) {
				subCategoriesList.get(index).setFileManger(kmFileManagerRepository
						.getKMFileLists(subCategoryDetails.getProviderServiceMapID(), subCategory.getSubCatFilePath()));
			}
		}
		return subCategoriesList;
	}

	@Override
	public List<SubCategoryDetails> getSubCategoryFilesWithURL(String request) throws IEMRException, JsonMappingException, JsonProcessingException {
		SubCategoryDetails subCategoryDetails;
		ObjectMapper objectMapper = new ObjectMapper();
		subCategoryDetails = objectMapper.readValue(request, SubCategoryDetails.class);
		List<SubCategoryDetails> subCategoriesList = new ArrayList<SubCategoryDetails>();
		if (subCategoryDetails.getSubCategoryID() != null) {
			subCategoriesList = subCategoryRepository.findBySubCategoryID(subCategoryDetails.getSubCategoryID());
		} else if ((subCategoryDetails.getCategoryID() != null)
				&& (subCategoryDetails.getProviderServiceMapID() != null)) {
			subCategoriesList = subCategoryRepository.findByProviderServiceMapCategoryID(
					subCategoryDetails.getProviderServiceMapID(), subCategoryDetails.getCategoryID());
		} else if (subCategoryDetails.getProviderServiceMapID() != null) {
			subCategoriesList = subCategoryRepository
					.findByProviderServiceMapID(subCategoryDetails.getProviderServiceMapID());
		}
		for (int index = 0; index < subCategoriesList.size(); index++) {
			SubCategoryDetails subCategory = subCategoriesList.get(index);
			if (subCategory.getSubCatFilePath() != null && subCategory.getSubCatFilePath().length() > 0) {
				String subCatFilePath = subCategory.getSubCatFilePath();
				String dmsPath = ConfigProperties.getPropertyByName("km-base-path");
				String dmsProtocol = ConfigProperties.getPropertyByName("km-base-protocol");
				String userName = ConfigProperties.getPropertyByName("km-guest-user");
				String userPassword = ConfigProperties.getPassword("km-guest-user");
				String fileUIDAsURI = dmsProtocol + "://" + userName + ":" + userPassword + "@" + dmsPath
						+ "/Download?uuid=" + subCategory.getSubCatFilePath();
				subCategory.setSubCatFilePath(fileUIDAsURI);
				subCategoriesList.get(index).setFileManger(kmFileManagerRepository
						.getKMFileLists(subCategoryDetails.getProviderServiceMapID(), subCatFilePath));
			}
		}
		return subCategoriesList;
	}

	@Override
	public Iterable<CategoryDetails> getCategories(String request) throws IEMRException, JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		CategoryDetails categoryRequest = objectMapper.readValue(request, CategoryDetails.class);
		List<CategoryDetails> categoriesList = new ArrayList<CategoryDetails>();
		
		if (categoryRequest.getIsWellBeing() != null) {
			categoriesList = categoryRepository.getAllCategories(categoryRequest.getSubServiceID(),
					categoryRequest.getProviderServiceMapID(), categoryRequest.getIsWellBeing());
		} else {
			categoriesList = categoryRepository.getAllCategories(categoryRequest.getSubServiceID(),
					categoryRequest.getProviderServiceMapID());
		}
		return categoriesList;
	}

	@Override
	public Iterable<SubService> getActiveServiceTypes(String request) throws IEMRException, JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		SubService subServiceRequest =objectMapper.readValue(request, SubService.class);
		List<SubService> subServices = new ArrayList<SubService>();
		ArrayList<Object[]> lists = serviceTypeRepository
				.findActiveServiceTypes(subServiceRequest.getProviderServiceMapID());
		for (Object[] objects : lists) {
			if (objects != null && objects.length >= 4) {
				subServices.add(new SubService((Integer) objects[0], (String) objects[1], (String) objects[2],
						(Boolean) objects[3]));
			}
		}
		return subServices;
	}
	
	private ArrayList<Map<String, String>> createFile(List<DocFileManager> docFileManagerList, String basePath,
			String currDate) throws IOException {
		ArrayList<Map<String, String>> responseList = new ArrayList<>();
		Map<String, String> responseMap;
		FileOutputStream fileOutput = null;
		try
		{
		
		
		for (DocFileManager dFM : docFileManagerList) {
			if (dFM.getFileName() != null && dFM.getFileExtension() != null) {
				responseMap = new HashMap<>();
				dFM.setFileName(dFM.getFileName().replace("`", "").replace("'", "").replace("$", "").replace("\\", "")
						.replace("/", "").replace("~", "").replace("`", "").replace("!", "").replace("@", "")
						.replace("#", "").replace("$", "").replace("%", "").replace("^", "").replace("&", "")
						.replace("*", "").replace("(", "").replace(")", "").replace("{", "").replace("}", "")
						.replace("[", "").replace("]", "").replace("|", "").replace("\\", "").replace(":", "")
						.replace(";", "").replace("-", "").replace("_", "").replace("+", "").replace("=", "")
						.replace("\"", "").replace("'", ""));

				Long currTimestamp = System.currentTimeMillis();
				fileOutput = new FileOutputStream(
						basePath + "/" + currDate + "/" + currTimestamp + dFM.getFileName());

				fileOutput.write(Base64.getDecoder().decode(dFM.getFileContent()));

				responseMap.put("fileName", dFM.getFileName());
				responseMap.put(FILE_PATH, basePath + "/" + currDate + "/" + currTimestamp + dFM.getFileName());


				fileOutput.flush();
				
				
				responseList.add(responseMap);
			}
			
			if(fileOutput!=null)
    			fileOutput.close();
		}
		}
		catch(Exception e)
    	{
    		logger.error(e.getMessage());
    	}
    	finally
    	{
    		if(fileOutput!=null)
    			fileOutput.close();
    	}
		return responseList;
		
	}


	// files upload/save start
		@Override
		public String saveFiles(List<DocFileManager> docFileManagerList) throws IOException, Exception {
			ArrayList<Map<String, String>> responseList = new ArrayList<>();

			String basePath = fileBasePath;

			String currDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			if (!docFileManagerList.isEmpty()) {
				Integer vanId = docFileManagerList.get(0).getVanID();
				if (vanId == null) {
					throw new IllegalArgumentException("VanId cannot be null or empty");
				}
				basePath += vanId;
				File file = new File(basePath + File.separator + currDate);

				if (file.isDirectory()) {
					responseList = createFile(docFileManagerList, basePath, currDate);
				} else {
					// create a new directory
					Files.createDirectories(Paths.get(basePath + "/" + currDate));
					responseList = createFile(docFileManagerList, basePath, currDate);
				}
			}
			/*
			 *
			 *
			 AN40085822 - Internal path disclosure -encryption
			 *
			 *
			 */
					for (Map<String, String> obj : responseList) {
						String encryptedFilePath = aESEncryptionDecryption.encrypt(obj.get(FILE_PATH));
						obj.put(FILE_PATH,encryptedFilePath);
					}
					return new Gson().toJson(responseList);
				}

	
}
