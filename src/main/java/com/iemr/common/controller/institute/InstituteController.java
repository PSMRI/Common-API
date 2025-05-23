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
package com.iemr.common.controller.institute;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iemr.common.data.institute.Institute;
import com.iemr.common.service.institute.DesignationService;
import com.iemr.common.service.institute.InstituteService;
import com.iemr.common.service.institute.InstituteTypeService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;


@RequestMapping({"/institute"})
@RestController
public class InstituteController {
    private OutputResponse response;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private InstituteService instituteService;

    private InputMapper inputMapper = new InputMapper();

    @Autowired
    public void setInstituteService(InstituteService instituteService) {
        this.instituteService = instituteService;
    }

    private InstituteTypeService instituteTypeService;

    @Autowired
    public void setInstituteTypeService(InstituteTypeService instituteTypeService) {
        this.instituteTypeService = instituteTypeService;
    }

    private DesignationService designationService;

    @Autowired
    public void setDesignationService(DesignationService designationService) {
        this.designationService = designationService;
    }


    @Operation(summary = "Get institutes by location")
    @RequestMapping(value = "/getInstitutesByLocation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getInstitutesByLocation(@RequestBody String instituteRequest) {
        JSONObject responseObj = new JSONObject();
        OutputResponse response = new OutputResponse();
        logger.info("getInstitutesByLocation request " + instituteRequest);
        try {
            Institute instituteQuery = inputMapper.gson().fromJson(instituteRequest, Institute.class);
            responseObj.put("institute",
                    instituteService.getInstitutesByStateDistrictBranch(instituteQuery.getStateID(),
                            instituteQuery.getDistrictID(), instituteQuery.getDistrictBranchMappingID()));
            response.setResponse(responseObj.toString());
        } catch (Exception e) {
            logger.error("getInstitutesByLocation failed with error " + e.getMessage(), e);
            response.setError(e);
        }

        return response.toString();
    }


    @Operation(summary = "Get institute by branch")
    @RequestMapping(value = {
            "/getInstituteByBranch"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getInstituteByBranch(@RequestBody String instituteRequest) {
        JSONObject responseObj = new JSONObject();
        OutputResponse response = new OutputResponse();
        logger.info("getInstituteByBranch request " + instituteRequest);
        try {
            Institute instituteQuery = inputMapper.gson().fromJson(instituteRequest, Institute.class);
            responseObj.put("institute",
                    instituteService.getInstitutesByBranch(instituteQuery.getDistrictBranchMappingID()));
            response.setResponse(responseObj.toString());
        } catch (Exception e) {
            response.setError(e);
            logger.error("getInstituteByBranch failed with error " + e.getMessage(), e);
        }

        return responseObj.toString();
    }


    @Operation(summary = "Get institute type")
    @RequestMapping(value = "/getInstituteTypes", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getInstituteTypes(
            @Param(value = "{\"providerServiceMapID\": \"Optional Integer\"}") @RequestBody String instituteTypeRequest) {
        OutputResponse response = new OutputResponse();
        try {
            logger.info("getInstituteTypes request " + instituteTypeRequest);

            response.setResponse(instituteTypeService.getInstitutionTypes(instituteTypeRequest).toString());
        } catch (Exception e) {
            logger.error("getInstituteTypes failed with error " + e.getMessage(), e);
        }
        logger.info("getInstituteTypes response " + response.toString());
        return response.toString();
    }


    @Operation(summary = "Get institute name")
    @RequestMapping(value = "/getInstituteName/{institutionTypeID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getInstituteName(@PathVariable("institutionTypeID") Integer institutionTypeID) {
        OutputResponse response = new OutputResponse();
        try {
            logger.info("getInstituteTypes request " + institutionTypeID);

            response.setResponse(instituteTypeService.getInstitutionName(institutionTypeID).toString());
        } catch (Exception e) {
            logger.error("getInstituteTypes failed with error " + e.getMessage(), e);
        }
        logger.info("getInstituteTypes response " + response.toString());
        return response.toString();
    }


    @Operation(summary = "Get designation")
    @RequestMapping(value = {
            "/getDesignations"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getDesignations() {

        OutputResponse response = new OutputResponse();
        try {
            response.setResponse(designationService.getDesignations().toString());
        } catch (Exception e) {
            logger.error("getDesignations failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();

    }


    @Operation(summary = "Get institute name by type and district")
    @RequestMapping(value = "/getInstituteNameByTypeAndDistrict/{institutionTypeID}/{districtID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getInstituteNameByTypeAndDistrict(@PathVariable("institutionTypeID") Integer institutionTypeID,
                                                    @PathVariable("districtID") Integer districtID) {
        OutputResponse response = new OutputResponse();
        try {
            logger.info("getInstituteTypes request " + institutionTypeID + "," + districtID);

            response.setResponse(
                    instituteTypeService.getInstitutionNameByTypeAndDistrict(institutionTypeID, districtID).toString());
        } catch (Exception e) {
            logger.error("getInstituteTypes failed with error " + e.getMessage(), e);
        }
        logger.info("getInstituteTypes response " + response.toString());
        return response.toString();
    }
}
