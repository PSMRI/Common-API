package com.iemr.common.service.videocall;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.mapper.videocall.VideoCallMapper;
import com.iemr.common.model.videocall.UpdateCallRequest;
import com.iemr.common.model.videocall.VideoCallRequest;
import com.iemr.common.repository.videocall.VideoCallParameterRepository;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;
import org.springframework.beans.factory.annotation.Value;

@Service
public class VideoCallServiceImpl implements VideoCallService {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
    private VideoCallParameterRepository videoCallRepository;
    
	@Autowired
    private VideoCallMapper videoCallMapper;

    private String meetingLink;

    private boolean isLinkSent = false;
    private String consultationStatus = "Not Initiated";

    @Value("${video-call-url}")
    private String jitsiLink;

    public VideoCallServiceImpl() {
        // this.jitsiLink = ConfigProperties.getPropertyByName("video-call-url");
        // logger.info("Jitsi Link fetched: " + this.jitsiLink);
    }

    @Override
    public String generateMeetingLink() {
        logger.info("Jitsi Link: " + jitsiLink);
        meetingLink=jitsiLink+"m="+RandomStringUtils.randomAlphanumeric(8);
        logger.info("Meeting link: " + meetingLink);
        return meetingLink;
    }

    @Override
    public String sendMeetingLink(VideoCallRequest request) throws Exception {
    OutputResponse response = new OutputResponse();

    if (meetingLink == null || meetingLink.isEmpty()) {
        throw new Exception("Meeting link not generated yet.");
    }

    isLinkSent = true;

    VideoCallParameters videoCallEntity = videoCallMapper.videoCallToEntity(request);
    videoCallEntity.setMeetingLink(meetingLink);
    videoCallEntity.setLinkGeneratedAt(Timestamp.valueOf(LocalDateTime.now()));
    videoCallEntity.setLinkUsed(false);

    videoCallRepository.save(videoCallEntity);

    VideoCallRequest responseData = videoCallMapper.videoCallToRequest(videoCallEntity);
    response.setResponse(responseData.toJson());

    return OutputMapper.gsonWithoutExposeRestriction()
        .toJson(response) .replace("\\u003d", "=")
    .replace("\\u003c", "<")
    .replace("\\u003e", ">")
    .replace("\\u0026", "&");
    }

@Override
public String updateCallStatus(UpdateCallRequest callRequest) throws Exception {
    VideoCallParameters videoCall = null;

    VideoCallParameters requestEntity = videoCallMapper.updateRequestToVideoCall(callRequest);

    videoCall = videoCallRepository.findByMeetingLink(requestEntity.getMeetingLink());

    int updateCount = videoCallRepository.updateCallStatusByMeetingLink(
        requestEntity.getMeetingLink(),
        requestEntity.getCallStatus(),
        requestEntity.getCallDuration(),
        requestEntity.getModifiedBy()
    );

    if (updateCount > 0) {
        videoCall.setLinkUsed(true);
        videoCallRepository.save(videoCall); 
        
        //  if ("Completed".equalsIgnoreCase(requestEntity.getCallStatus())) {
        //     saveRecordingFile(videoCall.getMeetingLink());
        //  }
    } else {
        throw new Exception("Failed to update the call status");
    }

    return OutputMapper.gsonWithoutExposeRestriction()
        .toJson(videoCallMapper.videoCallToResponse(videoCall));
}
private void saveRecordingFile(String meetingLink) {
    try {
        // Configurable Jibri recording location
        String jibriOutputDir = ConfigProperties.getPropertyByName("jibri.output.path"); // e.g., /srv/jibri/recordings
        String saveDir = ConfigProperties.getPropertyByName("video.recording.path"); // e.g., /srv/recordings

        File jibriDir = new File(jibriOutputDir);
        File[] matchingFiles = jibriDir.listFiles((dir, name) -> name.contains(meetingLink) && name.endsWith(".mp4"));

        if (matchingFiles != null && matchingFiles.length > 0) {
            File recording = matchingFiles[0];
            Path targetPath = Paths.get(saveDir, meetingLink + ".mp4");

            Files.copy(recording.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Recording file saved: " + targetPath);
        } else {
            logger.warn("No matching recording file found for meeting: " + meetingLink);
        }
    } catch (IOException e) {
        logger.error("Error saving recording file: ", e);
    }
}

}


