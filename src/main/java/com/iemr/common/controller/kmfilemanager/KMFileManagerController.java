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
package com.iemr.common.controller.kmfilemanager;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.mapper.utils.InputMapper;
import com.iemr.common.service.kmfilemanager.KMFileManagerService;
import com.iemr.common.service.scheme.SchemeServiceImpl;
import com.iemr.common.service.services.CommonServiceImpl;
import com.iemr.common.utils.response.OutputResponse;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.common.DocFileManager;


@RequestMapping({"/kmfilemanager"})
@RestController
public class KMFileManagerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private KMFileManagerService kmFileManagerService;

    @Autowired
    private CommonServiceImpl commonServiceImpl;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public void setKmFileManagerService(KMFileManagerService kmFileManagerService) {
        this.kmFileManagerService = kmFileManagerService;
    }

    @Autowired
    private SchemeServiceImpl schemeServiceImpl;


    public KMFileManagerController(CommonServiceImpl commonServiceImpl) {
        this.commonServiceImpl = commonServiceImpl;

    }


    @Operation(summary = "Save beneficairy documents locally")
    @PostMapping(value = {"/saveFiles"}, consumes = "application/json", produces = "application/json")
    public String saveFiles(@RequestBody String request) {
        OutputResponse response = new OutputResponse();

        try {
            DocFileManager[] docFileManager = InputMapper.getInstance().fromJson(request, DocFileManager[].class);

            List<DocFileManager> docFileManagerList = Arrays.asList(docFileManager);
            String s = commonServiceImpl.saveFiles(docFileManagerList);
            if (s != null)
                response.setResponse(s);
        } catch (Exception e) {
            logger.error("Error while saving files : {0}", e);
            response.setError(5000, "Error while saving files : " + e);
        }

        return response.toString();
    }


    @Operation(summary = "Add file")
    @RequestMapping(value = "/addFile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String addFile(
            @Param(value = "{\"fileName\":\"String: name of file\", \"fileExtension:\"String: file extension\", "
                    + "\"providerServiceMapID\":\"Integer: service provider map ID\", "
                    + "userID:\"Integer: user ID of the supervisor\", "
                    + "\"validFrom\":\"Epoch date time: Document validity start time\", "
                    + "\"validUpto\":\"Epoch date time: Document validity end time\", "
                    + "\"fileContent\":\"String: base64encoded binary file content\", "
                    + "\"createdBy\":\"String: username of supervisor\", "
                    + "\"categoryID\":\"Optional: Integer: category ID selected\", "
                    + "\"subCategoryID\":\"Optional: Integer: sub-category ID selected\"}") @RequestBody String addFileRequest) {
        OutputResponse response = new OutputResponse();
        logger.info("add file request is " + addFileRequest);
        try {
            response.setResponse(kmFileManagerService.addKMFile(addFileRequest));
        } catch (Exception e) {
            logger.error("add file failed with exception: " + e.getMessage(), e);
            response.setError(e);
        }
        logger.debug("add file response is " + response.toString());
        logger.info("add file response sent with status code " + response.getStatusCode());
        return response.toString();
    }


    @Operation(summary = "Get KM file download URL")
    @RequestMapping(value = "/getKMFileDownloadURL", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getKMFileDownloadURL(@Param(value = "{}") @RequestBody String request) {
        OutputResponse response = new OutputResponse();
        logger.info("add file request is " + request);
        try {
            KMFileManager kmFileManager = objectMapper.readValue(request, KMFileManager.class);
            String s = schemeServiceImpl.getFilePath(kmFileManager);
            if (s != null)
                response.setResponse(s);
        } catch (Exception e) {
            logger.error("Error while getting file download url : " + e);
            response.setError(5000, "Error while getting file download url : " + e);
        }
        return response.toString();
    }
}
