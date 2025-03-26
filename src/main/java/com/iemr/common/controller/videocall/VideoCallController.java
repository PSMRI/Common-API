import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/video-consultation")
@CrossOrigin()
public class VideoCallController {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private VideoCallService videoCallService;

    @PostMapping(value = "/generate-link", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/send-link", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/start-call", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getConsultationStatus() {
        Map<String, String> response = new HashMap<>();
        try {
            response.put("status", videoCallService.getConsultationStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {  // Added try-catch for error handling
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
