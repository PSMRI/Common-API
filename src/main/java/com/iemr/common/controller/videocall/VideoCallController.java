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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

/**
 * Public redirect endpoint hit when a beneficiary clicks the short SMS link.
 *
 * Flow:
 *   1. Jitsi host nginx receives "https://vc.piramalswasthya.org/?m=&lt;slug&gt;"
 *      and proxies/redirects it to this endpoint.
 *   2. This endpoint looks up the slug, mints a fresh Jitsi JWT bound to the
 *      room and the agent, and 302-redirects the browser to the full Jitsi URL
 *      "https://vc.piramalswasthya.org/&lt;room&gt;?jwt=&lt;token&gt;".
 *   3. The Jitsi server enforces the JWT (prosody token-auth) and admits the user.
 *
 * Intentionally NOT guarded by Authorization header - the SMS recipient is on
 * a phone browser and has no app session. Access control is the JWT itself
 * plus the slug being unguessable and the meeting row existing.
 */
@GetMapping(value = "/resolve")
public ResponseEntity<Void> resolveMeetingLink(@RequestParam("m") String slug) {
    try {
        String redirectUrl = videoCallService.resolveMeetingLink(slug);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    } catch (IllegalArgumentException e) {
        logger.warn("resolveMeetingLink rejected: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
    } catch (Exception e) {
        logger.error("resolveMeetingLink failed for slug={}: {}", slug, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

}
