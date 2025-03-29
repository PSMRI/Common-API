package com.iemr.common.controller.videocall;

import java.util.HashMap;
import java.util.Map;

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

import com.iemr.common.service.videocall.VideoCallService;

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
        logger.info("inside generate link");
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
    public ResponseEntity<Map<String, String>> sendVideoLink() {
        Map<String, String> response = new HashMap<>();
        try {
            String link = videoCallService.sendMeetingLink();
            response.put("status", "success");
            response.put("meetingLink", link);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
