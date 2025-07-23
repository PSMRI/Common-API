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

package com.iemr.common.controller.videocall;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.model.videocall.UpdateCallRequest;
import com.iemr.common.model.videocall.VideoCallRequest;
import com.iemr.common.service.videocall.VideoCallService;
import com.iemr.common.utils.response.OutputResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/video-consultation")
public class VideoCallController {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private VideoCallService videoCallService;

    @PostMapping(value = "/generate-link", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
    public ResponseEntity<Map<String, String>> generateJitsiLink() {
        Map<String, String> response = new HashMap<>();
        try {
            String link = videoCallService.generateMeetingLink();
            response.put("meetingLink", link);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

@PostMapping(value = "/send-link", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
public String sendVideoLink(@RequestBody String requestModel, HttpServletRequest request) {
    OutputResponse response = new OutputResponse();

    try {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        VideoCallRequest requestData = objectMapper.readValue(requestModel, VideoCallRequest.class);
        String serviceResponse = videoCallService.sendMeetingLink(requestData);
        
        return serviceResponse;
       
    } catch (Exception e) {
        logger.error("send MeetingLink failed with error: " + e.getMessage(), e);
        response.setError(e);
        return response.toString();
    }
}

@PostMapping(value = "/update-call-status", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
public ResponseEntity<String> updateCallStatus(@RequestBody UpdateCallRequest requestModel, HttpServletRequest request) {
    OutputResponse response = new OutputResponse();

    try {
        if (requestModel.getMeetingLink() == null || requestModel.getCallStatus() == null) {
            throw new IllegalArgumentException("Meeting Link and Status are required");
        }

        String result = videoCallService.updateCallStatus(requestModel);

        JSONObject responseObj = new JSONObject();
        responseObj.put("status", "success");
        responseObj.put("message", result);
        response.setResponse(responseObj.toString());

    } catch (IllegalArgumentException e) {
        logger.error("Validation error: " + e.getMessage(), e);
        return ResponseEntity.badRequest().body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
    } catch (Exception e) {
        logger.error("updateCallStatus failed with error: " + e.getMessage(), e);
        response.setError(e);
    }

    return ResponseEntity.ok(response.toString());
}

    
    
}
