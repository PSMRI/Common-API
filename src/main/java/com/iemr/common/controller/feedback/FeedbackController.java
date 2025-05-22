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
package com.iemr.common.controller.feedback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.feedback.FeedbackDetails;
import com.iemr.common.data.feedback.FeedbackLog;
import com.iemr.common.data.feedback.FeedbackResponse;
import com.iemr.common.data.feedback.FeedbackSeverity;
import com.iemr.common.data.feedback.FeedbackType;
import com.iemr.common.model.feedback.FeedbackListRequestModel;
import com.iemr.common.service.feedback.FeedbackRequestService;
import com.iemr.common.service.feedback.FeedbackResponseService;
import com.iemr.common.service.feedback.FeedbackService;
import com.iemr.common.service.feedback.FeedbackSeverityServiceImpl;
import com.iemr.common.service.feedback.FeedbackTypeService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

@RequestMapping(value = "/feedback")
@RestController
public class FeedbackController {
    InputMapper inputMapper = new InputMapper();
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private FeedbackService feedbackService;

    @Autowired
    public void setFeedbackService(FeedbackService feedbackService) {

        this.feedbackService = feedbackService;
    }

    private FeedbackTypeService feedbackTypeService;

    @Autowired
    public void setfeedbackTypeService(FeedbackTypeService feedbackTypeService) {

        this.feedbackTypeService = feedbackTypeService;
    }

    private FeedbackResponseService feedbackResponseService;

    @Autowired
    public void setFeedbackResponseService(FeedbackResponseService feedbackResponseService) {
        this.feedbackResponseService = feedbackResponseService;
    }

    private FeedbackRequestService feedbackRequestService;

    @Autowired
    public void setFeedbackRequestService(FeedbackRequestService feedbackRequestService) {
        this.feedbackRequestService = feedbackRequestService;
    }


