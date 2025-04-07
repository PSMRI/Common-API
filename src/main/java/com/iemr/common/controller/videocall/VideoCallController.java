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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.model.videocall.VideoCallRequest;
import com.iemr.common.service.videocall.VideoCallService;
import com.iemr.common.utils.response.OutputResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin()
@RequestMapping(value = "/video-consultation")
public class VideoCallController {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private VideoCallService videoCallService;

    @CrossOrigin()
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

@CrossOrigin()
@PostMapping(value = "/send-link", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
public ResponseEntity<String> sendVideoLink(@RequestBody VideoCallRequest requestModel, HttpServletRequest request) {
    OutputResponse response = new OutputResponse();

    try {
        if (requestModel.getCallerPhoneNumber() == null || requestModel.getMeetingLink() == null) {
            throw new IllegalArgumentException("callerPhoneNumber and meetingLink are required");
        }

        String status = videoCallService.sendMeetingLink(requestModel);
        JSONObject responseObj = new JSONObject();
        responseObj.put("status", "success");
        responseObj.put("message", status);
        response.setResponse(responseObj.toString());

    } catch (IllegalArgumentException e) {
        logger.error("Validation error: " + e.getMessage(), e);
        return ResponseEntity.badRequest().body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
    } catch (Exception e) {
        logger.error("sendVideoLink failed with error: " + e.getMessage(), e);
        response.setError(e);
    }

    return ResponseEntity.ok(response.toString());
}

    
    @CrossOrigin()
    @PostMapping(value = "/start-call", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
    public ResponseEntity<Map<String, String>> startCall() {
        Map<String, String> response = new HashMap<>();
        try {
            String status = videoCallService.startCall();
            response.put("status", "success");
            response.put("message", status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @CrossOrigin()
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
    public ResponseEntity<Map<String, String>> getConsultationStatus() {
        Map<String, String> response = new HashMap<>();
        try {
            response.put("status", videoCallService.getConsultationStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {  
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