    @Operation(summary = "Beneficiary request")
    @RequestMapping(value = "/beneficiaryRequests", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String feedbackRequest(@RequestBody String request) {
        OutputResponse response = new OutputResponse();
        try {
            FeedbackDetails feedbackDetails = inputMapper.gson().fromJson(request, FeedbackDetails.class);
            List<FeedbackDetails> feedbackList = feedbackService
                    .getFeedbackRequests(feedbackDetails.getBeneficiaryRegID());
            response.setResponse(feedbackList.toString());
        } catch (Exception e) {
            logger.error("getfeedbacklist failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Get feedback from feedback id")
    @RequestMapping(value = "/getfeedback/{feedbackID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getFeedbackByPost(@PathVariable("feedbackID") Long feedbackID) {
        OutputResponse response = new OutputResponse();
        try {
            logger.info("" + feedbackID);
            List<FeedbackDetails> savedDetails = feedbackService.getFeedbackRequests(feedbackID);
            response.setResponse(savedDetails.toString());
        } catch (Exception e) {
            logger.error("getfeedback failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Create feedback")
    @RequestMapping(value = "/createFeedback", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String createFeedback(@RequestBody String feedbackDetails) {

        OutputResponse response = new OutputResponse();
        try {
            String feedbackResponse = feedbackService.saveFeedback(feedbackDetails);
            response.setResponse(feedbackResponse);
        } catch (Exception e) {
            logger.error("createFeedback failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Feedback list")
    @RequestMapping(value = "/feedbacksList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String feedbacksList(@RequestBody String request) {

        OutputResponse response = new OutputResponse();
        try {
            FeedbackDetails feedbackDetails = inputMapper.gson().fromJson(request, FeedbackDetails.class);
            List<FeedbackDetails> feedbackList = feedbackService
                    .getFeedbackRequests(feedbackDetails.getBeneficiaryRegID());
            response.setResponse(feedbackList.toString());
        } catch (Exception e) {
            logger.error("createFeedback failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Get feedback")
    @RequestMapping(value = "/getFeedback", method = RequestMethod.POST, headers = "Authorization")
    public String getFeedback(@RequestBody String feedbackRequest) {
        OutputResponse response = new OutputResponse();
        try {
            response.setResponse(feedbackService.getAllData(feedbackRequest));
        } catch (Exception e) {
            logger.error("createFeedback failed with error " + e.getMessage(), e);
            response.setError(e);

        }
        return response.toString();
    }


    @Operation(summary = "Update feedback")
    @RequestMapping(value = "/updatefeedback", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String updateFeedback(@RequestBody String feedbackDetails) {
        OutputResponse response = new OutputResponse();
        try {
            response.setResponse(feedbackService.updateFeedback(feedbackDetails).toString());
        } catch (Exception e) {
            logger.error("updatefeedback failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Create a new beneficiary")
    @RequestMapping(value = "/updateFeedbackStatus", method = RequestMethod.POST, headers = "Authorization")
    public String updateFeedbackStatus(@RequestBody String feedbackDetails) {
        OutputResponse response = new OutputResponse();
        logger.info("updateFeedbackStatus request " + feedbackDetails);
        try {
            response.setResponse(feedbackService.updateFeedbackStatus(feedbackDetails).toString());
        } catch (Exception e) {
            logger.error("updateFeedbackStatus failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.info("updateFeedbackStatus response " + response.toString());
        return response.toString();
    }


    @Operation(summary = "Search feedback")
    @RequestMapping(value = "/searchFeedback", method = RequestMethod.POST, headers = "Authorization")
    public String searchFeedback(@RequestBody String feedbackDetails) {
        OutputResponse response = new OutputResponse();
        logger.info("searchFeedback request " + feedbackDetails);
        try {
            response.setResponse(feedbackService.searchFeedback(feedbackDetails));
        } catch (Exception e) {
            logger.error("searchFeedback failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.info("searchFeedback response " + response.toString());
        return response.toString();
    }


    @Operation(summary = "Search feedback1")
    @RequestMapping(value = "/searchFeedback1", method = RequestMethod.POST, headers = "Authorization")
    public String searchFeedback1(@RequestBody String feedbackDetails) {
        OutputResponse response = new OutputResponse();
        logger.info("searchFeedback1 request " + feedbackDetails);
        try {
            response.setResponse(feedbackService.searchFeedback1(feedbackDetails));
        } catch (Exception e) {
            logger.error("searchFeedback1 failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.info("searchFeedback1 response " + response.toString());
        return response.toString();
    }


    @Operation(summary = "Get all feedback")
    @RequestMapping(value = "/getAllFeedbackById", method = RequestMethod.POST, headers = "Authorization")
    public String getAllFeedbackById(@RequestBody String feedbackrequest) {
        OutputResponse response = new OutputResponse();
        logger.info("getAllFeedbackById request " + feedbackrequest);
        try {
            response.setResponse(feedbackRequestService.getAllFeedback(feedbackrequest));
        } catch (Exception e) {
            logger.error("getAllFeedbackById failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.info("getAllFeedbackById response " + response.toString());
        return response.toString();
    }


    @Operation(summary = "Get all feedback by id")
    @RequestMapping(value = "/getAllFeedbackById1", method = RequestMethod.POST, headers = "Authorization")
    public String getAllfeedback(@RequestBody FeedbackResponse tfeedbackresponce) {
        Map<String, Object> resMap = null;
        List<Map<String, Object>> resList = new ArrayList<>();
        ArrayList<Object[]> data2 = feedbackResponseService.getdataById(tfeedbackresponce.getFeedbackID());

        if (data2 != null && data2.size() > 0) {
            for (Object[] objList : data2) {
                resMap = new HashMap<>();
                resMap.put("ResponseSummary", objList[0]);
                resMap.put("FeedbackRequestID", objList[1]);
                resMap.put("Comments", objList[2]);
                resMap.put("AuthName", objList[3]);
                resMap.put("AuthDesignation", objList[4]);
                resMap.put("FeedbackID", objList[5]);
                resMap.put("FeedbackSupSummary", objList[6]);
                resMap.put("Feedback", objList[8]);
                resList.add(resMap);

            }
        }

        return OutputMapper.gsonWithoutExposeRestriction().toJson(resList);
    }


    @Operation(summary = "Get feedback status")
    @RequestMapping(value = "/getFeedbackStatus", method = RequestMethod.POST, headers = "Authorization")
    public String getFeedbackStatusTypes(@RequestBody String request) {
        OutputResponse response = new OutputResponse();
        try {
            response.setResponse(feedbackService.getFeedbackStatus(request));
        } catch (Exception e) {
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Get email status")
    @RequestMapping(value = "/getEmailStatus", method = RequestMethod.POST, headers = "Authorization")
    public String getEmailStatus(@RequestBody String request) {
        OutputResponse response = new OutputResponse();
        try {
            response.setResponse(feedbackService.getEmailStatus(request));
        } catch (Exception e) {
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Get feedback request by id")
    @RequestMapping(value = "/getFeedbackRequestById", method = RequestMethod.POST, headers = "Authorization")
    public String getFeedbackRequestById(@RequestBody String feedbackrequest) {
        OutputResponse response = new OutputResponse();
        logger.info("getAllFeedbackById request " + feedbackrequest);
        try {
            response.setResponse(feedbackRequestService.getAllFeedback(feedbackrequest));
        } catch (Exception e) {
            logger.error("getAllFeedbackById failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.info("getAllFeedbackById response " + response.toString());
        return response.toString();
    }


    @Operation(summary = "Get feedback response by id")
    @RequestMapping(value = "/getFeedbackResponseById", method = RequestMethod.POST, headers = "Authorization")
    public String getFeedbackResponseById(@RequestBody String feedbackrequest) {
        OutputResponse response = new OutputResponse();
        logger.info("getAllFeedbackById request " + feedbackrequest);
        try {
            response.setResponse(feedbackRequestService.getAllFeedback(feedbackrequest));
        } catch (Exception e) {
            logger.error("getAllFeedbackById failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.info("getAllFeedbackById response " + response.toString());
        return response.toString();
    }


    @Operation(summary = "Get feedback list")
    @RequestMapping(value = "/getFeedbacksList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getFeedbacksList(@RequestBody FeedbackListRequestModel feedbackDetails,
                                   HttpServletRequest httpRequest) {
        OutputResponse response = new OutputResponse();
        logger.info("getFeedbacksList request " + feedbackDetails);
        try {
            response.setResponse(
                    feedbackService.getFeedbacksList(feedbackDetails, httpRequest.getHeader("Authorization")));
        } catch (Exception e) {
            logger.error("getFeedbacksList failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.debug("getFeedbacksList response " + response.toString());
        logger.info("getFeedbacksList response code " + response.getStatusCode());
        return response.toString();

    }


    @Operation(summary = "Update response")
    @PostMapping(value = "/updateResponse", headers = "Authorization")
    public String updateResponse(@RequestBody String feedbackresponce) {
        OutputResponse response = new OutputResponse();
        logger.info("updateResponse request " + feedbackresponce);
        try {
            response.setResponse(feedbackService.updateResponse(feedbackresponce));
        } catch (Exception e) {
            logger.error("responceFeedback failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.debug("updateResponse response " + response.toString());
        logger.info("updateResponse response " + response.getStatusCode());
        return response.toString();
    }


    @Operation(summary = "Request feedback")
    @RequestMapping(value = "/requestFeedback", method = RequestMethod.POST, headers = "Authorization")
    public String requestFeedback(@RequestBody String feedbackRequest) {
        OutputResponse response = new OutputResponse();
        logger.info("requestFeedback request " + feedbackRequest);
        try {
            response.setResponse(feedbackService.createFeedbackRequest(feedbackRequest));
        } catch (Exception e) {
            logger.error("requestFeedback failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        logger.debug("requestFeedback response " + response.toString());
        logger.info("requestFeedback response " + response.getStatusCode());
        return response.toString();
    }

    FeedbackSeverityServiceImpl feedbackSeverityService;

    @Autowired
    public void setFeedbackSeverityService(FeedbackSeverityServiceImpl feedbackSeverityService) {
        this.feedbackSeverityService = feedbackSeverityService;
    }


    @Operation(summary = "Get severity")
    @RequestMapping(value = "/getSeverity", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getFeedbackSeverity(
            @Param(value = "{providerServiceMapID : Integer}") @RequestBody String severityRequest) {

        OutputResponse response = new OutputResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            FeedbackSeverity severity = objectMapper.readValue(severityRequest, FeedbackSeverity.class);
            response.setResponse(
                    feedbackSeverityService.getActiveFeedbackSeverity(severity.getProviderServiceMapID()).toString());
        } catch (Exception e) {
            logger.error("", e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Get feedback type")
    @RequestMapping(value = "/getFeedbackType", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getFeedbackType(
            @Param(value = "{providerServiceMapID : Integer}") @RequestBody String severityRequest) {

        OutputResponse response = new OutputResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            FeedbackType feedbackType = objectMapper.readValue(severityRequest, FeedbackType.class);
            response.setResponse(
                    feedbackTypeService.getActiveFeedbackTypes(feedbackType.getProviderServiceMapID()).toString());
        } catch (Exception e) {
            logger.error("", e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Get grievances by created date")
    @RequestMapping(value = "/getGrievancesByCreatedDate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getGrievancesByCreatedDate(@RequestBody FeedbackListRequestModel feedbackDetails,
                                             HttpServletRequest httpRequest) {
        OutputResponse response = new OutputResponse();
        logger.info("getGrievancesByCreatedDate request " + feedbackDetails);
        try {
            response.setResponse(feedbackService.getGrievancesByCreatedDate(feedbackDetails,
                    httpRequest.getHeader("Authorization")));
        } catch (Exception e) {
            logger.error("getGrievancesByCreatedDate failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Get grievances by updated date")
    @RequestMapping(value = "/getGrievancesByUpdatedDate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getGrievancesByUpdatedDate(@RequestBody FeedbackListRequestModel feedbackDetails,
                                             HttpServletRequest httpRequest) {
        OutputResponse response = new OutputResponse();
        logger.info("getGrievancesByUpdatedDate request " + feedbackDetails);
        try {
            response.setResponse(feedbackService.getGrievancesByUpdatedDate(feedbackDetails,
                    httpRequest.getHeader("Authorization")));
        } catch (Exception e) {
            logger.error("getGrievancesByUpdatedDate failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Save feedback request")
    @RequestMapping(value = "/saveFeedbackRequest", method = RequestMethod.POST, headers = "Authorization")
    public String createFeedbackRequest(@RequestBody String feedbackRequest) {
        OutputResponse response = new OutputResponse();
        logger.info("saveFeedbackRequest request " + feedbackRequest);
        try {
            response.setResponse(feedbackService.saveFeedbackRequest(feedbackRequest));
        } catch (Exception e) {
            logger.error("saveFeedbackRequest failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }


    @Operation(summary = "Get feedback logs")
    @RequestMapping(value = "/getFeedbackLogs", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String getFeedbackLogs(@RequestBody String request) {
        OutputResponse response = new OutputResponse();
        logger.info("getFeedbackLogs request " + request);
        try {
            FeedbackLog feedbackLogs = inputMapper.gson().fromJson(request, FeedbackLog.class);
            response.setResponse(feedbackService.getFeedbackLogs(feedbackLogs));
        } catch (Exception e) {
            logger.error("getFeedbackLogs failed with error " + e.getMessage(), e);
            response.setError(e);
        }
        return response.toString();
    }
}
